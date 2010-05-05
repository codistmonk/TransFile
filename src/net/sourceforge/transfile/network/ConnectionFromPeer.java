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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

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
	
	private final FutureTask<Socket> peerConnectionAcceptor;

	public ConnectionFromPeer(final PeerURL peer, final int localPort) {
		super(peer);
		
		if(localPort <= 0 || localPort > 65535)
			throw new LogicError("invalid local server port number");
		
		peerConnectionAcceptor = new FutureTask<Socket>(new ServerTask(localPort, peer));
	}

	public void establishInBackground() {
		(new Thread(peerConnectionAcceptor)).start();	
	}

	public void interruptBackgroundTask() {
		peerConnectionAcceptor.cancel(true);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void establish() throws InterruptedException, ConnectIOException, ConnectTimeoutException, 
			ConnectSocketFailedToCloseException, ServerFailedToCloseException, ServerFailedToBindException, 
			ConnectSecurityException, ConnectFailedToSetTimeoutException {
		try {
			socket = peerConnectionAcceptor.get();
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
			}
		
		} catch(CancellationException e) {
			// Nothing major to do here. If ServerTask was cancelled, then so was this thread,
			// causing peerConnectionAcceptor.get() to throw an InterruptedException
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
				serverSocket = new ServerSocket(port);
				
				// set the timeout in milliseconds after which serverSocket.accept() will stop blocking
				// so that we can check for thread interruption
				serverSocket.setSoTimeout(connectIntervalTimeout);
				
				// attempt to receive a connection for a maximum of connectMaxIntervals times
				for(int i = 0; ; i++) {

					try {
						clientSocket = serverSocket.accept();
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
					if(clientSocket != null) {
						if(clientSocket.isConnected()) {
							// check if the connection originates from the expected peer
							if(clientSocket.getInetAddress().equals(peer.getInetAddress()))
								// if so, break the loop -> connection established
								break;
							else
								// if not, discard the connection and keep going
								//TODO log
								clientSocket = null;
						}
					}

					// check if the maximum number of connection attempts is reached and time out the entire operation if so
					if(i >= connectMaxIntervals - 1)
						throw new ConnectTimeoutException();
				}
				
				// if the flow reaches this point, a connection from the correct peer has been accepted
				setConnected(true);
				return clientSocket;
			} catch(SocketException e) {
				throw new ConnectFailedToSetTimeoutException(e);
			} catch(IOException e) {
				throw new ServerFailedToBindException(port, e);
			} catch(SecurityException e) {
				throw new ServerFailedToBindException(port, e);
			} finally {
				// whatever happened, close the server socket if it exists
				if(serverSocket != null) {
					try {
						serverSocket.close();
					} catch(IOException e) {
						throw new ServerFailedToCloseException(e);
					}
				}
				// unless the connection was successfully established, close the client socket if it exists
				if(!isConnected()) {
					if(clientSocket != null) {
						try {
							clientSocket.close();
						} catch(IOException e) {
							throw new ConnectSocketFailedToCloseException(e);
						}
					}
				}
			}
		}
	}
	
}
