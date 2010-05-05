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

package transfile.network.exceptions;

/**
 * TODO ...
 * 
 * @author Martin Riedel
 *
 */
public class ConnectFailedToSetTimeoutException extends ConnectException {

	private static final long serialVersionUID = 4212248323350869232L;

	public ConnectFailedToSetTimeoutException() {

	}

	public ConnectFailedToSetTimeoutException(String message) {
		super(message);
	}

	public ConnectFailedToSetTimeoutException(Throwable cause) {
		super(cause);
	}

	public ConnectFailedToSetTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
