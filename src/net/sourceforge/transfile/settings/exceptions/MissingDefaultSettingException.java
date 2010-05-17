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
 * <p>Thrown when a configuration key for which a default should exist in defaults.properties
 * could not be found.</p>
 * 
 * <p>Thrown by code using Settings on a case-by-case basis. There is no mechanism inside the settings package
 * enforcing default value completeness.</p>
 *
 * @author Martin Riedel
 *
 */
public class MissingDefaultSettingException extends SettingsException {

	private static final long serialVersionUID = -4482437296266276194L;


	/**
	 * Constructs a new instance
	 * 
	 * @param key 
	 * <br />The key for which a default setting was expected but not found
	 * <br />Should not be null
	 */
	public MissingDefaultSettingException(final String key) {
		super(key);
	}

	/**
	 * Constructs a new instance
	 * 
	 * @param key 
	 * <br />The key for which a default setting was expected but not found
	 * <br />Should not be null
	 * @param cause
	 * <br />The that caused this exception to be thrown
	 * <br />Should not be null
	 */
	public MissingDefaultSettingException(final String key, final Throwable cause) {
		super(key, cause);
	}

}
