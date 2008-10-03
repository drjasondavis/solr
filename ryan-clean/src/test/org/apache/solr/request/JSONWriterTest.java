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

package org.apache.solr.request;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.solr.util.AbstractSolrTestCase;

/** Test some aspects of JSON/python writer output (very incomplete)
 *
 */
public class JSONWriterTest extends AbstractSolrTestCase {
    
  @Override
  public String getSchemaFile() { return "schema.xml"; }
  @Override
  public String getSolrConfigFile() { return "solrconfig.xml"; }
    
  public void testNaNInf() throws IOException {
    SolrQueryRequest req = req("dummy");
    SolrQueryResponse rsp = new SolrQueryResponse();
    QueryResponseWriter w = new PythonResponseWriter();

    StringWriter buf = new StringWriter();
    rsp.add("data1", Float.NaN);
    rsp.add("data2", Double.NEGATIVE_INFINITY);
    rsp.add("data3", Float.POSITIVE_INFINITY);
    w.write(buf, req, rsp);
    assertEquals(buf.toString(), "{'data1':float('NaN'),'data2':-float('Inf'),'data3':float('Inf')}");

    w = new RubyResponseWriter();
    buf = new StringWriter();
    w.write(buf, req, rsp);
    assertEquals(buf.toString(), "{'data1'=>(0.0/0.0),'data2'=>-(1.0/0.0),'data3'=>(1.0/0.0)}");

  }
  
}
