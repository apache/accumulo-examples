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
package org.apache.accumulo.examples.shard;

import java.util.Map.Entry;

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.examples.cli.BatchWriterOpts;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.examples.cli.Help;
import org.apache.accumulo.examples.cli.ScannerOpts;
import org.apache.hadoop.io.Text;

import com.beust.jcommander.Parameter;

/**
 * The program reads an accumulo table written by {@link Index} and writes out to another table. It writes out a mapping of documents to terms. The document to
 * term mapping is used by {@link ContinuousQuery}.
 */
public class Reverse {

  static class Opts extends Help {

    @Parameter(names = "--shardTable")
    String shardTable = "shard";

    @Parameter(names = "--doc2Term")
    String doc2TermTable = "doc2Term";
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    opts.parseArgs(Reverse.class.getName(), args);

    Connector conn = Connector.builder().usingProperties("conf/accumulo-client.properties").build();

    try (Scanner scanner = conn.createScanner(opts.shardTable, Authorizations.EMPTY);
         BatchWriter bw = conn.createBatchWriter(opts.doc2TermTable)) {
      for (Entry<Key, Value> entry : scanner) {
        Key key = entry.getKey();
        Mutation m = new Mutation(key.getColumnQualifier());
        m.put(key.getColumnFamily(), new Text(), new Value(new byte[0]));
        bw.addMutation(m);
      }
    }
  }
}
