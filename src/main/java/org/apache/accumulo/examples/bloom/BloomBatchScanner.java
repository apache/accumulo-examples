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

import static org.apache.accumulo.examples.client.RandomBatchWriter.abs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

/**
 * Simple example for reading random batches of data from Accumulo.
 */
public class BloomBatchScanner {

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();

    scan(connector, "bloom_test1", 7);
    scan(connector, "bloom_test2", 7);
  }

  static void scan(Connector connector, String tableName, int seed) throws TableNotFoundException {
    Random r = new Random(seed);
    HashSet<Range> ranges = new HashSet<>();
    HashMap<String,Boolean> expectedRows = new HashMap<>();
    while (ranges.size() < 500) {
      long rowId = abs(r.nextLong()) % 1_000_000_000;
      String row = String.format("row_%010d", rowId);
      ranges.add(new Range(row));
      expectedRows.put(row, false);
    }

    long t1 = System.currentTimeMillis();
    long results = 0;
    long lookups = ranges.size();

    System.out.println("Scanning " + tableName + " with seed " + seed);
    try (BatchScanner scan = connector.createBatchScanner(tableName, Authorizations.EMPTY, 20)) {
      scan.setRanges(ranges);
      for (Entry<Key, Value> entry : scan) {
        Key key = entry.getKey();
        if (!expectedRows.containsKey(key.getRow().toString())) {
          System.out.println("Encountered unexpected key: " + key);
        } else {
          expectedRows.put(key.getRow().toString(), true);
        }
        results++;
      }
    }

    long t2 = System.currentTimeMillis();
    System.out.println(String.format("Scan finished! %6.2f lookups/sec, %.2f secs, %d results",
            lookups / ((t2 - t1) / 1000.0), ((t2 - t1) / 1000.0), results));

    int count = 0;
    for (Entry<String,Boolean> entry : expectedRows.entrySet()) {
      if (!entry.getValue()) {
        count++;
      }
    }
    if (count > 0)
      System.out.println("Did not find " + count);
    else
      System.out.println("All expected rows were scanned");
  }
}
