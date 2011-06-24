package org.ros.actionlib.state;

/**
 * 
 * A SimpleClientGoalState represents a SimpleActionClient's goal state. It
 * defines an enumeration of possible states and stores one of them as the
 * current state. The states are:
 * <ul>
 * <li>PENDING</li>
 * <li>ACTIVE</li>
 * <li>RECALLED</li>
 * <li>REJECTED</li>
 * <li>PREEMPTED</li>
 * <li>SUCCEEDED</li>
 * <li>ABORTED</li>
 * <li>LOST</li>
 * </ul>
 * A simplified representation of the goal state is available through the
 * SimpleGoalState class, which reduces the number of possible states.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 */
public class SimpleClientGoalState {

  /**
   * Current goal state
   */
  private StateEnum state;

  /**
   * Optional annotated text
   */
  private String text;

  /**
   * Enumeration of possible states
   */
  public static enum StateEnum {
    PENDING, ACTIVE, RECALLED, REJECTED, PREEMPTED, SUCCEEDED, ABORTED, LOST;
  }

  /**
   * Simple constructor.
   * 
   * @param initialState
   *          The initial state
   */
  public SimpleClientGoalState(StateEnum initialState) {
    this.state = initialState;
  }

  /**
   * Constructor used to create a SimpleClientGoalState with a given initial
   * state and a String object as an annotation.
   * 
   * @param initialState
   *          The initial state
   * @param text
   *          An annotation
   */
  public SimpleClientGoalState(StateEnum initialState, String text) {
    this.state = initialState;
    this.text = text;
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

  /**
   * Gets an annotation associated with the state.
   * 
   * @return An annotation
   */
  public String getText() {
    return this.text;
  }

  /**
   * Sets String object as an annotation.
   * 
   * @param text
   *          An annotation
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Checks whether the current state is a final state or not.
   * 
   * @return <tt>true</tt> - if current state is a final state<br>
   *         <tt>false</tt> - otherwise
   */
  public boolean isDone() {

    switch (state) {
    case RECALLED:
    case REJECTED:
    case PREEMPTED:
    case SUCCEEDED:
    case ABORTED:
    case LOST:
      return true;
    default:
      return false;
    }

  }

  @Override
  public boolean equals(Object o) {

    if (o != null) {
      if (o instanceof SimpleClientGoalState)
        return this.state.equals(((SimpleClientGoalState) o).getState());
      if (o instanceof StateEnum) {
        return this.state.equals((StateEnum) o);
      }
    }
    return false;

  }

  @Override
  public String toString() {
    return this.state.toString();
  }

}
