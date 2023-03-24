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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.NewTableConfiguration;
import org.apache.accumulo.examples.Common;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.apache.hadoop.io.Text;

public final class SetupTable {

  static final String BULK_INGEST_TABLE = Common.NAMESPACE + ".test_bulk";

  private SetupTable() {}

  public static void main(String[] args)
      throws AccumuloSecurityException, TableNotFoundException, AccumuloException {

    final Stream<String> splits = Stream.of("row_00000333", "row_00000666");
    ClientOpts opts = new ClientOpts();
    opts.parseArgs(SetupTable.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      // create a table with initial partitions
      TreeSet<Text> initialPartitions = splits.map(Text::new)
          .collect(Collectors.toCollection(TreeSet::new));
      Common.createTableWithNamespace(client, BULK_INGEST_TABLE,
          new NewTableConfiguration().withSplits(initialPartitions));
    }
  }
}
