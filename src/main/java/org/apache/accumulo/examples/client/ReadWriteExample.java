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

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.NamespaceExistsException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteExample {

  private static final Logger log = LoggerFactory.getLogger(ReadWriteExample.class);
  private static final String namespace = "examples";
  private static final String table = namespace + ".readwrite";

  public static void main(String[] args) throws Exception {

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

    // write data
    try (BatchWriter writer = connector.createBatchWriter(table)) {
      for (int i = 0; i < 10; i++) {
        Mutation m = new Mutation("hello" + i);
        m.put("cf", "cq", new Value("world" + i));
        writer.addMutation(m);
      }
    }

    // read data
    try (Scanner scanner = connector.createScanner(table, Authorizations.EMPTY)) {
      for (Entry<Key, Value> entry : scanner) {
        log.info(entry.getKey().toString() + " -> " + entry.getValue().toString());
      }
    }

    // delete table
    connector.tableOperations().delete(table);
  }
}
