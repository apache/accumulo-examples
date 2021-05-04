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
# Apache Accumulo Visibility, Authorizations, and Permissions Example

## Creating a new user

    root@instance> createnamespace examples
    root@instance> createuser username
    Enter new password for 'username': ********
    Please confirm new password for 'username': ********

    root@instance> user username
    Enter password for user username: ********
    username@instance> createtable examples.vistest
    06 10:48:47,931 [shell.Shell] ERROR: org.apache.accumulo.core.client.AccumuloSecurityException: Error PERMISSION_DENIED - User does not have permission to perform this action
    username@instance> userpermissions
    System permissions:

    Namespace permissions (accumulo): Namespace.READ

    Table permissions (accumulo.metadata): Table.READ
    Table permissions (accumulo.replication): Table.READ
    Table permissions (accumulo.root): Table.READ
    username@instance>

A user does not by default have permission to create a table.

## Granting permissions to a user

    username@instance> user root
    Enter password for user root: ********
    root@instance> grant -s System.CREATE_TABLE -u username
    root@instance> user username
    Enter password for user username: ********
    username@instance> createtable examples.vistest
    username@instance examples.vistest> userpermissions
    System permissions: System.CREATE_TABLE

    Namespace permissions (accumulo): Namespace.READ

    Table permissions (accumulo.metadata): Table.READ
    Table permissions (accumulo.replication): Table.READ
    Table permissions (accumulo.root): Table.READ
    Table permissions (examples.vistest): Table.READ, Table.WRITE, Table.BULK_IMPORT, Table.ALTER_TABLE, Table.GRANT, Table.DROP_TABLE, Table.GET_SUMMARIES

## Inserting data with visibilities

Visibilities are boolean AND (&) and OR (|) combinations of authorization
tokens. Authorization tokens are arbitrary strings taken from a restricted
ASCII character set. Parentheses are required to specify order of operations
in visibilities.

    username@instance examples.vistest> insert row f1 q1 v1 -l A
    username@instance examples.vistest> insert row f2 q2 v2 -l A&B
    username@instance examples.vistest> insert row f3 q3 v3 -l apple&carrot|broccoli|spinach
    06 11:19:01,432 [shell.Shell] ERROR: org.apache.accumulo.core.util.BadArgumentException: cannot mix | and & near index 12
    apple&carrot|broccoli|spinach
                ^
    username@instance examples.vistest> insert row f3 q3 v3 -l (apple&carrot)|broccoli|spinach
    username@instance examples.vistest>

## Scanning with authorizations

Authorizations are sets of authorization tokens. Each Accumulo user has
authorizations and each Accumulo scan has authorizations. Scan authorizations
are only allowed to be a subset of the user's authorizations. By default, a
user's authorizations set is empty.

    username@instance examples.vistest> scan
    username@instance examples.vistest> scan -s A
    06 11:43:14,951 [shell.Shell] ERROR: java.lang.RuntimeException: org.apache.accumulo.core.client.AccumuloSecurityException: Error BAD_AUTHORIZATIONS - The user does not have the specified authorizations assigned
    username@instance examples.vistest>

## Setting authorizations for a user

    username@instance examples.vistest> setauths -s A
    06 11:53:42,056 [shell.Shell] ERROR: org.apache.accumulo.core.client.AccumuloSecurityException: Error PERMISSION_DENIED - User does not have permission to perform this action
    username@instance examples.vistest>

A user cannot set authorizations unless the user has the System.ALTER_USER permission.
The root user has this permission.

    username@instance examples.vistest> user root
    Enter password for user root: ********
    root@instance examples.vistest> setauths -s A -u username
    root@instance examples.vistest> user username
    Enter password for user username: ********
    username@instance examples.vistest> scan -s A
    row f1:q1 [A]    v1
    username@instance examples.vistest> scan
    row f1:q1 [A]    v1
    username@instance examples.vistest>

The default authorizations for a scan are the user's entire set of authorizations.

    username@instance examples.vistest> user root
    Enter password for user root: ********
    root@instance examples.vistest> setauths -s A,B,broccoli -u username
    root@instance examples.vistest> user username
    Enter password for user username: ********
    username@instance examples.vistest> getauths
    A,B,broccoli
    username@instance examples.vistest> getauths -u username
    A,B,broccoli

    username@instance examples.vistest> scan
    row f1:q1 [A]    v1
    row f2:q2 [A&B]    v2
    row f3:q3 [(apple&carrot)|broccoli|spinach]    v3
    username@instance examples.vistest> scan -s B
    username@instance examples.vistest>

If you want, you can limit a user to only be able to insert data which they can read themselves.
First, check for any existing constraints.

    username@instance examples.vistest> constraint -l -t examples.vistest
    org.apache.accumulo.core.constraints.DefaultKeySizeConstraint=1

If existing constraints exists, take note of the values assigned to the constraints and use a
unique value when creating the new constraint.

In this example, since a constraint exists with a value of '1', we will choose the next
available value. In this case '2'.

The constraint can be set with the following command:

    username@instance examples.vistest> config -t examples.vistest -s table.constraint.2=org.apache.accumulo.core.data.constraints.VisibilityConstraint
    username@instance examples.vistest> constraint -l
    org.apache.accumulo.core.data.constraints.DefaultKeySizeConstraint=1
    org.apache.accumulo.core.data.constraints.VisibilityConstraint=2
    username@instance examples.vistest> insert row f4 q4 v4 -l spinach
         Constraint Failures:
            ConstraintViolationSummary(constrainClass:org.apache.accumulo.core.constraints.VisibilityConstraint, violationCode:2, violationDescription:User does not have authorization on column visibility, numberOfViolatingMutations:1)
    username@instance examples.vistest> insert row f4 q4 v4 -l spinach|broccoli
    username@instance examples.vistest> scan
    row f1:q1 [A]    v1
    row f2:q2 [A&B]    v2
    row f3:q3 [(apple&carrot)|broccoli|spinach]    v3
    row f4:q4 [spinach|broccoli]    v4
    username@instance examples.vistest>

