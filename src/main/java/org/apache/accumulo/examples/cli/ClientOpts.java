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

import java.io.File;
import java.time.Duration;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.ClientInfo;
import org.apache.accumulo.core.client.security.tokens.AuthenticationToken;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;

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

  public static class TimeConverter implements IStringConverter<Long> {
    @Override
    public Long convert(String value) {
      if(value.matches("[0-9]+"))
        value = "PT"+value+"S"; //if only numbers then assume seconds
      return Duration.parse(value).toMillis();
    }
  }

  public static class MemoryConverter implements IStringConverter<Long> {
    @Override
    public Long convert(String str) {
      try {
        char lastChar = str.charAt(str.length() - 1);
        int multiplier = 0;
        switch (Character.toUpperCase(lastChar)) {
          case 'G':
            multiplier += 10;
          case 'M':
            multiplier += 10;
          case 'K':
            multiplier += 10;
          case 'B':
            break;
          default:
            return Long.parseLong(str);
        }
        return Long.parseLong(str.substring(0, str.length() - 1)) << multiplier;
      } catch (Exception ex) {
        throw new IllegalArgumentException("The value '" + str + "' is not a valid memory setting. A valid value would a number "
            + "possibily followed by an optional 'G', 'M', 'K', or 'B'.");
      }
    }
  }

  public static class PropertiesConverter implements IStringConverter<File> {
    @Override
    public File convert(String filename) {
      try {
        return new File(filename);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Parameter(names = {"-c", "--conf"}, required = true, converter = PropertiesConverter.class,
      description = "Accumulo client properties file.  See README.md for details.")
  private File config = null;

  @Parameter(names = {"-auths", "--auths"}, converter = AuthConverter.class, description = "the authorizations to use when reading or writing")
  public Authorizations auths = Authorizations.EMPTY;

  private ClientInfo cachedInfo = null;
  private AccumuloClient cachedAccumuloClient = null;

  public AccumuloClient getAccumuloClient() {
    if (cachedAccumuloClient == null) {
      try {
        cachedAccumuloClient = Accumulo.newClient().usingClientInfo(getClientInfo()).build();
      } catch (AccumuloException|AccumuloSecurityException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return cachedAccumuloClient;
  }

  public ClientInfo getClientInfo() {
    if (cachedInfo == null) {
      cachedInfo = Accumulo.newClient().usingProperties(config.getAbsolutePath()).info();
    }
    return cachedInfo;
  }

  public String getPrincipal() {
    return getClientInfo().getPrincipal();
  }

  public AuthenticationToken getToken() {
    return getClientInfo().getAuthenticationToken();
  }
}
