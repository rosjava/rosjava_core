/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/auth/AuthState.java,v 1.3 2004/11/02 19:39:16 olegk Exp $
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

/**
 * This class provides detailed information about the state of the
 * authentication process.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @since 3.0
 */
public class AuthState {

    public static final String PREEMPTIVE_AUTH_SCHEME = "basic";
    
    /** Actual authentication scheme */
    private AuthScheme authScheme = null;

    /** Whether an authetication challenged has been received */
    private boolean authRequested = false;

    /** Whether the authetication challenge has been responsed to */
    private boolean authAttempted = false;

    /** Whether preemtive authentication is attempted */
    private boolean preemptive  = false; 
      
    /**
     * Default constructor.
     * 
     */
    public AuthState() {
        super();
    }

    /**
     * Invalidates the authentication state by resetting its parameters.
     */
    public void invalidate() {
        this.authScheme = null;
        this.authRequested = false;
        this.authAttempted = false;
        this.preemptive = false;
    }

    /** 
     * Tests whether authenication challenge has been received
     *  
     * @return <tt>true</tt> if authenication challenge has been received, 
     *  <tt>false</tt> otherwise
     */
    public boolean isAuthRequested() {
        return this.authRequested;
    }
        
    /** 
     * Sets authentication request status
     *  
     * @param challengeReceived <tt>true</tt> if authenication has been requested, 
     *  <tt>false</tt> otherwise
     */
    public void setAuthRequested(boolean challengeReceived) {
        this.authRequested = challengeReceived;
    }
    
    /** 
     * Tests whether authenication challenge has been responsed to
     *  
     * @return <tt>true</tt> if authenication challenge has been responsed to, 
     *  <tt>false</tt> otherwise
     */
    public boolean isAuthAttempted() {
        return this.authAttempted;
    }
        
    /** 
     * Sets authentication attempt status
     *  
     * @param challengeResponded <tt>true</tt> if authenication has been attempted, 
     *  <tt>false</tt> otherwise
     */
    public void setAuthAttempted(boolean challengeResponded) {
        this.authAttempted = challengeResponded;
    }
    
    /**
     * Preemptively assigns Basic authentication scheme.
     */
    public void setPreemptive() {
        if (!this.preemptive) {
            if (this.authScheme != null) {
                throw new IllegalStateException("Authentication state already initialized");
            }
            this.authScheme = AuthPolicy.getAuthScheme(PREEMPTIVE_AUTH_SCHEME);
            this.preemptive = true;
        }
    }

    /**
     * Tests if preemptive authentication is used.
     * 
     * @return <tt>true</tt> if using the default Basic {@link AuthScheme 
     * authentication scheme}, <tt>false</tt> otherwise.
     */
    public boolean isPreemptive() {
        return this.preemptive;
    }
    
    /**
     * Assigns the given {@link AuthScheme authentication scheme}.
     * 
     * @param authScheme the {@link AuthScheme authentication scheme}
     */
    public void setAuthScheme(final AuthScheme authScheme) {
        if (authScheme == null) {
            invalidate();
            return;
        }
        if (this.preemptive && !(this.authScheme.getClass().isInstance(authScheme))) {
            this.preemptive = false;
            this.authAttempted = false;
        }
        this.authScheme = authScheme;
    }

    /**
     * Returns the {@link AuthScheme authentication scheme}.
     * 
     * @return {@link AuthScheme authentication scheme}
     */
    public AuthScheme getAuthScheme() {
        return authScheme;
    }
    
    /**
     * Returns the authentication realm.
     * 
     * @return the name of the authentication realm
     */
    public String getRealm() {
        if (this.authScheme != null) {
            return this.authScheme.getRealm();
        } else {
            return null;
        }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Auth state: auth requested [");
        buffer.append(this.authRequested);
        buffer.append("]; auth attempted [");
        buffer.append(this.authAttempted);
        if (this.authScheme != null) {
            buffer.append("]; auth scheme [");
            buffer.append(this.authScheme.getSchemeName());
            buffer.append("]; realm [");
            buffer.append(this.authScheme.getRealm());            
        }
        buffer.append("] preemptive [");
        buffer.append(this.preemptive);
        buffer.append("]");
        return buffer.toString();
    }
}
