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

package transfile;

import transfile.backend.Backend;
import transfile.gui.GUI;
import transfile.gui.swing.SwingGUI;

/**
 * 
 * 
 * @author Martin Riedel
 *
 */
public class TransFile implements Runnable {
	
	/*
	 * Reference to the GUI used to asynchronously inform the GUI about state changes
	 */
	private GUI gui;
	
	/**
	 * Creates a TransFile application object. Constructor is private
	 * because only the static main() method in this class should ever
	 * call it.
	 * 
	 */
	private TransFile() {}
	
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
		
		app.gui = gui;
		
		return app;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		gui.start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TransFile application = swingFactory();
		application.run();
	}

}
