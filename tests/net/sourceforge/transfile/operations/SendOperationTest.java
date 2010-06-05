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

import java.io.File;
import java.io.FileInputStream;
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
public class SendOperationTest extends AbstractOperationTestBase {
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Connection[] createMatchingConnectionPair() {
		return new DummyConnectionTest().createMatchingConnectionPair();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Operation createOperation(final Connection connection, final File file) {
		return new SendOperationImplementation(connection, file);
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static abstract class AbstractOperation implements Operation {
		
		private final Thread thread;
		
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
			this.thread = Thread.currentThread();
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
		public final synchronized void addConnectionListener(final Listener listener) {
			this.listeners.add(listener);
		}
		
		@Override
		public final synchronized void removeConnectionListener(final Listener listener) {
			this.listeners.remove(listener);
		}
		
		@Override
		public final synchronized String getFileName() {
			checkThread(this.thread);
			
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
			checkThread(this.thread);
			
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
		
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static class SendOperationImplementation extends AbstractOperation implements SendOperation {
		
		private final Controller controller;
		
		/**
		 * 
		 * @param connection
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param sourceFile
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public SendOperationImplementation(final Connection connection, final File sourceFile) {
			super(connection, sourceFile.getName());
			this.controller = this.new Controller();
			
			this.setLocalFile(sourceFile);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final Controller getController() {
			return this.controller;
		}
		
		/**
		 * TODO doc
		 *
		 * @author codistmonk (creation 2010-06-05)
		 *
		 */
		private class Controller implements Operation.Controller, Connection.Listener {
			
			private long sentByteCount;
			
			private State remoteState;
			
			Controller() {
				SendOperationImplementation.this.getConnection().addConnectionListener(this);
				this.new DataSender().start();
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void cancel() {
				this.updateState(State.CANCELED, State.PAUSED, State.PROGRESSING);
				this.sendStateMessage();
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void pause() {
				this.updateState(State.PAUSED, State.PROGRESSING);
				this.sendStateMessage();
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void remove() {
				this.updateState(State.REMOVED, State.CANCELED, State.DONE, State.QUEUED);
				this.sendStateMessage();
				SendOperationImplementation.this.getConnection().removeConnectionListener(this);
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void retry() {
				this.updateState(State.PROGRESSING, State.CANCELED);
				this.sendStateMessage();
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void start() {
				this.updateState(State.PROGRESSING, State.DONE, State.QUEUED);
				this.sendStateMessage();
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void messageReceived(final Message message) {
				if (message instanceof OperationMessage && ((OperationMessage) message).getSourceFile().equals(SendOperationImplementation.this.getLocalFile())) {
					if (message instanceof StateMessage) {
						this.setRemoteState(((StateMessage) message).getState());
					}
				}
			}
			
			/** 
			 * {@inheritDoc}
			 */
			@Override
			public final void stateChanged() {
				// TODO Auto-generated method stub
				
			}
			
			/**
			 * TODO doc
			 * 
			 * @param byteCount
			 * <br>Range: {@code [0 .. totalByteCount - this.sentByteCount]}
			 * @param totalByteCount 
			 * <br>Range: {@code [0 .. Long.MAX_VALUE]}
			 */
			final void dataSent(final int byteCount, final long totalByteCount) {
				this.sentByteCount += byteCount;
				
				SendOperationImplementation.this.setProgress((double) this.sentByteCount / totalByteCount);
				
				if (this.sentByteCount == totalByteCount) {
					SendOperationImplementation.this.setState(State.DONE);
				}
			}
			
			/**
			 * 
			 * @return
			 * <br>A possibly null value
			 * <br>A shared value
			 */
			final synchronized State getRemoteState() {
				return this.remoteState;
			}
			
			/**
			 * 
			 * @param remoteState
			 * <br>Can be null
			 * <br>Shared parameter
			 */
			private final synchronized void setRemoteState(final State remoteState) {
				this.remoteState = remoteState;
			}
			
			private final void sendStateMessage() {
				final Operation operation = SendOperationImplementation.this;
				
				operation.getConnection().sendMessage(new StateMessage(operation.getLocalFile(), operation.getState()));
			}
			
			/**
			 * TODO doc
			 * 
			 * @param newState
			 * <br>Should not be null
			 * <br>Shared parameter
			 * @param allowedCurrentStates
			 * <br>Should not be null
			 */
			private final void updateState(final State newState, final State... allowedCurrentStates) {
				SendOperationImplementation.this.checkState(allowedCurrentStates);
				SendOperationImplementation.this.setState(newState);
			}
			
			/**
			 * TODO doc
			 *
			 * @author codistmonk (creation 2010-06-05)
			 *
			 */
			private final class DataSender extends Thread {
				
				/**
				 * Package-private default constructor to suppress visibility warnings.
				 */
				DataSender() {
					// Do nothing
				}
				
				@Override
				public final void run() {
					try {
						FileInputStream input = null;
						
						try {
							Operation.State localState = this.getLocalState();
							Operation.State remoteState = this.getRemoteState();
							
							while (localState != Operation.State.REMOVED && !this.isInterrupted()) {
								if (localState == remoteState && localState == Operation.State.PROGRESSING && this.getSourceFile() != null) {
									if (input == null) {
										input = new FileInputStream(this.getSourceFile());
									}
									
									final int c = input.read();
									
									if (c != -1) {
										this.sendData((byte) c);
									}
								}
								
								yield();
								
								localState = this.getLocalState();
								remoteState = this.getRemoteState();
							}
						} finally {
							if (input != null) {
								input.close();
							}
						}
					} catch (final Exception exception) {
						exception.printStackTrace();
					}
				}
				
				private final File getSourceFile() {
					return SendOperationImplementation.this.getLocalFile();
				}
				
				/**
				 * 
				 * @return
				 * <br>A non-null value
				 * <br>A shared value
				 */
				private final Operation.State getLocalState() {
					return SendOperationImplementation.this.getState();
				}
				
				/**
				 * 
				 * @return
				 * <br>A non-null value
				 * <br>A shared value
				 */
				private final Operation.State getRemoteState() {
					return Controller.this.getRemoteState();
				}
				
				/**
				 * TODO doc
				 * 
				 * @param data
				 * <br>Should not be null
				 * <br>Shared parameter
				 */
				private final void sendData(final byte... data) {
					SendOperationImplementation.this.getConnection().sendMessage(new DataMessage(this.getSourceFile(), data));
					Controller.this.dataSent(data.length, this.getSourceFile().length());
				}
				
			}
			
		}
		
	}
	
	/**
	 * TODO doc
	 * 
	 * @param thread
	 * <br>Should not be null
	 * @throws IllegalStateException if the current thread is not {@code thread}
	 */
	public static final void checkThread(final Thread thread) {
		if (Thread.currentThread() != thread) {
			throw new IllegalStateException("This section should have been executed in thread " + thread + " but was executed in thread " + Thread.currentThread());
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
