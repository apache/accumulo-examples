package org.apache.accumulo.examples.constraints;

import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.NamespaceExistsException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.examples.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstraintsCommon {

  private static final Logger log = LoggerFactory.getLogger(ConstraintsCommon.class);

  static final String CONSTRAINTS_TABLE = Constants.NAMESPACE + ".testConstraints";
  static final String CONSTRAINT_VIOLATED_MSG = "Constraint violated: {}";

  static void createConstraintsTable(AccumuloClient client)
      throws AccumuloException, AccumuloSecurityException {
    try {
      client.namespaceOperations().create(Constants.NAMESPACE);
    } catch (NamespaceExistsException e) {
      log.info(Constants.NAMESPACE_EXISTS_MSG + Constants.NAMESPACE);
    }
    try {
      client.tableOperations().create(ConstraintsCommon.CONSTRAINTS_TABLE);
    } catch (TableExistsException e) {
      log.warn(Constants.TABLE_EXISTS_MSG + ConstraintsCommon.CONSTRAINTS_TABLE);
    }
  }
}
