package org.ros.manifest;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

public class Description implements XmlSerializable {
  
  static final String DESCRIPTION_TAG = "description";
  static final String BRIEF_ATTRIBUTE = "brief";
  
  private String description;
  private String brief;
  
  public Description(String description) {
    Preconditions.checkNotNull(description);
    this.description = description;
    this.brief = "";
  }
  
  public Description(String description, String brief) {
    this(description);
    Preconditions.checkNotNull(brief);
    this.brief = brief;
  }
  
  public Description(Element element) {
    Preconditions.checkArgument(element.getNodeName().equals(DESCRIPTION_TAG));
    this.description = element.getFirstChild().getNodeValue();
    this.brief = element.getAttribute(BRIEF_ATTRIBUTE);
  }
  
  public static boolean checkNodeName(Node node) {
    return node.getNodeName().equals(DESCRIPTION_TAG);
  }
  
  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    Element element = doc.createElement(DESCRIPTION_TAG);
    element.appendChild(doc.createTextNode(description));
    if (brief.length() != 0) {
      element.setAttribute(BRIEF_ATTRIBUTE, brief);
    }
    return element;
  }
  
}
