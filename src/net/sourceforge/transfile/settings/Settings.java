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
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.sourceforge.transfile.settings.exceptions.IllegalConfigValueException;

/**
 * Provides simple key-value pair persistence. Default values are in transfile.settings.defaults.properties,
 * per-user configuration files are saved to user preferences (Java mechanism).
 * 
 * <br>Singleton class.
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
		return this.cfgDir;
	}
	
	/**
	 * Saves the current key-value pairs to user preferences.
	 * 
	 */
	public final void save() {
		if(!this.persistent) {
			return;
		}
		
		final Preferences userPreferences = Preferences.userNodeForPackage(Settings.class);
		
		for (final Object key : this.keySet()) {
			userPreferences.put(key.toString(), this.get(key).toString());
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
		return this.defaults.getProperty(key);
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
			return Integer.parseInt(this.defaults.getProperty(key));
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
		
		this.persistent = getPreferences() != null;
		
		this.cfgDir = new File(System.getProperty("user.home"), ".transfile");
		
		if(!this.cfgDir.isDirectory()) {
			this.cfgDir.mkdir();
		}
		
		load();
	}
	
	/**
	 * Returns the user preferences for this package.
	 * 
	 * @return {@code null} if the user preferences for this package could not be created or retrieved
	 * <br>A possibly null value
	 * <br>A possibly new value
	 * @see Preferences#userNodeForPackage(Class)
	 */
	private static final Preferences getPreferences() {
		try {
			return Preferences.userNodeForPackage(Settings.class);
		} catch (final Exception exception) {
			exception.printStackTrace();
			
			return null;
		}
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
		} catch (final IOException exception) {
			exception.printStackTrace();
		}
		
		return new Settings(defaults);
	}
	
	/**
	 * Loads settings from user preferences
	 * 
	 */
	private final void load() {
		if (!this.persistent) {
			return;
		}
		
		final Preferences userPreferences = Preferences.userNodeForPackage(Settings.class);

		try {
			for (final String key : userPreferences.keys()) {
				this.put(key, userPreferences.get(key, this.getDefault(key)));
			}
		} catch (final BackingStoreException exception) {
			throw new RuntimeException(exception);
		}
	}

}
