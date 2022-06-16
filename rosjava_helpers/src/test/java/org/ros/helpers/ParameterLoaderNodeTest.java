/*
 * Copyright (C) 2018 Ekumen, Inc.
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
package org.ros.helpers;


import org.junit.Before;
import org.junit.Test;
import org.ros.RosTest;
import org.ros.exception.ParameterNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.parameter.ParameterTree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author jubeira@ekumenlabs.com (Juan I. Ubeira)
 */
public class ParameterLoaderNodeTest extends RosTest {

    private ParameterTree parameters;
    private RosLog log;

    @Before
    public void setup() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        nodeMainExecutor.execute(new AbstractNodeMain() {
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of("node_name");
            }

            @Override
            public void onStart(ConnectedNode connectedNode) {
                parameters = connectedNode.getParameterTree();
                log = connectedNode.getLog();
                latch.countDown();
            }
        }, nodeConfiguration);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void testParameterLoad() throws InterruptedException {
        final String namespace = "foo";
        List<ParameterLoaderNode.Resource> resourceList = new ArrayList<ParameterLoaderNode.Resource>() {{
           add(new ParameterLoaderNode.Resource(getClass().getResourceAsStream("/parameters.yaml"), ""));
           add(new ParameterLoaderNode.Resource(getClass().getResourceAsStream("/parameters.yaml"), namespace));
        }};
        ParameterLoaderNode parameterLoaderNode = new ParameterLoaderNode(resourceList);

        final CountDownLatch parameterNodeLatch = new CountDownLatch(1);
        nodeMainExecutor.execute(parameterLoaderNode, nodeConfiguration, new ArrayList<NodeListener>() {{
            add(new DefaultNodeListener() {
                @Override
                public void onShutdown(Node node) {
                    parameterNodeLatch.countDown();
                }
            });
        }});

        assertTrue(parameterNodeLatch.await(1, TimeUnit.SECONDS));

        try {
            // Without namespace.
            assertEquals("bar", parameters.getString("/string_param"));
            assertEquals(1823, parameters.getInteger("/int_param"));
            assertEquals(1.74, parameters.getDouble("/double_param"), 0.001);
            assertEquals(false, parameters.getBoolean("/boolean_param"));
            List<?> list = parameters.getList("/list_param");
            assertEquals("Hello", list.get(0));
            assertEquals(1, list.get(1));
            assertEquals(2.3, list.get(2));
            assertEquals(true, list.get(3));

            // With namespace.
            assertEquals("bar", parameters.getString(namespace + "/string_param"));
            assertEquals(1823, parameters.getInteger(namespace + "/int_param"));
            assertEquals(1.74, parameters.getDouble(namespace + "/double_param"), 0.001);
            assertEquals(false, parameters.getBoolean(namespace + "/boolean_param"));
            list = parameters.getList(namespace + "/list_param");
            assertEquals("Hello", list.get(0));
            assertEquals(1, list.get(1));
            assertEquals(2.3, list.get(2));
            assertEquals(true, list.get(3));
        } catch (ParameterNotFoundException e) {
            log.error("Error: " + e.getMessage());
            fail();
        }
    }

    @Test
    public void testEmptyYaml() throws InterruptedException {
        List<ParameterLoaderNode.Resource> resourceList = new ArrayList<ParameterLoaderNode.Resource>() {{
            add(new ParameterLoaderNode.Resource(getClass().getResourceAsStream("/empty.yaml"), ""));
        }};
        ParameterLoaderNode parameterLoaderNode = new ParameterLoaderNode(resourceList);

        final CountDownLatch parameterNodeLatch = new CountDownLatch(1);
        nodeMainExecutor.execute(parameterLoaderNode, nodeConfiguration, new ArrayList<NodeListener>() {{
            add(new DefaultNodeListener() {
                @Override
                public void onShutdown(Node node) {
                    parameterNodeLatch.countDown();
                }
            });
        }});

        // No exceptions shall be thrown on node execution, and it should shut down properly.
        assertTrue(parameterNodeLatch.await(1, TimeUnit.SECONDS));
    }
}
