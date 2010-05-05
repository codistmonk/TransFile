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

package transfile.exceptions;

/**
 * This Error represents a logical programming error. It may, for instance, be thrown when a backend method receives
 * obviously invalid parameters from the GUI which should have performed a validity check on that data.
 * 
 * @author Martin Riedel
 *
 */
public class LogicError extends Error {

	private static final long serialVersionUID = 2590986221484853599L;

	public LogicError() {

	}

	public LogicError(String arg0) {
		super(arg0);
	}

	public LogicError(Throwable arg0) {
		super(arg0);
	}

	public LogicError(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
