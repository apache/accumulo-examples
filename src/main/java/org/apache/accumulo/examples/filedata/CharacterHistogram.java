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
package org.apache.accumulo.examples.filedata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.SummingArrayCombiner;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.hadoop.mapreduce.AccumuloOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import com.beust.jcommander.Parameter;

/**
 * A MapReduce that computes a histogram of byte frequency for each file and stores the histogram
 * alongside the file data. The {@link ChunkInputFormat} is used to read the file data from
 * Accumulo.
 */
public class CharacterHistogram {

  private static final String VIS = "vis";

  public static class HistMapper extends Mapper<List<Entry<Key,Value>>,InputStream,Text,Mutation> {
    private ColumnVisibility cv;

    @Override
    public void map(List<Entry<Key,Value>> k, InputStream v, Context context)
        throws IOException, InterruptedException {
      Long[] hist = new Long[256];
      for (int i = 0; i < hist.length; i++)
        hist[i] = 0L;
      int b = v.read();
      while (b >= 0) {
        hist[b] += 1L;
        b = v.read();
      }
      v.close();
      Mutation m = new Mutation(k.get(0).getKey().getRow());
      m.put("info", "hist", cv,
          new Value(SummingArrayCombiner.STRING_ARRAY_ENCODER.encode(Arrays.asList(hist))));
      context.write(new Text(), m);
    }

    @Override
    protected void setup(Context context) {
      cv = new ColumnVisibility(context.getConfiguration().get(VIS, ""));
    }
  }

  static class Opts extends ClientOpts {
    @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
    String tableName;
    @Parameter(names = "--vis")
    String visibilities = "";
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(CharacterHistogram.class.getName(), args);

    Job job = Job.getInstance(opts.getHadoopConfig());
    job.setJobName(CharacterHistogram.class.getSimpleName());
    job.setJarByClass(CharacterHistogram.class);
    job.setInputFormatClass(ChunkInputFormat.class);
    job.getConfiguration().set(VIS, opts.visibilities);
    job.setMapperClass(HistMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Mutation.class);

    job.setNumReduceTasks(0);

    job.setOutputFormatClass(AccumuloOutputFormat.class);
    AccumuloOutputFormat.configure().clientProperties(opts.getClientProperties())
        .defaultTable(opts.tableName).createTables(true);

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
