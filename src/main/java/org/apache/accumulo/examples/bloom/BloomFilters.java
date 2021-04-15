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

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.NamespaceExistsException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.examples.client.RandomBatchWriter;
import org.apache.accumulo.examples.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BloomFilters {

  private static final Logger log = LoggerFactory.getLogger(BloomFilters.class);

  private BloomFilters() {}

  public static void main(String[] args)
      throws AccumuloException, AccumuloSecurityException, TableNotFoundException {

    ClientOpts opts = new ClientOpts();
    opts.parseArgs(BloomFilters.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      try {
        client.namespaceOperations().create(Constants.NAMESPACE);
      } catch (NamespaceExistsException e) {
        log.info(Constants.NAMESPACE_EXISTS_MSG + Constants.NAMESPACE);
      }
      createTableAndSetCompactionRatio(client, BloomCommon.BLOOM_TEST1_TABLE);
      createTableAndSetCompactionRatio(client, BloomCommon.BLOOM_TEST2_TABLE);
      client.tableOperations().setProperty(BloomCommon.BLOOM_TEST2_TABLE,
          BloomCommon.BLOOM_ENABLED_PROPERTY, "true");
      writeAndFlushData(BloomCommon.BLOOM_TEST1_TABLE, client);
      writeAndFlushData(BloomCommon.BLOOM_TEST2_TABLE, client);
    }
  }

  private static void createTableAndSetCompactionRatio(AccumuloClient client,
      final String tableName) throws AccumuloException, AccumuloSecurityException {
    try {
      log.info("Creating {}", tableName);
      client.tableOperations().create(tableName);
      client.tableOperations().setProperty(tableName, "table.compaction.major.ratio", "7");
    } catch (TableExistsException e) {
      log.warn(Constants.TABLE_EXISTS_MSG + "tableName");
    }
  }

  // Write a million rows 3 times flushing files to disk separately
  private static void writeAndFlushData(final String tableName, final AccumuloClient client)
      throws TableNotFoundException, AccumuloSecurityException, AccumuloException {
    log.info("Writing data to {}", tableName);
    writeData(client, tableName, 7);
    client.tableOperations().flush(tableName, null, null, true);
    writeData(client, tableName, 8);
    client.tableOperations().flush(tableName, null, null, true);
    writeData(client, tableName, 9);
    client.tableOperations().flush(tableName, null, null, true);
  }

  // write a million random rows
  static void writeData(AccumuloClient client, String tableName, int seed)
      throws TableNotFoundException, MutationsRejectedException {
    Random r = new Random(seed);
    try (BatchWriter bw = client.createBatchWriter(tableName)) {
      for (int x = 0; x < 1_000_000; x++) {
        long rowId = RandomBatchWriter.abs(r.nextLong()) % 1_000_000_000;
        Mutation m = RandomBatchWriter.createMutation(rowId, 50, new ColumnVisibility());
        bw.addMutation(m);
      }
    }
  }
}
