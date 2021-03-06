package net.sourceforge.transfile.operations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.Message;

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
	protected final void doSendMessage(final Message message) {
		EXECUTOR.execute(new Runnable() {
			
			@Override
			public void run() {
				DummyConnection.this.remoteDispatch(message);
			}
			
		});
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final void connect() {
		switch (this.getState()) {
		case DISCONNECTED:
			this.setState(State.CONNECTING);
			
			if (this.getRemoteConnection() != null && this.getRemoteConnection().getState() == State.CONNECTING) {
				this.setState(State.CONNECTED);
				this.getRemoteConnection().setState(State.CONNECTED);
			} else if (this.getRemoteConnection() == null) {
				this.setState(State.DISCONNECTED);
			}
			break;
		default:
			break;
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final void disconnect() {
		switch (this.getState()) {
		case CONNECTING:
			this.setState(State.DISCONNECTED);
			break;
		case CONNECTED:
			this.sendMessage(new DisconnectMessage());
			this.setState(State.DISCONNECTED);
			break;
		default:
			break;
		}
	}
	
	/**
	 * TODO doc
	 * 
	 * @param message
	 * <br>Should not be null
	 * <br>Maybe shared parameter
	 */
	final void remoteDispatch(final Message message) {
		if (this.getRemoteConnection() != null) {
			this.getRemoteConnection().dispatchMessage(message);
		}
	}
	
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	
	/**
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public static final DummyConnection createDummyConnectionThatConnectsToItself() {
		final DummyConnection result = new DummyConnection();
		
		result.setRemoteConnection(result);
		
		return result;
	}
	
	/**
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public static final DummyConnection createDummyConnectionConnectedToItself() {
		final DummyConnection result = createDummyConnectionThatConnectsToItself();
		
		result.connect();
		
		return result;
	}
	
}
