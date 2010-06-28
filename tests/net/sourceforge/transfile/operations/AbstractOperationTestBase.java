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

import net.sourceforge.transfile.operations.Operation.State;
import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.StateMessage;
import net.sourceforge.jenerics.Tools;

import org.junit.Test;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public abstract class AbstractOperationTestBase extends AbstractTestWithConnections {
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testNoInfiniteLoop() {
		final DummyConnection connection = DummyConnection.createDummyConnectionConnectedToItself();
		
		this.waitUntilConnectionsAreReady(connection);
		
		assertEquals(Connection.State.CONNECTED, connection.getState());
		
		{
			final Operation operation = this.createOperation(connection, SOURCE_FILE);
			Tools.debugPrint(operation);
			
			operation.getController().start();
			operation.getController().pause();
		}
		{
			final Operation operation = this.createOperation(connection, SOURCE_FILE);
			Tools.debugPrint(operation);
			
			operation.getController().pause();
			operation.getController().start();
		}
		// Should terminate normally
	}
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testStartPauseResumeCancelRemove() {
		this.createAndConnectMatchingConnectionPair();
		
		final File sourceFile = SOURCE_FILE;
		final Operation operation = this.createOperation(this.getConnection1(), sourceFile);
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		
		assertEquals(State.QUEUED, operation.getState());
		
		operation.getController().start();
		this.waitUntilConnectionsAreReady(this.getConnections());
		
		assertEquals(State.PROGRESSING, operation.getState());
		
		operation.getController().pause();
		this.waitUntilConnectionsAreReady(this.getConnections());
		
		assertEquals(State.PAUSED, operation.getState());
		
		operation.getController().start();
		this.waitUntilConnectionsAreReady(this.getConnections());
		
		assertEquals(State.PROGRESSING, operation.getState());
		
		operation.getController().cancel();
		this.waitUntilConnectionsAreReady(this.getConnections());
		
		assertEquals(State.CANCELED, operation.getState());
		
		operation.getController().remove();
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection1().disconnect();
		waitAndAssertState(Connection.State.DISCONNECTED, this.getConnections());
		
		assertEquals(State.REMOVED, operation.getState());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				Connection.State.DISCONNECTED
		), this.getConnectionRecorder1().getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, State.PROGRESSING),
				new StateMessage(sourceFile, State.PAUSED),
				new StateMessage(sourceFile, State.PROGRESSING),
				new StateMessage(sourceFile, State.CANCELED),
				new StateMessage(sourceFile, State.REMOVED),
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), this.getConnectionRecorder2().getEvents());
		assertEquals(Arrays.asList(
				State.PROGRESSING,
				State.PAUSED,
				State.PROGRESSING,
				State.CANCELED,
				State.REMOVED
		), operationRecorder.getEvents());
	}
	
	/**
	 * TODO doc
	 * 
	 * @param operation
	 * <br>Should not be null
	 * @param state
	 * <br>Can be null
	 */
	public static final void waitUntilState(final Operation operation, final State state) {
		while (operation.getState() != state) {
			Thread.yield();
		}
	}
	
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
	protected abstract Operation createOperation(Connection connection, File file);
	
	public static final File SOURCE_FILE = new File("tests/" + ReceiveOperationTest.class.getPackage().getName().replaceAll("\\.", "/") + "/data.txt");
	
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
			
			this.getOperation().addOperationListener(this);
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
