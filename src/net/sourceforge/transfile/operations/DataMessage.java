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

package net.sourceforge.transfile.operations;

import java.io.File;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class DataMessage extends AbstractOperationMessage {
	
	private final byte[] data;
	
	/**
	 * 
	 * @param sourceFile
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param data
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public DataMessage(final File sourceFile, final byte[] data) {
		super(sourceFile);
		this.data = data;
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final byte[] getData() {
		return this.data;
	}
	
	private static final long serialVersionUID = 8990157032564141377L;
	
}
