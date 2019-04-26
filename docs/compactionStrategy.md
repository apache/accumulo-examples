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
# Apache Accumulo Customizing the Compaction Strategy

This tutorial uses the following Java classes, which can be found in org.apache.accumulo.tserver.compaction: 

 * DefaultCompactionStrategy.java - determines which files to compact based on table.compaction.major.ratio and table.file.max
 * EverythingCompactionStrategy.java - compacts all files
 * SizeLimitCompactionStrategy.java - compacts files no bigger than table.majc.compaction.strategy.opts.sizeLimit
 * BasicCompactionStrategy.java - uses default compression table.majc.compaction.strategy.opts.filter.size to filter input 
                                  files based on size set and table.majc.compaction.strategy.opts.large.compress.threshold
                                  and table.majc.compaction.strategy.opts.file.large.compress.type for larger files.                            
                                  

This is an example of how to configure a compaction strategy. By default Accumulo will always use the DefaultCompactionStrategy, unless 
these steps are taken to change the configuration.  Use the strategy and settings that best fits your Accumulo setup. This example shows
how to configure and test one of the more complicated strategies, the BasicCompactionStrategy. Note that this example requires hadoop
native libraries built with snappy in order to use snappy compression.

To begin, run the command to create a table for testing.

```bash
$ accumulo shell -u <username> -p <password> -e "createtable test1"
```

The commands below will configure the BasicCompactionStrategy to:
 - Avoid compacting files over 250M
 - Compact files over 100M using gz
 - Compact files less than 100M using snappy.
 
```bash
 $ accumulo shell -u <username> -p <password> -e "config -t test1 -s table.file.compress.type=snappy"
 $ accumulo shell -u <username> -p <password> -e "config -t test1 -s table.majc.compaction.strategy=org.apache.accumulo.tserver.compaction.strategies.BasicCompactionStrategy"
 $ accumulo shell -u <username> -p <password> -e "config -t test1 -s table.majc.compaction.strategy.opts.filter.size=250M"
 $ accumulo shell -u <username> -p <password> -e "config -t test1 -s table.majc.compaction.strategy.opts.large.compress.threshold=100M"
 $ accumulo shell -u <username> -p <password> -e "config -t test1 -s table.majc.compaction.strategy.opts.large.compress.type=gz"
```

Generate some data and files in order to test the strategy:

```bash
$ ./bin/runex client.SequentialBatchWriter -t test1 --start 0 --num 10000 --size 50
$ accumulo shell -u <username> -p <password> -e "flush -t test1"
$ ./bin/runex client.SequentialBatchWriter -t test1 --start 0 --num 11000 --size 50
$ accumulo shell -u <username> -p <password> -e "flush -t test1"
$ ./bin/runex client.SequentialBatchWriter -t test1 --start 0 --num 12000 --size 50
$ accumulo shell -u <username> -p <password> -e "flush -t test1"
$ ./bin/runex client.SequentialBatchWriter -t test1 --start 0 --num 13000 --size 50
$ accumulo shell -u <username> -p <password> -e "flush -t test1"
```

View the tserver log in <accumulo_home>/logs for the compaction and find the name of the <rfile> that was compacted for your table. Print info about this file using the PrintInfo tool:

```bash
$ accumulo rfile-info <rfile>
```
Details about the rfile will be printed and the compression type should match the type used in the compaction...

```bash    
Meta block     : RFile.index
      Raw size             : 319 bytes
      Compressed size      : 180 bytes
      Compression type     : gz
```