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
enum FieldType {
  BOOL, BOOL_ARRAY,
  INT8, INT8_ARRAY,
  UINT8, UINT8_ARRAY,
  INT16, INT16_ARRAY,
  UINT16, UINT16_ARRAY,
  INT32, INT32_ARRAY,
  UINT32, UINT32_ARRAY,
  INT64, INT64_ARRAY,
  UINT64, UINT64_ARRAY,
  FLOAT32, FLOAT32_ARRAY,
  FLOAT64, FLOAT64_ARRAY,
  STRING, STRING_ARRAY,
  TIME, TIME_ARRAY,
  DURATION, DURATION_ARRAY,
  MESSAGE, MESSAGE_ARRAY;
}