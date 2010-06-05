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

import static net.sourceforge.transfile.tools.Tools.array;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class DummyConnectionTest extends AbstractConnectionTestBase {
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Connection[] createMatchingConnectionPair() {
		final DummyConnection connection1 = new DummyConnection();
		final DummyConnection connection2 = new DummyConnection();
		
		connection1.setRemoteConnection(connection2);
		connection2.setRemoteConnection(connection1);
		
		return array(connection1, connection2);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Connection createUnmatchedConnection() {
		return new DummyConnection();
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static abstract class AbstractConnection implements Connection {
		
		private final Collection<Listener> listeners;
		
		private String localPeer;
		
		private String remotePeer;
		
		private State state;
		
		/**
		 * 
		 * @param localPeer
		 * <br>Should not be null
		 * <br>Shared parameter
		 * @param remotePeer
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public AbstractConnection(final String localPeer, final String remotePeer) {
			this.listeners = new ArrayList<Listener>();
			this.localPeer = localPeer;
			this.remotePeer = remotePeer;
			this.state = State.DISCONNECTED;
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A new value
		 */
		public final synchronized Listener[] getListeners() {
			return this.listeners.toArray(new Listener[this.listeners.size()]);
		}
		
		@Override
		public final synchronized void addConnectionListener(final Listener listener) {
			this.listeners.add(listener);
		}
		
		@Override
		public final synchronized void removeConnectionListener(final Listener listener) {
			this.listeners.remove(listener);
		}
		
		@Override
		public final String getLocalPeer() {
			return this.localPeer;
		}
		
		@Override
		public final String getRemotePeer() {
			return this.remotePeer;
		}
		
		/**
		 * 
		 * @param localPeer
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public final void setLocalPeer(final String localPeer) {
			if (this.getState() != State.DISCONNECTED) {
				throw new IllegalStateException(this.getState().toString());
			}
			
			this.localPeer = localPeer;
		}
		
		/**
		 * 
		 * @param remotePeer
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public final void setRemotePeer(final String remotePeer) {
			if (this.getState() != State.DISCONNECTED) {
				throw new IllegalStateException(this.getState().toString());
			}
			
			this.remotePeer = remotePeer;
		}
		
		@Override
		public final State getState() {
			return this.state;
		}
		
		/**
		 * 
		 * @param state
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public final void setState(State state) {
			if (this.getState() != state) {
				this.state = state;
				
				for (final Listener listener : this.getListeners()) {
					listener.stateChanged();
				}
			}
		}
		
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static class DummyConnection extends AbstractConnection {
		
		private DummyConnection remoteConnection;
		
		public DummyConnection() {
			super("", "");
			
			this.setLocalPeer(this.toString());
			
			this.addConnectionListener(new Listener() {
				
				@Override
				public final void stateChanged() {
					// Do nothing
				}
				
				@Override
				public final void messageReceived(final Message message) {
					if (message instanceof DisconnectMessage) {
						DummyConnection.this.setState(State.DISCONNECTED);
					}
				}
				
			});
		}
		
		/**
		 * 
		 * @return
		 * <br>A possibly null value
		 * <br>A shared value
		 */
		public final DummyConnection getRemoteConnection() {
			return this.remoteConnection;
		}
		
		/**
		 * 
		 * @param remoteConnection
		 * <br>Can be null
		 * <br>Shared parameter
		 */
		public final void setRemoteConnection(final DummyConnection remoteConnection) {
			this.setRemotePeer(remoteConnection != null ? remoteConnection.toString() : "");
			
			this.remoteConnection = remoteConnection;
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void sendMessage(final Message message) {
			if (this.getRemoteConnection() != null) {
				for (final Listener listener : this.getRemoteConnection().getListeners()) {
					listener.messageReceived(message);
				}
			}
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void toggleConnection() {
			switch (this.getState()) {
			case DISCONNECTED:
				this.setState(State.CONNECTING);
				if (this.getRemoteConnection() != null && this.getRemoteConnection().getState() == State.CONNECTING) {
					this.setState(State.CONNECTED);
					this.getRemoteConnection().setState(State.CONNECTED);
				}
				break;
			case CONNECTING:
				this.setState(State.DISCONNECTED);
				break;
			case CONNECTED:
				this.sendMessage(new DisconnectMessage());
				this.setState(State.DISCONNECTED);
				break;
			}
		}
		
	}
	
}
