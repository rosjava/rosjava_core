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
package org.apache.xmlrpc.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/** A utility class for using reflection.
 */
public class ReflectionUtil {
    /**
     * This method attempts to set a property value on a given object by calling a
     * matching setter.
     * @param pObject The object, on which a property is being set.
     * @param pPropertyName The property name.
     * @param pPropertyValue The property value.
     * @throws IllegalAccessException Setting the property value failed, because invoking
     *   the setter raised an {@link IllegalAccessException}.
     * @throws InvocationTargetException Setting the property value failed, because invoking
     *   the setter raised another exception.
     * @return Whether a matching setter was found. The value false indicates, that no such
     *   setter exists.
     */
    public static boolean setProperty(Object pObject, String pPropertyName, String pPropertyValue)
            throws IllegalAccessException, InvocationTargetException {
        final String methodName = "set" + pPropertyName.substring(0, 1).toUpperCase() + pPropertyName.substring(1);   
        // try to find method signature that matches init param
        Method[] methods = pObject.getClass().getMethods();

        for (int i = 0;  i < methods.length;  i++) {
            final Method method = methods[i];
            if (!method.getName().equals(methodName)) {
                continue; // Ignore methods, which does have the right name 
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;  // Ignore methods, which aren't public
            }
            
            Class[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                continue; // Ignore methods, which don't not have exactly one parameter
            }
            
            Class parameterType = parameterTypes[0];
            final Object param;
            try {
                if (parameterType.equals(boolean.class) || parameterType.equals(Boolean.class)) {
                    param = Boolean.valueOf(pPropertyValue);
                } else if (parameterType.equals(char.class) || parameterType.equals(Character.class)) {
                    if (pPropertyValue.length() != 1) {
                        throw new IllegalArgumentException("Invalid value for parameter "
                                + pPropertyName + "(length != 1):"
                                + pPropertyValue);
                    }
                    param = new Character(pPropertyValue.charAt(0));
                } else if (parameterType.equals(byte.class) || parameterType.equals(Byte.class)) {
                    param = Byte.valueOf(pPropertyValue);
                } else if (parameterType.equals(short.class) || parameterType.equals(Short.class)) {
                    param = Short.valueOf(pPropertyValue);
                } else if (parameterType.equals(int.class) || parameterType.equals(Integer.class)) {
                    param = Integer.valueOf(pPropertyValue);
                } else if (parameterType.equals(long.class) || parameterType.equals(Long.class)) {
                    param = Long.valueOf(pPropertyValue);
                } else if (parameterType.equals(float.class) || parameterType.equals(Float.class)) {
                    param = Float.valueOf(pPropertyValue);
                } else if (parameterType.equals(double.class) || parameterType.equals(Double.class)) {
                    param = Double.valueOf(pPropertyValue);
                } else if (parameterType.equals(String.class)) {
                    param = pPropertyValue;
                } else {
                    throw new IllegalStateException("The property " + pPropertyName
                            + " has an unsupported type of " + parameterType.getName());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value for property "
                        + pPropertyName + ": " + pPropertyValue);
            }
            method.invoke(pObject, new Object[]{param});
            return true;
        }
        return false;
    }
}
