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

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.hadoop.mapreduce.AccumuloInputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

public class RegexExample {

  private static final Logger log = LoggerFactory.getLogger(RegexExample.class);

  public static class RegexMapper extends Mapper<Key,Value,Key,Value> {
    @Override
    public void map(Key row, Value data, Context context) throws IOException, InterruptedException {
      context.write(row, data);
    }
  }

  static class Opts extends ClientOpts {
    @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
    String tableName;
    @Parameter(names = "--rowRegex")
    String rowRegex;
    @Parameter(names = "--columnFamilyRegex")
    String columnFamilyRegex;
    @Parameter(names = "--columnQualifierRegex")
    String columnQualifierRegex;
    @Parameter(names = "--valueRegex")
    String valueRegex;
    @Parameter(names = "--output", required = true)
    String destination;
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(RegexExample.class.getName(), args);

    Job job = Job.getInstance(opts.getHadoopConfig());
    job.setJobName(RegexExample.class.getSimpleName());
    job.setJarByClass(RegexExample.class);

    job.setInputFormatClass(AccumuloInputFormat.class);

    IteratorSetting regex = new IteratorSetting(50, "regex", RegExFilter.class);
    RegExFilter.setRegexs(regex, opts.rowRegex, opts.columnFamilyRegex, opts.columnQualifierRegex,
        opts.valueRegex, false);

    AccumuloInputFormat.configure().clientProperties(opts.getClientProperties())
        .table(opts.tableName).addIterator(regex).store(job);

    job.setMapperClass(RegexMapper.class);
    job.setMapOutputKeyClass(Key.class);
    job.setMapOutputValueClass(Value.class);
    job.setNumReduceTasks(0);
    job.setOutputFormatClass(TextOutputFormat.class);
    TextOutputFormat.setOutputPath(job, new Path(opts.destination));

    log.info("setRowRegex: " + opts.rowRegex);
    log.info("setColumnFamilyRegex: " + opts.columnFamilyRegex);
    log.info("setColumnQualifierRegex: " + opts.columnQualifierRegex);
    log.info("setValueRegex: " + opts.valueRegex);

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
