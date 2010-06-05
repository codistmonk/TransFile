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
import java.util.Arrays;
import java.util.List;

import net.sourceforge.transfile.operations.AbstractConnectionTestBase.ConnectionLogger;
import net.sourceforge.transfile.operations.AbstractConnectionTestBase.MessageMatcher;

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
		final ConnectionLogger connectionLogger1 = new ConnectionLogger(connection1);
		final ConnectionLogger connectionLogger2 = new ConnectionLogger(connection2);
		
		assertEquals(connection1.getState(), Connection.State.DISCONNECTED);
		assertEquals(connection2.getState(), Connection.State.DISCONNECTED);
		
		connection1.toggleConnection();
		connection2.toggleConnection();
		waitAWhile();
		
		final File sourceFile = new File("Dummy");
		final Operation operation = this.createOperation(connection1, sourceFile);
		final OperationLogger operationLogger = new OperationLogger(operation);
		final StartMessage acceptMessage = new StartMessage(sourceFile);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgess(), 0.0);
		
		operation.getController().start();
		connection2.sendMessage(acceptMessage);
		waitAWhile();
		connection2.toggleConnection();
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				Connection.State.DISCONNECTED,
				new MessageMatcher(DisconnectMessage.class)
				), connectionLogger1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new MessageMatcher(StartMessage.class),
				new MessageMatcher(DataMessage.class),
				Connection.State.DISCONNECTED
		), connectionLogger2.getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING,
				1.0,
				Operation.State.DONE
		), operationLogger.getEvents());
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
	
	public static class OperationLogger implements Operation.Listener {
		
		private final Operation operation;
		
		private final List<Object> events;
		
		/**
		 * 
		 * @param operation
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public OperationLogger(final Operation operation) {
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
			this.getEvents().add(this.getOperation().getProgess());
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
