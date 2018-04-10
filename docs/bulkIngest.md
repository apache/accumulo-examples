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

This is an example of how to bulk ingest data into accumulo using map reduce.

The following commands show how to run this example. This example creates a
table called test_bulk which has two initial split points. Then 1000 rows of
test data are created in HDFS. After that the 1000 rows are ingested into
accumulo. Then we verify the 1000 rows are in accumulo.

    $ PKG=org.apache.accumulo.examples.mapreduce.bulk
    $ accumulo $PKG.SetupTable
    $ accumulo-util hadoop-jar target/accumulo-examples-*.jar $PKG.BulkIngestExample
    $ accumulo $PKG.VerifyIngest

For a high level discussion of bulk ingest, see the docs dir.
