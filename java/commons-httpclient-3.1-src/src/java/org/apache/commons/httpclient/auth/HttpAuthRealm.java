/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/auth/HttpAuthRealm.java,v 1.9 2004/06/12 22:47:23 olegk Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 *
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
 */

package org.apache.commons.httpclient.auth;

/** The key used to look up authentication credentials.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:adrian@intencha.com">Adrian Sutton</a>
 * 
 * @deprecated no longer used
 */
public class HttpAuthRealm extends AuthScope {
    
    /** Creates a new HttpAuthRealm for the given <tt>domain</tt> and 
     * <tt>realm</tt>.
     * 
     * @param domain the domain the credentials apply to. May be set
     *   to <tt>null</tt> if credenticals are applicable to
     *   any domain. 
     * @param realm the realm the credentials apply to. May be set 
     *   to <tt>null</tt> if credenticals are applicable to
     *   any realm. 
     *   
     */
    public HttpAuthRealm(final String domain, final String realm) {
        super(domain, ANY_PORT, realm, ANY_SCHEME);
    }
    
}
