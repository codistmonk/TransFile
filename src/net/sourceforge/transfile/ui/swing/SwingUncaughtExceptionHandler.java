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

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.SwingUtilities;

/**
 * Handles uncaught exceptions
 *
 * @author Martin Riedel
 *
 */
class SwingUncaughtExceptionHandler implements UncaughtExceptionHandler {

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void uncaughtException(final Thread thread, final Throwable exception) {
		if(SwingUtilities.isEventDispatchThread()) {
			handleUncaughtException(exception);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					handleUncaughtException(exception);
				}
			});
		}
	}
	
	/**
	 * Handles uncaught exceptions
	 * 
	 * @param exception
	 * <br />An uncaught exception
	 * <br />Never null
	 */
	private void handleUncaughtException(final Throwable exception) {
		//TODO implement properly
		exception.printStackTrace();
	}

}