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

import java.io.Writer;


/** A simple serializer for XML documents, which is writing to
 * an instance of {@link java.io.Writer}.
 */
public interface XMLWriter extends org.xml.sax.ContentHandler {
	/** Sets the writers encoding.
	 * @param pEncoding Writers encoding, by default null, in
	 * which case UTF-8 is being used.
	 */
	public void setEncoding(String pEncoding);

	/** Returns the writers encoding.
	 * @return Writers encoding, by default null, in
	 * which case UTF-8 is being used.
	 */
	public String getEncoding();

	/** Sets, whether an XML declaration is being generated.
	 * @param pDeclarating Whether an XML declaration is generated. Defaults
	 * to false.
	 */
	public void setDeclarating(boolean pDeclarating);

	/** Returns, whether an XML declaration is being generated.
	 * @return Whether an XML declaration is generated. Defaults
	 * to false.
	 */
	public boolean isDeclarating();

	/** Sets the target {@link Writer}. This is typically an instance
	 * of {@link java.io.BufferedWriter}, which is connected to an
	 * instance of {@link java.io.OutputStreamWriter} with an encoding
	 * matching the XML documents encoding.
	 * @param pWriter The target writer.
	 */
	public void setWriter(Writer pWriter);

	/** Returns the target {@link Writer}. This is typically an instance
	 * of {@link java.io.BufferedWriter}, which is connected to an
	 * instance of {@link java.io.OutputStreamWriter} with an encoding
	 * matching the XML documents encoding.
	 * @return The target writer.
	 */
	public Writer getWriter();

	/** <p>Returns whether the XMLWriter can encode the character
	 * <code>c</code> without an escape sequence like &amp;#ddd;.</p>
	 * @param pChar The character being checked for escaping.
	 * @return Whether to encode the character.
	 */
	public boolean canEncode(char pChar);

	/** Returns, whether the <code>XMLWriter</code> is indenting
	 * (pretty printing). If you want indenting,
	 * you should consider to invoke the methods
	 * {@link #setIndentString(java.lang.String)} and
	 * {@link #setLineFeed(java.lang.String)} as well.
	 * @param pIndenting Whether indentation is enabled. Defaults to false.
	 */
	public void setIndenting(boolean pIndenting);

	/** Returns, whether the <code>XMLWriter</code> is indenting
	 * (pretty printing). If you want indenting,
	 * you should consider to invoke the methods
	 * {@link #setIndentString(java.lang.String)} and
	 * {@link #setLineFeed(java.lang.String)} as well.
	 * @return Whether indentation is enabled. Defaults to false.
	 */
	public boolean isIndenting();

	/** Sets the string being used to indent an XML element
	 * by one level. Ignored, if indentation is disabled.
	 * @param pIndentString The indentation string, by default "  " (two blanks).
	 */
	void setIndentString(String pIndentString);

	/** Returns the string being used to indent an XML element
	 * by one level. Ignored, if indentation is disabled.
	 * @return The indentation string, by default "  " (two blanks).
	 */
	String getIndentString();

	/** Sets the line terminator. Ignored, if indentation is
	 * disabled.
	 * @param pLineFeed The line terminator, by default "\n"
	 * (Line Feed). You might prefer "\r\n" (Carriage Return,
	 * Line Feed), which is the default on Windows and related
	 * operating systems.
	 */
	void setLineFeed(String pLineFeed);

	/** Returns the line terminator. Ignored, if indentation is
	 * disabled.
	 * @return The line terminator, by default "\n"
	 * (Line Feed). You might prefer "\r\n" (Carriage Return,
	 * Line Feed), which is the default on Windows and related
	 * operating systems.
	 */
	String getLineFeed();

	/** Sets, whether the method {@link org.xml.sax.ContentHandler#endDocument}
	 * should do a flush on the target stream.
	 * @param pFlushing True, if a flush should be done. Defaults to
	 * false.
	 */
	void setFlushing(boolean pFlushing);

	/** Returns, whether the method {@link org.xml.sax.ContentHandler#endDocument}
	 * should do a flush on the target stream.
	 * @return True, if a flush should be done. Defaults to false.
	 */
	boolean isFlushing();
}
