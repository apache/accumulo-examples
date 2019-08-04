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
package org.apache.accumulo.spark;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.hadoop.mapreduce.AccumuloFileOutputFormat;
import org.apache.accumulo.hadoop.mapreduce.AccumuloInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class CopyPlus5K {

  public static class AccumuloRangePartitioner extends Partitioner {

    private static final long serialVersionUID = 1L;
    private List<String> splits;

    AccumuloRangePartitioner(String... listSplits) {
      this.splits = Arrays.asList(listSplits);
    }

    @Override
    public int getPartition(Object o) {
      int index = Collections.binarySearch(splits, ((Key) o).getRow().toString());
      index = index < 0 ? (index + 1) * -1 : index;
      return index;
    }

    @Override
    public int numPartitions() {
      return splits.size() + 1;
    }
  }

  private static void cleanupAndCreateTables(Properties props) throws Exception {
    FileSystem hdfs = FileSystem.get(new Configuration());
    if (hdfs.exists(rootPath)) {
      hdfs.delete(rootPath, true);
    }
    try (AccumuloClient client = Accumulo.newClient().from(props).build()) {
      if (client.tableOperations().exists(inputTable)) {
        client.tableOperations().delete(inputTable);
      }
      if (client.tableOperations().exists(outputTable)) {
        client.tableOperations().delete(outputTable);
      }
      // Create tables
      client.tableOperations().create(inputTable);
      client.tableOperations().create(outputTable);

      // Write data to input table
      try (BatchWriter bw = client.createBatchWriter(inputTable)) {
        for (int i = 0; i < 100; i++) {
          Mutation m = new Mutation(String.format("%03d", i));
          m.at().family("cf1").qualifier("cq1").put("" + i);
          bw.addMutation(m);
        }
      }
    }
  }

  private static final String inputTable = "spark_example_input";
  private static final String outputTable = "spark_example_output";
  private static final Path rootPath = new Path("/spark_example/");

  public static void main(String[] args) throws Exception {

    if ((!args[0].equals("batch") && !args[0].equals("bulk")) || args[1].isEmpty()) {
      System.out.println("Usage: ./run.sh [batch|bulk] /path/to/accumulo-client.properties");
      System.exit(1);
    }

    // Read client properties from file
    final Properties props = Accumulo.newClientProperties().from(args[1]).build();

    cleanupAndCreateTables(props);

    SparkConf conf = new SparkConf();
    conf.setAppName("CopyPlus5K");
    // KryoSerializer is needed for serializing Accumulo Key when partitioning data for bulk import
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    conf.registerKryoClasses(new Class[] {Key.class, Value.class, Properties.class});

    try (JavaSparkContext sc = new JavaSparkContext(conf)) {

      Job job = Job.getInstance();

      // Read input from Accumulo
      AccumuloInputFormat.configure().clientProperties(props).table(inputTable).store(job);
      JavaPairRDD<Key,Value> data = sc.newAPIHadoopRDD(job.getConfiguration(),
          AccumuloInputFormat.class, Key.class, Value.class);

      // Add 5K to all values
      JavaPairRDD<Key,Value> dataPlus5K = data
          .mapValues(v -> new Value("" + (Integer.parseInt(v.toString()) + 5_000)));

      if (args[0].equals("batch")) {
        // Write output using batch writer
        dataPlus5K.foreachPartition(iter -> {
          // Intentionally created an Accumulo client for each partition to avoid attempting to
          // serialize it and send it to each remote process.
          try (AccumuloClient client = Accumulo.newClient().from(props).build();
              BatchWriter bw = client.createBatchWriter(outputTable)) {
            iter.forEachRemaining(kv -> {
              Key key = kv._1;
              Value val = kv._2;
              Mutation m = new Mutation(key.getRow());
              m.at().family(key.getColumnFamily()).qualifier(key.getColumnQualifier())
                  .visibility(key.getColumnVisibility()).timestamp(key.getTimestamp()).put(val);
              try {
                bw.addMutation(m);
              } catch (MutationsRejectedException e) {
                e.printStackTrace();
              }
            });
          }
        });
      } else if (args[0].equals("bulk")) {
        // Write output using bulk import

        // Create HDFS directory for bulk import
        FileSystem hdfs = FileSystem.get(new Configuration());
        hdfs.mkdirs(rootPath);
        Path outputDir = new Path(rootPath.toString() + "/output");

        // Write Spark output to HDFS
        AccumuloFileOutputFormat.configure().outputPath(outputDir).store(job);
        Partitioner partitioner = new AccumuloRangePartitioner("3", "7");
        JavaPairRDD<Key,Value> partData = dataPlus5K
            .repartitionAndSortWithinPartitions(partitioner);
        partData.saveAsNewAPIHadoopFile(outputDir.toString(), Key.class, Value.class,
            AccumuloFileOutputFormat.class);

        // Bulk import into Accumulo
        try (AccumuloClient client = Accumulo.newClient().from(props).build()) {
          client.tableOperations().importDirectory(outputDir.toString()).to(outputTable).load();
        }
      } else {
        System.out.println("Unknown method to write output: " + args[0]);
        System.exit(1);
      }
    }
  }
}
