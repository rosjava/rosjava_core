/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/contrib/org/apache/commons/httpclient/contrib/proxy/PluginProxyUtil.java $
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
package org.apache.commons.httpclient.contrib.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A utility class that gives applets the ability to detect proxy host settings.
 * This was adapted from a post from Chris Forster on 20030227 to a Sun Java 
 * forum here:
 * http://forum.java.sun.com/thread.jspa?threadID=364342&tstart=120
 * 
 * The algorithm - which relies on Sun java plugin internal classes in some 
 * cases - was maintained, but the following changes were made:
 * 
 * 1. Logging was used to allow control of debug type messages.
 * 2. Reflection is used instead of direct references to Sun internal classes
 *    to avoid the need to have these classes in the CLASSPATH to compile.
 * 3. Removed the use of global variables to allow this class to be used in 
 *    a multi-threaded environment.
 * 4. Add the use of exception to indicate to the caller when proxy detection
 *    failed as opposed to when no proxy is configured.
 *    
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * for use without additional customization.
 * </p>
 */
public class PluginProxyUtil {
    
    /** Log object for this class */
    private static final Log LOG = LogFactory.getLog(PluginProxyUtil.class);  
    
    /** 
     * This is used internally to indicate that no proxy detection succeeded 
     * and no proxy setting is to be used - failover is unnecessary
     */
    private static final ProxyHost NO_PROXY_HOST = new ProxyHost("",80);
    
    /**
     * The system property that is used to convey proxy information in some VM's 
     */
    private static final String PLUGIN_PROXY_CONFIG_PROP = 
                                                "javaplugin.proxy.config.list";
    
    /**
     * Returns the Proxy Host information using settings from the java plugin.
     * 
     * @param sampleURL the url target for which proxy host information is
     *                  required 
     * @return the proxy host info (name and port) or null if a direct 
     *         connection is allowed to the target url.  
     * @throws ProxyDetectionException if detection failed
     */
    public static ProxyHost detectProxy(URL sampleURL) 
        throws ProxyDetectionException
    {
        
        ProxyHost result = null;
        String javaVers = System.getProperty("java.runtime.version");
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("About to attempt auto proxy detection under Java " +
                      "version:"+javaVers);
        }
        
        // If specific, known detection methods fail may try fallback 
        // detection method
        boolean invokeFailover = false; 
     
        if (javaVers.startsWith("1.3"))  {
            result = detectProxySettingsJDK13(sampleURL);
            if (result == null) {
                invokeFailover = true;
            }
        } else if (javaVers.startsWith("1.4") || (javaVers.startsWith("1.5") || javaVers.startsWith("1.6")))  {
            result = detectProxySettingsJDK14_JDK15_JDK16(sampleURL);
            if (result == null) {
                invokeFailover = true;
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sun Plugin reported java version not 1.3.X, " +
                          "1.4.X, 1.5.X or 1.6.X - trying failover detection...");
            }
            invokeFailover = true;
        }
        if (invokeFailover) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using failover proxy detection...");
            }
            result = getPluginProxyConfigSettings();
        }
        if (NO_PROXY_HOST.equals(result)) {
            result = null;
        }
        return result;
    }

    /**
     * Use Sun-specific internal plugin proxy classes for 1.3.X
     * Look around for the 1.3.X plugin proxy detection class. Without it, 
     * cannot autodetect...
     * 
     * @param sampleURL the URL to check proxy settings for
     * @return ProxyHost the host and port of the proxy that should be used
     * @throws ProxyDetectionException if detection failed
     */
    private static ProxyHost detectProxySettingsJDK13(URL sampleURL) 
        throws ProxyDetectionException
    {
        ProxyHost result = null;
        try {
            // Attempt to discover proxy info by asking internal plugin 
            // code to locate proxy path to server sampleURL...
            Class pluginProxyHandler = 
                Class.forName("sun.plugin.protocol.PluginProxyHandler");
            Method getDefaultProxyHandlerMethod = 
                pluginProxyHandler.getDeclaredMethod("getDefaultProxyHandler",
                                                     null);
            Object proxyHandlerObj = 
                getDefaultProxyHandlerMethod.invoke(null, null);
            if (proxyHandlerObj != null) {
                Class proxyHandlerClass = proxyHandlerObj.getClass();
                Method getProxyInfoMethod = 
                    proxyHandlerClass.getDeclaredMethod("getProxyInfo",
                                                        new Class[]{URL.class});
                Object proxyInfoObject = 
                    getProxyInfoMethod.invoke(proxyHandlerObj, 
                                              new Object[] { sampleURL });
                if (proxyInfoObject != null) {
                    Class proxyInfoClass = proxyInfoObject.getClass();
                    Method getProxyMethod = 
                        proxyInfoClass.getDeclaredMethod("getProxy", null);
                    boolean useProxy = 
                        (getProxyMethod.invoke(proxyInfoObject, null) != null);
                    if (useProxy) {
                        String proxyIP = 
                            (String)getProxyMethod.invoke(proxyInfoObject, null);
                        Method getProxyPortMethod = 
                            proxyInfoClass.getDeclaredMethod("getPort", null);
                        Integer portInteger = 
                            (Integer)getProxyPortMethod.invoke(proxyInfoObject, null);
                        int proxyPort = portInteger.intValue();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("1.3.X: proxy=" + proxyIP+
                                      " port=" + proxyPort);
                        }
                        result = new ProxyHost(proxyIP, proxyPort);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("1.3.X reported NULL for " +
                                      "proxyInfo.getProxy (no proxy assumed)");
                        }
                        result = NO_PROXY_HOST;                                            
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("NULL proxyInfo in 1.3.X auto proxy " +
                                       "detection, (no proxy assumed)");
                    }
                    result = NO_PROXY_HOST;
                }
            } else {
                throw new ProxyDetectionException(
                  "Sun Plugin 1.3.X failed to provide a default proxy handler");
            }
        } catch (Exception e) {
            LOG.warn("Sun Plugin 1.3.X proxy detection class not " +
                     "found, will try failover detection, e:"+e);
        }        
        return result;
    }
    
    /**
     * Returns the proxy information for the specified sampleURL using JRE 1.4
     * specific plugin classes.
     * 
     * Notes:
     *     Plugin 1.4 Final added 
     *     com.sun.java.browser.net.* classes ProxyInfo & ProxyService... 
     *     Use those with JREs => 1.4
     *
     * @param sampleURL the URL to check proxy settings for
     * @return ProxyHost the host and port of the proxy that should be used
     */
    private static ProxyHost detectProxySettingsJDK14_JDK15_JDK16(URL sampleURL) {
        ProxyHost result = null;
        try {
            // Look around for the 1.4.X plugin proxy detection class... 
            // Without it, cannot autodetect...
            Class ProxyServiceClass = 
                Class.forName("com.sun.java.browser.net.ProxyService");
            Method getProxyInfoMethod = 
                ProxyServiceClass.getDeclaredMethod("getProxyInfo", 
                                                    new Class[] {URL.class});
            Object proxyInfoArrayObj = 
                getProxyInfoMethod.invoke(null, new Object[] {sampleURL});
            
            if (proxyInfoArrayObj == null  
                    || Array.getLength(proxyInfoArrayObj) == 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("1.4.X reported NULL proxy (no proxy assumed)");
                }
                result = NO_PROXY_HOST;                    
            } else {
                Object proxyInfoObject = Array.get(proxyInfoArrayObj, 0);
                Class proxyInfoClass = proxyInfoObject.getClass();
                Method getHostMethod = 
                    proxyInfoClass.getDeclaredMethod("getHost",null);
                String proxyIP = 
                    (String)getHostMethod.invoke(proxyInfoObject, null);
                Method getPortMethod = 
                    proxyInfoClass.getDeclaredMethod("getPort",null);
                Integer portInteger = 
                    (Integer)getPortMethod.invoke(proxyInfoObject, null);
                int proxyPort = portInteger.intValue(); 
                if (LOG.isDebugEnabled()) {
                    LOG.debug("1.4.X Proxy info geProxy:"+proxyIP+ 
                              " get Port:"+proxyPort);
                }
                result = new ProxyHost(proxyIP, proxyPort);
            }
        } catch (Exception e) { 
            e.printStackTrace();
            LOG.warn("Sun Plugin 1.4.X proxy detection class not found, " +
                     "will try failover detection, e:"+e);
        }        
        return result;
    }
    
    /**
     * Returns the proxy host information found by inspecting the system 
     * property "javaplugin.proxy.config.list".
     * 
     * @return ProxyHost the host and port of the proxy that should be used
     * @throws ProxyDetectionException if an exception is encountered while
     *                                 parsing the value of 
     *                                 PLUGIN_PROXY_CONFIG_PROP
     */
    private static ProxyHost getPluginProxyConfigSettings() 
        throws ProxyDetectionException
    {
        ProxyHost result = null;
        try {
            Properties properties = System.getProperties();
            String proxyList = 
                properties.getProperty("javaplugin.proxy.config.list");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Plugin Proxy Config List Property:"+proxyList);
            }
            boolean useProxy = (proxyList != null);
            if (useProxy) {
                proxyList = proxyList.toUpperCase();
                //  Using HTTP proxy as proxy for HTTP proxy tunnelled SSL 
                //  socket (should be listed FIRST)....
                //  1/14/03 1.3.1_06 appears to omit HTTP portion of 
                //  reported proxy list... Mod to accomodate this...
                //  Expecting proxyList of "HTTP=XXX.XXX.XXX.XXX:Port" OR 
                //  "XXX.XXX.XXX.XXX:Port" & assuming HTTP...
                String proxyIP="";
                if (proxyList.indexOf("HTTP=") > -1) {
                    proxyIP = 
                        proxyList.substring(proxyList.indexOf("HTTP=")+5, 
                                            proxyList.indexOf(":"));
                } else {
                    proxyIP = proxyList.substring(0, proxyList.indexOf(":"));
                }
                int endOfPort = proxyList.indexOf(",");
                if (endOfPort < 1) endOfPort = proxyList.length();
                String portString = 
                    proxyList.substring(proxyList.indexOf(":")+1,endOfPort);
                int proxyPort = Integer.parseInt(portString);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("proxy " + proxyIP+" port " + proxyPort);
                }
                result = new ProxyHost(proxyIP, proxyPort);
            } else {
                LOG.debug("No configured plugin proxy list");
                result = NO_PROXY_HOST;
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Exception during failover auto proxy detection, " +
                          ", e:"+e);
                throw new ProxyDetectionException(
                        "Encountered unexpected exception while attempting " +
                        "to parse proxy information stored in "+
                        PLUGIN_PROXY_CONFIG_PROP, e);
            }
        }
        return result;
    }    
}
