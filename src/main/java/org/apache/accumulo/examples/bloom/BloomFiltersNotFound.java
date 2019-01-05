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

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      try {
        client.tableOperations().create("bloom_test3");
        client.tableOperations().create("bloom_test4");
        client.tableOperations().setProperty("bloom_test4", "table.bloom.enabled", "true");
      } catch (TableExistsException e) {
        // ignore
      }
      System.out.println("Writing data to bloom_test3 and bloom_test4 (bloom filters enabled)");
      writeData(client, "bloom_test3", 7);
      client.tableOperations().flush("bloom_test3", null, null, true);
      writeData(client, "bloom_test4", 7);
      client.tableOperations().flush("bloom_test4", null, null, true);

      BloomBatchScanner.scan(client, "bloom_test3", 8);
      BloomBatchScanner.scan(client, "bloom_test4", 8);
    }
  }
}
