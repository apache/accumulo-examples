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

After creating the tables, index some files. The following command indexes all the java files in the Accumulo source code.

    $ find /path/to/accumulo/core -name "*.java" | xargs ./bin/runex shard.Index -t examples.shard --partitions 30

The following command queries the index to find all files containing 'foo' and 'bar'.

    $ ./bin/runex shard.Query -t examples.shard foo bar
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/spi/balancer/BaseHostRegexTableLoadBalancerTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/iterators/user/WholeRowIteratorTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/iteratorsImpl/IteratorConfigUtilTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/data/KeyBuilderTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/spi/balancer/HostRegexTableLoadBalancerReconfigurationTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/security/ColumnVisibilityTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/summary/SummaryCollectionTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/spi/balancer/HostRegexTableLoadBalancerTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/client/IteratorSettingTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/data/KeyExtentTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/security/VisibilityEvaluatorTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/iterators/user/TransformingIteratorTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/client/admin/NewTableConfigurationTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/conf/HadoopCredentialProviderTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/clientImpl/TableOperationsHelperTest.java
      /path/to/accumulo/core/src/test/java/org/apache/accumulo/core/iterators/user/WholeColumnFamilyIteratorTest.java

In order to run ContinuousQuery, we need to run Reverse.java to populate the `examples.doc2term` table.

    $ ./bin/runex shard.Reverse --shardTable examples.shard --doc2Term examples.doc2term

Below ContinuousQuery is run using 5 terms. So it selects 5 random terms from each document, then it continually
randomly selects one set of 5 terms and queries. It prints the number of matching documents and the time in seconds.

    $ ./bin/runex shard.ContinuousQuery --shardTable examples.shard --doc2Term examples.doc2term --terms 5
      [string, protected, sizeopt, cache, build] 1  0.084
      [public, these, exception, to, as] 25  0.267   
      [by, encodeprevendrow, 0, work, as] 4  0.056
      [except, to, a, limitations, one] 969  0.197           
      [copy, as, asf, version, is] 969  0.341                                                 
      [core, class, may, regarding, without] 862  0.437
      [max_data_to_print, default_visibility_cache_size, use, accumulo_export_info, fate] 1  0.066


[Index.java]: ../src/main/java/org/apache/accumulo/examples/shard/Index.java
[Query.java]: ../src/main/java/org/apache/accumulo/examples/shard/Query.java
[Reverse.java]: ../src/main/java/org/apache/accumulo/examples/shard/Reverse.java
[ContinuousQuery.java]: ../src/main/java/org/apache/accumulo/examples/shard/ContinuousQuery.java
