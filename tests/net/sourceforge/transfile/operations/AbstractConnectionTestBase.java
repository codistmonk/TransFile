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

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.transfile.operations.messages.DataOfferMessage;
import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.Message;

import org.junit.Test;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public abstract class AbstractConnectionTestBase {
	
	@Test
	public final void testToggleUnmatchedConnection() {
		final Connection connection = this.createUnmatchedConnection();
		final ConnectionRecorder connectionLogger = new ConnectionRecorder(connection);
		
		assertEquals(Connection.State.DISCONNECTED, connection.getState());
		
		connection.connect();
		this.waitUntilConnectionsAreReady(connection);
		connection.disconnect();
		this.waitUntilConnectionsAreReady(connection);
		
		assertEquals(Connection.State.DISCONNECTED, connection.getState());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.DISCONNECTED
				), connectionLogger.getEvents());
	}
	
	@Test
	public final void testConnectionAndDataTransfer() {
		final Connection[] connections = this.createMatchingConnectionPair();
		final Connection connection1 = connections[0];
		final Connection connection2 = connections[1];
		final ConnectionRecorder connectionRecorder1 = new ConnectionRecorder(connection1);
		final ConnectionRecorder connectionRecorder2 = new ConnectionRecorder(connection2);
		final Message dataMessage1 = new DataOfferMessage(new File("dummy"), 0L, "Hello world!".getBytes());
		final Message dataMessage2 = new DataOfferMessage(new File("dummy"), 0L, "42".getBytes());
		
		assertEquals(Connection.State.DISCONNECTED, connection1.getState());
		assertEquals(Connection.State.DISCONNECTED, connection2.getState());
		
		connection1.connect();
		connection2.connect();
		this.waitUntilConnectionsAreReady(connections);
		
		assertEquals(Connection.State.CONNECTED, connection1.getState());
		assertEquals(Connection.State.CONNECTED, connection2.getState());
		
		connection1.sendMessage(dataMessage1);
		this.waitUntilConnectionsAreReady(connections);
		connection2.sendMessage(dataMessage2);
		this.waitUntilConnectionsAreReady(connections);
		connection1.disconnect();
		this.waitUntilConnectionsAreReady(connections);
		
		assertEquals(Connection.State.DISCONNECTED, connection1.getState());
		assertEquals(Connection.State.DISCONNECTED, connection2.getState());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				dataMessage2,
				Connection.State.DISCONNECTED
				), connectionRecorder1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				dataMessage1,
				// The state is changed as soon as the disconnect message is received
				// That's why the logger detects the state change before the disconnect message
				// TODO should this behavior be changed?
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), connectionRecorder2.getEvents());
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public abstract Connection createUnmatchedConnection();
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public abstract Connection[] createMatchingConnectionPair();
	
	/**
	 * TODO doc
	 * 
	 * @param connections
	 * <br>Should not be null
	 */
	public abstract void waitUntilConnectionsAreReady(Connection... connections);
	
	/**
	 * Duration in milliseconds.
	 */
	public static final long WAIT_DURATION = 400L;
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static class ConnectionRecorder implements Connection.Listener {
		
		private final Connection connection;
		
		private final List<Object> events;
		
		/**
		 * 
		 * @param connection
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public ConnectionRecorder(final Connection connection) {
			this.connection = connection;
			this.events = new ArrayList<Object>();
			
			this.getConnection().addConnectionListener(this);
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
		public final List<Object> getEvents() {
			return this.events;
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void messageReceived(final Message message) {
			this.getEvents().add(message);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void stateChanged() {
			this.getEvents().add(this.getConnection().getState());
		}
		
	}
	
}
