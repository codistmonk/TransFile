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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
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
import net.sourceforge.transfile.operations.AbstractConnection;
import net.sourceforge.transfile.operations.Connection;
import net.sourceforge.transfile.operations.Connection.State;
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
	private JComboBox localIPAddressBox;
	private JTextField localInternetAddressField;
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
		
		this.getWindow().getSession().getConnection().addConnectionListener(new Connection.AbstractListener() {
			
			@Override
			protected final void doStateChanged() {
				final Connection connection = NetworkPanel.this.getWindow().getSession().getConnection();
				
				NetworkPanel.this.postErrorMessage(connection.getConnectionError());
				
				final State state = connection.getState();
				
				NetworkPanel.this.getConnectButton().setVisible(state == State.DISCONNECTED);
				NetworkPanel.this.getStopButton().setVisible(state != State.DISCONNECTED);
				
				if (state == State.CONNECTED) {
					NetworkPanel.this.getWindow().onConnectSuccessful();
				}
			}
			
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadState() {
		// load last entered local port (property always exists because there is a default)
		this.getLocalPort().setValue(Settings.getPreferences().getInt("local_port", Settings.LOCAL_PORT));
		
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
		Settings.getPreferences().put("local_port", this.getLocalPort().getValue().toString());
		
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
		return this.localInternetAddressField;
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
	 * Updates the ComboBox containing the local IP addresses so that it contains
	 * exactly the elements from the provided Set<String> of addresses
	 * 
	 */
	void updateLocalIPAddrBox() {
		this.localIPAddressBox.removeAllItems();
		
		if(this.localInternetIPAddress != null && !("".equals(this.localInternetIPAddress)))
			if(!(this.localLANAddresses.contains(this.localInternetIPAddress)))
				this.localIPAddressBox.addItem(this.localInternetIPAddress);
		
		for(String address: this.localLANAddresses)
			this.localIPAddressBox.addItem(address);
		
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
	void onUserActionSelectLocalAddress(final String selectedItem) {	
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
			this.getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_invalid_peerurl")));
			
			return;
		}
		
		this.getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("status_connecting")));
		
		this.getWindow().getSession().getConnection().connect();
	}
	
	/**
	 * TODO doc
	 * 
	 * @param throwable
	 * <br>Maybe null
	 * <br>Maybe shared
	 */
	final void postErrorMessage(final Throwable throwable) {
		if (throwable == null) {
			return;
		}
		
		try {
			throw throwable;
		} catch (final PeerURLFormatException exception) {
				getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_invalid_peerurl")));
				getLoggerForThisMethod().log(Level.INFO, "failed to connect", exception);
		} catch (final UnknownHostException exception) {
			getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_unknown_host")));
			getLoggerForThisMethod().log(Level.INFO, "failed to connect", exception);
		} catch (final IllegalStateException exception) {
			getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_illegal_state"), exception));
			getLoggerForThisMethod().log(Level.SEVERE, "failed to connect", exception);
		} catch (final BilateralConnectException exception) {
			// TODO...
			getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_no_link")));
			getLoggerForThisMethod().log(Level.INFO, "failed to connect", exception);
		} catch (final InterruptedException exception) {
			// ignore, situation handled by CancellationException
		} catch (final SocketTimeoutException exception) {
			getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_timeout")));
			getLoggerForThisMethod().log(Level.INFO, "failed to connect", exception);
		} catch (final Throwable throwable2) {
			throwable2.printStackTrace();
			getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("connect_fail_unknown")));
			getLoggerForThisMethod().log(Level.SEVERE, "failed to connect (unknown error)", throwable2);
		}
	}

	/**
	 * Invoked when the user interrupts a running connection attempt
	 * 
	 */
	final void onUserActionInterrupt() {
		this.getWindow().getSession().getConnection().disconnect();
	}
	
	final PeerURLBar getRemoteURLBar() {
		return this.remoteURLBar;
	}
	
	/*
	 * END USER ACTION EVENT HANDLERS
	 */
	
	/**
	 * Sets up the "Remote PeerURL" panel
	 * 
	 */
	private void setupRemoteURLPanel() {
		final int maxRetainedItems = Settings.getPreferences().getInt("peerurlbar_max_retained_items", Settings.PEERURLBAR_MAX_RETAINED_ITEMS);
		
		if (maxRetainedItems < 1) {
			throw new IllegalConfigValueException("peerurlbar_max_retained_items", Integer.toString(maxRetainedItems));
		}
		
		try {
			this.remoteURLBar = new PeerURLBar(Settings.getPreferences().get("remote_peerurlbar_state_file_name", Settings.REMOTE_PEERURLBAR_STATE_FILE_NAME), maxRetainedItems);
		} catch(SerializationFileInUseException e) {
			getLoggerForThisMethod().log(Level.WARNING, "remote PeerURLBar state file already in use, falling back to a non-persistent PeerURLBar", e);
			this.remoteURLBar = new PeerURLBar(maxRetainedItems);
		}
		
		final GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets = new Insets(5, 5, 5, 5);
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		
		final JPanel remoteURLDetails = new JPanel();
		
		GUITools.add(this.remoteURLPanel, new FoldableComponent(this.remoteURLBar, remoteURLDetails), constraints);
		
		final JTextField remoteIPTextField = this.createRemoteIPTextField();
		final PortSpinner remotePortSpinner = this.createRemotePortSpinner();
		
		gridBagTable(remoteURLDetails, new Component[][] {
				{ translate(new JLabel("label_remote_ip")), remoteIPTextField },
				{ translate(new JLabel("label_remote_port")), remotePortSpinner }
		});
		
		this.synchronizeConnectionWithRemoteURLBar();
	}
	
	private final JTextField createRemoteIPTextField() {
		final JTextField result = new JTextField();
		
		// TODO synchronize with model
		
		return result;
	}
	
	private final PortSpinner createRemotePortSpinner() {
		final PortSpinner result = new PortSpinner();
		
		// TODO synchronize with model
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @param result
	 * <br>Not null
	 * <br>Input-output
	 * @param components
	 * <br>Not null
	 * @return {@code result}
	 * <br>Not null
	 */
	private static final JPanel gridBagTable(final JPanel result, final Component[][] components) {
		final GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.gridy = 0;
		
		for (final Component[] row : components) {
			switch (row.length) {
			case 1:
				constraints.gridwidth = 2;
				constraints.gridx = 0;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.weightx = 1;
				
				GUITools.add(result, row[0], constraints);
				
				break;
			case 2:
				constraints.gridwidth = 1;
				constraints.gridx = 0;
				constraints.fill = GridBagConstraints.NONE;
				constraints.weightx = 0;
				
				GUITools.add(result, row[0], constraints);
				
				constraints.gridx = 1;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.weightx = 1;
				
				GUITools.add(result, row[1], constraints);
				
				break;
			case 3:
				throw new IllegalArgumentException("Each row must have 1 or 2 cells, but row " + constraints.gridy + " has " + row.length);
			}
			
			++constraints.gridy;
		}
		
		return result;
	}
	
	private final void synchronizeConnectionWithRemoteURLBar() {
		final Connection connection = NetworkPanel.this.getWindow().getSession().getConnection();
		
		this.getRemoteURLBar().addItemListener(new ItemListener() {
			
			@Override
			public final void itemStateChanged(final ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					connection.setRemotePeer(event.getItem().toString());
				}
			}
			
		});
		
		connection.addConnectionListener(new Connection.AbstractListener() {
			
			@Override
			protected final void doRemotePeerChanged() {
				NetworkPanel.this.getRemoteURLBar().setSelectedItem(connection.getRemotePeer());
			}
			
		});
		
		this.getRemoteURLBar().setSelectedItem(connection.getRemotePeer());
	}
	
	/**
	 * Sets up the "Local PeerURL" panel
	 * 
	 */
	private void setupLocalURLPanel() {
		this.localURLField = createLocalURLField();
		final JPanel localURLDetails = new JPanel();
		final GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		
		GUITools.add(this.getLocalURLPanel(), new FoldableComponent(this.localURLField, localURLDetails), constraints);
		
		this.localIPAddressBox = this.createLocalIPAddressBox();
		this.localInternetAddressField = createLocalInternetAddressField();
		this.localPort = this.createLocalPortSpinner();
		
		gridBagTable(localURLDetails, new Component[][] {
				{ translate(new JLabel("label_local_lan_addresses")), this.localIPAddressBox },
				{ translate(new JLabel("label_local_internet_address")), this.localInternetAddressField },
				{ translate(new JLabel("label_local_port")), this.getLocalPort() },
		});
		
		this.synchronizeConnectionWithLocalPort();
	}

	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	private PortSpinner createLocalPortSpinner() {
		final PortSpinner result = new PortSpinner();
		
		result.addChangeListener(new ChangeListener() {
			
			@Override
			public final void stateChanged(final ChangeEvent event) {
				NetworkPanel.this.onUserActionChangeLocalPort();
			}
			
		});
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	private static final JTextField createLocalInternetAddressField() {
		final JTextField result = new JTextField();
		
		result.setEditable(false);
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	private final JComboBox createLocalIPAddressBox() {
		final JComboBox result = new JComboBox();
		
		result.setEditable(false);
		result.addActionListener(new ActionListener() {

			@Override
			public final void actionPerformed(final ActionEvent event) {
				final JComboBox source = (JComboBox) event.getSource();
				final String selectedItem = (String) source.getSelectedItem();
				
				if (event.getActionCommand().equals("comboBoxChanged")) {
					if (selectedItem != null) {
						NetworkPanel.this.onUserActionSelectLocalAddress(selectedItem);
					}
				}
			}
			
		});
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>Not null
	 * <br>New
	 */
	private static final JTextField createLocalURLField() {
		final JTextField result = new JTextField();
		
		result.setEditable(false);
		
		return result;
	}
	
	private final void synchronizeConnectionWithLocalPort() {
		final Connection connection = NetworkPanel.this.getWindow().getSession().getConnection();
		
		this.getLocalPort().addChangeListener(new ChangeListener() {
			
			@Override
			public final void stateChanged(final ChangeEvent event) {
				final String[] protocolHostPort = AbstractConnection.getProtocolHostPort(connection.getLocalPeer());
				
				protocolHostPort[2] = NetworkPanel.this.getLocalPort().getValue().toString();
				
				connection.setLocalPeer(AbstractConnection.getPeer(protocolHostPort));
			}
			
		});
		
		connection.addConnectionListener(new Connection.AbstractListener() {
			
			@Override
			protected final void doLocalPeerChanged() {
				NetworkPanel.this.getLocalPort().setValue(AbstractConnection.getPort(connection.getLocalPeer()));
			}
			
		});
		
		this.getLocalPort().setValue(AbstractConnection.getPort(connection.getLocalPeer()));
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
	 * Updates the "Local PeerURL" field using the IP address and port selected by the user
	 * 
	 */
	private void updateLocalURL() {
		if(this.selectedLocalAddress == null || "".equals(this.selectedLocalAddress)) {
			this.localURLField.setText("N/A");
			return;
		}
		
		this.localURLField.setText(this.backend.makePeerURL(this.selectedLocalAddress, ((Number) this.getLocalPort().getValue()).intValue()));
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
			this.localIPAddressBox.setSelectedItem(ipToSelect);
		else
			if(ipToSelect.equals(this.localInternetIPAddress))
				this.localIPAddressBox.setSelectedItem(ipToSelect);	
		
		// selection events not to be processed by the 
		if(this.disregardNextLocalIPChange)
			this.disregardNextLocalIPChange = false;
	}

}