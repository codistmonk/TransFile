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

import static net.sourceforge.transfile.tools.Tools.getLoggerForThisMethod;
import static net.sourceforge.transfile.i18n.Translator.Helpers.translate;

import java.util.logging.Level;

import javax.swing.SwingUtilities;

import net.sourceforge.transfile.ui.swing.StatusService.StatusMessage;

/**
 * Handles uncaught exceptions
 *
 * @author Martin Riedel
 *
 */
class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
	
	/*
	 * Reference to the StatusService used by this UncaughtExceptionHandler
	 */
	private final StatusService statusService;
	
	
	/**
	 * Constructs a new {@code UncaughtExceptionHandler} instance
	 * 
	 * @param statusService
	 * <br />The {@link StatusService} to use
	 * <br />Should not be null
	 */
	public UncaughtExceptionHandler(final StatusService statusService) {
		this.statusService = statusService;
	}

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
	protected void handleUncaughtException(final Throwable exception) {
		exception.printStackTrace();
		getLoggerForThisMethod().log(Level.SEVERE, "unexpected error / uncaught exception", exception);
		this.statusService.postStatusMessage(exception.getCause() == null ? translate(new StatusMessage("status_unexpected_error"), exception.getClass().getSimpleName())
																	 : translate(new StatusMessage("status_unexpected_error_with_cause"), exception.getClass().getSimpleName(), exception.getCause().getClass().getSimpleName()));
	}

}