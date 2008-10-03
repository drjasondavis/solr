/**
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

package org.apache.solr.search.function;

import org.apache.lucene.index.IndexReader;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.ValueSource;
import org.apache.lucene.search.FieldCache;

import java.io.IOException;

/**
 * Obtains the ordinal of the field value from the default Lucene {@link org.apache.lucene.search.FieldCache} using getStringIndex().
 * <br>
 * The native lucene index order is used to assign an ordinal value for each field value.
 * <br>Field values (terms) are lexicographically ordered by unicode value, and numbered starting at 1.
 * <br>
 * Example:<br>
 *  If there were only three field values: "apple","banana","pear"
 * <br>then ord("apple")=1, ord("banana")=2, ord("pear")=3
 * <p>
 * WARNING: ord() depends on the position in an index and can thus change when other documents are inserted or deleted,
 *  or if a MultiSearcher is used.
 * @version $Id$
 */

public class OrdFieldSource extends ValueSource {
  protected String field;

  public OrdFieldSource(String field) {
    this.field = field;
  }

  @Override
  public String description() {
    return "ord(" + field + ')';
  }

  @Override
  public DocValues getValues(IndexReader reader) throws IOException {
    final int[] arr = FieldCache.DEFAULT.getStringIndex(reader, field).order;
    return new DocValues() {
      @Override
      public float floatVal(int doc) {
        return arr[doc];
      }

      @Override
      public int intVal(int doc) {
        return arr[doc];
      }

      @Override
      public long longVal(int doc) {
        return arr[doc];
      }

      @Override
      public double doubleVal(int doc) {
        return arr[doc];
      }

      @Override
      public String strVal(int doc) {
        // the string value of the ordinal, not the string itself
        return Integer.toString(arr[doc]);
      }

      @Override
      public String toString(int doc) {
        return description() + '=' + intVal(doc);
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (o.getClass() !=  OrdFieldSource.class) return false;
    OrdFieldSource other = (OrdFieldSource)o;
    return this.field.equals(field);
  }

  private static final int hcode = OrdFieldSource.class.hashCode();
  @Override
  public int hashCode() {
    return hcode + field.hashCode();
  };

}
