package org.apache.accumulo.examples.cli;

import com.beust.jcommander.Parameter;

public class ClientOnDefaultTable extends ClientOpts {
  @Parameter(names = "--table", description = "table to use")
  private String tableName;

  public ClientOnDefaultTable(String table) {
    this.tableName = table;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
}
