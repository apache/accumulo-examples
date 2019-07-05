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
package org.apache.accumulo.examples.mapreduce;

import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.examples.util.FormatUtil;
import org.apache.accumulo.hadoop.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.hadoop.mapreduce.InputFormatBuilder;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.beust.jcommander.Parameter;

/**
 * Takes a table and outputs the specified column to a set of part files on hdfs
 */
public class TableToFile {

  static class Opts extends ClientOpts {
    @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
    String tableName;
    @Parameter(names = "--output", required = true, description = "output directory")
    String output;
    @Parameter(names = "--columns", description = "columns to extract, in cf:cq{,cf:cq,...} form")
    String columns = "";
  }

  /**
   * The Mapper class that given a row number, will generate the appropriate output line.
   */
  public static class TTFMapper extends Mapper<Key,Value,NullWritable,Text> {
    @Override
    public void map(Key row, Value data, Context context) throws IOException, InterruptedException {
      Map.Entry<Key,Value> entry = new SimpleImmutableEntry<>(row, data);
      context.write(NullWritable.get(), new Text(FormatUtil.formatTableEntry(entry, false)));
      context.setStatus("Outputed Value");
    }
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(TableToFile.class.getName(), args);

    List<IteratorSetting.Column> columnsToFetch = new ArrayList<>();
    for (String col : opts.columns.split(",")) {
      int idx = col.indexOf(":");
      String cf = idx < 0 ? col : col.substring(0, idx);
      String cq = idx < 0 ? null : col.substring(idx + 1);
      if (!cf.isEmpty())
        columnsToFetch.add(new IteratorSetting.Column(cf, cq));
    }

    Job job = Job.getInstance(opts.getHadoopConfig());
    job.setJobName(TableToFile.class.getSimpleName() + "_" + System.currentTimeMillis());
    job.setJarByClass(TableToFile.class);
    job.setInputFormatClass(AccumuloInputFormat.class);
    InputFormatBuilder.InputFormatOptions<Job> inputOpts = AccumuloInputFormat.configure()
        .clientProperties(opts.getClientProperties()).table(opts.tableName);
    if (!columnsToFetch.isEmpty()) {
      inputOpts.fetchColumns(columnsToFetch);
    }
    inputOpts.store(job);
    job.setMapperClass(TTFMapper.class);
    job.setMapOutputKeyClass(NullWritable.class);
    job.setMapOutputValueClass(Text.class);
    job.setNumReduceTasks(0);
    job.setOutputFormatClass(TextOutputFormat.class);
    TextOutputFormat.setOutputPath(job, new Path(opts.output));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
