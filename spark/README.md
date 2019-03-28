<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
# Apache Accumulo Spark Example

## Requirements

* Accumulo 2.0+
* Hadoop YARN installed & `HADOOP_CONF_DIR` set in environment
* Spark installed & `SPARK_HOME` set in environment

## Spark example

The [CopyPlus5K] example will create an Accumulo table called `spark_example_input`
and write 100 key/value entries into Accumulo with the values `0..99`. It then launches
a Spark application that does following:

* Read data from `spark_example_input` table using `AccumuloInputFormat`
* Add 5000 to each value
* Write the data to a new Accumulo table (called `spark_example_output`) using one of
  two methods.
  1. **Bulk import** - Write data to an RFile in HDFS using `AccumuloFileOutputFormat` and
     bulk import to Accumulo table
  2. **Batchwriter** - Creates a `BatchWriter` in Spark code to write to the table. 

This application can be run using the command:

    ./run.sh batch /path/to/accumulo-client.properties

Change `batch` to `bulk` to use Bulk import method.

[CopyPlus5K]: src/main/java/org/apache/accumulo/spark/CopyPlus5K.java
