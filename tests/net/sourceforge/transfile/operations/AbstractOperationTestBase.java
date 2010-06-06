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

import static net.sourceforge.transfile.operations.AbstractConnectionTestBase.waitAWhile;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.transfile.operations.AbstractConnectionTestBase.ConnectionRecorder;
import net.sourceforge.transfile.operations.Operation.State;
import net.sourceforge.transfile.tools.Tools;

import org.junit.Test;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public abstract class AbstractOperationTestBase {
	
	@Test
	public final void testStartPauseResumeCancelRemove() {
		final Connection[] connections = this.createMatchingConnectionPair();
		final Connection connection1 = connections[0];
		final Connection connection2 = connections[1];
		final ConnectionRecorder connectionRecorder1 = new ConnectionRecorder(connection1);
		final ConnectionRecorder connectionRecorder2 = new ConnectionRecorder(connection2);
		
		assertEquals(connection1.getState(), Connection.State.DISCONNECTED);
		assertEquals(connection2.getState(), Connection.State.DISCONNECTED);
		
		connection1.toggleConnection();
		connection2.toggleConnection();
		waitAWhile();
		
		final File sourceFile = new File("tests/" + this.getClass().getPackage().getName().replaceAll("\\.", "/") + "/data.txt");
		final Operation operation = this.createOperation(connection1, sourceFile);
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		
		assertEquals(State.QUEUED, operation.getState());
		
		operation.getController().start();
		
		assertEquals(State.PROGRESSING, operation.getState());
		
		operation.getController().pause();
		
		assertEquals(State.PAUSED, operation.getState());
		
		operation.getController().start();
		
		assertEquals(State.PROGRESSING, operation.getState());
		
		operation.getController().cancel();
		
		assertEquals(State.CANCELED, operation.getState());
		
		operation.getController().remove();
		
		assertEquals(State.REMOVED, operation.getState());
		
		Tools.debugPrint("\nconnection1 events:", connectionRecorder1.getEvents());
		Tools.debugPrint("\nconnection2 events:", connectionRecorder2.getEvents());
		Tools.debugPrint("\noperation events:", operationRecorder.getEvents());
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
