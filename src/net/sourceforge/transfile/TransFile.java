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

package net.sourceforge.transfile;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.ConsoleHandler;

import net.sourceforge.jmacadapter.MacAdapterTools;
import net.sourceforge.transfile.backend.Backend;
import net.sourceforge.transfile.settings.Settings;
import net.sourceforge.transfile.ui.UserInterface;
import net.sourceforge.transfile.ui.swing.SwingGUI;

/**
 * 
 * 
 * @author Martin Riedel
 *
 */
public class TransFile implements Runnable {
	
	/*
	 * The application title to be used throughout the program
	 */
	public static final String applicationTitle = "TransFile";
	
	/*
	 * Reference to the UserInterface used to asynchronously inform the user / user interface about state changes in the backend
	 */
	private UserInterface ui;
	
	/*
	 * Sets the application's title to be used in the automatically generated Mac OS X application menu
	 * and "About" menu item
	 * 
	 * This has to be done early, hence this static initializer
	 */
	static {
		MacAdapterTools.setApplicationName(TransFile.applicationTitle);
	}
	
	
	/**
	 * Creates a TransFile application object. Constructor is private
	 * because only the static main() method in this class should ever
	 * call it.
	 * 
	 */
	private TransFile() { 
		// do nothing, just prevent instantiation
	}
	
	/**
	 * Creates the TransFile application using the Swing GUI
	 * 
	 * @return the runnable TransFile application
	 */
	public static TransFile swingFactory() {
		TransFile app = new TransFile();
		
		SwingGUI gui = new SwingGUI();
		Backend backend = new Backend(gui);
		gui.setBackend(backend);
		
		app.ui = gui;
		
		return app;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		this.ui.start();
	}
	
	public static final String PACKAGE_NAME = TransFile.class.getPackage().getName();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			configureLogger();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TransFile application = swingFactory();
		application.run();
	}
	
	/**
	 * Sets up the base logger for the project (net.sourceforge.transfile) 
	 * 
	 * @throws IOException if an I/O error occurred while trying to access the log file
	 */
	private static final void configureLogger() throws IOException {
		
		//TODO let the user configure
		// - whether to log to console at all
		// - what log level to use for console logging
		
		// remove all default handlers
		for(Handler handler: Logger.getLogger("").getHandlers())
			Logger.getLogger("").removeHandler(handler);
		
		final Level logLevel = Level.parse(Settings.getPreferences().get("log_level", Settings.LOG_LEVEL.getName()));
		
		final Handler consoleHandler = new ConsoleHandler();
		final Handler fileHandler = new FileHandler(Settings.getPreferences().get("log_path", Settings.LOG_PATH.getAbsolutePath()));
		
		consoleHandler.setLevel(logLevel);
		fileHandler.setLevel(logLevel);
		fileHandler.setFormatter(new SimpleFormatter());
		
		Logger.getLogger(PACKAGE_NAME).addHandler(consoleHandler);
		Logger.getLogger(PACKAGE_NAME).addHandler(fileHandler);
		
		Logger.getLogger(PACKAGE_NAME).setLevel(logLevel);
	}
	
}
