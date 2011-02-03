package org.ros.manifest;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ManifestDepend extends Depend {
  
  private final String package_name;
  
  public ManifestDepend(String package_name) {
    this.package_name = package_name;
  }
  
  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    Element element = super.toElement(doc);
    element.setAttribute("package", package_name);
    return element;
  }

}
