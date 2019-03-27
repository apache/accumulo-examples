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
package org.apache.accumulo.examples;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.security.tokens.AuthenticationToken;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.AgeOffFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.examples.client.RandomBatchScanner;
import org.apache.accumulo.examples.client.ReadWriteExample;
import org.apache.accumulo.examples.client.RowOperations;
import org.apache.accumulo.examples.client.SequentialBatchWriter;
import org.apache.accumulo.examples.combiner.StatsCombiner;
import org.apache.accumulo.examples.constraints.MaxMutationSize;
import org.apache.accumulo.examples.helloworld.Insert;
import org.apache.accumulo.examples.helloworld.Read;
import org.apache.accumulo.examples.isolation.InterferenceTest;
import org.apache.accumulo.examples.mapreduce.RegexExample;
import org.apache.accumulo.examples.mapreduce.RowHash;
import org.apache.accumulo.examples.mapreduce.TableToFile;
import org.apache.accumulo.examples.mapreduce.TeraSortIngest;
import org.apache.accumulo.examples.mapreduce.WordCount;
import org.apache.accumulo.examples.shard.ContinuousQuery;
import org.apache.accumulo.examples.shard.Index;
import org.apache.accumulo.examples.shard.Query;
import org.apache.accumulo.examples.shard.Reverse;
import org.apache.accumulo.harness.AccumuloClusterHarness;
import org.apache.accumulo.minicluster.MemoryUnit;
import org.apache.accumulo.miniclusterImpl.MiniAccumuloConfigImpl;
import org.apache.accumulo.test.TestIngest;
import org.apache.accumulo.test.TestIngest.IngestParams;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterators;

public class ExamplesIT extends AccumuloClusterHarness {
  private static final BatchWriterConfig bwc = new BatchWriterConfig();
  private static final String auths = "A,B";

  private AccumuloClient c;
  private BatchWriter bw;
  private IteratorSetting is;
  private String dir;
  private FileSystem fs;
  private Authorizations origAuths;

  @Override
  public void configureMiniCluster(MiniAccumuloConfigImpl cfg, Configuration hadoopConf) {
    // 128MB * 3
    cfg.setDefaultMemory(cfg.getDefaultMemory() * 3, MemoryUnit.BYTE);
    cfg.setProperty(Property.TSERV_NATIVEMAP_ENABLED, "false");
  }

  @Before
  public void setupTest() throws Exception {
    c = Accumulo.newClient().from(getClientProps()).build();
    String user = c.whoami();
    String instance = getClientInfo().getInstanceName();
    String keepers = getClientInfo().getZooKeepers();
    AuthenticationToken token = getAdminToken();
    if (token instanceof PasswordToken) {
      String passwd = new String(((PasswordToken) getAdminToken()).getPassword(), UTF_8);
      writeClientPropsFile(getClientPropsFile(), instance, keepers, user, passwd);
    } else {
      Assert.fail("Unknown token type: " + token);
    }
    fs = getCluster().getFileSystem();
    dir = new Path(cluster.getTemporaryPath(), getClass().getName()).toString();

    origAuths = c.securityOperations().getUserAuthorizations(user);
    c.securityOperations().changeUserAuthorizations(user, new Authorizations(auths.split(",")));
  }

  @After
  public void teardownTest() throws Exception {
    if (null != origAuths) {
      c.securityOperations().changeUserAuthorizations(getAdminPrincipal(), origAuths);
    }
    c.close();
  }

  public static void writeClientPropsFile(String file, String instance, String keepers, String user,
      String password) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file))) {
      writer.write("instance.name=" + instance + "\n");
      writer.write("instance.zookeepers=" + keepers + "\n");
      writer.write("auth.type=password\n");
      writer.write("auth.principal=" + user + "\n");
      writer.write("auth.token=" + password + "\n");
    }
  }

  private String getClientPropsFile() {
    return System.getProperty("user.dir") + "/target/accumulo-client.properties";
  }

  @Override
  public int defaultTimeoutSeconds() {
    return 6 * 60;
  }

  @Test
  public void testAgeoffFilter() throws Exception {
    String tableName = getUniqueNames(1)[0];
    c.tableOperations().create(tableName);
    is = new IteratorSetting(10, AgeOffFilter.class);
    AgeOffFilter.setTTL(is, 1000L);
    c.tableOperations().attachIterator(tableName, is);
    sleepUninterruptibly(500, TimeUnit.MILLISECONDS); // let zookeeper updates propagate.
    bw = c.createBatchWriter(tableName, bwc);
    Mutation m = new Mutation("foo");
    m.put("a", "b", "c");
    bw.addMutation(m);
    bw.close();
    sleepUninterruptibly(1, TimeUnit.SECONDS);
    assertEquals(0, Iterators.size(c.createScanner(tableName, Authorizations.EMPTY).iterator()));
  }

  @Test
  public void testStatsCombiner() throws Exception {
    String table = getUniqueNames(1)[0];
    c.tableOperations().create(table);
    is = new IteratorSetting(10, StatsCombiner.class);
    StatsCombiner.setCombineAllColumns(is, true);
    StatsCombiner.setRadix(is, 10);
    assertTrue(is.getOptions().containsKey(StatsCombiner.RADIX_OPTION));

    c.tableOperations().attachIterator(table, is);
    bw = c.createBatchWriter(table, bwc);
    // Write two mutations otherwise the NativeMap would dedupe them into a single update
    Mutation m = new Mutation("foo");
    m.put("a", "b", "1");
    bw.addMutation(m);
    m = new Mutation("foo");
    m.put("a", "b", "3");
    bw.addMutation(m);
    bw.flush();

    Iterator<Entry<Key,Value>> iter = c.createScanner(table, Authorizations.EMPTY).iterator();
    assertTrue("Iterator had no results", iter.hasNext());
    Entry<Key,Value> e = iter.next();
    assertEquals("Results ", "1,3,4,2", e.getValue().toString());
    assertFalse("Iterator had additional results", iter.hasNext());

    m = new Mutation("foo");
    m.put("a", "b", "0,20,20,2");
    bw.addMutation(m);
    bw.close();

    iter = c.createScanner(table, Authorizations.EMPTY).iterator();
    assertTrue("Iterator had no results", iter.hasNext());
    e = iter.next();
    assertEquals("Results ", "0,20,24,4", e.getValue().toString());
    assertFalse("Iterator had additional results", iter.hasNext());
  }

  @Test
  public void testShardedIndex() throws Exception {
    File src = new File(System.getProperty("user.dir") + "/src");
    assumeTrue(src.exists());
    String[] names = getUniqueNames(3);
    final String shard = names[0], index = names[1];
    c.tableOperations().create(shard);
    c.tableOperations().create(index);
    bw = c.createBatchWriter(shard, bwc);
    Index.index(30, src, "\\W+", bw);
    bw.close();
    BatchScanner bs = c.createBatchScanner(shard, Authorizations.EMPTY, 4);
    List<String> found = Query.query(bs, Arrays.asList("foo", "bar"), null);
    bs.close();
    // should find ourselves
    boolean thisFile = false;
    for (String file : found) {
      if (file.endsWith("/ExamplesIT.java"))
        thisFile = true;
    }
    assertTrue(thisFile);

    String[] args = new String[] {"-c", getClientPropsFile(), "--shardTable", shard, "--doc2Term",
        index};

    // create a reverse index
    goodExec(Reverse.class, args);
    args = new String[] {"-c", getClientPropsFile(), "--shardTable", shard, "--doc2Term", index,
        "--terms", "5", "--count", "1000"};
    // run some queries
    goodExec(ContinuousQuery.class, args);
  }

  @Test
  public void testMaxMutationConstraint() throws Exception {
    String tableName = getUniqueNames(1)[0];
    c.tableOperations().create(tableName);
    c.tableOperations().addConstraint(tableName, MaxMutationSize.class.getName());
    IngestParams params = new IngestParams(c.properties(), tableName, 1);
    params.cols = 1000;
    try {
      TestIngest.ingest(c, params);
    } catch (MutationsRejectedException ex) {
      assertEquals(1, ex.getConstraintViolationSummaries().size());
    }
  }

  @Test
  public void testTeraSortAndRead() throws Exception {
    assumeTrue(getAdminToken() instanceof PasswordToken);
    String tableName = getUniqueNames(1)[0];
    String[] args = new String[] {"--count", (1000 * 1000) + "", "-nk", "10", "-xk", "10", "-nv",
        "10", "-xv", "10", "-t", tableName, "-c", getClientPropsFile(), "--splits", "4"};
    goodExec(TeraSortIngest.class, args);
    Path output = new Path(dir, "tmp/nines");
    if (fs.exists(output)) {
      fs.delete(output, true);
    }
    args = new String[] {"-c", getClientPropsFile(), "-t", tableName, "--rowRegex", ".*999.*",
        "--output", output.toString()};
    goodExec(RegexExample.class, args);
    args = new String[] {"-c", getClientPropsFile(), "-t", tableName, "--column", "c:"};
    goodExec(RowHash.class, args);
    output = new Path(dir, "tmp/tableFile");
    if (fs.exists(output)) {
      fs.delete(output, true);
    }
    args = new String[] {"-c", getClientPropsFile(), "-t", tableName, "--output",
        output.toString()};
    goodExec(TableToFile.class, args);
  }

  @Test
  public void testWordCount() throws Exception {
    assumeTrue(getAdminToken() instanceof PasswordToken);
    Path readme = new Path(new Path(System.getProperty("user.dir")), "README.md");
    if (!new File(readme.toString()).exists()) {
      Assert.fail("README.md does not exist!");
    }
    fs.copyFromLocalFile(readme, new Path(dir + "/tmp/wc/README.md"));
    String[] args = new String[] {"-c", getClientPropsFile(), "-i", dir + "/tmp/wc", "-t",
        getUniqueNames(1)[0]};
    goodExec(WordCount.class, args);
  }

  @Test
  public void testInsertWithBatchWriterAndReadData() throws Exception {
    String[] args;
    args = new String[] {"-c", getClientPropsFile()};
    goodExec(Insert.class, args);
    goodExec(Read.class, args);
  }

  @Test
  public void testIsolatedScansWithInterference() throws Exception {
    String[] args;
    args = new String[] {"-c", getClientPropsFile(), "-t", getUniqueNames(1)[0], "--iterations",
        "100000", "--isolated"};
    goodExec(InterferenceTest.class, args);
  }

  @Test
  public void testScansWithInterference() throws Exception {
    String[] args;
    args = new String[] {"-c", getClientPropsFile(), "-t", getUniqueNames(1)[0], "--iterations",
        "100000"};
    goodExec(InterferenceTest.class, args);
  }

  @Test
  public void testRowOperations() throws Exception {
    goodExec(RowOperations.class, "-c", getClientPropsFile());
  }

  @Test
  public void testReadWriteAndDelete() throws Exception {
    goodExec(ReadWriteExample.class, "-c", getClientPropsFile());
  }

  @Test
  public void testBatch() throws Exception {
    goodExec(SequentialBatchWriter.class, "-c", getClientPropsFile());
    goodExec(RandomBatchScanner.class, "-c", getClientPropsFile());
  }

  private void goodExec(Class<?> theClass, String... args) throws IOException {
    Entry<Integer,String> pair;
    // We're already slurping stdout into memory (not redirecting to file). Might as well add it
    // to error message.
    pair = getClusterControl().execWithStdout(theClass, args);
    Assert.assertEquals("stdout=" + pair.getValue(), 0, pair.getKey().intValue());
  }
}
