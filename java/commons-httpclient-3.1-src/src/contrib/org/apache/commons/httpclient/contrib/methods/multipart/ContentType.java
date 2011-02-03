/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * &lt;http://www.apache.org/&gt;.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient.contrib.methods.multipart;

import java.io.File;

/**
 * This class provides mappings from file name extensions to content types.
 *
 * @author <a href="mailto:emdevlin@charter.net">Eric Devlin</a>
 * @author <a href="mailto:adrian@intencha.com">Adrian Sutton</a>
 * 
 * @version $Revision: 480424 $
 * 
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * to be used without additional customization.
 */

public final class ContentType {

    /** Mime Type mappings 'liberated' from Tomcat4.1.18/conf/web.xml*/
    public static final String[][] MIME_TYPE_MAPPINGS = { { "abs", "audio/x-mpeg" }, {
            "ai", "application/postscript" }, {
            "aif", "audio/x-aiff" }, {
            "aifc", "audio/x-aiff" }, {
            "aiff", "audio/x-aiff" }, {
            "aim", "application/x-aim" }, {
            "art", "image/x-jg" }, {
            "asf", "video/x-ms-asf" }, {
            "asx", "video/x-ms-asf" }, {
            "au", "audio/basic" }, {
            "avi", "video/x-msvideo" }, {
            "avx", "video/x-rad-screenplay" }, {
            "bcpio", "application/x-bcpio" }, {
            "bin", "application/octet-stream" }, {
            "bmp", "image/bmp" }, {
            "body", "text/html" }, {
            "cdf", "application/x-cdf" }, {
            "cer", "application/x-x509-ca-cert" }, {
            "class", "application/java" }, {
            "cpio", "application/x-cpio" }, {
            "csh", "application/x-csh" }, {
            "css", "text/css" }, {
            "dib", "image/bmp" }, {
            "doc", "application/msword" }, {
            "dtd", "text/plain" }, {
            "dv", "video/x-dv" }, {
            "dvi", "application/x-dvi" }, {
            "eps", "application/postscript" }, {
            "etx", "text/x-setext" }, {
            "exe", "application/octet-stream" }, {
            "gif", "image/gif" }, {
            "gtar", "application/x-gtar" }, {
            "gz", "application/x-gzip" }, {
            "hdf", "application/x-hdf" }, {
            "hqx", "application/mac-binhex40" }, {
            "htc", "text/x-component" }, {
            "htm", "text/html" }, {
            "html", "text/html" }, {
            "hqx", "application/mac-binhex40" }, {
            "ief", "image/ief" }, {
            "jad", "text/vnd.sun.j2me.app-descriptor" }, {
            "jar", "application/java-archive" }, {
            "java", "text/plain" }, {
            "jnlp", "application/x-java-jnlp-file" }, {
            "jpe", "image/jpeg" }, {
            "jpeg", "image/jpeg" }, {
            "jpg", "image/jpeg" }, {
            "js", "text/javascript" }, {
            "jsf", "text/plain" }, {
            "jspf", "text/plain" }, {
            "kar", "audio/x-midi" }, {
            "latex", "application/x-latex" }, {
            "m3u", "audio/x-mpegurl" }, {
            "mac", "image/x-macpaint" }, {
            "man", "application/x-troff-man" }, {
            "me", "application/x-troff-me" }, {
            "mid", "audio/x-midi" }, {
            "midi", "audio/x-midi" }, {
            "mif", "application/x-mif" }, {
            "mov", "video/quicktime" }, {
            "movie", "video/x-sgi-movie" }, {
            "mp1", "audio/x-mpeg" }, {
            "mp2", "audio/x-mpeg" }, {
            "mp3", "audio/x-mpeg" }, {
            "mpa", "audio/x-mpeg" }, {
            "mpe", "video/mpeg" }, {
            "mpeg", "video/mpeg" }, {
            "mpega", "audio/x-mpeg" }, {
            "mpg", "video/mpeg" }, {
            "mpv2", "video/mpeg2" }, {
            "ms", "application/x-wais-source" }, {
            "nc", "application/x-netcdf" }, {
            "oda", "application/oda" }, {
            "pbm", "image/x-portable-bitmap" }, {
            "pct", "image/pict" }, {
            "pdf", "application/pdf" }, {
            "pgm", "image/x-portable-graymap" }, {
            "pic", "image/pict" }, {
            "pict", "image/pict" }, {
            "pls", "audio/x-scpls" }, {
            "png", "image/png" }, {
            "pnm", "image/x-portable-anymap" }, {
            "pnt", "image/x-macpaint" }, {
            "ppm", "image/x-portable-pixmap" }, {
            "ps", "application/postscript" }, {
            "psd", "image/x-photoshop" }, {
            "qt", "video/quicktime" }, {
            "qti", "image/x-quicktime" }, {
            "qtif", "image/x-quicktime" }, {
            "ras", "image/x-cmu-raster" }, {
            "rgb", "image/x-rgb" }, {
            "rm", "application/vnd.rn-realmedia" }, {
            "roff", "application/x-troff" }, {
            "rtf", "application/rtf" }, {
            "rtx", "text/richtext" }, {
            "sh", "application/x-sh" }, {
            "shar", "application/x-shar" }, {
            "smf", "audio/x-midi" }, {
            "snd", "audio/basic" }, {
            "src", "application/x-wais-source" }, {
            "sv4cpio", "application/x-sv4cpio" }, {
            "sv4crc", "application/x-sv4crc" }, {
            "swf", "application/x-shockwave-flash" }, {
            "t", "application/x-troff" }, {
            "tar", "application/x-tar" }, {
            "tcl", "application/x-tcl" }, {
            "tex", "application/x-tex" }, {
            "texi", "application/x-texinfo" }, {
            "texinfo", "application/x-texinfo" }, {
            "tif", "image/tiff" }, {
            "tiff", "image/tiff" }, {
            "tr", "application/x-troff" }, {
            "tsv", "text/tab-separated-values" }, {
            "txt", "text/plain" }, {
            "ulw", "audio/basic" }, {
            "ustar", "application/x-ustar" }, {
            "xbm", "image/x-xbitmap" }, {
            "xml", "text/xml" }, {
            "xpm", "image/x-xpixmap" }, {
            "xsl", "text/xml" }, {
            "xwd", "image/x-xwindowdump" }, {
            "wav", "audio/x-wav" }, {
            "svg", "image/svg+xml" }, {
            "svgz", "image/svg+xml" }, {
            "wbmp", "image/vnd.wap.wbmp" }, {
            "wml", "text/vnd.wap.wml" }, {
            "wmlc", "application/vnd.wap.wmlc" }, {
            "wmls", "text/vnd.wap.wmlscript" }, {
            "wmlscriptc", "application/vnd.wap.wmlscriptc" }, {
            "wrl", "x-world/x-vrml" }, {
            "Z", "application/x-compress" }, {
            "z", "application/x-compress" }, {
            "zip", "application/zip" }
    };

    /** The constructor is intentionally private as the class only provides static methods.
     */
    private ContentType() {
    }

    /**
     * Get the content type based on the extension of the file name&lt;br&gt;
     *
     * @param fileName for which the content type is to be determined.
     *
     * @return the content type for the file or null if no mapping was
     * possible.
     */
    public static String get(String fileName) {
        String contentType = null;

        if (fileName != null) {
            int extensionIndex = fileName.lastIndexOf('.');
            if (extensionIndex != -1) {
                if (extensionIndex + 1 < fileName.length()) {
                    String extension = fileName.substring(extensionIndex + 1);
                    for (int i = 0; i < MIME_TYPE_MAPPINGS.length; i++) {
                        if (extension.equals(MIME_TYPE_MAPPINGS[i][0])) {
                            contentType = MIME_TYPE_MAPPINGS[i][1];
                            break;
                        }
                    }
                }
            }
        }

        return contentType;
    }

    /**
     * Get the content type based on the extension of the file name&lt;br&gt;
     *
     * @param file for which the content type is to be determined.
     *
     * @return the content type for the file or null if no mapping was
     * possible.
     *
     * @throws IOException if the construction of the canonical path for 
     * the file fails.
     */
    public static String get(File file) {
        String contentType = null;

        if (file != null) {
            contentType = get(file.getName());
        }

        return contentType;
    }
}
