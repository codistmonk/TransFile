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
	public abstract void addConnectionListener(Listener listener);
	
	/**
	 * 
	 * TODO doc
	 * @param listener
	 * <br>Can be null
	 */
	public abstract void removeConnectionListener(Listener listener);
	
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
	public abstract double getProgess();
	
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
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-05)
	 *
	 */
	public interface Controller {
		
		public abstract void cancel();
		
		public abstract void pause();
		
		public abstract void remove();
		
		public abstract void retry();
		
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
		
		CANCELED, DONE, PAUSED, PROGRESSING, QUEUED;
		
	}
	
}
