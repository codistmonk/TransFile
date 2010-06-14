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
import java.io.IOException;
import java.util.Arrays;

import net.sourceforge.transfile.operations.AbstractConnectionTestBase.ConnectionRecorder;
import net.sourceforge.transfile.operations.Operation.State;
import net.sourceforge.transfile.operations.messages.DataOfferMessage;
import net.sourceforge.transfile.operations.messages.DataRequestMessage;
import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.FileOfferMessage;
import net.sourceforge.transfile.operations.messages.Message;
import net.sourceforge.transfile.operations.messages.StateMessage;
import net.sourceforge.transfile.tools.Tools;

import org.junit.Test;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class ReceiveOperationTest extends AbstractOperationTestBase {
	
	@Test
	public final void testReceiveRequestMessage() {
		final DummyConnection connection = new DummyConnection();
		
		connection.setRemoteConnection(connection);
		connection.connect();
		
		assertEquals(Connection.State.CONNECTED, connection.getState());
		
		final ReceiveOperation operation = this.createOperation(connection, SOURCE_FILE);
		
		operation.getController().start();
		// Should terminate normally
	}
	
	@Test
	public final void testRequestData() {
		final Connection[] connections = this.createMatchingConnectionPair();
		final Connection connection1 = connections[0];
		final Connection connection2 = connections[1];
		final ConnectionRecorder connectionRecorder1 = new ConnectionRecorder(connection1);
		final ConnectionRecorder connectionRecorder2 = new ConnectionRecorder(connection2);
		
		assertEquals(Connection.State.DISCONNECTED, connection1.getState());
		assertEquals(Connection.State.DISCONNECTED, connection2.getState());
		
		connection1.connect();
		connection2.connect();
		waitAWhile();
		
		final File sourceFile = SOURCE_FILE;
		final ReceiveOperation operation = this.createOperation(connection1, sourceFile);
		final File destinationFile = operation.getDestinationFileProvider().getDestinationFile("");
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		assertEquals(0L, destinationFile.length());
		
		// TODO test changing the order of the following 2 instructions
		operation.getController().start();
		connection2.sendMessage(acceptMessage);
		connection2.disconnect();
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), connectionRecorder1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataRequestMessage(sourceFile, 0L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				Connection.State.DISCONNECTED
		), connectionRecorder2.getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING
		), operationRecorder.getEvents());
	}
	
	@Test
	public final void testReceiveData() {
		final Connection[] connections = this.createMatchingConnectionPair();
		final Connection connection1 = connections[0];
		final Connection connection2 = connections[1];
		final ConnectionRecorder connectionRecorder1 = new ConnectionRecorder(connection1);
		final ConnectionRecorder connectionRecorder2 = new ConnectionRecorder(connection2);
		
		assertEquals(Connection.State.DISCONNECTED, connection1.getState());
		assertEquals(Connection.State.DISCONNECTED, connection2.getState());
		
		connection1.connect();
		connection2.connect();
		waitAWhile();
		
		final File sourceFile = SOURCE_FILE;
		final ReceiveOperation operation = this.createOperation(connection1, sourceFile);
		final File destinationFile = operation.getDestinationFileProvider().getDestinationFile("");
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		assertEquals(0L, destinationFile.length());
		
		operation.getController().start();
		connection2.sendMessage(acceptMessage);
		connection2.sendMessage(new DataOfferMessage(sourceFile, 0L, (byte) '4'));
		connection2.sendMessage(new DataOfferMessage(sourceFile, 1L, (byte) '2'));
		waitUntilState(operation, State.DONE, WAIT_DURATION);
		connection2.disconnect();
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				new DataOfferMessage(sourceFile, 0L, (byte) '4'),
				new DataOfferMessage(sourceFile, 1L, (byte) '2'),
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
				), connectionRecorder1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataRequestMessage(sourceFile, 0L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new DataRequestMessage(sourceFile, 1L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new DataRequestMessage(sourceFile, 2L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new StateMessage(sourceFile, Operation.State.DONE),
				Connection.State.DISCONNECTED
		), connectionRecorder2.getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING,
				0.5,
				1.0,
				Operation.State.DONE
		), operationRecorder.getEvents());
		assertEquals(destinationFile.length(), sourceFile.length());
	}
	
	@Test
	public final void testReceiveDataWithPause() {
		final Connection[] connections = this.createMatchingConnectionPair();
		final Connection connection1 = connections[0];
		final Connection connection2 = connections[1];
		final ConnectionRecorder connectionRecorder1 = new ConnectionRecorder(connection1);
		final ConnectionRecorder connectionRecorder2 = new ConnectionRecorder(connection2);
		
		assertEquals(Connection.State.DISCONNECTED, connection1.getState());
		assertEquals(Connection.State.DISCONNECTED, connection2.getState());
		
		connection1.connect();
		connection2.connect();
		waitAWhile();
		
		final File sourceFile = SOURCE_FILE;
		final ReceiveOperation operation = this.createOperation(connection1, sourceFile);
		final File destinationFile = operation.getDestinationFileProvider().getDestinationFile("");
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		assertEquals(0L, destinationFile.length());
		
		operation.getController().start();
		connection2.sendMessage(acceptMessage);
		connection2.sendMessage(new DataOfferMessage(sourceFile, 0L, (byte) '4'));
		connection2.sendMessage(new StateMessage(sourceFile, State.PAUSED));
		connection2.sendMessage(new StateMessage(sourceFile, State.PROGRESSING));
		operation.getController().start();
		connection2.sendMessage(new DataOfferMessage(sourceFile, 1L, (byte) '2'));
		waitUntilState(operation, State.DONE, WAIT_DURATION);
		connection2.disconnect();
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				new DataOfferMessage(sourceFile, 0L, (byte) '4'),
				new StateMessage(sourceFile, Operation.State.PAUSED),
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataOfferMessage(sourceFile, 1L, (byte) '2'),
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), connectionRecorder1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataRequestMessage(sourceFile, 0L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new DataRequestMessage(sourceFile, 1L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new StateMessage(sourceFile, Operation.State.PAUSED),
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataRequestMessage(sourceFile, 1L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new DataRequestMessage(sourceFile, 2L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new StateMessage(sourceFile, Operation.State.DONE),
				Connection.State.DISCONNECTED
		), connectionRecorder2.getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING,
				0.5,
				Operation.State.PAUSED,
				Operation.State.PROGRESSING,
				1.0,
				Operation.State.DONE
		), operationRecorder.getEvents());
		assertEquals(destinationFile.length(), sourceFile.length());
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
	public final ReceiveOperation createOperation(final Connection connection, final File file) {
		try {
			return new ReceiveOperation(connection, new FileOfferMessage(file), new TemporaryDestinationFileProvider(file));
		} catch (final IOException exception) {
			return Tools.throwUnchecked(exception);
		}
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static class TemporaryDestinationFileProvider implements ReceiveOperation.DestinationFileProvider {
		
		private File file;
		
		/**
		 * 
		 * @param sourceFile
		 * <br>Can be null
		 * <br>Shared parameter
		 * @throws IOException if a temporary destination file cannot be created
		 */
		TemporaryDestinationFileProvider(final File sourceFile) throws IOException {
			this.file = File.createTempFile(sourceFile.getName(), null);
			this.file.deleteOnExit();
		}
		
		@Override
		public final File getDestinationFile(final String fileName) {
			return this.file;
		}
		
	}
	
}
