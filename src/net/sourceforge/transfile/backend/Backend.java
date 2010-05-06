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

import net.sourceforge.transfile.network.Link;
import net.sourceforge.transfile.network.PeerURL;
import net.sourceforge.transfile.network.exceptions.LinkFailedException;
import net.sourceforge.transfile.network.exceptions.PeerURLFormatException;
import net.sourceforge.transfile.settings.Settings;


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
	
	/*
	 * Stores the active connection, if any.
	 * 
	 * Multiple simultaneous connections might be supported at a later point and could be implemented
	 * in the shape of a List<Connection>.
	 *  
	 */
	private Link connection = null;
	
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
		
		Settings.getInstance().save();
		
		System.exit(0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String findExternalAddress() throws MalformedURLException, IOException {
		return Link.findExternalAddress();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> findLocalAddresses() throws SocketException {
		return Link.findLocalAddresses(false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> findLocalAddresses(final boolean ipv4Only) throws SocketException {
		return Link.findLocalAddresses(ipv4Only);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String makePeerURLString(final String address, final int port) {
		return PeerURL.makePeerURLString(address, port);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(final String remoteURL, final int localPort) 
			throws PeerURLFormatException, UnknownHostException, InterruptedException, LinkFailedException {
		if(connection != null)
			throw new IllegalStateException("already connected to a peer");
		
		try {
			connection = new Link(remoteURL, localPort);
		} finally {
			// if something went wrong reset connection to null
			connection = null;
		}
	}

}
