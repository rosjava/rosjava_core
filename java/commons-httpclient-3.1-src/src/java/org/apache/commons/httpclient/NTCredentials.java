/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/NTCredentials.java,v 1.10 2004/04/18 23:51:35 jsdever Exp $
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

package org.apache.commons.httpclient;

import org.apache.commons.httpclient.util.LangUtils;

/** {@link Credentials} for use with the NTLM authentication scheme which requires additional
 * information.
 *
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * 
 * @version $Revision: 480424 $ $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 * 
 * @since 2.0
 */
public class NTCredentials extends UsernamePasswordCredentials {

    // ----------------------------------------------------- Instance Variables

    /** The Domain to authenticate with.  */
    private String domain;

    /** The host the authentication request is originating from.  */
    private String host;


    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     * 
     * @deprecated Do not use. Null user name, domain & host no longer allowed
     */
    public NTCredentials() {
        super();
    }

    /**
     * Constructor.
     * @param userName The user name.  This should not include the domain to authenticate with.
     * For example: "user" is correct whereas "DOMAIN\\user" is not.
     * @param password The password.
     * @param host The host the authentication request is originating from.  Essentially, the
     * computer name for this machine.
     * @param domain The domain to authenticate within.
     */
    public NTCredentials(String userName, String password, String host,
            String domain) {
        super(userName, password);
        if (domain == null) {
            throw new IllegalArgumentException("Domain may not be null");
        }
        this.domain = domain;
        if (host == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        this.host = host;
    }
    // ------------------------------------------------------- Instance Methods


    /**
     * Sets the domain to authenticate with. The domain may not be null.
     *
     * @param domain the NT domain to authenticate in.
     * 
     * @see #getDomain()
     * 
     * @deprecated Do not use. The NTCredentials objects should be immutable
     */
    public void setDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain may not be null");
        }
        this.domain = domain;
    }

    /**
     * Retrieves the name to authenticate with.
     *
     * @return String the domain these credentials are intended to authenticate with.
     * 
     * @see #setDomain(String)
     * 
     */
    public String getDomain() {
        return domain;
    }

    /** 
     * Sets the host name of the computer originating the request. The host name may
     * not be null.
     *
     * @param host the Host the user is logged into.
     * 
     * @deprecated Do not use. The NTCredentials objects should be immutable
     */
    public void setHost(String host) {
        if (host == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        this.host = host;
    }

    /**
     * Retrieves the host name of the computer originating the request.
     *
     * @return String the host the user is logged into.
     */
    public String getHost() {
        return this.host;
    }
    
    /**
     * Return a string representation of this object.
     * @return A string represenation of this object.
     */
    public String toString() {
        final StringBuffer sbResult = new StringBuffer(super.toString());
        
        sbResult.append("@");
        sbResult.append(this.host);
        sbResult.append(".");
        sbResult.append(this.domain);

        return sbResult.toString();
    }

    /**
     * Computes a hash code based on all the case-sensitive parts of the credentials object.
     *
     * @return  The hash code for the credentials.
     */
    public int hashCode() {
        int hash = super.hashCode();
        hash = LangUtils.hashCode(hash, this.host);
        hash = LangUtils.hashCode(hash, this.domain);
        return hash;
    }

    /**
     * Performs a case-sensitive check to see if the components of the credentials
     * are the same.
     *
     * @param o  The object to match.
     *
     * @return <code>true</code> if all of the credentials match.
     */
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (super.equals(o) ) {
            if (o instanceof NTCredentials) {
                NTCredentials that = (NTCredentials) o;

                return LangUtils.equals(this.domain, that.domain)
                    && LangUtils.equals(this.host, that.host);
            }
        }

        return false;
    }
}
