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
 * A TerminalState represents an action client's final goal state. It defines an
 * enumeration of possible states and stores one of them as the current state.
 * The states are:
 * <ul>
 * <li>RECALLED</li>
 * <li>REJECTED</li>
 * <li>PREEMPTED</li>
 * <li>SUCCEEDED</li>
 * <li>ABORTED</li>
 * <li>LOST</li>
 * </ul>
 * 
 * @author Alexander C. Perzylo, perzylo@cs.tum.edu
 * 
 */
public class TerminalState {

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
    RECALLED, REJECTED, PREEMPTED, SUCCEEDED, ABORTED, LOST;
  }

  /**
   * Simple constructor
   * 
   * @param initialState
   *          The initial state
   */
  public TerminalState(StateEnum initialState) {
    this.state = initialState;
    this.text = null;
  }

  /**
   * Constructor used to create a TerminalState with a given initial state and a
   * String object as an annotation.
   * 
   * @param initialState
   *          The initial state
   * @param text
   *          An annotation
   */
  public TerminalState(StateEnum initialState, String text) {
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

  @Override
  public boolean equals(Object o) {

    if (o != null) {
      if (o instanceof TerminalState)
        return this.state.equals(((TerminalState) o).getState());
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
