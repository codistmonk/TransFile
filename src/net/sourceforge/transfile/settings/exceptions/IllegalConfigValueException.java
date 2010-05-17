/*
 * Copyright � 2010 Martin Riedel
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

//TODO override getMessage(), getLocalizedMessage() and toString()(?)

/**
 * Thrown when an invalid configuration settings is encountered
 * 
 * @author Martin Riedel
 *
 */
public class IllegalConfigValueException extends SettingsException {

	private static final long serialVersionUID = 451538573200525501L;
	
	/*
	 * The configuration value that was deemed illegal
	 */
	private final String value;
	

	/**
	 * Constructs a new instance
	 * 
	 * @param key the configuration key for which an illegal value was encountered
	 * @param value the value which was deemed illegal
	 */
	public IllegalConfigValueException(final String key, final String value) {
		super(key);
		
		this.value = value;
	}
	
	/**
	 * Constructs a new instance
	 * 
	 * @param key the configuration key for which an illegal value was encountered
	 * @param value the value which was deemed illegal
	 * @param cause the {@code Throwable} causing/describing the illegality of {@code value}
	 */
	public IllegalConfigValueException(final String key, final String value, final Throwable cause) {
		super(key, cause);
		
		this.value = value;
	}
	
	/**
	 * 
	 * @return the configuration value that was deemed illegal
	 */
	public String getValue() {
		return value;
	}

}
