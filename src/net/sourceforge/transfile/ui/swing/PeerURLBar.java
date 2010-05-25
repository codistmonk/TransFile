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

import static net.sourceforge.transfile.tools.Tools.getLoggerForThisMethod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;

import net.sourceforge.transfile.exceptions.SerializationException;
import net.sourceforge.transfile.exceptions.SerializationFileInUseException;
import net.sourceforge.transfile.network.PeerURL;
import net.sourceforge.transfile.tools.Tools;


/**
 * The text bar where the user enters the "PeerURL" of the peer they wish to connect to for file transfer.
 * 
 * PeerURLBar is a lazy-loaded singleton. It must be ensured at all times that there is at most one instance
 * of PeerURLBar to avoid persistence / serialization conflicts.
 * 
 * @author Martin Riedel
 *
 */
class PeerURLBar extends JComboBox {
	
	/*
	 * The maximum number of items in the drop-down menu. Requesting to add another item
	 * when the PeerURLBar already has maxRetainedItems items will cause the oldest present
	 * item to be deleted. Then, the remaining 4 present items will be shifted one slot towards
	 * the "older" end of the list, so that the previously second oldest item is now the oldest one.
	 * Finally, the item whose addition was requested is inserted into the now free spot at the
	 * "youngest" end of the list.
	 * 
	 * Typically, entering a (valid) PeerURL and pressing enter causes such a request to add for an item
	 * to be added, with said item being the PeerURL entered.
	 */
	public final int maxRetainedItems;
	
	private static final long serialVersionUID = -8782347394069390311L;
		
	/*
	 * The file the data model will be serialized and saved to to achieve persistence
	 */
	private final File stateFile;
	
	/*
	 * A reference to the data model used by the PeerURLBar
	 */
	private PeerURLBarModel model;
	
	/*
	 * A Set of all state files already in use by instances of this class
	 */
	private static final Set<File> usedStateFiles = new HashSet<File>();
	
	/*
	 * True iff state should be saved to disk when #saveModel is called
	 */
	private final boolean persistent;

	
	/**
	 * Constructs a new instance, loading state from the specified file. State will be saved upon
	 * request ({@link #saveModel}).
	 * 
	 * @param stateFileName
	 * <br />File name (not path) of the file load state from and to save state to
	 * <br />Should not be null
	 * @param maxRetainedItems
	 * <br />The number of recent values to remember
	 * <br />Should be at least 1
	 * <br />Should not be null
	 * @throws SerializationFileInUseException if the specified file name is already in use 
	 * 
	 */
	public PeerURLBar(final String stateFileName, final int maxRetainedItems) throws SerializationFileInUseException {
		this.stateFile = new File(Tools.getUserApplicationDirectory(), stateFileName);
		
		if(usedStateFiles.contains(this.stateFile))
			throw new SerializationFileInUseException(this.stateFile);
		
		this.persistent = true;
		
		this.maxRetainedItems = maxRetainedItems;
			
		usedStateFiles.add(this.stateFile);
		
		setup();
	}
	
	/**
	 * Constructs a new instance without loading state. State will NOT be saved to,
	 * even if {@link #saveModel} is called.
	 * 
	 * @param maxRetainedItems
	 * <br />The number of recent values to remember
	 * <br />Should be at least 1
	 * <br />Should not be null
	 */
	public PeerURLBar(final int maxRetainedItems) {
		this.stateFile = null;
		this.persistent = false;
		
		this.maxRetainedItems = maxRetainedItems;
		
		setup();
	}

	/**
	 * Saves the PeerURLBar's data state to disk. Does nothing if this PeerURLBar instance has been
	 * created without setting a state file.
	 * 
	 * @throws SerializationException if serializing or saving the serialized data to disk failed
	 */
	public void saveModel() throws SerializationException {
		if(this.persistent)
			this.model.saveHolder();
	}
	
	/**
	 * Checks whether this PeerURLBar instance is persistent (saves state to file upon request)
	 * 
	 * @return true iff this PeerURLBar instance is persistent (saves state to file upon request) 
	 */
	public boolean isPersistent() {
		return this.persistent;
	}
	
	/**
	 * Getter for {@code stateFile}
	 * 
	 * @return the file serialized versions of PeerURLBar's state are written to
	 */
	protected final File getStateFile() {
		return this.stateFile;
	}
	
	private void setup() {
		setEditable(true);

		addActionListener(new PeerURLBarListener());

		this.model = new PeerURLBarModel();
		setModel(this.model);
	}

	/**
	 * Listens for events in the PeerURLBar.
	 * 
	 * @author Martin Riedel
	 *
	 */
	private class PeerURLBarListener implements ActionListener {
		
		/**
		 * Constructs a new instance
		 * 
		 */
		public PeerURLBarListener() {
			// do nothing, just allow instantiation
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {			
			if(e.getActionCommand().equals("comboBoxEdited"))
				addItem(getSelectedItem());
		}
		
	}
	
	/**
	 * Implements the PeerURLBar's data model.
	 * 
	 * @author Martin Riedel
	 *
	 */
	private class PeerURLBarModel extends AbstractListModel implements MutableComboBoxModel {
		
		private static final long serialVersionUID = -7254391160953049844L;		
			
		/*
		 * Holds the items in the PeerURLBar's drop-down menu
		 * 
		 * Invariants:
		 * holder.items.get(0) is the youngest (most recently added) item in the list
		 * holder.items.get(maxRetainedItems - 1) is the oldest item in the list
		 */
		private ComboBoxItemsHolder holder;
		
		/**
		 * Constructs a new PeerURLBarModel, attempting to load a previously serialized ItemsHolder
		 * instance from disk, or, failing that, creating a new ItemsHolder object.
		 * 
		 */
		public PeerURLBarModel() {
			if(PeerURLBar.this.isPersistent()) {
				try {
					getLoggerForThisMethod().log(Level.FINER, "attempting to load PeerURLBar state from file: " + PeerURLBar.this.getStateFile().getAbsolutePath());
					this.holder = ComboBoxItemsHolder.load(PeerURLBar.this.getStateFile());
					// maxRetainedItems may have changed since the last time state was saved
					removeExcessiveItems();
					getLoggerForThisMethod().log(Level.FINE, "successfully loaded PeerURLBar state from file: " + PeerURLBar.this.getStateFile().getAbsolutePath());
				} catch (Throwable e) {
					getLoggerForThisMethod().log(Level.WARNING, "failed to load PeerURLBar state from file: " + PeerURLBar.this.getStateFile().getAbsolutePath());
					this.holder = new ComboBoxItemsHolder(PeerURLBar.this.maxRetainedItems, PeerURLBar.this.getStateFile());
				}
			} else {
				getLoggerForThisMethod().log(Level.FINE, "not loading PeerURLBar state from file, initializing empty model");
				this.holder = new ComboBoxItemsHolder(PeerURLBar.this.maxRetainedItems, null);
			}
		}
		
		/**
		 * Saves the state of the ComboBoxItemsHolder to disk
		 * 
		 * @throws SerializationException if serializing or saving the serialized data to disk failed
		 */
		public void saveHolder() throws SerializationException {
			this.holder.save();
		}

		/**
		 * Inserts the provided element as the first element. If the model already contains {@code maxRetainedItems} elements,
		 * the last (oldest) element in the model is removed. Does not set or change the selected item. Ignores attempts to
		 * add duplicate elements without throwing an exception.
		 * 
		 * @param e the element to insert
		 */
		@Override
		public void addElement(final Object e) {
			if(this.holder.items.contains(e))
				return;
			
			this.holder.items.add(0, e);
			
			removeExcessiveItems();
			
			fireContentsChanged(PeerURLBar.this, 0, this.holder.items.size() - 1);
		}
		
		/**
		 * Operation not supported
		 * 
		 */
		@Override
		public void insertElementAt(final Object e, final int ePosition) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".insertElementAt(Object, int)");
		}

		/**
		 * Operation not supported
		 * 
		 */
		@Override
		public void removeElement(final Object e) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".removeElement(Object)");
		}

		/**
		 * Operation not supported
		 * 
		 */
		@Override
		public void removeElementAt(final int ePosition) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + ".removeElementAt(int)");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getSelectedItem() {
			return this.holder.selectedItem;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setSelectedItem(final Object itemToSelect) {
			this.holder.selectedItem = itemToSelect;
			int i = this.holder.items.indexOf(itemToSelect);
			// if the selected item already exists (user picked it from the drop-down list or re-entered it)
			if(i >= 0) {
				// make the selected item the "youngest" item (move it to the first position in the list)
				this.holder.items.remove(i);
				this.holder.items.add(0, itemToSelect);
			// if it's a new item
			} else {
				if(this.holder.selectedItem instanceof String) {
					String selectedString = (String) this.holder.selectedItem;
					// if the selected item is a string and doesn't start with the correct protocol prefix
					if(!selectedString.startsWith(PeerURL.protocolPrefix)) {
						// prepend the protocol prefix
						//TODO be smarter, recognize at least incomplete or maybe even mistyped prefixes and correct them
						selectedString = PeerURL.protocolPrefix + selectedString;
						this.holder.selectedItem = selectedString;
					}
				}
			}
			fireContentsChanged(PeerURLBar.this, 0, this.holder.items.size() - 1);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getElementAt(final int index) {			
			return this.holder.items.get(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getSize() {
			return this.holder.items.size();
		}
		
		/**
		 * Removes excessive items until the model is holding at most {@code maxRetainedItems} items
		 * 
		 */
		private void removeExcessiveItems() {
			while(this.holder.items.size() > PeerURLBar.this.maxRetainedItems)
				this.holder.items.remove(this.holder.items.size() - 1);			
		}
		
	}
	
}
