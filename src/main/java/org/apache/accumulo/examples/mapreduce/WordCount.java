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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.iterators.user.SummingCombiner;
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
 * A simple MapReduce job that inserts word counts into Accumulo. See docs/mapred.md
 */
public class WordCount {

  private static final Logger log = LoggerFactory.getLogger(WordCount.class);

  static class Opts extends ClientOpts {
    @Parameter(names = {"-t", "--table"}, description = "Name of output Accumulo table")
    String tableName = "wordCount";
    @Parameter(names = {"-i", "--input"}, required = true, description = "HDFS input directory")
    String inputDirectory;
    @Parameter(names = {"-d", "--dfsPath"},
        description = "HDFS Path where accumulo-client.properties exists")
    String hdfsPath;
  }

  public static class MapClass extends Mapper<LongWritable,Text,Text,Mutation> {
    @Override
    public void map(LongWritable key, Text value, Context output) throws IOException {
      String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
      String[] words = value.toString().split("\\s+");

      for (String word : words) {
        Mutation mutation = new Mutation(word);
        mutation.at().family("count").qualifier(today).put("1");

        try {
          output.write(null, mutation);
        } catch (InterruptedException e) {
          log.error("Could not write mutation to Context.", e);
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(WordCount.class.getName(), args);

    // Create Accumulo table and attach Summing iterator
    try (AccumuloClient client = opts.createAccumuloClient()) {
      client.tableOperations().create(opts.tableName);
      IteratorSetting is = new IteratorSetting(10, SummingCombiner.class);
      SummingCombiner.setColumns(is,
          Collections.singletonList(new IteratorSetting.Column("count")));
      SummingCombiner.setEncodingType(is, SummingCombiner.Type.STRING);
      client.tableOperations().attachIterator(opts.tableName, is);
    } catch (TableExistsException e) {
      // ignore
    }

    // Create M/R job
    Job job = Job.getInstance(opts.getHadoopConfig());
    job.setJobName(WordCount.class.getName());
    job.setJarByClass(WordCount.class);
    job.setInputFormatClass(TextInputFormat.class);
    TextInputFormat.setInputPaths(job, new Path(opts.inputDirectory));

    job.setMapperClass(MapClass.class);
    job.setNumReduceTasks(0);
    job.setOutputFormatClass(AccumuloOutputFormat.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Mutation.class);

    if (opts.hdfsPath != null) {
      AccumuloOutputFormat.configure().clientPropertiesPath(opts.hdfsPath)
          .defaultTable(opts.tableName).store(job);
    } else {
      AccumuloOutputFormat.configure().clientProperties(opts.getClientProperties())
          .defaultTable(opts.tableName).store(job);
    }
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
