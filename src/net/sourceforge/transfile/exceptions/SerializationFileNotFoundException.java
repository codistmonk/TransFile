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

import java.io.File;

/**
 * 
 * Thrown when a (de)serialization process fails due to the target (source) file being absent
 *
 * @author Martin Riedel
 *
 */
public class SerializationFileNotFoundException extends SerializationFileException {

	private static final long serialVersionUID = 8165059832840873632L;

	/**
	 * Constructs a new instance

	 * @param serializationFile
	 * <br />The file the serializable object(s) was/were meant to be stored in or read from
	 * <br />Should not be null
	 */
	public SerializationFileNotFoundException(final File serializationFile) {
		super(serializationFile);
	}

	/**
	 * Constructs a new instance
	 * 
	 * @param serializationFile
	 * <br />The file the serializable object(s) was/were meant to be stored in or read from
	 * <br />Should not be null
	 * @param cause
	 * <br />The {@code Throwable} that caused this exception to be thrown
	 * <br />Should not be null
	 */
	public SerializationFileNotFoundException(final File serializationFile, Throwable cause) {
		super(serializationFile, cause);
	}

}
