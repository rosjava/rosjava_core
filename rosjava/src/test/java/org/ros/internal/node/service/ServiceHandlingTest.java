package org.ros.internal.node.service;

import org.apache.commons.logging.Log;
import org.ros.exception.RemoteException;
import rosjava_test_msgs.AddTwoInts;
import rosjava_test_msgs.AddTwoIntsRequest;
import rosjava_test_msgs.AddTwoIntsResponse;
import org.ros.RosTest;
import org.ros.message.MessageFactory;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Class for running a multi-threaded service request
 */
public class ServiceHandlingTest extends RosTest {

    /**
     * Test if a multi-threaded node can safely call services from another node
     */
    @Test public void testServiceResponeOrder() throws Exception {
        final NodeConfiguration secondConfig = NodeConfiguration.newPrivate(rosCore.getUri());
        final MessageFactory messageFactory = secondConfig.getMessageFactory();
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        final String SERVICE_NAME = "test_service";
        final int NUM_THREADS = 10;

        // Create the anonymous node to test the server
        AbstractNodeMain serverNode = new AbstractNodeMain() {
            @Override public GraphName getDefaultNodeName() {
                return GraphName.of("server_node");
            }

            @Override public void onStart(final ConnectedNode connectedNode) {

                // Setup Server
                ServiceServer<AddTwoIntsRequest, AddTwoIntsResponse> testServer =
                        connectedNode.newServiceServer(SERVICE_NAME, AddTwoInts._TYPE,
                                new ServiceResponseBuilder<AddTwoIntsRequest, AddTwoIntsResponse>() {

                                    @Override
                                    public void build(AddTwoIntsRequest req, AddTwoIntsResponse res) {
                                        res.setSum(req.getA() + req.getB());
                                    }
                                });
            }
        };

        final List<ClientThread> threads = new LinkedList<>();

        // Create the anonymous node to test the server
        AbstractNodeMain clientNode = new AbstractNodeMain() {

            @Override public GraphName getDefaultNodeName() {
                return GraphName.of("client_node");
            }

            @Override public void onStart(final ConnectedNode connectedNode) {

                // Assert that the service was created

                ServiceClient<AddTwoIntsRequest, AddTwoIntsResponse> serviceClient;
                try {
                    serviceClient = connectedNode.newServiceClient(SERVICE_NAME, AddTwoInts._TYPE);
                    for(int i = 0; i < NUM_THREADS; i++) {
                        ClientThread newThread = new ClientThread( i , i, countDownLatch, messageFactory,
                                SERVICE_NAME, serviceClient, connectedNode.getLog());
                        newThread.start();
                        threads.add(newThread);
                    }
                } catch (org.ros.exception.ServiceNotFoundException e) {
                    fail("Couldn't find service " + SERVICE_NAME);
                }
            }

            @Override
            public void onShutdown(Node node) {
                for (ClientThread thread: threads) {
                    thread.interrupt();
                }
            }
        };

        // Start the transform server node
        nodeMainExecutor.execute(serverNode, nodeConfiguration);
        // Give time for service to be available
        Thread.sleep(1000);
        // Start the anonymous node to test the server
        nodeMainExecutor.execute(clientNode, secondConfig);

        Thread.sleep(1000);
        //assertTrue(countDownLatch.await(20, TimeUnit.SECONDS)); // Check if service calls were successful

        for(ClientThread a : threads) {
            assertTrue(a.correct[0]);
        }
        // // Shutdown nodes
        // nodeMainExecutor.shutdownNodeMain(clientNode);
        // // Shutting down the transform server from this test results in a exception on printing the service address
        // nodeMainExecutor.shutdownNodeMain(serverNode);
        // Stack trace is automatically logged
        // ROS is shutdown automatically in cleanup from ROS Test
    }

    /**
     * Class for making service requests
     */
    public static class ClientThread extends Thread {
        final int a;
        final int b;
        final CountDownLatch countDownLatch;
        final MessageFactory messageFactory;
        final ServiceClient<AddTwoIntsRequest, AddTwoIntsResponse> serviceClient;
        final String service;
        final boolean[] done = new boolean[1];
        final Log log;
        final boolean[] correct = new boolean[1];

        ClientThread(int reqA, int reqB, CountDownLatch countDownLatch, MessageFactory messageFactory,
                     String service, ServiceClient<AddTwoIntsRequest, AddTwoIntsResponse> serviceClient, Log log) {
            this.a = reqA;
            this.b = reqB;
            this.countDownLatch = countDownLatch;
            this.messageFactory = messageFactory;
            this.service = service;
            this.serviceClient = serviceClient;
            this.done[0] = true;
            this.log = log;
            this.correct[0] = false;
        }

        @Override
        public void run(){
            // Build ros messages
            if (done[0]) {

                done[0] = false;
                final AddTwoIntsRequest req = messageFactory.newFromType(AddTwoIntsRequest._TYPE);


                req.setA(a);
                req.setB(b);

                serviceClient.call(req, new ServiceResponseListener<AddTwoIntsResponse>() {
                    @Override public void onSuccess(AddTwoIntsResponse response) {
                        log.info("Request: " + req.getA() + "+" + req.getB() + " Result: " + response.getSum());
                        correct[0] = response.getSum() == (req.getA()+req.getB());
                        countDownLatch.countDown();
                        done[0] = true;
                    }

                    @Override
                    public void onFailure(RemoteException e) {
                        fail("Service request failed for request " + a +"+"+ b);
                    }
                });
            }
        }
    }
}