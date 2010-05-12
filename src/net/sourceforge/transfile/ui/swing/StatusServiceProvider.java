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

/**
 * Receives new status messages and stores them in a last-in first-out fashion
 *
 * @author Martin Riedel
 *
 */
interface StatusServiceProvider extends Iterable<StatusMessage> {
	
	/**
	 * Posts the provided status message, making it the newest one
	 * 
	 * @param message 
	 * <br />the status message to post
	 * <br />should not be null
	 * <br />should not be empty
	 */
	public void postStatusMessage(final StatusMessage message);
	
	/**
	 * Adds a {@link StatusListener} that will be informed about new status messages
	 * 
	 * @param listener
	 * <br />the listener to inform about new status messages
	 * <br />should not be null
	 */
	public void addStatusListener(final StatusListener listener);

}
