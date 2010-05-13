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

import static net.sourceforge.transfile.ui.swing.SwingTranslator.createLocale;
import static net.sourceforge.transfile.ui.swing.SwingTranslator.getDefaultTranslator;
import static net.sourceforge.transfile.ui.swing.SwingTranslator.Helpers.translate;
import static net.sourceforge.transfile.ui.swing.StatusService.StatusMessage;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
import net.sourceforge.transfile.settings.Settings;
import net.sourceforge.transfile.settings.exceptions.IllegalConfigValueException;
import net.sourceforge.transfile.ui.UserInterface;
import net.sourceforge.transfile.ui.swing.exceptions.NativeLookAndFeelException;


/**
 * Main class of the Swing GUI for TransFile. Handles global events, aggregates GUI windows
 * and components and incorporates the application's main window.
 * 
 * @author Martin Riedel
 *
 */
public class SwingGUI extends JFrame implements UserInterface, BackendEventHandler {
	
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
	 * Handles status messages
	 */
	private final StatusService statusService;
	
	/*
	 * References to the TopLevelPanels
	 */
	private NetworkPanel networkPanel;
	private StatusPanel statusPanel;
	private SendPanel sendPanel;
	private ReceivePanel receivePanel;

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
		
		this.statusService = new StatusServiceProvider();
		
		// check whether the application is running on Mac OS X and store the result
		onMacOSX = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
		
		setStartupLocale();
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
	 * 
	 * @return the {@link StatusService} handling status messages for this window
	 */
	public StatusService getStatusService() {
		return this.statusService;
	}
	
	/**
	 * Packs the window and uses the resulting size as a minimum size which will be
	 * enforced by the listener added in setup().
	 */
	public final void packAndSetMinimumSize() {
		this.pack();
		this.setMinimumSize(this.getSize());
	}
	
	/**
	 * Invoked when a connection to a peer has been established
	 * 
	 */
	void onConnectSuccessful() {
		getStatusService().postStatusMessage(translate(new StatusMessage("status_connected")));
		
		showTransferScreen();
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
		new PreferencesFrame(this).setVisible(true);
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
			this.setNativeLookAndFeel();
		} catch(NativeLookAndFeelException e) {
			this.showErrorDialog(e);
		}
		
		this.addWindowListener(this.new MainWindowListener());
		
		this.setup();
		
		this.showConnectScreen();
		
		this.statusService.postStatusMessage(translate(new StatusMessage("status_ready")));
		
		this.packAndSetMinimumSize();
		
		// Center the window on the screen and show it
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 *	Sets the initial locale during startup
	 * 
	 */
	private void setStartupLocale() {
		Locale locale;
		
		try {
			// load locale from user settings file
			locale = loadLocale();
			// if not present, use the host's default locale (and implicitly fall back to en_US if the host's
			// default locale is unsupported
			if(locale == null)
				locale = getLocale();
		} catch(final IllegalConfigValueException e) {
			// user set an invalid locale, fall back to host's default locale
			locale = getLocale();
			//TODO log: value e.getValue() illegal for key e.getKey()
			//TODO inform the user (i.e. dialog)
		}
		
		getDefaultTranslator().setLocale(locale);
	}
	
	/**
	 * Loads the user's selected locale from the user-specific config file
	 * 
	 * @throws IllegalConfigValueException if the "locale" setting is present but invalid
	 */
	private static Locale loadLocale() {
		String userLocaleSetting = Settings.getInstance().getProperty("locale");

		if(userLocaleSetting == null || "".equals(userLocaleSetting))
			return null;

		return createLocale(userLocaleSetting);	
	}
	
	/**
	 * Creates all GUI components
	 * 
	 */
	private void setup() {
		// Add a translator listener to update the display when the user changes the language
		getDefaultTranslator().addTranslatorListener(new SwingTranslator.Listener() {
			
			@Override
			public final void localeChanged(final Locale oldLocale, final Locale newLocale) {
				SwingGUI.this.repaint();
			}
			
		});
		
		setupMenuBar();
		
		// set up content pane
		
		final Container pane = getContentPane();
		
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		
		// "Network" panel
		
		networkPanel = new NetworkPanel(this, backend);
		networkPanel.setPreferredSize(new Dimension(340, 300));
		panels.add(networkPanel);
		pane.add(networkPanel);
		
		// "Send Files" panel
		
		sendPanel = new SendPanel(this);
		sendPanel.setPreferredSize(new Dimension(340, 150));
		panels.add(sendPanel);
		pane.add(sendPanel);
		
		// "Receive Files" panel
		
		receivePanel = new ReceivePanel(this);
		receivePanel.setPreferredSize(new Dimension(340, 150));
		panels.add(receivePanel);
		pane.add(receivePanel);
		
		// "Status" panel
		
		statusPanel = new StatusPanel(this);
		statusPanel.setPreferredSize(new Dimension(360, 20));
		panels.add(statusPanel);
		pane.add(statusPanel);
		
		// Add a resize listener to prevent the window from becoming too small
		// The minimum size must be set after calling pack() (cf _start())
		this.addComponentListener(new ComponentAdapter() {
			
			@Override
			public final void componentResized(final ComponentEvent event) {
				SwingGUI.this.setSize(
						Math.max(SwingGUI.this.getWidth(), SwingGUI.this.getMinimumSize().width),
						Math.max(SwingGUI.this.getHeight(), SwingGUI.this.getMinimumSize().height));
			}
			
		});
	}
	
	/**
	 * Creates the menu bar
	 * 
	 */
	private final void setupMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		final JMenu fileMenu = translate(new JMenu("menu_file"));
		
		// add the "Exit" item to the "File" menu, unless running on Mac OS (X) in which
		// case there is already a "Quit" item in the application menu
		if(!this.onMacOSX) {
			final JMenuItem exitItem = translate(new JMenuItem("menu_item_exit"));
			
			exitItem.addActionListener(new ActionListener() {
				
				@Override
				public final void actionPerformed(final ActionEvent event) {
					SwingGUI.this.quit();
				}
				
			});
			
			fileMenu.add(exitItem);
		}
		
		final JMenu settingsMenu = translate(new JMenu("menu_settings"));
		
		// add the "Preferences..." item to the "Settings" menu, unless running on Mac OS (X) in which
		// case there is already a "Preferences..." item in the application menu
		if(!this.onMacOSX) {
			final JMenuItem preferencesItem = translate(new JMenuItem("menu_item_preferences"));
			
			preferencesItem.addActionListener(new ActionListener() {
				
				@Override
				public final void actionPerformed(final ActionEvent event) {
					SwingGUI.this.showPreferences();
				}
				
			});
			
			settingsMenu.add(preferencesItem);
		}
		
		// show the "File" menu if it has at least one element
		addMenuIfNotEmpty(menuBar, fileMenu);
		// show the "Settings" menu if it has at least one element
		addMenuIfNotEmpty(menuBar, settingsMenu);
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
		
		// Mac-specific adaptations
		if(onMacOSX)
			MacOSXAdapter.adapt(this);
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
	 * Changes the main window so that it represents the screen where the user
	 * sends and receives files through the previously established connection
	 * 
	 */
	private void showTransferScreen() {
		Set<TopLevelPanel> visiblePanels = new HashSet<TopLevelPanel>(3);
		
		visiblePanels.add(sendPanel);
		visiblePanels.add(receivePanel);
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
		JOptionPane.showMessageDialog(this, errorMessage, translate("Error"), JOptionPane.ERROR_MESSAGE);
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
	
	/**
	 * Adds {@code menu} to {@code menuBar} if {@code menu} contains at least one sub element.
	 * @param menuBar
	 * <br>Should not be null
	 * @param menu
	 * <br>Should not be null
	 * <br>Reference parameter
	 */
	private static final void addMenuIfNotEmpty(final JMenuBar menuBar, final JMenu menu) {
		if (menu.getSubElements() != null && menu.getSubElements().length > 0) {
			menuBar.add(menu);
		}
	}
	
}
