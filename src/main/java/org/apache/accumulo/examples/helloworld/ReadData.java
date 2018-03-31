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
package org.apache.accumulo.examples.helloworld;

import java.util.Map.Entry;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads all data between two rows
 */
public class ReadData {

  private static final Logger log = LoggerFactory.getLogger(ReadData.class);

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {

    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();

    try (Scanner scan = connector.createScanner("hellotable", Authorizations.EMPTY)) {
      scan.setRange(new Range(new Key("row_0"), new Key("row_1002")));
      for (Entry<Key, Value> e : scan) {
        Key key = e.getKey();
        log.trace(key.getRow() + " " + key.getColumnFamily() + " " + key.getColumnQualifier() + " " + e.getValue());
      }
    }
  }
}
