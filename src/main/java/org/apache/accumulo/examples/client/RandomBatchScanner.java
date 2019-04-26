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
package org.apache.accumulo.examples.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.accumulo.examples.client.RandomBatchWriter.abs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple example for reading random batches of data from Accumulo.
 */
public class RandomBatchScanner {

  private static final Logger log = LoggerFactory.getLogger(RandomBatchScanner.class);

  public static void main(String[] args)
      throws AccumuloException, AccumuloSecurityException, TableNotFoundException {

    ClientOpts opts = new ClientOpts();
    opts.parseArgs(RandomBatchScanner.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {

      try {
        client.tableOperations().create("batch");
      } catch (TableExistsException e) {
        // ignore
      }

      int totalLookups = 1000;
      int totalEntries = 10000;
      Random r = new Random();
      HashSet<Range> ranges = new HashSet<>();
      HashMap<String,Boolean> expectedRows = new HashMap<>();
      log.info("Generating {} random ranges for BatchScanner to read", totalLookups);
      while (ranges.size() < totalLookups) {
        long rowId = abs(r.nextLong()) % totalEntries;
        String row = String.format("row_%010d", rowId);
        ranges.add(new Range(row));
        expectedRows.put(row, false);
      }

      long t1 = System.currentTimeMillis();
      long lookups = 0;

      log.info("Reading ranges using BatchScanner");
      try (BatchScanner scan = client.createBatchScanner("batch", Authorizations.EMPTY, 20)) {
        scan.setRanges(ranges);
        for (Entry<Key,Value> entry : scan) {
          Key key = entry.getKey();
          Value value = entry.getValue();
          String row = key.getRow().toString();
          long rowId = Integer.parseInt(row.split("_")[1]);

          Value expectedValue = SequentialBatchWriter.createValue(rowId, 50);

          if (!Arrays.equals(expectedValue.get(), value.get())) {
            log.error("Unexpected value for key: {} expected: {} actual: {}", key,
                new String(expectedValue.get(), UTF_8), new String(value.get(), UTF_8));
          }

          if (!expectedRows.containsKey(key.getRow().toString())) {
            log.error("Encountered unexpected key: {} ", key);
          } else {
            expectedRows.put(key.getRow().toString(), true);
          }

          lookups++;
          if (lookups % 100 == 0) {
            log.trace("{} lookups", lookups);
          }
        }
      }

      long t2 = System.currentTimeMillis();
      log.info(String.format("Scan finished! %6.2f lookups/sec, %.2f secs, %d results",
          lookups / ((t2 - t1) / 1000.0), ((t2 - t1) / 1000.0), lookups));

      int count = 0;
      for (Entry<String,Boolean> entry : expectedRows.entrySet()) {
        if (!entry.getValue()) {
          count++;
        }
      }
      if (count > 0) {
        log.warn("Did not find {} rows", count);
        System.exit(1);
      }
      log.info("All expected rows were scanned");
    }
  }
}
