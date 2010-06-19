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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.transfile.operations.messages.DisconnectMessage;
import net.sourceforge.transfile.operations.messages.FileOfferMessage;
import net.sourceforge.transfile.tools.Tools;

import org.junit.Test;


/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-15)
 *
 */
public abstract class AbstractSessionTestBase extends AbstractTestWithConnections {
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testOfferFile() throws IOException {
		this.createAndConnectMatchingConnectionPair();
		
		final File sourceFile = AbstractOperationTestBase.SOURCE_FILE;
		final Session session = new Session(this.getConnection1(), new ReceiveOperationTest.TemporaryDestinationFileProvider(sourceFile));
		final SessionRecorder sessionRecorder = new SessionRecorder(session);
		
		session.offerFile(sourceFile);
		this.waitUntilMatchingConnectionPairAreReady();
		this.getConnection1().disconnect();
		
		waitAndAssertState(Connection.State.DISCONNECTED, this.getConnections());
		
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				Connection.State.DISCONNECTED
				), this.getConnectionRecorder1().getEvents());
		assertEquals(Arrays.asList(
				Connection.State.CONNECTING,
				Connection.State.CONNECTED,
				new FileOfferMessage(sourceFile),
				Connection.State.DISCONNECTED,
				new DisconnectMessage()
		), this.getConnectionRecorder2().getEvents());
		assertTrue(sessionRecorder.getEvents().size() == 1);
		
		final SendOperation sendOperation = Tools.cast(SendOperation.class, sessionRecorder.getEvents().get(0));
		
		assertNotNull(sendOperation);
		assertEquals(sourceFile, sendOperation.getLocalFile());
	}
	
	@Test(timeout = TEST_TIMEOUT)
	public final void testOfferAndTransfer() throws IOException, InterruptedException {
		this.createAndConnectMatchingConnectionPair();
		
		final File sourceFile = AbstractOperationTestBase.SOURCE_FILE;
		final Session localSession = new Session(this.getConnection1(), new ReceiveOperationTest.TemporaryDestinationFileProvider(sourceFile));
		final SessionRecorder localSessionRecorder = new SessionRecorder(localSession);
		final ReceiveOperationTest.TemporaryDestinationFileProvider destinationFileProvider = new ReceiveOperationTest.TemporaryDestinationFileProvider(sourceFile);
		final Session remoteSession = new Session(this.getConnection2(), destinationFileProvider);
		final SessionRecorder remoteSessionRecorder = new SessionRecorder(remoteSession);
		
		localSession.offerFile(sourceFile);
		this.waitUntilMatchingConnectionPairAreReady();
		Tools.debugPrint(localSessionRecorder.getEvents());
		Tools.debugPrint(remoteSessionRecorder.getEvents());
		
		final Thread localUser = new LocalUser((SendOperation) localSessionRecorder.getEvents().get(0));
		final Thread remoteUser = new RemoteUser((ReceiveOperation) remoteSessionRecorder.getEvents().get(0));
		
		localUser.start();
		remoteUser.start();
		
		localUser.join();
		remoteUser.join();
		
		this.waitUntilMatchingConnectionPairAreReady();
		localSession.getConnection().disconnect();
		this.waitUntilMatchingConnectionPairAreReady();
		
		assertEquals(sourceFile.length(), destinationFileProvider.getDestinationFile("").length());
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-18)
	 *
	 */
	private static abstract class AbstractUser extends Thread {
		
		private final Operation operation;
		
		/**
		 * 
		 * @param operation
		 * <br>Not null
		 * <br>Shared
		 */
		public AbstractUser(final Operation operation) {
			this.operation = operation;
		}
		
		/**
		 * 
		 * @return
		 * <br>Not null
		 * <br>Shared
		 */
		public final Operation getOperation() {
			return this.operation;
		}
		
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-18)
	 *
	 */
	private static final class LocalUser extends AbstractUser {
		
		/**
		 * 
		 * @param operation
		 * <br>Not null
		 * <br>Shared
		 */
		public LocalUser(final Operation operation) {
			super(operation);
		}
		
		@Override
		public final void run() {
			this.getOperation().getController().start();
		}
		
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-18)
	 *
	 */
	private static final class RemoteUser extends AbstractUser {
		
		/**
		 * 
		 * @param operation
		 * <br>Not null
		 * <br>Shared
		 */
		public RemoteUser(final Operation operation) {
			super(operation);
		}
		
		@Override
		public final void run() {
			this.getOperation().getController().start();
		}
		
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-08)
	 *
	 */
	public static class SessionRecorder implements Session.Listener {
		
		private final Session session;
		
		private final List<Object> events;
		
		/**
		 * 
		 * @param session
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public SessionRecorder(final Session session) {
			this.session = session;
			this.events = new ArrayList<Object>();
			
			this.getSession().addSessionListener(this);
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 */
		public final Session getSession() {
			return this.session;
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
		
		@Override
		public final void receiveOperationAdded(final ReceiveOperation receiveOperation) {
			this.getEvents().add(receiveOperation);
		}
		
		@Override
		public final void sendOperationAdded(final SendOperation sendOperation) {
			this.getEvents().add(sendOperation);
		}
		
	}
	
}
