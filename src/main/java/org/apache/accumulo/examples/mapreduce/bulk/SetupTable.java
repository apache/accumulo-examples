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

import java.util.TreeSet;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.accumulo.examples.common.Constants;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SetupTable {

  private static final Logger log = LoggerFactory.getLogger(SetupTable.class);

  private SetupTable() {}

  public static void main(String[] args)
      throws AccumuloSecurityException, TableNotFoundException, AccumuloException {

    final String[] splits = {"row_00000333", "row_00000666"};
    ClientOpts opts = new ClientOpts();
    opts.parseArgs(SetupTable.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      try {
        client.tableOperations().create(BulkCommon.BULK_INGEST_TABLE);
      } catch (TableExistsException e) {
        log.warn(Constants.TABLE_EXISTS_MSG + BulkCommon.BULK_INGEST_TABLE);
      }

      // create a table with initial partitions
      TreeSet<Text> initialPartitions = new TreeSet<>();
      for (String split : splits) {
        initialPartitions.add(new Text(split));
      }
      client.tableOperations().addSplits(BulkCommon.BULK_INGEST_TABLE, initialPartitions);
    }
  }
}
