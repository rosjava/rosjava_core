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

package org.ros.internal.node.client;

import org.ros.internal.node.response.BooleanResultFactory;
import org.ros.internal.node.response.IntegerResultFactory;
import org.ros.internal.node.response.ObjectResultFactory;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StringListResultFactory;
import org.ros.internal.node.response.StringResultFactory;
import org.ros.internal.node.response.VoidResultFactory;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.xmlrpc.ParameterServer;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Provide access to the XML-RPC API for a ROS {@link ParameterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
// TODO(kwc): it's a bit odd that both Master and ParameterServer are "Nodes".
// The only API methods that require a ROS Node is the subscribe/unsubscribe,
// which are ancillary.
public class ParameterClient extends NodeClient<org.ros.internal.node.xmlrpc.ParameterServer> {

  private final SlaveIdentifier slaveIdentifier;
  private final String nodeName;

  /**
   * Create a new {@link ParameterClient} connected to the specified
   * {@link ParameterServer} URI.
   * 
   * @param uri
   *          the {@link URI} of the {@link ParameterServer} to connect to
   * @throws MalformedURLException
   */
  public ParameterClient(SlaveIdentifier slaveIdentifier, URI uri) {
    super(uri, org.ros.internal.node.xmlrpc.ParameterServer.class);
    this.slaveIdentifier = slaveIdentifier;
    nodeName = slaveIdentifier.getName().toString();
  }

  public Response<Object> getParam(String parameterName) {
    return Response.fromListCheckedFailure(node.getParam(nodeName, parameterName),
        new ObjectResultFactory());
  }

  public Response<Void> setParam(String parameterName, Boolean parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, Integer parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, Double parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, String parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, Character parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, Byte parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, Short parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, List<?> parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String parameterName, Map<?, ?> parameterValue) {
    return Response.fromListChecked(node.setParam(nodeName, parameterName, parameterValue),
        new VoidResultFactory());
  }

  public Response<String> searchParam(String parameterName) {
    return Response.fromListCheckedFailure(node.searchParam(nodeName, parameterName),
        new StringResultFactory());
  }

  public Response<Object> subscribeParam(String parameterName) {
    return Response.fromListChecked(
        node.subscribeParam(nodeName, slaveIdentifier.getUri().toString(), parameterName),
        new ObjectResultFactory());
  }

  public Response<Integer> unsubscribeParam(String parameterName) {
    return Response.fromListChecked(
        node.unsubscribeParam(nodeName, slaveIdentifier.getUri().toString(), parameterName),
        new IntegerResultFactory());
  }

  public Response<Boolean> hasParam(String parameterName) {
    return Response.fromListChecked(node.hasParam(nodeName, parameterName),
        new BooleanResultFactory());
  }

  public Response<Void> deleteParam(String parameterName) {
    return Response.fromListChecked(node.deleteParam(nodeName, parameterName),
        new VoidResultFactory());
  }

  public Response<List<String>> getParamNames() {
    return Response.fromListChecked(node.getParamNames(nodeName), new StringListResultFactory());
  }

}
