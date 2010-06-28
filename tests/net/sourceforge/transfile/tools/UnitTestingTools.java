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

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;

/**
 * Provides helper functions for unit testing
 *
 * @author Martin Riedel
 *
 */
public final class UnitTestingTools {
	
	public static final <T> void addElementsToModel(final MutableModelAdapter model, final T... elements) {
		addArrayElementsToModel(model, elements);
	}
	
	
	public static final <T> void addArrayElementsToModel(final MutableModelAdapter model, final T[] elements) {
		for (final T e: elements)
			model.addElement(e);
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T[] modelToArray(final ListModel model) {
		final List<T> elements = new ArrayList<T>(model.getSize());
		
		for (int i = 0; i < model.getSize(); i++)
			elements.add((T) model.getElementAt(i));
		
		return (T[]) elements.toArray();
	}
	
	private UnitTestingTools() {
		// do nothing, just prevent instantiation
	}
	
}
