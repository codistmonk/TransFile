package net.sourceforge.transfile.operations;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.Message;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public abstract class AbstractConnection implements Connection {
	
	private final Collection<Listener> listeners;
	
	private String localPeer;
	
	private String remotePeer;
	
	private Exception connectionError;
	
	private State state;
	
	private long lastMessageTime;
	
	private long receivedMessageCount;
	
	private long sentMessageCount;
	
	private final Object synchronizer;
	
	public AbstractConnection() {
		this(DEFAULT_LOCAL_PEER, DEFAULT_REMOTE_PEER);
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
	public AbstractConnection(final String localPeer, final String remotePeer) {
		this.listeners = new ArrayList<Listener>();
		this.localPeer = localPeer;
		this.remotePeer = remotePeer;
		this.state = State.DISCONNECTED;
		this.synchronizer = new Object();
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public final synchronized Listener[] getListeners() {
		return this.listeners.toArray(new Listener[this.listeners.size()]);
	}
	
	@Override
	public final synchronized void addConnectionListener(final Listener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public final synchronized void removeConnectionListener(final Listener listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	public final String getLocalPeer() {
		return this.localPeer;
	}
	
	/**
	 * 
	 * @param localPeer
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	@Override
	public final void setLocalPeer(final String localPeer) {
		if (this.getState() != State.DISCONNECTED) {
			throw new IllegalStateException(this.getState().toString());
		}
		
		if (!this.getLocalPeer().equals(localPeer)) {
			this.localPeer = localPeer;
			
			this.new LocalPeerChangedEvent().fire();
		}
	}
	
	@Override
	public final String getRemotePeer() {
		return this.remotePeer;
	}
	
	/**
	 * 
	 * @param remotePeer
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	@Override
	public final void setRemotePeer(final String remotePeer) {
		if (this.getState() != State.DISCONNECTED) {
			throw new IllegalStateException(this.getState().toString());
		}
		
		if (!this.getRemotePeer().equals(remotePeer)) {
			this.remotePeer = remotePeer;
			
			this.new RemotePeerChangedEvent().fire();
		}
	}
	
	@Override
	public final Exception getConnectionError() {
		return this.connectionError;
	}
	
	/**
	 * 
	 * @return
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 */
	public final long getLastMessageTime() {
		synchronized (this.synchronizer) {
			return this.lastMessageTime;
		}
	}
	
	@Override
	public final synchronized State getState() {
		return this.state;
	}
	
	/**
	 * 
	 * @param state
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void setState(State state) {
		if (this.getState() != state) {
			synchronized (this) {
				this.state = state;
			}
			
			this.new StateChangedEvent().fire();
		}
	}
	
	@Override
	public final void sendMessage(final Message message) {
		if (this.getState() != State.CONNECTED) {
			return;
		}
		
		synchronized (this.synchronizer) {
			this.setLastMessageTime();
			++this.sentMessageCount;
		}
		
		this.doSendMessage(message);
	}
	
	/**
	 * 
	 * @return
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 */
	public final long getReceivedMessageCount() {
		synchronized (this.synchronizer) {
			return this.receivedMessageCount;
		}
	}
	
	/**
	 * 
	 * @return
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 */
	public final long getSentMessageCount() {
		synchronized (this.synchronizer) {
			return this.sentMessageCount;
		}
	}

	/**
	 * TODO doc
	 * 
	 * @param message
	 * <br>Should not be null
	 */
	protected abstract void doSendMessage(final Message message);
	
	/**
	 * TODO doc
	 * 
	 * @param connectionError
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	protected final void setConnectionError(final Exception connectionError) {
		this.connectionError = connectionError;
	}
	
	/**
	 * TODO doc
	 * 
	 * @param message
	 * <br>Should not be null
	 * <br>Maybe shared parameter
	 */
	protected final void dispatchMessage(final Message message) {
		synchronized (this.synchronizer) {
			this.setLastMessageTime();
			++this.receivedMessageCount;
		}
		
		if (message instanceof DisconnectMessage) {
			this.setState(State.DISCONNECTED);
		}
		
		this.new MessageReceivedEvent(message).fire();
	}
	
	protected final void setLastMessageTime() {
		this.lastMessageTime = System.currentTimeMillis();
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-19)
	 *
	 */
	protected abstract class AbstractEvent {
		
		public final void fire() {
			for (final Listener listener : AbstractConnection.this.getListeners()) {
				this.notifyListener(listener);
			}
		}
		
		/**
		 * TODO doc
		 * 
		 * @param listener
		 * <br>Not null
		 * <br>Shared
		 * <br>Input-output
		 */
		protected abstract void notifyListener(Listener listener);
		
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-19)
	 *
	 */
	private class LocalPeerChangedEvent extends AbstractEvent {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		LocalPeerChangedEvent() {
			// Do nothing
		}
		
		@Override
		protected final void notifyListener(final Listener listener) {
			listener.localPeerChanged();
		}
		
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-19)
	 *
	 */
	private class RemotePeerChangedEvent extends AbstractEvent {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		RemotePeerChangedEvent() {
			// Do nothing
		}
		
		@Override
		protected final void notifyListener(final Listener listener) {
			listener.remotePeerChanged();
		}
		
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-19)
	 *
	 */
	private class StateChangedEvent extends AbstractEvent {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		StateChangedEvent() {
			// Do nothing
		}
		
		@Override
		protected final void notifyListener(final Listener listener) {
			listener.stateChanged();
		}
		
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-19)
	 *
	 */
	private class MessageReceivedEvent extends AbstractEvent {
		
		private final Message message;
		
		/**
		 * 
		 * @param message
		 * <br>Not null
		 * <br>Shared
		 */
		MessageReceivedEvent(final Message message) {
			this.message = message;
		}
		
		@Override
		protected final void notifyListener(final Listener listener) {
			listener.messageReceived(this.message);
		}
		
	}
	
	public static final String DEFAULT_LOCAL_PEER = getPeer("transfile", "0.0.0.0", "12345");
	
	public static final String DEFAULT_REMOTE_PEER = getPeer("transfile", "0.0.0.0", "54321");
	
	/**
	 * Converts a string {@code "protocol://host:port"} into an array { {@code "protocol"}, {@code "host"}, {@code "port"} },
	 * and a string {@code "host:port"} into an array { {@code ""}, {@code "host"}, {@code "port"} }.
	 * 
	 * @param peer
	 * <br>Should not be null
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	public static final String[] getProtocolHostPort(final String peer) {
		final String[] result = ((peer.contains("://") ? "" : ":") + peer).split(":");
		
		if (result[1].startsWith("//")) {
			result[1] = result[1].substring("//".length());
		}
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @param peer
	 * <br>Should not be null
	 * @return
	 * <br>Range: any integer
	 */
	public static final int getPort(final String peer) {
		return Integer.parseInt(getProtocolHostPort(peer)[2]);
	}
	
	/**
	 * Converts an array { {@code "protocol"}, {@code "host"}, {@code "port"} } into a string "protocol://host:port",
	 * and an array { {@code "host"}, {@code "port"} } into a string {@code "host:port"}.
	 * 
	 * @param protocolHostPort
	 * <br>Should not be null
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	public static final String getPeer(final String... protocolHostPort) {
		final StringBuilder result = new StringBuilder(protocolHostPort[0]);
		
		result.append(protocolHostPort.length == 3 ? "://" : ":");
		result.append(protocolHostPort[1]);
		
		if (protocolHostPort.length == 3) {
			result.append(":");
			result.append(protocolHostPort[2]);
		}
		
		return result.toString();
	}
	
}
