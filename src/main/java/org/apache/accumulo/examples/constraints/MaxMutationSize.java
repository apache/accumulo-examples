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
package org.apache.accumulo.examples.constraints;

import java.util.Collections;
import java.util.List;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.constraints.Constraint;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

/**
 * Ensure that mutations are a reasonable size: we must be able to fit several in memory at a time.
 */
public class MaxMutationSize implements Constraint {
  static final long MAX_SIZE = Runtime.getRuntime().maxMemory() >> 8;
  static final List<Short> empty = Collections.emptyList();
  static final List<Short> violations = Collections.singletonList(Short.valueOf((short) 0));

  @Override
  public String getViolationDescription(short violationCode) {
    return String.format("mutation exceeded maximum size of %d", MAX_SIZE);
  }

  @Override
  public List<Short> check(Environment env, Mutation mutation) {
    if (mutation.estimatedMemoryUsed() < MAX_SIZE)
      return empty;
    return violations;
  }

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();
    try {
      connector.tableOperations().create("testConstraints");
    } catch (TableExistsException e) {
      // ignore
    }

    /**
     * Add the {@link MaxMutationSize} constraint to the table. Be sure to use the fully qualified class name
     */
    int num = connector.tableOperations().addConstraint("testConstraints", "org.apache.accumulo.examples.constraints.MaxMutationSize");

    System.out.println("Attempting to write a lot of mutations to testConstraints");
    try (BatchWriter bw = connector.createBatchWriter("testConstraints")) {
      Mutation m = new Mutation("r1");
      for (int i = 0; i < 1_000_000; i++)
        m.put("cf" + i % 5000, "cq" + i, new Value(("value" + i).getBytes()));
      bw.addMutation(m);
    } catch (MutationsRejectedException e) {
      e.getConstraintViolationSummaries().forEach(m -> System.out.println("Constraint violated: " + m.constrainClass));
    }

    connector.tableOperations().removeConstraint("testConstraints", num);
  }

}
