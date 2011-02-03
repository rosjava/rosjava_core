/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/auth/TestChallengeProcessor.java,v 1.1 2004/03/25 20:37:20 olegk Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpParams;

/**
 * Unit tests for {@link testParsingChallenge}.
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */
public class TestChallengeProcessor extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestChallengeProcessor(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestChallengeProcessor.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestChallengeProcessor.class);
    }


    public void testChallengeSelection() throws Exception {
        List authPrefs = new ArrayList(3);
        authPrefs.add(AuthPolicy.NTLM);
        authPrefs.add(AuthPolicy.DIGEST);
        authPrefs.add(AuthPolicy.BASIC);
        HttpParams httpparams = new DefaultHttpParams(); 
        httpparams.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
        
        AuthChallengeProcessor processor = new AuthChallengeProcessor(httpparams);

        Map map = new HashMap(); 
        map.put("unknown", "unknown realm=\"whatever\"");
        map.put("basic", "basic realm=\"whatever\"");
        
        AuthScheme authscheme = processor.selectAuthScheme(map);
        assertTrue(authscheme instanceof BasicScheme);
    }


    public void testInvalidChallenge() throws Exception {
        List authPrefs = new ArrayList(3);
        authPrefs.add("unsupported1");
        authPrefs.add("unsupported2");
        HttpParams httpparams = new DefaultHttpParams(); 
        httpparams.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
        
        AuthChallengeProcessor processor = new AuthChallengeProcessor(httpparams);

        Map map = new HashMap(); 
        map.put("unsupported1", "unsupported1 realm=\"whatever\"");
        map.put("unsupported2", "unsupported2 realm=\"whatever\"");
        try {
            AuthScheme authscheme = processor.selectAuthScheme(map);
            fail("AuthChallengeException should have been thrown");
        } catch (AuthChallengeException e) {
            //ignore
        }
    }


    public void testUnsupportedChallenge() throws Exception {
        List authPrefs = new ArrayList(3);
        authPrefs.add(AuthPolicy.NTLM);
        authPrefs.add(AuthPolicy.BASIC);
        authPrefs.add(AuthPolicy.DIGEST);
        HttpParams httpparams = new DefaultHttpParams(); 
        httpparams.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
        
        AuthChallengeProcessor processor = new AuthChallengeProcessor(httpparams);

        Map map = new HashMap(); 
        map.put("unsupported1", "unsupported1 realm=\"whatever\"");
        map.put("unsupported2", "unsupported2 realm=\"whatever\"");
        
        try {
            AuthScheme authscheme = processor.selectAuthScheme(map);
            fail("AuthChallengeException should have been thrown");
        } catch (AuthChallengeException e) {
            //expected
        }
    }

    public void testChallengeProcessing() throws Exception {
        HttpParams httpparams = new DefaultHttpParams(); 
        AuthChallengeProcessor processor = new AuthChallengeProcessor(httpparams);

        Map map = new HashMap(); 
        map.put("basic", "basic realm=\"whatever\", param=\"value\"");
        
        AuthState authstate = new AuthState();
        
        AuthScheme authscheme = processor.processChallenge(authstate, map);
        assertTrue(authscheme instanceof BasicScheme);
        assertEquals("whatever", authscheme.getRealm());
        assertEquals(authscheme, authstate.getAuthScheme());
        assertEquals("value", authscheme.getParameter("param"));
    }

    public void testInvalidChallengeProcessing() throws Exception {
        HttpParams httpparams = new DefaultHttpParams(); 
        AuthChallengeProcessor processor = new AuthChallengeProcessor(httpparams);

        Map map = new HashMap(); 
        map.put("basic", "basic realm=\"whatever\", param=\"value\"");
        
        AuthState authstate = new AuthState();
        
        AuthScheme authscheme = processor.processChallenge(authstate, map);
        assertTrue(authscheme instanceof BasicScheme);
        assertEquals("whatever", authscheme.getRealm());
        assertEquals(authscheme, authstate.getAuthScheme());
        assertEquals("value", authscheme.getParameter("param"));

        Map map2 = new HashMap(); 
        map2.put("ntlm", "NTLM");
        try {
            // Basic authentication scheme expected
            authscheme = processor.processChallenge(authstate, map2);
            fail("AuthenticationException should have been thrown");
        } catch (AuthenticationException e) {
            //expected
        }
    }
}
