package org.ros.actionlib.state;

/**
 * 
 * A CommState represents a CommStateMachine's state. It defines an enumeration
 * of possible states and stores one of them as the current state. The states
 * are:
 * <ul>
 * <li>WAITING_FOR_GOAL_ACK</li>
 * <li>PENDING</li>
 * <li>ACTIVE</li>
 * <li>WAITING_FOR_CANCEL_ACK</li>
 * <li>RECALLING</li>
 * <li>PREEMPTING</li>
 * <li>WAITING_FOR_RESULT</li>
 * <li>DONE</li>
 * </ul>
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 */
public class CommState {

  /**
   * Current comm state
   */
  private StateEnum state;

  /**
   * Enumeration of possible states
   */
  public static enum StateEnum {
    WAITING_FOR_GOAL_ACK, PENDING, ACTIVE, WAITING_FOR_CANCEL_ACK, RECALLING, PREEMPTING,
    WAITING_FOR_RESULT, DONE;
  }

  /**
   * Simple constructor.
   * 
   * @param initialState
   *          The initial state
   */
  public CommState(StateEnum initialState) {
    this.state = initialState;
  }

  /**
   * Gets current state.
   * 
   * @return The current state
   */
  public StateEnum getState() {
    return this.state;
  }

  /**
   * Sets current state.
   * 
   * @param state
   *          A new state
   */
  public void setState(StateEnum state) {
    this.state = state;
  }

  @Override
  public boolean equals(Object o) {

    if (o != null) {
      if (o instanceof CommState)
        return this.state.equals(((CommState) o).getState());
      if (o instanceof StateEnum) {
        return this.state.equals(o);
      }
    }
    return false;

  }

  @Override
  public String toString() {
    return this.state.toString();
  }

}
