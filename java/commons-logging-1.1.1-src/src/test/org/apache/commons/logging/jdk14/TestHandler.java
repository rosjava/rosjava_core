/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.apache.commons.logging.jdk14;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


/**
 * <p>Test implementation of <code>java.util.logging.Handler</code>.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 424108 $ $Date: 2006-07-21 01:19:55 +0200 (fr, 21 jul 2006) $
 */

public class TestHandler extends Handler {



    // ----------------------------------------------------- Instance Variables


    // The set of logged records for this handler
    private List records = new ArrayList();


    // --------------------------------------------------------- Public Methods


    public Iterator records() {
        return (records.iterator());
    }


    // -------------------------------------------------------- Handler Methods


    public void close() {
    }


    public void flush() {
        records.clear();
    }


    public void publish(LogRecord record) {
        records.add(record);
    }


}
