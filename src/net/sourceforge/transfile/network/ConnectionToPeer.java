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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

import net.sourceforge.transfile.exceptions.LogicError;
import net.sourceforge.transfile.network.exceptions.ConnectFailedToSetTimeoutException;
import net.sourceforge.transfile.network.exceptions.ConnectIOException;
import net.sourceforge.transfile.network.exceptions.ConnectSocketFailedToCloseException;
import net.sourceforge.transfile.network.exceptions.ConnectTimeoutException;


/**
 * TODO ...
 * 
 * @author Martin Riedel
 *
 */
class ConnectionToPeer extends Connection {

	/**
	 * 
	 * TODO doc
	 */
	public ConnectionToPeer(final PeerURL peer) {
		super(peer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void establish() throws ConnectIOException, InterruptedException, ConnectTimeoutException, 
			ConnectSocketFailedToCloseException, ConnectFailedToSetTimeoutException {	
		InetSocketAddress peerAddr = getPeerURL().toInetSocketAddress();
				
		try {
			// attempt to connect for a maximum of connectMaxIntervals times
			for(int i = 0; ; i++) {
				try {
					socket = new Socket();
					// attempt to connect, timing out after connectIntervalTimeout milliseconds to check for thread interruption
					socket.connect(peerAddr, connectIntervalTimeout);
				} catch(SocketTimeoutException e) {
					if(Thread.interrupted())
						throw new InterruptedException();
				} catch(IOException e) {
					throw new ConnectIOException(e);	
				} catch(IllegalBlockingModeException e) {
					throw new LogicError(e);
				} catch(IllegalArgumentException e) {
					throw new LogicError(e);
				} 
				
				// check if a connection has been established
				if(socket.isConnected())
					break;

				// check if the maximum number of connection attempts is reached and time out the entire operation if so
				if(i >= connectMaxIntervals - 1)
					throw new ConnectTimeoutException();
			}
			setConnected(true);
		} finally {
			// unless the connection was successfully established, close the socket if it exists
			if(!isConnected()) {
				if(socket != null) {
					try {
						socket.close();
					} catch(IOException e) {
						throw new ConnectSocketFailedToCloseException(e);
					}
				}
			}
		}
	}

}
