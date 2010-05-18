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
 * Thrown when {@link net.sourceforge.transfile.settings.Settings#getConstantAsString} 
 * fails to reflective find and/or access the requested constant.
 *
 * @author Martin Riedel
 *
 */
public class ConstantReflectionException extends SettingsException {

	private static final long serialVersionUID = 7935531235975326494L;

	/**
	 * Constructs a new instance
	 *
	 * @param fieldName
	 * <br />The name of the constant that could not be found or accesses reflectively
	 * <br />Should not be null 
	 */
	public ConstantReflectionException(String fieldName) {
		super(fieldName);
	}

	/**
	 * Constructs a new instance
	 * 
	 * @param fieldName
	 * <br />The name of the constant that could not be found or accesses reflectively
	 * <br />Should not be null
	 * @param cause
	 * <br />The exception that describes the error that caused this exception
	 * <br />Should not be null
	 */
	public ConstantReflectionException(String fieldName, Throwable cause) {
		super(fieldName, cause);
	}

}
