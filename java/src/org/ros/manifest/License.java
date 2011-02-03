package org.ros.manifest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

public class License implements XmlSerializable {
  
  private static final String URL_ATTRIBUTE = "url";
  private static final String LICENSE_TAG = "license";
  private final String license;
  private final URL url;
  
  public License(String license, URL url) {
    Preconditions.checkNotNull(license);
    Preconditions.checkNotNull(url);
    this.license = license;
    this.url = url;
  }
  
  public License(Element element) throws MalformedURLException {
    Preconditions.checkArgument(element.getNodeName().equals(LICENSE_TAG));
    this.license = element.getFirstChild().getNodeValue();
    this.url = new URL(element.getAttribute(URL_ATTRIBUTE));
  }
  
  public static boolean checkNodeName(Node node) {
    return node.getNodeName().equals(LICENSE_TAG);
  }

  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    Element element = doc.createElement(LICENSE_TAG);
    element.appendChild(doc.createTextNode(license));
    if (url != null) {
      element.setAttribute(URL_ATTRIBUTE, url.toString());
    }
    return element;
  }

}
