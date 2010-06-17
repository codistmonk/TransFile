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
import java.io.IOException;
import java.util.Arrays;

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
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testRequestData() {
		this.createAndConnectMatchingConnectionPair();
		
		final File sourceFile = SOURCE_FILE;
		final ReceiveOperation operation = this.createOperation(this.getConnection1(), sourceFile);
		final File destinationFile = operation.getDestinationFileProvider().getDestinationFile("");
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		assertEquals(0L, destinationFile.length());
		
		// TODO test changing the order of the following 2 instructions
		operation.getController().start();
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(acceptMessage);
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().disconnect();
		waitAndAssertState(Connection.State.DISCONNECTED, this.getConnections());
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), this.getConnectionRecorder1().getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataRequestMessage(sourceFile, 0L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				Connection.State.DISCONNECTED
		), this.getConnectionRecorder2().getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING
		), operationRecorder.getEvents());
	}
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testReceiveData() {
		this.createAndConnectMatchingConnectionPair();
		
		final File sourceFile = SOURCE_FILE;
		final ReceiveOperation operation = this.createOperation(this.getConnection1(), sourceFile);
		final File destinationFile = operation.getDestinationFileProvider().getDestinationFile("");
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		assertEquals(0L, destinationFile.length());
		
		operation.getController().start();
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(acceptMessage);
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(new DataOfferMessage(sourceFile, 0L, (byte) '4'));
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(new DataOfferMessage(sourceFile, 1L, (byte) '2'));
		waitUntilState(operation, State.DONE);
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().disconnect();
		waitAndAssertState(Connection.State.DISCONNECTED, this.getConnections());
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				new DataOfferMessage(sourceFile, 0L, (byte) '4'),
				new DataOfferMessage(sourceFile, 1L, (byte) '2'),
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
				), this.getConnectionRecorder1().getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new DataRequestMessage(sourceFile, 0L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new DataRequestMessage(sourceFile, 1L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new DataRequestMessage(sourceFile, 2L, ReceiveOperation.PREFERRED_TRANSFERRED_BYTE_COUNT),
				new StateMessage(sourceFile, Operation.State.DONE),
				Connection.State.DISCONNECTED
		), this.getConnectionRecorder2().getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING,
				0.5,
				1.0,
				Operation.State.DONE
		), operationRecorder.getEvents());
		assertEquals(destinationFile.length(), sourceFile.length());
	}
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testReceiveDataWithPause() {
		this.createAndConnectMatchingConnectionPair();
		
		final File sourceFile = SOURCE_FILE;
		final ReceiveOperation operation = this.createOperation(this.getConnection1(), sourceFile);
		final File destinationFile = operation.getDestinationFileProvider().getDestinationFile("");
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		assertEquals(0L, destinationFile.length());
		
		operation.getController().start();
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(acceptMessage);
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(new DataOfferMessage(sourceFile, 0L, (byte) '4'));
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(new StateMessage(sourceFile, State.PAUSED));
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(new StateMessage(sourceFile, State.PROGRESSING));
		this.waitUntilConnectionsAreReady(this.getConnections());
		operation.getController().start();
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().sendMessage(new DataOfferMessage(sourceFile, 1L, (byte) '2'));
		waitUntilState(operation, State.DONE);
		this.waitUntilConnectionsAreReady(this.getConnections());
		this.getConnection2().disconnect();
		waitAndAssertState(Connection.State.DISCONNECTED, this.getConnections());
		
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
		), this.getConnectionRecorder1().getEvents());
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
		), this.getConnectionRecorder2().getEvents());
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
	
	@Override
	protected final Connection[] createMatchingConnectionPair() {
		return new DummyConnectionTest().createMatchingConnectionPair();
	}
	
	@Override
	protected final ReceiveOperation createOperation(final Connection connection, final File file) {
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
