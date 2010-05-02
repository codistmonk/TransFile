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

package transfile.gui.swing;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Mac OS X ApplicationEvent handler. Allows for the handling of events fired from the auto-generated
 * Mac OS X application menu.
 * 
 * @author Martin Riedel
 *
 */
class MacOSXApplicationAdapter extends ApplicationAdapter {
	
	/*
	 * Back-reference to the SwingGUI instance
	 */
	private final SwingGUI gui;
	
	/**
	 * Constructs a MacOSXApplicationAdapter instance
	 * 
	 * @param gui back-reference to the SwingGUI instance creating this object
	 */
	public MacOSXApplicationAdapter(final SwingGUI gui) {
		this.gui = gui;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void handleQuit(final ApplicationEvent e)
	{
		gui.quit();
	}

	/**
	 * {@inheritDoc}
	 */
	public void handleAbout(final ApplicationEvent e)
	{
		// keep Mac OS X default About dialog from being shown
		e.setHandled(true);
		
		//TODO show About dialog
	}

	/**
	 * {@inheritDoc}
	 */
	public void handlePreferences(final ApplicationEvent e)
	{
		//TODO show Preferences window
	}	

}
