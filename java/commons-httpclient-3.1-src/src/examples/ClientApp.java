/*
 * $Header$
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
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
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * A simple Swing application that demonstrates how to use the Jakarta
 * HttpClient API.  This application loads HTML from servers and displays the
 * content as text and as rendered HTML.
 *
 * @author Sean C. Sullivan
 * @author Ortwin Glück
 * @author Michael Becke
 */
public class ClientApp {

    public static void main(String[] args) {
        HttpClientFrame f = new HttpClientFrame();
        f.setTitle("HttpClient demo application");
        f.setSize(700, 500);
        f.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            }
        );
        f.setVisible(true);
    }

    public static class HttpClientFrame extends JFrame {

        private JComboBox cmbURL;     
        private JTextArea taTextResponse;
        private JEditorPane htmlPane;
        
        private HttpClient client;
        
        public HttpClientFrame() {            
            client = new HttpClient(new MultiThreadedHttpConnectionManager());
            client.getHttpConnectionManager().
                getParams().setConnectionTimeout(30000);

            JPanel panInput = new JPanel(new FlowLayout());

            String[] aURLs = {
                "http://www.apache.org/",
                "http://www.google.com/",
                "http://www.opensource.org/",
                "http://www.anybrowser.org/",
                "http://jakarta.apache.org/",
                "http://www.w3.org/"
            };

            final JButton btnGET = new JButton("GET");
            btnGET.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String url = (String) cmbURL.getSelectedItem();
                        if (url != null && url.length() > 0) {
                            loadPage(url);
                        }
                    }
                }
            );
            
            cmbURL = new JComboBox(aURLs);
            cmbURL.setToolTipText("Enter a URL");
            cmbURL.setEditable(true);
            cmbURL.setSelectedIndex(0);

            JLabel lblURL = new JLabel("URL:");

            panInput.add(lblURL);
            panInput.add(cmbURL);
            panInput.add(btnGET);

            taTextResponse = new JTextArea();
            taTextResponse.setEditable(false);
            taTextResponse.setCaretPosition(0);

            htmlPane = new JEditorPane();
            htmlPane.setContentType("text/html");
            htmlPane.setEditable(false);

            JSplitPane splitResponsePane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(taTextResponse),
                new JScrollPane(htmlPane)
            );
            splitResponsePane.setOneTouchExpandable(false);
            splitResponsePane.setDividerLocation(350);
            // it would be better to set resizeWeight, but this method does
            // not exist in JRE 1.2.2
//            splitResponsePane.setResizeWeight(0.5);

            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add(panInput, BorderLayout.NORTH);
            this.getContentPane().add(splitResponsePane, BorderLayout.CENTER);
        }

        /**
         * Sets the HTML content to be displayed.
         * 
         * @param content an HTML document
         */
        private void setDocumentContent(String content) {
        
            HTMLDocument doc = new HTMLDocument();
            try {
                doc.remove(0, doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        
            try {
                htmlPane.read(new ByteArrayInputStream(content.getBytes()), doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        
            htmlPane.setDocument(doc);
            htmlPane.setCaretPosition(0);

            taTextResponse.setText(content);
            taTextResponse.setCaretPosition(0);
            taTextResponse.requestFocus();
        }
        
        /**
         * Loads the page at the given URL from a separate thread.
         * @param url
         */
        private void loadPage(final String url) {
            // create a new thread to load the URL from
            new Thread() {
                public void run() {
                    GetMethod get = new GetMethod(url);
                    get.setFollowRedirects(true);
                    
                    try {
                        int iGetResultCode = client.executeMethod(get);
                        final String strGetResponseBody = get.getResponseBodyAsString();

                        if (strGetResponseBody != null) {
                            // set the HTML on the UI thread
                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        setDocumentContent(strGetResponseBody);
                                    }
                                }
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        get.releaseConnection();
                    }
                }
            }.start();
        }

    }
        
}
