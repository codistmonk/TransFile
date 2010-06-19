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

import net.sourceforge.transfile.operations.messages.Message;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public interface Connection {
	
	/**
	 * TODO doc
	 * 
	 * @param listener
	 * <br>Should not be null
	 */
	public abstract void addConnectionListener(Listener listener);
	
	/**
	 * TODO doc
	 * 
	 * @param listener
	 * <br>Can be null
	 */
	public abstract void removeConnectionListener(Listener listener);
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 */
	public abstract String getLocalPeer();
	
	/**
	 * TODO doc
	 * 
	 * @param localPeer
	 * <br>Should not be null
	 */
	public abstract void setLocalPeer(String localPeer);
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 */
	public abstract String getRemotePeer();
	
	/**
	 * TODO doc
	 * 
	 * @param remotePeer
	 * <br>Should not be null
	 */
	public abstract void setRemotePeer(String remotePeer);
	
	/**
	 * Non-blocking operation.
	 * TODO doc
	 */
	public abstract void connect();
	
	/**
	 * Non-blocking operation.
	 * TODO doc
	 */
	public abstract void disconnect();
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	public abstract Exception getConnectionError();
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 */
	public abstract State getState();
	
	/**
	 * TODO doc
	 * 
	 * @param message
	 * <br>Should not be null
	 */
	public abstract void sendMessage(Message message);
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static interface Listener {
		
		/**
		 * TODO doc
		 * 
		 */
		public abstract void localPeerChanged();
		
		/**
		 * TODO doc
		 * 
		 */
		public abstract void remotePeerChanged();
		
		/**
		 * TODO doc
		 * 
		 */
		public abstract void stateChanged();
		
		/**
		 * TODO doc
		 * 
		 * @param message
		 * <br>Should not be null
		 * <br>Maybe shared parameter
		 */
		public abstract void messageReceived(Message message);
		
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-19)
	 *
	 */
	public static abstract class AbstractListener implements Listener {
		
		@Override
		public final void localPeerChanged() {
			this.doLocalPeerChanged();
		}
		
		@Override
		public final void messageReceived(final Message message) {
			this.doMessageReceived(message);
		}
		
		@Override
		public final void remotePeerChanged() {
			this.doRemotePeerChanged();
		}
		
		@Override
		public final void stateChanged() {
			this.doStateChanged();
		}
		
		/**
		 * Called by {@link #localPeerChanged()}.
		 * <br>The default implementation does nothing.
		 */
		protected void doLocalPeerChanged() {
			// Do nothing
		}
		
		/**
		 * Called by {@link #remotePeerChanged()}.
		 * <br>The default implementation does nothing.
		 */
		protected void doRemotePeerChanged() {
			// Do nothing
		}
		
		/**
		 * Called by {@link #stateChanged()}.
		 * <br>The default implementation does nothing.
		 */
		protected void doStateChanged() {
			// Do nothing
		}
		
		/**
		 * Called by {@link #messageReceived(Message)}.
		 * <br>The default implementation does nothing.
		 * @param message
		 * <br>Not null
		 * <br>Maybe shared
		 */
		protected void doMessageReceived(final Message message) {
			// Do nothing
		}
		
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static enum State {
		
		CONNECTED, CONNECTING, DISCONNECTED;
		
	}
	
}
