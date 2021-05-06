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

import java.util.Map.Entry;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.examples.Common;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteExample {

  private static final Logger log = LoggerFactory.getLogger(ReadWriteExample.class);

  private static final String READWRITE_TABLE = Common.NAMESPACE + ".readwrite";

  private ReadWriteExample() {}

  public static void main(String[] args) throws AccumuloSecurityException, AccumuloException {
    ClientOpts opts = new ClientOpts();
    opts.parseArgs(ReadWriteExample.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      Common.createTableWithNamespace(client, READWRITE_TABLE);
      // write data
      try (BatchWriter writer = client.createBatchWriter(READWRITE_TABLE)) {
        for (int i = 0; i < 10; i++) {
          Mutation m = new Mutation("hello" + i);
          m.put("cf", "cq", new Value("world" + i));
          writer.addMutation(m);
        }
      } catch (TableNotFoundException e) {
        log.error("Could not find table {}: {}", e.getTableName(), e.getMessage());
        System.exit(1);
      }

      // read data
      try (Scanner scanner = client.createScanner(READWRITE_TABLE, Authorizations.EMPTY)) {
        for (Entry<Key,Value> entry : scanner) {
          log.info("{} -> {}", entry.getKey().toString(), entry.getValue().toString());
        }
      } catch (TableNotFoundException e) {
        log.error("Could not find table {}: {}", e.getTableName(), e.getMessage());
        System.exit(1);
      }

      // delete table
      try {
        client.tableOperations().delete(READWRITE_TABLE);
      } catch (TableNotFoundException e) {
        log.error("Unable to delete table '{}': {}", e.getTableName(), e.getMessage());
      }
    }
  }
}
