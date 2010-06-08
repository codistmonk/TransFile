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

package net.sourceforge.transfile.operations.messages;

import java.io.File;

import net.sourceforge.transfile.tools.Tools;

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
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 */
	public final long getSourceByteCount() {
		return this.sourceByteCount;
	}
	
	@Override
	public final boolean equals(final Object object) {
		final FileOfferMessage that = Tools.cast(this.getClass(), object);
		
		return this == that || that != null && this.getSourceFile().equals(that.getSourceFile()) && this.getSourceByteCount() == that.getSourceByteCount();
	}
	
	@Override
	public final int hashCode() {
		return (int) (this.getSourceFile().hashCode() + this.getSourceByteCount());
	}
	
	@Override
	public final String toString() {
		return "FileOfferMessage [sourceFile="
				+ this.getSourceFile() + ", sourceByteCount="
				+ this.getSourceByteCount() + "]";
	}
	
	private static final long serialVersionUID = 1615168356494289103L;
	
}
