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

import java.time.Instant;
import java.util.Map.Entry;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.trace.TraceUtil;
// import org.apache.accumulo.core.trace.DistributedTrace;
import org.apache.accumulo.examples.Common;
import org.apache.accumulo.examples.cli.ClientOnDefaultTable;
import org.apache.accumulo.examples.cli.ScannerOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

/**
 * A simple example showing how to use the distributed tracing API in client code
 *
 */
public class TracingExample {
  private static final Logger log = LoggerFactory.getLogger(TracingExample.class);
  private static final String DEFAULT_TABLE_NAME = "test";

  private final AccumuloClient client;

  static class Opts extends ClientOnDefaultTable {
    @Parameter(names = {"--createtable"}, description = "create table before doing anything")
    boolean createtable = false;
    @Parameter(names = {"--deletetable"}, description = "delete table when finished")
    boolean deletetable = false;
    @Parameter(names = {"--create"}, description = "create entries before any deletes")
    boolean createEntries = false;
    @Parameter(names = {"--read"}, description = "read entries after any creates/deletes")
    boolean readEntries = false;

    public Opts() {
      super(DEFAULT_TABLE_NAME);
      auths = new Authorizations();
    }
  }

  private TracingExample(AccumuloClient client) {
    this.client = client;
  }

  private void enableTracing() {
    TraceUtil.enable();
  }

  private void execute(Opts opts) throws TableNotFoundException, AccumuloException,
      AccumuloSecurityException, TableExistsException {

    if (opts.createtable) {
      Common.createTableWithNamespace(client, opts.getTableName());
    }

    if (opts.createEntries) {
      createEntries(opts);
    }

    if (opts.readEntries) {
      readEntries(opts);
    }

    if (opts.deletetable) {
      client.tableOperations().delete(opts.getTableName());
    }
  }

  private void createEntries(Opts opts) throws TableNotFoundException, AccumuloException {

    // Trace the write operation. Note, unless you flush the BatchWriter, you will not capture
    // the write operation as it is occurs asynchronously. You can optionally create additional
    // Spans
    // within a given Trace as seen below around the flush
    Span span = TraceUtil.startSpan(TracingExample.class, "createEntries");
    try (Scope scope = span.makeCurrent()) {
      try (BatchWriter batchWriter = client.createBatchWriter(opts.getTableName())) {
        Mutation m = new Mutation("row");
        m.put("cf", "cq", "value");

        batchWriter.addMutation(m);
        // You can add timeline annotations to Spans which will be able to be viewed in the Monitor
        span.addEvent("Initiating Flush", Instant.now());
        batchWriter.flush();
      }
    } finally {
      span.end();
    }

  }

  private void readEntries(Opts opts) throws TableNotFoundException {

    try (Scanner scanner = client.createScanner(opts.getTableName(), opts.auths)) {
      // Trace the read operation.
      Span span = TraceUtil.startSpan(TracingExample.class, "createEntries");
      try (Scope scope = span.makeCurrent()) {
        int numberOfEntriesRead = 0;
        for (Entry<Key,Value> entry : scanner) {
          System.out.println(entry.getKey().toString() + " -> " + entry.getValue().toString());
          ++numberOfEntriesRead;
        }
        // You can add additional metadata (key, values) to Spans
        span.setAttribute("Number of Entries Read", numberOfEntriesRead);
      } finally {
        span.end();
      }
    }
  }

  public static void main(String[] args) {
    Opts opts = new Opts();
    ScannerOpts scannerOpts = new ScannerOpts();
    opts.parseArgs(TracingExample.class.getName(), args, scannerOpts);

    try (AccumuloClient client = opts.createAccumuloClient()) {
      TracingExample tracingExample = new TracingExample(client);
      tracingExample.enableTracing();
      tracingExample.execute(opts);
    } catch (Exception e) {
      log.error("Caught exception running TraceExample", e);
      System.exit(1);
    }
  }

}
