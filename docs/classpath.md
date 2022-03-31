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
# Apache Accumulo Classpath Example

This example shows how to use per table classpaths. The example leverages a
test jar which contains a Filter that suppresses rows containing "foo". The
example shows copying the FooFilter.jar into HDFS and then making an Accumulo
table reference that jar. For this example, a directory, `/user1/lib`, is
assumed to exist in HDFS.

Create `/user1/lib` in HDFS if it does not exist.

    hadoop fs -mkdir -p /user1/lib

Execute the following command in the shell. Note that the `FooFilter.jar`
is located within the Accumulo source distribution. 

    $ hadoop fs -copyFromLocal /path/to/accumulo/test/src/main/resources/org/apache/accumulo/test/FooFilter.jar /user1/lib

Execute following in Accumulo shell to setup classpath context

    root@uno> config -s general.vfs.context.classpath.cx1=hdfs://<namenode host>:<namenode port>/user1/lib/[^.].*.jar

Create a namespace and table

    root@uno> createnamespace examples
    root@uno> createtable examples.nofoo

The following command makes this table use the configured classpath context

    root@uno examples.nofoo> config -t examples.nofoo -s table.class.loader.context=cx1

The following command configures an iterator that's in FooFilter.jar

    root@uno examples.nofoo> setiter -n foofilter -p 10 -scan -minc -majc -class org.apache.accumulo.test.FooFilter
    Filter accepts or rejects each Key/Value pair
    ----------> set FooFilter parameter negate, default false keeps k/v that pass accept method, true rejects k/v that pass accept method: false

The commands below show the filter is working.

    root@uno examples.nofoo> insert foo1 f1 q1 v1
    root@uno examples.nofoo> insert noo1 f1 q1 v2
    root@uno examples.nofoo> scan
    noo1 f1:q1 []    v2
    root@uno examples.nofoo>

Below, an attempt is made to add the FooFilter to a table that's not configured
to use the classpath context cx1. This fails until the table is configured to
use cx1.

    root@uno examples.nofoo> createtable examples.nofootwo
    root@uno examples.nofootwo> setiter -n foofilter -p 10 -scan -minc -majc -class org.apache.accumulo.test.FooFilter
        2013-05-03 12:49:35,943 [shell.Shell] ERROR: org.apache.accumulo.shell.ShellCommandException: Command could 
    not be initialized (Unable to load org.apache.accumulo.test.FooFilter; class not found.)
    root@uno examples.nofootwo> config -t nofootwo -s table.class.loader.context=cx1
    root@uno examples.nofootwo> setiter -n foofilter -p 10 -scan -minc -majc -class org.apache.accumulo.test.FooFilter
    Filter accepts or rejects each Key/Value pair
    ----------> set FooFilter parameter negate, default false keeps k/v that pass accept method, true rejects k/v that pass accept method: false


