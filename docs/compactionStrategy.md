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

This is an example of how to configure a compaction strategy. By default, Accumulo will always use the DefaultCompactionStrategy, unless 
these steps are taken to change the configuration.  Use the strategy and settings that best fits your Accumulo setup. This example shows
how to configure a non-default strategy. Note that this example requires hadoop native libraries built with snappy in order to 
use snappy compression. Within this example, commands starting with `user@uno>` are run from within the Accumulo shell, whereas
commands beginning with `$` are executed from the command line terminal.

Start by creating a table that will be used for the compactions.

    user@uno> createnamespace examples
    user@uno> createtable examples.test1

Take note of the TableID for examples.test1. This will be needed later. The TableID can be found by running:


    user@uno> tables -l
    accumulo.metadata    =>        !0
    accumulo.replication =>      +rep
    accumulo.root        =>        +r
    examples.test1       =>         2

The commands below will configure the desired compaction strategy. The goals are:

 - Avoid compacting files over 250M.
 - Compact files over 100M using gz.
 - Compact files less than 100M using snappy.
 - Limit the compaction throughput to 40MB/s.

Create a compaction service named `cs1` that has three executors. The first executor named `small` has 
8 threads and runs compactions less than 16M. The second executor, `medium`, runs compactions less than 
128M with 4 threads. The last executor, `large`, runs all other compactions with 2 threads.

    user@uno> config -s tserver.compaction.major.service.cs1.planner=org.apache.accumulo.core.spi.compaction.DefaultCompactionPlanner
    user@uno> config -s 'tserver.compaction.major.service.cs1.planner.opts.executors=[{"name":"small","type":"internal","maxSize":"16M","numThreads":8},{"name":"medium","type":"internal","maxSize":"128M","numThreads":4},{"name":"large","type":"internal","numThreads":2}]'

Create a compaction service named `cs2` that has three executors. It has a similar configuration to `cs1`, but its 
executors have fewer threads. For service, `cs2`, files over 250M should not be compacted. It also limits 
the total I/O of all compactions within the service to 40MB/s.

    user@uno> config -s tserver.compaction.major.service.cs2.planner=org.apache.accumulo.core.spi.compaction.DefaultCompactionPlanner
    user@uno> config -s 'tserver.compaction.major.service.cs2.planner.opts.executors=[{"name":"small","type":"internal","maxSize":"16M","numThreads":4},{"name":"medium","type":"internal","maxSize":"128M","numThreads":2},{"name":"large","type":"internal","maxSize":"250M","numThreads":1}]' 
    user@uno> config -s tserver.compaction.major.service.cs2.rate.limit=40M

Configurations can be verified for correctness with the  `check-compaction-config` tool in 
Accumulo. Place your compaction configuration into a file and run the tool. For example, if you create a file
`myconfig` that contains the following:

    tserver.compaction.major.service.cs1.planner=org.apache.accumulo.core.spi.compaction2.DefaultCompactionPlanner
    tserver.compaction.major.service.cs1.planner.opts.executors=[{"name":"small","type":"internal","maxSize":"16M","numThreads":8},{"name":"medium","type":"internal","maxSize":"128M","numThreads":4},{"name":"large","type":"internal","numThreads":2}]
    tserver.compaction.major.service.cs2.planner=org.apache.accumulo.core.spi.compaction.DefaultCompactionPlanner
    tserver.compaction.major.service.cs2.planner.opts.executors=[{"name":"small","type":"internal","maxSize":"16M","numThreads":4},{"name":"medium","type":"internal","maxSize":"128M","numThreads":2},{"name":"large","type":"internal","maxSize":"250M","numThreads":1}]
    tserver.compaction.major.service.cs2.rate.limit=40M

The following command would check the configuration for errors:

    $ accumulo check-compaction-config /path/to/myconfig


With the compaction configuration set, configure table specific properties.

Configure the compression for table `examples.test1`. Files over 100M will be compressed using `gz`. All
others will be compressed via `snappy`.

    user@uno> config -t examples.test1 -s table.compaction.configurer=org.apache.accumulo.core.client.admin.compaction.CompressionConfigurer
    user@uno> config -t examples.test1 -s table.compaction.configurer.opts.large.compress.threshold=100M
    user@uno> config -t examples.test1 -s table.compaction.configurer.opts.large.compress.type=gz
    user@uno> config -t examples.test1 -s table.file.compress.type=snappy
    user@uno> config -t examples.test1 -s table.compaction.dispatcher=org.apache.accumulo.core.spi.compaction.SimpleCompactionDispatcher

Set table `examples.test1` to use compaction service `cs1` for system compactions and service `cs2`
for user compactions.

    user@uno> config -t examples.test1 -s table.compaction.dispatcher.opts.service=cs1
    user@uno> config -t examples.test1 -s table.compaction.dispatcher.opts.service.user=cs2

If needed, `chop` compactions can be configured also.
    
    user@uno> config -t examples.test1 -s table.compaction.dispatcher.opts.service.chop=cs2

Generate some data and files in order to test the strategy:

    $ ./bin/runex client.SequentialBatchWriter -t examples.test1 --start 0 --num 1000 --size 50
    $ accumulo shell -u <username> -p <password> -e "flush -t examples.test1"

    $ ./bin/runex client.SequentialBatchWriter -t examples.test1 --start 0 --num 2000 --size 50
    $ accumulo shell -u <username> -p <password> -e "flush -t examples.test1"

    $ accumulo shell -u <username> -p <password> -e "compact -t examples.test1 -w"

View the `tserver` log in <accumulo_home>/logs for the compaction and find the name of the `rfile` that was
compacted for your table. Print info about this file using the `rfile-info` tool. Replace the TableID with
the TableID from above. Note, your filenames will differ from the ones within this example.

    accumulo rfile-info hdfs:///accumulo/tables/2/default_tablet/A000000a.rf

Details about the rfile will be printed. The compression type should match the type used in the compaction.
In this case, `snappy` is used since the size is less than 100M.

```bash    
Meta block     : RFile.index
      Raw size             : 168 bytes
      Compressed size      : 127 bytes
      Compression type     : snappy
```

Continue to add additional data.

    $ ./bin/runex client.SequentialBatchWriter -t examples.test1 --start 0 --num 1000000 --size 50
    $ accumulo shell -u <username> -p <password> -e "flush -t examples.test1"

    $ ./bin/runex client.SequentialBatchWriter -t examples.test1 --start 1000000 --num 1000000 --size 50
    $ accumulo shell -u <username> -p <password> -e "flush -t examples.test1"

    $ ./bin/runex client.SequentialBatchWriter -t examples.test1 --start 2000000 --num 1000000 --size 50
    $ accumulo shell -u <username> -p <password> -e "flush -t examples.test1"

    $ accumulo shell -u <username> -p <password> -e "compact -t examples.test1 -w"

Again, view the tserver log in <accumulo_home>/logs for the compaction and find the name of the `rfile` that was
compacted for your table. Print info about this file using the `rfile-info` tool:

    accumulo rfile-info hdfs:///accumulo/tables/2/default_tablet/A000000o.rf

In this case, the compression type should be `gz`. 

```bash    
Meta block     : RFile.index
      Raw size             : 56,044 bytes
      Compressed size      : 21,460 bytes
      Compression type     : gz
```

Examining the size of `A000000o.rf` within HDFS should verify that the rfile is greater than 100M. 

    $ hdfs dfs -ls -h /accumulo/tables/2/default_tablet/A000000o.rf
