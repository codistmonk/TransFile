/*
 * Copyright © 2010 Martin Riedel
 * 
 * This file is part of TransFile.
 *
 * TransFile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TransFile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TransFile.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sourceforge.transfile.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.sourceforge.transfile.settings.exceptions.IllegalConfigValueException;

/**
 * Provides simple key-value pair persistence. Default values are in transfile.settings.defaults.properties,
 * per-user configuration files are saved to USER_HOME/.transfile/settings.properties.
 * 
 * Singelton class.
 * 
 * @author Martin Riedel
 *
 */
public class Settings extends Properties {

	private static final long serialVersionUID = 312178159322230641L;

	/*
	 * Singleton instance.
	 * Deliberately NOT using the Initialization on Demand Holder idiom to avoid 
	 * concurrency issues and because it wouldn't produce any advantage for this class.
	 */
	private static final Settings _instance = makeInstance();
	
	/*
	 * Configuration file directory
	 */
	private final File cfgDir;
	
	/*
	 * Configuration file
	 */
	private final File cfgFile;
	
	/*
	 * True if the user home directory couldn't be determined, making it impossible to load or save
	 * user-specific settings
	 */
	private final boolean persistent; 

	/**
	 * Returns the Singleton instance. 
	 * 
	 * @return the Singleton instance of Settings
	 */
	public static Settings getInstance() {
		return _instance;
	}
	
	/**
	 * Returns the user-specific configuration directory (i.e. USER_HOME/.transfile/)
	 * 
	 * @return the user-specific configuration directory
	 */
	public File getCfgDir() {
		return cfgDir;
	}
	
	/**
	 * Saves the current key-value pairs to disk
	 * 
	 */
	public void save() {
		if(!persistent)
			return;
		
		try {
			store(new FileOutputStream(cfgFile), "TransFile per-user configuration file");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns the value for the specified property key as an integer
	 * 
	 * @param key the property key to look up
	 * @return the value for the specified property key as an integer, or null if the key could not be found
	 * @throws IllegalConfigValueException if the value is not an integer
	 */
	public int getInt(final String key) {
		try {
			return Integer.parseInt(getProperty(key));
		} catch(NumberFormatException e) {
			throw new IllegalConfigValueException(e);
		}
	}
	
	/**
	 * Returns the default setting for the provided property key.
	 * 
	 * @param key the property key to look up
	 * @return the default for the specified property key, or null if the key could no be found
	 */
	public String getDefault(final String key) {
		return defaults.getProperty(key);
	}

	/**
	 * Returns the default setting for the specified property key as an integer
	 * 
	 * @param key the property key to look up
	 * @return the default for the specified property key as an integer, or null if the key could not be found
	 * @throws IllegalConfigValueException if the value is not an integer
	 */
	public int getDefaultInt(final String key) {
		try {
			return Integer.parseInt(defaults.getProperty(key));
		} catch(NumberFormatException e) {
			throw new IllegalConfigValueException(e);
		}
	}
	
	/**
	 * Constructs a Settings instance. Internal use only.
	 * 
	 * @param defaults Properties object containing default values
	 */
	private Settings(final Properties defaults) {
		super(defaults);
		
		//TODO handle this differently?
		if(System.getProperty("user.home") == null) {
			persistent = false;
			throw new IllegalStateException("user.home == null");
		} else {
			persistent = true;
		}
		
		cfgDir = new File(System.getProperty("user.home"), ".transfile");
		cfgFile = new File(cfgDir, "settings.properties");
		
		if(!cfgDir.isDirectory())
			cfgDir.mkdir();
		
		load();
	}
	
	/**
	 * Factory method for Settings
	 * 
	 * @return a Settings instance
	 */
	private static Settings makeInstance() {
		Properties defaults = new Properties();
		
		try {
			defaults.load(Settings.class.getResourceAsStream("defaults.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new Settings(defaults);
	}
	
	/**
	 * Loads settings from disk
	 * 
	 */
	private void load() {
		if(!persistent)
			return;
		
		if(cfgDir.isDirectory() && cfgFile.isFile()) {
			try {
				super.load(new FileInputStream(cfgFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

}
