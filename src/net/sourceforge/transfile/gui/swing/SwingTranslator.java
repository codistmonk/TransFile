package net.sourceforge.transfile.gui.swing;

import static net.sourceforge.transfile.gui.swing.PreferencesFrame.debugPrint;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.TextComponent;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.text.JTextComponent;

import net.sourceforge.transfile.settings.Settings;

/**
 * 
 * @author codistmonk (creation 2009-09-04)
 *
 */
public final class SwingTranslator implements Serializable {
	
	private final Collection<ITranslatorListener> listeners;
	
	private final MultiMap<Object, Pair<Method, String>> translationMethods;
	
	private final Map<String, Void> untranslated;
	
	private String messagesBase;
	
	private Locale locale;
	
	/**
	 * 
	 */
	public SwingTranslator() {
		this.listeners = new ArrayList<ITranslatorListener>();
		this.translationMethods = new MultiMap<Object, Pair<Method, String>>(IdentityHashMap.class, HashSet.class);
		this.untranslated = new IdentityHashMap<String, Void>();
		this.messagesBase = "";
		this.locale = Locale.getDefault();
		
		this.addTranslatorListener(new ITranslatorListener() {
			
			@Override
			public final void messagesBaseChanged(final String oldMessagesBase, final String newMessagesBase) {
				SwingTranslator.this.updateAutotranslated();
			}
			
			@Override
			public final void localeChanged(final Locale oldLocale, final Locale newLocale) {
				SwingTranslator.this.updateAutotranslated();
			}
			
		});
	}
	
	/**
	 * 
	 */
	public final void updateAutotranslated() {
		for (final Map.Entry<Object, Collection<Pair<Method, String>>> entry : this.translationMethods.entrySet()) {
			for (final Pair<Method, String> pair : entry.getValue()) {
				try {
					this.translate(entry.getKey(), pair.getFirst(), pair.getSecond());
				} catch (final Exception exception) {
					getLoggerForThisMethod().log(Level.WARNING, "", exception);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param string
	 * <ul>
	 *  <li>IN</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @return
	 * <ul>
	 *  <li>MAYBE_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final String untranslated(final String string) {
		if (string == null || string.trim().length() == 0) {
			throw new IllegalArgumentException();
		}
		
		if (this.untranslated.containsKey(string)) {
			return string;
		}
		
		final String result = new String(string);
		
		this.untranslated.put(result, null);
		
		return result;
	}
	
	/**
	 * 
	 * @param listener
	 * <ul>
	 *  <li>REF</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final void addTranslatorListener(final ITranslatorListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 * <ul>
	 *  <li>IN</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final void removeTranslatorListener(final ITranslatorListener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * 
	 * @return
	 * <ul>
	 *  <li>NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final ITranslatorListener[] getTranslatorListeners() {
		return this.listeners.toArray(new ITranslatorListener[this.listeners.size()]);
	}
	
	/**
	 * 
	 * @return
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final String getMessagesBase() {
		return this.messagesBase;
	}
	
	/**
	 * 
	 * @param messagesBase
	 * <ul>
	 *  <li>REF</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final void setMessagesBase(final String messagesBase) {
		final String oldMessagesBase = this.getMessagesBase();
		
		this.messagesBase = messagesBase;
		
		if (this.getMessagesBase() != oldMessagesBase) {
			for (final ITranslatorListener listener : this.getTranslatorListeners()) {
				listener.messagesBaseChanged(oldMessagesBase, this.getMessagesBase());
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final Locale getLocale() {
		return this.locale;
	}
	
	/**
	 * 
	 * @param locale
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final void setLocale(final Locale locale) {
		final Locale oldLocale = this.getLocale();
		
		this.locale = locale;
		
		if (this.getLocale() != oldLocale) {
			for (final ITranslatorListener listener : this.getTranslatorListeners()) {
				listener.localeChanged(oldLocale, this.getLocale());
			}
		}
	}
	
	/**
	 * 
	 * @param string
	 * <ul>
	 *  <li>IN</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @return
	 * <ul>
	 *  <li>MAYBE_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final String translate(final String string) {
		if (this.untranslated.containsKey(string)) {
			return string;
		}
		
		final ResourceBundle messages = this.getMessages();
		
		try {
			return this.reEncode(messages.getString(string));
		} catch (final MissingResourceException exception) {
			getLoggerForThisMethod().log(Level.WARNING, "Missing translation for locale (" + this.getLocale() + ") of " + exception.getKey());
		}
		
		return string;
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
	private final String reEncode(final String translatedMessage) {
		try {
			return new String(translatedMessage.getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException exception) {
			exception.printStackTrace();
			return translatedMessage;
		}
	}
	
	/**
	 * 
	 * @param <T>
	 * @param object
	 * <ul>
	 *  <li>REF</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @param getterName
	 * <ul>
	 *  <li>IN</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @param setterName
	 * <ul>
	 *  <li>IN</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @return <tt>object</tt>
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final <T> T autotranslate(final T object, final String getterName, final String setterName) {
		try {
			final Method textGetter = object.getClass().getMethod(getterName);
			final Method textSetter = object.getClass().getMethod(setterName, String.class);
			final String translationKey = cast(String.class, textGetter.invoke(object));
			
			if (translationKey != null && translationKey.trim().length() > 0 && !this.untranslated.containsKey(translationKey)) {
				this.translate(object, textSetter, translationKey);
				this.translationMethods.put(object, new Pair<Method, String>(textSetter, translationKey));
			}
		} catch (final Exception exception) {
			// Do nothing
		}
		
		return object;
	}
	
	/**
	 * 
	 * TODO doc
	 * @param object
	 * <br>Should not be null
	 * @param textSetter
	 * <br>Should not be null
	 * @param translationKey
	 * <br>Should not be null
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private final void translate(final Object object, final Method textSetter, final String translationKey) throws IllegalAccessException, InvocationTargetException {
		this.translate(object, textSetter, translationKey, object);
	}
	
	/**
	 * 
	 * TODO doc
	 * @param object
	 * <br>Should not be null
	 * @param textSetter
	 * <br>Should not be null
	 * @param translationKey
	 * <br>Should not be null
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private final void translate(final Object object, final Method textSetter, final String translationKey, final Object objectThatMightProvideAResourceBundle) throws IllegalAccessException, InvocationTargetException {
		try {
			final ResourceBundle resourceBundle = ResourceBundle.getBundle(objectThatMightProvideAResourceBundle.getClass().getSimpleName(), this.getLocale());
			
			textSetter.invoke(object, this.reEncode(resourceBundle.getString(translationKey)));
		} catch (final Exception exception) {
			if (objectThatMightProvideAResourceBundle instanceof Component) {
				this.translate(object, textSetter, translationKey, ((Component) objectThatMightProvideAResourceBundle).getParent());
			}
			else if (objectThatMightProvideAResourceBundle instanceof Menu) {
				this.translate(object, textSetter, translationKey, ((Menu) objectThatMightProvideAResourceBundle).getParent());
			}
			else {
				textSetter.invoke(object, this.translate(translationKey));
			}
		}
	}
	
	/**
	 * 
	 * @param <T>
	 * @param object
	 * <ul>
	 *  <li>REF</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @param getterName
	 * <ul>
	 *  <li>IN</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @param setterName
	 * <ul>
	 *  <li>IN</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @return <tt>object</tt>
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final <T> T translate(final T object, final String getterName, final String setterName) {
		try {
			final Method textGetter = object.getClass().getMethod(getterName);
			final Method textSetter = object.getClass().getMethod(setterName, String.class);
			final String translationKey = cast(String.class, textGetter.invoke(object));
			
			if (translationKey != null && translationKey.trim().length() > 0 && !this.untranslated.containsKey(translationKey)) {
				this.translate(object, textSetter, translationKey);
			}
		} catch (final Exception exception) {
			// Nothing
		}
		
		return object;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param autotranslated
	 * <ul>
	 *  <li>IN</li>
	 *  <li>MAYBE_NULL</li>
	 * </ul>
	 * @return <tt>autotranslated</tt>
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>MAYBE_NULL</li>
	 * </ul>
	 */
	public final <T> T stopAutotranslate(final T autotranslated) {
		final Collection<Pair<Method, String>> translationMethods = this.translationMethods.remove(autotranslated);
		
		if (translationMethods != null) {
			for (final Pair<Method, String> pair : translationMethods) {
				try {
					// Change the object text back to the translation key
					pair.getFirst().invoke(autotranslated, pair.getSecond());
				} catch (final Exception exception) {
					getLoggerForThisMethod().log(Level.WARNING, "", exception);
				}
			}
		}
		
		return autotranslated;
	}
	
	/**
	 * 
	 * @param object
	 * <ul>
	 *  <li>IN</li>
	 *  <li>MAYBE_NULL</li>
	 * </ul>
	 * @return
	 * <ul>
	 *  <li>MAYBE_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final Boolean isAutotranslated(final Object object) {
		return this.translationMethods.containsKey(object);
	}
	
	/**
	 * 
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	public final Locale[] getAvailableLocales() {
		final Set<Locale> result = new HashSet<Locale>();
		
		result.add(new Locale(""));
		
		for (final Locale locale : Locale.getAvailableLocales()) {
			try {
				if (locale.equals(ResourceBundle.getBundle(this.getMessagesBase(), locale).getLocale())) {
					result.add(locale);
				}
			} catch (final Exception exception) {
				// Do nothing
			}
		}
		
		return result.toArray(new Locale[result.size()]);
	}
	
	/**
	 * 
	 * @return
	 * <ul>
	 *  <li>MAYBE_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final Locale getBestAvailableLocale() {
		return this.getMessages().getLocale();
	}
	
	/**
	 * 
	 * @return
	 * <ul>
	 *  <li>MAYBE_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final synchronized ResourceBundle getMessages() {
		// XXX using the default locale feels awkward
		// but ResourceBundle.getBundle doesn't seem
		// to care about its locale argument :(
		final Locale globalLocale = Locale.getDefault();
		
		Locale.setDefault(this.getLocale());
		
		final ResourceBundle messages = ResourceBundle.getBundle(this.getMessagesBase(), this.getLocale(), this.getClass().getClassLoader());
		
		Locale.setDefault(globalLocale);
		
		return messages;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param component
	 * <ul>
	 *  <li>REF</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @return <tt>component</tt>
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final <T> T translate(final T component) {
		for (final Object c : new ComponentIterable(component)) {
			this.translate(c, "getTitle", "setTitle");
			this.translate(c, "getToolTipText", "setToolTipText");
			
			if (!(c instanceof TextComponent) && !(c instanceof JTextComponent)) {
				this.translate(c, "getText", "setText");
			}
			
			if (c instanceof JTabbedPane) {
				final JTabbedPane tabbedPane = (JTabbedPane) c;
				
				for (Integer index = 0; index < tabbedPane.getTabCount(); ++index) {
					this.translate(new TabbedPaneTitleAccessor(tabbedPane, index), "getTitle", "setTitle");
				}
			}
			
			if (c instanceof AbstractButton) {
				final Action action = ((AbstractButton) c).getAction();
				
				if (action != null) {
					for (final String key : array(Action.SHORT_DESCRIPTION, Action.NAME, Action.LONG_DESCRIPTION)) {
						this.translate(new ActionStringPropertyAccessor(action, key), "getStringValue", "setStringValue");
					}
				}
			}
		}
		
		return component;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param component
	 * <ul>
	 *  <li>REF</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @return <tt>component</tt>
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final <T> T autotranslate(final T component) {
		for (final Object c : new ComponentIterable(component)) {
			this.autotranslate(c, "getTitle", "setTitle");
			this.autotranslate(c, "getToolTipText", "setToolTipText");
			
			if (!(c instanceof TextComponent) && !(c instanceof JTextComponent)) {
				this.autotranslate(c, "getText", "setText");
			}
			
			if (c instanceof JTabbedPane) {
				final JTabbedPane tabbedPane = (JTabbedPane) c;
				
				for (Integer index = 0; index < tabbedPane.getTabCount(); ++index) {
					this.autotranslate(new TabbedPaneTitleAccessor(tabbedPane, index), "getTitle", "setTitle");
				}
			}
			
			if (c instanceof AbstractButton) {
				final Action action = ((AbstractButton) c).getAction();
				
				if (action != null) {
					for (final String key : array(Action.SHORT_DESCRIPTION, Action.NAME, Action.LONG_DESCRIPTION)) {
						this.autotranslate(new ActionStringPropertyAccessor(action, key), "getStringValue", "setStringValue");
					}
				}
			}
		}
		
		return component;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param component
	 * <ul>
	 *  <li>REF</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @return <tt>component</tt>
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public final <T> T stopAutotranslateComponent(final T component) {
		for (final Object c : new ComponentIterable(component)) {
			this.stopAutotranslate(c);
		}
		
		return component;
	}
	
	private static final long serialVersionUID = 1618686009215601072L;
	
	private static SwingTranslator defaultTranslator;
	
	/**
	 * 
	 * @return
	 * <br>A possibly new value
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public static final synchronized SwingTranslator getDefaultTranslator() {
		if (defaultTranslator == null) {
			defaultTranslator = new SwingTranslator();
			
			// TODO the following call should should be done outside this class because of the dependency on Settings
			defaultTranslator.setLocale(createLocale(emptyIfNull((String) Settings.getInstance().get("locale"))));
			
			// Warning: The call to getAvailableLocales() has side-effects, don't remove it
			debugPrint("Available locales:", Arrays.toString(defaultTranslator.getAvailableLocales()));
		}
		
		return defaultTranslator;
	}
	
	/**
	 * This method gets or creates a {@link Locale} corresponding to {@code languageCountryVariant}.
	 * <br>{@code languageCountryVariant} is a String made of 1 to 3 elements separated by "_":
	 * <br>language ("" or ISO 639 2-letter code) ["_" country ("" or ISO 3166 2-letter code) ["_" variant (can be "")]]
	 * @param languageCountryVariant
	 * <br>NOT_NULL
	 * @return
	 * <br>MAYBE_NEW
	 * <br>NOT_NULL
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
	 * <br>NOT_NULL
	 * @return
	 * <br>NEW
	 * <br>NOT_NULL
	 */
	public static final String getLanguageCountryVariant(final Locale locale) {
		return locale.getLanguage() + "_" + locale.getCountry() + "_" + locale.getVariant();
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-09-28)
	 *
	 */
	public static interface ITranslatorListener {
		
		/**
		 * 
		 * @param oldLocale
		 * <ul>
		 *  <li>IN</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 * @param newLocale
		 * <ul>
		 *  <li>IN</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public abstract void localeChanged(Locale oldLocale, Locale newLocale);
		
		/**
		 * 
		 * @param oldMessagesBase
		 * <ul>
		 *  <li>IN</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 * @param newMessagesBase
		 * <ul>
		 *  <li>IN</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public abstract void messagesBaseChanged(String oldMessagesBase, String newMessagesBase);
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-10-06)
	 *
	 */
	public static final class TranslatedCharSequence implements CharSequence {
		
		private final String translationKey;
		
		private final SwingTranslator translator;
		
		/**
		 * 
		 * @param translationKey
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 * @param translator
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public TranslatedCharSequence(final String translationKey, final SwingTranslator translator) {
			this.translationKey = translationKey;
			this.translator = translator;
		}
		
		/**
		 * 
		 * @return
		 * <ul>
		 *  <li>NOT_NEW</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public final String getTranslationKey() {
			return this.translationKey;
		}
		
		/**
		 * 
		 * @return
		 * <ul>
		 *  <li>NOT_NEW</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public final SwingTranslator getTranslator() {
			return this.translator;
		}
		
		@Override
		public final char charAt(final int index) {
			return this.toString().charAt(index);
		}
		
		@Override
		public final int length() {
			return this.toString().length();
		}
		
		@Override
		public final CharSequence subSequence(final int beginIndex, final int endIndex) {
			return this.toString().subSequence(beginIndex, endIndex);
		}
		
		@Override
		public final boolean equals(final Object object) {
			final TranslatedCharSequence that = cast(this.getClass(), object);
			
			return that != null && this.getTranslationKey().equals(that.getTranslationKey()) && this.getTranslator().equals(that.getTranslator());
		}
		
		@Override
		public final int hashCode() {
			return this.getTranslationKey().hashCode() + this.getTranslator().hashCode();
		}
		
		@Override
		public final String toString() {
			return this.getTranslator().translate(this.getTranslationKey());
		}
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-10-03)
	 *
	 */
	public static final class ComponentIterable implements Iterable<Object> {
		
		private final Object component;
		
		/**
		 * 
		 * @param component
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public ComponentIterable(final Object component) {
			this.component = component;
		}
		
		@Override
		public final Iterator<Object> iterator() {
			return new ComponentIterator(this.component);
		}
		
		/**
		 * 
		 * @author codistmonk (creation 2009-10-03)
		 *
		 */
		public static final class ComponentIterator implements Iterator<Object> {
			
			private final Set<Object> done = new HashSet<Object>();
			
			private final List<Object> nexts = new LinkedList<Object>();
			
			/**
			 * 
			 * @param component
			 * <ul>
			 *  <li>REF</li>
			 *  <li>NOT_NULL</li>
			 * </ul>
			 */
			public ComponentIterator(final Object component) {
				this.nexts.add(component);
			}
			
			@Override
			public final void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public final Object next() {
				final Object result = this.nexts.remove(0);
				
				this.done.add(result);
				
				if (result instanceof JComponent) {
					this.nexts.add(((JComponent) result).getBorder());
				}
				
				if (result instanceof Container) {
					add(this.nexts, ((Container) result).getComponents());
				}
				
				if (result instanceof Menu) {
					final Menu menu = (Menu) result;
					
					for (Integer index = 0; index < menu.getItemCount(); ++index) {
						this.nexts.add(menu.getItem(index));
					}
				}
				
				if (result instanceof Frame) {
					final MenuBar menuBar = ((Frame) result).getMenuBar();
					
					if (menuBar != null) {
						for (Integer index = 0; index < menuBar.getMenuCount(); ++index) {
							this.nexts.add(menuBar.getMenu(index));
						}
					}
				}
				
				if (result instanceof JMenu) {
					add(this.nexts, ((JMenu) result).getMenuComponents());
				}
				
				if (result instanceof JFrame) {
					final JMenuBar menuBar = ((JFrame) result).getJMenuBar();
					
					if (menuBar != null) {
						for (Integer index = 0; index < menuBar.getMenuCount(); ++index) {
							this.nexts.add(menuBar.getComponent(index));
						}
					}
				}
				
				this.nexts.removeAll(this.done);
				
				return result;
			}
			
			@Override
			public final boolean hasNext() {
				return !this.nexts.isEmpty();
			}
			
		}
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-09-28)
	 *
	 */
	public static final class ActionStringPropertyAccessor {
		
		private final Action action;
		
		private final String propertyKey;
		
		/**
		 * 
		 * @param action
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 * @param propertyKey
		 */
		public ActionStringPropertyAccessor(final Action action, final String propertyKey) {
			this.action = action;
			this.propertyKey = propertyKey;
		}
		
		/**
		 * 
		 * @return
		 * <ul>
		 *  <li>NOT_NEW</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 */
		public final String getStringValue() {
			final Object result = this.action.getValue(this.propertyKey);
			
			return result == null ? null : (String) result;
		}
		
		/**
		 * 
		 * @param value
		 * <ul>
		 *  <li>REF</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 */
		public final void setStringValue(final String value) {
			this.action.putValue(this.propertyKey, value);
		}
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-09-28)
	 *
	 */
	public static final class TabbedPaneTitleAccessor {
		
		private final JTabbedPane tabbedPane;
		
		private final Integer index;
		
		/**
		 * 
		 * @param tabbedPane
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 * @param index
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public TabbedPaneTitleAccessor(final JTabbedPane tabbedPane, final Integer index) {
			this.tabbedPane = tabbedPane;
			this.index = index;
		}
		
		/**
		 * 
		 * @param title
		 * <ul>
		 *  <li>REF</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 */
		public final void setTitle(final String title) {
			this.tabbedPane.setTitleAt(this.index, title);
		}
		
		/**
		 * 
		 * @return
		 * <ul>
		 *  <li>NOT_NEW</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 */
		public final String getTitle() {
			return this.tabbedPane.getTitleAt(this.index);
		}
		
	}
	
	/*
	 * TODO move the following methods elsewhere
	 * These methods are usually part of utility classes I use in my projects.
	 * I put them here because I didn't want to create too much classes at first.
	 */
	
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
	 * @param <Element>
	 * @param elements IN MAYBE_NULL
	 * @return MAYBE_NEW MAYBE_NULL
	 */
	public static final <Element> Element[] array(final Element... elements) {
		return elements;
	}
	
	/**
	 * 
	 * @param <E> element
	 * @param <C> collection
	 * @param collection
	 * <ul>
	 *  <li>IN_OUT</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 * @param elements
	 * <ul>
	 *  <li>IN</li>
	 *  <li>MAYBE_NULL</li>
	 * </ul>
	 * @return <tt>collection</tt>
	 * <ul>
	 *  <li>NOT_NEW</li>
	 *  <li>NOT_NULL</li>
	 * </ul>
	 */
	public static final <E, C extends Collection<? super E>> C add(final C collection, final E... elements) {
		if (elements != null) {
			for (final E element : elements) {
				collection.add(element);
			}
		}
		
		return collection;
	}
	
	/**
	 * 
	 * @param <Type>
	 * @param cls IN NOT_NULL
	 * @param object IN MAYBE_NULL
	 * @return NOT_NEW MAYBE_NULL
	 */
	public static final <Type> Type cast(final Class<Type> cls, final Object object) {
		if (object == null || !cls.isAssignableFrom(object.getClass()))
			return null;
		
		return cls.cast(object);
	}
	
	/**
	 * 
	 * @param object0 IN MAYBE_NULL
	 * @param object1 IN MAYBE_NULL
	 * @return MAYBE_NEW NOT_NULL
	 */
	public static final Boolean equals(final Object object0, final Object object1) {
		return object0 == object1 || (object0 != null && object0.equals(object1));
	}
	
	/**
	 * 
	 * @param object IN MAYBE_NULL
	 * @return MAYBE_NEW NOT_NULL
	 */
	public static final Integer hashCode(final Object object) {
		return object == null ? 0 : object.hashCode();
	}
	
	/**
	 * 
	 * @return MAYBE_NEW NOT_NULL
	 */
	public static final Logger getLoggerForThisMethod() {
		return Logger.getLogger(getCallerClass().getCanonicalName() + "." + getCallerMethodName());
	}
	
	/**
	 * 
	 * @return MAYBE_NEW MAYBE_NULL
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
	 * @return MAYBE_NEW MAYBE_NULL
	 */
	public static final String getCallerMethodName() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		return stackTrace.length > 3 ? stackTrace[3].getMethodName() : null;
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-08-04)
	 *
	 * @param <First>
	 * @param <Second>
	 */
	public final class Pair<First, Second> implements Serializable {
		
		private final First first;
		
		private final Second second;
		
		/**
		 * 
		 * @param first IN MAYBE_NULL
		 * @param second IN MAYBE_NULL
		 */
		public Pair(final First first, final Second second) {
			this.first = first;
			this.second = second;
		}
		
		public final First getFirst() {
			return this.first;
		}
		
		public final Second getSecond() {
			return this.second;
		}
		
		@Override
		public final boolean equals(final Object object) {
			final Pair<?, ?> that = cast(Pair.class, object);
			
			return that != null && SwingTranslator.equals(this.getFirst(), that.getFirst()) && SwingTranslator.equals(this.getSecond(), that.getSecond());
		}
		
		@Override
		public final int hashCode() {
			return SwingTranslator.hashCode(this.getFirst()) + SwingTranslator.hashCode(this.getSecond());
		}
		
		@Override
		public final String toString() {
			return "(" + this.getFirst() + ", " + this.getSecond() + ")";
		}
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -888016663679364439L;
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2009-05-29)
	 *
	 * @param <K> key
	 * @param <V> value
	 */
	@SuppressWarnings("unchecked")
	public final class MultiMap<K, V> implements Map<K, Collection<V>> {

		private final Map<K, Collection<V>> data;
		
		private final Class<? extends Collection> collectionClass;
		
		/**
		 * 
		 * @param mapClass
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 * @param collectionClass
		 * <ul>
		 *  <li>REF</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public MultiMap(final Class<? extends Map> mapClass, final Class<? extends Collection> collectionClass) {
			try {
				this.data = mapClass.newInstance();
				this.collectionClass = collectionClass;
			} catch (final Exception exception) {
				throw new RuntimeException(exception);
			}
		}
		
		@Override
		public final void clear() {
			this.data.clear();
		}

		@Override
		public final boolean containsKey(final Object key) {
			return this.data.containsKey(key);
		}

		@Override
		public final boolean containsValue(final Object value) {
			return this.data.containsValue(value);
		}
		
		@Override
		public final Set<java.util.Map.Entry<K, Collection<V>>> entrySet() {
			return this.data.entrySet();
		}

		@Override
		public final boolean equals(final Object o) {
			return this.data.equals(o);
		}

		@Override
		public final Collection<V> get(final Object key) {
			return this.data.get(key);
		}

		@Override
		public final int hashCode() {
			return this.data.hashCode();
		}

		@Override
		public final boolean isEmpty() {
			return this.data.isEmpty();
		}

		@Override
		public final Set<K> keySet() {
			return this.data.keySet();
		}

		@Override
		public final Collection<V> put(final K key, final Collection<V> value) {
			return this.data.put(key, value);
		}
		
		/**
		 * 
		 * @param key
		 * <ul>
		 *  <li>IN</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 * @param value
		 * <ul>
		 *  <li>IN</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 * @return
		 * <ul>
		 *  <li>MAYBE_NEW</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public final Collection<V> put(final K key, final V value) {
			final Collection<V> result = createCollection(key);
			
			result.add(value);
			
			return result;
		}
		
		/**
		 * 
		 * @param key
		 * <ul>
		 *  <li>IN</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 * @param values
		 * <ul>
		 *  <li>IN</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 * @return
		 * <ul>
		 *  <li>MAYBE_NEW</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		public final Collection<V> putAll(final K key, final Collection<V> values) {
			final Collection<V> result = createCollection(key);
			
			result.addAll(values);
			
			return result;
		}
		
		@Override
		public final void putAll(final Map<? extends K, ? extends Collection<V>> t) {
			this.data.putAll(t);
		}
		
		@Override
		public final Collection<V> remove(final Object key) {
			return this.data.remove(key);
		}
		
		@Override
		public final int size() {
			return this.data.size();
		}
		
		@Override
		public final Collection<Collection<V>> values() {
			return this.data.values();
		}
		
		@Override
		public final String toString() {
			return this.data.toString();
		}
		
		/**
		 * 
		 * @param key
		 * <ul>
		 *  <li>IN</li>
		 *  <li>MAYBE_NULL</li>
		 * </ul>
		 * @return
		 * <ul>
		 *  <li>MAYBE_NEW</li>
		 *  <li>NOT_NULL</li>
		 * </ul>
		 */
		private final Collection<V> createCollection(final K key) {
			Collection<V> result = this.data.get(key);
			
			if (result == null) {
				try {
					this.data.put(key, result = this.collectionClass.newInstance());
				} catch (final Exception exception) {
					throw new RuntimeException(exception);
				}
			}
			return result;
		}
		
	}
	
}
