package org.ros.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RosDep implements XmlSerializable {

  private final String name;
  
  public RosDep(String name) {
    this.name = name;
  }
  
  @Override
  public Element toElement(Document doc) {
    Element element = doc.createElement("rosdep");
    element.setAttribute("name", name);
    return element;
  }

}
