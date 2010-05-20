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

package net.sourceforge.transfile.tools;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.transfile.settings.Settings;
import net.sourceforge.transfile.tools.exceptions.UserApplicationDirectoryException;

/**
 * Holder for static utility methods.
 * Should not be instantiated.
 *
 * @author Martin Riedel (creation 2010-05-15)
 * @author codistmonk (modifications since 2010-05-19)
 *
 */
public final class Tools {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private Tools() {
		// Do nothing
	}
	
	/**
	 * Returns the user-specific application directory for the current system user.
	 * Guarantees that the directory exists and is both read- and writable.
	 * 
	 * @return 
	 * <br />A {@link File} representing the user-specific application directory
	 * <br />A non-null value
	 * @throws UserApplicationDirectoryException if
	 * <ul>
	 *  <li>the system does not provide a valid "user.home" property;
	 *  <li>the application directory does not exist or cannot be created;
	 *  <li>the application directory exists but cannot be read from;
	 *  <li>the application directory exists but cannot be written to;
	 *  <li>a security violation occurs.
	 * </ul>
	 */
	public static final File getUserApplicationDirectory() {
		final String userHomeDirectoryString = System.getProperty("user.home");
		
		if (userHomeDirectoryString == null || "".equals(userHomeDirectoryString)) {
			throw new UserApplicationDirectoryException("System did not provide a \"user.home\" property");
		}
		
		final File userApplicationDirectory = new File(userHomeDirectoryString, Settings.getPreferences().get("user_application_directory", Settings.USER_APPLICATION_SUBDIRECTORY.getPath()));
		
		try {
			if (!userApplicationDirectory.isDirectory()) {
				getLoggerForThisMethod().log(Level.INFO, "user application directory nonexistent, creating: " + userApplicationDirectory.getAbsolutePath());
				userApplicationDirectory.mkdirs();

				if (!userApplicationDirectory.isDirectory()) {
					getLoggerForThisMethod().log(Level.WARNING, "failed to create user application directory: " + userApplicationDirectory.getAbsolutePath());
					throw new UserApplicationDirectoryException("directory does not exist and cannot be created");
				}
			}
			
			if (!userApplicationDirectory.canRead()) {
				getLoggerForThisMethod().log(Level.WARNING, "user application directory exists but cannot be read from: " + userApplicationDirectory.getAbsolutePath());
				throw new UserApplicationDirectoryException("directory exists but cannot be read from");
			}
			
			if (!userApplicationDirectory.canWrite()) {
				getLoggerForThisMethod().log(Level.WARNING, "user application directory exists but cannot be written to: " + userApplicationDirectory.getAbsolutePath());
				throw new UserApplicationDirectoryException("directory exists but cannot be written to");	
			}

			return userApplicationDirectory;
		} catch (final SecurityException exception) {
			throw new UserApplicationDirectoryException(exception);
		}
	}
	
	/**
	 * If a method {@code A.a()} calls a method {@code B.b()}, then the result of calling this method in {@code b()} will be {@code A.class}.
	 * <br>Warning: this method can only be used directly.
	 * <br>If you want to refactor your code, you can re-implement the functionality using {@code Thread.currentThread().getStackTrace()}.
	 * 
	 * @return {@code null} if the caller class cannot be retrieved
	 * <br>A possibly null value
	 */
	public static final Class<?> getCallerClass() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		if (stackTrace.length > 3)
			try {
				return Class.forName(stackTrace[3].getClassName());
			} catch (final ClassNotFoundException exception) {
				// Nothing
			}
		
		return null;
	}
	
	/**
	 * If a method {@code a()} calls a method {@code b()}, then the result of calling this method in b() will be "a".
	 * <br>Warning: this method can only be used directly.
	 * <br>If you want to refactor your code, you can re-implement the functionality using {@code Thread.currentThread().getStackTrace()}.
	 * 
	 * @return {@code null} if the caller method cannot be retrieved
	 * <br>A possibly null value
	 */
	public static final String getCallerMethodName() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		return stackTrace.length > 3 ? stackTrace[3].getMethodName() : null;
	}
	
	/**
	 * Calls {@link Logger#getLogger(String)} using the fully qualified name of the calling method.
	 * <br>Warning: this method can only be used directly.
	 * <br>If you want to refactor your code, you can re-implement the functionality using {@code Thread.currentThread().getStackTrace()}.
	 * 
	 * @return
	 * <br>A non-null value
	 * @throws NullPointerException if the caller class cannot be retrieved
	 */
	public static final Logger getLoggerForThisMethod() {
		return Logger.getLogger(getCallerClass().getCanonicalName() + "." + getCallerMethodName());
	}
	
	/**
	 * This method reinterprets strings read from property files using UTF-8.
	 * <br>{@link ResourceBundle} interprets the contents of .properties files as if they used ISO-8859-1 encoding.
	 * <br>If UTF-8 is used to encode these files, the retrieved messages will present bad characters.
	 * <br>For instance, the character 'Ω' is encoded as {@code 0xCEA9} in UTF-8 but cannot be directly encoded in ISO-8859-1.
	 * <br>Instead, the code \u03A9 would have to be used so that {@link ResourceBundle} retrieves the character 'Ω'.
	 * <br>If a file contains 'Ω' in UTF-8, {@link ResourceBundle} will interpret it using ISO-8859-1 as "Î©".
	 * because {@code 0xCE} is 'Î' and {@code 0xA9} is '©' in this encoding.
	 * <br>If {@code s = "Î©"} is the string retrieved from a file containing 'Ω' in UTF-8,
	 * then {@code !s.equals("Ω")} but {@code iso88591ToUTF8(s).equals("Ω")}.
	 * 
	 * @param translatedMessage
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @return a new string or {@code translatedMessage} if the conversion fails
	 * <br>A non-null value
	 * <br>Shared value
	 */
	public static final String iso88591ToUTF8(final String translatedMessage) {
		try {
			return new String(translatedMessage.getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException exception) {
			getLoggerForThisMethod().log(Level.WARNING, "", exception);
			
			return translatedMessage;
		}
	}
	
	/**
	 * Does the same thing as {@link Class#cast(Object)}, but returns {@code null} instead of throwing an exception if the cast cannot be performed.
	 * 
	 * @param <T> the type into which {@code object} is tentatively being cast
	 * @param cls
	 * <br>Should not be null
	 * @param object
	 * <br>Can be null
	 * @return {@code null} if {@code object} is {@code null} or cannot be cast into {@code T}, otherwise {@code object}
	 * <br>A possibly null value
	 */
	public static final <T> T cast(final Class<T> cls, final Object object) {
		if (object == null || !cls.isAssignableFrom(object.getClass())) {
			return null;
		}
		
		return cls.cast(object);
	}
	
	/**
	 * This method tries to find a setter starting with "set" for the specified property of the object.
	 * <br>Eg: {@code getSetter(object, "text", String.class)} tries to find a method {@code setText(String)}
	 *
	 * @param object
	 * <br>Should not be null
	 * @param propertyName
	 * <br>Should not be null
	 * @param propertyClass 
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 * @throws RuntimeException if an appropriate setter cannot be retrieved
	 */
	public static final Method getSetter(final Object object, final String propertyName, final Class<?> propertyClass) {
		final String setterName = "set" + toUpperCamelCase(propertyName);
		
		try {
			// Try to retrieve a public setter
			return object.getClass().getMethod(setterName, propertyClass);
		} catch (final Exception exception) {
			// Do nothing
		}
		
		try {
			// Try to retrieve a setter declared in object's class, regardless of its visibility
			return object.getClass().getDeclaredMethod(setterName, propertyClass);
		} catch (final Exception exception) {
			// Do nothing
		}
		
		throw new RuntimeException("Unable to retrieve a getter for property " + propertyName);
	}
	
	/**
	 * This method tries to find a getter starting with "get", "is", or "has" (in that order) for the specified property of the object.
	 * <br>Eg: {@code getGetter(object, "empty")} tries to find a method {@code getEmpty()} or {@code isEmpty()} or {@code hasEmpty()}
	 * 
	 * @param object
	 * <br>Should not be null
	 * @param propertyName the camelCase name of the property
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 * @throws RuntimeException if an appropriate getter cannot be retrieved
	 */
	public static final Method getGetter(final Object object, final String propertyName) {
		final String upperCamelCase = toUpperCamelCase(propertyName);
		
		for (final String prefix : array("get", "is", "has")) {
			final String getterName = prefix + upperCamelCase;
			
			try {
				// Try to retrieve a public getter
				return object.getClass().getMethod(getterName);
			} catch (final Exception exception) {
				// Do nothing
			}
			
			try {
				// Try to retrieve a getter declared in object's class, regardless of its visibility
				return object.getClass().getDeclaredMethod(getterName);
			} catch (final Exception exception) {
				// Do nothing
			}
		}
		
		throw new RuntimeException("Unable to retrieve a getter for property " + propertyName);
	}
	
	/**
	 * Converts "someName" into "SomeName".
	 * 
	 * @param lowerCamelCase
	 * <br>Should not be null
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	public static final String toUpperCamelCase(final String lowerCamelCase) {
		return Character.toUpperCase(lowerCamelCase.charAt(0)) + lowerCamelCase.substring(1);
	}
	
	/**
	 * Converts {@code null} into "", otherwise returns the parameter untouched. 
	 * 
	 * @param string
	 * <br>Can be null
	 * <br>Shared parameter
	 * @return {@code string} or ""
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public static final String emptyIfNull(final String string) {
		return string == null ? "" : string;
	}
	
	/**
	 * Convenience method to build arrays for non-primitive types.
	 * 
	 * @param <T> the actual type of the elements
	 * @param elements
	 * <br>Can be null
	 * <br>Shared parameter
	 * @return {@code elements}
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	public static final <T> T[] array(final T... elements) {
		return elements;
	}
	
	/**
	 * Use this method when you want to propagate a checked exception wrapped in a runtime exception
	 * instead of using the normal checked exception mechanism. 
	 * 
	 * @param <T> the type that the caller is supposed to return
	 * @param cause
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @return
	 * <br>Does not return
	 * @throws RuntimeException with {@code cause} as cause if it is a checked exception, otherwise {@code cause} is re-thrown
	 */
	public static final <T> T throwUnchecked(final Throwable cause) {
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		} else if (cause instanceof Error) {
			throw (Error) cause;
		}
		
		throw new RuntimeException(cause);
	}
	
    /**
     * Concatenates the source location of the call and the string representations of the parameters separated by spaces.
     * <br>This is method helps to perform console debugging using System.out or System.err.
     * 
     * @param stackIndex 1 is the source of this method, 2 is the source of the call, 3 is the source of the call's caller, and so forth
     * <br>Range: {@code [O .. Integer.MAX_VALUE]}
     * @param objects
     * <br>Should not be null
     * @return
     * <br>A new value
     * <br>A non-null value
     * @throws IndexOutOfBoundsException if {@code stackIndex} is invalid
     */
    public static final String debug(final int stackIndex, final Object... objects) {
        final StringBuilder builder = new StringBuilder(Thread.currentThread().getStackTrace()[stackIndex].toString());

        for (final Object object : objects) {
            builder.append(" ").append(object);
        }

        return builder.toString();
    }
    
    /**
     * Prints on the standard output the concatenation of the source location of the call
     * and the string representations of the parameters separated by spaces.
     * 
     * @param objects
     * <br>Should not be null
     */
    public static final void debugPrint(final Object... objects) {
        System.out.println(debug(3, objects));
    }
    
}
