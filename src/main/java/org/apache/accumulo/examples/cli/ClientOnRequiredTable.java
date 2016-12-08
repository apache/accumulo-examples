package org.apache.accumulo.examples.cli;

import com.beust.jcommander.Parameter;

public class ClientOnRequiredTable extends ClientOpts {
  @Parameter(names = {"-t", "--table"}, required = true, description = "table to use")
  private String tableName;

  public String getTableName() {
    return tableName;
  }
}
