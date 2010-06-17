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
public abstract class AbstractConnectionTestBase extends AbstractTestWithConnections {
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testToggleUnmatchedConnection() {
		final Connection connection = this.createUnmatchedConnection();
		final ConnectionRecorder connectionLogger = new ConnectionRecorder(connection);
		
		assertEquals(Connection.State.DISCONNECTED, connection.getState());
		
		connection.connect();
		this.waitUntilConnectionsAreReady(connection);
		
		assertEquals(Connection.State.CONNECTING, connection.getState());
		
		connection.disconnect();
		
		waitAndAssertState(Connection.State.DISCONNECTED, connection);
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.DISCONNECTED
				), connectionLogger.getEvents());
	}
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testConnectionAndDataTransfer() {
		this.createAndConnectMatchingConnectionPair();
		
		final Message dataMessage1 = new DataOfferMessage(new File("dummy"), 0L, "Hello world!".getBytes());
		final Message dataMessage2 = new DataOfferMessage(new File("dummy"), 0L, "42".getBytes());
		
		this.getConnection1().sendMessage(dataMessage1);
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(dataMessage2);
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection1().disconnect();
		
		waitAndAssertState(Connection.State.DISCONNECTED, this.getConnections());
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				dataMessage2,
				Connection.State.DISCONNECTED
				), this.getConnectionRecorder1().getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				dataMessage1,
				// The state is changed as soon as the disconnect message is received
				// That's why the logger detects the state change before the disconnect message
				// TODO should this behavior be changed?
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), this.getConnectionRecorder2().getEvents());
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	protected abstract Connection createUnmatchedConnection();
	
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
