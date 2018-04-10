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
import java.io.PrintStream;
import java.util.TreeSet;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;


public class SetupTable {

  static String[] splits = {"row_00000333", "row_00000666"};
  static String tableName = "test_bulk";
  static int numRows = 1000;
  static String outputFile = "bulk/test_1.txt";

  public static void main(String[] args) throws Exception {
    Connector conn = Connector.builder().usingProperties("conf/accumulo-client.properties").build();
    try {
      conn.tableOperations().create(tableName);
    } catch (TableExistsException e) {
      //ignore
    }

    // create a table with initial partitions
    TreeSet<Text> intialPartitions = new TreeSet<>();
    for (String split : splits) {
      intialPartitions.add(new Text(split));
    }
    conn.tableOperations().addSplits(tableName, intialPartitions);

    FileSystem fs = FileSystem.get(new Configuration());
    try (PrintStream out = new PrintStream(new BufferedOutputStream(fs.create(new Path(outputFile))))) {
      // create some data in outputFile
      for (int i = 0; i < numRows; i++) {
        out.println(String.format("row_%010d\tvalue_%010d", i, i));
      }
    }
  }
}
