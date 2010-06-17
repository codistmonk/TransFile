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

package net.sourceforge.transfile.operations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.Message;
import net.sourceforge.transfile.tools.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-15)
 *
 */
public class SimpleSocketConnection extends AbstractConnection {
	
	private ExecutorService executor;
	
	private ObjectOutputStream output;
	
	public SimpleSocketConnection() {
		// Do nothing
	}
	
	/**
	 * 
	 * @param localPeer
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param remotePeer
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public SimpleSocketConnection(final String localPeer, final String remotePeer) {
		super(localPeer, remotePeer);
	}
	
	@Override
	public final void connect() {
		if (this.getState() == State.DISCONNECTED) {
			this.setState(State.CONNECTING);
			
			this.getExecutor().execute(this.new ConnectionTask());
		}
	}
	
	@Override
	public final void disconnect() {
		try {
			if (this.output != null) {
				this.sendMessage(new DisconnectMessage());
			}
		} finally {
			this.setExecutor(null);
		}
	}
	
	@Override
	public final synchronized void doSendMessage(final Message message) {
		try {
			if (this.output != null) {
				this.output.writeObject(message);
				this.output.flush();
			}
		} catch (final IOException exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param output
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	final void setOutput(final ObjectOutputStream output) {
		synchronized (this) {
			if (output != this.output && this.output != null) {
				try {
					this.output.close();
				} catch (final Exception exception) {
					this.setConnectionError(exception);
				}
			}
			
			this.output = output;
		}
		
		if (this.output != null) {
			this.setState(State.CONNECTED);
		}
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	final synchronized ExecutorService getExecutor() {
		if (this.executor == null) {
			this.executor = Executors.newSingleThreadExecutor();
		}
		
		return this.executor;
	}
	
	/**
	 * 
	 * @param executor
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	final void setExecutor(final ExecutorService executor) {
		if (executor != this.getExecutor() && this.getExecutor() != null) {
			try {
				synchronized (this) {
					this.getExecutor().shutdownNow();
				}
				
				this.setState(State.DISCONNECTED);
			} finally {
				this.setOutput(null);
			}
		}
		
		this.executor = executor;
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-15)
	 *
	 */
	private class ConnectionTask implements Runnable {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		ConnectionTask() {
			// Do nothing
		}
		
		@Override
		public final void run() {
			// TODO find a better fix
			// If a connection is canceled (using disconnect()) while the socket is trying to connect,
			// then the socket will still be able to connect before timing out, thus preventing a new
			// connection with the same port to be established, and also making the peer connection
			// unavailable (connected to an unused socket)
			// The following call to sleep() is a quick fix to this problem encountered during testing
			try {
				Thread.sleep(2 * SOCKET_TIMEOUT);
			} catch (final InterruptedException exception1) {
				return;
			}
			
			final long maximumTime = System.currentTimeMillis() + CONNECT_TIMEOUT;
			final InetSocketAddress localAddress = new InetSocketAddress(getPort(SimpleSocketConnection.this.getLocalPeer()));
			final InetSocketAddress remoteAddress = getInetSocketAddress(SimpleSocketConnection.this.getRemotePeer());
			
			do {
				try {
					SimpleSocketConnection.this.setConnectionError(null);
					
					final Socket socket = new Socket();
					
					socket.setReuseAddress(true);
					socket.setSoTimeout(0);
					socket.bind(localAddress);
					socket.connect(remoteAddress, SOCKET_TIMEOUT);
					Tools.debugPrint(SimpleSocketConnection.this, socket);
					SimpleSocketConnection.this.setOutput(new ObjectOutputStream(socket.getOutputStream()));
					SimpleSocketConnection.this.getExecutor().execute(SimpleSocketConnection.this.new ReceptionTask(socket));
					
					return;
				} catch (final Exception exception) {
					SimpleSocketConnection.this.setConnectionError(exception);
				}
			} while (System.currentTimeMillis() < maximumTime);
		}
		
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-15)
	 *
	 */
	private class ReceptionTask implements Runnable {
		
		private final Socket socket;
		
		private ObjectInputStream input;
		
		/**
		 * @param socket
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		ReceptionTask(final Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public final void run() {
			try {
				this.input = new ObjectInputStream(this.socket.getInputStream());
			} catch (final IOException exception) {
				Tools.throwUnchecked(exception);
			}
			
			Object object = null;
			
			do {
				try {
					object = this.input.readObject();
					
					if (object instanceof Message) {
						SimpleSocketConnection.this.dispatchMessage((Message) object);
					}
				} catch (final IOException exception) {
					object = null;
				} catch (final ClassNotFoundException exception) {
					System.err.println(Tools.debug(2, exception.getMessage()));
					object = "retry";
				}
			} while (object != null && !(object instanceof DisconnectMessage));
			
			SimpleSocketConnection.this.setExecutor(null);
		}
		
	}
	
	/**
	 * Time in milliseconds.
	 */
	public static final int SOCKET_TIMEOUT = 100;
	
	/**
	 * Time in milliseconds.
	 */
	public static final long CONNECT_TIMEOUT = 10000L;
	
	/**
	 * TODO doc
	 * 
	 * @param peer
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public static final InetSocketAddress getInetSocketAddress(final String peer) {
		final String[] protocolHostPort = getProtocolHostPort(peer);
		final String host = protocolHostPort[1];
		final int port = Integer.parseInt(protocolHostPort[2]);
		
		return new InetSocketAddress(host, port);
	}
	
}
