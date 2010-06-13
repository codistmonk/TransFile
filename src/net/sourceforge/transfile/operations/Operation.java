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

import java.io.File;

/**
 * Common interface to receive and send operations.
 * <br>An operation manages state information (file information, transfer state)
 * and offers a listening mechanism that can be used by the UI (among others).
 * <br>The initial state is {@link State#QUEUED}.
 * <br>The initial progress is {@code 0%}.
 * <br>Communication protocol is handled by implementations of the {@link Controller} interface.
 * <br>If {@code this.getLocalFile() != null}, then {@code this.getLocalFile().getName().equals(this.getFileName)} is true.
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public interface Operation {
	
	/**
	 * 
	 * @param listener
	 * <br>Should not be null
	 */
	public abstract void addOperationListener(Listener listener);
	
	/**
	 * 
	 * @param listener
	 * <br>Can be null
	 */
	public abstract void removeOperationListener(Listener listener);
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 */
	public abstract State getState();
	
	/**
	 * 
	 * @return
	 * <br>Range: {@code [0.0 .. 1.0]}
	 */
	public abstract double getProgress();
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 */
	public abstract String getFileName();
	
	/**
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	public abstract File getLocalFile();
	
	/**
	 * 
	 * @param localFile
	 * <br>Can be null
	 */
	public abstract void setLocalFile(File localFile);
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 */
	public abstract Controller getController();
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public abstract Connection getConnection();
	
	/**
	 * Implementations of this interface handle the communication protocol.
	 * <br>This interface only specifies state messages for the operation.
	 * <br>It is up to the implementation to decide how to receive network messages
	 * (eg by attaching a listener to the connection) and respond to them.
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public interface Controller {
		
		/**
		 * Changes the operation state to {@link State#CANCELED}.
		 */
		public abstract void cancel();
		
		/**
		 * Changes the operation state to {@link State#DONE}.
		 */
		public abstract void done();
		
		/**
		 * Changes the operation state to {@link State#PAUSED}.
		 */
		public abstract void pause();
		
		/**
		 * Changes the operation state to {@link State#REMOVED}.
		 */
		public abstract void remove();
		
		/**
		 * Changes the operation state to {@link State#PROGRESSING}.
		 */
		public abstract void start();
		
	}
	
	/**
	 * This interface specifies the events that can be used by the UI to monitor the operation,
	 * or by the controller to perform communication actions.
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static interface Listener {
		
		/**
		 * Called each time the operation state changes.
		 */
		public abstract void stateChanged();
		
		/**
		 * Called each time the operation progress changes.
		 */
		public abstract void progressChanged();
		
	}
	
	/**
	 * Enum representing the overall transfer state.
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static enum State {
		
		CANCELED, DONE, PAUSED, PROGRESSING, QUEUED, REMOVED;
		
	}
	
}
