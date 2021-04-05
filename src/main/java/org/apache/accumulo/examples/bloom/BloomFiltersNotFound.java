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

import static org.apache.accumulo.examples.bloom.BloomFilters.writeData;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.examples.cli.ClientOpts;

public class BloomFiltersNotFound {

  public static void main(String[] args)
      throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    ClientOpts opts = new ClientOpts();
    opts.parseArgs(BloomFiltersNotFound.class.getName(), args);

    final String BLOOM_TEST3 = "bloom_test3";
    final String BLOOM_TEST4 = "bloom_test4";

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      try {
        client.tableOperations().create(BLOOM_TEST3);
      } catch (TableExistsException e) {
        // ignore
      }
      try {
        client.tableOperations().create(BLOOM_TEST4);
        client.tableOperations().setProperty(BLOOM_TEST4, BloomFilters.BLOOM_ENABLED_PROPERTY,
            "true");
      } catch (TableExistsException e) {
        // ignore
      }

      writeAndFlush(BLOOM_TEST3, client);
      writeAndFlush(BLOOM_TEST4, client);

      BloomBatchScanner.scan(client, BLOOM_TEST3, 8);
      BloomBatchScanner.scan(client, BLOOM_TEST4, 8);
    }
  }

  private static void writeAndFlush(String tableName, AccumuloClient client)
      throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
    System.out.println("Writing data to " + tableName + " (bloom filters enabled)");
    writeData(client, tableName, 7);
    client.tableOperations().flush(tableName, null, null, true);
  }
}
