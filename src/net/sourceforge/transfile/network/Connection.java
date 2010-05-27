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

package net.sourceforge.transfile.network;

import java.io.IOException;
import java.net.Socket;

import net.sourceforge.transfile.network.exceptions.ConnectionFailedToCloseException;

/**
 * TODO ...
 * 
 * @author Martin Riedel
 *
 */
class Connection {
	
	/*
	 * Local host for this connection 
	 */
	private final Peer localPeer;
	
	/*
	 * Remote host for this connection
	 */
	private final Peer remotePeer;
	
	/*
	 * The Socket that connects to the remote peer
	 */
	private final Socket socket;

	
	/**
	 * TODO doc
	 * 
	 */
	public final Peer getLocalPeer() {
		return this.localPeer;
	}
	
	/**
	 * TODO doc
	 * 
	 */
	public final Peer getRemotePeer() {
		return this.remotePeer;
	}
	
	/**
	 * TODO doc
	 * 
	 */
	public final boolean isConnected() {
		return this.socket.isConnected();
	}
	
	/**
	 * TODO doc
	 * @throws ConnectionFailedToCloseException 
	 * 
	 */
	public final void close() throws ConnectionFailedToCloseException {
		try {
			this.socket.close();
		} catch (final IOException e) {
			throw new ConnectionFailedToCloseException(e);
		}
	}
	
	/**
	 * TODO doc
	 * 
	 */
	@Override
	public final void finalize() {
		if(!this.socket.isClosed()) {
			try {
				close();
			} catch (ConnectionFailedToCloseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * TODO doc
	 * 
	 */
	Connection(final Socket socket, final Peer localPeer, final Peer remotePeer) {
		this.socket = socket;
		this.localPeer = localPeer;
		this.remotePeer = remotePeer;
	}
}
