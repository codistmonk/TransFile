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
			Tools.debugPrint("Connecting", this.getLocalPeer(), "to", this.getRemotePeer());
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
				Thread.sleep(2 * CONNECT_INTERVAL);
			} catch (final InterruptedException exception1) {
				return;
			}
			
			final long maximumTime = System.currentTimeMillis() + CONNECT_TIMEOUT;
			final InetSocketAddress localAddress = new InetSocketAddress(getPort(SimpleSocketConnection.this.getLocalPeer()));
			final InetSocketAddress remoteAddress = getInetSocketAddress(SimpleSocketConnection.this.getRemotePeer());
			
			if (this.connect(maximumTime, localAddress, remoteAddress) == null) {
				SimpleSocketConnection.this.setExecutor(null);
			}
		}

		/**
		 * TODO doc
		 * <br>Blocking.
		 * 
		 * @param maximumTime
		 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
		 * @param localAddress
		 * <br>Not null
		 * <br>Shared
		 * @param remoteAddress
		 * <br>Not null
		 * <br>Shared
		 * @return
		 * <br>Not null
		 * <br>New
		 */
		private final Socket connect(final long maximumTime, final InetSocketAddress localAddress, final InetSocketAddress remoteAddress) {
			do {
				SimpleSocketConnection.this.setConnectionError(null);
				
				try {
					return this.prepareToReadAndWrite(this.connect(localAddress, remoteAddress));
				} catch (final Exception exception) {
					SimpleSocketConnection.this.setConnectionError(exception);
				}
			} while (System.currentTimeMillis() < maximumTime && !Thread.currentThread().isInterrupted());
			
			return null;
		}
		
		/**
		 * TODO doc
		 * 
		 * @param socket
		 * <br>Not null
		 * @return {@code socket}
		 * <br>Not null
		 * @throws IOException if an I/O error occurs
		 */
		private final Socket prepareToReadAndWrite(final Socket socket) throws IOException {
			SimpleSocketConnection.this.setOutput(new ObjectOutputStream(socket.getOutputStream()));
			SimpleSocketConnection.this.getExecutor().execute(SimpleSocketConnection.this.new ReceptionTask(socket));
			
			return socket;
		}
		
		/**
		 * TODO doc
		 * <br>Blocking.
		 * 
		 * @param localAddress
		 * <br>Not null
		 * <br>Shared
		 * @param remoteAddress
		 * <br>Not null
		 * <br>Shared
		 * @return
		 * <br>Not null
		 * <br>New
		 * @throws Exception if an error occurs
		 */
		private final Socket connect(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress) throws Exception {
			final Socket result = new Socket();
			
			result.setReuseAddress(true);
			result.setSoTimeout(0);
			result.bind(localAddress);
			result.connect(remoteAddress, CONNECT_INTERVAL);
			
			Tools.debugPrint(SimpleSocketConnection.this, result);
			
			return result;
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
				this.setInput();
				this.receiveAndProcessObjects();
			} finally {
				SimpleSocketConnection.this.setExecutor(null);
			}
		}

		/**
		 * TODO doc
		 * <br>Blocking.
		 */
		private void receiveAndProcessObjects() {
			Object object = null;
			
			do {
				try {
					object = this.readInput();
				} catch (final IOException exception) {
					object = STOP;
				} catch (final ClassNotFoundException exception) {
					System.err.println(Tools.debug(2, exception.getMessage()));
					object = RETRY;
				}
			} while (object != STOP && !(object instanceof DisconnectMessage));
		}
		
		/**
		 * Creates an instance of {@link ObjectInputStream} from {@code this.socket}.
		 * <br>Blocking.
		 * 
		 * @throws RuntimeException if an I/O error occurs
		 */
		private final void setInput() {
			try {
				this.input = new ObjectInputStream(this.socket.getInputStream());
			} catch (final IOException exception) {
				Tools.throwUnchecked(exception);
			}
		}
		
		/**
		 * Waits for an object and then dispatches it if it is an instance of {@link Message}.
		 * <br>Blocking.
		 * 
		 * @return
		 * <br>Maybe null
		 * <br>Maybe shared
		 * @throws IOException if an I/O error occurs
		 * @throws ClassNotFoundException if the class of a serialized object cannot be found
		 */
		private final Object readInput() throws IOException, ClassNotFoundException {
			final Object result = this.input.readObject();
			
			if (result instanceof Message) {
				SimpleSocketConnection.this.dispatchMessage((Message) result);
			}
			
			return result;
		}
		
	}
	
	static final Object STOP = null;
	
	static final Object RETRY = "retry";
	
	/**
	 * Time in milliseconds.
	 */
	public static final int CONNECT_INTERVAL = 100;
	
	/**
	 * Time in milliseconds.
	 */
	public static final long CONNECT_TIMEOUT = 20000L;
	
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
