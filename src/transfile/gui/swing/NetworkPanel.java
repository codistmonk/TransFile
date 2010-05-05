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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import transfile.backend.ControllableBackend;
import transfile.exceptions.SerializationException;
import transfile.network.exceptions.PeerURLFormatException;
import transfile.settings.Settings;

/**
 * The area where the user enters the remote PeerURL, selects their local port and ip address
 * and finally initiates a connection.
 * 
 * @author Martin Riedel
 *
 */
public class NetworkPanel extends TopLevelPanel {

	private static final long serialVersionUID = -6730149437479457030L;
	
	/*
	 * Back-reference to the main GUI class used to delegate events
	 */
	private final SwingGUI gui;
	
	/*
	 * Reference to the application backend 
	 */
	private final ControllableBackend backend;
	
	/*
	 * Represents the local IP address selected by the user or the application,
	 *  or null if the selected address is invalid / no address has been selected
	 */
	private String selectedLocalAddress = null;
	
	/*
	 * Represents the last local IP address selected before the current one 
	 */
	private String lastSelectedLocalAddress = null;
	
	/*
	 * Represents the local LAN IP addresses known to the application - or null if none have been found (yet)
	 */
	private Set<String> localLANAddresses = new HashSet<String>();
	
	/*
	 * Represents the local internet/external/public IP address - or null if it hasn't been discovered yet
	 */
	private String localInternetIPAddress = null;
	
	/*
	 * Represents the local Port selected by the user, or null if the port entered by the user is invalid
	 */
	private Integer localPort = null;
	
	/*
	 * The SwingWorker used to initiate a connection. Null whenever a connection attempt is not
	 * currently running.
	 */
	private SwingWorker<Void, Void> connectWorker = null;
	
	/*
	 * True if the user has selected a local IP address during this execution of the program
	 */
	private boolean userHasSelectedALocalIP = false;
	
	/*
	 * Set to true by SwingWorker done()-methods executed in the Swing event dispatch thread if they
	 * have an intention of performing any action on localIPAddrBox that would otherwise be interpreted 
	 * as the user selecting an item from the drop-down menu (or selecting an item via their keyboard).
	 */
	private boolean _disregardNextLocalIPChange = false;
	
	/*
	 * GUI subpanels
	 */
	private JPanel remoteURLPanel;
	private JPanel localURLPanel;
	
	/*
	 * Dynamic GUI elements
	 */
	private JLabel localLANAddrLabel;
	private JLabel localInternetAddrLabel;
	private PeerURLBar remoteURLBar;
	private JComboBox localIPAddrBox;
	private JTextField localInternetAddrField;
	private JTextField localPortField;
	private JTextField localURLField;
	private JButton connectButton;
	private JButton stopButton;
	
	
	/**
	 * Creates a NetworkPanel
	 * 
	 * @param gui the main GUI class aggregating this NetworkPanel
	 * @param backend the backend to use
	 */
	public NetworkPanel(final SwingGUI gui, final ControllableBackend backend) {
		super("Network");
		
		this.gui = gui;
		this.backend = backend;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void onShow() {
		// do nothing
	}
	
	/**
	 * {@inheritDoc}
	 */	
	protected void onHide() {
		// do nothing
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void onInit() {
		retrieveLocalInternetIPAddress();
		retrieveLocalLANAddresses();	
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void onQuit() {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setup() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		remoteURLPanel = new JPanel();
		remoteURLPanel.setBorder(BorderFactory.createTitledBorder("Remote PeerURL"));
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 5);
		add(remoteURLPanel, c);
		setupRemoteURLPanel();
		
		localURLPanel = new JPanel();
		localURLPanel.setBorder(BorderFactory.createTitledBorder("Local PeerURL"));
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 5);
		add(localURLPanel, c);
		setupLocalURLPanel();
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onUserActionConnect();
			}
		});
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(15, 5, 15, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		add(connectButton, c);
		
		stopButton = new JButton("Stop");
		stopButton.setVisible(false);
		stopButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onUserActionInterrupt();				
			}
		});
		add(stopButton, c);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void loadState() {
		// load last entered local port
		try {
			localPort = Integer.parseInt(Settings.getInstance().getProperty("local_port")); // always exists because there is a default
			localPortField.setText(localPort.toString());
		} catch(NumberFormatException e) {
			localPort = null;
			localPortField.setText("N/A");
		}
		
		// selected local IP address is loaded in onLANAddressesDiscovered() (if applicable)
	}
	
	/**
	 * {@inheritDoc} 
	 */
	protected void saveState() {
		// save PeerURLBar state
		try {
			PeerURLBar.getInstance().saveModel();
		} catch (SerializationException e) {
			//TODO LOG
			e.printStackTrace(); //TODO remove
		}
		
		// save local port
		String localPort = localPortField.getText();
		if(localPort != null && !("".equals(localPort)))
			Settings.getInstance().setProperty("local_port", localPortField.getText());
		
		// save selected local IP address
		if(selectedLocalAddress != null && !("".equals(selectedLocalAddress)))
			Settings.getInstance().setProperty("selected_local_ip", selectedLocalAddress);
	}
	
	/**
	 * Sets up the "Remote PeerURL" panel
	 * 
	 */
	private void setupRemoteURLPanel() {
		remoteURLPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 5, 5, 5);
		
		c.gridx = 0;
		c.gridy = 0;
		remoteURLBar = PeerURLBar.getInstance();
		remoteURLPanel.add(remoteURLBar, c);		
	}
	
	/**
	 * Sets up the "Local PeerURL" panel
	 * 
	 */
	private void setupLocalURLPanel() {
		localURLPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		
		/*
		 * GLOBAL
		 */
		c.insets = new Insets(5, 5, 5, 5);
		
		/*
		 * LOCAL PEERURL FIELD
		 */
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.anchor = GridBagConstraints.CENTER;
		
		localURLField = new JTextField();
		localURLField.setEditable(false);
		localURLPanel.add(localURLField, c);
		
		/*
		 * LEFT COLUMN
		 */
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.anchor = GridBagConstraints.LINE_START;
			
		localLANAddrLabel = new JLabel("Select local IP address: ");
		c.gridy = 1;
		localURLPanel.add(localLANAddrLabel, c);
		
		localInternetAddrLabel = new JLabel("Your internet IP address: ");
		c.gridy = 2;
		localURLPanel.add(localInternetAddrLabel, c);
		
		JLabel localPortLabel = new JLabel("Local Port: ");
		c.gridy = 3;
		localURLPanel.add(localPortLabel, c);
		
		/*
		 *  RIGHT COLUMN
		 */
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		
		localIPAddrBox = new JComboBox();
		localIPAddrBox.setEditable(false);
		localIPAddrBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				String selectedItem = (String) source.getSelectedItem();
				
				if(e.getActionCommand().equals("comboBoxChanged")) {
					if(selectedItem != null) {
						onUserActionSelectLocalAddr(selectedItem);	
					}
				}
			}
		});
		c.gridy = 1;
		localURLPanel.add(localIPAddrBox, c);
		
		localInternetAddrField = new JTextField();
		localInternetAddrField.setEditable(false);
		c.gridy = 2;
		localURLPanel.add(localInternetAddrField, c);
		
		localPortField = new JTextField();
		localPortField.setEditable(true);
		localPortField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				JTextField source = (JTextField) e.getSource();
				// if the port field loses focus while it's empty...
				if(source.getText() == null || "".equals(source.getText()))
					// set it to the last port the user entered if available/vald
					if(localPort != null)
						source.setText(localPort.toString());
					// or if not, set it to the port currently stored in Settings
					else
						source.setText(Settings.getInstance().getProperty("local_port"));
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				// do nothing
			}
		});
		localPortField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// do nothing
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				onUserActionChangeLocalPort();
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
				// do nothing
			}
		});
		c.gridy = 3;
		localURLPanel.add(localPortField, c);
	}
	
	/**
	 * Retrieves the local LAN IP addresses in a separate thread in order to not block GUI creation. 
	 * Also updates the localIPAddrBox from the Swing event dispatch thread after retrieving the necessary data
	 * 
	 */
	private void retrieveLocalLANAddresses() {
		new SwingWorker<Set<String>, Void>() {

			@Override
			protected Set<String> doInBackground() throws Exception {
				return backend.findLocalAddresses(true);
			}

			@Override
			protected void done() {
				try {
					localLANAddresses = get();
					_disregardNextLocalIPChange = true;
					updateLocalIPAddrBox();
				} catch(InterruptedException e) {
					//TODO when exactly does this happen. should be while the third thread
					// involved with this SwingWorker gets interrupted while waiting for get to
					// stop blocking - handle? if yes, how?
				} catch(ExecutionException e) {
					Throwable cause = e.getCause();
					if(cause instanceof SocketException) {
						gui.setStatus("failed to discover local LAN addresses: socket error");
					} else {
						gui.setStatus("failed to discover local LAN addresses: unknown error");
					}
				}
			}
			
		}.execute();	
	}
	
	/**
	 * Retrieves the local internet/external/public IP addresses in a separate thread in order to not block GUI creation. 
	 * Also updates the localIPAddrBox from the Swing event dispatch thread after retrieving the necessary data
	 * 
	 */
	private void retrieveLocalInternetIPAddress() {
		new SwingWorker<String, Void>() {

			@Override
			protected String doInBackground() throws Exception {
				return backend.findExternalAddress();
			}

			@Override
			protected void done() {
				try {
					localInternetIPAddress = get();
					localInternetAddrField.setText(localInternetIPAddress);
					_disregardNextLocalIPChange = true;
					updateLocalIPAddrBox();
				} catch(InterruptedException e) {
					//TODO when exactly does this happen. should be while the third thread
					// involved with this SwingWorker gets interrupted while waiting for get to
					// stop blocking - handle? if yes, how?
				} catch(ExecutionException e) {
					Throwable cause = e.getCause();
					if(cause instanceof UnknownHostException) {
						gui.setStatus("failed to discover external IP address: unknown host \"" + cause.getMessage() + "\"");
						localInternetAddrField.setText("N/A");
					} else if(cause instanceof MalformedURLException) {
						gui.setStatus("failed to discover external IP address: malformed URL: \"" + cause.getMessage() + "\"");
						localInternetAddrField.setText("N/A");
					} else if(cause instanceof IOException) {
						gui.setStatus("failed to discover external IP address: I/O error");
						localInternetAddrField.setText("N/A");
					} else {
						gui.setStatus("failed to discover external IP address: unexpected error");
						localInternetAddrField.setText("N/A");
						e.printStackTrace();
					}
				}
			}
			
		}.execute();	
	}
	
	/**
	 * Updates the ComboBox containing the local IP addresses so that it contains
	 * exactly the elements from the provided Set<String> of addresses
	 * 
	 */
	private void updateLocalIPAddrBox() {
		localIPAddrBox.removeAllItems();
		
		if(localInternetIPAddress != null && !("".equals(localInternetIPAddress)))
			if(!(localLANAddresses.contains(localInternetIPAddress)))
				localIPAddrBox.addItem(localInternetIPAddress);
		
		for(String address: localLANAddresses)
			localIPAddrBox.addItem(address);
		
		onLocalIPAddrBoxUpdated();
	}
	
	/**
	 * Updates the "Local PeerURL" field using the IP address and port selected by the user
	 * 
	 */
	private void updateLocalURL() {
		if(localPort == null || selectedLocalAddress == null || "".equals(selectedLocalAddress)) {
			localURLField.setText("N/A");
			return;
		}
		
		localURLField.setText(backend.makePeerURLString(selectedLocalAddress, localPort));
	}
	
	/**
	 * Shows the "Connect" button, hiding the "Stop" button
	 * 
	 */
	private void showConnectButton() {
		connectButton.setVisible(true);
		stopButton.setVisible(false);
	}
	
	/**
	 * Shows the "Stop" button, hiding the "Connect" button
	 * 
	 */
	private void showStopButton() {
		connectButton.setVisible(false);
		stopButton.setVisible(true);
	}
	
	/*
	 * INTERNAL EVENT HANDLERS
	 */
	
	/**
	 * Invoked when a connection has been established successfully
	 * 
	 */
	private void onConnectSuccessful() {
		//TODO cancel the SwingWorkers retrieving the local and local external IP addresses
		// if they're still running
		
		// inform the main GUI class
		gui.onConnectSuccessful();
	}
	
	/**
	 * Invoked whenever the JComboBox containing the local IP addresses is updated by the application
	 * 
	 */
	private void onLocalIPAddrBoxUpdated() {
		// if the user has selected an IP before during this execution of the application,
		// re-select that one (if still present). if not, use the one stored in Settings if there is one and the
		// IP is still present.
		String ipToSelect = userHasSelectedALocalIP ?
							lastSelectedLocalAddress :
							Settings.getInstance().getProperty("selected_local_ip");

		if(ipToSelect == null || "".equals(ipToSelect))
			return;

		if(localLANAddresses.contains(ipToSelect))
			localIPAddrBox.setSelectedItem(ipToSelect);
		else
			if(ipToSelect.equals(localInternetIPAddress))
				localIPAddrBox.setSelectedItem(ipToSelect);	
		
		// selection events not to be processed by the 
		if(_disregardNextLocalIPChange)
			_disregardNextLocalIPChange = false;
	}
	
	/*
	 * USER ACTION EVENT HANDLERS
	 */

	/**
	 * Invoked when the user selects a local IP address from the drop-down menu
	 * 
	 * ACTUALLY INVOKED WHENEVER THE SELECTED LOCAL IP ADDRESS CHANGES, even if the user didn't
	 * initiate it.
	 * 
	 * TODO RENAME TO onSelectedLocalAddressChanged or similar
	 * 
	 * @param selectedItem the IP address selected by the user
	 */
	private void onUserActionSelectLocalAddr(final String selectedItem) {	
		lastSelectedLocalAddress = selectedLocalAddress;
		selectedLocalAddress = selectedItem;
		
		updateLocalURL();
		
		if(!_disregardNextLocalIPChange)
			if(!userHasSelectedALocalIP)
				userHasSelectedALocalIP = true;
	}

	/**
	 * Invoked when the user changes the local port
	 * 
	 */
	private void onUserActionChangeLocalPort() {
		try {
			localPort = Integer.parseInt((String) localPortField.getText());
		} catch(NumberFormatException e) {
			// set local port to null to indicate an error in the port number entered by the user
			localPort = null;
		}
		
		updateLocalURL();
	}
	
	/**
	 * Invoked when the user initializes a connection attempt, i.e. by pressing the "Connect" button
	 * 
	 */
	private void onUserActionConnect() {
		final String remoteURL = (String) remoteURLBar.getSelectedItem();
		
		if(remoteURL == null || "".equals(remoteURL)) {
			gui.setStatus("Invalid PeerURL");
			return;
		}
		
		if(localPort == null) {
			gui.setStatus("Invalid local port");
			return;
		}
		
		showStopButton();
		
		gui.setStatus("Connecting...");
		
		connectWorker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				backend.connect(remoteURL, localPort);
				return null;
			}
			
			@Override
			protected void done() {
				try {
					get();
					// if the flow reaches this / no exceptions are thrown, the connection has been established.
					onConnectSuccessful();
				} catch(CancellationException e) {
					gui.setStatus("Connection attempt interrupted by user");
				} catch(InterruptedException e) {
					//TODO when exactly does this happen. should be while the third thread
					// involved with this SwingWorker gets interrupted while waiting for get to
					// stop blocking - handle? if yes, how?
				} catch(ExecutionException e) {
					Throwable cause = e.getCause();
					String connectFail = "Failed to connect: ";
					
					if(cause instanceof PeerURLFormatException) {
						gui.setStatus(connectFail + "invalid Peer URL");
					} else if(cause instanceof UnknownHostException) {
						gui.setStatus(connectFail + "unknown host");
					} else if(cause instanceof IOException) {
						gui.setStatus(connectFail + "I/O error");
					} else if(cause instanceof IllegalStateException) {
						gui.setStatus(connectFail + cause.getMessage());
					} else {
						gui.setStatus(connectFail + "unknown error");
						cause.printStackTrace();
					}
				} finally {
					// in any case, revert to default connection state
					// (either the connection attempt failed, or it succeeded and the panel should be reset for future use)
					connectWorker = null;
					showConnectButton();
				}
			}
			
		};
		
		connectWorker.execute();
	}
	
	/**
	 * Invoked when the user interrupts a running connection attempt
	 * 
	 */
	private void onUserActionInterrupt() {
		if(connectWorker == null)
			return;
		
		connectWorker.cancel(true);
	}

}