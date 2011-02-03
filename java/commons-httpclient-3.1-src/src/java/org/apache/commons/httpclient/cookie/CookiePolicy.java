/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/cookie/CookiePolicy.java,v 1.15 2004/09/14 20:11:31 olegk Exp $
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

package org.apache.commons.httpclient.cookie;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cookie management policy class. The cookie policy provides corresponding
 * cookie management interfrace for a given type or version of cookie. 
 * <p>RFC 2109 specification is used per default. Other supported specification
 * can be  chosen when appropriate or set default when desired
 * <p>The following specifications are provided:
 *  <ul>
 *   <li><tt>BROWSER_COMPATIBILITY</tt>: compatible with the common cookie 
 *   management practices (even if they are not 100% standards compliant)
 *   <li><tt>NETSCAPE</tt>: Netscape cookie draft compliant
 *   <li><tt>RFC_2109</tt>: RFC2109 compliant (default)
 *   <li><tt>IGNORE_COOKIES</tt>: do not automcatically process cookies
 *  </ul>
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *
 * @since 2.0
 */
public abstract class CookiePolicy {

    private static Map SPECS = Collections.synchronizedMap(new HashMap());
    
    /**
     * The policy that provides high degree of compatibilty 
     * with common cookie management of popular HTTP agents.
     * 
     * @since 3.0
     */
    public static final String BROWSER_COMPATIBILITY = "compatibility";
    
    /** 
     * The Netscape cookie draft compliant policy. 
     * 
     * @since 3.0
     */
    public static final String NETSCAPE = "netscape";

    /** 
     * The RFC 2109 compliant policy. 
     * 
     * @since 3.0
     */
    public static final String RFC_2109 = "rfc2109";

    /**
     * The RFC 2965 compliant policy.
     *
     * @since 3.0
     */
    public static final String RFC_2965 = "rfc2965";

    /**
     * The policy that ignores cookies. 
     * 
     * @since 3.0
     */
    public static final String IGNORE_COOKIES = "ignoreCookies";
    
    /** 
     * The default cookie policy. 
     * 
     * @since 3.0
     */
    public static final String DEFAULT = "default";
    
    static {
        CookiePolicy.registerCookieSpec(DEFAULT, RFC2109Spec.class);
        CookiePolicy.registerCookieSpec(RFC_2109, RFC2109Spec.class);
        CookiePolicy.registerCookieSpec(RFC_2965, RFC2965Spec.class);
        CookiePolicy.registerCookieSpec(BROWSER_COMPATIBILITY, CookieSpecBase.class);
        CookiePolicy.registerCookieSpec(NETSCAPE, NetscapeDraftSpec.class);
        CookiePolicy.registerCookieSpec(IGNORE_COOKIES, IgnoreCookiesSpec.class);
    }
    
    /**
     * The <tt>COMPATIBILITY</tt> policy provides high compatibilty 
     * with common cookie management of popular HTTP agents.
     * 
     * @deprecated Use {@link #BROWSER_COMPATIBILITY}
     */
    public static final int COMPATIBILITY = 0;

    /** 
     * The <tt>NETSCAPE_DRAFT</tt> Netscape draft compliant policy.
     * 
     * @deprecated Use {@link #NETSCAPE} 
     */
    public static final int NETSCAPE_DRAFT = 1;

    /** 
     * The <tt>RFC2109</tt> RFC 2109 compliant policy.
     * 
     * @deprecated Use {@link #RFC_2109} 
     */
    public static final int RFC2109 = 2;

    /**
     * The <tt>RFC2965</tt> RFC 2965 compliant policy.
     *
     * @deprecated Use {@link #RFC_2965}
     */
    public static final int RFC2965 = 3;

    /**
     * The default cookie policy.
     *  
     * @deprecated Use {@link #DEFAULT} 
     */
    private static int defaultPolicy = RFC2109;

    /** Log object. */
    protected static final Log LOG = LogFactory.getLog(CookiePolicy.class);

    /**
     * Registers a new {@link CookieSpec cookie specification} with the given identifier. 
     * If a specification with the given ID already exists it will be overridden.  
     * This ID is the same one used to retrieve the {@link CookieSpec cookie specification} 
     * from {@link #getCookieSpec(String)}.
     * 
     * @param id the identifier for this specification
     * @param clazz the {@link CookieSpec cookie specification} class to register
     * 
     * @see #getCookieSpec(String)
     * 
     * @since 3.0
     */
    public static void registerCookieSpec(final String id, final Class clazz) {
         if (id == null) {
             throw new IllegalArgumentException("Id may not be null");
         }
        if (clazz == null) {
            throw new IllegalArgumentException("Cookie spec class may not be null");
        }
        SPECS.put(id.toLowerCase(), clazz);
    }

    /**
     * Unregisters the {@link CookieSpec cookie specification} with the given ID.
     * 
     * @param id the ID of the {@link CookieSpec cookie specification} to unregister
     * 
     * @since 3.0
     */
    public static void unregisterCookieSpec(final String id) {
         if (id == null) {
             throw new IllegalArgumentException("Id may not be null");
         }
         SPECS.remove(id.toLowerCase());
    }

    /**
     * Gets the {@link CookieSpec cookie specification} with the given ID.
     * 
     * @param id the {@link CookieSpec cookie specification} ID
     * 
     * @return {@link CookieSpec cookie specification}
     * 
     * @throws IllegalStateException if a policy with the ID cannot be found
     * 
     * @since 3.0
     */
    public static CookieSpec getCookieSpec(final String id) 
        throws IllegalStateException {

        if (id == null) {
            throw new IllegalArgumentException("Id may not be null");
        }
        Class clazz = (Class)SPECS.get(id.toLowerCase());

        if (clazz != null) {
            try {
                return (CookieSpec)clazz.newInstance();
            } catch (Exception e) {
                LOG.error("Error initializing cookie spec: " + id, e);
                throw new IllegalStateException(id + 
                    " cookie spec implemented by " +
                    clazz.getName() + " could not be initialized");
            }
        } else {
            throw new IllegalStateException("Unsupported cookie spec " + id);
        }
    } 

    /**
     * @return default cookie policy
     * 
     * @deprecated Use {@link #getDefaultSpec()}
     * 
     * @see #getDefaultSpec()
     */
    public static int getDefaultPolicy() {
        return defaultPolicy;
    }
    

    /**
     * @param policy new default cookie policy
     * 
     * @deprecated Use {@link CookiePolicy#registerCookieSpec(String, Class)}
     * @see #DEFAULT 
     */
    public static void setDefaultPolicy(int policy) {
        defaultPolicy = policy;
    }
    
    /**
     * @param policy cookie policy to get the CookieSpec for
     * @return cookie specification interface for the given policy
     * 
     * @deprecated Use {@link CookiePolicy#getCookieSpec(String)} 
     */
    public static CookieSpec getSpecByPolicy(int policy) {
        switch(policy) {
            case COMPATIBILITY: 
                return new CookieSpecBase(); 
            case NETSCAPE_DRAFT: 
                return new NetscapeDraftSpec(); 
            case RFC2109:
                return new RFC2109Spec();
            case RFC2965:
                return new RFC2965Spec();
            default:
                return getDefaultSpec(); 
        }
    }


    /**
     * Returns {@link CookieSpec cookie specification} registered as {@link #DEFAULT}. 
     * If no default {@link CookieSpec cookie specification} has been registered, 
     * {@link RFC2109Spec RFC2109 specification} is returned.
     *  
     * @return default {@link CookieSpec cookie specification}
     * 
     * @see #DEFAULT
     */
    public static CookieSpec getDefaultSpec() {
        try {
            return getCookieSpec(DEFAULT);
        } catch (IllegalStateException e) {
            LOG.warn("Default cookie policy is not registered");
            return new RFC2109Spec();
        }
    }
    

    /**
     * Gets the CookieSpec for a particular cookie version.
     * 
     * <p>Supported versions:
     * <ul>
     *  <li><tt>version 0</tt> corresponds to the Netscape draft
     *  <li><tt>version 1</tt> corresponds to the RFC 2109
     *  <li>Any other cookie value coresponds to the default spec
     * <ul>
     *
     * @param ver the cookie version to get the spec for
     * @return cookie specification interface intended for processing 
     *  cookies with the given version
     * 
     * @deprecated Use {@link CookiePolicy#getCookieSpec(String)}
     */
    public static CookieSpec getSpecByVersion(int ver) {
        switch(ver) {
            case 0: 
                return new NetscapeDraftSpec(); 
            case 1:
                return new RFC2109Spec();
            default:
                return getDefaultSpec(); 
        }
    }

    /**
     * @return cookie specification interface that provides high compatibilty 
     * with common cookie management of popular HTTP agents
     * 
     * @deprecated Use {@link CookiePolicy#getCookieSpec(String)}
     */
    public static CookieSpec getCompatibilitySpec() {
        return getSpecByPolicy(COMPATIBILITY);
    }

    /**
     * Obtains the currently registered cookie policy names.
     * 
     * Note that the DEFAULT policy (if present) is likely to be the same
     * as one of the other policies, but does not have to be.
     * 
     * @return array of registered cookie policy names
     * 
     * @since 3.1
     */
    public static String[] getRegisteredCookieSpecs(){
            return (String[]) SPECS.keySet().toArray(new String [SPECS.size()]); 
    }
    
}
