/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.commons.logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A ClassLoader which sees only specified classes, and which can be
 * set to do parent-first or child-first path lookup.
 * <p>
 * Note that this classloader is not "industrial strength"; users
 * looking for such a class may wish to look at the Tomcat sourcecode
 * instead. In particular, this class may not be threadsafe.
 * <p>
 * Note that the ClassLoader.getResources method isn't overloaded here.
 * It would be nice to ensure that when child-first lookup is set the
 * resources from the child are returned earlier in the list than the
 * resources from the parent. However overriding this method isn't possible
 * as the java 1.4 version of ClassLoader declares this method final
 * (though the java 1.5 version has removed the final qualifier). As the
 * ClassLoader javadoc doesn't specify the order in which resources
 * are returned, it's valid to return the resources in any order (just
 * untidy) so the inherited implementation is technically ok.
 */

public class PathableClassLoader extends URLClassLoader {
    
    private static final URL[] NO_URLS = new URL[0];
    
    /**
     * A map of package-prefix to ClassLoader. Any class which is in
     * this map is looked up via the specified classloader instead of
     * the classpath associated with this classloader or its parents.
     * <p>
     * This is necessary in order for the rest of the world to communicate
     * with classes loaded via a custom classloader. As an example, junit
     * testcases which are loaded via a custom classloader needs to see
     * the same junit classes as the code invoking the testcase, otherwise
     * they can't pass result objects back. 
     * <p>
     * Normally, only a classloader created with a null parent needs to
     * have any lookasides defined.
     */
    private HashMap lookasides = null;

    /**
     * See setParentFirst.
     */
    private boolean parentFirst = true;

    /**
     * Constructor.
     * <p>
     * Often, null is passed as the parent, ie the parent of the new
     * instance is the bootloader. This ensures that the classpath is
     * totally clean; nothing but the standard java library will be
     * present.
     * <p>
     * When using a null parent classloader with a junit testcase, it *is*
     * necessary for the junit library to also be visible. In this case, it
     * is recommended that the following code be used:
     * <pre>
     * pathableLoader.useExplicitLoader(
     *   "junit.",
     *   junit.framework.Test.class.getClassLoader());
     * </pre>
     * Note that this works regardless of whether junit is on the system
     * classpath, or whether it has been loaded by some test framework that
     * creates its own classloader to run unit tests in (eg maven2's
     * Surefire plugin).
     */
    public PathableClassLoader(ClassLoader parent) {
        super(NO_URLS, parent);
    }
    
    /**
     * Allow caller to explicitly add paths. Generally this not a good idea;
     * use addLogicalLib instead, then define the location for that logical
     * library in the build.xml file.
     */
    public void addURL(URL url) {
        super.addURL(url);
    }

    /**
     * Specify whether this classloader should ask the parent classloader
     * to resolve a class first, before trying to resolve it via its own
     * classpath.
     * <p> 
     * Checking with the parent first is the normal approach for java, but
     * components within containers such as servlet engines can use 
     * child-first lookup instead, to allow the components to override libs
     * which are visible in shared classloaders provided by the container.
     * <p>
     * Note that the method getResources always behaves as if parentFirst=true,
     * because of limitations in java 1.4; see the javadoc for method
     * getResourcesInOrder for details.
     * <p>
     * This value defaults to true.
     */
    public void setParentFirst(boolean state) {
        parentFirst = state;
    }

    /**
     * For classes with the specified prefix, get them from the system
     * classpath <i>which is active at the point this method is called</i>.
     * <p>
     * This method is just a shortcut for
     * <pre>
     * useExplicitLoader(prefix, ClassLoader.getSystemClassLoader());
     * </pre>
     * <p>
     * Of course, this assumes that the classes of interest are already
     * in the classpath of the system classloader.
     */
    public void useSystemLoader(String prefix) {
        useExplicitLoader(prefix, ClassLoader.getSystemClassLoader());
        
    }

    /**
     * Specify a classloader to use for specific java packages.
     * <p>
     * The specified classloader is normally a loader that is NOT
     * an ancestor of this classloader. In particular, this loader
     * may have the bootloader as its parent, but be configured to 
     * see specific other classes (eg the junit library loaded
     * via the system classloader).
     * <p>
     * The differences between using this method, and using
     * addLogicalLib are:
     * <ul>
     * <li>If code calls getClassLoader on a class loaded via
     * "lookaside", then traces up its inheritance chain, it
     * will see the "real" classloaders. When the class is remapped
     * into this classloader via addLogicalLib, the classloader
     * chain seen is this object plus ancestors.
     * <li>If two different jars contain classes in the same
     * package, then it is not possible to load both jars into
     * the same "lookaside" classloader (eg the system classloader)
     * then map one of those subsets from here. Of course they could
     * be loaded into two different "lookaside" classloaders and
     * then a prefix used to map from here to one of those classloaders.
     * </ul>
     */
    public void useExplicitLoader(String prefix, ClassLoader loader) {
        if (lookasides == null) {
            lookasides = new HashMap();
        }
        lookasides.put(prefix, loader);
    }

    /**
     * Specify a collection of logical libraries. See addLogicalLib.
     */
    public void addLogicalLib(String[] logicalLibs) {
        for(int i=0; i<logicalLibs.length; ++i) {
            addLogicalLib(logicalLibs[i]);
        }
    }

    /**
     * Specify a logical library to be included in the classpath used to
     * locate classes. 
     * <p>
     * The specified lib name is used as a key into the system properties;
     * there is expected to be a system property defined with that name
     * whose value is a url that indicates where that logical library can
     * be found. Typically this is the name of a jar file, or a directory
     * containing class files.
     * <p>
     * If there is no system property, but the classloader that loaded
     * this class is a URLClassLoader then the set of URLs that the
     * classloader uses for its classpath is scanned; any jar in the
     * URL set whose name starts with the specified string is added to
     * the classpath managed by this instance. 
     * <p>
     * Using logical library names allows the calling code to specify its
     * desired classpath without knowing the exact location of the necessary
     * classes. 
     */
    public void addLogicalLib(String logicalLib) {
        // first, check the system properties
        String filename = System.getProperty(logicalLib);
        if (filename != null) {
            try {
                URL libUrl = new File(filename).toURL();
                addURL(libUrl);
                return;
            } catch(java.net.MalformedURLException e) {
                throw new UnknownError(
                    "Invalid file [" + filename + "] for logical lib [" + logicalLib + "]");
            }
        }

        // now check the classpath for a similar-named lib
        URL libUrl = libFromClasspath(logicalLib);
        if (libUrl != null) {
            addURL(libUrl);
            return;
        }

        // lib not found
        throw new UnknownError(
            "Logical lib [" + logicalLib + "] is not defined"
            + " as a System property.");
    }

    /**
     * If the classloader that loaded this class has this logical lib in its
     * path, then return the matching URL otherwise return null.
     * <p>
     * This only works when the classloader loading this class is an instance
     * of URLClassLoader and thus has a getURLs method that returns the classpath
     * it uses when loading classes. However in practice, the vast majority of the
     * time this type is the classloader used.
     * <p>
     * The classpath of the classloader for this instance is scanned, and any
     * jarfile in the path whose name starts with the logicalLib string is
     * considered a match. For example, passing "foo" will match a url
     * of <code>file:///some/where/foo-2.7.jar</code>.
     * <p>
     * When multiple classpath entries match the specified logicalLib string,
     * the one with the shortest filename component is returned. This means that
     * if "foo-1.1.jar" and "foobar-1.1.jar" are in the path, then a logicalLib
     * name of "foo" will match the first entry above.
     */
    private URL libFromClasspath(String logicalLib) {
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof URLClassLoader == false) {
            return null;
        }
        
        URLClassLoader ucl = (URLClassLoader) cl;
        URL[] path = ucl.getURLs();
        URL shortestMatch = null;
        int shortestMatchLen = Integer.MAX_VALUE;
        for(int i=0; i<path.length; ++i) {
            URL u = path[i];
            
            // extract the filename bit on the end of the url
            String filename = u.toString();
            if (!filename.endsWith(".jar")) {
                // not a jarfile, ignore it
                continue;
            }

            int lastSlash = filename.lastIndexOf('/');
            if (lastSlash >= 0) {
                filename = filename.substring(lastSlash+1);
            }
            
            if (filename.startsWith(logicalLib)) {
                // ok, this is a candidate
                if (filename.length() < shortestMatchLen) {
                    shortestMatch = u;
                    shortestMatchLen = filename.length();
                }
            }
        }
        
        return shortestMatch;
    }

    /**
     * Override ClassLoader method.
     * <p>
     * For each explicitly mapped package prefix, if the name matches the 
     * prefix associated with that entry then attempt to load the class via 
     * that entries' classloader.
     */
    protected Class loadClass(String name, boolean resolve) 
    throws ClassNotFoundException {
        // just for performance, check java and javax
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name, resolve);
        }

        if (lookasides != null) {
            for(Iterator i = lookasides.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry) i.next();
                String prefix = (String) entry.getKey();
                if (name.startsWith(prefix) == true) {
                    ClassLoader loader = (ClassLoader) entry.getValue();
                    Class clazz = Class.forName(name, resolve, loader);
                    return clazz;
                }
            }
        }
        
        if (parentFirst) {
            return super.loadClass(name, resolve);
        } else {
            // Implement child-first. 
            //
            // It appears that the findClass method doesn't check whether the
            // class has already been loaded. This seems odd to me, but without
            // first checking via findLoadedClass we can get java.lang.LinkageError
            // with message "duplicate class definition" which isn't good.
            
            try {
                Class clazz = findLoadedClass(name);
                if (clazz == null) {
                    clazz = super.findClass(name);
                }
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } catch(ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }
    }
    
    /**
     * Same as parent class method except that when parentFirst is false
     * the resource is looked for in the local classpath before the parent
     * loader is consulted.
     */
    public URL getResource(String name) {
        if (parentFirst) {
            return super.getResource(name);
        } else {
            URL local = super.findResource(name);
            if (local != null) {
                return local;
            }
            return super.getResource(name);
        }
    }
    
    /**
     * Emulate a proper implementation of getResources which respects the
     * setting for parentFirst.
     * <p>
     * Note that it's not possible to override the inherited getResources, as
     * it's declared final in java1.4 (thought that's been removed for 1.5).
     * The inherited implementation always behaves as if parentFirst=true.
     */
    public Enumeration getResourcesInOrder(String name) throws IOException {
        if (parentFirst) {
            return super.getResources(name);
        } else {
            Enumeration localUrls = super.findResources(name);
            
            ClassLoader parent = getParent();
            if (parent == null) {
                // Alas, there is no method to get matching resources
                // from a null (BOOT) parent classloader. Calling
                // ClassLoader.getSystemClassLoader isn't right. Maybe
                // calling Class.class.getResources(name) would do?
                //
                // However for the purposes of unit tests, we can
                // simply assume that no relevant resources are
                // loadable from the parent; unit tests will never be
                // putting any of their resources in a "boot" classloader
                // path!
                return localUrls;
            }
            Enumeration parentUrls = parent.getResources(name);

            ArrayList localItems = toList(localUrls);
            ArrayList parentItems = toList(parentUrls);
            localItems.addAll(parentItems);
            return Collections.enumeration(localItems);
        }
    }
    
    /**
     * 
     * Clean implementation of list function of 
     * {@link java.utils.Collection} added in JDK 1.4 
     * @param en <code>Enumeration</code>, possibly null
     * @return <code>ArrayList</code> containing the enumerated
     * elements in the enumerated order, not null
     */
    private ArrayList toList(Enumeration en) {
        ArrayList results = new ArrayList();
        if (en != null) {
            while (en.hasMoreElements()){
                Object element = en.nextElement();
                results.add(element);
            }
        }
        return results;
    }
    
    /**
     * Same as parent class method except that when parentFirst is false
     * the resource is looked for in the local classpath before the parent
     * loader is consulted.
     */
    public InputStream getResourceAsStream(String name) {
        if (parentFirst) {
            return super.getResourceAsStream(name);
        } else {
            URL local = super.findResource(name);
            if (local != null) {
                try {
                    return local.openStream();
                } catch(IOException e) {
                    // TODO: check if this is right or whether we should
                    // fall back to trying parent. The javadoc doesn't say...
                    return null;
                }
            }
            return super.getResourceAsStream(name);
        }
    }
}
