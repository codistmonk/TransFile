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

package net.sourceforge.transfile.settings.exceptions;

/**
 * Base exception class for errors caused by deficient configuration integrity
 *
 * @author Martin Riedel
 *
 */
public class SettingsException extends RuntimeException {

	private static final long serialVersionUID = -4828164371123273282L;
	
	/*
	 * The configuration key which the exception concerns
	 */
	private final String key;

	
	/**
	 * 
	 * 
	 * @param key
	 * <br />The configuration key this exception is concerned about
	 * <br />Should not be null
	 */
	public SettingsException(final String key) {
		super();
		this.key = key;
	}

	/**
	 * 
	 * 
	 * @param key
	 * <br />The configuration key this exception is concerned about
	 * <br />Should not be null
	 * @param cause
	 * <br />The exception that caused this exception to be thrown
	 * <br />Should not be null
	 */
	public SettingsException(final String key, final Throwable cause) {
		super(cause);
		this.key = key;
	}
	
	/**
	 * 
	 * @return the configuration key whose value was deemed illegal
	 */
	public String getKey() {
		return key;
	}

}
