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

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;

public class BloomFiltersNotFound {

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();
    try {
      connector.tableOperations().create("bloom_test3");
      connector.tableOperations().create("bloom_test4");
      connector.tableOperations().setProperty("bloom_test4", "table.bloom.enabled", "true");
    } catch (TableExistsException e) {
      // ignore
    }
    System.out.println("Writing data to bloom_test3 and bloom_test4 (bloom filters enabled)");
    writeData(connector, "bloom_test3", 7);
    connector.tableOperations().flush("bloom_test3", null, null, true);
    writeData(connector, "bloom_test4", 7);
    connector.tableOperations().flush("bloom_test4", null, null, true);

    BloomBatchScanner.scan(connector, "bloom_test3", 8);
    BloomBatchScanner.scan(connector, "bloom_test4", 8);
  }
}
