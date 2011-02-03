package org.ros.manifest;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

public class Review implements XmlSerializable {

  private static final String REVIEW_TAG = "review";
  private static final String NOTES_ATTRIBUTE = "notes";
  private static final String STATUS_ATTRIBUTE = "status";
  
  private final String status;
  private final String notes;

  public Review(String status, String notes) {
    Preconditions.checkNotNull(status);
    Preconditions.checkNotNull(notes);
    this.status = status;
    this.notes = notes;
  }
  
  public Review(Element element) {  
    Preconditions.checkArgument(element.getNodeName().equals(REVIEW_TAG));
    this.status = element.getAttribute(STATUS_ATTRIBUTE);
    this.notes = element.getAttribute(NOTES_ATTRIBUTE);
  }
  
  public static boolean checkNode(Node node) {
    return node.getNodeName().equals(REVIEW_TAG);
  }

  @Override
  public Element toElement(Document doc) throws ParserConfigurationException {
    Element review = doc.createElement(REVIEW_TAG);
    review.setAttribute(STATUS_ATTRIBUTE, status);
    review.setAttribute(NOTES_ATTRIBUTE, notes);
    return review;
  }

}
