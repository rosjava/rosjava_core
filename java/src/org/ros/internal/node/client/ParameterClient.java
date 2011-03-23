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

import org.ros.internal.node.RemoteException;
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

/**
 * Provide access to the XML-RPC API for a ROS {@link ParameterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
// TODO(kwc): it's a bit odd that both Master and ParameterServer are "Nodes".
// The only API methods that require a ROS Node is the subscribe/unsubscribe,
// which are ancillary.
public class ParameterClient extends NodeClient<org.ros.internal.node.xmlrpc.ParameterServer> {

  /**
   * Create a new {@link ParameterClient} connected to the specified
   * {@link ParameterServer} URI.
   * 
   * @param uri
   *          the {@link URI} of the {@link ParameterServer} to connect to
   * @throws MalformedURLException
   */
  public ParameterClient(URI uri) throws MalformedURLException {
    super(uri, org.ros.internal.node.xmlrpc.ParameterServer.class);
  }

  public Response<Object> getParam(String callerId, String parameterName) throws RemoteException {
    return Response.fromListCheckedFailure(node.getParam(callerId, parameterName),
        new ObjectResultFactory());
  }

  public Response<Boolean> hasParam(String callerId, String parameterName) throws RemoteException {
    return Response.fromListChecked(node.hasParam(callerId, parameterName),
        new BooleanResultFactory());
  }

  public Response<Void> deleteParam(String callerId, String parameterName) throws RemoteException {
    return Response.fromListChecked(node.deleteParam(callerId, parameterName),
        new VoidResultFactory());
  }

  public Response<Void> setParam(String callerId, String parameterName, Object parameterValue)
      throws RemoteException {
    // Convert Longs to ints due to Apache XMLRPC restriction. An alternative
    // would be to change setParam to have type-specific overloads, though we
    // would still have issues with arrays and maps.
    if (parameterValue instanceof Long) {
      return Response.fromListChecked(
          node.setParam(callerId, parameterName, ((Long) parameterValue).intValue()),
          new VoidResultFactory());
    } else {
      return Response.fromListChecked(node.setParam(callerId, parameterName, parameterValue),
          new VoidResultFactory());
    }
  }

  public Response<String> searchParam(String callerId, String parameterName) throws RemoteException {
    return Response.fromListCheckedFailure(node.searchParam(callerId, parameterName),
        new StringResultFactory());
  }

  public Response<Object> subscribeParam(SlaveIdentifier slave, String parameterName)
      throws RemoteException {
    return Response.fromListChecked(
        node.subscribeParam(slave.getName().toString(), slave.getUri().toString(), parameterName),
        new ObjectResultFactory());
  }

  public Response<Integer> unsubscribeParam(SlaveIdentifier slave, String parameterName)
      throws RemoteException {
    return Response.fromListChecked(
node.unsubscribeParam(slave.getName().toString(), slave.getUri()
            .toString(), parameterName),
        new IntegerResultFactory());
  }

  public Response<List<String>> getParamNames(String callerId) throws RemoteException {
    return Response.fromListChecked(node.getParamNames(callerId), new StringListResultFactory());
  }

}
