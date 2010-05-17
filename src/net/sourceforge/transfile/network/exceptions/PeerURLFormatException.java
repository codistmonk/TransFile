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
 * Thrown when a provided PeerURL string representation is invalid
 * 
 * @author Martin Riedel
 *
 */
public class PeerURLFormatException extends Exception {

	private static final long serialVersionUID = 2646737369506421047L;

	public PeerURLFormatException() {
		// exception default constructor, do nothing
	}

	public PeerURLFormatException(String arg0) {
		super(arg0);
	}

	public PeerURLFormatException(Throwable arg0) {
		super(arg0);
	}

	public PeerURLFormatException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
