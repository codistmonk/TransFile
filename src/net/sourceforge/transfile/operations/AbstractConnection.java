package net.sourceforge.transfile.operations;

import java.util.ArrayList;
import java.util.Collection;

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
	
	private State state;
	
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
	
	@Override
	public final String getRemotePeer() {
		return this.remotePeer;
	}
	
	/**
	 * 
	 * @param localPeer
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void setLocalPeer(final String localPeer) {
		if (this.getState() != State.DISCONNECTED) {
			throw new IllegalStateException(this.getState().toString());
		}
		
		this.localPeer = localPeer;
	}
	
	/**
	 * 
	 * @param remotePeer
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void setRemotePeer(final String remotePeer) {
		if (this.getState() != State.DISCONNECTED) {
			throw new IllegalStateException(this.getState().toString());
		}
		
		this.remotePeer = remotePeer;
	}
	
	@Override
	public final State getState() {
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
			this.state = state;
			
			for (final Listener listener : this.getListeners()) {
				listener.stateChanged();
			}
		}
	}
	
}