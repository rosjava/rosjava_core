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

import java.util.Arrays;
import java.util.Comparator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/** A subclass of {@link org.apache.ws.commons.serialize.XMLWriterImpl},
 * which writes the attributes ordered alphabetically. This is mainly
 * useful for test purposes, when a canonical representation of the
 * result is required for comparing against an expected value.
 */
public class OrderedAttributeXMLWriter extends XMLWriterImpl {
    public void startElement(String pNamespaceURI, String pLocalName,
                             String pQName, final Attributes pAttrs)
            throws SAXException {
        Integer[] attributeNumbers = new Integer[pAttrs.getLength()];
        for (int i = 0;  i < attributeNumbers.length;  i++) {
        	attributeNumbers[i] = new Integer(i);
        }
        Arrays.sort(attributeNumbers, new Comparator(){
			public int compare(Object pNum1, Object pNum2) {
                int i1 = ((Integer) pNum1).intValue();
                int i2 = ((Integer) pNum2).intValue();
                String uri1 = pAttrs.getURI(i1);
                if (uri1 == null) {
                	uri1 = "";
                }
                String uri2 = pAttrs.getURI(i2);
                if (uri2 == null) {
                	uri2 = "";
                }
                int result = uri1.compareTo(uri2);
                if (result == 0) {
                	result = pAttrs.getLocalName(i1).compareTo(pAttrs.getLocalName(i2));
                }
                return result;
			}
        });
        AttributesImpl orderedAttributes = new AttributesImpl();
        for (int i = 0;  i < attributeNumbers.length;  i++) {
        	int num = attributeNumbers[i].intValue();
            orderedAttributes.addAttribute(pAttrs.getURI(num), pAttrs.getLocalName(num),
                                           pAttrs.getQName(num), pAttrs.getType(num),
                                           pAttrs.getValue(num));
        }
        super.startElement(pNamespaceURI, pLocalName, pQName, orderedAttributes);
    }
}
