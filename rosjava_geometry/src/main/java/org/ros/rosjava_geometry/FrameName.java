/*
 * Copyright (C) 2013 Google Inc.
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

package org.ros.rosjava_geometry;

import java.lang.String;

/**
 * Provides a simple wrapper around strings to represent
 * frame names with backwards compatibility (pre ros hydro)
 * catered for by ignoring graph name style leading slashes.
 *
 * @author d.stonier@gmail.com (Daniel Stonier)
 */
public class FrameName {
    private static final String LEGACY_SEPARATOR = "/";
    private String name;

    public static FrameName of(String name) {
        return new FrameName(name);
    }

    private FrameName(String name) {
        this.name = stripLeadingSlash(name);
    }

    /**
     * TF2 names (from hydro on) do not make use of leading slashes.
     */
    private static String stripLeadingSlash(String name) {
        return name.replaceFirst("^/", "");
    }

    public String toString() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FrameName other = (FrameName) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
