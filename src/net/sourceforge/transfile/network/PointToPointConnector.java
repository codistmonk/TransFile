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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

import net.sourceforge.transfile.exceptions.LogicError;
import net.sourceforge.transfile.network.exceptions.ConnectBindException;
import net.sourceforge.transfile.network.exceptions.ConnectException;
import net.sourceforge.transfile.network.exceptions.ConnectIOException;
import net.sourceforge.transfile.network.exceptions.ConnectSocketConfigException;
import net.sourceforge.transfile.network.exceptions.ConnectSocketFailedToCloseException;
import net.sourceforge.transfile.network.exceptions.ConnectTimeoutException;

/**
 * TODO doc
 *
 * @author Martin Riedel
 *
 */
public class PointToPointConnector implements Connector {

	/*
	 * TODO doc
	 */
	private final Peer localPeer;

	/*
	 * TODO doc
	 */
	private final Peer remotePeer;
	
	
	/**
	 * 
	 * Constructs a new instance
	 * TODO doc
	 * @param localPeer
	 * @param remotePeer
	 */
	public PointToPointConnector(final Peer localPeer, final Peer remotePeer) {
		this.localPeer = localPeer;
		this.remotePeer = remotePeer;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Peer getLocalPeer() {
		return this.localPeer;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Peer getRemotePeer() {
		return this.remotePeer;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Connection connect() throws ConnectException, InterruptedException {
		final Socket socket = new Socket();
		final long startTime = System.currentTimeMillis();
		
		try {
			
			// bind to the local IP address and port
			try {
				socket.bind(this.localPeer.toInetSocketAddress());
			} catch(final IOException e) {
				throw new ConnectBindException(e);
			}
			
			try {
				socket.setReuseAddress(true);
			} catch(final SocketException e) {
				throw new ConnectSocketConfigException(e);
			}
						
			// attempt to connect until timed out
			while(true) {
				try {
					// attempt to connect, timing out after connectIntervalTimeout milliseconds to check for thread interruption
					socket.connect(this.remotePeer.toInetSocketAddress(), CONNECT_INTERVAL_TIMEOUT);
				} catch(final SocketTimeoutException e) {
					// check if this thread has been interrupted
					if(Thread.interrupted())
						throw new InterruptedException();
				} catch(final IOException e) {
					//TODO ?
					// ignore / retry until timeout
					//TODO log
				} catch(final IllegalBlockingModeException e) {
					//TODO throw a runtime exception instead
					throw new LogicError(e);
				} catch(final IllegalArgumentException e) {
					//TODO throw a runtime exception instead
					throw new LogicError(e);
				} 
				
				// check if a connection has been established
				if(socket.isConnected())
					break;

				if(System.currentTimeMillis() - startTime >= CONNECT_TIMEOUT)
					throw new ConnectTimeoutException();
			}
			
			return new Connection(socket, this.localPeer, this.remotePeer);
			
		} finally {
			
			// unless the connection was successfully established, close the socket if it exists
			if(!socket.isConnected()) {
				try {
					socket.close();
				} catch(IOException e) {
					throw new ConnectSocketFailedToCloseException(e);
				}
			}
			
		}
	}

}
