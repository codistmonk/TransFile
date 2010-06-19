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
import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.transfile.operations.ReceiveOperation.DestinationFileProvider;
import net.sourceforge.transfile.operations.messages.FileOfferMessage;
import net.sourceforge.transfile.operations.messages.Message;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-08)
 *
 */
public class Session {
	
	private final Collection<Listener> listeners;
	
	private final Connection connection;
	
	private final DestinationFileProvider destinationFileProvider;
	
	/**
	 * 
	 * @param connection
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param destinationFileProvider
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public Session(final Connection connection, final DestinationFileProvider destinationFileProvider) {
		this.listeners = new ArrayList<Listener>();
		this.connection = connection;
		this.destinationFileProvider = destinationFileProvider;
		
		this.getConnection().addConnectionListener(this.new FileOfferReceiver());
	}
	
	/**
	 * TODO doc
	 * 
	 * @param file
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void offerFile(final File file) {
		final SendOperation sendOperation = new SendOperation(this.getConnection(), file);
		
		for (final Listener listener : this.getListeners()) {
			listener.sendOperationAdded(sendOperation);
		}
		
		this.getConnection().sendMessage(new FileOfferMessage(file));
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final Connection getConnection() {
		return this.connection;
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
	 * @param listener
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void addSessionListener(final Listener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 * <br>Can be null
	 */
	public final void removeSessionListener(final Listener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	final Listener[] getListeners() {
		return this.listeners.toArray(new Listener[this.listeners.size()]);
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-08)
	 *
	 */
	private final class FileOfferReceiver extends Connection.AbstractListener {
		
		/**
		 * Package-private default contructor to suppress visibility warnings.
		 */
		FileOfferReceiver() {
			// Do nothing
		}
		
		@Override
		public final void doMessageReceived(final Message message) {
			if (message instanceof FileOfferMessage) {
				final ReceiveOperation receiveOperation = new ReceiveOperation(Session.this.getConnection(), (FileOfferMessage) message, Session.this.getDestinationFileProvider());
				
				for (final Listener listener : Session.this.getListeners()) {
					listener.receiveOperationAdded(receiveOperation);
				}
			}
		}
		
	}

	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-08)
	 *
	 */
	public static interface Listener {
		
		/**
		 * TODO doc
		 * 
		 * @param sendOperation
		 * <br>Should not be null
		 */
		public abstract void sendOperationAdded(SendOperation sendOperation);
		
		/**
		 * 
		 * TODO doc
		 * @param receiveOperation
		 * <br>Should not be null
		 */
		public abstract void receiveOperationAdded(ReceiveOperation receiveOperation);
		
	}
	
}
