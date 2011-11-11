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

package org.ros.internal.node.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.ros.exception.RosRuntimeException;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveIdentifier {

  private final GraphName name;
  private final URI uri;

  public static SlaveIdentifier newFromStrings(String nodeName, String uri) {
    try {
      return new SlaveIdentifier(new GraphName(nodeName), new URI(uri));
    } catch (URISyntaxException e) {
      throw new RosRuntimeException(e);
    }
  }

  /**
   * Constructs a new {@link SlaveIdentifier}.
   * 
   * Note that either {@code name} or {@code uri} may be null but not both. This
   * is necessary because either is enough to uniquely identify a
   * {@link SlaveServer} and because, depending on context, one or the other may
   * not be available.
   * 
   * Although either value may be {@code null}, we do not treat {@code null} as
   * a wildcard with respect to equality. Even though it should be safe to do so,
   * wildcards are unnecessary in this case and would likely lead to buggy code.
   * 
   * @param name
   *          the {@link GraphName} that the {@link SlaveServer} is known as
   * @param uri
   *          the {@link URI} of the {@link SlaveServer}'s XML-RPC server
   */
  public SlaveIdentifier(GraphName name, URI uri) {
    Preconditions.checkArgument(name != null || uri != null);
    if (name != null) {
      Preconditions.checkArgument(name.isGlobal());
    }
    this.name = name;
    this.uri = uri;
  }

  /**
   * @param uri
   *          the {@link URI} of the {@link SlaveServer}
   * @return an anonymous {@link SlaveIdentifier} with the specified {@link URI}
   */
  static SlaveIdentifier newAnonymous(URI uri) {
    return new SlaveIdentifier(GraphName.newAnonymous(), uri);
  }

  @Override
  public String toString() {
    return "SlaveIdentifier<" + name + ", " + uri + ">";
  }

  public GraphName getName() {
    return name;
  }

  public URI getUri() {
    return uri;
  }

  public Map<String, String> toHeader() {
    return new ImmutableMap.Builder<String, String>().put(ConnectionHeaderFields.CALLER_ID,
        name.toString()).build();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((uri == null) ? 0 : uri.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SlaveIdentifier other = (SlaveIdentifier) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (uri == null) {
      if (other.uri != null)
        return false;
    } else if (!uri.equals(other.uri))
      return false;
    return true;
  }

}
