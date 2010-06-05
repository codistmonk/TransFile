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
		
		assertEquals(connection.getState(), Connection.State.DISCONNECTED);
		
		connection.toggleConnection();
		waitAWhile();
		connection.toggleConnection();
		waitAWhile();
		
		assertEquals(connection.getState(), Connection.State.DISCONNECTED);
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
		final ConnectionRecorder connectionLogger1 = new ConnectionRecorder(connection1);
		final ConnectionRecorder connectionLogger2 = new ConnectionRecorder(connection2);
		final Message dataMessage1 = new DataMessage(new File("Dummy"), "Hello world!".getBytes());
		final Message dataMessage2 = new DataMessage(new File("Dummy"), "42".getBytes());
		
		assertEquals(connection1.getState(), Connection.State.DISCONNECTED);
		assertEquals(connection2.getState(), Connection.State.DISCONNECTED);
		
		connection1.toggleConnection();
		connection2.toggleConnection();
		waitAWhile();
		
		assertEquals(connection1.getState(), Connection.State.CONNECTED);
		assertEquals(connection2.getState(), Connection.State.CONNECTED);
		
		connection1.sendMessage(dataMessage1);
		connection2.sendMessage(dataMessage2);
		connection1.toggleConnection();
		waitAWhile();
		
		assertEquals(connection1.getState(), Connection.State.DISCONNECTED);
		assertEquals(connection2.getState(), Connection.State.DISCONNECTED);
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				dataMessage2,
				Connection.State.DISCONNECTED
				), connectionLogger1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				dataMessage1,
				// The state is changed as soon as the disconnect message is received
				// That's why the logger detects the state change before the disconnect message
				// TODO should this behavior be changed?
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), connectionLogger2.getEvents());
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
	
	public static final long WAIT_DURATION = 200L;
	
	public static final void waitAWhile() {
		try {
			Thread.sleep(WAIT_DURATION);
		} catch (final InterruptedException exception) {
			exception.printStackTrace();
		}
	}
	
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
