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

public class SerializationFileNotFoundException extends SerializationException {

	private static final long serialVersionUID = 8165059832840873632L;

	public SerializationFileNotFoundException() {

	}

	public SerializationFileNotFoundException(String arg0) {
		super(arg0);
	}

	public SerializationFileNotFoundException(Throwable arg0) {
		super(arg0);
	}

	public SerializationFileNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
