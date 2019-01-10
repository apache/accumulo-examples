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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.hadoop.mapreduce.AccumuloOutputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

/**
 * Map job to ingest n-gram files from
 * http://storage.googleapis.com/books/ngrams/books/datasetsv2.html
 */
public class NGramIngest {

  private static final Logger log = LoggerFactory.getLogger(NGramIngest.class);

  static class Opts extends ClientOpts {
    @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
    String tableName;
    @Parameter(names = {"-i", "--input"}, required = true, description = "HDFS input directory")
    String inputDirectory;
  }

  static class NGramMapper extends Mapper<LongWritable,Text,Text,Mutation> {

    @Override
    protected void map(LongWritable location, Text value, Context context)
        throws IOException, InterruptedException {
      String parts[] = value.toString().split("\\t");
      if (parts.length >= 4) {
        Mutation m = new Mutation(parts[0]);
        m.put(parts[1], String.format("%010d", Long.parseLong(parts[2])),
            new Value(parts[3].trim().getBytes()));
        context.write(null, m);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(NGramIngest.class.getName(), args);

    Job job = Job.getInstance(opts.getHadoopConfig());
    job.setJobName(NGramIngest.class.getSimpleName());
    job.setJarByClass(NGramIngest.class);

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(AccumuloOutputFormat.class);
    AccumuloOutputFormat.configure().clientProperties(opts.getClientProperties())
        .defaultTable(opts.tableName).store(job);

    job.setMapperClass(NGramMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Mutation.class);

    job.setNumReduceTasks(0);
    job.setSpeculativeExecution(false);

    try (AccumuloClient client = opts.createAccumuloClient()) {
      if (!client.tableOperations().exists(opts.tableName)) {
        log.info("Creating table " + opts.tableName);
        client.tableOperations().create(opts.tableName);
        SortedSet<Text> splits = new TreeSet<>();
        String numbers[] = "1 2 3 4 5 6 7 8 9".split("\\s");
        String lower[] = "a b c d e f g h i j k l m n o p q r s t u v w x y z".split("\\s");
        String upper[] = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z".split("\\s");
        for (String[] array : new String[][] {numbers, lower, upper}) {
          for (String s : array) {
            splits.add(new Text(s));
          }
        }
        client.tableOperations().addSplits(opts.tableName, splits);
      }
    }

    TextInputFormat.addInputPath(job, new Path(opts.inputDirectory));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
