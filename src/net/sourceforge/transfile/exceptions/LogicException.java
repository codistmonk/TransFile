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
 * Similarly to {@link LogicError} this is thrown when a logical programming error
 * is encountered. However, unlike LogicErrors, LogicExceptions may be recovered from
 * 
 * @author Martin Riedel
 *
 */
public class LogicException extends RuntimeException {

	private static final long serialVersionUID = -1985777108233507024L;

	public LogicException() {
		// exception default constructor, do nothing
	}

	public LogicException(String message) {
		super(message);
	}

	public LogicException(Throwable cause) {
		super(cause);
	}

	public LogicException(String message, Throwable cause) {
		super(message, cause);
	}

}
