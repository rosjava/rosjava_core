package org.ros.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Depend implements XmlSerializable {
  
  @Override
  public Element toElement(Document doc) {
    return doc.createElement("depend");
  }

}
