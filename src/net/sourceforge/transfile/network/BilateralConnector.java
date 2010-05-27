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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

import net.sourceforge.transfile.exceptions.LogicError;
import net.sourceforge.transfile.network.exceptions.BilateralConnectException;
import net.sourceforge.transfile.network.exceptions.ConnectException;
import net.sourceforge.transfile.network.exceptions.ConnectSocketConfigException;
import net.sourceforge.transfile.network.exceptions.ConnectIOException;
import net.sourceforge.transfile.network.exceptions.ConnectSecurityException;
import net.sourceforge.transfile.network.exceptions.ConnectSocketFailedToCloseException;
import net.sourceforge.transfile.network.exceptions.ConnectTimeoutException;
import net.sourceforge.transfile.network.exceptions.ServerException;
import net.sourceforge.transfile.network.exceptions.ServerFailedToBindException;
import net.sourceforge.transfile.network.exceptions.ServerFailedToCloseException;

/**
 * TODO doc
 *
 * @author Martin Riedel
 *
 */
public class BilateralConnector implements Connector {

	private final Peer localPeer;
	
	private final Peer remotePeer;
	
	private final FutureTask<Socket> inboundConnectionAcceptor;
	
	
	/**
	 * 
	 * Constructs a new instance
	 * TODO doc
	 * @param localPeer
	 * @param remotePeer
	 */
	public BilateralConnector(final Peer localPeer, final Peer remotePeer) {
		this.localPeer = localPeer;
		this.remotePeer = remotePeer;
		
		this.inboundConnectionAcceptor = new FutureTask<Socket>(new ListenerTask(localPeer.getPort(), remotePeer));
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 */
	@Override
	public Peer getLocalPeer() {
		return this.localPeer;
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 */
	@Override
	public Peer getRemotePeer() {
		return this.remotePeer;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Connection connect()	throws BilateralConnectException, InterruptedException {		
		
		Connection outboundConnection = null;
		Connection inboundConnection = null;
		
		// make these members and define getters as well as other Link state
		Exception outboundConnectionError = null;
		Exception inboundConnectionError = null;
		
		(new Thread(this.inboundConnectionAcceptor)).start();
		
		try {
			outboundConnection = establishOutboundConnection();
		} catch(final InterruptedException e) {
			// if establishing a Link to the peer has been interrupted, make sure to interrupt
			// both connection attempts (both outgoing and incoming) by interrupting connectionFromPeer
			this.inboundConnectionAcceptor.cancel(true);
			throw e;
		} catch(final ConnectException e) {
			outboundConnectionError = e;
		}
		
		try {
			inboundConnection = new Connection(this.inboundConnectionAcceptor.get(), this.localPeer, this.remotePeer);
		} catch(final CancellationException e) {
			throw new InterruptedException();
		} catch(final ExecutionException e) {
			// listening for an incoming connection from the peer failed
			final Throwable cause = e.getCause();
			if(cause instanceof ConnectException) {
				inboundConnectionError = (Exception) cause;
			} else if(cause instanceof ServerException) {
				inboundConnectionError = (Exception) cause;
			} else if(cause instanceof InterruptedException) {
				// ignore
				//TODO safe?
			} else {
				//TODO handle
			}
		}
	
		if(!((outboundConnection != null && outboundConnection.isConnected()) || 
			 (inboundConnection != null && inboundConnection.isConnected())))
			throw new BilateralConnectException(outboundConnectionError, inboundConnectionError);
		
		return selectConnection(outboundConnection, inboundConnection);
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * @throws ConnectException
	 * @throws InterruptedException
	 */
	private Connection establishOutboundConnection() throws ConnectException, InterruptedException {
		final Socket socket = new Socket();
		final InetSocketAddress peerAddr = getRemotePeer().toInetSocketAddress();
		final long startTime = System.currentTimeMillis();
		
		try {
			
			try {
				socket.setReuseAddress(true);
			} catch(final SocketException e) {
				throw new ConnectSocketConfigException(e);
			}
			
			// attempt to connect for a maximum of connectMaxIntervals times
			while(true) {
				try {
					// attempt to connect, timing out after connectIntervalTimeout milliseconds to check for thread interruption
					socket.connect(peerAddr, CONNECT_INTERVAL_TIMEOUT);
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
		
	/**
	 * 
	 * TODO doc
	 * @return
	 */
	private Connection selectConnection(final Connection c1, final Connection c2) {
		//TODO implement
		return c1;
	}
	
	/**
	 * TODO ...
	 * 
	 * author Martin Riedel
	 *
	 */
	private static class ListenerTask implements Callable<Socket> {
		
		/*
		 * The local port the ServerThread will bind to
		 */
		private final int port;
		
		/*
		 * The peer who's expected to connect to the local host
		 */
		private final Peer remotePeer;
		
		/*
		 * The ServerSocket used to listen for incoming connections
		 */
		private ServerSocket serverSocket = null;
		
		/*
		 * The socket representing the connection accepted from the peer
		 */
		private Socket clientSocket = null;
		
		
		/**
		 * Creates a new listener binding to the provided local port. Only connections
		 * from the specified peer will be accepted.
		 * 
		 * @param port the local port that the ServerThread will bind to
		 * @param remotePeer the peer to accept connections from
		 */
		//TODO properly bind to the entire local address, not just the port
		public ListenerTask(final int port, final Peer remotePeer) {
			this.port = port;
			this.remotePeer = remotePeer;
		}
		
		/**
		 * TODO ... 
		 *
		 */
		@Override
		public Socket call() throws ConnectException, ServerException, InterruptedException {
			try {
				final long startTime = System.currentTimeMillis();
				
				// start listening
				//TODO bind to the specific address selected via the GUI, not just any/all
				this.serverSocket = new ServerSocket(this.port);
				
				// set the timeout in milliseconds after which serverSocket.accept() will stop blocking
				// so that we can check for thread interruption
				this.serverSocket.setSoTimeout(CONNECT_INTERVAL_TIMEOUT);
				
				this.serverSocket.setReuseAddress(true);
				
				// attempt to receive a connection for a maximum of connectMaxIntervals times
				while(true) {

					try {
						this.clientSocket = this.serverSocket.accept();
					} catch(final SocketTimeoutException e) {
						// accept timed out as requested - check for thread interruption and abort if present, otherwise retry
						if(Thread.interrupted())
							throw new InterruptedException();
					} catch(final IOException e) {
						throw new ConnectIOException(e);
					} catch(final SecurityException e) {
						throw new ConnectSecurityException(e);
					} catch(final IllegalBlockingModeException e) {
						throw new LogicError(e);
					}

					// check if a connection has been established
					if(this.clientSocket != null) {
						if(this.clientSocket.isConnected()) {
							// check if the connection originates from the expected peer
							if(this.clientSocket.getInetAddress().equals(this.remotePeer.getInetAddress()))
								// if so, break the loop -> connection established
								break;
							// if not, discard the connection and keep going
							getLoggerForThisMethod().log(Level.WARNING, "dropped connection from remote host " + this.clientSocket.getInetAddress().toString() + ": host is not the expected peer");
							this.clientSocket = null;
						}
					}

					if(System.currentTimeMillis() - startTime >= CONNECT_TIMEOUT)
						throw new ConnectTimeoutException();
				}
				
				// if the flow reaches this point, a connection from the correct peer has been accepted
				return this.clientSocket;
			} catch(SocketException e) {
				throw new ConnectSocketConfigException(e);
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
				if(!this.clientSocket.isConnected()) {
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
