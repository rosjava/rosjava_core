package org.ros.actionlib.client;

import org.ros.actionlib.ActionConstants;
import org.ros.actionlib.ActionSpec;
import org.ros.exception.RosException;
import org.ros.message.Message;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.message.actionlib_msgs.GoalID;
import org.ros.message.actionlib_msgs.GoalStatusArray;
import org.ros.node.Node;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * An ActionClient is the client interface of the actionlib package. It provides
 * means to interact with an action server, i.e. sending goal messages to the
 * action server, which tries to compute/create a result according to the
 * request that is sent back to the client. During the process of getting the
 * final result an action server may send feedback messages in order to update
 * the action client on the current state of its request. A request may be
 * canceled by the ActionClient by sending a cancel message to the server.<br>
 * Every goal that is sent to the server is represented by a
 * {@link ClientGoalHandle} that allows to monitor the goal's progress. An
 * ActionClient may send more than one goal message at a time.
 * 
 * @see SimpleActionClient
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 * @param <T_ACTION_FEEDBACK>
 *          action feedback message
 * @param <T_ACTION_GOAL>
 *          action goal message
 * @param <T_ACTION_RESULT>
 *          action result message
 * @param <T_FEEDBACK>
 *          feedback message
 * @param <T_GOAL>
 *          goal message
 * @param <T_RESULT>
 *          result message
 */
public class ActionClient<T_ACTION_FEEDBACK extends Message, T_ACTION_GOAL extends Message, T_ACTION_RESULT extends Message, T_FEEDBACK extends Message, T_GOAL extends Message, T_RESULT extends Message> {
  /**
   * The action specification
   */
  protected ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec;

  /**
   * The GoalManager used to initialize new goals and to update all active
   * GoalHandles on the reception of status, feedback and result messages
   */
  protected GoalManager<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalManager;

  /**
   * The ActionClient's node.
   */
  protected Node node;

  /**
   * Name of the action client's node.
   */
  protected String nodeName;

  /**
   * A Subscriber of the action server's status topic.
   */
  protected Subscriber<GoalStatusArray> subStatus;

  /**
   * A Subscriber of the action server's feedback topic.
   */
  protected Subscriber<T_ACTION_FEEDBACK> subFeedback;

  /**
   * A Subscriber of the action server's result topic.
   */
  protected Subscriber<T_ACTION_RESULT> subResult;

  /**
   * A Publisher for goal messages using the goal topic.
   */
  protected Publisher<T_ACTION_GOAL> pubGoal;

  /**
   * A Publisher for cancel messages using the cancel topic.
   */
  protected Publisher<GoalID> pubCancelGoal;

  /**
   * A flag indicating whether or not a first status message was already
   * received from the action server or not
   */
  protected boolean statusReceived = false;

  /**
   * The action server's frame ID
   */
  protected String callerID = "";

  /**
   * The time when the last status message was received
   */
  protected Time latestStatusTime;

  /**
   * A flag indicating whether or not this ActionClient was shutdown and cannot
   * be used anymore
   */
  protected volatile boolean active = true;

  /**
   * Listeners to know when the client is connected or not.
   */
  private CountDownLatch readyLatch;
  private PublisherListener<T_ACTION_GOAL> goalPublisherListener;
  private PublisherListener<GoalID> cancelPublisherListener;

  /**
   * Constructor used to create an ActionClient that will be a child node of the
   * node represented by the given node handle. It communicates in a given
   * nodeName space on a given action specification.
   * 
   * @param nameSpace
   *          The nodeName space to communicate within (specified by the action
   *          server)
   * @param spec
   *          The specification of the action the ActionClient shall use
   * @throws RosException
   *           If setting up the needed subscribers and publishers fail
   */
  public ActionClient(
      String name,
      ActionSpec<?, T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> spec)
      throws RosException {
    this.nodeName = name;
    this.spec = spec;
    this.readyLatch = new CountDownLatch(2);
    this.goalPublisherListener = new DefaultPublisherListener<T_ACTION_GOAL>() {
      @Override
      public void onNewSubscriber(Publisher<T_ACTION_GOAL> publisher) {
        readyLatch.countDown();
      }
    };
    this.cancelPublisherListener = new DefaultPublisherListener<GoalID>() {
      @Override
      public void onNewSubscriber(Publisher<GoalID> publisher) {
        readyLatch.countDown();
      }
    };
  }

  /**
   * Add all action client publishers and subscribers to the given node.
   * 
   * <p>
   * Lifetime of the node is taken over by the client.
   * 
   * @param node
   */
  public void addClientPubSub(Node node) {
    this.node = node;

    MessageListener<T_ACTION_FEEDBACK> feedbackCallback = new MessageListener<T_ACTION_FEEDBACK>() {
      @Override
      public void onNewMessage(T_ACTION_FEEDBACK actionFeedback) {
        doFeedbackCallback(actionFeedback);
      }
    };
    subFeedback =
        node.newSubscriber(ActionConstants.TOPIC_NAME_FEEDBACK, spec.getActionFeedbackMessage());
    subFeedback.addMessageListener(feedbackCallback);

    MessageListener<T_ACTION_RESULT> resultCallback = new MessageListener<T_ACTION_RESULT>() {
      @Override
      public void onNewMessage(T_ACTION_RESULT actionResult) {
        doResultCallback(actionResult);
      }
    };
    subResult =
        node.newSubscriber(ActionConstants.TOPIC_NAME_RESULT, spec.getActionResultMessage());
    subResult.addMessageListener(resultCallback);

    MessageListener<GoalStatusArray> statusCallback = new MessageListener<GoalStatusArray>() {
      @Override
      public void onNewMessage(GoalStatusArray statusArray) {
        doStatusCallback(statusArray);
      }
    };
    subStatus =
        node.newSubscriber(ActionConstants.TOPIC_NAME_STATUS, ActionConstants.MESSAGE_TYPE_STATUS);
    subStatus.addMessageListener(statusCallback);

    pubGoal = node.newPublisher(ActionConstants.TOPIC_NAME_GOAL, spec.getActionGoalMessage());
    pubGoal.addListener(goalPublisherListener);
    pubCancelGoal =
        node.newPublisher(ActionConstants.TOPIC_NAME_CANCEL, ActionConstants.MESSAGE_TYPE_CANCEL);
    pubCancelGoal.addListener(cancelPublisherListener);

    // Uses the node of the action client so must be done here.
    goalManager =
        new GoalManager<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>(
            this);
  }

  /**
   * Sends a goal message to the action server without demanding callback
   * methods which would enable the user to track the progress of the goal.
   * 
   * @param goal
   *          The goal message
   * @return The ClientGoalHandle that is associated with the goal message.
   */
  public
      ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>
      sendGoal(T_GOAL goal) throws RosException {
    return sendGoal(goal, null);
  }

  /**
   * Sends a goal message to the action server demanding callback methods which
   * enable the user to track the progress of the goal.
   * 
   * @param goal
   *          The goal message
   * @param callbacks
   *          The user's callback methods
   * @return The ClientGoalHandle that is associated with the goal message.
   */
  public
      ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT>
      sendGoal(
          T_GOAL goal,
          ActionClientCallbacks<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> callbacks)
          throws RosException {

    ClientGoalHandle<T_ACTION_FEEDBACK, T_ACTION_GOAL, T_ACTION_RESULT, T_FEEDBACK, T_GOAL, T_RESULT> goalHandle =
        null;

    if (active) {
      goalHandle = goalManager.initGoal(goal, callbacks, spec);
    } else {
      node.getLog()
          .warn(
              "[ActionClient] Trying to send a goal on an inactive ActionClient. You are incorrectly using an ActionClient.");
    }

    return goalHandle;

  }

  /**
   * Cancels the execution of all goals that were sent out with a timestamp
   * equal to or before the specified time.
   * 
   * @param time
   *          A point in time.
   */
  public void cancelGoalsAtAndBeforeTime(Time time) {

    if (!active) {
      node.getLog()
          .warn(
              "[ActionClient] Trying to cancel goals on an inactive ActionClient. You are incorrectly using an ActionClient.");
      return;
    }

    GoalID cancelMessage = new GoalID();
    cancelMessage.id = "";
    cancelMessage.stamp = time;
    pubCancelGoal.publish(cancelMessage);

  }

  /**
   * Cancels the execution of all goals.
   */
  public void cancelAllGoals() {
    cancelGoalsAtAndBeforeTime(new Time(0, 0));
  }

  /**
   * Checks if the ActionClient is active or was shutdown before.
   * 
   * @return <tt>true</tt> - If the ActionClient is active<br>
   *         <tt>false</tt> - Otherwise
   * 
   * @see #shutdown()
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Shut the action client down.
   */
  public void shutdown() {
    // Shuts down all subscribers and publishers and stops tracking all
    // goals without canceling them. All ClientGoalHandles created by
    // this ActionClient get deactivated. After an ActionClient was shutdown, it
    // cannot be used anymore.
    active = false;

    goalManager.clear();

    // pubGoal.shutdown();
    // pubCancelGoal.shutdown();
    // subResult.shutdown();
    // subFeedback.shutdown();
    // subStatus.shutdown();

    node.shutdown();
  }

  /**
   * Waits for the action server to start up.
   * 
   * @return <tt>true</tt> - if the SimpleActionClient could establish a
   *         connection to the action server<br>
   *         <tt>false</tt> - If the action client's node handle is not ok
   *         (normally, the method would block forever if no connection is
   *         established)
   */
  public void waitForActionServerToStart() throws InterruptedException {
    readyLatch.await();
  }

  /**
   * Waits up to the specified duration for the action server to start up. If no
   * connection could be established within the given time, the method returns
   * indicating the error.
   * 
   * @param timeout
   *          The maximum duration to wait for the action server to start up. A
   *          zero length duration results in unlimited waiting.
   * @param units
   *          The units for {@code timeout}
   * @return <tt>true</tt> - if the ActionClient could establish a connection to
   *         the action server within the given time<br>
   *         <tt>false</tt> - otherwise
   */
  public boolean waitForActionServerToStart(long timeout, TimeUnit units)
      throws InterruptedException {
    // TODO(keith): This isn't quite right since it will only have connection to
    // the goal and cancel publishers. This should be extended to include the
    // subscribers.
    return readyLatch.await(timeout, units);
  }

  /**
   * Gets the ActionClient's node.
   * 
   * @return The node
   */
  protected Node getNode() {
    return node;
  }

  /**
   * Publishes the given action goal message on the goal topic.
   * 
   * @param actionGoal
   *          The action goal message
   */
  protected void publishActionGoal(T_ACTION_GOAL actionGoal) {
    if (active) {
      pubGoal.publish(actionGoal);
    }
  }

  /**
   * Publishes the given GoalID message on the cancel topic. This should cause
   * the action server to stop working on the goal represented by this GoalID.
   * 
   * @param cancelMessage
   *          The GoalID message using the id of the goal that shall be canceled
   */
  protected void publishCancelGoal(GoalID cancelMessage) {
    if (active) {
      pubCancelGoal.publish(cancelMessage);
    }
  }

  /**
   * Callback method used when the subscriber of the action server's status
   * topic receives a new list of GoalStatus messages. This ActionClient's
   * GoalManager gets informed about the newly arrived messages.
   * 
   * @param goalStatuses
   *          A list of GoalStatus messages
   * 
   * @see GoalManager#updateStatuses(GoalStatusArray)
   */
  protected void doStatusCallback(GoalStatusArray goalStatuses) {

    String currCallerID = goalStatuses.header.frame_id;

    if (!statusReceived) {
      statusReceived = true;
      node.getLog().debug(
          "[ActionClient] Received first status message from action server (" + currCallerID + ")");
    } else if (!callerID.equals(currCallerID)) {
      node.getLog().warn(
          "[ActionClient] Previously received status from '" + callerID + "', now from '"
              + currCallerID + "'. Did the action server change?");
    }
    callerID = currCallerID;
    latestStatusTime = goalStatuses.header.stamp;

    goalManager.updateStatuses(goalStatuses);
  }

  /**
   * Callback method used when the subscriber of the action server's feedback
   * topic receives a new action feedback message. This ActionClient's
   * GoalManager gets informed about the newly arrived message.
   * 
   * @param actionFeedback
   *          An action feedback message
   * 
   * @see GoalManager#updateFeedbacks(Message)
   */
  protected void doFeedbackCallback(T_ACTION_FEEDBACK actionFeedback) {
    try {
      goalManager.updateFeedbacks(actionFeedback);
    } catch (RosException e) {
      node.getLog().error("Exception during feedback callback", e);
    }
  }

  /**
   * Callback method used when the subscriber of the action server's result
   * topic receives a new action result message. This ActionClient's GoalManager
   * gets informed about the newly arrived message.
   * 
   * @param actionResult
   *          An action result message
   * 
   * @see GoalManager#updateResults(Message)
   */
  protected void doResultCallback(T_ACTION_RESULT actionResult) {
    try {
      goalManager.updateResults(actionResult);
    } catch (RosException e) {
      node.getLog().error("Exception in Aresult callback", e);
    }
  }
}
