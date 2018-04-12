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
# Apache Accumulo Client Examples

The following Java classes are examples of the Accumulo client API:

 * [RowOperations.java] - reads and writes rows
 * [ReadWriteExample.java] - creates a table, writes to it, and reads from it

[RowOperations.java] demonstrates how to read, write and delete rows using the BatchWriter and Scanner:

    $ ./bin/runex client.RowOperations
    [examples.client.RowOperations] INFO : This is only row2
    [examples.client.RowOperations] INFO : Key: row2 col:1 [] 1523301597006 false Value: v1
    [examples.client.RowOperations] INFO : Key: row2 col:2 [] 1523301597006 false Value: v2
    [examples.client.RowOperations] INFO : Key: row2 col:3 [] 1523301597006 false Value: v3
    [examples.client.RowOperations] INFO : This is everything
    [examples.client.RowOperations] INFO : Key: row1 col:1 [] 1523301597006 false Value: v1
    [examples.client.RowOperations] INFO : Key: row1 col:2 [] 1523301597006 false Value: v2
    [examples.client.RowOperations] INFO : Key: row1 col:3 [] 1523301597006 false Value: v3
    [examples.client.RowOperations] INFO : Key: row2 col:1 [] 1523301597006 false Value: v1
    [examples.client.RowOperations] INFO : Key: row2 col:2 [] 1523301597006 false Value: v2
    [examples.client.RowOperations] INFO : Key: row2 col:3 [] 1523301597006 false Value: v3
    [examples.client.RowOperations] INFO : Key: row3 col:1 [] 1523301597006 false Value: v1
    [examples.client.RowOperations] INFO : Key: row3 col:2 [] 1523301597006 false Value: v2
    [examples.client.RowOperations] INFO : Key: row3 col:3 [] 1523301597006 false Value: v3
    [examples.client.RowOperations] INFO : This is row1 and row3
    [examples.client.RowOperations] INFO : Key: row1 col:1 [] 1523301597006 false Value: v1
    [examples.client.RowOperations] INFO : Key: row1 col:2 [] 1523301597006 false Value: v2
    [examples.client.RowOperations] INFO : Key: row1 col:3 [] 1523301597006 false Value: v3
    [examples.client.RowOperations] INFO : Key: row3 col:1 [] 1523301597006 false Value: v1
    [examples.client.RowOperations] INFO : Key: row3 col:2 [] 1523301597006 false Value: v2
    [examples.client.RowOperations] INFO : Key: row3 col:3 [] 1523301597006 false Value: v3
    [examples.client.RowOperations] INFO : This is just row3
    [examples.client.RowOperations] INFO : Key: row3 col:1 [] 1523301597006 false Value: v1
    [examples.client.RowOperations] INFO : Key: row3 col:2 [] 1523301597006 false Value: v2
    [examples.client.RowOperations] INFO : Key: row3 col:3 [] 1523301597006 false Value: v3

To create a table, write to it and read from it:

    $ ./bin/runex client.ReadWriteExample
    [examples.client.ReadWriteExample] INFO : hello0 cf:cq [] 1523306675130 false -> world0
    [examples.client.ReadWriteExample] INFO : hello1 cf:cq [] 1523306675130 false -> world1
    [examples.client.ReadWriteExample] INFO : hello2 cf:cq [] 1523306675130 false -> world2
    [examples.client.ReadWriteExample] INFO : hello3 cf:cq [] 1523306675130 false -> world3
    [examples.client.ReadWriteExample] INFO : hello4 cf:cq [] 1523306675130 false -> world4
    [examples.client.ReadWriteExample] INFO : hello5 cf:cq [] 1523306675130 false -> world5
    [examples.client.ReadWriteExample] INFO : hello6 cf:cq [] 1523306675130 false -> world6
    [examples.client.ReadWriteExample] INFO : hello7 cf:cq [] 1523306675130 false -> world7
    [examples.client.ReadWriteExample] INFO : hello8 cf:cq [] 1523306675130 false -> world8
    [examples.client.ReadWriteExample] INFO : hello9 cf:cq [] 1523306675130 false -> world9

[Flush.java]: ../src/main/java/org/apache/accumulo/examples/client/Flush.java
[RowOperations.java]: ../src/main/java/org/apache/accumulo/examples/client/RowOperations.java
[ReadWriteExample.java]: ../src/main/java/org/apache/accumulo/examples/client/ReadWriteExample.java
