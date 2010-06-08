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

import net.sourceforge.transfile.tools.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-08)
 *
 */
public class DataRequestMessage extends AbstractDataMessage {
	
	private final int requestedByteCount;
	
	/**
	 * 
	 * @param sourceFile
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param firstByteOffset
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 * @param requestedByteCount
	 * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
	 */
	public DataRequestMessage(File sourceFile, long firstByteOffset, final int requestedByteCount) {
		super(sourceFile, firstByteOffset);
		this.requestedByteCount = requestedByteCount;
	}
	
	/**
	 * 
	 * @return
	 * <br>Range: {@code [0 .. Integer.MAX_VALUE]}
	 */
	public final int getRequestedByteCount() {
		return this.requestedByteCount;
	}
	
	@Override
	public final boolean equals(final Object object) {
		final DataRequestMessage that = Tools.cast(this.getClass(), object);
		
		return
			this == that ||
			that != null &&
			this.getSourceFile().equals(that.getSourceFile()) &&
			this.getFirstByteOffset() == that.getFirstByteOffset() &&
			this.getRequestedByteCount() == that.getRequestedByteCount();
	}
	
	@Override
	public final int hashCode() {
		return (int) (this.getSourceFile().hashCode() + this.getFirstByteOffset() + this.getRequestedByteCount());
	}
	
	@Override
	public final String toString() {
		return "DataRequestMessage [sourceFile="
				+ this.getSourceFile() + ", firstByteOffset="
				+ this.getFirstByteOffset() + ", requestByteCount="
				+ this.getRequestedByteCount() + "]";
	}
	
	private static final long serialVersionUID = -4068725267652068795L;
	
}
