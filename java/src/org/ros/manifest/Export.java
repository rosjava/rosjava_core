package org.ros.manifest;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Maps;

public class Export implements XmlSerializable {
  
  private final String name;
  private final String value;
  private final Map<String, String> attributes;
  
  public Export(String name, String value) {
    this.name = name;
    this.value = value;
    attributes = Maps.newHashMap();
  }
  
  public void addAttribute(String name, String value) {
    attributes.put(name, value);
  }

  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    Element element = doc.createElement(name);
    for (Entry<String, String> entry : attributes.entrySet()) {
      element.setAttribute(entry.getKey(), entry.getValue());
    }
    element.appendChild(doc.createTextNode(value));
    return element;
  }

}
