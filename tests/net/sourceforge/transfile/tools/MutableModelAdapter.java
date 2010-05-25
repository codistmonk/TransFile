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

package net.sourceforge.transfile.tools;

/**
 * Adapts mutable models (i.e. {@link javax.swing.MutableComboBoxModel}, 
 * {@link net.sourceforge.transfile.ui.swing.StatusList.StatusListModel}) 
 * for the use with helper functions in {@link UnitTestingTools}.
 *
 * @author Martin Riedel
 *
 */
public interface MutableModelAdapter {

	/**
	 * Adds a new element to the model
	 */
	public <T> void addElement(final T newElement);
	
}
