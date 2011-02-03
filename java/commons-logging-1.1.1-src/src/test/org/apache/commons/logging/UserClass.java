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
 
package org.apache.commons.logging;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;

public class UserClass {
    

    /**
     * Set the ALLOW_FLAWED_CONTEXT feature on the LogFactoryImpl object
     * associated with this class' classloader.
     * <p>
     * Don't forget to set the context classloader to whatever it will be
     * when an instance of this class is actually created <i>before</i> calling
     * this method!
     */
    public static void setAllowFlawedContext(String state) {
        LogFactory f = LogFactory.getFactory();
        f.setAttribute(LogFactoryImpl.ALLOW_FLAWED_CONTEXT_PROPERTY, state); 
    }

    public UserClass() {
        Log log = LogFactory.getLog(LoadTestCase.class);
      }
    
}
