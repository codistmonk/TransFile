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
import java.util.Arrays;

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
	public final Operation.Controller getController() {
		return this.controller;
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	private class Controller extends AbstractController {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		Controller() {
			// Do nothing
		}
		
		@Override
		protected final void operationMessageReceived(final OperationMessage operationMessage) {
			if (operationMessage instanceof DataRequestMessage && this.canTransferData()) {
				final DataRequestMessage request = (DataRequestMessage) operationMessage;
				
				this.dataReceived(request.getFirstByteOffset(), this.getSourceFile().length());
				
				if (request.getRequestedByteCount() > 0) {
					try {
						FileInputStream input = null;
						
						try {
							input = new FileInputStream(this.getSourceFile());
							
							input.skip(request.getFirstByteOffset());
							
							final byte[] bytes = new byte[request.getRequestedByteCount()];
							final int readByteCount = input.read(bytes);
							
							if (readByteCount > 0) {
								SendOperation.this.getConnection().sendMessage(new DataOfferMessage(
										this.getSourceFile(),
										request.getFirstByteOffset(),
										Arrays.copyOfRange(bytes, 0, readByteCount)));
							}
						} finally {
							if (input != null) {
								input.close();
							}
						}
						
					} catch (final Exception exception) {
						// TODO better error handling
						exception.printStackTrace();
					}
				}
			}
		}
		
		/**
		 * TODO doc
		 * 
		 * @param byteCount
		 * <br>Range: {@code [0L .. totalByteCount]}
		 * @param totalByteCount 
		 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
		 */
		private final void dataReceived(final long byteCount, final long totalByteCount) {
			SendOperation.this.setProgress((double) byteCount / totalByteCount);
			
			if (byteCount == totalByteCount) {
				SendOperation.this.setState(SendOperation.this.getState().getNextStateOnDone());
			}
		}
		
	}
	
}
