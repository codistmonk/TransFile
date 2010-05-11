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

package net.sourceforge.transfile.gui.swing;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Instances of this class can translate messages using locales and resource bundles.
 * <br>The easiest way add translation to a Swing program with this class is by using the static methods in {@link SwingTranslator.Helpers}.
 *
 * @author codistmonk (2010-05-11)
 *
 */
public class SwingTranslator {
	
	private final Collection<Listener> listeners;
	
	private final Set<Autotranslator> autotranslators;
	
	private final Set<Locale> availableLocales;
	
	private Locale locale;
	
	public SwingTranslator() {
		this.listeners = new ArrayList<Listener>();
		this.autotranslators = new HashSet<Autotranslator>();
		this.availableLocales = new HashSet<Locale>();
		
		this.availableLocales.add(new Locale(""));
	}
	
	/**
	 * 
	 * @param listener
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void addTranslatorListener(final Listener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 * <br>Can be null
	 */
	public final void removeTranslatorListener(final Listener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public final Listener[] getTranslatorListeners() {
		return this.listeners.toArray(new Listener[this.listeners.size()]);
	}
	
	/**
	 * 
	 * TODO doc
	 * @param <T> the actual type of {@code object}
	 * @param object
	 * <br>Should not be null
	 * <br>Input-output parameter
	 * <br>Shared parameter
	 * @param textPropertyName
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param translationKey
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param messagesBase 
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param parameters 
	 * <br>Should not be null
	 * @return {@code object}
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final <T> T set(final T object, final String textPropertyName, final String translationKey, final String messagesBase, final Object... parameters) {
		final Autotranslator autotranslator = this.new Autotranslator(object, textPropertyName, translationKey, messagesBase, parameters);
		
		this.collectAvailableLocales(messagesBase);
		
		autotranslator.translate();
		
		// If there is already another autotranslator with the same object and textProprtyName
		// remove it before adding the new autotranslator
		this.autotranslators.remove(autotranslator);
		this.autotranslators.add(autotranslator);
		
		return object;
	}
	
	/**
	 * 
	 * TODO doc
	 * @param object
	 * <br>Should not be null
	 * <br>Input-output parameter
	 * <br>Shared parameter
	 * @param textPropertyName
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void reset(final Object object, final String textPropertyName) {
		for (final Iterator<Autotranslator> iterator = this.autotranslators.iterator(); iterator.hasNext();) {
			final Autotranslator autotranslator = iterator.next();
			
			if (autotranslator.getObject().equals(object) && autotranslator.getTextPropertyName().equals(textPropertyName)) {
				iterator.remove();
				
				autotranslator.untranslate();
				
				return;
			}
		}
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final Locale getLocale() {
		return this.locale;
	}
	
	/**
	 * 
	 * TODO doc
	 * @param locale
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void setLocale(final Locale locale) {
		if (this.getLocale() != locale) {
			final Locale oldLocale = this.getLocale();
			
			this.locale = locale;
			
			for (final Autotranslator autotranslator : this.autotranslators) {
				autotranslator.translate();
			}
			
			for (final Listener listener : this.getTranslatorListeners()) {
				listener.localeChanged(oldLocale, this.getLocale());
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	public final Locale[] getAvailableLocales() {
		return this.availableLocales.toArray(new Locale[this.availableLocales.size()]);
	}
	
	/**
	 * 
	 * TODO doc
	 * @param translationKey
	 * <br>Should not be null
	 * @param messagesBase
	 * <br>Should not be null
	 * @param parameters 
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 */
	public final String translate(final String translationKey, final String messagesBase, final Object... parameters) {
		final ResourceBundle messages = ResourceBundle.getBundle(messagesBase, this.getLocale());
		
		String translatedMessage = translationKey;
		
		try {
			translatedMessage = iso88591ToUTF8(messages.getString(translationKey));
		} catch (final MissingResourceException exception) {
			getLoggerForThisMethod().log(Level.WARNING, "Missing translation for locale (" + SwingTranslator.this.getLocale() + ") of " + exception.getKey());
		}
		
		final Object[] localizedParameters = parameters.clone();
		
		for (int i = 0; i < localizedParameters.length; ++i) {
			if (localizedParameters[i] instanceof Throwable) {
				localizedParameters[i] = ((Throwable) localizedParameters[i]).getLocalizedMessage();
			}
		}
		
		return MessageFormat.format(translatedMessage, localizedParameters);
	}
	
	/**
	 * 
	 * TODO doc
	 * @param messagesBase
	 * <br>Should not be null
	 */
	private final void collectAvailableLocales(final String messagesBase) {
		for (final Locale locale : Locale.getAvailableLocales()) {
			try {
				if (locale.equals(ResourceBundle.getBundle(messagesBase, locale).getLocale())) {
					this.availableLocales.add(locale);
				}
			} catch (final Exception exception) {
				// Do nothing
			}
		}
	}
	
	/**
	 * 
	 * This class defines a property translation operation.
	 * 
	 * @author codistmonk (creation 2010-05-11)
	 *
	 */
	private final class Autotranslator {
		
		private final Object object;
		
		private final String textPropertyName;
		
		private final String translationKey;
		
		private final String messagesBase;
		
		private final Object[] parameters;
		
		private final Method setter;
		
		/**
		 * 
		 * @param object
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param textPropertyName
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param translationKey
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param messagesBase
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param parameters 
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @throws RuntimeException if a setter cannot be retrieved for the property.
		 */
		public Autotranslator(final Object object, final String textPropertyName,
				final String translationKey, final String messagesBase, final Object... parameters) {
			this.object = object;
			this.textPropertyName = textPropertyName;
			this.translationKey = translationKey;
			this.messagesBase = messagesBase;
			this.parameters = parameters;
			this.setter = getSetter(object, textPropertyName, String.class);
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public final Object getObject() {
			return this.object;
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public final String getTextPropertyName() {
			return this.textPropertyName;
		}
		
		public final void translate() {
			this.set(SwingTranslator.this.translate(this.translationKey, this.messagesBase, this.parameters));
		}
		
		/**
		 * Sets the property with the translation key.
		 */
		public final void untranslate() {
			this.set(this.translationKey);
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Autotranslator that = cast(this.getClass(), object);
			
			return that != null && this.getObject().equals(that.getObject()) && this.getTextPropertyName().equals(that.getTextPropertyName());
		}
		
		@Override
		public final int hashCode() {
			return this.object.hashCode() + this.textPropertyName.hashCode();
		}
		
		/**
		 * Calls {@code this.setter} with parameter {@code text}.
		 * 
		 * @param text
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		private final void set(final String text) {
			try {
				this.setter.invoke(this.getObject(), text);
			} catch (final Exception exception) {
				getLoggerForThisMethod().log(Level.WARNING, "", exception);
			}
		}
		
	}
	
	private static SwingTranslator defaultTranslator;
	
	/**
	 * This method creates the default translator if necessary, and then always returns the same value.
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public static final synchronized SwingTranslator getDefaultTranslator() {
		if (defaultTranslator == null) {
			defaultTranslator = new SwingTranslator();
		}
		
		return defaultTranslator;
	}
	
	/**
	 * This method gets or creates a {@link Locale} corresponding to {@code languageCountryVariant}.
	 * <br>{@code languageCountryVariant} is a String made of 1 to 3 elements separated by "_":
	 * <br>language ("" or ISO 639 2-letter code) ["_" country ("" or ISO 3166 2-letter code) ["_" variant (can be "")]]
	 * @param languageCountryVariant
	 * <br>Should not be null
	 * @return
	 * <br>A possibly new value
	 * <br>A non-null value
	 */
	public static final Locale createLocale(final String languageCountryVariant) {
		final String[] tmp = languageCountryVariant.split("_");
		final String language = tmp[0];
		final String country = tmp.length > 1 ? tmp[1] : "";
		final String variant = tmp.length > 2 ? tmp[2] : "";
		
		for (final Locale locale : Locale.getAvailableLocales()) {
			if (locale.getLanguage().equals(language) && locale.getCountry().equals(country) && locale.getVariant().equals(variant)) {
				return locale;
			}
		}
		
		return new Locale(language, country, variant);
	}
	
	/**
	 * 
	 * @param locale
	 * <br>Should not be null
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	public static final String getLanguageCountryVariant(final Locale locale) {
		return locale.getLanguage() + "_" + locale.getCountry() + "_" + locale.getVariant();
	}
	
	/**
	 * 
	 * TODO doc
	 * @param translatedMessage
	 * <br>Should not be null
	 * <br>May be a shared parameter
	 * @return
	 * <br>A non-null value
	 * <br>May be a shared value
	 */
	public static final String iso88591ToUTF8(final String translatedMessage) {
		try {
			return new String(translatedMessage.getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException exception) {
			exception.printStackTrace();
			return translatedMessage;
		}
	}
	
	/**
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
		if (object == null || !cls.isAssignableFrom(object.getClass()))
			return null;
		
		return cls.cast(object);
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 */
	public static final Logger getLoggerForThisMethod() {
		return Logger.getLogger(getCallerClass().getCanonicalName() + "." + getCallerMethodName());
	}
	
	/**
	 * 
	 * @return
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
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	public static final String getCallerMethodName() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		return stackTrace.length > 3 ? stackTrace[3].getMethodName() : null;
	}
	
	/**
	 * 
	 * TODO doc
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
		try {
			return object.getClass().getMethod("set" + toUpperCamelCase(propertyName), propertyClass);
		} catch (final Exception exception) {
			return throwRuntimeException(exception);
		}
	}
	
	/**
	 * 
	 * TODO doc
	 * @param object
	 * <br>Should not be null
	 * @param propertyName
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 * @throws RuntimeException if an appropriate getter cannot be retrieved
	 */
	public static final Method getGetter(final Object object, final String propertyName) {
		final String upperCamelCase = toUpperCamelCase(propertyName);
		
		for (final String prefix : array("get", "is", "has")) {
			try {
				return object.getClass().getMethod(prefix + upperCamelCase);
			} catch (final Exception exception) {
				// Do nothing
			}
		}
		
		throw new RuntimeException("Unable to retrieve a getter for property " + propertyName);
	}
	
	/**
	 * 
	 * TODO doc
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
	 * 
	 * @param string
	 * <br>Can be null
	 * <br>Shared parameter
	 * @return {@code string}
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public static final String emptyIfNull(final String string) {
		return string == null ? "" : string;
	}
	
	/**
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
	 * 
	 * @param <T> the type that the caller is supposed to return
	 * @param cause
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @return
	 * <br>Does not return
	 * @throws RuntimeException with {@code cause} as cause if it is a checked exception, otherwise {@code cause} is re-thrown
	 */
	public static final <T> T throwRuntimeException(final Throwable cause) {
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		}
		
		throw new RuntimeException(cause);
	}
	
	/**
	 * This class contains static methods that help manipulate the default translator.
	 * <br>It is recommended to use a static import so that the only name to remember is translate.
	 *
	 * @author codistmonk (creation 2010-05-11)
	 *
	 */
	public static final class Helpers {
		
		/**
		 * Private constructor to prevent this class from being instantiated.
		 */
		private Helpers() {
			// Do nothing
		}
		
		/**
		 * This method registers {@code object} in the default translator and translates it using the specified translation key and optional parameters.
		 * <br>The messages bundle is the one associated with the caller class.
		 * 
		 * @param <T> the actual type of {@code object} 
		 * @param object the object whose properties need to be translated
		 * <br>Should not be null
		 * <br>Input-output parameter
		 * <br>Shared parameter
		 * @param textPropertyName the lowerCamelCase name of the property to translate; a setter named "set" + UpperCamelCase name is expected
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param translationKey
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param parameters optional parameters to build the translated message; if an exception is passed, its localized message will be used
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @return {@code object}
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public static final <T> T translate(final T object, final String textPropertyName, final String translationKey, final Object... parameters) {
			return getDefaultTranslator().set(object, textPropertyName, translationKey, getCallerClass().getSimpleName(), parameters);
		}
		
		/**
		 * This method tries to translate {@code component}'s properties named "text", "title" and "toolTipText".
		 * <br>If the property doesn't exist or is not accessible with public getter and setter, nothing happens.
		 * <br>The translation key for each property is the value of the property before the call.
		 * <br>Warning: {@code parameters} will be used for all 3 properties if they are accessible.
		 * <br>The messages bundle is the one associated with the caller class.
		 * 
		 * @param <T> the actual type of {@code component} 
		 * @param component 
		 * <br>Should not be null
		 * <br>Input-output parameter
		 * <br>Shared parameter
		 * @param parameters
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @return {@code component}
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public static final <T> T translate(final T component, final Object... parameters) {
			for (final String textPropertyName : array("text", "title", "toolTipText")) {
				try {
					getDefaultTranslator().set(component, textPropertyName, (String) getGetter(component, textPropertyName).invoke(component), getCallerClass().getSimpleName(), parameters);
				} catch (final Exception exception) {
					// Do nothing
				}
			}
			
			return component;
		}
		
		/**
		 * This method tries to translate {@code translationKey} with the specified parameter using the caller's class
		 * to obtain a resource bundle.
		 * 
		 * @param translationKey
		 * <br>Should not be null
		 * @param parameters 
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @return
		 * <br>A non-null value
		 */
		public static final String translate(final String translationKey, final Object... parameters) {
			return getDefaultTranslator().translate(translationKey, getCallerClass().getSimpleName(), parameters);
		}
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-09-28)
	 *
	 */
	public static interface Listener {
		
		/**
		 * 
		 * @param oldLocale
		 * <br>Should not be null
		 * @param newLocale
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public abstract void localeChanged(Locale oldLocale, Locale newLocale);
		
	}
	
}
