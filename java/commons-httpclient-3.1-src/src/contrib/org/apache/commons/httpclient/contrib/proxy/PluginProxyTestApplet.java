/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/contrib/org/apache/commons/httpclient/contrib/proxy/PluginProxyTestApplet.java $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 *
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
 */
package org.apache.commons.httpclient.contrib.proxy;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.ProxyHost;

/**
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * for use without additional customization.
 * </p>
 */

public class PluginProxyTestApplet extends JApplet {

    
    private JTextField urlTextField = new JTextField();
    private JPanel grid = null;
    private JLabel hostLabel = null;
    private JLabel portLabel = null;
    
    
    public PluginProxyTestApplet() {
        
    }
    
    public void init() {
        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        // Proxy info table
        grid = getPanel(new GridLayout(2,3,2,2));
        grid.add(getHeaderLabel("URL"));
        grid.add(getHeaderLabel("Proxy Host"));
        grid.add(getHeaderLabel("Proxy Port"));
        grid.add(urlTextField);
        hostLabel = getLabel("");
        portLabel = getLabel("");
        grid.add(hostLabel);
        grid.add(portLabel);        
        grid.validate();
        content.add(grid, BorderLayout.CENTER);
        
        // Button panel - SOUTH
        JPanel buttonPanel = getPanel(new FlowLayout());
        JButton button = new JButton("Detect Proxy");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        detectProxy();
                    }
                });
            }
        });
        buttonPanel.add(button);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        // version panel - NORTH
        JPanel versionPanel = getPanel(new FlowLayout());
        String javaVersion = System.getProperty("java.runtime.version");
        JLabel versionLabel = getLabel("Java Version: "+javaVersion);
        versionPanel.add(versionLabel);
        content.add(versionPanel, BorderLayout.NORTH);
        validate();
        
        super.setSize(400,100);
    }
    
    private JPanel getPanel(LayoutManager layout) {
        JPanel result = new JPanel(layout);
        return result;
    }
    
    private JLabel getHeaderLabel(String text) {
        JLabel result = new JLabel("<html><u><b>" + text + "</b></u></html>");
        result.setHorizontalAlignment(JLabel.CENTER);
        return result;
    }
    
    private JLabel getLabel(String text) {
        JLabel result = new JLabel(text);
        result.setHorizontalAlignment(JLabel.CENTER);
        return result;
    }
    
    private void detectProxy() {
        String urlString = urlTextField.getText();
        if (urlString == null || "".equals(urlString)) {
            JOptionPane.showMessageDialog(super.getRootPane(),
                                          "URL can't be empty", 
                                          "Missing URL", 
                                          JOptionPane.ERROR_MESSAGE); 
            return;
        }
        if (!urlString.startsWith("http://")) {
            urlString = "http://" + urlString;
        }
        try {
            URL url = new URL(urlString);
            ProxyHost hostInfo = PluginProxyUtil.detectProxy(url);
            if (hostInfo != null) {
                hostLabel.setText(hostInfo.getHostName());
                portLabel.setText(""+hostInfo.getPort());
            } else {
                hostLabel.setText("none");
                portLabel.setText("none");
            }
            grid.validate();
        } catch (ProxyDetectionException e) { 
            JOptionPane.showMessageDialog(getRootPane(), 
                                          e.getMessage() ,
                                          "Proxy Detection Failed", 
                                          JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getRootPane(), 
                    e.getMessage() ,
                    "Unexpected Exception", 
                    JOptionPane.ERROR_MESSAGE);            
            e.printStackTrace();
        }        
    }
    
    public String getProxyHost(String urlString) {
        String result = urlString;
        try {
            URL url = new URL(urlString);
            ProxyHost hostInfo = PluginProxyUtil.detectProxy(url);
            if (hostInfo != null) {
                result = hostInfo.getHostName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getProxyPort(String urlString) {
        int result = 80;
        try {
            URL url = new URL(urlString);
            ProxyHost hostInfo = PluginProxyUtil.detectProxy(url);
            if (hostInfo != null) {
                result = hostInfo.getPort();
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
