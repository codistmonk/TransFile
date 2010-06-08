package net.sourceforge.transfile.operations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.transfile.tools.Tools;

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
	public final synchronized void setProgress(final double progress) {
		try {
			logEntry(progress);
			
			this.progress = progress;
			
			for (final Listener listener : this.getListeners()) {
				listener.progressChanged();
			}
		} finally {
			logReturn(null);
		}
	}
	
	/**
	 * 
	 * @param state
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final synchronized void setState(final State state) {
		try {
			logEntry(state);
			
			this.state = state;
			
			for (final Listener listener : this.getListeners()) {
				listener.stateChanged();
			}
		} finally {
			logReturn(null);
		}
	}
	
	/**
	 * 
	 * @param states
	 * <br>Should not be null
	 * @throws IllegalStateException if the current state is not in {@code states}
	 */
	public final void checkState(final State... states) {
		if (!set(states).contains(this.getState())) {
			throw new IllegalStateException("This section should have been executed in state " + Arrays.toString(states) + " but was executed in state " + this.getState());
		}
	}
	
	/**
	 * 
	 * @param <T> the common type of the elements
	 * @param elements
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public static final <T> HashSet<T> set(final T... elements) {
		return new HashSet<T>(Arrays.asList(elements));
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
			AbstractOperation.this.setState(AbstractOperation.this.getState().getNextStateOnCancel());
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void pause() {
			AbstractOperation.this.setState(AbstractOperation.this.getState().getNextStateOnPause());
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void remove() {
			AbstractOperation.this.setState(AbstractOperation.this.getState().getNextStateOnRemove());
			AbstractOperation.this.getConnection().removeConnectionListener(this.messageHandler);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void start() {
			AbstractOperation.this.checkState(State.DONE, State.PAUSED, State.QUEUED);
			
			if (this.canStart()) {
				AbstractOperation.this.setState(AbstractOperation.this.getState().getNextStateOnStart());
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
		private class MessageHandler implements Connection.Listener {
			
			MessageHandler() {
				AbstractOperation.this.getConnection().addConnectionListener(this);
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void messageReceived(final Message message) {
				if (message instanceof OperationMessage && ((OperationMessage) message).getSourceFile().equals(AbstractController.this.getSourceFile())) {
					if (message instanceof StateMessage) {
						AbstractController.this.setRemoteState(((StateMessage) message).getState());
					}
					
					AbstractController.this.operationMessageReceived((OperationMessage) message);
				}
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void stateChanged() {
				// TODO Auto-generated method stub
				
			}
			
		}
		
	}
	
	/**
	 * TODO doc
	 * 
	 * @param parameters
	 * <br>Should not be null
	 */
	public static final void logEntry(final Object... parameters) {
		final Class<?> callerClass = Tools.getCallerClass();
		final String callerMethodName = Tools.getCallerMethodName();
		final String methodQualifiedName = callerClass.getName() + "." + callerMethodName;
		final Logger logger = Logger.getLogger(methodQualifiedName);
		
		logger.log(Level.FINEST, "Entering " + methodQualifiedName + " in thread " + Thread.currentThread() + " with parameters " + Arrays.toString(parameters));
	}
	
	/**
	 * TODO doc
	 * 
	 * @param returnedValue
	 * <br>Can be null
	 */
	public static final void logReturn(final Object returnedValue) {
		final Class<?> callerClass = Tools.getCallerClass();
		final String callerMethodName = Tools.getCallerMethodName();
		final String methodQualifiedName = callerClass.getName() + "." + callerMethodName;
		final Logger logger = Logger.getLogger(methodQualifiedName);
		
		logger.log(Level.FINEST, "Exiting " + methodQualifiedName + " in thread " + Thread.currentThread() + " with return value " + returnedValue);
	}
	
}
