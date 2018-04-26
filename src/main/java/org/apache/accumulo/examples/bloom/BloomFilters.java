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
package org.apache.accumulo.examples.bloom;

import java.util.Random;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.examples.client.RandomBatchWriter;

public class BloomFilters {

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();
    try {
      System.out.println("Creating bloom_test1 and bloom_test2");
      connector.tableOperations().create("bloom_test1");
      connector.tableOperations().setProperty("bloom_test1", "table.compaction.major.ratio", "7");
      connector.tableOperations().create("bloom_test2");
      connector.tableOperations().setProperty("bloom_test2", "table.bloom.enabled", "true");
      connector.tableOperations().setProperty("bloom_test2", "table.compaction.major.ratio", "7");
    } catch (TableExistsException e) {
      // ignore
    }

    // Write a million rows 3 times flushing files to disk separately
    System.out.println("Writing data to bloom_test1");
    writeData(connector, "bloom_test1", 7);
    connector.tableOperations().flush("bloom_test1", null, null, true);
    writeData(connector, "bloom_test1", 8);
    connector.tableOperations().flush("bloom_test1", null, null, true);
    writeData(connector, "bloom_test1", 9);
    connector.tableOperations().flush("bloom_test1", null, null, true);

    System.out.println("Writing data to bloom_test2");
    writeData(connector, "bloom_test2", 7);
    connector.tableOperations().flush("bloom_test2", null, null, true);
    writeData(connector, "bloom_test2", 8);
    connector.tableOperations().flush("bloom_test2", null, null, true);
    writeData(connector, "bloom_test2", 9);
    connector.tableOperations().flush("bloom_test2", null, null, true);
  }

  // write a million random rows
  static void writeData(Connector connector, String tableName, int seed) throws TableNotFoundException,
        MutationsRejectedException{
    Random r = new Random(seed);
    try (BatchWriter bw = connector.createBatchWriter(tableName)) {
      for (int x = 0; x < 1_000_000; x++) {
        Long rowId = RandomBatchWriter.abs(r.nextLong()) % 1_000_000_000;
        Mutation m = RandomBatchWriter.createMutation(rowId, 50, new ColumnVisibility());
        bw.addMutation(m);
      }
    }
  }
}
