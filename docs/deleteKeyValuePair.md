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
# Apache Accumulo Key-Value Pair Removal Example

This example shows how Accumulo internals handle removing a key-value pair

```
$ /path/to/accumulo shell -u username -p secret
username@instance> createtable deleteKeyValuePair
username@instance deleteKeyValuePair> insert 567890 name first Joe
username@instance deleteKeyValuePair> insert 567890 name last Smith
username@instance deleteKeyValuePair> insert 567890 address city Columbia
```

```
username@instance deleteKeyValuePair> scan
567890 address:city []    Columbia
567890 name:first []    Joe
567890 name:last []    Smith
```

```
username@instance deleteKeyValuePair> flush -w
2019-04-18 11:01:47,444 [shell.Shell] INFO : Flush of table deleteKeyValuePair completed.
```

Get the deleteKeyValuePair table id.

```
username@instance deleteKeyValuePair> tables -l
accumulo.metadata    =>        !0
accumulo.replication =>      +rep
accumulo.root        =>        +r
deleteKeyValuePair   =>        1t
```

Scan accumulo.metadata table to see the list of RFiles Accumulo is currently using.

```
username@instance deleteKeyValuePair> scan -t accumulo.metadata -c file -r 1t<
1t< file:hdfs://localhost:8020/accumulo/tables/1t/default_tablet/F00007em.rf
```

View the contents of RFile and verify each key-value pair's deletion flag is false.

```
$ /path/to/accumulo rfile-info -d hdfs://localhost/accumulo/tables/1t/default_tablet/F00007em.rf

Locality group           : <DEFAULT>
    Num   blocks           : 1
    Index level 0          : 37 bytes  1 blocks
    First key              : 567890 address:city [] 1555418057811 false
    Last key               : 567890 name:last [] 1555418063052 false
    Num entries            : 3
    Column families        : [address, name]

Meta block     : BCFile.index
      Raw size             : 4 bytes
      Compressed size      : 12 bytes
      Compression type     : gz

Meta block     : RFile.index
      Raw size             : 141 bytes
      Compressed size      : 97 bytes
      Compression type     : gz
567890 address:city [] 1555418057811 false -> Columbia
567890 name:first [] 1555418067848 false -> Joe
567890 name:last [] 1555418063052 false -> Smith
```
Delete a key-value pair and view a newly created RFile to verify the deletion flag is true.

```
$ /path/to/accumulo shell -u username -p secret
username@instance> table deleteKeyValuePair
username@instance deleteKeyValuePair> delete 567890 name first
username@instance deleteKeyValuePair> flush -w
```

```
username@instance deleteKeyValuePair> scan -t accumulo.metadata -c file -r 1t<
1t< file:hdfs://localhost:8020/accumulo/tables/1t/default_tablet/F00007em.rf
1t< file:hdfs://localhost:8020/accumulo/tables/1t/default_tablet/F00007fq.rf
```

```
$ /path/to/accumulo rfile-info -d hdfs://localhost/accumulo/tables/1t/default_tablet/F00007fq.rf

Locality group           : <DEFAULT>
    Num   blocks           : 1
    Index level 0          : 38 bytes  1 blocks
    First key              : 567890 name:first [] 1555419184531 true
    Last key               : 567890 name:first [] 1555419184531 true
    Num entries            : 1
    Column families        : [name]

Meta block     : BCFile.index
      Raw size             : 4 bytes
      Compressed size      : 12 bytes
      Compression type     : gz

Meta block     : RFile.index
      Raw size             : 121 bytes
      Compressed size      : 68 bytes
      Compression type     : gz

567890 name:first [] 1555419184531 true ->
```

Compact the RFiles and verify the key-value pair was removed.  The new RFile will start with 'A'.

```
$ /path/to/accumulo shell -u username -p secret
username@instance deleteKeyValuePair> compact -t deleteKeyValuePair -w
2019-04-17 08:17:15,468 [shell.Shell] INFO : Compacting table ...
2019-04-17 08:17:16,143 [shell.Shell] INFO : Compaction of table deleteKeyValuePair completed for given range
```

```
username@instance deleteKeyValuePair> scan -t accumulo.metadata -c file -r 1t<
lt< file:hdfs://localhost:8020/accumulo/tables/1t/default_tablet/A00007g1.rf
```

 ```
$ /path/to/accumulo rfile-info -v hdfs://localhost/accumulo/tables/1t/default_tablet/A00007g1.rf

Locality group           : <DEFAULT>
    Num   blocks           : 1
    Index level 0          : 37 bytes  1 blocks
    First key              : 567890 address:city [] 1555418057811 false
    Last key               : 567890 name:last [] 1555418063052 false
    Num entries            : 2
    Column families        : [address, name]

Meta block     : BCFile.index
      Raw size             : 4 bytes
      Compressed size      : 12 bytes
      Compression type     : gz

Meta block     : RFile.index
      Raw size             : 141 bytes
      Compressed size      : 96 bytes
      Compression type     : gz

567890 address:city [] 1555418057811 false -> Columbia
567890 name:last [] 1555418063052 false -> Smith
```
