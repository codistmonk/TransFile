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

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Provides simple key-value pair persistence. Default values are in transfile.settings.defaults.properties,
 * per-user configuration files are saved to user preferences (Java mechanism).
 * 
 * <br>Singleton class.
 * 
 * @author Martin Riedel
 * @author codistmonk (modifications since 2010-05-10)
 *
 */
public class Settings {
	
	/**
	 * Private constructor to prevent this class from being instantiated.
	 */
	private Settings() {
		// Do nothing
	}
	
	private static final long serialVersionUID = 312178159322230641L;
	
	private static Properties defaults;
	
	/**
	 * Loads the default preferences values from the resource "defaults.properties".
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 * <br>A possibly new value
	 */
	private static final synchronized Properties getDefaults() {
		if (defaults == null) {
			defaults = new Properties();
			
			try {
				// TODO check if this works with WebStart
				// When run by WebStart, class.getResourceAsStream() uses the default class loader
				// in a way that may prevent it from finding the resources in the jar
				// As it is now, class.getClassLoader().getResourceAsStream() (the correct way, I think)
				// cannot be used because getDefaults() is called during other classes static initialization while
				// this class  hasn't been loaded yet
				// If it turns out there is no problem, then remove all these comments
				// Otherwise, a possible fix could be to use C.class.getClassLoader().getResourceAsStream() where C
				// is a class that doesn't depend directly or indirectly on this class.
				defaults.load(Settings.class.getResourceAsStream("defaults.properties"));
			} catch (final IOException exception) {
				exception.printStackTrace();
			}
			
			for (final java.util.Map.Entry<Object, Object> entry : defaults.entrySet()) {
				if (getPreferences().get(entry.getKey().toString(), null) == null) {
					getPreferences().put(entry.getKey().toString(), entry.getValue().toString());
				}
			}
		}
		
		return defaults;
	}
	
	/**
	 * Returns the user preferences for this package.
	 * <br>The preferences are initialized with default values if needed. 
	 * 
	 * @return {@code null} if the user preferences for this package could not be created or retrieved
	 * <br>A non-null value
	 * <br>A possibly new value
	 * @see Preferences#userNodeForPackage(Class)
	 * @throws SecurityException if the preferences cannot be loaded
	 */
	public static final Preferences getPreferences() {
		getDefaults();
		
		return Preferences.userNodeForPackage(Settings.class);
	}
	
	/**
	 * Returns the default setting for the provided property key.
	 * 
	 * @param key the property key to look up
	 * @return the default for the specified property key, or null if the key could no be found
	 */
	public static final String getDefault(final String key) {
		return getDefaults().getProperty(key);
	}

	/**
	 * Returns the default setting for the specified property key as an integer
	 * 
	 * @param key the property key to look up
	 * @return the default for the specified property key as an integer, or null if the key could not be found
	 */
	public static final int getDefaultInt(final String key) {
		final Object value = getDefaults().get(key);
		
		return value == null ? null : Integer.valueOf(value.toString());
	}
	
}
