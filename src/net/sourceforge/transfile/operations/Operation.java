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
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public interface Operation {
	
	/**
	 * 
	 * TODO doc
	 * @param listener
	 * <br>Should not be null
	 */
	public abstract void addOperationListener(Listener listener);
	
	/**
	 * 
	 * TODO doc
	 * @param listener
	 * <br>Can be null
	 */
	public abstract void removeOperationListener(Listener listener);
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 */
	public abstract State getState();
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>Range: {@code [0.0 .. 1.0]}
	 */
	public abstract double getProgress();
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 */
	public abstract String getFileName();
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A possibly null value
	 */
	public abstract File getLocalFile();
	
	/**
	 * 
	 * TODO doc
	 * @param localFile
	 * <br>Can be null
	 */
	public abstract void setLocalFile(File localFile);
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 */
	public abstract Controller getController();
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public abstract Connection getConnection();
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public interface Controller {
		
		public abstract void cancel();
		
		public abstract void done();
		
		public abstract void pause();
		
		public abstract void remove();
		
		public abstract void start();
		
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static interface Listener {
		
		public abstract void stateChanged();
		
		public abstract void progressChanged();
		
	}
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public static enum State {
		
		CANCELED, DONE, PAUSED, PROGRESSING, QUEUED, REMOVED;
		
		private State nextStateOnCancel;
		
		private State nextStateOnDone;
		
		private State nextStateOnPause;
		
		private State nextStateOnRemove;
		
		private State nextStateOnStart;
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 * @throws IllegalStateException if this state cannot handle the requested transition
		 */
		public final State getNextStateOnCancel() {
			return this.checkNotNull(this.nextStateOnCancel);
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 * @throws IllegalStateException if this state cannot handle the requested transition
		 */
		public final State getNextStateOnDone() {
			return this.checkNotNull(this.nextStateOnDone);
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 * @throws IllegalStateException if this state cannot handle the requested transition
		 */
		public final State getNextStateOnPause() {
			return this.checkNotNull(this.nextStateOnPause);
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 * @throws IllegalStateException if this state cannot handle the requested transition
		 */
		public final State getNextStateOnRemove() {
			return this.checkNotNull(this.nextStateOnRemove);
		}
		
		/**
		 * 
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 * @throws IllegalStateException if this state cannot handle the requested transition
		 */
		public final State getNextStateOnStart() {
			return this.checkNotNull(this.nextStateOnStart);
		}
		
		/**
		 * TODO doc
		 * 
		 * @param nextState
		 * <br>Can be null
		 * <br>Shared parameter
		 * @return
		 * <br>A non-null value
		 * <br>A shared value
		 * @throws IllegalStateException if {@code nextState} is null
		 */
		private final State checkNotNull(final State nextState) {
			if (nextState == null) {
				throw new IllegalStateException(this.toString());
			}
			
			return nextState;
		}
		
		static {
			CANCELED.nextStateOnRemove = REMOVED;
			CANCELED.nextStateOnStart = PROGRESSING;
			
			DONE.nextStateOnRemove = REMOVED;
			
			PAUSED.nextStateOnCancel = CANCELED;
			PAUSED.nextStateOnStart = PROGRESSING;
			
			PROGRESSING.nextStateOnCancel = CANCELED;
			PROGRESSING.nextStateOnDone = DONE;
			PROGRESSING.nextStateOnPause = PAUSED;
			
			QUEUED.nextStateOnRemove = REMOVED;
			QUEUED.nextStateOnStart = PROGRESSING;
		}
		
	}
	
}
