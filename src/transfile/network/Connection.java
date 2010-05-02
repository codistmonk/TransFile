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

package transfile.network;

import java.net.Socket;

import transfile.network.exceptions.ConnectFailedToSetTimeoutException;
import transfile.network.exceptions.ConnectSocketFailedToCloseException;
import transfile.network.exceptions.ConnectIOException;
import transfile.network.exceptions.ConnectSecurityException;
import transfile.network.exceptions.ConnectTimeoutException;
import transfile.network.exceptions.ServerFailedToBindException;
import transfile.network.exceptions.ServerFailedToCloseException;
import transfile.settings.Settings;

/**
 * TODO ...
 * 
 * @author Martin Riedel
 *
 */
abstract class Connection {
	
	/*
	 * PeerURL representing the peer this Connection connects to
	 */
	private final PeerURL peer;
	
	/*
	 * True if the connection has been established successfully
	 */
	private boolean connected = false;
	
	//TODO handle NumberFormatExceptions due to non-integer values in config files here and elsewhere
	
	/*
	 * The maximum time in milliseconds for which both attempts to establish a connection from localhost to the remote peer
	 * and attempts to accept a connection from the remote peer to the local host block in between checking
	 * whether their respective threads have been interrupted
	 */
	protected static final int connectIntervalTimeout = Integer.parseInt(Settings.getInstance().getProperty("connect_interval_time"));
	
	/*
	 * The number of times those connection attempts (see above) check for thread interruption
	 * before considering themselves timed out
	 */
	protected static final int connectMaxIntervals = Integer.parseInt(Settings.getInstance().getProperty("connect_intervals"));
	
	/*
	 * The Socket that connects to the remote peer
	 */
	protected Socket socket = null;

	
	public Connection(final PeerURL peer) {
		this.peer = peer;
	}
	
	public final PeerURL getPeerURL() {
		return peer;
	}
	
	public final boolean isConnected() {
		return connected;
	}
	
	/**
	 * Blocks the calling thread until the connection has been established
	 * 
	 * @throws ServerFailedToBindException TODO ... 
	 * @throws ConnectFailedToSetTimeoutException TODO ...
	 * TODO ...
	 * 
	 */
	public abstract void establish() throws ConnectIOException, ConnectSecurityException, 
			ConnectTimeoutException, InterruptedException, ConnectSocketFailedToCloseException,
			ServerFailedToCloseException, ServerFailedToBindException, ConnectFailedToSetTimeoutException;
	
	public final void authenticate() {
		//TODO ...
	}
	
	protected final void setConnected(final boolean connected) {
		this.connected = connected;
	}
	
}
