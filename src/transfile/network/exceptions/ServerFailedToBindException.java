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
 * Thrown when a ServerThread fails to bind to the specified port.
 * 
 * @author Martin Riedel
 *
 */
public class ServerFailedToBindException extends ServerException {
	
	/*
	 * The port that the server failed to bind to
	 */
	private final int port;

	private static final long serialVersionUID = -2643404778728431801L;

	/**
	 * 
	 * @param port the port that the server failed to bind to
	 */
	public ServerFailedToBindException(final int port) {		
		this.port = port;
	}

	/**
	 * 
	 * @param port the port that the server failed to bind to
	 * @param cause the exception that caused the binding attempt to fail
	 */
	public ServerFailedToBindException(final int port, Throwable cause) {
		super(cause);
		this.port = port;
	}
	
	/**
	 * 
	 * @return the port that the server failed to bind to
	 */
	public final int getPort() {
		return port;
	}

}
