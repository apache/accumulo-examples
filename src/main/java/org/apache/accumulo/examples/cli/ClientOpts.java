package org.apache.accumulo.examples.cli;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.ClientConfiguration;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.AuthenticationToken;
import org.apache.accumulo.core.client.security.tokens.KerberosToken;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

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

  public static class PropertiesConverter implements IStringConverter<Configuration> {
    @Override
    public Configuration convert(String filename) {
      try {
        return new PropertiesConfiguration(filename);
      } catch (ConfigurationException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Parameter(names = {"-c", "--conf"}, required = true, converter = PropertiesConverter.class,
      description = "Config file for connecting to Accumulo.  See README.md for details.")
  private Configuration config = null;

  @Parameter(names = {"-auths", "--auths"}, converter = AuthConverter.class, description = "the authorizations to use when reading or writing")
  public Authorizations auths = Authorizations.EMPTY;

  public Connector getConnector() {
    try {
      ZooKeeperInstance zki = new ZooKeeperInstance(config);
      return zki.getConnector(getPrincipal(), getToken());
    } catch (AccumuloException | AccumuloSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  public ClientConfiguration getClientConfiguration() {
    return new ClientConfiguration(config);
  }

  public String getPrincipal() {
    return config.getString("accumulo.examples.principal", "root");
  }

  public AuthenticationToken getToken() {
    if (config.containsKey("instance.rpc.sasl.enabled")) {
      try {
        if (config.containsKey("accumulo.examples.keytab")) {
          String keytab = config.getString("accumulo.examples.keytab");
          return new KerberosToken(getPrincipal(), new File(keytab));
        } else {
          return new KerberosToken(getPrincipal());
        }
      } catch (IOException ioe) {
        throw new UncheckedIOException(ioe);
      }
    } else {
      return new PasswordToken(config.getString("accumulo.examples.password", "secret"));
    }
  }
}
