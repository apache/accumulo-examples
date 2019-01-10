/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.examples.cli;

import java.nio.file.Paths;
import java.util.Properties;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.hadoop.conf.Configuration;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

public class ClientOpts extends Help {

  public static class AuthConverter implements IStringConverter<Authorizations> {
    @Override
    public Authorizations convert(String value) {
      return new Authorizations(value.split(","));
    }
  }

  public static class VisibilityConverter implements IStringConverter<ColumnVisibility> {
    @Override
    public ColumnVisibility convert(String value) {
      return new ColumnVisibility(value);
    }
  }

  @Parameter(names = {"-c", "--conf"}, description = "Path to accumulo-client.properties."
      + "If not set, defaults to path set by env variable ACCUMULO_CLIENT_PROPS.")
  private String propsPath = null;

  @Parameter(names = {"-auths", "--auths"}, converter = AuthConverter.class,
      description = "the authorizations to use when reading or writing")
  public Authorizations auths = Authorizations.EMPTY;

  private Properties cachedProps = null;

  public AccumuloClient createAccumuloClient() {
    return Accumulo.newClient().from(getClientPropsPath()).build();
  }

  public String getClientPropsPath() {
    if (propsPath == null) {
      propsPath = System.getenv("ACCUMULO_CLIENT_PROPS");
      if (propsPath == null) {
        throw new IllegalArgumentException("accumulo-client.properties must be set!");
      }
      if (!Paths.get(propsPath).toFile().exists()) {
        throw new IllegalArgumentException(propsPath + " does not exist!");
      }
    }
    return propsPath;
  }

  public Properties getClientProperties() {
    if (cachedProps == null) {
      cachedProps = Accumulo.newClientProperties().from(getClientPropsPath()).build();
    }
    return cachedProps;
  }

  public Configuration getHadoopConfig() {
    Configuration config = new Configuration();
    config.set("mapreduce.job.classloader", "true");
    return config;
  }
}
