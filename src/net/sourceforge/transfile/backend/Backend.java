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

package net.sourceforge.transfile.backend;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;

import net.sourceforge.transfile.network.NetworkTools;
import net.sourceforge.transfile.network.Peer;
import net.sourceforge.transfile.network.exceptions.BilateralConnectException;
import net.sourceforge.transfile.network.exceptions.PeerURLFormatException;


/**
 * Central, concrete Backend class - handles/organizes all logical operations
 * 
 * Backend should not perform any time-intensive operation on initialization (of itself or its aggregates).
 * 
 * @author Martin Riedel
 *
 */
public class Backend implements ControllableBackend {
	
	/*
	 * Reference to the user interface handling events triggered by the Backend
	 */
	private BackendEventHandler ui;
	
	/**
	 * Constructs a Backend object
	 * 
	 * @param ui a reference to the GUI in use, used to inform the UI about asynchronous events
	 */
	public Backend(final BackendEventHandler ui) {
		this.ui = ui;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void quit() {
		//TODO ...
		
		System.exit(0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String findExternalAddress() throws MalformedURLException, IOException {
		return NetworkTools.findExternalAddress();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> findLocalAddresses() throws SocketException {
		return NetworkTools.findLocalAddresses(false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> findLocalAddresses(final boolean ipv4Only) throws SocketException {
		return NetworkTools.findLocalAddresses(ipv4Only);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String makePeerURL(final String address, final int port) {
		return Peer.makePeerURL(address, port);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(final String remoteURL, final int localPort) 
			throws PeerURLFormatException, UnknownHostException, InterruptedException, BilateralConnectException {
		//TODO implement
	}

}
