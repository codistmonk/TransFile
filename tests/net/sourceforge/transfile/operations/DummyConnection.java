package net.sourceforge.transfile.operations;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class DummyConnection extends AbstractConnection {
	
	private DummyConnection remoteConnection;
	
	public DummyConnection() {
		super("", "");
		
		this.setLocalPeer(this.toString());
		
		this.addConnectionListener(new Listener() {
			
			@Override
			public final void stateChanged() {
				// Do nothing
			}
			
			@Override
			public final void messageReceived(final Message message) {
				if (message instanceof DisconnectMessage) {
					DummyConnection.this.setState(State.DISCONNECTED);
				}
			}
			
		});
	}
	
	/**
	 * 
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	public final DummyConnection getRemoteConnection() {
		return this.remoteConnection;
	}
	
	/**
	 * 
	 * @param remoteConnection
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	public final void setRemoteConnection(final DummyConnection remoteConnection) {
		this.setRemotePeer(remoteConnection != null ? remoteConnection.toString() : "");
		
		this.remoteConnection = remoteConnection;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final void sendMessage(final Message message) {
		if (this.getRemoteConnection() != null) {
			for (final Listener listener : this.getRemoteConnection().getListeners()) {
				listener.messageReceived(message);
			}
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final void toggleConnection() {
		switch (this.getState()) {
		case DISCONNECTED:
			this.setState(State.CONNECTING);
			if (this.getRemoteConnection() != null && this.getRemoteConnection().getState() == State.CONNECTING) {
				this.setState(State.CONNECTED);
				this.getRemoteConnection().setState(State.CONNECTED);
			}
			break;
		case CONNECTING:
			this.setState(State.DISCONNECTED);
			break;
		case CONNECTED:
			this.sendMessage(new DisconnectMessage());
			this.setState(State.DISCONNECTED);
			break;
		}
	}
	
}
