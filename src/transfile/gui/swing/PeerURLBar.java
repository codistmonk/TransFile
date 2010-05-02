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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;

import transfile.exceptions.SerializationException;
import transfile.exceptions.SerializationFileNotFoundException;
import transfile.network.PeerURL;
import transfile.settings.Settings;

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

	private static final long serialVersionUID = -8782347394069390311L;
	
	/**
	 * Initialization on Demand Holder idiom.
	 * See http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom.
	 * 
	 * @author Martin Riedel
	 *
	 */
	private static class LazyHolder {
		private static final PeerURLBar _instance = new PeerURLBar();
	}
	
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
	private static final int maxRetainedItems = Integer.parseInt(Settings.getInstance().getProperty("peerurlbar_max_retained_items"));
	
	/*
	 * The file the data model will be serialized and saved to to achieve persistence
	 */
	private final File stateFile = new File(Settings.getInstance().getCfgDir(), "PeerURLBar.state");
	
	/*
	 * A reference to the data model used by the PeerURLBar
	 */
	private final PeerURLBarModel model;

	
	/**
	 * Returns the singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static PeerURLBar getInstance() {
		return LazyHolder._instance;
	}
	
	/**
	 * Saves the PeerURLBar's data state to disk
	 * 
	 * @throws SerializationException if serializing or saving the serialized data to disk failed
	 */
	public void saveModel() throws SerializationException {
		model.saveHolder();
	}
	
	/**
	 * Constructs the PeerURLBar instance. Private since there may only be
	 * one PeerURLBar instance at most at any given time.
	 * 
	 */
	private PeerURLBar() {
		setEditable(true);
		addActionListener(new PeerURLBarListener());
		model = new PeerURLBarModel();
		setModel(model);
	}

	/**
	 * Listens for events in the PeerURLBar.
	 * 
	 * @author Martin Riedel
	 *
	 */
	private class PeerURLBarListener implements ActionListener {
		
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
		private final ComboBoxItemsHolder holder;
		
		/**
		 * Constructs a new PeerURLBarModel, attempting to load a previously serialized ItemsHolder
		 * instance from disk, or, failing that, creating a new ItemsHolder object.
		 * 
		 */
		public PeerURLBarModel() {
			ComboBoxItemsHolder holder = null;
			
			try {
				holder = ComboBoxItemsHolder.load(stateFile);
			} catch(SerializationFileNotFoundException e) {
				//TODO LOG
				e.printStackTrace(); //TODO remove
			} catch(SerializationException e) {
				//TODO LOG
				e.printStackTrace(); //TODO remove
			} finally {
				if(holder == null)
					holder = new ComboBoxItemsHolder(maxRetainedItems, stateFile);
			}
			
			this.holder = holder; 
		}
		
		/**
		 * Saves the state of the ComboBoxItemsHolder to disk
		 * 
		 * @throws SerializationException if serializing or saving the serialized data to disk failed
		 */
		public void saveHolder() throws SerializationException {
			holder.save();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addElement(Object e) {
			if(holder.items.contains(e))
				return;
			
			holder.items.add(0, e);
			while(holder.items.size() > maxRetainedItems)
				holder.items.remove(holder.items.size() - 1);
			fireContentsChanged(PeerURLBar.this, 0, holder.items.size() - 1);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void insertElementAt(Object e, int ePosition) {
			if(holder.items.contains(e))
				return;
			
			holder.items.add(ePosition, e);
			while(holder.items.size() > maxRetainedItems)
				holder.items.remove(holder.items.size() - 1);
			fireContentsChanged(PeerURLBar.this, ePosition, holder.items.size() - 1);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void removeElement(Object e) {
			removeElementAt(holder.items.indexOf(e));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void removeElementAt(int ePosition) {
			holder.items.remove(ePosition);
			if(ePosition == holder.items.size())
				fireIntervalRemoved(PeerURLBar.this, ePosition, ePosition);
			else
				fireContentsChanged(PeerURLBar.this, ePosition, holder.items.size() - 1);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getSelectedItem() {
			return holder.selectedItem;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setSelectedItem(Object itemToSelect) {
			holder.selectedItem = itemToSelect;
			int i = holder.items.indexOf(itemToSelect);
			// if the selected item already exists (user picked it from the drop-down list or re-entered it)
			if(i >= 0) {
				// make the selected item the "youngest" item (move it to the first position in the list)
				holder.items.remove(i);
				holder.items.add(0, itemToSelect);
			// if it's a new item
			} else {
				if(holder.selectedItem instanceof String) {
					String selectedString = (String) holder.selectedItem;
					// if the selected item is a string and doesn't start with the correct protocol prefix
					if(!selectedString.startsWith(PeerURL.protocolPrefix)) {
						// prepend the protocol prefix
						//TODO be smarter, recognize at least incomplete or maybe even mistyped prefixes and correct them
						selectedString = PeerURL.protocolPrefix + selectedString;
						holder.selectedItem = selectedString;
					}
				}
			}
			fireContentsChanged(PeerURLBar.this, 0, holder.items.size() - 1);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getElementAt(int index) {			
			return holder.items.get(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getSize() {
			return holder.items.size();
		}
		
	}
	
}
