/*
 * Copyright (C) 2011 Alexander Perzylo, Technische Universität München
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

package org.ros.actionlib.state;

/**
 * 
 * A SimpleGoalState represents an SimpleActionClient's goal state. It defines
 * an enumeration of possible states and stores one of them as the current
 * state. The states are:
 * <ul>
 * <li>PENDING</li>
 * <li>ACTIVE</li>
 * <li>DONE</li>
 * </ul>
 * A SimpleGoalState uses a reduced set of possible states compared to a
 * SimpleClientGoalState. To be more explicit, all final states are merged into
 * the state 'DONE'.
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 */
public class SimpleGoalState {

  /**
   * Current goal state
   */
  private StateEnum state;

  /**
   * Enumeration of possible states
   */
  public static enum StateEnum {
    PENDING, ACTIVE, DONE;
  }

  /**
   * Simple constructor.
   * 
   * @param initialState
   *          The initial state
   */
  public SimpleGoalState(StateEnum initialState) {
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
      if (o instanceof SimpleGoalState)
        return this.state.equals(((SimpleGoalState) o).getState());
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
