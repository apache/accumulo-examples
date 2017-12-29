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

 * [Flush.java] - flushes a table
 * [RowOperations.java] - reads and writes rows
 * [ReadWriteExample.java] - creates a table, writes to it, and reads from it

The Flush class will flush a table:

    $ ./bin/runex client.Flush -c ./examples.conf -t trace

The RowOperations class demonstrates how to read and write rows using the BatchWriter and Scanner:

    $ ./bin/runex client.RowOperations -c ./examples.conf
    2013-01-14 14:45:24,738 [client.RowOperations] INFO : This is everything
    2013-01-14 14:45:24,744 [client.RowOperations] INFO : Key: row1 column:1 [] 1358192724640 false Value: This is the value for this key
    2013-01-14 14:45:24,744 [client.RowOperations] INFO : Key: row1 column:2 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,744 [client.RowOperations] INFO : Key: row1 column:3 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,744 [client.RowOperations] INFO : Key: row1 column:4 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,746 [client.RowOperations] INFO : Key: row2 column:1 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,746 [client.RowOperations] INFO : Key: row2 column:2 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,746 [client.RowOperations] INFO : Key: row2 column:3 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,746 [client.RowOperations] INFO : Key: row2 column:4 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,747 [client.RowOperations] INFO : Key: row3 column:1 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,747 [client.RowOperations] INFO : Key: row3 column:2 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,747 [client.RowOperations] INFO : Key: row3 column:3 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,747 [client.RowOperations] INFO : Key: row3 column:4 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,756 [client.RowOperations] INFO : This is row1 and row3
    2013-01-14 14:45:24,757 [client.RowOperations] INFO : Key: row1 column:1 [] 1358192724640 false Value: This is the value for this key
    2013-01-14 14:45:24,757 [client.RowOperations] INFO : Key: row1 column:2 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,757 [client.RowOperations] INFO : Key: row1 column:3 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,757 [client.RowOperations] INFO : Key: row1 column:4 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,761 [client.RowOperations] INFO : Key: row3 column:1 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,761 [client.RowOperations] INFO : Key: row3 column:2 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,761 [client.RowOperations] INFO : Key: row3 column:3 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,761 [client.RowOperations] INFO : Key: row3 column:4 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,765 [client.RowOperations] INFO : This is just row3
    2013-01-14 14:45:24,769 [client.RowOperations] INFO : Key: row3 column:1 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,770 [client.RowOperations] INFO : Key: row3 column:2 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,770 [client.RowOperations] INFO : Key: row3 column:3 [] 1358192724642 false Value: This is the value for this key
    2013-01-14 14:45:24,770 [client.RowOperations] INFO : Key: row3 column:4 [] 1358192724642 false Value: This is the value for this key

To create a table, write to it and read from it:

    $ ./bin/runex client.ReadWriteExample -c ./examples.conf --createtable --create --read
    hello%00; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%01; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%02; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%03; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%04; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%05; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%06; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%07; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%08; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world
    hello%09; datatypes:xml [LEVEL1|GROUP1] 1358192329450 false -> world

[Flush.java]: ../src/main/java/org/apache/accumulo/examples/client/Flush.java
[RowOperations.java]: ../src/main/java/org/apache/accumulo/examples/client/RowOperations.java
[ReadWriteExample.java]: ../src/main/java/org/apache/accumulo/examples/client/ReadWriteExample.java
