package org.ros.manifest;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Depend implements XmlSerializable {
  
  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    return doc.createElement("depend");
  }

}
