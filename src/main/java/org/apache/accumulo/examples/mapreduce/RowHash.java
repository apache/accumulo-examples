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
import java.util.Base64;
import java.util.Collections;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.hadoop.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.hadoop.mapreduce.AccumuloOutputFormat;
import org.apache.accumulo.hadoop.mapreduce.InputFormatBuilder;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import com.beust.jcommander.Parameter;

public class RowHash {

  /**
   * The Mapper class that given a row number, will generate the appropriate output line.
   */
  public static class HashDataMapper extends Mapper<Key,Value,Text,Mutation> {
    @Override
    public void map(Key row, Value data, Context context) throws IOException, InterruptedException {
      Mutation m = new Mutation(row.getRow());
      m.put("cf-HASHTYPE", "cq-MD5BASE64",
          new Value(Base64.getEncoder().encode(MD5Hash.digest(data.toString()).getDigest())));
      context.write(null, m);
      context.progress();
    }

    @Override
    public void setup(Context job) {}
  }

  private static class Opts extends ClientOpts {
    @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
    String tableName;
    @Parameter(names = "--column", required = true)
    String column;
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(RowHash.class.getName(), args);

    Job job = Job.getInstance(opts.getHadoopConfig());
    job.setJobName(RowHash.class.getName());
    job.setJarByClass(RowHash.class);
    job.setInputFormatClass(AccumuloInputFormat.class);
    InputFormatBuilder.InputFormatOptions<Job> inputOpts = AccumuloInputFormat.configure()
        .clientProperties(opts.getClientProperties()).table(opts.tableName);

    String col = opts.column;
    int idx = col.indexOf(":");
    String cf = idx < 0 ? col : col.substring(0, idx);
    String cq = idx < 0 ? null : col.substring(idx + 1);
    if (cf.length() > 0) {
      inputOpts.fetchColumns(Collections.singleton(new IteratorSetting.Column(cf, cq)));
    }
    inputOpts.store(job);

    job.setMapperClass(HashDataMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Mutation.class);
    job.setNumReduceTasks(0);

    job.setOutputFormatClass(AccumuloOutputFormat.class);
    AccumuloOutputFormat.configure().clientProperties(opts.getClientProperties())
        .defaultTable(opts.tableName).store(job);

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
