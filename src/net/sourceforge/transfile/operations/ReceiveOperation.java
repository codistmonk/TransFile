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
import java.io.FileOutputStream;

import net.sourceforge.transfile.operations.messages.DataOfferMessage;
import net.sourceforge.transfile.operations.messages.DataRequestMessage;
import net.sourceforge.transfile.operations.messages.FileOfferMessage;
import net.sourceforge.transfile.operations.messages.OperationMessage;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class ReceiveOperation extends AbstractOperation {
	
	private final Controller controller;
	
	private final FileOfferMessage fileOffer;
	
	private final DestinationFileProvider destinationFileProvider;
	
	/**
	 * 
	 * @param connection
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param fileOffer
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param destinationFileProvider
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public ReceiveOperation(final Connection connection, final FileOfferMessage fileOffer, final DestinationFileProvider destinationFileProvider) {
		super(connection, fileOffer.getSourceFile().getName());
		this.fileOffer = fileOffer;
		this.controller = this.new Controller();
		this.destinationFileProvider = destinationFileProvider;
	}
	
	/**
	 * 
	 * @return the file offer message used to create this operation
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final FileOfferMessage getFileOffer() {
		return this.fileOffer;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Operation.Controller getController() {
		return this.controller;
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final DestinationFileProvider getDestinationFileProvider() {
		return this.destinationFileProvider;
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (2010-06-06)
	 *
	 */
	private class Controller extends AbstractController {
		
		private long receivedByteCount;
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		Controller() {
			ReceiveOperation.this.addOperationListener(new Listener() {
				
				@Override
				public void stateChanged() {
					if (Controller.this.canTransferData()) {
						Controller.this.requestData();
					}
				}
				
				@Override
				public final void progressChanged() {
					// Do nothing
				}
				
			});
		}
		
		@Override
		protected final boolean canStart() {
			final ReceiveOperation receiveOperation = ReceiveOperation.this;
			
			if (receiveOperation.getLocalFile() == null) {
				receiveOperation.setLocalFile(
						receiveOperation.getDestinationFileProvider().getDestinationFile(receiveOperation.getFileName()));
			}
			
			return receiveOperation.getLocalFile() != null;
		}
		
		@Override
		protected final void operationMessageReceived(final OperationMessage operationMessage) {
			if (operationMessage instanceof DataOfferMessage && this.canTransferData()) {
				if (ReceiveOperation.this.getLocalFile() == null) {
					throw new IllegalStateException("Destination file is null but the following message has been received: " + operationMessage);
				}
				
				try {
					FileOutputStream output = null;
					
					try {
						output = new FileOutputStream(ReceiveOperation.this.getLocalFile(), true);
						final byte[] data = ((DataOfferMessage) operationMessage).getBytes();
						
						output.write(data);
						
						this.dataReceived(data.length, ReceiveOperation.this.getFileOffer().getSourceByteCount());
					} finally {
						if (output != null) {
							output.close();
						}
					}
				} catch (final Exception exception) {
					// TODO better error handling
					exception.printStackTrace();
				}
			}
			
			if (!(operationMessage instanceof DataRequestMessage) && this.canTransferData()) {
				this.requestData();
			}
		}
		
		@Override
		protected final File getSourceFile() {
			return ReceiveOperation.this.getFileOffer().getSourceFile();
		}
		
		final void requestData() {
			if (this.canTransferData()) {
				ReceiveOperation.this.getConnection().sendMessage(new DataRequestMessage(this.getSourceFile(), this.receivedByteCount, PREFERRED_TRANSFERRED_BYTE_COUNT));
			}
		}
		
		/**
		 * TODO doc
		 * 
		 * @param byteCount
		 * <br>Range: {@code [0 .. totalByteCount - this.sentByteCount]}
		 * @param totalByteCount 
		 * <br>Range: {@code [0 .. Long.MAX_VALUE]}
		 */
		private final void dataReceived(final int byteCount, final long totalByteCount) {
			this.receivedByteCount += byteCount;
			
			ReceiveOperation.this.setProgress((double) this.receivedByteCount / totalByteCount);
			
			if (this.receivedByteCount == totalByteCount) {
				ReceiveOperation.this.getConnection().sendMessage(new DataRequestMessage(this.getSourceFile(), this.receivedByteCount, PREFERRED_TRANSFERRED_BYTE_COUNT));
				this.done();
			}
		}
		
	}
	
	public static final int PREFERRED_TRANSFERRED_BYTE_COUNT = 1;
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-06)
	 *
	 */
	public static interface DestinationFileProvider {
		
		/**
		 * TODO doc
		 * 
		 * @param fileName
		 * <br>Should not be null
		 * @return
		 * <br>A possibly null value
		 */
		public abstract File getDestinationFile(String fileName);
		
	}
	
}
