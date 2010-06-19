package net.sourceforge.transfile.operations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.transfile.operations.messages.Message;
import net.sourceforge.transfile.operations.messages.OperationMessage;
import net.sourceforge.transfile.operations.messages.StateMessage;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public abstract class AbstractOperation implements Operation {
	
	private final Collection<Listener> listeners;
	
	private final Connection connection;
	
	private final String fileName;
	
	private State state;
	
	private double progress;
	
	private File localFile;
	
	/**
	 * 
	 * @param connection
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param fileName
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public AbstractOperation(final Connection connection, final String fileName) {
		this.listeners = new ArrayList<Listener>();
		this.connection = connection;
		this.fileName = fileName;
		this.state = State.QUEUED;
	}
	
	@Override
	public final synchronized Connection getConnection() {
		return this.connection;
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
	public final synchronized void addOperationListener(final Listener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public final synchronized void removeOperationListener(final Listener listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	public final synchronized String getFileName() {
		return this.fileName;
	}
	
	@Override
	public final synchronized File getLocalFile() {
		return this.localFile;
	}
	
	@Override
	public final synchronized double getProgress() {
		return this.progress;
	}
	
	@Override
	public final synchronized State getState() {
		return this.state;
	}
	
	@Override
	public final synchronized void setLocalFile(final File localFile) {
		this.localFile = localFile;
	}
	
	/**
	 * 
	 * @param progress
	 * <br>Range: {@code [0.0 .. 1.0]}
	 */
	public final void setProgress(final double progress) {
		if (this.getProgress() != progress) {
			synchronized (this) {
				this.progress = progress;
			}
			
			for (final Listener listener : this.getListeners()) {
				listener.progressChanged();
			}
		}
	}
	
	/**
	 * 
	 * @param state
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void setState(final State state) {
		if (this.getState() != state) {
			synchronized (this) {
				this.state = state;
			}
			
			for (final Listener listener : this.getListeners()) {
				listener.stateChanged();
			}
		}
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-06)
	 *
	 */
	protected abstract class AbstractController implements Controller {
		
		private final Connection.Listener messageHandler;
		
		private State remoteState;
		
		public AbstractController() {
			this.messageHandler = this.new MessageHandler();
			
			AbstractOperation.this.addOperationListener(this.new StateMessageSender());
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void cancel() {
			AbstractOperation.this.setState(State.CANCELED);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void done() {
			AbstractOperation.this.setState(State.DONE);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void pause() {
			AbstractOperation.this.setState(State.PAUSED);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void remove() {
			AbstractOperation.this.setState(State.REMOVED);
			AbstractOperation.this.getConnection().removeConnectionListener(this.messageHandler);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void start() {
			if (this.canStart()) {
				AbstractOperation.this.setState(State.PROGRESSING);
			}
		}
		
		/**
		 * 
		 * @return
		 * <br>A possibly null value
		 * <br>A shared value
		 */
		public final synchronized State getRemoteState() {
			return this.remoteState;
		}
		
		public final boolean canTransferData() {
			return AbstractOperation.this.getState() == this.getRemoteState() && AbstractOperation.this.getState() == State.PROGRESSING;
		}
		
		/**
		 * 
		 * @param remoteState
		 * <br>Can be null
		 * <br>Shared parameter
		 */
		final synchronized void setRemoteState(final State remoteState) {
			this.remoteState = remoteState;
			
			if (this.getRemoteState() == State.PAUSED || this.getRemoteState() == State.CANCELED) {
				AbstractOperation.this.setState(this.getRemoteState());
			}
		}
		
		protected boolean canStart() {
			return true;
		}
		
		/**
		 * TODO doc
		 * 
		 * @param operationMessage
		 * <br>Should not be null
		 */
		protected void operationMessageReceived(final OperationMessage operationMessage) {
			// Default implementation, do nothing
		}
		
		/**
		 * TODO doc
		 * 
		 * @return
		 * <br>Should not be null
		 * <br>A shared value 
		 */
		protected File getSourceFile() {
			return AbstractOperation.this.getLocalFile();
		}
		
		/**
		 * TODO doc
		 *
		 * @author codistmonk (creation 2010-06-07)
		 *
		 */
		private class StateMessageSender implements Listener {
			
			/**
			 * Package-private default constructor to suppress visibility warnings.
			 */
			StateMessageSender() {
				// Do nothing
			}
			
			@Override
			public final void stateChanged() {
				final Operation operation = AbstractOperation.this;
				
				operation.getConnection().sendMessage(
						new StateMessage(AbstractController.this.getSourceFile(), operation.getState()));
			}
			
			@Override
			public final void progressChanged() {
				// Do nothing
			}
			
		}
		
		/**
		 * TODO doc
		 *
		 * @author codistmonk (creation 2010-06-06)
		 *
		 */
		private class MessageHandler extends Connection.AbstractListener {
			
			MessageHandler() {
				AbstractOperation.this.getConnection().addConnectionListener(this);
			}
			
			@Override
			protected final void doMessageReceived(final Message message) {
				if (message instanceof OperationMessage && ((OperationMessage) message).getSourceFile().equals(AbstractController.this.getSourceFile())) {
					if (message instanceof StateMessage) {
						AbstractController.this.setRemoteState(((StateMessage) message).getState());
					}
					
					AbstractController.this.operationMessageReceived((OperationMessage) message);
				}
			}
			
		}
		
	}
	
}
