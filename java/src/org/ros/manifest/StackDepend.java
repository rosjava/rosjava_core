package org.ros.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StackDepend extends Depend {
  
  private final String stack;
  
  public StackDepend(String stack) {
    this.stack = stack;
  }

  @Override
  public Element toElement(Document doc) {
    Element element = super.toElement(doc);
    element.setAttribute("stack", stack);
    return element;
  }

}
