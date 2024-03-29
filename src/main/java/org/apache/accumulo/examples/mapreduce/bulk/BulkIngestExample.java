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
package org.apache.accumulo.examples.mapreduce.bulk;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Base64;
import java.util.Collection;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.hadoop.mapreduce.AccumuloFileOutputFormat;
import org.apache.accumulo.hadoop.mapreduce.partition.RangePartitioner;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/**
 * Example map reduce job that bulk ingest data into an accumulo table. The expected input is text
 * files containing tab separated key value pairs on each line.
 */
public final class BulkIngestExample {

  static final String workDir = "tmp/bulkWork";
  static final String inputDir = "bulk";
  static final String outputFile = "bulk/test_1.txt";
  static final int numRows = 1000;

  static final String SLASH_FILES = "/files";
  static final String FAILURES = "failures";
  static final String SPLITS_TXT = "/splits.txt";

  private BulkIngestExample() {}

  public static class MapClass extends Mapper<LongWritable,Text,Text,Text> {
    private final Text outputKey = new Text();
    private final Text outputValue = new Text();

    @Override
    public void map(LongWritable key, Text value, Context output)
        throws IOException, InterruptedException {
      // split on tab
      int index = -1;
      for (int i = 0; i < value.getLength(); i++) {
        if (value.getBytes()[i] == '\t') {
          index = i;
          break;
        }
      }

      if (index > 0) {
        outputKey.set(value.getBytes(), 0, index);
        outputValue.set(value.getBytes(), index + 1, value.getLength() - (index + 1));
        output.write(outputKey, outputValue);
      }
    }
  }

  public static class ReduceClass extends Reducer<Text,Text,Key,Value> {
    @Override
    public void reduce(Text key, Iterable<Text> values, Context output)
        throws IOException, InterruptedException {
      // be careful with the timestamp... if you run on a cluster
      // where the time is whacked you may not see your updates in
      // accumulo if there is already an existing value with a later
      // timestamp in accumulo... so make sure ntp is running on the
      // cluster or consider using logical time... one options is
      // to let accumulo set the time
      long timestamp = System.currentTimeMillis();

      int index = 0;
      for (Text value : values) {
        Key outputKey = new Key(key, new Text("colf"), new Text(String.format("col_%07d", index)),
            timestamp);
        index++;

        Value outputValue = new Value(value.getBytes(), 0, value.getLength());
        output.write(outputKey, outputValue);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    ClientOpts opts = new ClientOpts();
    opts.parseArgs(BulkIngestExample.class.getName(), args);
    FileSystem fs = FileSystem.get(opts.getHadoopConfig());

    generateTestData(fs);
    ingestTestData(fs, opts);
  }

  private static void generateTestData(FileSystem fs) throws IOException {
    try (PrintStream out = new PrintStream(
        new BufferedOutputStream(fs.create(new Path(outputFile))))) {
      for (int i = 0; i < numRows; i++) {
        out.printf("row_%010d\tvalue_%010d%n", i, i);
      }
    }
  }

  private static int ingestTestData(FileSystem fs, ClientOpts opts) throws Exception {
    Job job = Job.getInstance(opts.getHadoopConfig());
    job.setJobName(BulkIngestExample.class.getSimpleName());
    job.setJarByClass(BulkIngestExample.class);

    job.setInputFormatClass(TextInputFormat.class);

    job.setMapperClass(MapClass.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);

    job.setReducerClass(ReduceClass.class);
    job.setOutputFormatClass(AccumuloFileOutputFormat.class);

    TextInputFormat.setInputPaths(job, new Path(inputDir));
    AccumuloFileOutputFormat.configure().outputPath(new Path(workDir + SLASH_FILES)).store(job);

    try (AccumuloClient client = opts.createAccumuloClient()) {

      try (PrintStream out = new PrintStream(
          new BufferedOutputStream(fs.create(new Path(workDir + SPLITS_TXT))))) {
        Collection<Text> splits = client.tableOperations().listSplits(SetupTable.BULK_INGEST_TABLE,
            100);
        for (Text split : splits)
          out.println(Base64.getEncoder().encodeToString(split.copyBytes()));
        job.setNumReduceTasks(splits.size() + 1);
      }

      job.setPartitionerClass(RangePartitioner.class);
      RangePartitioner.setSplitFile(job, workDir + SPLITS_TXT);

      job.waitForCompletion(true);
      Path failures = new Path(workDir, FAILURES);
      fs.delete(failures, true);
      fs.mkdirs(new Path(workDir, FAILURES));
      // With HDFS permissions on, we need to make sure the Accumulo user can read/move the rfiles
      FsShell fsShell = new FsShell(opts.getHadoopConfig());
      fsShell.run(new String[] {"-chmod", "-R", "777", workDir});
      System.err.println("Importing Directory '" + workDir + SLASH_FILES + "' to table '"
          + SetupTable.BULK_INGEST_TABLE + "'");
      client.tableOperations().importDirectory(workDir + SLASH_FILES)
          .to(SetupTable.BULK_INGEST_TABLE).load();
    }
    return job.isSuccessful() ? 0 : 1;
  }
}
