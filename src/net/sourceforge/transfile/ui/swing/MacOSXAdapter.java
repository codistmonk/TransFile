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

package net.sourceforge.transfile.ui.swing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sourceforge.transfile.ui.swing.exceptions.MacOSXAdaptationException;


//TODO catch MacOSXAdaptationExceptions somewhere and handle them gracefully (log but don't exit)
//TODO (optional) generalise this as a generic reflective Java <-> Mac OS X application adapter

/**
 * Performs Mac-specific adaptations, like the use of the Mac OS X menu bar and the application menu.
 * 
 * Classes in the com.apple.eawt package are used reflectively in order to not require an import of said package on
 * platforms other than Mac OS X.
 * 
 * @author Martin Riedel
 *
 */
class MacOSXAdapter {
	
	/*
	 * Static instance holder
	 */
	private static MacOSXAdapter _instance = null;
	
	/*
	 * Back-reference to the SwingGUI instance
	 */
	private final SwingGUI gui;
	
	/*
	 * Represents the com.apple.eawt.ApplicationEvent class
	 */
	private Class<?> applicationEvent;
	
	/*
	 * Represents the com.apple.eawt.ApplicationEvent.setHandled(boolean) method
	 */
	private Method applicationEvent_setHandled;
	
	
	/**
	 * Performs Mac OS X specific adaptations to the running application
	 * 
	 * @param gui back-reference to the SwingGUI instance creating this object
	 */
	public static void adapt(final SwingGUI gui) {
		if(_instance != null)
			throw new IllegalStateException("adaptation already performed");
		
		// since the application title to be used in the mac os x application menu bar and for the 
		// "About" menu item has to be set early, so this is done in SwingGUI's static initializer
		
		_instance = new MacOSXAdapter(gui);
		
		// use system menu bar on Mac OS X
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
		_instance.makeApplicationListener();
	}
	
	/**
	 * Getter for {@code gui}
	 * 
	 * @return the GUI this MacOSXAdapater adapts to Mac OS X
	 */
	protected final SwingGUI getGUI() {
		return this.gui;
	}
	
	/**
	 * Getter for {@code applicationEvent}
	 * 
	 * @return an instance of Class representing {@link com.apple.eawt.ApplicationEvent}
	 */
	protected final Class<?> getApplicationEvent() {
		return this.applicationEvent;
	}
	
	/**
	 * Getter for {@code applicationEvent_setHandled}
	 * 
	 * @return an instance of Method representing {@link com.apple.eawt.ApplicationEvent#setHandled(boolean)}
	 */
	protected final Method getApplicationEvent_setHandled() {
		return this.applicationEvent_setHandled;
	}
	
	/**
	 * Constructs a new MacOSXAdapter 
	 * 
	 * @param gui back-reference to the SwingGUI instance creating this object
	 */
	private MacOSXAdapter(final SwingGUI gui) {
		this.gui = gui;
	}
	
	/**
	 * Creates a proxy instance implementing com.apple.eawt.ApplicationLister and registers it 
	 * with the com.apple.eawt.Application instance representing the application.
	 */
	private void makeApplicationListener() {
		try {
			// get Class instances for com.apple.eawt.Application, com.apple.eawt.ApplicationListener and com.apple.eawt.ApplicationEvent
			Class<?> application = Class.forName("com.apple.eawt.Application");
			Class<?> applicationListener = Class.forName("com.apple.eawt.ApplicationListener");
			this.applicationEvent = Class.forName("com.apple.eawt.ApplicationEvent");

			// get Method instances for the required methods of com.apple.eawt.Application
			Method application_getApplication = application.getMethod("getApplication", new Class<?>[] { });
			Method application_addApplicationListener = application.getMethod("addApplicationListener", new Class<?>[] { applicationListener } );
			Method application_setEnabledAboutMenu = application.getMethod("setEnabledAboutMenu", new Class<?>[] { boolean.class });
			Method application_setEnabledPreferencesMenu = application.getMethod("setEnabledPreferencesMenu", new Class<?>[] { boolean.class });
			
			// get and store a Method object for the com.apple.eawt.ApplicationEvent.setHandled(boolean) method
			this.applicationEvent_setHandled = this.applicationEvent.getMethod("setHandled", new Class<?>[] { boolean.class });

			// retrieve the com.apple.eawt.Application instance for this application
			Object applicationInstance = application_getApplication.invoke(null, new Object[] { });

			// invoke com.apple.eawt.Application.setEnabledAboutMenu(boolean)
			//TODO change argument to true when the About dialog is implemented
			application_setEnabledAboutMenu.invoke(applicationInstance, new Object[] { false });
			
			// invoke com.apple.eawt.Application.setEnabledPreferencesMenu(boolean)
			application_setEnabledPreferencesMenu.invoke(applicationInstance, new Object[] { true });
			
			// create a proxy instance of a proxy class implementing com.apple.eawt.ApplicationListener
			// see http://java.sun.com/javase/6/docs/api/java/lang/reflect/Proxy.html
			Object applicationListenerProxy = Proxy.newProxyInstance(applicationListener.getClassLoader(), 
																	new Class<?>[] { applicationListener },
																	new ApplicationListenerProxyInvocationHandler());
			
			// invoke com.apple.eawt.Application.addApplicationListener(com.apple.eawt.ApplicationListener),
			// passing it the previously created proxy instance implementing com.apple.eawt.ApplicationListener
			application_addApplicationListener.invoke(applicationInstance, new Object[] { applicationListenerProxy });
			
		} catch(Throwable e) {
			throw new MacOSXAdaptationException(e);
		}
	}
	
	/**
	 * InvocationHandler for the anonymous proxy class implementing com.apple.eawt.ApplicationListener used
	 * to reflectively create such an ApplicationListener.
	 * 
	 * 
	 * @see java.lang.reflect.InvocationHandler
	 * @see java.lang.reflect.Proxy
	 * @author Martin Riedel
	 *
	 */
	private class ApplicationListenerProxyInvocationHandler implements InvocationHandler  {

		/**
		 * Constructs a new instance
		 */
		public ApplicationListenerProxyInvocationHandler() {
			// do nothing, just allow instantiation
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			
			// all methods in com.apple.eawt.ApplicationListener accept exactly one argument
			if(args.length != 1)
				throw new MacOSXAdaptationException("method with an argument count != 1 invoked on the proxy class implementing com.apple.eawt.ApplicationListener");
			
			Object arg = args[0];
			
			// make sure the provided argument is an instance of com.apple.eawt.ApplicationEvent
			if(!(MacOSXAdapter.this.getApplicationEvent().isInstance(arg)))
				throw new MacOSXAdaptationException("method with an argument that is not an instance of com.apple.eawt.ApplicationEvent invoked on com.apple.eawt.ApplicationListener");
			
			// delegate to the corresponding event handler method
			if("handleAbout".equals(methodName)) {
				handleAbout(arg);
			} else if("handleOpenApplication".equals(methodName)) {
				handleOpenApplication(arg);
			} else if("handleOpenFile".equals(methodName)) {
				handleOpenFile(arg);
			} else if("handlePreferences".equals(methodName)) {
				handlePreferences(arg);
			} else if("handlePrintFile".equals(methodName)) {
				handlePrintFile(arg);
			} else if("handleQuit".equals(methodName)) {
				handleQuit(arg);
			} else if("handleReOpenApplication".equals(methodName)) {
				handleReOpenApplication(arg);
			} else {
				throw new MacOSXAdaptationException("unknown method \"" + methodName + "\" invoked on the proxy class implementing com.apple.eawt.ApplicationListener");
			}
			
			// all Methods in com.apple.eawt.ApplicationListener return void, so just return null here
			return null;
		}
		
		/*
		 * EVENT HANDLERS as in com.apple.eawt.ApplicationListener
		 * see http://developer.apple.com/mac/library/documentation/Java/Reference/JavaSE6_AppleExtensionsRef/api/com/apple/eawt/ApplicationListener.html
		 */
		
		/**
		 * Called when the user selects the "About" item in the application menu.
		 * 
		 * @param arg an instance of com.apple.eawt.ApplicationEvent representing the event being handled
		 */
		private void handleAbout(Object arg) {
			// keep Mac OS X default About dialog from being shown
			try {
				MacOSXAdapter.this.getApplicationEvent_setHandled().invoke(arg, new Object[] { true });
			} catch (Throwable e) {
				throw new MacOSXAdaptationException(e);
			} finally {
				MacOSXAdapter.this.getGUI().showAboutDialog();
			}
		}
		
		/**
		 * Called when the application receives an "open application" event from Finder or another application.
		 * 
		 * @param arg an instance of com.apple.eawt.ApplicationEvent representing the event being handled
		 */
		private void handleOpenApplication(Object arg) {
			// do nothing
		}
		
		/**
		 * Called when the application receives an "open document" event from Finder or another application.
		 * 
		 * @param arg an instance of com.apple.eawt.ApplicationEvent representing the event being handled
		 */
		private void handleOpenFile(Object arg) {
			// do nothing
		}
		
		/**
		 * Called when the application receives a request to print a file
		 *  
		 * @param arg an instance of com.apple.eawt.ApplicationEvent representing the event being handled
		 */
		private void handlePrintFile(Object arg) {
			// do nothing
		}
		
		/**
		 * Called when the user selects the "Preferences" item in the application menu.
		 * 
		 * @param arg an instance of com.apple.eawt.ApplicationEvent representing the event being handled
		 */
		private void handlePreferences(Object arg) {
			MacOSXAdapter.this.getGUI().showPreferences();
		}
		
		/**
		 * Called when the application receives a "quit" event.
		 * 
		 * @param arg an instance of com.apple.eawt.ApplicationEvent representing the event being handled
		 */
		private void handleQuit(Object arg) {
			MacOSXAdapter.this.getGUI().quit();
		}

		/**
		 * Called when the application receives a "reopen application" event from Finder or another application
		 * 
		 * @param arg an instance of com.apple.eawt.ApplicationEvent representing the event being handled
		 */
		private void handleReOpenApplication(Object arg) {
			// do nothing
		}
		
	}
	
}
