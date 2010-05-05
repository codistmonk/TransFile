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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sourceforge.transfile.backend.BackendEventHandler;
import net.sourceforge.transfile.backend.ControllableBackend;
import net.sourceforge.transfile.gui.GUI;
import net.sourceforge.transfile.gui.swing.exceptions.NativeLookAndFeelException;


/**
 * Main class of the Swing GUI for TransFile. Handles global events, aggregates GUI windows
 * and components and incorporates the application's main window.
 * 
 * @author Martin Riedel
 *
 */
public class SwingGUI extends JFrame implements GUI, BackendEventHandler {
	
	private static final long serialVersionUID = 3087671371254147452L;

	/*
	 * Reference to the Backend, which is responsible for all non-gui computation performed
	 */
	private ControllableBackend backend;
	
	/*
	 * Main window title
	 */
	private final static String title = "TransFile";
	
	/*
	 * References to the TopLevelPanels
	 */
	private NetworkPanel networkPanel;
	private StatusPanel statusPanel;

	/*
	 * List containing all TopLevelPanels
	 */
	private List<TopLevelPanel> panels = new LinkedList<TopLevelPanel>();
	
	/*
	 * True if running on Mac OS X
	 */
	private final boolean onMacOSX;
	
	
	/**
	 * Constructs a SwingGUI instance
	 * 
	 */
	public SwingGUI() {
		super(title);
		
		// check whether the application is running on Mac OS X and store the result
		onMacOSX = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBackend(final ControllableBackend controller) {
		this.backend = controller;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void start() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				_start();
			}
			
		});
	}
	
	/**
	 * Invoked when a connection to a peer has been established
	 * 
	 */
	void onConnectSuccessful() {
		setStatus("Connected");
		
		// TODO ...
	}
	
	/**
	 * Tells the main window to display the provided status (or error) message
	 * 
	 * @param status the message to display
	 */
	void setStatus(final String status) {
		statusPanel.setStatus(status);
	}
	
	/**
	 * Shows the "About" dialog
	 */
	void showAboutDialog() {
		//TODO implement
	}
	
	/**
	 * Shows the preferences window
	 */
	void showPreferences() {
		//TODO implement
	}
	
	/**
	 * Quits the application
	 * 
	 */
	void quit() {
		// inform all TopLevelPanels about the impending shutdown
		for(TopLevelPanel panel: panels)
			panel.informQuit();

		// tell the Backend to quit
		backend.quit();
	}
	
	/**
	 * Sets up and starts the Swing GUI. Should be invoked from the Swing event dispatch thread
	 * 
	 */
	private void _start() {
		try {
			setNativeLookAndFeel();
		} catch(NativeLookAndFeelException e) {
			showErrorDialog(e);
		}
		
		addWindowListener(new MainWindowListener());
		
		setup();
		
		showConnectScreen();
		
		setBounds(300, 200, 0, 0);
		setVisible(true);
	}
	
	/**
	 * Creates all GUI components
	 * 
	 */
	private void setup() {
		setupMenuBar();
		
		// set up content pane
		
		final Container pane = getContentPane();
		
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		
		// "Network" panel
		
		networkPanel = new NetworkPanel(this, backend);
		networkPanel.setPreferredSize(new Dimension(340, 300));
		panels.add(networkPanel);
		pane.add(networkPanel);
		
		// "Status" panel
		
		statusPanel = new StatusPanel();
		statusPanel.setPreferredSize(new Dimension(360, 20));
		panels.add(statusPanel);
		pane.add(statusPanel);
	}
	
	/**
	 * Creates the menu bar
	 * 
	 */
	private void setupMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		final JMenu fileMenu = new JMenu("File");
		
		// add the "Exit" item to the "File" menu, unless running on Mac OS (X) in which
		// case there is already a "Quit" item in the application menu
		if(!onMacOSX) {
			final JMenuItem exitItem = new JMenuItem("Exit");
			exitItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					quit();
				}
			});
			fileMenu.add(exitItem);
		}
		
		// show the "File" menu if it has at least one element
		if(fileMenu.getSubElements() != null)
			if(fileMenu.getSubElements().length > 0)
				menuBar.add(fileMenu);
	}
	
	/**
	 * 	Makes this Swing GUI look like a native application on the respective OS it's running on
	 * 
	 */
	private void setNativeLookAndFeel() throws NativeLookAndFeelException {
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			throw new NativeLookAndFeelException(e);
		}
		
		// Mac-specific adaptation
		if(onMacOSX)
			new MacOSXAdapter(this);
	}
	
	/**
	 * Changes the main window so that it represents the screen where the user
	 * configures and initiates a connection.
	 * 
	 */
	private void showConnectScreen() {
		Set<TopLevelPanel> visiblePanels = new HashSet<TopLevelPanel>(2);
		visiblePanels.add(networkPanel);
		visiblePanels.add(statusPanel);
		setVisiblePanels(visiblePanels);
	}
	
	/**
	 * Shows the panels contained in the provided set, hides all others
	 * 
	 * @param visiblePanels the panels to show
	 */
	private void setVisiblePanels(Set<TopLevelPanel> visiblePanels) {
		for(TopLevelPanel panel: panels) {
			if(visiblePanels.contains(panel))
				panel.showPanel();
			else
				panel.hidePanel();
		}
		
		// resize the main window to fit the currently active panels
		pack();
	}
	
	/**
	 * Shows a (more or less) human-readable, localized error dialog for the provided Throwable
	 * 
	 * @param t the Throwable to visualise as an error dialog
	 */
	private void showErrorDialog(final Throwable t) {	
		showErrorDialog(t.toString());
	}
	
	/**
	 * Shows an error dialog displaying the provided message 
	 * 
	 * @param errorMessage the error message to show
	 */
	private void showErrorDialog(final String errorMessage) {
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Listens for when the user closes the window
	 * 
	 * @author Martin Riedel
	 *
	 */
	private class MainWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			quit();
		}
	}

}
