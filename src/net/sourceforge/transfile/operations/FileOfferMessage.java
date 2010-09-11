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
public class FileOfferMessage extends AbstractOperationMessage {
	
	private final long sourceByteCount;
	
	/**
	 * 
	 * @param sourceFile
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public FileOfferMessage(final File sourceFile) {
		super(sourceFile);
		this.sourceByteCount = sourceFile.length();
	}
	
	/**
	 * 
	 * @return
	 * <br>Range: {@code [0 .. Long.MAX_VALUE]}
	 */
	public final long getSourceByteCount() {
		return this.sourceByteCount;
	}
	
	private static final long serialVersionUID = 1615168356494289103L;
	
}
