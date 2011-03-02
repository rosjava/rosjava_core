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
package org.ros;

import org.ros.exceptions.RosNameException;
import org.ros.namespace.RosResolver;

/**
 * A context that allows nodeless api, and allows for flexible
 * name resolution, and FIXME fillin...
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class RosContext {
  private RosResolver resolver;

  /**
   * default context
   */
  public RosContext() {
    resolver = new RosResolver();
  }

  /** RosContext constructor will init with remappings.
   * @param args Arguments will be parsed for remappings and ros specific content.
   * @return The stripped arguments, for further argument parsing.
   * @throws RosNameException 
   */
  public String[] init(String[] args) throws RosNameException {
    resolver = new RosResolver();
    return resolver.initRemapping(args);
  }

  /**
   * @return The RosResolver fo this context, this should be used to resolve names that have been remapped.
   * @see RosResolver
   */
  public RosResolver getResolver() {
    return resolver;
  }

}
