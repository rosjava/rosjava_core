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
package org.apache.commons.logging.avalon;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.commons.logging.impl.AvalonLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.AbstractLogTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:neeme@apache.org">Neeme Praks</a>
 * @version $Revision: 581090 $ $Date: 2007-10-02 00:01:06 +0200 (ti, 02 okt 2007) $
 */
public class AvalonLoggerTestCase extends AbstractLogTest {

    public static void main(String[] args) {
        String[] testCaseName = { AvalonLoggerTestCase.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(AvalonLoggerTestCase.class);
        return suite;
    }

    public Log getLogObject() {
        Log log = new AvalonLogger(new ConsoleLogger());
        return log;
    }
}
