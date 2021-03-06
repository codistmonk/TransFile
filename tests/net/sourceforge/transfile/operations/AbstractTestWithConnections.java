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

import static org.junit.Assert.assertEquals;

import org.junit.Before;

import net.sourceforge.transfile.operations.AbstractConnectionTestBase.ConnectionRecorder;
import net.sourceforge.transfile.operations.Connection.State;
import net.sourceforge.jenerics.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-08)
 *
 */
public abstract class AbstractTestWithConnections {
	
	private Connection[] connections;
	
	private Connection connection1;
	
	private Connection connection2;
	
	private ConnectionRecorder connectionRecorder1;
	
	private ConnectionRecorder connectionRecorder2;
	
	@Before
	public final void before() {
		this.setConnections(null);
	}
	
	/**
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	protected final Connection[] getConnections() {
		return this.connections;
	}
	
	/**
	 * @param connections
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	protected final void setConnections(final Connection[] connections) {
		this.connections = connections;
		
		if (this.getConnections() != null) {
			this.setConnection1(this.getConnections()[0]);
			this.setConnection2(this.getConnections()[1]);
		} else {
			this.setConnection1(null);
			this.setConnection2(null);
		}
	}
	
	/**
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	protected final Connection getConnection1() {
		return this.connection1;
	}
	
	/**
	 * @param connection1
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	protected final void setConnection1(final Connection connection1) {
		this.connection1 = connection1;
		
		this.setConnectionRecorder1(this.getConnection1() != null ? new ConnectionRecorder(this.getConnection1()) : null);
	}

	/**
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	protected final Connection getConnection2() {
		return this.connection2;
	}
	
	/**
	 * @param connection2
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	protected final void setConnection2(final Connection connection2) {
		this.connection2 = connection2;
		
		this.setConnectionRecorder2(this.getConnection2() != null ? new ConnectionRecorder(this.getConnection2()) : null);
	}
	
	/**
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	protected final ConnectionRecorder getConnectionRecorder1() {
		return this.connectionRecorder1;
	}
	
	/**
	 * @param connectionRecorder1
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	protected final void setConnectionRecorder1(final ConnectionRecorder connectionRecorder1) {
		this.connectionRecorder1 = connectionRecorder1;
	}
	
	/**
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	protected final ConnectionRecorder getConnectionRecorder2() {
		return this.connectionRecorder2;
	}

	/**
	 * @param connectionRecorder2
	 * <br>Can be null
	 * <br>Shared parameter
	 */
	protected final void setConnectionRecorder2(final ConnectionRecorder connectionRecorder2) {
		this.connectionRecorder2 = connectionRecorder2;
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	protected abstract Connection[] createMatchingConnectionPair();
	
	/**
	 * TODO doc
	 * 
	 * @param connections
	 * <br>Should not be null
	 */
	protected final void waitUntilConnectionsAreReady(final Connection... connections) {
		try {
			Thread.sleep(this.getInactivityThreshold());
		} catch (final InterruptedException exception) {
			exception.printStackTrace();
		}
		
		for (final Connection connection : connections) {
			if (connection instanceof AbstractConnection) {
				while (System.currentTimeMillis() < ((AbstractConnection) connection).getLastMessageTime() + this.getInactivityThreshold()) {
					atomicWait();
				}
			}
		}
	}
	
	protected void waitUntilMatchingConnectionPairAreReady() {
		try {
			Thread.sleep(this.getInactivityThreshold());
		} catch (final InterruptedException exception) {
			Tools.throwUnchecked(exception);
		}
		
		final AbstractConnection connection1 = (AbstractConnection) this.getConnection1();
		final AbstractConnection connection2 = (AbstractConnection) this.getConnection2();
		
		long i = 0L;
		Tools.debugPrint(
				"\n", i, connection1, connection2,
				"\n", connection1.getReceivedMessageCount(), connection1.getSentMessageCount(),
				"\n", connection2.getReceivedMessageCount(), connection2.getSentMessageCount()
		);
		while (connection1.getReceivedMessageCount() != connection2.getSentMessageCount() || connection1.getSentMessageCount() != connection2.getReceivedMessageCount()) {
			++i;
			
			if (i % INFO_PERIOD == 0L) {
				Tools.debugPrint(
						"\n", i,
						"\n", connection1.getReceivedMessageCount(), connection1.getSentMessageCount(),
						"\n", connection2.getReceivedMessageCount(), connection2.getSentMessageCount()
				);
			}
			
			atomicWait();
		}
	}
	
	/**
	 * TODO doc
	 * 
	 * @return a time in milliseconds
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 */
	protected long getInactivityThreshold() {
		return 500L;
	}
	
	/**
	 * TODO doc
	 * 
	 */
	protected final void createAndConnectMatchingConnectionPair() {
		this.setConnections(this.createMatchingConnectionPair());
		Tools.debugPrint((Object[]) this.getConnections());
		assertEquals(Connection.State.DISCONNECTED, this.getConnection1().getState());
		assertEquals(Connection.State.DISCONNECTED, this.getConnection2().getState());
		
		this.getConnection1().connect();
		this.getConnection2().connect();
		
		waitAndAssertState(Connection.State.CONNECTED, this.getConnections());
	}
	
	/**
	 * Number affecting the frequency of debugPrint calls to display messages when an expected state is taking too long to occur.
	 */
	private static final long INFO_PERIOD = 100L;
	
	/**
	 * Time in milliseconds.
	 */
	public static final long TEST_TIMEOUT = 30000L;
	
	/**
	 * Time in milliseconds.
	 */
	public static final long ATOMIC_WAIT_DURATION = 10L;
	
	public static final void atomicWait() {
		Thread.yield();
		
		try {
			Thread.sleep(ATOMIC_WAIT_DURATION);
		} catch (final InterruptedException exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * TODO doc
	 * 
	 * @param state
	 * <br>Can be null
	 * @param connections
	 * <br>Should not be null
	 */
	public static final void waitAndAssertState(final State state, final Connection... connections) {
		boolean wait = true;
		
		long j = 0L;
		while (wait) {
			atomicWait();
			
			wait = false;
			
			if ((j++) % INFO_PERIOD == 0L && connections.length == 2) {
				Tools.debugPrint(connections[0].getConnectionError(), connections[1].getConnectionError());
			}
			
			for (final Connection connection : connections) {
				wait |= connection.getState() != state;
			}
		}
		
		int i = 0;
		
		for (final Connection connection : connections) {
			assertEquals("connections[" + (i++) + "]", state, connection.getState());
		}
	}
	
}
