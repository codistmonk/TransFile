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

import java.util.ResourceBundle;

import javax.swing.JPanel;

import net.sourceforge.transfile.gui.swing.exceptions.DoubleInitializationException;


/**
 * TopLevelPanels are the highest level components inside SwingGUI. SwingGUI only aggregates
 * TopLevelPanels, and there is bilateral communication between SwingGUI and the individual
 * TopLevelPanels to inform each other of major events.
 * 
 * TopLevelPanels typically encapsulate a particular feature of the application, i.e. the area
 * where the user configures and initiates a connection.
 * 
 * TopLevelPanels should NOT perform any expensive operations in their constructors 
 * and should instead use their onInit() methods for that.
 * 
 * @author Martin Riedel
 *
 */
abstract class TopLevelPanel extends JPanel {

	private static final long serialVersionUID = -1438024844244777250L;
	
	/*
	 * Back-reference to the SwingGUI instance aggregating this TopLevelPanel
	 */
	private final SwingGUI window;
	
	/*
	 * True if this panel has been initialized
	 */
	private boolean isInit = false;
	
	/*
	 * True when the panel is currently being shown to the user
	 */
	private boolean isShown = false;
	
	/**
	 * Constructs a TopLevelPanel instance
	 * 
	 * @param title the title to be used for this TopLevelPanel
	 */
	public TopLevelPanel(final SwingGUI window) {
		this.window = window;
		
		// inherit the main window's locale
		setLocale(window.getLocale());
		
		// create GUI elements
		setup();
		
		// by default, TopLevelPanels are hidden
		setVisible(false);
	}
	
	/**
	 * 
	 * @return true if the panel has been initialized
	 */
	public final boolean isInitialized() {
		return isInit;
	}
	
	/**
	 * 
	 * @return true if the panel is currently being shown
	 */
	public final boolean isShown() {
		return isShown;
	}
	
	/**
	 * Tells the panel to initialize and perform all expensive initialization operations
	 * 
	 */
	final void initPanel() {
		if(isInit)
			throw new DoubleInitializationException("TopLevelPanel was asked to initialize a second time: " + getClass().getSimpleName());
		
		onInit();
	}
	
	/**
	 * Shows this panel, initializing it if it hasn't been initialized. Should be invoked as late as
	 * possible. Does nothing if the panel is already visible.
	 * 
	 */
	final void showPanel() {
		if(isShown)
			return;
		
		if(!isInit)
			initPanel();
		
		loadState();
		onShow();
		setVisible(true);
	}
	
	/**
	 * Hides this panel. Does nothing if the panel is already hidden.
	 * 
	 */
	final void hidePanel() {
		if(!isShown)
			return;
		
		setVisible(false);
		onHide();
	}
	
	/**
	 * Informs the panel that the application is quitting
	 * 
	 */
	final void informQuit() {
		saveState();
		onQuit();
	}
	
	/**
	 * Returns a reference to the SwingGUI instance aggregating this TopLevelPanel
	 * 
	 * @return a reference to the Swing window this TopLevelPanel belongs to
	 */
	final protected SwingGUI getWindow() {
		return window;
	}
	
	/**
	 * Invoked just before the GUI exits. State should not be saved here but in
	 * {@link #saveState()}.
	 * 
	 */
	protected abstract void onQuit();
	
	/**
	 * Invoked when the panel is asked to initialize. Any expensive initialization operations
	 * should be performed here. State should not be loaded here but in {@link #loadState()}.
	 * 
	 */
	protected abstract void onInit();
	
	/**
	 * Invoked when this panel goes from being hidden from the user by the GUI to being shown.
	 * State will be loaded via {@link #loadState()} BEFORE this method is invoked.
	 * 
	 */
	protected abstract void onShow();
	
	/**
	 * Invoked when this panel goes from being shown to the user by the GUI to being hidden
	 * 
	 */
	protected abstract void onHide();
	
	/**
	 * Creates the panel's GUI elements
	 * 
	 */
	protected abstract void setup();
	
	/**
	 * Loads state/settings
	 * 
	 */
	protected abstract void loadState();
	
	/**
	 * Saves state/settings
	 * 
	 */
	protected abstract void saveState();

}
