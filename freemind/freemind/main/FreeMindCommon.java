/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2006  Joerg Mueller, Daniel Polansky, Christian Foltin and others.
 *
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Created on 10.01.2006
 */
/*$Id: FreeMindCommon.java,v 1.1.2.2.2.19 2007-01-03 22:05:27 christianfoltin Exp $*/
package freemind.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;


/**
 * @author foltin
 * 
 */
public class FreeMindCommon {

	public static final String FREEMIND_FILE_EXTENSION_WITHOUT_DOT = "mm";

	public static final String FREEMIND_FILE_EXTENSION = "."+FREEMIND_FILE_EXTENSION_WITHOUT_DOT;

	public static final String POSTFIX_TRANSLATE_ME = "[translate me]";

    private static PropertyResourceBundle languageResources;

	private PropertyResourceBundle defaultResources;

	public static final String RESOURCE_LANGUAGE = "language";

    public static final String RESOURCE_ANTIALIAS = "antialias";

    public static final String DEFAULT_LANGUAGE = "en";

    public static final String LOCAL_PROPERTIES = "LocalProperties.";

	private final FreeMindMain mFreeMindMain;

    private String baseDir;

    /**
     * Holds the last opened map.
     */
    public static final String ON_START_IF_NOT_SPECIFIED = "onStartIfNotSpecified";
    public static final String LOAD_LAST_MAP = "loadLastMap";

	private static Logger logger = null;

	/**
	 * 
	 */
	public FreeMindCommon(FreeMindMain main) {
		super();
		// TODO Auto-generated constructor stub
		this.mFreeMindMain = main;
		if (logger == null)
			logger = main.getLogger(this.getClass().getName());
	}

	public String getProperty(String key) {
		return mFreeMindMain.getProperty(key);
	}

	/** Returns the ResourceBundle with the current language */
    public ResourceBundle getResources() {
		if (languageResources == null) {
			try {
				String lang = getProperty(RESOURCE_LANGUAGE);
				if (lang == null || lang.equals("automatic")) {
					lang = Locale.getDefault().getLanguage() + "_"
							+ Locale.getDefault().getCountry();
					if (getLanguageResources(lang) == null) {
						lang = Locale.getDefault().getLanguage();
						if (getLanguageResources(lang) == null) {
							// default is english.
							lang = DEFAULT_LANGUAGE;
						}
					}
				}
				languageResources = getLanguageResources(lang);
				defaultResources = getLanguageResources(DEFAULT_LANGUAGE);
			} catch (Exception ex) {
			    freemind.main.Resources.getInstance().logException(				ex);
				logger.severe("Error loading Resources");
				return null;
			}
		}
		return languageResources;
	}

	/**
	 * @throws IOException
	 */
	private PropertyResourceBundle getLanguageResources(String lang)
			throws IOException {
		URL systemResource = mFreeMindMain.getResource("Resources_" + lang
				+ ".properties");
		if (systemResource == null) {
			return null;
		}
		InputStream in = systemResource.openStream();
		if (in == null) {
			return null;
		}
		PropertyResourceBundle bundle = new PropertyResourceBundle(in);
		in.close();
		return bundle;
	}

    public String getResourceString(String key) {
        try {
            return getResources().getString(key);
        } catch (Exception ex) {
            logger.severe("Warning - resource string not found:" + key);
            try {
                return defaultResources.getString(key) + POSTFIX_TRANSLATE_ME;
            } catch (Exception e) {
                freemind.main.Resources.getInstance().logException(e);
                logger
                        .severe("Warning - default resource string not found (even in english):"
                                + key);
                return key;
            }
        }
    }

    public String getResourceString(String key, String resource) {
        try {
            return getResources().getString(key);
        } catch (Exception ex) {
            logger.severe("Warning - resource string not found:" + key);
            try {
                logger.severe("Warning - default resource string not found:" + key);
                return defaultResources.getString(key) + POSTFIX_TRANSLATE_ME;
            } catch (Exception e) {
                return resource;
            }
        }
    }

    public void clearLanguageResources() {
		languageResources = null;
	}
    
	public ClassLoader getFreeMindClassLoader() {
		ClassLoader classLoader = this.getClass().getClassLoader();
		try {
			return new URLClassLoader(new URL[] { new File(getFreemindBaseDir()).toURL() }, classLoader);
		} catch (MalformedURLException e) {
			freemind.main.Resources.getInstance().logException(e);
			return classLoader;
		}
	}

	/**
     * Old version using String manipulation out of the classpath to find the base dir.
	 */
	public String getFreemindBaseDirOld() {
        if(baseDir == null){
            final String classPath = System.getProperty("java.class.path");
            final String mainJarFile = "freemind.jar";
            int lastpos = classPath.indexOf(mainJarFile);
            int firstpos = 0;
            // if freemind.jar is not found in the class path use user.dir as Basedir
            if(lastpos == -1){
                baseDir = System.getProperty("user.dir");
                logger.info("Basedir is user.dir: "+baseDir);
                return baseDir;
            }
            /* fc: Now, if freemind.jar is the first, firstpos == -1. 
             * This results in bad results in the substring method, or not??*/
            firstpos = classPath.lastIndexOf(File.pathSeparator, lastpos) + 1;
            lastpos -= 1;
            if (lastpos > firstpos) {
            	logger.info("First " + firstpos +  " and last " + lastpos + " and string " + classPath);
                baseDir = classPath.substring(firstpos, lastpos);
            }
            else 
                baseDir = "";
            final File basePath = new File(baseDir);
            baseDir = basePath.getAbsolutePath();
            logger.info("First basedir is: "+baseDir);
            /* I suppose, that here, the freemind.jar is removed together with the last path. 
             * Example: /home/foltin/freemindapp/lib/freemind.jar gives 
             * /home/foltin/freemindapp */
            lastpos = baseDir.lastIndexOf(File.separator);
            if (lastpos > -1) 
                baseDir = baseDir.substring(0, lastpos);
            logger.info("Basedir is: "+baseDir);
        }
        return baseDir;
	}
    /* We define the base dir of FreeMind either as the directory where the
     * main jar file is (freemind.jar), or the root of the class hierarchy
     * (if no jar file is used).
     * One can overwrite this definition by setting the freemind.base.dir
     * property.
     */
    public String getFreemindBaseDir() {
        if (baseDir == null) {
            try {
                File file;
                String dir = System.getProperty("freemind.base.dir");
                if (dir == null) {
                    // Property isn't set, we try to find the
                    // base directory ourselves.
                    // System.err.println("property not set");
                    // We locate first the current class.
                    String classname = this.getClass().getName();
                    URL url = this.getClass().getResource(
                            classname.replaceFirst("^"
                                    + this.getClass().getPackage().getName()
                                    + ".", "")
                                    + ".class");
                    // then we create a file out of it, after
                    // removing file: and jar:, removing everything
                    // after !, as well as the class name part.
                    // Finally we decode everything (e.g. %20)
                    // TODO: is UTF-8 always the right value?
                    file = new File(URLDecoder.decode(
                            url.getPath().replaceFirst("^(file:|jar:)+", "")
                                    .replaceFirst("!.*$", "").replaceFirst(
                                            classname.replace('.', '/')
                                                    + ".class$", ""), "UTF-8"));
                    // if it's a file, we take its parent, a dir
                    if (file.isFile()) {
                        file = file.getParentFile();
                    }
                } else {
                    file = new File(dir);
                }
                // then we check if the directory exists and is really
                // a directory.
                if (!file.exists()) {
                    throw new IllegalArgumentException("FreeMind base dir '"
                            + file + "' does not exist.");
                }
                if (!file.isDirectory()) {
                    throw new IllegalArgumentException(
                            "FreeMind base dir (!) '" + file
                                    + "' is not a directory.");
                }
                // set the static variable
                baseDir = file.getCanonicalPath();
                /* Now, we remove the lib directory:
                 * Example: /home/foltin/freemindapp/lib/freemind.jar gives 
                 * /home/foltin/freemindapp */
                int lastpos = baseDir.lastIndexOf(File.separator);
                if (lastpos > -1) 
                    baseDir = baseDir.substring(0, lastpos);
                logger.info("Basedir is: "+baseDir);
            } catch (Exception e) {
                Resources.getInstance().logException(e);
                throw new IllegalArgumentException(
                        "FreeMind base dir can't be determined.");
            }            
        }
        // return the value of the static variable
        return baseDir;
    }

    

    public String getAdjustableProperty(final String label) {
        String value = getProperty(label);
        if(value == null){
            return value;
        }
        if(value.startsWith("?")){
            // try to look in the language specific properties
            try{
                value = getResources().getString(LOCAL_PROPERTIES + label);
            }
            // remove leading question mark if not succesful
            catch(MissingResourceException ex){
                value = value.substring(1).trim();
            }
        }
        return value;
    }


    
}
