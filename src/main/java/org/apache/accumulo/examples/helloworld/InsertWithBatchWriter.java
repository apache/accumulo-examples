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

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inserts 10K rows (50K entries) into accumulo with each row having 5 entries.
 */
public class InsertWithBatchWriter {

  private static final Logger log = LoggerFactory.getLogger(InsertWithBatchWriter.class);

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {

    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();
    try {
      connector.tableOperations().create("hellotable");
    } catch (TableExistsException e) {
      // ignore
    }

    try (BatchWriter bw = connector.createBatchWriter("hellotable")) {
      log.trace("writing ...");
      for (int i = 0; i < 10000; i++) {
        Mutation m = new Mutation(String.format("row_%d", i));
        for (int j = 0; j < 5; j++) {
          m.put("colfam", String.format("colqual_%d", j), new Value((String.format("value_%d_%d", i, j)).getBytes()));
        }
        bw.addMutation(m);
        if (i % 100 == 0) {
          log.trace(String.valueOf(i));
        }
      }
    }
  }
}
