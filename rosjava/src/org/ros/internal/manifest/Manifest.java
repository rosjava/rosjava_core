/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.manifest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Manifest implements XmlSerializable {

  private static final String LOGO_TAG = "logo";
  private static final String URL_TAG = "url";
  private static final String PACKAGE_TAG = "package";
  private static final String AUTHOR_TAG = "author";

  private final String type;
  private final Collection<Depend> depends;
  private final Collection<RosDep> rosdeps;
  private final Collection<Export> exports;

  private String author;
  private License license;
  private Description description;
  private String logo;
  private Review review;
  private URL url;
  private VersionControl versionControl;

  private Manifest() {
    type = PACKAGE_TAG;
    depends = Lists.newArrayList();
    rosdeps = Lists.newArrayList();
    exports = Lists.newArrayList();
  }

  public Manifest(String author, License license) {
    this();
    Preconditions.checkNotNull(author);
    Preconditions.checkNotNull(license);
    this.author = author;
    this.license = license;
  }

  public Manifest(Element element) throws MalformedURLException {
    this();
    Preconditions.checkArgument(element.getNodeName().equals(type));
    fromElement(element);
  }

  public static Manifest parseFromXml(String xml) throws ParserConfigurationException,
      SAXException, IOException {
    DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
    doc.getDocumentElement().normalize();
    return new Manifest(doc.getDocumentElement());
  }

  public void addDepend(Depend depend) {
    depends.add(depend);
  }

  public boolean removeDepend(Depend depend) {
    return depends.remove(depend);
  }

  public void addRosDep(RosDep rosdep) {
    rosdeps.add(rosdep);
  }

  public boolean removeRosDep(RosDep rosdep) {
    return rosdeps.remove(rosdep);
  }

  public void addExport(Export export) {
    exports.add(export);
  }

  public boolean removeExport(Export export) {
    return exports.remove(export);
  }

  public String getType() {
    return type;
  }

  public Description getDescription() {
    return description;
  }

  public void setDescription(Description description) {
    this.description = description;
  }

  public String getAuthor() {
    return author;
  }

  public License getLicense() {
    return license;
  }

  public Review getReview() {
    return review;
  }
  
  public void setReview(Review review) {
    this.review = review;
  }
  
  public String getLogo() {
    return logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public VersionControl getVersionControl() {
    return versionControl;
  }

  public void setVersionControl(VersionControl versionControl) {
    this.versionControl = versionControl;
  }

  public String toXml() throws ParserConfigurationException, TransformerException {
    TransformerFactory transfac = TransformerFactory.newInstance();
    Transformer trans = transfac.newTransformer();
    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    StringWriter sw = new StringWriter();
    StreamResult result = new StreamResult(sw);
    DOMSource source = new DOMSource(toDocument());
    trans.transform(source, result);
    return sw.toString();
  }

  private Document toDocument() throws DOMException, ParserConfigurationException {
    DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    doc.appendChild(toElement(doc));
    return doc;
  }

  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    Element root = doc.createElement(type);
    addElementWithText(doc, root, AUTHOR_TAG, author);
    root.appendChild(license.toElement(doc));
    if (description != null) {
      root.appendChild(description.toElement(doc));
    }
    if (url != null) {
      addElementWithText(doc, root, URL_TAG, url.toString());
    }
    if (logo != null && logo.length() != 0) {
      addElementWithText(doc, root, LOGO_TAG, logo);
    }
    if (versionControl != null) {
      root.appendChild(versionControl.toElement(doc));
    }
    if (review != null) {
      root.appendChild(review.toElement(doc));
    }
    if (!exports.isEmpty()) {
      Element exportsElement = addElement(doc, root, "export");
      addAll(doc, exportsElement, exports);
    }
    addAll(doc, root, depends);
    addAll(doc, root, rosdeps);
    return root;
  }

  private void addAll(Document doc, Element root, Collection<? extends XmlSerializable> elements)
      throws DOMException, ParserConfigurationException {
    for (XmlSerializable element : elements) {
      root.appendChild(element.toElement(doc));
    }
  }

  private Element addElementWithText(Document doc, Element root, String name, String text) {
    Element element = addElement(doc, root, name);
    element.appendChild(doc.createTextNode(text));
    return element;
  }

  private Element addElement(Document doc, Element root, String name) {
    Element element = doc.createElement(name);
    root.appendChild(element);
    return element;
  }

  private void fromElement(Element element) throws MalformedURLException {
    Preconditions.checkArgument(element.getNodeName().equals(type));
    NodeList nodes = element.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      Preconditions.checkState(childNode.getNodeType() == Node.ELEMENT_NODE);
      String name = childNode.getNodeName();
      if (name.equals(AUTHOR_TAG)) {
        author = childNode.getFirstChild().getNodeValue();
      }
      if (License.checkNodeName(childNode)) {
        license = new License((Element) childNode);
      }
      if (Description.checkNodeName(childNode)) {
        description = new Description((Element) childNode);
      }
      if (name.equals(URL_TAG)) {
        url = new URL(childNode.getFirstChild().getNodeValue());
      }
      if (name.equals(LOGO_TAG)) {
        logo = childNode.getFirstChild().getNodeValue();
      }
      if (VersionControl.checkNode(childNode)) {
        versionControl = new VersionControl((Element) childNode);
      }
      if (Review.checkNode(childNode)) {
        review = new Review((Element) childNode);
      }
    }
  }
}
