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
import java.io.IOException;
import java.util.Arrays;

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
public class ReceiveOperationTest extends AbstractOperationTestBase {
	
	@Test
	public final void testReceiveData() {
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
		final ReceiveOperation operation = this.createOperation(connection1, sourceFile);
		final OperationRecorder operationRecorder = new OperationRecorder(operation);
		final Message acceptMessage = new StateMessage(sourceFile, State.PROGRESSING);
		
		assertEquals(Operation.State.QUEUED, operation.getState());
		assertEquals(0.0, operation.getProgress(), 0.0);
		
		operation.getController().start();
		connection2.sendMessage(acceptMessage);
		connection2.sendMessage(new DataMessage(sourceFile, (byte) '4'));
		connection2.sendMessage(new DataMessage(sourceFile, (byte) '2'));
		waitUntilState(operation, State.DONE, WAIT_DURATION);
		connection2.toggleConnection();
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				acceptMessage,
				new DataMessage(sourceFile, (byte) '4'),
				new DataMessage(sourceFile, (byte) '2'),
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
				), connectionRecorder1.getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new StateMessage(sourceFile, Operation.State.PROGRESSING),
				new StateMessage(sourceFile, Operation.State.DONE),
				Connection.State.DISCONNECTED
		), connectionRecorder2.getEvents());
		assertEquals(Arrays.asList(
				(Object) Operation.State.PROGRESSING,
				0.5,
				1.0,
				Operation.State.DONE
		), operationRecorder.getEvents());
		assertEquals(operation.getMissingDestinationFileListener().destinationFileRequested().length(), sourceFile.length());
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
			return new ReceiveOperation(connection, new RequestMessage(file), new TemporaryDestinationFileProvider(file));
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
	private class TemporaryDestinationFileProvider implements ReceiveOperation.MissingDestinationFileListener {
		
		private final File file;
		
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
		public final File destinationFileRequested() {
			return this.file;
		}
		
	}
	
}