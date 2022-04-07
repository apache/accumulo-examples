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
# Apache Accumulo Tracing Example

This tutorial uses the [TracingExample.java] Java class to create an OpenTelemetry
span in the TracingExample application and then create and read entries from Accumulo.
Tracing output should show up in logs for the TracingExample class and the Accumulo client,
and logs for the Accumulo server processes.

## Setup

  1. Download the OpenTelemetry [JavaAgent] jar file and place it into the `/path/to/accumulo/lib/` directory.
  2. Add the property `general.opentelemetry.enabled=true` to accumulo.properties
  3. Set the following environment variables in your environment:
  
    ACCUMULO_JAVA_OPTS="-javaagent:/path/to/accumulo/lib/opentelemetry-javaagent-1.12.1.jar"
    OTEL_TRACES_EXPORTER="logging"

## Run the Example

  1. Start Accumulo
  2. Run the Tracing Example:
  
    $ ./bin/runex client.TracingExample --createtable --deletetable --create --read --table traceTest

[JavaAgent]: https://search.maven.org/remotecontent?filepath=io/opentelemetry/javaagent/opentelemetry-javaagent/1.12.1/opentelemetry-javaagent-1.12.1.jar
[TracingExample.java]: ../src/main/java/org/apache/accumulo/examples/client/TracingExample.java
