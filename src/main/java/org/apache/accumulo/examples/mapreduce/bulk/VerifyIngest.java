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

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VerifyIngest {

  private static final Logger log = LoggerFactory.getLogger(VerifyIngest.class);
  private static final String ROW_FORMAT = "row_%010d";
  private static final String VALUE_FORMAT = "value_%010d";

  private VerifyIngest() {}

  public static void main(String[] args) throws TableNotFoundException {

    ClientOpts opts = new ClientOpts();
    opts.parseArgs(VerifyIngest.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build();
        Scanner scanner = client.createScanner(SetupTable.tableName, Authorizations.EMPTY)) {

      scanner.setRange(new Range(String.format(ROW_FORMAT, 0), null));

      Iterator<Entry<Key,Value>> si = scanner.iterator();

      boolean ok = true;

      for (int i = 0; i < BulkIngestExample.numRows; i++) {

        if (si.hasNext()) {
          Entry<Key,Value> entry = si.next();

          if (!entry.getKey().getRow().toString().equals(String.format(ROW_FORMAT, i))) {
            log.error("unexpected row key {}; expected {}", entry.getKey().getRow(),
                String.format(ROW_FORMAT, i));
            ok = false;
          }

          if (!entry.getValue().toString().equals(String.format(VALUE_FORMAT, i))) {
            log.error("unexpected value {}; expected {}", entry.getValue(),
                String.format(VALUE_FORMAT, i));
            ok = false;
          }

        } else {
          log.error("no more rows, expected {}", String.format(ROW_FORMAT, i));
          ok = false;
          break;
        }

      }

      if (ok) {
        System.out.println("Data verification succeeded!");
        System.exit(0);
      } else {
        System.out.println("Data verification failed!");
        System.exit(1);
      }
    }
  }
}
