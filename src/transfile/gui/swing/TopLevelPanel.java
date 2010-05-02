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

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * TopLevelPanels are the highest level components inside SwingGUI. SwingGUI only aggregates
 * TopLevelPanels, and there is bilateral communication between SwingGUI and the individual
 * TopLevelPanels to inform each other of major events.
 * 
 * TopLevelPanels typically encapsulate a particular feature of the application, i.e. the area
 * where the user configures and initiates a connection.
 * 
 * @author Martin Riedel
 *
 */
abstract class TopLevelPanel extends JPanel {

	private static final long serialVersionUID = -1438024844244777250L;
	
	/*
	 * The panel's title
	 */
	private final String title;

	
	/**
	 * Constructs a TopLevelPanel instance
	 * 
	 * @param title the title to be used for this TopLevelPanel
	 */
	public TopLevelPanel(final String title) {
		this.title = title;
		
		// create GUI elements
		setup();
	}
	
	/**
	 * Invoked just _after_ the GUI starts (after SwingGUI's call of setVisible)
	 * 
	 */
	abstract void onInit();
	
	/**
	 * Invoked just before the GUI exits. Should be used to store settings etc.
	 * 
	 */
	abstract void onQuit();
	
	/**
	 * Invoked when this panel goes from being hidden from the user by the GUI to being shown
	 * 
	 */
	abstract void onShow();
	
	/**
	 * Invoked when this panel goes from being shown to the user by the GUI to being hidden
	 * 
	 */
	abstract void onHide();
	
	/**
	 * Creates the panel's GUI elements
	 * 
	 */
	protected abstract void setup();
	
	/**
	 * Creates a titled border around the panel using the panel title
	 * 
	 */
	protected void titledBorder() {
		// use look and feel default border, adding the title
		setBorder(BorderFactory.createTitledBorder(title));		
	}

}
