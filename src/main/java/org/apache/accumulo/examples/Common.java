package org.apache.accumulo.examples;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.NamespaceExistsException;
import org.apache.accumulo.core.client.TableExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Common {

  private static final Logger log = LoggerFactory.getLogger(Common.class);

  public static final String NAMESPACE = "examples";

  public static final String TABLE_EXISTS_MSG = "Table already exists. User may wish to delete "
      + "existing table and re-run example. Table name: ";
  public static final String NAMESPACE_EXISTS_MSG = "Namespace already exists. User can ignore "
      + "this message and continue. Namespace: ";

  /**
   * Create a table within the supplied namespace.
   *
   * The incoming table name is expected to have the form "namespace.tablename". If the namespace
   * portion of the name is blank then the table is created outside of a namespace.
   *
   * @param client
   *          AccumuloClient instance
   * @param table
   *          The name of the table to be created
   * @throws AccumuloException
   * @throws AccumuloSecurityException
   */
  public static void createTableWithNamespace(final AccumuloClient client, final String table)
      throws AccumuloException, AccumuloSecurityException {
    String[] name = table.split("\\.");
    if (name.length == 2 && !name[0].isEmpty()) {
      try {
        client.namespaceOperations().create(name[0]);
      } catch (NamespaceExistsException e) {
        log.info(NAMESPACE_EXISTS_MSG + name[0]);
      }
    }
    try {
      client.tableOperations().create(table);
    } catch (TableExistsException e) {
      log.warn(TABLE_EXISTS_MSG + table);
    }
  }
}
