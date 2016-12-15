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
# Apache Accumulo Release Testing

This repository contains an integration test (IT) that runs all of the
examples.  This can be used for testing Accumulo release candidates (RC). To
run the IT against a RC add the following to `~/.m2/settings.xml` changing
`XXXX` to the proper id for a given RC.

```xml
 <profiles>
   <profile>
     <id>rcAccumulo</id>
     <repositories>
       <repository>
         <id>accrc</id>
         <name>accrcp</name>
         <url>https://repository.apache.org/content/repositories/orgapacheaccumulo-XXXX</url>
       </repository>
     </repositories>
     <pluginRepositories>
       <pluginRepository>
         <id>accrcp</id>
         <name>accrcp</name>
         <url>https://repository.apache.org/content/repositories/orgapacheaccumulo-XXX</url>
       </pluginRepository>
     </pluginRepositories>
   </profile>
 </profiles>
```

After adding that, you can run the following command in this repository to run the IT.

```
mvn clean verify -PrcAccumulo -Daccumulo.version=$ACCUMULO_RC_VERSION
```
