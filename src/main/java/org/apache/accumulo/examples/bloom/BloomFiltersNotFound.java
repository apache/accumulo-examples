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

import java.util.Map;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.NewTableConfiguration;
import org.apache.accumulo.examples.Common;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BloomFiltersNotFound {

  private static final Logger log = LoggerFactory.getLogger(BloomFiltersNotFound.class);

  public static void main(String[] args)
      throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    ClientOpts opts = new ClientOpts();
    opts.parseArgs(BloomFiltersNotFound.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      Map<String,String> props = Map.of(BloomCommon.BLOOM_ENABLED_PROPERTY, "true");
      var newTableConfig = new NewTableConfiguration().setProperties(props);

      Common.createTableWithNamespace(client, BloomCommon.BLOOM_TEST3_TABLE);
      Common.createTableWithNamespace(client, BloomCommon.BLOOM_TEST4_TABLE, newTableConfig);

      writeAndFlush(BloomCommon.BLOOM_TEST3_TABLE, client);
      writeAndFlush(BloomCommon.BLOOM_TEST4_TABLE, client);

      BloomBatchScanner.scan(client, BloomCommon.BLOOM_TEST3_TABLE, 8);
      BloomBatchScanner.scan(client, BloomCommon.BLOOM_TEST4_TABLE, 8);
    }
  }

  private static void writeAndFlush(String tableName, AccumuloClient client)
      throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
    log.info("Writing data to {} (bloom filters enabled)", tableName);
    writeData(client, tableName, 7);
    client.tableOperations().flush(tableName, null, null, true);
  }
}
