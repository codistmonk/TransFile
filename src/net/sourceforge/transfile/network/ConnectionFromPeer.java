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

import static net.sourceforge.transfile.tools.Tools.getLoggerForThisMethod;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

import net.sourceforge.transfile.exceptions.LogicError;
import net.sourceforge.transfile.network.exceptions.ConnectFailedToSetTimeoutException;
import net.sourceforge.transfile.network.exceptions.ConnectIOException;
import net.sourceforge.transfile.network.exceptions.ConnectSecurityException;
import net.sourceforge.transfile.network.exceptions.ConnectSocketFailedToCloseException;
import net.sourceforge.transfile.network.exceptions.ConnectTimeoutException;
import net.sourceforge.transfile.network.exceptions.ServerFailedToBindException;
import net.sourceforge.transfile.network.exceptions.ServerFailedToCloseException;


/**
 * TODO ...
 * 
 * @author Martin Riedel
 *
 */
class ConnectionFromPeer extends Connection {
	
	/*
	 * TODO doc
	 */
	private final FutureTask<Socket> peerConnectionAcceptor;

	/**
	 * 
	 * TODO doc
	 */
	public ConnectionFromPeer(final PeerURL peer, final int localPort) {
		super(peer);
		
		if(localPort <= 0 || localPort > 65535)
			throw new LogicError("invalid local server port number");
		
		this.peerConnectionAcceptor = new FutureTask<Socket>(new ServerTask(localPort, peer));
	}

	/**
	 * 
	 * TODO doc
	 */
	public void establishInBackground() {
		(new Thread(this.peerConnectionAcceptor)).start();	
	}

	/**
	 * 
	 * TODO doc
	 */
	public void interruptBackgroundTask() {
		this.peerConnectionAcceptor.cancel(true);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void establish() throws InterruptedException, ConnectIOException, ConnectTimeoutException, 
			ConnectSocketFailedToCloseException, ServerFailedToCloseException, ServerFailedToBindException, 
			ConnectSecurityException, ConnectFailedToSetTimeoutException {
		try {
			this.socket = this.peerConnectionAcceptor.get();
		} catch(ExecutionException e) {
			// listening for an incoming connection from the peer failed
			Throwable cause = e.getCause();
			if(cause instanceof ConnectIOException) {
				throw (ConnectIOException) cause;
			} else if(cause instanceof ConnectTimeoutException) {
				throw (ConnectTimeoutException) cause;
			} else if(cause instanceof ConnectSecurityException) {
				throw (ConnectSecurityException) cause;
			} else if(cause instanceof ServerFailedToBindException) {
				throw (ServerFailedToBindException) cause;
			} else if(cause instanceof ServerFailedToCloseException) {
				throw (ServerFailedToCloseException) cause;
			} else if(cause instanceof ConnectSocketFailedToCloseException) {
				throw (ConnectSocketFailedToCloseException) cause;
			} else if(cause instanceof ConnectFailedToSetTimeoutException) {
				throw (ConnectFailedToSetTimeoutException) cause;
			} //TODO else (except InterruptedException)
		} finally {
			// if something went wrong we're not connected
			setConnected(false);
		}
	}
	
	/**
	 * TODO ...
	 * 
	 * author Martin Riedel
	 *
	 */
	private class ServerTask implements Callable<Socket> {
		
		/*
		 * The local port the ServerThread will bind to
		 */
		private final int port;
		
		/*
		 * The peer who's expected to connect to the local host
		 */
		private final PeerURL peer;
		
		/*
		 * The ServerSocket used to listen for incoming connections
		 */
		private ServerSocket serverSocket = null;
		
		/*
		 * The socket representing the connection accepted from the peer
		 */
		private Socket clientSocket = null;
		
		
		/**
		 * Creates a new ServerTask binding to the provided local port. Only connections
		 * from the specified peer will be accepted.
		 * 
		 * @param port the local port that the ServerThread will bind to
		 * @param peer the peer to accept connections from
		 */
		public ServerTask(final int port, final PeerURL peer) {			
			this.port = port;
			this.peer = peer;
		}
		
		/**
		 * TODO ... 
		 *
		 */
		@Override
		public Socket call() throws ServerFailedToBindException, ConnectIOException, ConnectSecurityException,
				ServerFailedToCloseException, ConnectSocketFailedToCloseException, InterruptedException,
				ConnectTimeoutException, ConnectFailedToSetTimeoutException {
			try {
				// start listening
				//TODO bind to the specific address selected via the GUI, not just any/all
				this.serverSocket = new ServerSocket(this.port);
				
				// set the timeout in milliseconds after which serverSocket.accept() will stop blocking
				// so that we can check for thread interruption
				this.serverSocket.setSoTimeout(connectIntervalTimeout);
				
				// attempt to receive a connection for a maximum of connectMaxIntervals times
				for(int i = 0; ; i++) {

					try {
						this.clientSocket = this.serverSocket.accept();
					} catch(SocketTimeoutException e) {
						// accept timed out as requested - check for thread interruption and abort if present, otherwise retry
						if(Thread.interrupted())
							throw new InterruptedException();
					} catch(IOException e) {
						throw new ConnectIOException(e);
					} catch(SecurityException e) {
						throw new ConnectSecurityException(e);
					} catch(IllegalBlockingModeException e) {
						throw new LogicError(e);
					}

					// check if a connection has been established
					if(this.clientSocket != null) {
						if(this.clientSocket.isConnected()) {
							// check if the connection originates from the expected peer
							if(this.clientSocket.getInetAddress().equals(this.peer.getInetAddress()))
								// if so, break the loop -> connection established
								break;
							// if not, discard the connection and keep going
							getLoggerForThisMethod().log(Level.WARNING, "dropped connection from remote host " + this.clientSocket.getInetAddress().toString() + ": host is not the expected peer");
							this.clientSocket = null;
						}
					}

					// check if the maximum number of connection attempts is reached and time out the entire operation if so
					if(i >= connectMaxIntervals - 1)
						throw new ConnectTimeoutException();
				}
				
				// if the flow reaches this point, a connection from the correct peer has been accepted
				setConnected(true);
				return this.clientSocket;
			} catch(SocketException e) {
				throw new ConnectFailedToSetTimeoutException(e);
			} catch(IOException e) {
				throw new ServerFailedToBindException(this.port, e);
			} catch(SecurityException e) {
				throw new ServerFailedToBindException(this.port, e);
			} finally {
				// whatever happened, close the server socket if it exists
				if(this.serverSocket != null) {
					try {
						this.serverSocket.close();
					} catch(IOException e) {
						throw new ServerFailedToCloseException(e);
					}
				}
				// unless the connection has been established successfully, close the client socket if it exists
				if(!isConnected()) {
					if(this.clientSocket != null) {
						try {
							this.clientSocket.close();
						} catch(IOException e) {
							throw new ConnectSocketFailedToCloseException(e);
						}
					}
				}
			}
		}
	}
	
}
