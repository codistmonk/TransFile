/*
 * Copyright � 2010 Martin Riedel
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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

import transfile.backend.BackendEventHandler;
import transfile.backend.ControllableBackend;
import transfile.gui.GUI;

/**
 * 
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
	 * Startup main window dimensions
	 */
	private final static int width = 340;
	private final static int height = 340;
	
	private NetworkPanel networkPanel;
	private StatusPanel statusPanel;
	
	/*
	 * All TopLevelPanels aggregated by SwingGUI must be added to this list.
	 * It's used to call some of he panels' event handlers, i.e. onInit() and onQuit();
	 */
	private final List<TopLevelPanel> panels = new ArrayList<TopLevelPanel>(2);
	
	/*
	 * All TopLevelPanels aggregated and currently visible must be present in this list.
	 * It is used to call some of the panels' event handlers, i.e. onShow() and onHide()
	 */
	private List<TopLevelPanel> activePanels = new LinkedList<TopLevelPanel>();
	
	
	public SwingGUI() {
		super(title);
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
		setNativeLookAndFeel();
		
		addWindowListener(new MainWindowListener());
		
		setBounds(300, 100, width, height);
		
		setup();
		
		showConnectScreen();
		
		setVisible(true);
		
		// tell all active panels to initialise
		for(TopLevelPanel panel: panels)
			panel.onInit();
	}
	
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
	 * Creates all GUI components
	 * 
	 */
	private void setup() {
		// set up menu bar
		
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		final JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		final JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		fileMenu.add(exitItem);
		
		// set up content pane
		
		final Container pane = getContentPane();
		
		pane.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		
		// "Network" panel
		
		networkPanel = new NetworkPanel(this, backend);
		panels.add(networkPanel);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		pane.add(networkPanel, c);
		activePanels.add(networkPanel);
		
		// "Status" panel
		
		statusPanel = new StatusPanel();
		panels.add(statusPanel);
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		pane.add(statusPanel, c);
		activePanels.add(statusPanel);
	}
	/**
	 * 	Makes this Swing GUI look like a native application on the respective OS it's running on
	 * 
	 */
	private void setNativeLookAndFeel() {
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ex) {
		  System.out.println("Unable to load native look and feel");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// use system menu bar on Mac OS X
		//TODO fine on windows/linux or surround with if System.getProperty("os.name")...?
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
	
	/**
	 * Quits the application
	 * 
	 */
	private void quit() {
		// inform all TopLevelPanels about the impending shutdown
		for(TopLevelPanel panel: panels)
			panel.onQuit();

		// tell the Backend to quit
		backend.quit();
	}
	
	/**
	 * Changes the main window so that it represents the screen where the user
	 * configures and initiates a connection.
	 * 
	 */
	private void showConnectScreen() {
		//TODO encapsulate the showing and hiding part of this code to a new class that
		// manages the TopLevelPanels (or somehow encapsulate it inside of SwingGUI, but encapsulate it)
		for(TopLevelPanel activePanel: activePanels) {
			if(activePanel != networkPanel && activePanel != statusPanel) {
				activePanel.onHide();
				activePanel.setVisible(false);
			}
		}
		
		activePanels.clear();
		
		//TODO only invoke onShow if the respective panel wasn't being shown at the time this method was invoked
		
		activePanels.add(networkPanel);
		networkPanel.onShow();
		
		activePanels.add(statusPanel);
		statusPanel.onShow();
	}
	
	private class MainWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			quit();
		}
	}

}
