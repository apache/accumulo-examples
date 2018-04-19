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
# Apache Accumulo Constraints Example

This tutorial uses the following Java classes, which can be found in org.apache.accumulo.examples.constraints:

 * [AlphaNumKeyConstraint.java] - a constraint that requires alphanumeric keys
 * [NumericValueConstraint.java] - a constraint that requires numeric string values
 * [MaxMutationSize.java] - a constraint that limits the size of mutations accepted into a table

Remember to copy the accumulo-examples-\*.jar to Accumulo's 'lib/ext' directory.

AlphaNumKeyConstraint prevents insertion of keys with characters not between aA and zZ or 0 to 9.  
NumericValueConstraint prevents insertion of values with characters not between 0 and 9. The examples create mutations
that violate the constraint, throwing an exception.

    $ ./bin/runex constraints.AlphaNumKeyConstraint
    $ ./bin/runex constraints.NumericValueConstraint

The MaxMutationSize constraint will force the table to reject any mutation that is larger than 1/256th of the
working memory of the tablet server.  The following example attempts to ingest a single row with a million columns,
which exceeds the memory limit. Depending on the amount of Java heap your tserver(s) are given, you may have to
increase the number of columns provided to see the failure.

    $ ./bin/runex constraints.MaxMutationSize

[AlphaNumKeyConstraint.java]: ../src/main/java/org/apache/accumulo/examples/constraints/AlphaNumKeyConstraint.java
[NumericValueConstraint.java]: ../src/main/java/org/apache/accumulo/examples/constraints/NumericValueConstraint.java
[MaxMutationSize.java]: ../src/main/java/org/apache/accumulo/examples/constraints/MaxMutationSize.java


