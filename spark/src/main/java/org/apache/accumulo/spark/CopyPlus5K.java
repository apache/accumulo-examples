package org.apache.accumulo.spark;

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
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class CopyPlus5K {

  public static void main(String[] args) throws Exception {

    if ((!args[0].equals("batch") && !args[0].equals("bulk")) || args[1].isEmpty()) {
        System.out.println("Usage: ./run.sh [batch|bulk] /path/to/accumulo-client.properties");
        System.exit(1);
    }

    final String inputTable = "spark_example_input";
    final String outputTable = "spark_example_output";
    final Properties props = Accumulo.newClientProperties().from(args[1]).build();

    try (AccumuloClient client = Accumulo.newClient().from(props).build()) {
      // Delete tables (if they exist) and create new tables
      if (client.tableOperations().exists(inputTable)) {
        client.tableOperations().delete(inputTable);
      }
      client.tableOperations().create(inputTable);
      if (client.tableOperations().exists(outputTable)) {
        client.tableOperations().delete(outputTable);
      }
      client.tableOperations().create(outputTable);
      // Write data to input table
      try (BatchWriter bw = client.createBatchWriter(inputTable)) {
        for (int i = 0; i < 100; i++) {
          Mutation m = new Mutation(String.format("%09d", i));
          m.at().family("cf1").qualifier("cq1").put("" + i);
          bw.addMutation(m);
        }
      }
    }

    SparkConf sparkConf = new SparkConf();
    sparkConf.setAppName("CopyPlus5K");

    JavaSparkContext sc = new JavaSparkContext(sparkConf);

    Job job = Job.getInstance();

    // Read input from Accumulo
    AccumuloInputFormat.configure().clientProperties(props).table(inputTable).store(job);
    JavaPairRDD<Key,Value> data = sc.newAPIHadoopRDD(job.getConfiguration(),
        AccumuloInputFormat.class, Key.class, Value.class);

    // Add 5K to all values
    JavaPairRDD<Key, Value> dataPlus5K = data.mapValues(v ->
        new Value("" + (Integer.parseInt(v.toString()) + 5_000)));

    if (args[0].equals("batch")) {
      // Write output using batch writer
      dataPlus5K.foreachPartition(iter -> {
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

      // Create HDFS directories for bulk import
      FileSystem hdfs = FileSystem.get(new Configuration());
      final String rootDir = "/spark_example/";
      Path rootPath = new Path(rootDir);
      if (hdfs.exists(rootPath)) {
        hdfs.delete(rootPath, true);
      }
      Path outputDir = new Path(rootDir + "/output");
      Path failDir = new Path(rootDir + "/fail");
      hdfs.mkdirs(rootPath);
      hdfs.mkdirs(failDir);

      // Write Spark output to HDFS
      AccumuloFileOutputFormat.configure().outputPath(outputDir).store(job);
      dataPlus5K.saveAsNewAPIHadoopFile(outputDir.toString(), Key.class, Value.class,
          AccumuloFileOutputFormat.class);

      // Bulk import into Accumulo
      try (AccumuloClient client = Accumulo.newClient().from(props).build()) {
        if (client.tableOperations().exists(outputTable)) {
          client.tableOperations().delete(outputTable);
        }
        client.tableOperations().create(outputTable);
        client.tableOperations().importDirectory(outputTable, outputDir.toString(),
            failDir.toString(), false);
      }
    } else {
      System.out.println("Unknown method to write output: " + args[0]);
      System.exit(1);
    }
  }
}
