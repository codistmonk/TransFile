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

package net.sourceforge.transfile.i18n;

import static net.sourceforge.transfile.tools.Tools.getCallerClass;
import static net.sourceforge.transfile.tools.Tools.getLoggerForThisMethod;
import static net.sourceforge.transfile.tools.Tools.iso88591ToUTF8;
import static net.sourceforge.transfile.tools.Tools.cast;
import static net.sourceforge.transfile.tools.Tools.array;
import static net.sourceforge.transfile.tools.Tools.getGetter;
import static net.sourceforge.transfile.tools.Tools.getSetter;

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

/**
 * Instances of this class can translate messages using locales and resource bundles.
 * <br>The easiest way to add translation to a Swing program with this class is by using the static methods in {@link Translator.Helpers}.
 * <br>To improve performance, call {@code this.setAutoCollectingLocales(false)} after all available locales have been collected.
 * <br>You can manually collect locales with {@link #collectAvailableLocales(String)}.
 * <br>Instances of this class are thread-safe as long as the listeners don't cause synchronization problems.
 *
 * @author codistmonk (2010-05-11)
 *
 */
public class Translator {
	
	private final Collection<Listener> listeners;
	
	private final Set<Autotranslator> autotranslators;
	
	private final Set<Locale> availableLocales;
	
	private Locale locale;
	
	private boolean autoCollectingLocales;
	
	public Translator() {
		this.listeners = new ArrayList<Listener>();
		this.autotranslators = new HashSet<Autotranslator>();
		this.availableLocales = new HashSet<Locale>();
		this.locale = Locale.getDefault();
		this.autoCollectingLocales = true;
	}
	
	/**
	 * 
	 * @param listener
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final synchronized void addTranslatorListener(final Listener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 * <br>Can be null
	 */
	public final synchronized void removeTranslatorListener(final Listener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * 
	 * @return {@code true} if {@link #collectAvailableLocales(String)} is called automatically each time a translation is performed
	 */
	public final boolean isAutoCollectingLocales() {
		return this.autoCollectingLocales;
	}
	
	/**
	 * 
	 * @param autoCollectingLocales {@code true} if {@link #collectAvailableLocales(String)} should be called automatically each time a translation is performed
	 */
	public final void setAutoCollectingLocales(final boolean autoCollectingLocales) {
		this.autoCollectingLocales = autoCollectingLocales;
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public final synchronized Listener[] getTranslatorListeners() {
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
	public final synchronized <T> T translate(final T object, final String textPropertyName, final String translationKey, final String messagesBase, final Object... parameters) {
		this.autoCollectLocales(messagesBase);
			
		final Autotranslator autotranslator = this.new Autotranslator(object, textPropertyName, translationKey, messagesBase, parameters);
		
		autotranslator.translate();
		
		// If there is already another autotranslator with the same object and textPropertyName
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
	public final synchronized void untranslate(final Object object, final String textPropertyName) {
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
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final synchronized Locale getLocale() {
		return this.locale;
	}
	
	/**
	 * If {@code this.getLocale()} is not equal to {@code locale},
	 * then the locale is changed, the autotranslators are updated and the listeners are notified.
	 * 
	 * @param locale
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final synchronized void setLocale(final Locale locale) {
		if (!this.getLocale().equals(locale)) {
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
	 * The set of available locales can be augmented with {@link #getAvailableLocales()}.
	 * <br>{@link #getAvailableLocales()} is called each time a translation is performed.
	 * 
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	public final synchronized Locale[] getAvailableLocales() {
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
	public final synchronized String translate(final String translationKey, final String messagesBase, final Object... parameters) {
		this.autoCollectLocales(messagesBase);
		
		String translatedMessage = translationKey;
		
		try {
			final ResourceBundle messages = ResourceBundle.getBundle(messagesBase, this.getLocale());
			
			translatedMessage = iso88591ToUTF8(messages.getString(translationKey));
		} catch (final MissingResourceException exception) {
			getLoggerForThisMethod().log(Level.WARNING, "Missing translation for locale (" + Translator.this.getLocale() + ") of " + translationKey);
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
	 * Scans {@code messagesBase} using {@link Locale#getAvailableLocales()} and adds the available locales to {@code this}.
	 * <br>A locale is "available" to the translator if an appropriate resource bundle is found.
	 * 
	 * @param messagesBase
	 * <br>Should not be null
	 */
	public final synchronized void collectAvailableLocales(final String messagesBase) {
		// TODO don't rely on Locale.getAvailableLocales(), use only messagesBase if possible
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
	 * Calls {@link #collectAvailableLocales(String)} if {@code this.isAutoCollectingLocales()}.
	 * 
	 * @param messagesBase
	 * <br>Should not be null
	 */
	private final void autoCollectLocales(final String messagesBase) {
		if (this.isAutoCollectingLocales()) {
			this.collectAvailableLocales(messagesBase);
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
			this.set(Translator.this.translate(this.translationKey, this.messagesBase, this.parameters));
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
	
	private static Translator defaultTranslator;
	
	/**
	 * This method creates the default translator if necessary, and then always returns the same value.
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public static final synchronized Translator getDefaultTranslator() {
		if (defaultTranslator == null) {
			defaultTranslator = new Translator();
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
	 * This method does the opposite of {@link #createLocale(String)}.
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
		 * 
		 * @param cls
		 * <br>Should not be null
		 * @return the top level class enclosing {@code cls}, or {@code cls} itself if it is a top level class
		 * <br>A non-null value
		 */
		private static final Class<?> getTopLevelEnclosingClass(final Class<?> cls) {
			return cls.getEnclosingClass() == null ? cls : getTopLevelEnclosingClass(cls.getEnclosingClass());
		}
		
		/**
		 * 
		 * 
		 * @param callerClass
		 * <br />The class {@code translate} was called from
		 * <br />Should not be null
		 * @return the correct ResourceBundle base name for the calling class
		 */
		private static final String makeResourceBundleBaseName(final Class<?> callerClass) {
			return "l10n/" + getTopLevelEnclosingClass(callerClass).getName().substring("net.sourceforge.transfile.".length()).replace(".", "/");
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
			return getDefaultTranslator().translate(object, textPropertyName, translationKey, makeResourceBundleBaseName(getCallerClass()), parameters);
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
					final String translationKey = (String) getGetter(component, textPropertyName).invoke(component);
					
					if (translationKey != null && !translationKey.isEmpty()) {
						getDefaultTranslator().translate(component, textPropertyName, translationKey, makeResourceBundleBaseName(getCallerClass()), parameters);
					}
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
			return getDefaultTranslator().translate(translationKey, makeResourceBundleBaseName(getCallerClass()), parameters);
		}
		
	}
	
	/**
	 * 
	 * Listener interface for translator events.
	 * 
	 * @author codistmonk (creation 2009-09-28)
	 *
	 */
	public static interface Listener {
		
		/**
		 * Called whenever the translator's locale has been changed, and after the registered
		 * objects have been translated.
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
