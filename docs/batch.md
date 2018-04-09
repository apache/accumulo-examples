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

This is an example of how to use the BatchWriter and BatchScanner.

This tutorial uses the following Java classes.

 * [SequentialBatchWriter.java] - writes mutations with sequential rows and random values
 * [RandomBatchScanner.java] - reads random rows and verifies their values

Run `SequentialBatchWriter` to add 10000 entries with random 50 bytes values to Accumulo.

    $ ./bin/runex client.SequentialBatchWriter

Verify data was ingested by scanning the table using the Accumulo shell:

    $ accumulo shell
    root@instance> table batch
    root@instance batch> scan

Run `RandomBatchScanner` to perform 1000 random queries and verify the results.

    $ ./bin/runex client.RandomBatchScanner
    16:04:05,950 [examples.client.RandomBatchScanner] INFO : Generating 1000 random ranges for BatchScanner to read
    16:04:06,020 [examples.client.RandomBatchScanner] INFO : Reading ranges using BatchScanner
    16:04:06,283 [examples.client.RandomBatchScanner] TRACE: 100 lookups
    16:04:06,290 [examples.client.RandomBatchScanner] TRACE: 200 lookups
    16:04:06,294 [examples.client.RandomBatchScanner] TRACE: 300 lookups
    16:04:06,297 [examples.client.RandomBatchScanner] TRACE: 400 lookups
    16:04:06,301 [examples.client.RandomBatchScanner] TRACE: 500 lookups
    16:04:06,304 [examples.client.RandomBatchScanner] TRACE: 600 lookups
    16:04:06,307 [examples.client.RandomBatchScanner] TRACE: 700 lookups
    16:04:06,309 [examples.client.RandomBatchScanner] TRACE: 800 lookups
    16:04:06,316 [examples.client.RandomBatchScanner] TRACE: 900 lookups
    16:04:06,320 [examples.client.RandomBatchScanner] TRACE: 1000 lookups
    16:04:06,330 [examples.client.RandomBatchScanner] INFO : Scan finished! 3246.75 lookups/sec, 0.31 secs, 1000 results
    16:04:06,331 [examples.client.RandomBatchScanner] INFO : All expected rows were scanned

[SequentialBatchWriter.java]: ../src/main/java/org/apache/accumulo/examples/client/SequentialBatchWriter.java
[RandomBatchWriter.java]:  ../src/main/java/org/apache/accumulo/examples/client/RandomBatchWriter.java
[RandomBatchScanner.java]: ../src/main/java/org/apache/accumulo/examples/client/RandomBatchScanner.java
