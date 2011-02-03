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

package org.apache.commons.logging.log4j.log4j12;

import java.util.List;

import org.apache.commons.logging.log4j.StandardTests;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A concrete class that runs the standard tests, and is compiled
 * specifically against log4j12. The parent class can't call any
 * log4j methods at all as that would mean it has to be compiled
 * against a particular version of log4j.
 */

public class Log4j12StandardTests extends StandardTests {

    public void setUpTestAppender(List logEvents) {
        TestAppender appender = new TestAppender(logEvents);
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAllAppenders();
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.INFO);
    }
}
