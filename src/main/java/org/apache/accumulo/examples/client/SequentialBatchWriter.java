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

import java.util.Random;

import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

/**
 * Simple example for writing random data in sequential order to Accumulo.
 */
public class SequentialBatchWriter {

  private static final Logger log = LoggerFactory.getLogger(SequentialBatchWriter.class);

  public static Value createValue(long rowId, int size) {
    Random r = new Random(rowId);
    byte value[] = new byte[size];

    r.nextBytes(value);

    // transform to printable chars
    for (int j = 0; j < value.length; j++) {
      value[j] = (byte) (((0xff & value[j]) % 92) + ' ');
    }

    return new Value(value);
  }

  static class Opts extends ClientOpts {
    @Parameter(names = {"-t"}, description = "table to use")
    public String tableName = "batch";

    @Parameter(names = {"--start"}, description = "starting row")
    public Integer start = 0;

    @Parameter(names = {"--num"}, description = "number of rows")
    public Integer num = 10_000;

    @Parameter(names = {"--size"}, description = "size of values")
    public Integer size = 50;
  }

  /**
   * Writes 1000 entries to Accumulo using a {@link BatchWriter}. The rows of the entries will be
   * sequential starting from 0. The column families will be "foo" and column qualifiers will be
   * "1". The values will be random 50 byte arrays.
   */
  public static void main(String[] args)
      throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Opts opts = new Opts();
    opts.parseArgs(SequentialBatchWriter.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      try {
        client.tableOperations().create(opts.tableName);
      } catch (TableExistsException e) {
        // ignore
      }

      try (BatchWriter bw = client.createBatchWriter(opts.tableName)) {
        for (int i = 0; i < opts.num; i++) {
          int row = i + opts.start;
          Mutation m = new Mutation(String.format("row_%010d", row));
          // create a random value that is a function of row id for verification purposes
          m.put("foo", "1", createValue(row, opts.size));
          bw.addMutation(m);
          if (i % 1000 == 0) {
            log.trace("wrote {} entries", i);
          }
        }
      }
    }
  }
}
