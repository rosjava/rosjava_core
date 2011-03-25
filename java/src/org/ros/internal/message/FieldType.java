/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.message;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
interface FieldType {

  static final short BOOL = 0;
  static final short INT8 = 1;
  static final short UINT8 = 2;
  static final short INT16 = 3;
  static final short UINT16 = 4;
  static final short INT32 = 5;
  static final short UINT32 = 6;
  static final short INT64 = 7;
  static final short UINT64 = 8;
  static final short FLOAT32 = 9;
  static final short FLOAT64 = 10;
  static final short STRING = 11;
  static final short TIME = 12;
  static final short DURATION = 13;
  static final short MESSAGE = 14;

}
