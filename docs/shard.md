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
# Apache Accumulo Shard Example

Accumulo has an iterator called the intersecting iterator which supports querying a term index that is partitioned by
document, or "sharded". This example shows how to use the intersecting iterator through these four programs:

 * [Index.java] - Indexes a set of text files into an Accumulo table
 * [Query.java] - Finds documents containing a given set of terms.
 * [Reverse.java] - Reads the index table and writes a map of documents to terms into another table.
 * [ContinuousQuery.java] - Uses the table populated by Reverse.java to select N random terms per document. Then it continuously and randomly queries those terms.

To run these example programs, create two tables like below.

    username@instance> createnamespace examples
    username@instance> createtable examples.shard
    username@instance examples.shard> createtable examples.doc2term

After creating the tables, index some files. The following command indexes all of the java files in the Accumulo source code.

    $ find /path/to/accumulo/core -name "*.java" | xargs ./bin/runex shard.Index -t examples.shard --partitions 30

The following command queries the index to find all files containing 'foo' and 'bar'.

    $ ./bin/runex shard.Query -t examples.shard foo bar
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/replication/ReplicationTargetTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/client/admin/NewTableConfigurationTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/spi/balancer/HostRegexTableLoadBalancerTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/data/KeyExtentTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/iterators/user/WholeRowIteratorTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/iterators/user/WholeColumnFamilyIteratorTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/data/KeyBuilderTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/security/ColumnVisibilityTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/conf/IterConfigUtilTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/summary/SummaryCollectionTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/clientImpl/TableOperationsHelperTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/clientImpl/mapreduce/BatchInputSplitTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/spi/balancer/HostRegexTableLoadBalancerReconfigurationTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/client/IteratorSettingTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/client/mapred/RangeInputSplitTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/iterators/user/TransformingIteratorTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/spi/balancer/BaseHostRegexTableLoadBalancerTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/conf/HadoopCredentialProviderTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/client/mapreduce/AccumuloInputFormatTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/replication/ReplicationSchemaTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/client/mapreduce/RangeInputSplitTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/security/VisibilityEvaluatorTest.java


In order to run ContinuousQuery, we need to run Reverse.java to populate the `examples.doc2term` table.

    $ ./bin/runex shard.Reverse --shardTable examples.shard --doc2Term examples.doc2term

Below ContinuousQuery is run using 5 terms. So it selects 5 random terms from each document, then it continually
randomly selects one set of 5 terms and queries. It prints the number of matching documents and the time in seconds.

    $ ./bin/runex shard.ContinuousQuery --shardTable examples.shard --doc2Term examples.doc2term --terms 5
    [public, core, class, binarycomparable, b] 2  0.081
    [wordtodelete, unindexdocument, doctablename, putdelete, insert] 1  0.041
    [import, columnvisibilityinterpreterfactory, illegalstateexception, cv, columnvisibility] 1  0.049
    [getpackage, testversion, util, version, 55] 1  0.048
    [for, static, println, public, the] 55  0.211
    [sleeptime, wrappingiterator, options, long, utilwaitthread] 1  0.057
    [string, public, long, 0, wait] 12  0.132

[Index.java]: ../src/main/java/org/apache/accumulo/examples/shard/Index.java
[Query.java]: ../src/main/java/org/apache/accumulo/examples/shard/Query.java
[Reverse.java]: ../src/main/java/org/apache/accumulo/examples/shard/Reverse.java
[ContinuousQuery.java]: ../src/main/java/org/apache/accumulo/examples/shard/ContinuousQuery.java
