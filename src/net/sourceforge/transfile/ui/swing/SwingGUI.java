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

import static net.sourceforge.transfile.i18n.Translator.createLocale;
import static net.sourceforge.transfile.i18n.Translator.getDefaultTranslator;
import static net.sourceforge.transfile.i18n.Translator.Helpers.translate;
import static net.sourceforge.transfile.tools.Tools.getLoggerForThisMethod;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sourceforge.jmacadapter.MacAdapterException;
import net.sourceforge.jmacadapter.MacAdapterTools;
import net.sourceforge.jmacadapter.eawtwrappers.Application;
import net.sourceforge.jmacadapter.eawtwrappers.ApplicationAdapter;
import net.sourceforge.jmacadapter.eawtwrappers.ApplicationEvent;
import net.sourceforge.transfile.backend.BackendEventHandler;
import net.sourceforge.transfile.backend.ControllableBackend;
import net.sourceforge.transfile.i18n.Translator;
import net.sourceforge.transfile.operations.ReceiveOperation;
import net.sourceforge.transfile.operations.Session;
import net.sourceforge.transfile.operations.SimpleSocketConnection;
import net.sourceforge.transfile.settings.Settings;
import net.sourceforge.transfile.settings.exceptions.IllegalConfigValueException;
import net.sourceforge.transfile.ui.UserInterface;
import net.sourceforge.transfile.ui.swing.StatusService.StatusMessage;
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
	private TransferPanel transferPanel;
	private StatusPanel statusPanel;
	
	/*
	 * List containing all TopLevelPanels
	 */
	private List<TopLevelPanel> panels = new LinkedList<TopLevelPanel>();
	
	/**
	 * Object managing the creation of operations.
	 */
	private final Session session;
	
	/**
	 * Constructs a new instance
	 * 
	 */
	public SwingGUI() {
		super(title);
		
		this.statusService = new StatusServiceProvider();
		
		this.session = createSession();
		
		this.setStartupLocale();
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final Session getSession() {
		return this.session;
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

			// suppressing the synthetic access warning for SwingGUI._start() in order
			// to not have to (visibly) expose the method to the entire package
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				SwingGUI.this.doStart();
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
	 * Packs the window and uses the resulting size as a minimum size, so that the user cannot
	 * make it any smaller.
	 * 
	 */
	@Override
	public void pack() {
		// unset minimum and maximum size so that pack can reduce window size if appropriate
		this.setMinimumSize(null);
		this.setMaximumSize(null);
				
		super.pack();
		
		this.setMinimumSize(this.getSize());
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.getSize().height));
	}

	/**
	 * Invoked when a connection to a peer has been established
	 * 
	 */
	final void onConnectSuccessful() {
		this.getStatusService().postStatusMessage(translate(new StatusMessage("status_connected")));
		
		this.showTransferScreen();
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
	final void quit() {
		// inform all TopLevelPanels about the impending shutdown
		for(TopLevelPanel panel: this.panels) {
			panel.informQuit();
		}
		
		this.getSession().getConnection().disconnect();
		
		if (this.backend != null) {
			this.backend.quit();
		}
	}
	
	/**
	 * Sets up and starts the Swing GUI. Should be invoked from the Swing event dispatch thread
	 * 
	 */
	private final void doStart() {
		GUITools.checkAWT();
		
		try {
			this.setNativeLookAndFeel();
		} catch (final NativeLookAndFeelException exception) {
			this.showErrorDialog(exception);
		}
		
		this.addWindowListener(this.new MainWindowListener());
		
		this.setup();
		
		this.showConnectScreen();
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this.statusService));
		
		this.statusService.postStatusMessage(translate(new StatusMessage("status_ready")));
		
		this.pack();
		this.enforceMaximumSize();
		
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
			//TODO this code is never reached as IllegalConfigValueException is never thrown
			// user set an invalid locale, fall back to host's default locale
			locale = getLocale();
			getLoggerForThisMethod().log(Level.WARNING, "failed to load user locale preference; illegal locale: " + e.getValue(), e);
			//TODO inform the user (i.e. dialog)
		}
		
		getDefaultTranslator().setLocale(locale);
	}
	
	/**
	 * Loads the user's selected locale from the user-specific config file
	 * @return
	 * <br>A possibly null value
	 * <br>A possibly new value
	 * 
	 * @throws IllegalConfigValueException if the "locale" setting is present but invalid
	 */
	private static final Locale loadLocale() {
		//TODO userLocaleSetting can never be null. Hence, the host default locale is never used.
		final String userLocaleSetting = Settings.getPreferences().get("locale", Settings.LOCALE.toString());
		
		if(userLocaleSetting == null || "".equals(userLocaleSetting)) {
			return null;
		}
		
		return createLocale(userLocaleSetting);	
	}
	
	/**
	 * Creates all GUI components
	 * 
	 */
	private void setup() {
		// Add a translator listener to update the display when the user changes the language
		getDefaultTranslator().addTranslatorListener(new Translator.Listener() {
			
			@Override
			public final void localeChanged(final Locale oldLocale, final Locale newLocale) {
				SwingGUI.this.repaint();
			}
			
		});
		
		this.setupMenuBar();
		
		// set up content pane
		
		final Container pane = getContentPane();
		
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		
		// "Network" panel
		
		this.networkPanel = new NetworkPanel(this, this.backend);
		this.panels.add(this.networkPanel);
		pane.add(this.networkPanel);
		
		// "Transfer" panel
		
		this.transferPanel = new TransferPanel(this);
		this.panels.add(this.transferPanel);
		pane.add(this.transferPanel);
		
		// "Status" panel
		
		this.statusPanel = new StatusPanel(this);
		this.statusPanel.setPreferredSize(new Dimension(360, 28));
		this.panels.add(this.statusPanel);
		pane.add(this.statusPanel);
	}
	
	/**
	 * Creates the menu bar
	 * 
	 */
	private final void setupMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		final JMenu instanceMenu = translate(new JMenu("menu_instance"));
		
		{
			final JMenuItem newInstanceItem = translate(new JMenuItem("menu_item_new_instance"));
			
			newInstanceItem.addActionListener(new ActionListener() {
				
				@Override
				public final void actionPerformed(final ActionEvent event) {
					final SwingGUI newSwingGUI = new SwingGUI();
					
					newSwingGUI.start();
					
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public final void run() {
							newSwingGUI.getSession().getConnection().setRemotePeer(SwingGUI.this.getSession().getConnection().getLocalPeer());
							newSwingGUI.getSession().getConnection().setLocalPeer(SwingGUI.this.getSession().getConnection().getRemotePeer());
						}
						
					});
				}
				
			});
			
			instanceMenu.add(newInstanceItem);
		}
		
		final JMenu fileMenu = translate(new JMenu("menu_file"));
		
		// add the "Exit" item to the "File" menu, unless running on Mac OS (X) in which
		// case there is already a "Quit" item in the application menu
		if(!MacAdapterTools.isMacOSX()) {
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
		if(!MacAdapterTools.isMacOSX()) {
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
		// show the "Instance" menu if it has at least one element
		addMenuIfNotEmpty(menuBar, instanceMenu);
		// show the "Settings" menu if it has at least one element
		addMenuIfNotEmpty(menuBar, settingsMenu);
	}
	
	/**
	 * 	Makes this Swing GUI look like a native application on the respective OS it's running on
	 * 
	 * @throws NativeLookAndFeelException if a native look and feel cannot be loaded
	 */
	private final void setNativeLookAndFeel() throws NativeLookAndFeelException {
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception exception) {
			throw new NativeLookAndFeelException(exception);
		}
		
		try {
			// Mac-specific adaptations
			if (MacAdapterTools.isMacOSX()) {
				MacAdapterTools.setUseScreenMenuBar(true);
				Application.getApplication().setEnabledAboutMenu(false);
				Application.getApplication().setEnabledPreferencesMenu(true);
				Application.getApplication().addApplicationListener(new MacOSXApplicationAdapter());
			}
		} catch (final MacAdapterException e) {
			//TODO handle properly
			getLoggerForThisMethod().warning("Exception in Mac OS X adapter");
		}
	}
	
	/**
	 * Enforces the window's maximum size
	 *  
	 */
	private void enforceMaximumSize() {
		this.addComponentListener(new ComponentAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void componentResized(final ComponentEvent event) {
				// enforce maximum height
				if (SwingGUI.this.getSize().height > SwingGUI.this.getMaximumSize().height) {
					SwingGUI.this.setSize(SwingGUI.this.getSize().width, SwingGUI.this.getMaximumSize().height);
				}
				
				// enforce maximum width
				if(SwingGUI.this.getSize().width > SwingGUI.this.getMaximumSize().width) {
					SwingGUI.this.setSize(SwingGUI.this.getMaximumSize().width, SwingGUI.this.getSize().height);
				}
			}
		});
	}
	
	/**
	 * Changes the main window so that it represents the screen where the user
	 * configures and initiates a connection.
	 * 
	 */
	private void showConnectScreen() {
		Set<TopLevelPanel> visiblePanels = new HashSet<TopLevelPanel>(2);
		
		visiblePanels.add(this.networkPanel);
		visiblePanels.add(this.statusPanel);
		
		setVisiblePanels(visiblePanels);
	}
	
	/**
	 * Changes the main window so that it represents the screen where the user
	 * sends and receives files through the previously established connection
	 * 
	 */
	private final void showTransferScreen() {
		final Set<TopLevelPanel> visiblePanels = new HashSet<TopLevelPanel>(3);
		
		visiblePanels.add(this.transferPanel);
		visiblePanels.add(this.statusPanel);
		
		this.setVisiblePanels(visiblePanels);
	}
	
	/**
	 * Shows the panels contained in the provided set, hides all others
	 * 
	 * @param visiblePanels the panels to show
	 */
	private final void setVisiblePanels(final Set<TopLevelPanel> visiblePanels) {
		for (TopLevelPanel panel : this.panels) {
			if (visiblePanels.contains(panel)) {
				panel.showPanel();
			}
			else {
				panel.hidePanel();
			}
		}
		
		// resize the main window to fit the currently active panels
		this.pack();
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
		
		/**
		 * Constructs a new instance
		 *
		 */
		public MainWindowListener() {
			// do nothing, just allow instantiation
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
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
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	static final Session createSession() {
		return new Session(new SimpleSocketConnection(), new DestinationFileProvider());
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-13)
	 *
	 */
	private static class DestinationFileProvider implements ReceiveOperation.DestinationFileProvider {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		public DestinationFileProvider() {
			// Do nothing
		}
		
		@Override
		public final File getDestinationFile(final String fileName) {
			final JFileChooser fileChooser = new JFileChooser();
			
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null) && fileChooser.getSelectedFile() != null) {
				return new File(fileChooser.getSelectedFile(), fileName);
			}
			
			return null;
		}
		
	}
	
	/**
	 * Mac OS X {@link ApplicationEvent} handler
	 *
	 * @author Martin Riedel
	 *
	 */
	private class MacOSXApplicationAdapter extends ApplicationAdapter {

		/**
		 * Constructs a new instance
		 *
		 */
		public MacOSXApplicationAdapter() {
			// do nothing, just allow non-synthetic constructor access by SwingGUI
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void handleAbout(final ApplicationEvent event) {
			SwingGUI.this.showAboutDialog();
			event.setHandled(true);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void handlePreferences(final ApplicationEvent event) {
			SwingGUI.this.showPreferences();
			event.setHandled(true);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void handleQuit(final ApplicationEvent event) {
			SwingGUI.this.quit();
		}
		
	}
	
}
