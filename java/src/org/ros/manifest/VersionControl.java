package org.ros.manifest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

public class VersionControl implements XmlSerializable {

  private static final String VERSION_CONTROL_TAG = "versioncontrol";
  private static final String URL_ATTRIBUTE = "url";
  private static final String TYPE_ATTRIBUTE = "type";
  
  private final String type;
  private final URL url;

  public VersionControl(String type, URL url) {
    Preconditions.checkNotNull(type);
    Preconditions.checkArgument(type.length() != 0);
    Preconditions.checkNotNull(url);
    this.type = type;
    this.url = url;
  }

  public VersionControl(Element element) throws MalformedURLException {
    Preconditions.checkArgument(element.getNodeName().equals(VERSION_CONTROL_TAG));
    this.type = element.getAttribute(TYPE_ATTRIBUTE);
    this.url = new URL(element.getAttribute(URL_ATTRIBUTE));
  }
  
  public static boolean checkNode(Node node) {
    return node.getNodeName().equals(VERSION_CONTROL_TAG);
  }

  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    Element element = doc.createElement(VERSION_CONTROL_TAG);
    element.setAttribute(TYPE_ATTRIBUTE, type);
    element.setAttribute(URL_ATTRIBUTE, url.toString());
    return element;
  }

}
