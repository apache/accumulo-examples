/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.examples.dirlist;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.admin.NewTableConfiguration;
import org.apache.accumulo.core.client.lexicoder.Encoder;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.LongCombiner;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.examples.Common;
import org.apache.accumulo.examples.cli.BatchWriterOpts;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.examples.filedata.ChunkCombiner;
import org.apache.accumulo.examples.filedata.FileDataIngest;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

/**
 * Recursively lists the files and directories under a given path, ingests their names and file info
 * into one Accumulo table, indexes the file names in a separate table, and the file data into a
 * third table.
 */
public final class Ingest {

  private static final Logger log = LoggerFactory.getLogger(Ingest.class);

  static final String DIR_TABLE = Common.NAMESPACE + ".dirTable";
  static final String INDEX_TABLE = Common.NAMESPACE + ".indexTable";
  static final String DATA_TABLE = Common.NAMESPACE + ".dataTable";

  static final Value nullValue = new Value(new byte[0]);
  public static final String LENGTH_CQ = "length";
  public static final String HIDDEN_CQ = "hidden";
  public static final String EXEC_CQ = "exec";
  public static final String LASTMOD_CQ = "lastmod";
  public static final String HASH_CQ = "md5";
  public static final Encoder<Long> encoder = LongCombiner.FIXED_LEN_ENCODER;

  private Ingest() {}

  public static Mutation buildMutation(ColumnVisibility cv, String path, boolean isDir,
      boolean isHidden, boolean canExec, long length, long lastmod, String hash) {
    if (path.equals("/"))
      path = "";
    Mutation m = new Mutation(QueryUtil.getRow(path));
    String colf;
    if (isDir)
      colf = QueryUtil.DIR_COLF.toString();
    else
      colf = new String(encoder.encode(Long.MAX_VALUE - lastmod), UTF_8);
    m.put(colf, LENGTH_CQ, cv, new Value(Long.toString(length).getBytes()));
    m.put(colf, HIDDEN_CQ, cv, new Value(Boolean.toString(isHidden).getBytes()));
    m.put(colf, EXEC_CQ, cv, new Value(Boolean.toString(canExec).getBytes()));
    m.put(colf, LASTMOD_CQ, cv, new Value(Long.toString(lastmod).getBytes()));
    if (hash != null && hash.length() > 0)
      m.put(colf, HASH_CQ, cv, new Value(hash.getBytes()));
    return m;
  }

  private static void ingest(File src, ColumnVisibility cv, BatchWriter dirBW, BatchWriter indexBW,
      FileDataIngest fdi, BatchWriter data) throws Exception {
    // build main table entry
    String path;
    try {
      path = src.getCanonicalPath();
    } catch (IOException e) {
      path = src.getAbsolutePath();
    }
    log.info(path);

    String hash = null;
    if (!src.isDirectory()) {
      try {
        hash = fdi.insertFileData(path, data);
      } catch (Exception e) {
        // if something goes wrong, just skip this one
        return;
      }
    }

    dirBW.addMutation(buildMutation(cv, path, src.isDirectory(), src.isHidden(), src.canExecute(),
        src.length(), src.lastModified(), hash));

    // build index table entries
    Text row = QueryUtil.getForwardIndex(path);
    if (row != null) {
      Text p = new Text(QueryUtil.getRow(path));
      Mutation m = new Mutation(row);
      m.put(QueryUtil.INDEX_COLF, p, cv, nullValue);
      indexBW.addMutation(m);

      row = QueryUtil.getReverseIndex(path);
      m = new Mutation(row);
      m.put(QueryUtil.INDEX_COLF, p, cv, nullValue);
      indexBW.addMutation(m);
    }
  }

  private static void recurse(File src, ColumnVisibility cv, BatchWriter dirBW, BatchWriter indexBW,
      FileDataIngest fdi, BatchWriter data) throws Exception {
    // ingest this File
    ingest(src, cv, dirBW, indexBW, fdi, data);
    // recurse into subdirectories
    if (src.isDirectory()) {
      File[] files = src.listFiles();
      if (files == null)
        return;
      for (File child : files) {
        recurse(child, cv, dirBW, indexBW, fdi, data);
      }
    }
  }

  static class Opts extends ClientOpts {
    @Parameter(names = "--dirTable", description = "a table to hold the directory information")
    String dirTable = DIR_TABLE;
    @Parameter(names = "--indexTable", description = "an index over the ingested data")
    String indexTable = INDEX_TABLE;
    @Parameter(names = "--dataTable", description = "the file data, chunked into parts")
    String dataTable = DATA_TABLE;
    @Parameter(names = "--vis", description = "the visibility to mark the data",
        converter = VisibilityConverter.class)
    ColumnVisibility visibility = new ColumnVisibility();
    @Parameter(names = "--chunkSize", description = "the size of chunks when breaking down files")
    int chunkSize = 100000;
    @Parameter(description = "<dir> { <dir> ... }")
    List<String> directories = new ArrayList<>();
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    BatchWriterOpts bwOpts = new BatchWriterOpts();
    opts.parseArgs(Ingest.class.getName(), args, bwOpts);

    try (AccumuloClient client = opts.createAccumuloClient()) {
      var ntc = new NewTableConfiguration()
          .attachIterator(new IteratorSetting(1, ChunkCombiner.class));

      Common.createTableWithNamespace(client, opts.dirTable);
      Common.createTableWithNamespace(client, opts.indexTable);
      Common.createTableWithNamespace(client, opts.dataTable, ntc);

      BatchWriter dirBW = client.createBatchWriter(opts.dirTable, bwOpts.getBatchWriterConfig());
      BatchWriter indexBW = client.createBatchWriter(opts.indexTable,
          bwOpts.getBatchWriterConfig());
      BatchWriter dataBW = client.createBatchWriter(opts.dataTable, bwOpts.getBatchWriterConfig());
      FileDataIngest fdi = new FileDataIngest(opts.chunkSize, opts.visibility);
      for (String dir : opts.directories) {
        recurse(new File(dir), opts.visibility, dirBW, indexBW, fdi, dataBW);

        // fill in parent directory info
        int slashIndex;
        while ((slashIndex = dir.lastIndexOf('/')) > 0) {
          dir = dir.substring(0, slashIndex);
          ingest(new File(dir), opts.visibility, dirBW, indexBW, fdi, dataBW);
        }
      }
      ingest(new File("/"), opts.visibility, dirBW, indexBW, fdi, dataBW);

      dirBW.close();
      indexBW.close();
      dataBW.close();
    }
  }
}
