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
enum PrimitiveFieldType implements FieldType {

  BOOL {
    @Override
    public int getSize() {
      return 1;
    }
  },
  BYTE {
    @Override
    public int getSize() {
      return 1;
    }
  },
  CHAR {
    @Override
    public int getSize() {
      return 1;
    }
  },
  INT8 {
    @Override
    public int getSize() {
      return 1;
    }
  },
  UINT8 {
    @Override
    public int getSize() {
      return 1;
    }
  },
  INT16 {
    @Override
    public int getSize() {
      return 2;
    }
  },
  UINT16 {
    @Override
    public int getSize() {
      return 2;
    }
  },
  INT32 {
    @Override
    public int getSize() {
      return 4;
    }
  },
  UINT32 {
    @Override
    public int getSize() {
      return 4;
    }
  },
  INT64 {
    @Override
    public int getSize() {
      return 8;
    }
  },
  UINT64 {
    @Override
    public int getSize() {
      return 8;
    }
  },
  FLOAT32 {
    @Override
    public int getSize() {
      return 4;
    }
  },
  FLOAT64 {
    @Override
    public int getSize() {
      return 8;
    }
  },
  STRING {
    @Override
    public int getSize() {
      throw new RuntimeException();
    }
  },
  TIME {
    @Override
    public int getSize() {
      return 8;
    }
  },
  DURATION {
    @Override
    public int getSize() {
      return 8;
    }
  };

  @Override
  public String getName() {
    return toString().toLowerCase();
  }

  /**
   * @return the serialized size of this {@link PrimitiveFieldType}
   */
  public abstract int getSize();

}