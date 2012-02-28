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

import java.util.List;
import java.util.Vector;


/** A {@link TypeConverter} is used when actually calling the
 * handler method or actually returning the result object. It's
 * purpose is to convert a single parameter or the return value
 * from a generic representation (for example an array of objects)
 * to an alternative representation, which is actually used in
 * the methods signature (for example {@link List}, or
 * {@link Vector}.
 */
public interface TypeConverter {
    /** Returns, whether the {@link TypeConverter} is
     * ready to handle the given object. If so,
     * {@link #convert(Object)} may be called.
     */
    boolean isConvertable(Object pObject);
    /** Converts the given object into the required
     * representation.
     */
    Object convert(Object pObject);
    /** Converts the given object into its generic
     * representation.
     */
    Object backConvert(Object result);
}
