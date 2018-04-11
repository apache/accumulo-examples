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
# Apache Accumulo Bulk Ingest Example

This is an example of how to bulk ingest data into Accumulo using map reduce.

This tutorial uses the following Java classes.

 * [SetupTable.java] - creates the table and some data to ingest
 * [BulkIngestExample.java] - ingest the data using map reduce
 * [VerifyIngest.java] - checks that the data was ingested
 
Remember to copy the accumulo-examples-\*.jar to Accumulo's 'lib/ext' directory.

    $ cp target/accumulo-examples-*.jar /path/accumulo/lib/ext

The following commands show how to run this example. This example creates a
table called test_bulk which has two initial split points. Then 1000 rows of
test data are created in HDFS. After that the 1000 rows are ingested into
Accumulo. Then we verify the 1000 rows are in Accumulo. 

    $ PKG=org.apache.accumulo.examples.mapreduce.bulk
    $ accumulo $PKG.SetupTable
    $ accumulo-util hadoop-jar target/accumulo-examples-*.jar $PKG.BulkIngestExample
    $ ./bin/runex mapreduce.bulk.VerifyIngest

[SetupTable.java]: ../src/main/java/org/apache/accumulo/examples/mapreduce/bulk/SetupTable.java
[BulkIngestExample.java]:  ../src/main/java/org/apache/accumulo/examples/mapreduce/bulk/BulkIngestExample.java
[VerifyIngest.java]: ../src/main/java/org/apache/accumulo/examples/mapreduce/bulk/VerifyIngest.java
