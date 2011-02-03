package org.ros.manifest;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XmlSerializable {
  
  public Element toElement(Document doc) throws ParserConfigurationException;

}
