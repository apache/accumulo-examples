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
package org.apache.accumulo.examples.util;

import java.util.Map;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.hadoop.io.Text;

public final class FormatUtil {

  /**
   * Format and return the specified table entry as a human-readable String suitable for logging.
   * <br/>
   * If {@code includeTimestamp} is true, the entry will be formatted as: <br/>
   * {@literal <row> <columnFamily>:<columnQualifier> <columnVisibility> <timestamp>\t<value>} <br/>
   * If false, the entry will be formatted as: <br/>
   * {@literal <row> <columnFamily>:<columnQualifier> <columnVisibility>\t<value>} <br/>
   * Examples: <br/>
   * {@literal a ~chunk:\x00\x00\x00d\x00\x00\x00\x00 [A&B] 9223372036854775807 asdfjkl;}
   * {@literal a ~chunk:\x00\x00\x00d\x00\x00\x00\x00 [A&B] asdfjkl;}
   *
   * @param entry
   *          the table entry to format
   * @param includeTimestamp
   *          if true, include the timestamp in the returned result
   * @return the specified entry as a formatted String, or null if the entry is null
   */
  public static String formatTableEntry(final Map.Entry<Key,Value> entry,
      final boolean includeTimestamp) {
    if (entry == null) {
      return null;
    }

    Key key = entry.getKey();
    StringBuilder sb = new StringBuilder();
    Text buffer = new Text();

    // Append row.
    appendBytes(sb, key.getRow(buffer).getBytes()).append(" ");

    // Append column family.
    appendBytes(sb, key.getColumnFamily().getBytes()).append(":");

    // Append column qualifier.
    appendBytes(sb, key.getColumnQualifier().getBytes()).append(" ");

    // Append visibility and timestamp.
    sb.append(new ColumnVisibility(key.getColumnVisibility(buffer)));

    if (includeTimestamp) {
      sb.append(" ").append(entry.getKey().getTimestamp());
    }

    // Append value.
    Value value = entry.getValue();
    if (value != null && value.getSize() > 0) {
      sb.append("\t");
      appendBytes(sb, value.get());
    }
    return sb.toString();
  }

  private static StringBuilder appendBytes(final StringBuilder sb, final byte[] ba) {
    for (byte b : ba) {
      int c = 0xff & b;
      if (c == '\\') {
        sb.append("\\\\");
      } else if (c >= 32 && c <= 126) {
        sb.append((char) c);
      } else {
        sb.append("\\x").append(String.format("%02X", c));
      }
    }
    return sb;
  }

  private FormatUtil() {
    throw new UnsupportedOperationException();
  }
}
