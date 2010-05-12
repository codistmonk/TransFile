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

package net.sourceforge.transfile.ui.swing;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps track of status messages in a LIFO (last-in first-out) fashion
 *
 * @author Martin Riedel
 *
 */
class StatusService extends LinkedList<StatusMessage> implements StatusServiceProvider {
	
	private static final long serialVersionUID = 1083092863888029986L;

	/*
	 * List of all registered StatusListeners to notify about status changes
	 */
	private List<StatusListener> statusListeners = new LinkedList<StatusListener>();
	

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void postStatusMessage(final StatusMessage message) {
		// StatusService is LIFO, so add the new message at the beginning of the list
		super.add(0, message);
		
		// inform all registered StatusListeners about the new status message
		for(StatusListener listener: statusListeners)
			listener.onNewStatusMessage(message);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void addStatusListener(StatusListener listener) {
		this.statusListeners.add(listener);
	}

}
