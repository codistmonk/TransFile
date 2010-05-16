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

package net.sourceforge.transfile.ui.swing;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Stores the items in a JComboBox' data model.
 * 
 * Presently only used by PeerURLBar and primarily separate from it for the sole purpose of
 * working around a bug on MacOSX that causes the serialization of JComboBoxes or their components
 * to fail when the Aqua look and feel is in use.
 * 
 * @author Martin Riedel
 *
 */
class ComboBoxItemsHolder implements Serializable {

	private static final long serialVersionUID = -7872331666395629697L;

	/*
	 * The container the items in the combo box' drop-down menu are stored in.
	 * The ArrayList is initialized with a size equal to the maximum number of items retained.
	 */
	public final ArrayList<Object> items;

	/*
	 * The currently selected item, or null if there is no selection
	 */
	public Object selectedItem = null;
	
	/**
	 * Constructs a new ComboBoxItemsHolder WITHOUT loading state from a previously serialized instance
	 * 
	 * @param maxRetainedItems the maximum number of items in the combo box' drop-down menu
	 */
	public ComboBoxItemsHolder(final int maxRetainedItems) {
		this.items = new ArrayList<Object>(maxRetainedItems);
	}

}
