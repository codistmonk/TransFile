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

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class ReceiveOperation extends AbstractOperation {
	
	private final Controller controller;
	
	private final RequestMessage requestMessage;
	
	/**
	 * 
	 * @param connection
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param requestMessage
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public ReceiveOperation(final Connection connection, final RequestMessage requestMessage) {
		super(connection, requestMessage.getSourceFile().getName());
		this.requestMessage = requestMessage;
		this.controller = this.new Controller();
	}
	
	/**
	 * 
	 * @return the request message used to create this operation
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final RequestMessage getRequestMessage() {
		return this.requestMessage;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Controller getController() {
		return this.controller;
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (2010-06-06)
	 *
	 */
	private class Controller extends AbstractController {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		Controller() {
			// Do nothing
		}
		
	}
	
}
