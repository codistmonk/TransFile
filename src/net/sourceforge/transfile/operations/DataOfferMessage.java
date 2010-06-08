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
import java.util.Arrays;

import net.sourceforge.transfile.tools.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class DataOfferMessage extends AbstractDataMessage {
	
	private final byte[] bytes;
	
	/**
	 * 
	 * @param sourceFile
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param firstByteOffset
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 * @param bytes
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public DataOfferMessage(final File sourceFile, final long firstByteOffset, final byte... bytes) {
		super(sourceFile, firstByteOffset);
		this.bytes = bytes;
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final byte[] getBytes() {
		return this.bytes;
	}
	
	@Override
	public final int hashCode() {
		return (int) this.getFirstByteOffset() + Arrays.hashCode(this.bytes);
	}
	
	@Override
	public final boolean equals(final Object object) {
		final DataOfferMessage that = Tools.cast(this.getClass(), object);
		
		return this == that || that != null && this.getSourceFile().equals(that.getSourceFile()) && this.getFirstByteOffset() == that.getFirstByteOffset() && Arrays.equals(this.getBytes(), that.getBytes());
	}
	
	@Override
	public final String toString() {
		return "DataMessage [firstByteOffset=" + this.getFirstByteOffset() + ", data=" + Arrays.toString(this.bytes) + "]";
	}
	
	private static final long serialVersionUID = 8990157032564141377L;
	
}
