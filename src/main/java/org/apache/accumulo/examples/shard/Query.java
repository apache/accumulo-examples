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
package org.apache.accumulo.examples.shard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.sample.SamplerConfiguration;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.IntersectingIterator;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.examples.cli.Help;
import org.apache.hadoop.io.Text;

import com.beust.jcommander.Parameter;

/**
 * This program queries a set of terms in the shard table (populated by {@link Index}) using the {@link IntersectingIterator}.
 */
public class Query {

  static class QueryOpts extends Help {

    @Parameter(description = " term { <term> ... }")
    List<String> terms = new ArrayList<>();

    @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
    private String tableName;

    @Parameter(names = {"--sample"}, description = "Do queries against sample, useful when sample is built using column qualifier")
    private boolean useSample = false;

    @Parameter(names = {"--sampleCutoff"},
        description = "Use sample data to determine if a query might return a number of documents over the cutoff.  This check is per tablet.")
    private Integer sampleCutoff = null;
  }

  public static List<String> query(BatchScanner bs, List<String> terms, Integer cutoff) {

    Text columns[] = new Text[terms.size()];
    int i = 0;
    for (String term : terms) {
      columns[i++] = new Text(term);
    }

    IteratorSetting ii;

    if (cutoff != null) {
      ii = new IteratorSetting(20, "ii", CutoffIntersectingIterator.class);
      CutoffIntersectingIterator.setCutoff(ii, cutoff);
    } else {
      ii = new IteratorSetting(20, "ii", IntersectingIterator.class);
    }

    IntersectingIterator.setColumnFamilies(ii, columns);
    bs.addScanIterator(ii);
    bs.setRanges(Collections.singleton(new Range()));
    List<String> result = new ArrayList<>();
    for (Entry<Key,Value> entry : bs) {
      result.add(entry.getKey().getColumnQualifier().toString());
    }
    return result;
  }

  public static void main(String[] args) throws Exception {
    QueryOpts opts = new QueryOpts();
    opts.parseArgs(Query.class.getName(), args);

    Connector conn = Connector.builder().usingProperties("conf/accumulo-client.properties")
        .build();

    try (BatchScanner bs = conn.createBatchScanner(opts.tableName, Authorizations.EMPTY, 10)) {
      if (opts.useSample) {
        SamplerConfiguration samplerConfig = conn.tableOperations().getSamplerConfiguration(opts.tableName);
        CutoffIntersectingIterator.validateSamplerConfig(conn.tableOperations().getSamplerConfiguration(opts.tableName));
        bs.setSamplerConfiguration(samplerConfig);
      }
      for (String entry : query(bs, opts.terms, opts.sampleCutoff)) {
        System.out.println("  " + entry);
      }
    }
  }
}
