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

import static net.sourceforge.transfile.operations.AbstractConnectionTestBase.WAIT_DURATION;
import static net.sourceforge.transfile.operations.AbstractConnectionTestBase.waitAWhile;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.transfile.operations.AbstractConnectionTestBase.ConnectionRecorder;
import net.sourceforge.transfile.operations.Operation.State;

import org.junit.Test;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public abstract class AbstractOperationTestBase {
	
	@Test
	public final void testFullOperationCycle() {
		final Connection[] connections = this.createMatchingConnectionPair();
		final Connection connection1 = connections[0];
		final Connection connection2 = connections[1];
		final ConnectionRecorder connectionLogger1 = new ConnectionRecorder(connection1);
		final ConnectionRecorder connectionLogger2 = new ConnectionRecorder(connection2);
		
		assertEquals(connection1.getState(), Connection.State.DISCONNECTED);
		assertEquals(connection2.getState(), Connection.State.DISCONNECTED);
		
		connection1.toggleConnection();
		connection2.toggleConnection();
		waitAWhile();
		
		final File sourceFile = new File("tests/" + this.getClass().getPackage().getName().replaceAll("\\.", "/") + "/data.txt");
		final Operation operation = this.createOperation(connection1, sourceFile);
		final OperationRecorder operationLogger = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		
		operation.getController().start();
		connection2.sendMessage(acceptMessage);
		waitUntilState(operation, State.DONE, WAIT_DURATION);
		connection2.toggleConnection();
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
				), connectionLogger1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataMessage(sourceFile, (byte) '4'),
				new DataMessage(sourceFile, (byte) '2'),
				Connection.State.DISCONNECTED
		), connectionLogger2.getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING,
				0.5,
				1.0,
				Operation.State.DONE
		), operationLogger.getEvents());
	}
	
	/**
	 * TODO doc
	 * 
	 * @param operation
	 * <br>Should not be null
	 * @param state
	 * <br>Should not be null
	 * @param timeout in milliseconds
	 * <br>Range: {@code [0 .. Long.MAX_VALUE]}
	 */
	public static final void waitUntilState(final Operation operation, final State state, final long timeout) {
		final long maximumTime = System.currentTimeMillis() + timeout;
		
		while (System.currentTimeMillis() <= maximumTime && operation.getState() != state) {
			Thread.yield();
		}
	}
	
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
	 * @param connection
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param file
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public abstract Operation createOperation(Connection connection, File file);
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static class OperationRecorder implements Operation.Listener {
		
		private final Operation operation;
		
		private final List<Object> events;
		
		/**
		 * 
		 * @param operation
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public OperationRecorder(final Operation operation) {
			this.operation = operation;
			this.events = new ArrayList<Object>();
			
			this.getOperation().addConnectionListener(this);
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public final Operation getOperation() {
			return this.operation;
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
		public final void progressChanged() {
			this.getEvents().add(this.getOperation().getProgress());
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void stateChanged() {
			this.getEvents().add(this.getOperation().getState());
		}
		
	}
	
}
