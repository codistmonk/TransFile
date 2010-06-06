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

import net.sourceforge.transfile.operations.Operation.State;
import net.sourceforge.transfile.tools.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public class StateMessage extends AbstractOperationMessage {
	
	private final State state;
	
	/**
	 * 
	 * @param sourceFile
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param state
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public StateMessage(final File sourceFile, final State state) {
		super(sourceFile);
		this.state = state;
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final State getState() {
		return this.state;
	}
	
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((this.state == null) ? 0 : this.state.hashCode());
		
		return result;
	}
	
	@Override
	public final boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		final StateMessage that = Tools.cast(this.getClass(), object);
		
		return that != null && this.getSourceFile().equals(that.getSourceFile()) && this.getState() == that.getState();
	}
	
	@Override
	public final String toString() {
		return "StateMessage [sourceFile=" + this.getSourceFile() + ", state=" + this.getState() + "]";
	}
	
	private static final long serialVersionUID = 8830383854291087890L;
	
}
