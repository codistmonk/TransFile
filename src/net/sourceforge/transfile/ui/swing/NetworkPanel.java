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
import net.sourceforge.transfile.network.exceptions.LinkFailedException;
import net.sourceforge.transfile.network.exceptions.PeerURLFormatException;
import net.sourceforge.transfile.settings.Settings;


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
	private boolean _disregardNextLocalIPChange = false;
	
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
	 * @param gui the main GUI class aggregating this NetworkPanel
	 * @param backend the backend to use
	 */
	public NetworkPanel(final SwingGUI window, final ControllableBackend backend) {
		super(window);
		
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
		remoteURLPanel.setBorder(translate(BorderFactory.createTitledBorder("section_remote_peerurl")));
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 5);
		add(remoteURLPanel, c);
		setupRemoteURLPanel();
		
		localURLPanel = new JPanel();
		localURLPanel.setBorder(translate(BorderFactory.createTitledBorder("section_local_peerurl")));
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 5);
		add(localURLPanel, c);
		setupLocalURLPanel();
		
		connectButton = translate(new JButton("button_connect"));
		connectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onUserActionConnect();
			}
		});
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		add(connectButton, c);
		
		stopButton = translate(new JButton("button_interrupt_connect"));
		stopButton.setVisible(false);
		stopButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onUserActionInterrupt();				
			}
		});
		add(stopButton, c);
		
//		SwingTranslator.getDefaultTranslator().autotranslate(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void loadState() {
		// load last entered local port (property always exists because there is a default)
		localPort.setValue(Settings.getPreferences().getInt("local_port", 0));
		
		// selected local IP address is loaded in onLANAddressesDiscovered() (if applicable)
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	protected void saveState() {
		final StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < PeerURLBar.getInstance().getItemCount(); ++i) {
			builder.append(PeerURLBar.getInstance().getItemAt(i)).append(',');
		}
		
		Settings.getPreferences().put("peerurlbar.state", builder.toString());
		
		// save local port
		Settings.getPreferences().put("local_port", this.localPort.getValue().toString());
		
		// save selected local IP address
		if(this.selectedLocalAddress != null && !("".equals(this.selectedLocalAddress))) {
			Settings.getPreferences().put("selected_local_ip", this.selectedLocalAddress);
		}
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
		
		localLANAddrLabel = translate(new JLabel("label_local_lan_addresses"));
		c.gridy = 1;
		localURLPanel.add(localLANAddrLabel, c);
		
		localInternetAddrLabel = translate(new JLabel("label_local_internet_address"));
		c.gridy = 2;
		localURLPanel.add(localInternetAddrLabel, c);
		
		JLabel localPortLabel = translate(new JLabel("label_local_port"));
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
		
		localPort = new PortSpinner();
		localPort.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				onUserActionChangeLocalPort();
			}
		});
		c.gridy = 3;
		localURLPanel.add(localPort, c);
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
					
					getLoggerForThisMethod().log(Level.WARNING, "failed to discover external IP address", cause);
					
					if(cause instanceof UnknownHostException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_unknown_host")));
						localInternetAddrField.setText(translate("not_available"));
					} else if(cause instanceof MalformedURLException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_malformed_url"), cause));
						localInternetAddrField.setText(translate("not_available"));
					} else if(cause instanceof IOException) {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_io_error")));
						localInternetAddrField.setText(translate("not_available"));
					} else {
						getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_discover_internet_unknown")));
						localInternetAddrField.setText(translate("N/A"));
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
		if(selectedLocalAddress == null || "".equals(selectedLocalAddress)) {
			localURLField.setText("N/A");
			return;
		}
		
		localURLField.setText(backend.makePeerURLString(selectedLocalAddress, ((Number) localPort.getValue()).intValue()));
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
		getWindow().onConnectSuccessful();
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
							Settings.getPreferences().get("selected_local_ip", "");

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
		updateLocalURL();
	}
	
	/**
	 * Invoked when the user initializes a connection attempt, i.e. by pressing the "Connect" button
	 * 
	 */
	private void onUserActionConnect() {
		final String remoteURL = (String) remoteURLBar.getSelectedItem();
		
		if(remoteURL == null || "".equals(remoteURL)) {
			getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("error_invalid_peerurl")));
			return;
		}
		
		showStopButton();
		
		getWindow().getStatusService().postStatusMessage(translate(new StatusMessage("status_connecting")));
		
		connectWorker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				backend.connect(remoteURL, ((Number) localPort.getValue()).intValue());
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
					} else if(cause instanceof LinkFailedException) {
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