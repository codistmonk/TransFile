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

package net.sourceforge.transfile.network.exceptions;

/**
 * TODO doc
 *
 * @author Martin Riedel
 *
 */
public class ConnectBindException extends ConnectException {

	private static final long serialVersionUID = 8195049437320084634L;

	/**
	 * Constructs a new instance
	 * TODO doc
	 */
	public ConnectBindException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructs a new instance
	 * TODO doc
	 * @param message
	 */
	public ConnectBindException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructs a new instance
	 * TODO doc
	 * @param cause
	 */
	public ConnectBindException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructs a new instance
	 * TODO doc
	 * @param message
	 * @param cause
	 */
	public ConnectBindException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
