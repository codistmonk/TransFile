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
 * Base class for all serialization exceptions caused by file access errors
 *
 * @author Martin Riedel
 *
 */
public class SerializationFileException extends SerializationException {

	private static final long serialVersionUID = -8305852593523482880L;

	private final File serializationFile;

	/**
	 * Constructs a new instance

	 * @param serializationFile
	 * <br />The file the serializable object(s) was/were meant to be stored in or read from
	 * <br />Should not be null
	 */
	public SerializationFileException(final File serializationFile) {
		super();
		this.serializationFile = serializationFile;
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
	public SerializationFileException(final File serializationFile, Throwable cause) {
		super(cause);
		this.serializationFile = serializationFile;
	}
	
	/**
	 * Returns the {@link File} that the serializable object(s)  was/were meant to be stored in or read from
	 * 
	 * @return 
	 * <br />The {@link File} that the serializable object(s) was/were meant top be stored in or read from
	 * <br />Never null
	 */
	public File getSerializationFile() {
		return this.serializationFile;
	}

}
