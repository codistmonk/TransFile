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

package transfile.gui.swing.exceptions;

import transfile.exceptions.LogicException;

/**
 * Thrown on attempts to show a {@link TopLevelPanel} that is already being shown.
 * 
 * @author Martin Riedel
 *
 */
public class DoubleShowException extends LogicException {

	private static final long serialVersionUID = -369589007320335262L;

	public DoubleShowException() {

	}

	public DoubleShowException(String message) {
		super(message);
	}

	public DoubleShowException(Throwable cause) {
		super(cause);
	}

	public DoubleShowException(String message, Throwable cause) {
		super(message, cause);
	}

}
