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
package org.apache.xmlrpc.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.w3c.dom.Document;


/** Default implementation of {@link TypeConverterFactory}.
 */
public class TypeConverterFactoryImpl implements TypeConverterFactory {
    private static class IdentityTypeConverter implements TypeConverter {
        private final Class clazz;
        IdentityTypeConverter(Class pClass) {
            clazz = pClass;
        }
        public boolean isConvertable(Object pObject) {
            return pObject == null  ||  clazz.isAssignableFrom(pObject.getClass());
        }
        public Object convert(Object pObject) {
            return pObject;
        }
        public Object backConvert(Object pObject) {
            return pObject;
        }
    }

    private static abstract class ListTypeConverter implements TypeConverter {
        private final Class clazz;
        ListTypeConverter(Class pClass) {
            clazz = pClass;
        }
        protected abstract List newList(int pSize);

        public boolean isConvertable(Object pObject) {
            return pObject == null  ||  pObject instanceof Object[]  ||  pObject instanceof Collection;
        }

        public Object convert(Object pObject) {
            if (pObject == null) {
                return null;
            }
            if (clazz.isAssignableFrom(pObject.getClass())) {
                return pObject;
            }
            if (pObject instanceof Object[]) {
                Object[] objects = (Object[]) pObject;
                List result = newList(objects.length);
                for (int i = 0;  i < objects.length;  i++) {
                    result.add(objects[i]);
                }
                return result;
            }
            Collection collection = (Collection) pObject;
            List result = newList(collection.size());
            result.addAll(collection);
            return result;
        }

        public Object backConvert(Object pObject) {
            return ((List) pObject).toArray();
        }
    }

    private static class PrimitiveTypeConverter implements TypeConverter {
        private final Class clazz;
        PrimitiveTypeConverter(Class pClass) {
            clazz = pClass;
        }
        public boolean isConvertable(Object pObject) {
            return pObject != null  &&  pObject.getClass().isAssignableFrom(clazz);
        }
        public Object convert(Object pObject) {
            return pObject;
        }
        public Object backConvert(Object pObject) {
            return pObject;
        }
    }

    private static final TypeConverter voidTypeConverter = new IdentityTypeConverter(void.class);
    private static final TypeConverter mapTypeConverter = new IdentityTypeConverter(Map.class);
    private static final TypeConverter objectArrayTypeConverter = new IdentityTypeConverter(Object[].class);
    private static final TypeConverter byteArrayTypeConverter = new IdentityTypeConverter(byte[].class);
    private static final TypeConverter stringTypeConverter = new IdentityTypeConverter(String.class);
    private static final TypeConverter booleanTypeConverter = new IdentityTypeConverter(Boolean.class);
    private static final TypeConverter characterTypeConverter = new IdentityTypeConverter(Character.class);
    private static final TypeConverter byteTypeConverter = new IdentityTypeConverter(Byte.class);
    private static final TypeConverter shortTypeConverter = new IdentityTypeConverter(Short.class);
    private static final TypeConverter integerTypeConverter = new IdentityTypeConverter(Integer.class);
    private static final TypeConverter longTypeConverter = new IdentityTypeConverter(Long.class);
    private static final TypeConverter bigDecimalTypeConverter = new IdentityTypeConverter(BigDecimal.class);
    private static final TypeConverter bigIntegerTypeConverter = new IdentityTypeConverter(BigInteger.class);
    private static final TypeConverter floatTypeConverter = new IdentityTypeConverter(Float.class);
    private static final TypeConverter doubleTypeConverter = new IdentityTypeConverter(Double.class);
    private static final TypeConverter dateTypeConverter = new IdentityTypeConverter(Date.class);
    private static final TypeConverter calendarTypeConverter = new IdentityTypeConverter(Calendar.class);
    private static final TypeConverter domTypeConverter = new IdentityTypeConverter(Document.class);
    private static final TypeConverter primitiveBooleanTypeConverter = new PrimitiveTypeConverter(Boolean.class);
    private static final TypeConverter primitiveCharTypeConverter = new PrimitiveTypeConverter(Character.class);
    private static final TypeConverter primitiveByteTypeConverter = new PrimitiveTypeConverter(Byte.class);
    private static final TypeConverter primitiveShortTypeConverter = new PrimitiveTypeConverter(Short.class);
    private static final TypeConverter primitiveIntTypeConverter = new PrimitiveTypeConverter(Integer.class);
    private static final TypeConverter primitiveLongTypeConverter = new PrimitiveTypeConverter(Long.class);
    private static final TypeConverter primitiveFloatTypeConverter = new PrimitiveTypeConverter(Float.class);
    private static final TypeConverter primitiveDoubleTypeConverter = new PrimitiveTypeConverter(Double.class);

    private static final TypeConverter propertiesTypeConverter = new TypeConverter() {
        public boolean isConvertable(Object pObject) {
            return pObject == null  ||  pObject instanceof Map;
        }

        public Object convert(Object pObject) {
            if (pObject == null) {
                return null;
            }
            Properties props = new Properties();
            props.putAll((Map) pObject);
            return props;
        }

        public Object backConvert(Object pObject) {
            return pObject;
        }
    };

    private static final TypeConverter hashTableTypeConverter = new TypeConverter() {
        public boolean isConvertable(Object pObject) {
            return pObject == null  ||  pObject instanceof Map;
        }

        public Object convert(Object pObject) {
            if (pObject == null) {
                return null;
            }
            return new Hashtable((Map) pObject);
        }

        public Object backConvert(Object pObject) {
            return pObject;
        }
    };

    private static final TypeConverter listTypeConverter = new ListTypeConverter(List.class) {
        protected List newList(int pSize) {
            return new ArrayList(pSize);
        }
    };

    private static final TypeConverter vectorTypeConverter = new ListTypeConverter(Vector.class) {
        protected List newList(int pSize) {
            return new Vector(pSize);
        }
    };

    private static class CastCheckingTypeConverter implements TypeConverter {
        private final Class clazz;
        CastCheckingTypeConverter(Class pClass) {
            clazz = pClass;
        }
        public boolean isConvertable(Object pObject) {
            return pObject == null  ||  clazz.isAssignableFrom(pObject.getClass());
        }
        public Object convert(Object pObject) {
            return pObject;
        }
        public Object backConvert(Object pObject) {
            return pObject;
        }
    }

    /** Returns a type converter for the given class.
     */
    public TypeConverter getTypeConverter(Class pClass) {
        if (void.class.equals(pClass)) {
            return voidTypeConverter;
        }
        if (pClass.isAssignableFrom(boolean.class)) {
            return primitiveBooleanTypeConverter;
        }
        if (pClass.isAssignableFrom(char.class)) {
            return primitiveCharTypeConverter;
        }
        if (pClass.isAssignableFrom(byte.class)) {
            return primitiveByteTypeConverter;
        }
        if (pClass.isAssignableFrom(short.class)) {
            return primitiveShortTypeConverter;
        }
        if (pClass.isAssignableFrom(int.class)) {
            return primitiveIntTypeConverter;
        }
        if (pClass.isAssignableFrom(long.class)) {
            return primitiveLongTypeConverter;
        }
        if (pClass.isAssignableFrom(float.class)) {
            return primitiveFloatTypeConverter;
        }
        if (pClass.isAssignableFrom(double.class)) {
            return primitiveDoubleTypeConverter;
        }
        if (pClass.isAssignableFrom(String.class)) {
            return stringTypeConverter;
        }
        if (pClass.isAssignableFrom(Boolean.class)) {
            return booleanTypeConverter;
        }
        if (pClass.isAssignableFrom(Character.class)) {
            return characterTypeConverter;
        }
        if (pClass.isAssignableFrom(Byte.class)) {
            return byteTypeConverter;
        }
        if (pClass.isAssignableFrom(Short.class)) {
            return shortTypeConverter;
        }
        if (pClass.isAssignableFrom(Integer.class)) {
            return integerTypeConverter;
        }
        if (pClass.isAssignableFrom(Long.class)) {
            return longTypeConverter;
        }
        if (pClass.isAssignableFrom(BigDecimal.class)) {
            return bigDecimalTypeConverter;
        }
        if (pClass.isAssignableFrom(BigInteger.class)) {
            return bigIntegerTypeConverter;
        }
        if (pClass.isAssignableFrom(Float.class)) {
            return floatTypeConverter;
        }
        if (pClass.isAssignableFrom(Double.class)) {
            return doubleTypeConverter;
        }
        if (pClass.isAssignableFrom(Date.class)) {
            return dateTypeConverter;
        }
        if (pClass.isAssignableFrom(Calendar.class)) {
            return calendarTypeConverter;
        }
        if (pClass.isAssignableFrom(Object[].class)) {
            return objectArrayTypeConverter;
        }
        if (pClass.isAssignableFrom(List.class)) {
            return listTypeConverter;
        }
        if (pClass.isAssignableFrom(Vector.class)) {
            return vectorTypeConverter;
        }
        if (pClass.isAssignableFrom(Map.class)) {
            return mapTypeConverter;
        }
        if (pClass.isAssignableFrom(Hashtable.class)) {
            return hashTableTypeConverter;
        }
        if (pClass.isAssignableFrom(Properties.class)) {
            return propertiesTypeConverter;
        }
        if (pClass.isAssignableFrom(byte[].class)) {
            return byteArrayTypeConverter;
        }
        if (pClass.isAssignableFrom(Document.class)) {
            return domTypeConverter;
        }
        if (Serializable.class.isAssignableFrom(pClass)) {
            return new CastCheckingTypeConverter(pClass);
        }
        throw new IllegalStateException("Invalid parameter or result type: " + pClass.getName());
    }
}
