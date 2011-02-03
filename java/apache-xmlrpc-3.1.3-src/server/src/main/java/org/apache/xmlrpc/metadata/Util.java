/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.xmlrpc.metadata;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.w3c.dom.Node;


/** Utility class, which provides services to meta data
 * handlers and handler mappings.
 */
public class Util {
	/** This field should solve the problem, that we do not
	 * want to depend on the presence of JAXB. However, if
	 * it is available, we want to support it.
	 */
	private static final Class jaxbElementClass;
	static {
		Class c;
		try {
			c = Class.forName("javax.xml.bind.Element");
		} catch (ClassNotFoundException e) {
			c = null;
		}
		jaxbElementClass = c;
	}
	
	/** Returns a signature for the given return type or
	 * parameter class.
	 * @param pType The class for which a signature is being
	 * queried.
	 * @return Signature, if known, or null.
	 */
	public static String getSignatureType(Class pType) {
		if (pType == Integer.TYPE || pType == Integer.class)
			return "int";
		if (pType == Double.TYPE || pType == Double.class)
			return "double";
		if (pType == Boolean.TYPE || pType == Boolean.class)
			return "boolean";
		if (pType == String.class)
			return "string";
		if (Object[].class.isAssignableFrom(pType)
			||  List.class.isAssignableFrom(pType))
			return "array";
		if (Map.class.isAssignableFrom(pType))
			return "struct";
		if (Date.class.isAssignableFrom(pType)
			||  Calendar.class.isAssignableFrom(pType))
			return "dateTime.iso8601";
		if (pType == byte[].class)
			return "base64";

		// extension types
		if (pType == void.class)
			return "ex:nil";
		if (pType == Byte.TYPE || pType == Byte.class)
			return "ex:i1";
		if (pType == Short.TYPE || pType == Short.class)
			return "ex:i2";
		if (pType == Long.TYPE || pType == Long.class)
			return "ex:i8";
		if (pType == Float.TYPE || pType == Float.class)
			return "ex:float";
		if (Node.class.isAssignableFrom(pType))
			return "ex:node";
		if (jaxbElementClass != null
			&&  jaxbElementClass.isAssignableFrom(pType)) {
			return "ex:jaxbElement";
		}
		if (Serializable.class.isAssignableFrom(pType))
			return "base64";

		// give up
		return null;
	}

	/** Returns a signature for the given methods.
	 * @param pMethods Methods, for which a signature is
	 * being queried.
	 * @return Signature string, or null, if no signature
	 * is available.
	 */
	public static String[][] getSignature(Method[] pMethods) {
        final List result = new ArrayList();
        for (int i = 0;  i < pMethods.length;  i++) {
            String[] sig = getSignature(pMethods[i]);
            if (sig != null) {
                result.add(sig);
            }
        }
        return (String[][]) result.toArray(new String[result.size()][]);
    }

    /** Returns a signature for the given methods.
     * @param pMethod Method, for which a signature is
     * being queried.
     * @return Signature string, or null, if no signature
     * is available.
     */
    public static String[] getSignature(Method pMethod) {    
		Class[] paramClasses = pMethod.getParameterTypes();
		String[] sig = new String[paramClasses.length + 1];
		String s = getSignatureType(pMethod.getReturnType());
		if (s == null) {
			return null;
		}
		sig[0] = s;
		for (int i = 0;  i < paramClasses.length;  i++) {
			s = getSignatureType(paramClasses[i]);
			if (s == null) {
				return null;
			}
			sig[i+1] = s;
		}
		return sig;
	}

    /** Returns a help string for the given method, which
     * is applied to the given class.
     */
    public static String getMethodHelp(Class pClass, Method[] pMethods) {
        final List result = new ArrayList();
        for (int i = 0;  i < pMethods.length;  i++) {
            String help = getMethodHelp(pClass, pMethods[i]);
            if (help != null) {
                result.add(help);
            }
        }
        switch (result.size()) {
            case 0:
                return null;
            case 1:
                return (String) result.get(0);
            default:
                StringBuffer sb = new StringBuffer();
                for (int i = 0;  i < result.size();  i++) {
                    sb.append(i+1);
                    sb.append(": ");
                    sb.append(result.get(i));
                    sb.append("\n");
                }
                return sb.toString();
        }
    }

    /** Returns a help string for the given method, which
	 * is applied to the given class.
	 */
	public static String getMethodHelp(Class pClass, Method pMethod) {
		StringBuffer sb = new StringBuffer();
		sb.append("Invokes the method ");
		sb.append(pClass.getName());
		sb.append(".");
		sb.append(pMethod.getName());
		sb.append("(");
		Class[] paramClasses = pMethod.getParameterTypes();
		for (int i = 0;  i < paramClasses.length;  i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(paramClasses[i].getName());
		}
		sb.append(").");
		return sb.toString();
	}

    /** Returns a signature for the given parameter set. This is used
     * in error messages.
     */
    public static String getSignature(Object[] args) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0;  i < args.length;  i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (args[i] == null) {
                sb.append("null");
            } else {
                sb.append(args[i].getClass().getName());
            }
        }
        return sb.toString();
    }

    /**
     * Creates a new instance of <code>pClass</code>.
     */
    public static Object newInstance(Class pClass) throws XmlRpcException {
        try {
            return pClass.newInstance();
        } catch (InstantiationException e) {
            throw new XmlRpcException("Failed to instantiate class " + pClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new XmlRpcException("Illegal access when instantiating class " + pClass.getName(), e);
        }
    }
}
