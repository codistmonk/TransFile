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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import net.sourceforge.transfile.operations.AbstractConnectionTestBase.ConnectionRecorder;
import net.sourceforge.transfile.operations.Operation.State;
import net.sourceforge.transfile.operations.messages.DataOfferMessage;
import net.sourceforge.transfile.operations.messages.DataRequestMessage;
import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.Message;
import net.sourceforge.transfile.operations.messages.StateMessage;

import org.junit.Test;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class SendOperationTest extends AbstractOperationTestBase {
	
	@Test
	public final void testSendData() {
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
		
		final File sourceFile = SOURCE_FILE;
		final Operation operation = this.createOperation(connection1, sourceFile);
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message accept = new StateMessage(sourceFile, State.PROGRESSING);
		final Message dataRequest1 = new DataRequestMessage(sourceFile, 0L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT);
		final Message dataRequest2 = new DataRequestMessage(sourceFile, 1L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT);
		final Message done = new DataRequestMessage(sourceFile, 2L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		
		operation.getController().start();
		connection2.sendMessage(accept);
		connection2.sendMessage(dataRequest1);
		connection2.sendMessage(dataRequest2);
		connection2.sendMessage(done);
		waitUntilState(operation, State.DONE, WAIT_DURATION);
		connection2.toggleConnection();
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				accept,
				dataRequest1,
				dataRequest2,
				done,
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
				), connectionRecorder1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataOfferMessage(sourceFile, 0L, (byte) '4'),
				new DataOfferMessage(sourceFile, 1L, (byte) '2'),
				new StateMessage(sourceFile, Operation.State.DONE),
				Connection.State.DISCONNECTED
		), connectionRecorder2.getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING,
				0.0,
				0.5,
				1.0,
				Operation.State.DONE
		), operationRecorder.getEvents());
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Connection[] createMatchingConnectionPair() {
		return new DummyConnectionTest().createMatchingConnectionPair();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Operation createOperation(final Connection connection, final File file) {
		return new SendOperation(connection, file);
	}
	
}
