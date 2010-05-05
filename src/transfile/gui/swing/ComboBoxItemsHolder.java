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

package transfile.gui.swing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import transfile.exceptions.SerializationException;
import transfile.exceptions.SerializationFileNotFoundException;

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
	 * The file this ComboBox items holder will be serialized and saved to to achieve persistence
	 */
	private final File stateFile;

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
	 * @param stateFile the file to serialize to to achieve persistence
	 */
	public ComboBoxItemsHolder(final int maxRetainedItems, final File stateFile) {
		this.stateFile = stateFile;
		items = new ArrayList<Object>(maxRetainedItems);
	}

	/**
	 * Loads the previously serialized and saved data model state from disk.
	 *  
	 * @throws SerializationFileNotFoundException if no ComboBoxItemsHolder has been saved to the provided file yet  
	 * @throws SerializationException if an error occurred while trying to load the model from disk
	 */
	public static ComboBoxItemsHolder load(final File stateFile) throws SerializationException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			fis = new FileInputStream(stateFile);
			ois = new ObjectInputStream(fis);
			return (ComboBoxItemsHolder) ois.readObject();
		} catch (FileNotFoundException e) {
			throw new SerializationFileNotFoundException(e);
		} catch (IOException e) {
			throw new SerializationException(e);
		} catch (ClassNotFoundException e) {
			throw new SerializationException(e);
		} finally {
			if(ois != null) {
				try {
					ois.close();
				} catch(IOException e) {
					throw new SerializationException(e);
				};
			}
			if(fis != null) {
				try {
					fis.close();
				} catch(IOException e) {
					throw new SerializationException(e);
				}
			}
		}	
	}

	/**
	 * Saves the state of the ComboBoxItemsHolder to disk
	 * 
	 * @throws SerializationException if serializing or saving the serialized data to disk failed
	 */
	public void save() throws SerializationException {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try {
			fos = new FileOutputStream(stateFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
		} catch (FileNotFoundException e) {
			throw new SerializationException(e);
		} catch (IOException e) {
			throw new SerializationException(e);
		} finally {
			if(oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					throw new SerializationException(e);
				}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					throw new SerializationException(e);
				}
			}
		}			

	}

}
