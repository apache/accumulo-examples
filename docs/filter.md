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
# Apache Accumulo Filter Example

This is a simple filter example. It uses the AgeOffFilter that is provided as
part of the core package org.apache.accumulo.core.iterators.user. Filters are
iterators that select desired key/value pairs (or weed out undesired ones).
Filters extend the org.apache.accumulo.core.iterators.Filter class
and must implement a method accept(Key k, Value v). This method returns true
if the key/value pair are to be delivered and false if they are to be ignored.
Filter takes a "negate" parameter which defaults to false. If set to true, the
return value of the accept method is negated, so that key/value pairs accepted
by the method are omitted by the Filter.

    username@instance> createnamespace examples
    username@instance> createtable examples.filtertest
    username@instance examples.filtertest> setiter -t examples.filtertest -scan -p 10 -n myfilter -ageoff
    AgeOffFilter removes entries with timestamps more than <ttl> milliseconds old
    ----------> set AgeOffFilter parameter negate, default false keeps k/v that pass accept method, true rejects k/v that pass accept method:
    ----------> set AgeOffFilter parameter ttl, time to live (milliseconds): 30000
    ----------> set AgeOffFilter parameter currentTime, if set, use the given value as the absolute time in milliseconds as the current time of day:
    username@instance examples.filtertest> scan
    username@instance examples.filtertest> insert foo a b c
    username@instance examples.filtertest> scan
    foo a:b []    c
    username@instance examples.filtertest>

... wait 30 seconds ...

    username@instance examples.filtertest> scan
    username@instance examples.filtertest>

Note the absence of the entry inserted more than 30 seconds ago. Since the
scope was set to "scan", this means the entry is still in Accumulo, but is
being filtered out at query time. To delete entries from Accumulo based on
the ages of their timestamps, AgeOffFilters should be set up for the "minc"
and "majc" scopes, as well.

To force an ageoff of the persisted data, after setting up the ageoff iterator
on the "minc" and "majc" scopes you can flush and compact your table. This will
happen automatically as a background operation on any table that is being
actively written to, but can also be requested in the shell.

The first setiter command used the special -ageoff flag to specify the
AgeOffFilter, but any Filter can be configured by using the -class flag. The
following commands show how to enable the AgeOffFilter for the minc and majc
scopes using the -class flag, then flush and compact the table.

    username@instance examples.filtertest> setiter -t examples.filtertest -minc -majc -p 10 -n myfilter -class org.apache.accumulo.core.iterators.user.AgeOffFilter
    AgeOffFilter removes entries with timestamps more than <ttl> milliseconds old
    ----------> set AgeOffFilter parameter negate, default false keeps k/v that pass accept method, true rejects k/v that pass accept method:
    ----------> set AgeOffFilter parameter ttl, time to live (milliseconds): 30000
    ----------> set AgeOffFilter parameter currentTime, if set, use the given value as the absolute time in milliseconds as the current time of day:
    username@instance examples.filtertest> flush
    06 10:42:24,806 [shell.Shell] INFO : Flush of table examples.filtertest initiated...
    username@instance examples.filtertest> compact
    06 10:42:36,781 [shell.Shell] INFO : Compaction of table examples.filtertest started for given range
    username@instance examples.filtertest> flush -t examples.filtertest -w
    06 10:42:52,881 [shell.Shell] INFO : Flush of table examples.filtertest completed.
    username@instance examples.filtertest> compact -t examples.filtertest -w
    06 10:43:00,632 [shell.Shell] INFO : Compacting table ...
    06 10:43:01,307 [shell.Shell] INFO : Compaction of table examples.filtertest completed for given range
    username@instance examples.filtertest>

By default, flush and compact execute in the background, but with the -w flag
they will wait to return until the operation has completed. Both are
demonstrated above, though only one call to each would be necessary. A
specific table can be specified with -t.

After the compaction runs, the newly created files will not contain any data
that should have been aged off, and the Accumulo garbage collector will remove
the old files.

To see the iterator settings for a table, use config.

    username@instance examples.filtertest> config -t examples.filtertest -f iterator
    ---------+---------------------------------------------+---------------------------------------------------------------------------
    SCOPE    | NAME                                        | VALUE
    ---------+---------------------------------------------+---------------------------------------------------------------------------
    table    | table.iterator.majc.myfilter .............. | 10,org.apache.accumulo.core.iterators.user.AgeOffFilter
    table    | table.iterator.majc.myfilter.opt.ttl ...... | 30000
    table    | table.iterator.majc.vers .................. | 20,org.apache.accumulo.core.iterators.user.VersioningIterator
    table    | table.iterator.majc.vers.opt.maxVersions .. | 1
    table    | table.iterator.minc.myfilter .............. | 10,org.apache.accumulo.core.iterators.user.AgeOffFilter
    table    | table.iterator.minc.myfilter.opt.ttl ...... | 30000
    table    | table.iterator.minc.vers .................. | 20,org.apache.accumulo.core.iterators.user.VersioningIterator
    table    | table.iterator.minc.vers.opt.maxVersions .. | 1
    table    | table.iterator.scan.myfilter .............. | 10,org.apache.accumulo.core.iterators.user.AgeOffFilter
    table    | table.iterator.scan.myfilter.opt.ttl ...... | 30000
    table    | table.iterator.scan.vers .................. | 20,org.apache.accumulo.core.iterators.user.VersioningIterator
    table    | table.iterator.scan.vers.opt.maxVersions .. | 1
    ---------+---------------------------------------------+---------------------------------------------------------------------------
    username@instance examples.filtertest>

When setting new iterators, make sure to order their priority numbers
(specified with -p) in the order you would like the iterators to be applied.
Also, each iterator must have a unique name and priority within each scope.
