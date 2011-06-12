/*
 * Copyright 2003, 2004  The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */
package org.apache.ws.commons.serialize;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/** Converts a stream of SAX events into a DOM node.
 */
public class DOMBuilder implements ContentHandler {
    private Document document;
    private Node target;
    private Node currentNode;
    private Locator locator;
    private boolean prefixMappingIsAttribute;
    private List prefixes;
    
    /** Sets whether the event {@link #startPrefixMapping}
     * shall create an <code>xmlns</code> attribute. Defaults
     * to false.
     * @return True, if <code>xmlns</code> attributes are being
     * created, false otherwise.
     */
    public boolean isPrefixMappingIsAttribute() {
        return prefixMappingIsAttribute;
    }

    /** Returns whether the event {@link #startPrefixMapping}
     * shall create an <code>xmlns</code> attribute. Defaults
     * to false.
     * @param pPrefixMappingIsAttribute True, if <code>xmlns</code>
     * attributes are being created, false otherwise.
     */
    public void setPrefixMappingIsAttribute(boolean pPrefixMappingIsAttribute) {
        prefixMappingIsAttribute = pPrefixMappingIsAttribute;
    }

    /** Sets the document being used as object factory.
     * @param pDocument The object factory.
     */
    public void setDocument(Document pDocument) {
        document = pDocument;
    }
    
    /** Returns the document being used as object factory.
     * @return pDocument The object factory.
     */
    public Document getDocument() {
        return document;
    }
    
    /** Sets the Locator.
     * @param pLocator The Locator being set.
     */
    public void setDocumentLocator(Locator pLocator) {
        locator = pLocator;
    }
    
    /** Returns the Locator.
     * @return The documents Locator.
     */
    public Locator getDocumentLocator() {
        return locator;
    }
    
    /** Sets the target node. The document is built as a fragment
     * in the target node.
     * @param pNode The target node.
     */
    public void setTarget(Node pNode) {
        target = pNode;
        currentNode = pNode;
        if (getDocument() == null) {
            setDocument(pNode.getNodeType() == Node.DOCUMENT_NODE ?
                    (Document) pNode : pNode.getOwnerDocument());
        }
    }
    
    /** Returns the target node. The document is built as a fragment
     * in the target node.
     * @return The target node.
     */
    public Node getTarget() {
        return target;
    }
    
    public void startDocument() throws SAXException {
    }
    
    public void endDocument() throws SAXException {
    }
    
    public void startPrefixMapping(String prefix, String uri)
    		throws SAXException {
        if (isPrefixMappingIsAttribute()) {
            if (prefixes == null) {
                prefixes = new ArrayList();
            }
            prefixes.add(prefix);
            prefixes.add(uri);
        }
    }
    
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    
    public void startElement(String pNamespaceURI, String pLocalName,
            				 String pQName, Attributes pAttr) throws SAXException {
        Document doc = getDocument();
        Element element;
        if (pNamespaceURI == null  ||  pNamespaceURI.length() == 0) {
            element = doc.createElement(pQName);
        } else {
            element = doc.createElementNS(pNamespaceURI, pQName);
        }
        if (pAttr != null) {
            for (int i = 0;  i < pAttr.getLength();  i++) {
                String uri = pAttr.getURI(i);
                String qName = pAttr.getQName(i);
                String value = pAttr.getValue(i);
                if (uri == null  ||  uri.length() == 0) {
                    element.setAttribute(qName, value);
                } else {
                    element.setAttributeNS(uri, qName, value);
                }
            }
        }
        if (prefixes != null) {
            for (int i = 0;  i < prefixes.size();  i += 2) {
                String prefix = (String) prefixes.get(i);
                String uri = (String) prefixes.get(i+1);
                if (prefix == null  ||  "".equals(prefix)) {
                    element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                                           XMLConstants.XMLNS_ATTRIBUTE, uri);
                } else {
                    element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                                           XMLConstants.XMLNS_ATTRIBUTE + ':' + prefix, uri);
                }
            }
            prefixes.clear();
        }
        currentNode.appendChild(element);
        currentNode = element;
    }
    
    public void endElement(String namespaceURI, String localName, String qName)
    		throws SAXException {
        currentNode = currentNode.getParentNode();
    }
    
    public void characters(char[] ch, int start, int length)
    		throws SAXException {
        Node node = currentNode.getLastChild();
        String s = new String(ch, start, length);
        if (node != null  &&  node.getNodeType() == Node.TEXT_NODE) {
            ((Text) node).appendData(s);
        } else {
            Text text = getDocument().createTextNode(s);
            currentNode.appendChild(text);
        }
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length)
    		throws SAXException {
        characters(ch, start, length);
    }
    
    public void processingInstruction(String pTarget, String pData)
    		throws SAXException {
        ProcessingInstruction pi = getDocument().createProcessingInstruction(pTarget, pData);
        currentNode.appendChild(pi);
    }
    
    public void skippedEntity(String pName) throws SAXException {
        EntityReference entity = getDocument().createEntityReference(pName);
        currentNode.appendChild(entity);
    }
}
