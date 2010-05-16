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

package net.sourceforge.transfile.exceptions;

/**
 * Thrown when a project resource is missing from the jar
 *
 * @author Martin Riedel
 *
 */
public class MissingResourceException extends RuntimeException {

	private static final long serialVersionUID = -199651264839641226L;

	/*
	 * The resource path of the resource that could not be found
	 */
	private final String resourcePath;
	

	/**
	 * Constructs a new MissingResourceException
	 * 
	 * @param resourcePath
	 * <br />The path to the resource that could not be found
	 * <br />Should not be null
	 */
	public MissingResourceException(final String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/**
	 * Constructs a new MissingResourceException
	 * 
	 * <br />The path to the resource that could not be found
	 * <br />Should not be null
	 * @param cause
	 * <br />The exception that caused this exception to be thrown
	 * <br />Should not be null
	 */
	public MissingResourceException(final String resourcePath, final Throwable cause) {
		super(cause);
		this.resourcePath = resourcePath;
	}
	
	/**
	 * Returns the path to the resource that could not be found
	 * 
	 * @return 
	 * <br />The path to the resource that could not be found
	 * <br />Never null
	 */
	public String getResourcePath() {
		return resourcePath;
	}

}
