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
# Apache Accumulo Batch Writing and Scanning Example

This tutorial uses the following Java classes:

 * [SequentialBatchWriter.java] - writes mutations with sequential rows and random values
 * [RandomBatchWriter.java] - used by SequentialBatchWriter to generate random values
 * [RandomBatchScanner.java] - reads random rows and verifies their values

This is an example of how to use the BatchWriter and BatchScanner.

First, you must ensure that the user you are running with (i.e `myuser` below) has the
`exampleVis` authorization.

    $ accumulo shell -u root -e "setauths -u myuser -s exampleVis"

Second, you must create the table, batchtest1, ahead of time.

    $ accumulo shell -u root -e "createtable batchtest1"

The command below adds 10000 entries with random 50 bytes values to Accumulo.

    $ ./bin/runex client.SequentialBatchWriter -c ./examples.conf -t batchtest1 --start 0 --num 10000 --size 50 --batchMemory 20M --batchLatency 500 --batchThreads 20 --vis exampleVis

The command below will do 100 random queries.

    $ ./bin/runex client.RandomBatchScanner -c ./examples.conf -t batchtest1 --num 100 --min 0 --max 10000 --size 50 --scanThreads 20 --auths exampleVis

    07 11:33:11,103 [client.CountingVerifyingReceiver] INFO : Generating 100 random queries...
    07 11:33:11,112 [client.CountingVerifyingReceiver] INFO : finished
    07 11:33:11,260 [client.CountingVerifyingReceiver] INFO : 694.44 lookups/sec   0.14 secs

    07 11:33:11,260 [client.CountingVerifyingReceiver] INFO : num results : 100

    07 11:33:11,364 [client.CountingVerifyingReceiver] INFO : Generating 100 random queries...
    07 11:33:11,370 [client.CountingVerifyingReceiver] INFO : finished
    07 11:33:11,416 [client.CountingVerifyingReceiver] INFO : 2173.91 lookups/sec   0.05 secs

    07 11:33:11,416 [client.CountingVerifyingReceiver] INFO : num results : 100

[SequentialBatchWriter.java]: ../src/main/java/org/apache/accumulo/examples/client/SequentialBatchWriter.java
[RandomBatchWriter.java]:  ../src/main/java/org/apache/accumulo/examples/client/RandomBatchWriter.java
[RandomBatchScanner.java]: ../src/main/java/org/apache/accumulo/examples/client/RandomBatchScanner.java
