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

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class SendOperation extends AbstractOperation {
	
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
	public SendOperation(final Connection connection, final File sourceFile) {
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
			SendOperation.this.getConnection().addConnectionListener(this);
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
			SendOperation.this.getConnection().removeConnectionListener(this);
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
			this.updateState(State.PROGRESSING, State.DONE, State.PAUSED, State.QUEUED);
			this.sendStateMessage();
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void messageReceived(final Message message) {
			if (message instanceof OperationMessage && ((OperationMessage) message).getSourceFile().equals(SendOperation.this.getLocalFile())) {
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
			
			SendOperation.this.setProgress((double) this.sentByteCount / totalByteCount);
			
			if (this.sentByteCount == totalByteCount) {
				SendOperation.this.setState(State.DONE);
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
			final Operation operation = SendOperation.this;
			
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
			SendOperation.this.checkState(allowedCurrentStates);
			SendOperation.this.setState(newState);
		}
		
		/**
		 * TODO doc
		 *
		 * @author codistmonk (creation 2010-06-05)
		 *
		 */
		private class DataSender extends Thread {
			
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
				return SendOperation.this.getLocalFile();
			}
			
			/**
			 * 
			 * @return
			 * <br>A non-null value
			 * <br>A shared value
			 */
			private final Operation.State getLocalState() {
				return SendOperation.this.getState();
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
				SendOperation.this.getConnection().sendMessage(new DataMessage(this.getSourceFile(), data));
				Controller.this.dataSent(data.length, this.getSourceFile().length());
			}
			
		}
		
	}
	
}
