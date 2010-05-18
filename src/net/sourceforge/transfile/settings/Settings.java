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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import net.sourceforge.transfile.settings.exceptions.ConstantReflectionException;
import net.sourceforge.transfile.tools.Tools;

/**
 * <p>Provides simple key-value pair persistence through the Java Preferences mechanism ({@link #getPreferences}). 
 * Among others, the default values for these preferences constants (both final and static field) in this class.</p>
 * 
 * <p>Also includes project-wide constants, a lot of which can effectively be overwritten by user preferences
 * and are thus considered defaults.</p>
 *  
 * <p>A list of the names of all constants defined by this class as well their respective values by name
 * are accessible through the reflective methods {@link #getConstantFieldNames} and {@link #getConstantAsString}.
 * However, these methods should only be used when absolutely necessary, i.e. to dynamically list all of them.</p>
 * 
 * @author Martin Riedel
 * @author codistmonk (modifications since 2010-05-10)
 *
 */
public final class Settings {
	
	private static final long serialVersionUID = 312178159322230641L;
	
	/*
	 * README: 
	 * 
	 * The public constants below this line are accessed reflectively by PreferencesFrame. Do not rename them
	 * unless you know what you're doing.
	 * 
	 * Do not add any additional public constants to this class (neither below nor above this line) without
	 * a) adding an exception for them in getConstantFieldNames if they are not supposed to be user-configurable or
	 * b) ensuring that their name is congruent with the respective settings in the user Preferences (except for case)
	 * 
	 */
	
	/*
	 * Default local port
	 */
	public static final int LOCAL_PORT = 42000;
	/*
	 * Local port range extrema
	 */
	public static final int LOCAL_PORT_MIN = 1024;
	public static final int LOCAL_PORT_MAX = 65535;
	
	/*
	 * URL to web service used to determine the local Internet IP address
	 */
	public static final String EXTERNAL_IP_SITE = "http://www.whatismyip.org/";
	
	/*
	 * The length of the time intervals between checks for thread interruption
	 * in threads establishing TransFile connections
	 */
	public static final int CONNECT_INTERVAL_TIME = 100;
	/*
	 * The number of such intervals to go through before the connection attempt is considered timed out
	 */
	public static final int CONNECT_INTERVALS = 100;
	
	/*
	 * The number of recent PeerURLs the PeerURLBar remembers
	 */
	public static final int PEERURLBAR_MAX_RETAINED_ITEMS = 5;
	
	/*
	 * The minimum log level to log
	 */
	public static final Level LOG_LEVEL = Level.FINEST;
	
	/*
	 * The name of the application's per-user directory
	 */
	public static final File USER_APPLICATION_SUBDIRECTORY = new File(".transfile");
	
	/*
	 * The default log directory and file
	 */
	public static final File LOG_PATH = new File(Tools.getUserApplicationDirectory(), "log.txt");
	
	/*
	 * The defauls locale. Should only be used if there is neither a user preference nor a usable host default
	 */
	public static final Locale LOCALE = Locale.ENGLISH;
	
	
	/**
	 * Returns the user preferences for the application
	 * 
	 * @return {@code null} if the user preferences for this package could not be created or retrieved
	 * <br>A non-null value
	 * <br>A possibly new value
	 * @see Preferences#userNodeForPackage(Class)
	 * @throws SecurityException if the preferences cannot be loaded
	 */
	public static final Preferences getPreferences() {	
		return Preferences.userNodeForPackage(Settings.class);
	}
	
	/**
	 * <p>Reflectively finds and returns the constant value (in most cases a default settings value)
	 * for the specified field name / settings key.</p>
	 * 
	 * <p>NEVER USE THIS METHOD UNLESS YOU CANNOT ACCESS THE CONSTANTS DIRECTLY!</p>
	 * 
	 * @param fieldName
	 * <br />The name of the constant field or the default settings value to be looked up
	 * <br />Should not be null
	 * @return
	 * <br />The string representation of the requested constant / default settings value
	 * <br />Never null
	 */
	public static final String getConstantAsString(final String fieldName) {
		try {
			final Class<Settings> settingsClass = Settings.class;
			final Field valueField = settingsClass.getDeclaredField(fieldName.toUpperCase());

			final Object value = valueField.get(null);

			if(valueField.getType().isAssignableFrom(String.class))
				return (String) value;

			return value.toString();
		} catch(IllegalAccessException e) {
			throw new ConstantReflectionException(fieldName, e);
		} catch(NoSuchFieldException e) {
			throw new ConstantReflectionException(fieldName, e);
		}
	}
	
	/**
	 * <p>Reflectively finds and returns a Set of the names of all public constant (both static and final) fields
	 * in Settings.</p>
	 * 
	 * <p>This method returns all constants defined by this class (except {@code serialVersionUID}). It does not
	 * make any qualitative guarantees about these constants. In particular, they are not guaranteed to be
	 * congruent with the per-user settings available via Preferences, although a best effort to achieve such 
	 * a congruency is made.</p>
	 * 
	 * @return 
	 * <br />The names of all constants in Settings
	 * <br />Never null
	 */
	public static final Set<String> getConstantFieldNames() {
		final Set<String> fieldNames = new HashSet<String>();
		final Class<Settings> settingsClass = Settings.class;
		
		int modifiers = 0x0;
		modifiers |= Modifier.PUBLIC;
		modifiers |= Modifier.STATIC;
		modifiers |= Modifier.FINAL;

		for(Field field: settingsClass.getDeclaredFields()) {
			String fieldName = field.getName();
			if(!fieldName.equals("serialVersionUID") && field.getModifiers() == modifiers)
				fieldNames.add(fieldName);
		}

		return fieldNames;
	}
	
	/**
	 * Private constructor to prevent this class from being instantiated.
	 */
	private Settings() {
		// Do nothing, just prevent instantiation
	}
	
}
