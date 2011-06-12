/*
 * Copyright 2003,2004  The Apache Software Foundation
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
package org.apache.ws.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


/** Default implementation of {@link javax.xml.namespace.NamespaceContext}.
 */
public class NamespaceContextImpl implements NamespaceContext {
	/* List of currently defined prefixes (even indexes, 0, 2, 4, ...)
	 * and namespace URI's (odd indexes, 1, 3, 5, ...)
	 */
	private List prefixList;
	/* The prefix and namespace URI, which have been defined
	 * last. It is assumed, that these are looked up the most.
	 * Thus we keep them in separate variables, for reasons
	 * of speed.
	 */
    private String cachedPrefix, cachedURI;
    
	/** Resets the NamespaceSupport's state. Allows reusing the
	 * object.
     */
    public void reset() {
        cachedURI = cachedPrefix = null;
        if (prefixList != null) {
            prefixList.clear();
        }
    }
    
	/** Declares a new prefix. Typically called from within
	 * {@link org.xml.sax.ContextHandler#startPrefixMapping(java.lang.String, java.lang.String)}.
	 * @throws IllegalArgumentException Prefix or URI are null.
     */
    public void startPrefixMapping(String pPrefix, String pURI) {
		if (pPrefix == null) {
			throw new IllegalArgumentException("The namespace prefix must not be null.");
		}
		if (pURI == null) {
			throw new IllegalArgumentException("The namespace prefix must not be null.");
		}
        if (cachedURI != null) {
            if (prefixList == null) { prefixList = new ArrayList(); }
            prefixList.add(cachedPrefix);
            prefixList.add(cachedURI);
        }
		cachedURI = pURI;
		cachedPrefix = pPrefix;
    }

	/** Removes the declaration of the prefix, which has been defined
	 * last. Typically called from within
	 * {@link org.xml.sax.ContextHandler#endPrefixMapping(java.lang.String)}.
	 * @throws IllegalArgumentException The prefix is null.
	 * @throws IllegalStateException The prefix is not the prefix, which
	 * has been defined last. In other words, the calls to
	 * {@link #startPrefixMapping(String, String)}, and
	 * {@link #endPrefixMapping(String)} aren't in LIFO order.
     */
    public void endPrefixMapping(String pPrefix) {
		if (pPrefix == null) {
			throw new IllegalArgumentException("The namespace prefix must not be null.");
		}
        if (pPrefix.equals(cachedPrefix)) {
            if (prefixList != null  &&  prefixList.size() > 0) {
                cachedURI = prefixList.remove(prefixList.size()-1).toString();
                cachedPrefix = prefixList.remove(prefixList.size()-1).toString();
            } else {
                cachedPrefix = cachedURI = null;
            }
        } else {
            throw new IllegalStateException("The prefix " + pPrefix
											+ " isn't the prefix, which has been defined last.");
        }
    }
    
    /** Given a prefix, returns the URI to which the prefix is
     * currently mapped or null, if there is no such mapping.</p>
     * <p><em>Note</em>: This methods behaviour is precisely
     * defined by {@link NamespaceContext#getNamespaceURI(java.lang.String)}.
     * @param pPrefix The prefix in question
     */
    public String getNamespaceURI(String pPrefix) {
		if (pPrefix == null) {
			throw new IllegalArgumentException("The namespace prefix must not be null.");
		}
        if (cachedURI != null) {
            if (cachedPrefix.equals(pPrefix)) { return cachedURI; }
            if (prefixList != null) {
                for (int i = prefixList.size();  i > 0;  i -= 2) {
                    if (pPrefix.equals(prefixList.get(i-2))) {
                        return (String) prefixList.get(i-1);
                    }
                }
            }
        }
        if (XMLConstants.XML_NS_PREFIX.equals(pPrefix)) {
            return XMLConstants.XML_NS_URI;
        } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(pPrefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        return null;
    }
    
	/** Returns a prefix currently mapped to the given URI or
     * null, if there is no such mapping. This method may be used
     * to find a possible prefix for an elements namespace URI. For
     * attributes you should use {@link #getAttributePrefix(String)}.
     * <em>Note</em>: This methods behaviour is precisely
     * defined by {@link NamespaceContext#getPrefix(java.lang.String)}.
     * @param pURI The namespace URI in question
     * @throws IllegalArgumentException The namespace URI is null.
     */
    public String getPrefix(String pURI) {
		if (pURI == null) {
			throw new IllegalArgumentException("The namespace URI must not be null.");
		}
        if (cachedURI != null) {
            if (cachedURI.equals(pURI)) { return cachedPrefix; }
            if (prefixList != null) {
                for (int i = prefixList.size();  i > 0;  i -= 2) {
                    if (pURI.equals(prefixList.get(i-1))) {
                        return (String) prefixList.get(i-2);
                    }
                }
            }
        }
        if (XMLConstants.XML_NS_URI.equals(pURI)) {
            return XMLConstants.XML_NS_PREFIX;
        } else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(pURI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        return null;
    }
    
	/** Returns a non-empty prefix currently mapped to the given
     * URL or null, if there is no such mapping. This method may be
     * used to find a possible prefix for an attributes namespace
     * URI. For elements you should use {@link #getPrefix(String)}.
     * @param pURI Thhe namespace URI in question
     * @throws IllegalArgumentException The namespace URI is null.
     */
    public String getAttributePrefix(String pURI) {
		if (pURI == null) {
			throw new IllegalArgumentException("The namespace URI must not be null.");
		}
        if (pURI.length() == 0) {
            return "";
        }
        if (cachedURI != null) {
            if (cachedURI.equals(pURI)  &&  cachedPrefix.length() > 0) {
                return cachedPrefix;
            }
            if (prefixList != null) {
                for (int i = prefixList.size();  i > 0;  i -= 2) {
                    if (pURI.equals(prefixList.get(i-1))) {
                        String prefix = (String) prefixList.get(i-2);
                        if (prefix.length() > 0) {
                            return prefix;
                        }
                    }
                }
            }
        }    
        if (XMLConstants.XML_NS_URI.equals(pURI)) {
            return XMLConstants.XML_NS_PREFIX;
        } else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(pURI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        return null;
    }
    
	/** Returns a collection to all prefixes bound to the given
     * namespace URI.
     * <em>Note</em>: This methods behaviour is precisely
     * defined by {@link NamespaceContext#getPrefixes(java.lang.String)}.
     * @param pURI The namespace prefix in question
     */
    public Iterator getPrefixes(String pURI) {
		if (pURI == null) {
			throw new IllegalArgumentException("The namespace URI must not be null.");
		}
        List list = new ArrayList();
        if (cachedURI != null) {
            if (cachedURI.equals(pURI)) { list.add(cachedPrefix); }
            if (prefixList != null) {
                for (int i = prefixList.size();  i > 0;  i -= 2) {
                    if (pURI.equals(prefixList.get(i-1))) {
                        list.add(prefixList.get(i-2));
                    }
                }
            }
        }
        if (pURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            list.add(XMLConstants.XMLNS_ATTRIBUTE);
        } else if (pURI.equals(XMLConstants.XML_NS_URI)) {
            list.add(XMLConstants.XML_NS_PREFIX);
        }
        return list.iterator();
    }
    
	/** Returns whether a given prefix is currently declared.
     */
    public boolean isPrefixDeclared(String pPrefix) {
        if (cachedURI != null) {
            if (cachedPrefix != null  &&  cachedPrefix.equals(pPrefix)) { return true; }
            if (prefixList != null) {
                for (int i = prefixList.size();  i > 0;  i -= 2) {
                    if (prefixList.get(i-2).equals(pPrefix)) {
                        return true;
                    }
                }
            }
        }
        return "xml".equals(pPrefix);
    }

	/** Returns the current number of assigned prefixes.
	 * Note, that a prefix may be assigned in several nested
	 * elements, in which case every assignment is counted.<br>
	 * This method is typically called before invoking the
	 * method
	 * {@link org.xml.sax.ContentHandler#startElement(String, String, String, org.xml.sax.Attributes)}.
	 * The return value is used as a saveable state. After
	 * invoking 
	 * {@link org.xml.sax.ContentHandler#endElement(String, String, String)},
	 * the state is restored by calling {@link #checkContext(int)}.
	 */
    public int getContext() {
        return (prefixList == null ? 0 : prefixList.size()) +
        	(cachedURI == null ? 0 : 2);
    }

	/** This method is used to restore the namespace state
	 * after an element is created. It takes as input a state,
	 * as returned by {@link #getContext()}.<br>
	 * For any prefix, which was since saving the state,
	 * the prefix is returned and deleted from the internal
	 * list. In other words, a typical use looks like this:
	 * <pre>
	 *   NamespaceSupport nss;
	 *   ContentHandler h;
	 *   int context = nss.getContext();
	 *   h.startElement("foo", "bar", "f:bar", new AttributesImpl());
	 *   ...
	 *   h.endElement("foo", "bar", "f:bar");
	 *   for (;;) {
	 *     String prefix = nss.checkContext(context);
	 *     if (prefix == null) {
	 *       break;
	 *     }
	 *     h.endPrefixMapping(prefix);
	 *   }
	 * </pre>
	 */
	public String checkContext(int i) {
        if (getContext() == i) {
            return null;
        }
        String result = cachedPrefix;
        if (prefixList != null  &&  prefixList.size() > 0) {
            cachedURI = prefixList.remove(prefixList.size()-1).toString();
            cachedPrefix = prefixList.remove(prefixList.size()-1).toString();
        } else {
            cachedURI = null;
            cachedPrefix = null;
        }
        return result;
    }

	/** Returns a list of all prefixes, which are currently declared,
	 * in the order of declaration. Duplicates are possible, if a
	 * prefix has been assigned to more than one URI, or repeatedly to
	 * the same URI.
	 */
	public List getPrefixes() {
		if (cachedPrefix == null) {
			return Collections.EMPTY_LIST;
		} else if (prefixList == null) {
			return Collections.singletonList(cachedPrefix);
		} else {
			List result = new ArrayList(prefixList.size() + 1);
			for (int i = 0;  i < prefixList.size();  i += 2) {
				result.add(prefixList.get(i));
			}
			result.add(cachedPrefix);
			return result;
		}
	}
}
