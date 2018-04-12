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

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.NamespaceExistsException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demonstration of reading entire rows and deleting entire rows.
 */
public class RowOperations {

  private static final Logger log = LoggerFactory.getLogger(RowOperations.class);
  private static final String namespace = "examples";
  private static final String table = namespace + ".rowops";

  private static void printAll(Connector connector) throws TableNotFoundException {
    try (Scanner scanner = connector.createScanner("rowops", Authorizations.EMPTY)) {
      for (Entry<Key,Value> entry : scanner) {
        log.info("Key: " + entry.getKey().toString() + " Value: " + entry.getValue().toString());
      }
    }
  }

  private static void printRow(String row, Connector connector) throws TableNotFoundException {
    try (Scanner scanner = connector.createScanner("rowops", Authorizations.EMPTY)) {
      scanner.setRange(Range.exact(row));
      for (Entry<Key,Value> entry : scanner) {
        log.info("Key: " + entry.getKey().toString() + " Value: " + entry.getValue().toString());
      }
    }
  }

  private static void deleteRow(String row, Connector connector, BatchWriter bw) throws MutationsRejectedException, TableNotFoundException {
    Mutation mut = new Mutation(row);
    try (Scanner scanner = connector.createScanner("rowops", Authorizations.EMPTY)) {
      scanner.setRange(Range.exact(row));
      for (Entry<Key,Value> entry : scanner) {
        mut.putDelete(entry.getKey().getColumnFamily(), entry.getKey().getColumnQualifier());
      }
    }
    bw.addMutation(mut);
    bw.flush();
  }

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();
    try {
      connector.namespaceOperations().create(namespace);
    } catch (NamespaceExistsException e) {
      // ignore
    }
    try {
      connector.tableOperations().create(table);
    } catch (TableExistsException e) {
      // ignore
    }

    // lets create 3 rows of information
    Mutation mut1 = new Mutation("row1");
    Mutation mut2 = new Mutation("row2");
    Mutation mut3 = new Mutation("row3");

    mut1.put("col", "1", "v1");
    mut1.put("col", "2", "v2");
    mut1.put("col", "3", "v3");

    mut2.put("col", "1", "v1");
    mut2.put("col", "2", "v2");
    mut2.put("col", "3", "v3");

    mut3.put("col", "1", "v1");
    mut3.put("col", "2", "v2");
    mut3.put("col", "3", "v3");

    // Now we'll make a Batch Writer
    try (BatchWriter bw = connector.createBatchWriter(table)) {

      // And add the mutations
      bw.addMutation(mut1);
      bw.addMutation(mut2);
      bw.addMutation(mut3);

      // Force a send
      bw.flush();

      log.info("This is only row2");
      printRow("row2", connector);

      log.info("This is everything");
      printAll(connector);

      deleteRow("row2", connector, bw);

      log.info("This is row1 and row3");
      printAll(connector);

      deleteRow("row1", connector, bw);
   }

    log.info("This is just row3");
    printAll(connector);

    connector.tableOperations().delete(table);
  }
}
