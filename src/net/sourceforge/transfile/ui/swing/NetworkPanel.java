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

import static net.sourceforge.transfile.i18n.Translator.Helpers.translate;
import static net.sourceforge.transfile.ui.swing.StatusService.StatusMessage;
import static net.sourceforge.transfile.tools.Tools.getLoggerForThisMethod;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.transfile.backend.ControllableBackend;
import net.sourceforge.transfile.exceptions.SerializationException;
import net.sourceforge.transfile.exceptions.SerializationFileInUseException;
import net.sourceforge.transfile.network.exceptions.BilateralConnectException;
import net.sourceforge.transfile.network.exceptions.PeerURLFormatException;
import net.sourceforge.transfile.operations.Connection;
import net.sourceforge.transfile.operations.Connection.State;
import net.sourceforge.transfile.operations.messages.Message;
import net.sourceforge.transfile.settings.Settings;
import net.sourceforge.transfile.settings.exceptions.IllegalConfigValueException;


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
	private boolean disregardNextLocalIPChange = false;
	
	/*
	 * GUI subpanels
	 */
	private JPanel remoteURLPanel;
	private JPanel localURLPanel;
	
	/*
	 * TODO doc
	 */
	private PeerURLBar remoteURLBar;
	
	/*
	 * TODO doc
	 */
	private PortSpinner localPort;
	
	/*
	 * Dynamic GUI elements
	 */
	private JLabel localLANAddrLabel;
	private JLabel localInternetAddrLabel;
	private JComboBox localIPAddrBox;
	private JTextField localInternetAddrField;
	private JTextField localURLField;
	private JButton connectButton;
	private JButton stopButton;

	
	/**
	 * Creates a NetworkPanel
	 * 
	 * @param window the main GUI class aggregating this NetworkPanel
	 * @param backend the backend to use
	 */
	public NetworkPanel(final SwingGUI window, final ControllableBackend backend) {
		super(window);
		
		this.backend = backend;
		
		this.setup();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onShow() {
		// do nothing
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onHide() {
		// do nothing
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onInit() {
		retrieveLocalInternetIPAddress();
		retrieveLocalLANAddresses();	
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onQuit() {
		// do nothing
	}

	private final void setup() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		this.remoteURLPanel = new JPanel();
		this.remoteURLPanel.setBorder(translate(BorderFactory.createTitledBorder("section_remote_peerurl")));
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 5);
		GUITools.add(this, this.remoteURLPanel, c);
		this.setupRemoteURLPanel();
		
		this.localURLPanel = new JPanel();
		this.localURLPanel.setBorder(translate(BorderFactory.createTitledBorder("section_local_peerurl")));
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 5);
		GUITools.add(this, this.localURLPanel, c);
		this.setupLocalURLPanel();
		
		this.connectButton = translate(new JButton("button_connect"));
		this.connectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkPanel.this.onUserActionConnect();
			}
			
		});
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		GUITools.add(this, this.connectButton, c);
		
		this.stopButton = translate(new JButton("button_interrupt_connect"));
		this.stopButton.setVisible(false);
		this.stopButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkPanel.this.onUserActionInterrupt();				
			}
			
		});
		GUITools.add(this, this.stopButton, c);
		
		this.getWindow().getSession().getConnection().addConnectionListener(new Connection.Listener() {
			
			@Override
			public final void stateChanged() {
				final State state = NetworkPanel.this.getWindow().getSession().getConnection().getState();
				
				NetworkPanel.this.getConnectButton().setVisible(state == State.DISCONNECTED);
				NetworkPanel.this.getStopButton().setVisible(state != State.DISCONNECTED);
				
				if (state == State.CONNECTED) {
					NetworkPanel.this.getWindow().onConnectSuccessful();
				}
			}
			
			@Override
			public final void messageReceived(final Message message) {
				// Do nothing
			}
			
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadState() {
		// load last entered local port (property always exists because there is a default)
		this.localPort.setValue(Settings.getPreferences().getInt("local_port", Settings.LOCAL_PORT));
		
		// selected local IP address is loaded in onLANAddressesDiscovered() (if applicable)
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	protected void saveState() {
		// save PeerURLBar state
		try {
			getLoggerForThisMethod().log(Level.FINER, "attempting to save PeerURLBar state");
			this.remoteURLBar.saveModel();
			getLoggerForThisMethod().log(Level.FINE, "successfully saved PeerURLBar state");
		} catch(final SerializationException e) {
			getLoggerForThisMethod().log(Level.WARNING, "failed to save PeerURLBar state", e);
		}
		
		// save local port
		Settings.getPreferences().put("local_port", this.localPort.getValue().toString());
		
		// save selected local IP address
		if(this.selectedLocalAddress != null && !("".equals(this.selectedLocalAddress))) {
			Settings.getPreferences().put("selected_local_ip", this.selectedLocalAddress);
		}
	}
	
	/**
	 * Getter for {@code backend}
	 * 
	 * @return the backend reference this NetworkPanel uses
	 */
	protected final ControllableBackend getBackend() {
		return this.backend;
	}
	
	/**
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	final JPanel getRemoteURLPanel() {
		return this.remoteURLPanel;
	}

	/**
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	final JPanel getLocalURLPanel() {
		return this.localURLPanel;
	}

	/**
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	final JButton getConnectButton() {
		return this.connectButton;
	}
	
	/**
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	final JButton getStopButton() {
		return this.stopButton;
	}
	
	/**
	 * Getter for {@code localLANAddresses}
	 *
	 * @return the local LAN addresses
	 */
	final Set<String> getLocalLANAddresses() {
		return this.localLANAddresses;
	}

	/**
	 * Setter for {@code localLANAddresses}
	 *
	 * @param localLANAddresses 
	 * <br />The local LAN addresses to set
	 * <br />Should not be null
	 */
	final void setLocalLANAddresses(Set<String> localLANAddresses) {
		this.localLANAddresses = localLANAddresses;
	}
	
	/**
	 * Getter for {@code localInternetAddrField}
	 *
	 * @return the local Internet address text field
	 */
	final JTextField getLocalInternetAddrField() {
		return this.localInternetAddrField;
	}

	/**
	 * Setter for {@code localInternetIPAddress}
	 *
	 * @param localInternetIPAddress
	 * <br />The local internet IP address to set
	 * <br />May be null
	 */
	final void setLocalInternetIPAddress(String localInternetIPAddress) {
		this.localInternetIPAddress = localInternetIPAddress;
	}

	/**
	 * Getter for {@code localInternetIPAddress}
	 *
	 * @return the local internet IP address
	 */
	final String getLocalInternetIPAddress() {
		return this.localInternetIPAddress;
	}

	/**
	 * Checks whether the next local IP address change will be disregarded in terms of updating the local
	 * addresses combobox's selected item
	 *
	 * @return whether the next local IP address change will be disregarded
	 */
	final boolean isDisregardNextLocalIPChange() {
		return this.disregardNextLocalIPChange;
	}

	/**
	 * Makes sure the next local IP address change is disregarded in terms of updating the local
	 * addresses combobox's selected item
	 *
	 * @param disregardNextLocalIPChange whether to disregard the next local IP address change
	 */
	final void setDisregardNextLocalIPChange(boolean disregardNextLocalIPChange) {
		this.disregardNextLocalIPChange = disregardNextLocalIPChange;
	}
	
	/**
	 * Getter for {@code localPort}
	 *
	 * @return the local port
	 */
	final PortSpinner getLocalPort() {
		return this.localPort;
	}
	
	/**
	 * Getter for {@code connectWorker}
	 *
	 * @return the worker thread trying to establish a connection, or null if none is active
	 */
	final SwingWorker<Void, Void> getConnectWorker() {
		return this.connectWorker;
	}

	/**
	 * Unsets the active worker thread for establishing a connection
	 *
	 */
	final void unsetConnectWorker() {
		this.connectWorker = null;
	}

	/**
	 * Updates the ComboBox containing the local IP addresses so that it contains
	 * exactly the elements from the provided Set<String> of addresses
	 * 
	 */
	void updateLocalIPAddrBox() {
		this.localIPAddrBox.removeAllItems();
		
		if(this.localInternetIPAddress != null && !("".equals(this.localInternetIPAddress)))
			if(!(this.localLANAddresses.contains(this.localInternetIPAddress)))
				this.localIPAddrBox.addItem(this.localInternetIPAddress);
		
		for(String address: this.localLANAddresses)
			this.localIPAddrBox.addItem(address);
		
		onLocalIPAddrBoxUpdated();
	}
	
	/**
	 * Shows the "Connect" button, hiding the "Stop" button
	 * 
	 */
	void showConnectButton() {
		this.connectButton.setVisible(true);
		this.stopButton.setVisible(false);
	}
	
	/**
	 * Invoked when a connection has been established successfully
	 * 
	 */
	void onConnectSuccessful() {
		//TODO cancel the SwingWorkers retrieving the local and local external IP addresses
		// if they're still running
		
		// inform the main GUI class
		getWindow().onConnectSuccessful();
	}
	
	/*
	 * BEGIN USER ACTION EVENT HANDLERS
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
	void onUserActionSelectLocalAddr(final String selectedItem) {	
		this.lastSelectedLocalAddress = this.selectedLocalAddress;
		this.selectedLocalAddress = selectedItem;
		
		updateLocalURL();
		
		if(!this.disregardNextLocalIPChange)
			if(!this.userHasSelectedALocalIP)
				this.userHasSelectedALocalIP = true;
	}

	/**
	 * Invoked when the user changes the local port
	 * 
	 */
	void onUserActionChangeLocalPort() {	
		updateLocalURL();
	}
	
	/**
	 * Invoked when the user initializes a connection attempt, i.e. by pressing the "Connect" button
	 * 
	 */
	final void onUserActionConnect() {
		final String remoteURL = (String) this.remoteURLBar.getSelectedItem();
		
		if(remoteURL == null || "".equals(remoteURL)) {
			getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_invalid_peerurl")));
			return;
		}
		
		getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("status_connecting")));
		
		this.getWindow().getSession().getConnection().setLocalPeer("transfile://0.0.0.0:" + this.localPort.getValue());
		this.getWindow().getSession().getConnection().setRemotePeer(remoteURL);
		this.getWindow().getSession().getConnection().connect();
		
		// TODO remove dead code
		
		if (true) {
			return;
		}
		
		showStopButton();
		
		this.connectWorker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				NetworkPanel.this.getBackend().connect(remoteURL, ((Number) NetworkPanel.this.getLocalPort().getValue()).intValue());
				return null;
			}
			
			@Override
			protected void done() {
				try {
					get();
					// if the flow reaches this / no exceptions are thrown, the connection has been established.
					onConnectSuccessful();
				} catch(CancellationException e) {
					getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("status_interrupted")));
				} catch(InterruptedException e) {
					//TODO when exactly does this happen. should be while the third thread
					// involved with this SwingWorker gets interrupted while waiting for get to
					// stop blocking - handle? if yes, how?
				} catch(ExecutionException e) {
					Throwable cause = e.getCause();
					
					if(cause instanceof PeerURLFormatException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_invalid_peerurl")));
						getLoggerForThisMethod().log(Level.INFO, "failed to connect", cause);
					} else if(cause instanceof UnknownHostException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_unknown_host")));
						getLoggerForThisMethod().log(Level.INFO, "failed to connect", cause);
					} else if(cause instanceof IllegalStateException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_illegal_state"), cause));
						getLoggerForThisMethod().log(Level.SEVERE, "failed to connect", cause);
					} else if(cause instanceof BilateralConnectException) {
						// TODO...
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_no_link")));
						getLoggerForThisMethod().log(Level.INFO, "failed to connect", cause);
					} else if(cause instanceof InterruptedException) {
						// ignore, situation handled by CancellationException
					} else {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_unknown")));
						getLoggerForThisMethod().log(Level.SEVERE, "failed to connect (unknown error)", cause);
					}
				} finally {
					// in any case, revert to default connection state
					// (either the connection attempt failed, or it succeeded and the panel should be reset for future use)
					NetworkPanel.this.unsetConnectWorker();
					showConnectButton();
				}
			}
			
		};
		
		this.connectWorker.execute();
	}

	/**
	 * Invoked when the user interrupts a running connection attempt
	 * 
	 */
	final void onUserActionInterrupt() {
		this.getWindow().getSession().getConnection().disconnect();
		
		// TODO remove dead code
		
		if (true) {
			return;
		}
		
		if (this.connectWorker == null) {
			return;
		}
		
		this.connectWorker.cancel(true);
	}
	
	/*
	 * END USER ACTION EVENT HANDLERS
	 */
	
	/**
	 * Sets up the "Remote PeerURL" panel
	 * 
	 */
	private void setupRemoteURLPanel() {
		this.remoteURLPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 5, 5, 5);
		
		c.gridx = 0;
		c.gridy = 0;
		
		final int maxRetainedItems = Settings.getPreferences().getInt("peerurlbar_max_retained_items", Settings.PEERURLBAR_MAX_RETAINED_ITEMS);
		if(maxRetainedItems < 1)
			throw new IllegalConfigValueException("peerurlbar_max_retained_items", Integer.toString(maxRetainedItems));
		
		try {
			this.remoteURLBar = new PeerURLBar(Settings.getPreferences().get("remote_peerurlbar_state_file_name", Settings.REMOTE_PEERURLBAR_STATE_FILE_NAME), maxRetainedItems);
		} catch(SerializationFileInUseException e) {
			getLoggerForThisMethod().log(Level.WARNING, "remote PeerURLBar state file already in use, falling back to a non-persistent PeerURLBar", e);
			this.remoteURLBar = new PeerURLBar(maxRetainedItems);
		}
		
		this.remoteURLPanel.add(this.remoteURLBar, c);		
	}
	
	/**
	 * Sets up the "Local PeerURL" panel
	 * 
	 */
	private void setupLocalURLPanel() {
		this.localURLPanel.setLayout(new GridBagLayout());
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
		
		this.localURLField = new JTextField();
		this.localURLField.setEditable(false);
		this.localURLPanel.add(this.localURLField, c);
		
		/*
		 * LEFT COLUMN
		 */
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.anchor = GridBagConstraints.LINE_START;
		
		this.localLANAddrLabel = translate(new JLabel("label_local_lan_addresses"));
		c.gridy = 1;
		this.localURLPanel.add(this.localLANAddrLabel, c);
		
		this.localInternetAddrLabel = translate(new JLabel("label_local_internet_address"));
		c.gridy = 2;
		this.localURLPanel.add(this.localInternetAddrLabel, c);
		
		JLabel localPortLabel = translate(new JLabel("label_local_port"));
		c.gridy = 3;
		this.localURLPanel.add(localPortLabel, c);
		
		/*
		 *  RIGHT COLUMN
		 */
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		
		this.localIPAddrBox = new JComboBox();
		this.localIPAddrBox.setEditable(false);
		this.localIPAddrBox.addActionListener(new ActionListener() {

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
		this.localURLPanel.add(this.localIPAddrBox, c);
		
		this.localInternetAddrField = new JTextField();
		this.localInternetAddrField.setEditable(false);
		
		c.gridy = 2;
		this.localURLPanel.add(this.localInternetAddrField, c);
		
		this.localPort = new PortSpinner();
		this.localPort.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				onUserActionChangeLocalPort();
			}
			
		});
		c.gridy = 3;
		this.localURLPanel.add(this.localPort, c);
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
				return NetworkPanel.this.getBackend().findExternalAddress();
			}

			@Override
			protected void done() {
				try {
					NetworkPanel.this.setLocalInternetIPAddress(get());
					NetworkPanel.this.getLocalInternetAddrField().setText(NetworkPanel.this.getLocalInternetIPAddress());
					NetworkPanel.this.setDisregardNextLocalIPChange(true);
					updateLocalIPAddrBox();
				} catch(InterruptedException e) {
					//TODO when exactly does this happen. should be while the third thread
					// involved with this SwingWorker gets interrupted while waiting for get to
					// stop blocking - handle? if yes, how?
				} catch(ExecutionException e) {
					Throwable cause = e.getCause();
					
					getLoggerForThisMethod().log(Level.WARNING, "failed to discover external IP address", cause);
					
					if(cause instanceof UnknownHostException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_unknown_host")));
						NetworkPanel.this.getLocalInternetAddrField().setText(translate("not_available"));
					} else if(cause instanceof MalformedURLException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_malformed_url"), cause));
						NetworkPanel.this.getLocalInternetAddrField().setText(translate("not_available"));
					} else if(cause instanceof IOException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_io_error")));
						NetworkPanel.this.getLocalInternetAddrField().setText(translate("not_available"));
					} else {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_unknown")));
						NetworkPanel.this.getLocalInternetAddrField().setText(translate("N/A"));
					}
				}
			}
			
		}.execute();	
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
				return NetworkPanel.this.getBackend().findLocalAddresses(true);
			}

			@Override
			protected void done() {
				try {
					NetworkPanel.this.setLocalLANAddresses(get());
					NetworkPanel.this.setDisregardNextLocalIPChange(true);
					updateLocalIPAddrBox();
				} catch(InterruptedException e) {
					//TODO when exactly does this happen. should be while the third thread
					// involved with this SwingWorker gets interrupted while waiting for get to
					// stop blocking - handle? if yes, how?
				} catch(ExecutionException e) {
					Throwable cause = e.getCause();
					
					getLoggerForThisMethod().log(Level.WARNING, "failed to discover local LAN addresses", cause);

					if(cause instanceof SocketException)
						NetworkPanel.this.getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_lan_sockets")));
					else
						NetworkPanel.this.getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_lan_unknown")));
				}
			}
			
		}.execute();	
	}
	
	/**
	 * Shows the "Stop" button, hiding the "Connect" button
	 * 
	 */
	private void showStopButton() {
		this.connectButton.setVisible(false);
		this.stopButton.setVisible(true);
	}
	
	/**
	 * Updates the "Local PeerURL" field using the IP address and port selected by the user
	 * 
	 */
	private void updateLocalURL() {
		if(this.selectedLocalAddress == null || "".equals(this.selectedLocalAddress)) {
			this.localURLField.setText("N/A");
			return;
		}
		
		this.localURLField.setText(this.backend.makePeerURL(this.selectedLocalAddress, ((Number) this.localPort.getValue()).intValue()));
	}
	
	/**
	 * Invoked whenever the JComboBox containing the local IP addresses is updated by the application
	 * 
	 */
	private void onLocalIPAddrBoxUpdated() {
		// if the user has selected an IP before during this execution of the application,
		// re-select that one (if still present). if not, use the one stored in Settings if there is one and the
		// IP is still present.
		String ipToSelect = this.userHasSelectedALocalIP ?
							this.lastSelectedLocalAddress :
							Settings.getPreferences().get("selected_local_ip", "");

		if(ipToSelect == null || "".equals(ipToSelect))
			return;

		if(this.localLANAddresses.contains(ipToSelect))
			this.localIPAddrBox.setSelectedItem(ipToSelect);
		else
			if(ipToSelect.equals(this.localInternetIPAddress))
				this.localIPAddrBox.setSelectedItem(ipToSelect);	
		
		// selection events not to be processed by the 
		if(this.disregardNextLocalIPChange)
			this.disregardNextLocalIPChange = false;
	}

}