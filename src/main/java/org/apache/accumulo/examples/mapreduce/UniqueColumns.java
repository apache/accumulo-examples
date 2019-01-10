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
import java.util.HashMap;
import java.util.HashSet;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.hadoop.mapreduce.AccumuloInputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.beust.jcommander.Parameter;

/**
 * A simple map reduce job that computes the unique column families and column qualifiers in a
 * table. This example shows one way to run against an offline table.
 */
public class UniqueColumns {

  private static final Text EMPTY = new Text();

  public static class UMapper extends Mapper<Key,Value,Text,Text> {
    private Text temp = new Text();
    private static final Text CF = new Text("cf:");
    private static final Text CQ = new Text("cq:");

    @Override
    public void map(Key key, Value value, Context context)
        throws IOException, InterruptedException {
      temp.set(CF);
      ByteSequence cf = key.getColumnFamilyData();
      temp.append(cf.getBackingArray(), cf.offset(), cf.length());
      context.write(temp, EMPTY);

      temp.set(CQ);
      ByteSequence cq = key.getColumnQualifierData();
      temp.append(cq.getBackingArray(), cq.offset(), cq.length());
      context.write(temp, EMPTY);
    }
  }

  public static class UReducer extends Reducer<Text,Text,Text,Text> {
    @Override
    public void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {
      context.write(key, EMPTY);
    }
  }

  static class Opts extends ClientOpts {
    @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
    String tableName;
    @Parameter(names = "--output", description = "output directory")
    String output;
    @Parameter(names = "--reducers", description = "number of reducers to use", required = true)
    int reducers;
    @Parameter(names = "--offline", description = "run against an offline table")
    boolean offline = false;
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(UniqueColumns.class.getName(), args);

    try (AccumuloClient client = opts.createAccumuloClient()) {

      Job job = Job.getInstance(opts.getHadoopConfig());
      String jobName = UniqueColumns.class.getSimpleName() + "_" + System.currentTimeMillis();
      job.setJobName(UniqueColumns.class.getSimpleName() + "_" + System.currentTimeMillis());
      job.setJarByClass(UniqueColumns.class);
      job.setInputFormatClass(AccumuloInputFormat.class);

      String table = opts.tableName;
      if (opts.offline) {
        /*
         * this example clones the table and takes it offline. If you plan to run map reduce jobs
         * over a table many times, it may be more efficient to compact the table, clone it, and
         * then keep using the same clone as input for map reduce.
         */
        table = opts.tableName + "_" + jobName;
        client.tableOperations().clone(opts.tableName, table, true, new HashMap<>(),
            new HashSet<>());
        client.tableOperations().offline(table);
      }

      AccumuloInputFormat.configure().clientProperties(opts.getClientProperties()).table(table)
          .offlineScan(opts.offline).store(job);
      job.setMapperClass(UMapper.class);
      job.setMapOutputKeyClass(Text.class);
      job.setMapOutputValueClass(Text.class);

      job.setCombinerClass(UReducer.class);
      job.setReducerClass(UReducer.class);
      job.setNumReduceTasks(opts.reducers);
      job.setOutputFormatClass(TextOutputFormat.class);
      TextOutputFormat.setOutputPath(job, new Path(opts.output));
      job.waitForCompletion(true);
      if (opts.offline) {
        client.tableOperations().delete(table);
      }
      System.exit(job.isSuccessful() ? 0 : 1);
    }
    System.exit(1);
  }
}
