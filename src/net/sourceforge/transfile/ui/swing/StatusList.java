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

import static net.sourceforge.jenerics.i18n.Translator.getDefaultTranslator;
import static net.sourceforge.transfile.ui.swing.StatusService.StatusMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import net.sourceforge.transfile.exceptions.LogicError;
import net.sourceforge.jenerics.i18n.Translator;

/**
 * <p>Custom JList displaying {@link StatusService.StatusMessage}s, from most recent (first position) to oldest (last position).</p>
 * 
 * <p>New messages added to the list will be considered the most recent ones, pushing older ones further down the list.</p>
 * 
 * <p>Automatically reflects locale changes.</p>
 *
 * @author Martin Riedel
 *
 */
public class StatusList extends JList {

	private static final long serialVersionUID = 7671184530745247412L;
	
	/*
	 * Reference to the custom model StatusList uses
	 */
	private final StatusListModel model;
	
	
	/**
	 * Constructs a new StatusListModel instance
	 * 
	 * @param minRows
	 * <br />The minimum number of rows in the list
	 * <br />Should not be < 1
	 */
	public StatusList(final int minRows) {
		if (minRows <1)
			throw new LogicError("can't initialise with a minimum of less than 1 rows");
		
		this.model = new StatusListModel(minRows);
		setModel(this.model);
		
		// make list items unselectable
		setSelectionModel(new StatusListSelectionModel());
	}
	
	/**
	 * Adds a new message to the list. The message will be considered the most recent one, and thus be
	 * inserted at the beginning of the list
	 * 
	 * @param message 
	 * <br />The message to be added.
	 * <br />Should not be null.
	 */
	public void addMessage(final StatusMessage message) {
		this.model.addMessage(message);
	}
	
	/**
	 * <p>ListModel for {@link StatusList}. Allows for insertion of new items (messages) at the
	 * first position in the list, pushing back older messages.</p>
	 * 
	 * <p>Automatically reflects locale changes / autotranslation and fires the appropriate events
	 * indicating that the list contents have changed.</p>
	 *
	 * @author Martin Riedel
	 *
	 */
	static class StatusListModel extends AbstractListModel {

		private static final long serialVersionUID = -4679723768372813345L;
			
		/*
		 * The messages currently being displayed / the [maxRetainedItems] most recent messages added to the list
		 */
		private final List<StatusMessage> messages = new LinkedList<StatusMessage>();
		
		/*
		 * The minimum number of rows in the list
		 */
		private final int minRows;
		
		
		/**
		 * Creates a new StatusListModel
		 * 
		 * 	 * @param minRows
		 * <br />The minimum number of rows in the list
		 * <br />Should not be < 1
		 */
		public StatusListModel(final int minRows) { 
			this.minRows = minRows;

			// fireContentsChanged when StatusMessages are autotranslated
			getDefaultTranslator().addTranslatorListener(new TranslatorListener());
			
			for (int i = 0; i < minRows; i++)
				this.messages.add(new StatusDummy());
		}
		
		/**
		 * Stores the provided message as the newest message in the model.
		 * 
		 * @param message
		 * <br />The message to be added to the list.
		 * <br />Should not be null.
		 */
		public void addMessage(final StatusMessage message) {
			// add new message to the beginning of the list
			this.messages.add(0, message);
			
			// if the list is holding more than the specified minimum number of messages/rows,
			// remove the oldest (rightmost) object if it is a StatusDummy
			if (this.messages.size() > this.minRows)
				if (this.messages.get(this.messages.size() - 1) instanceof StatusDummy)
					this.messages.remove(this.messages.size() - 1);
	
			// list has changed, fire event
			fireContentsChanged(this, 0, this.messages.size() - 1);
			
			//TODO fireIntervalAdded?
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public Object getElementAt(int pos) {
			return this.messages.get(pos);
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public int getSize() {
			return this.messages.size();
		}
		
		/**
		 * Called whenever the application's locale changes. All {@link StatusMessage}s have been
		 * dynamically translated already, so this method merely refreshes the list.
		 * 
		 */
		final void onLocaleChanged() {
			fireContentsChanged(this, 0, StatusListModel.this.messages.size() - 1);
		}
		
		/**
		 * Listens for locale changes and tells the list that its contents have changed (they have been autotranslated)
		 * when one happens. The messages themselves are dynamically translated, so there's no need to do anything 
		 * with them here.
		 *
		 * @author Martin Riedel
		 *
		 */
		private class TranslatorListener implements Translator.Listener {

			/**
			 * Constructs a new instance
			 */
			public TranslatorListener() {
				// do nothing, just allow instantiation
			}
			
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void localeChanged(Locale oldLocale, Locale newLocale) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						StatusListModel.this.onLocaleChanged();
					}
				});
			}
			
		}
		
	}
	
	/**
	 * A {@link ListSelectionModel} that doesn't allow selections of any kind.
	 *
	 * @author Martin Riedel
	 *
	 */
	private static class StatusListSelectionModel extends DefaultListSelectionModel {

		private static final long serialVersionUID = 2726424284954401907L;
		
		/**
		 * Constructs a new instance
		 *
		 */
		StatusListSelectionModel() {
			// do nothing, just allow instantiation
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSelectionEmpty() {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSelectedIndex(final int index) {
			return false;
		}
		
	}
	
	/**
	 * A dummy {@link StatusService.StatusMessage} used to pad the list so that it always
	 * displays a specified minimum of rows/messages.
	 *
	 * @author Martin Riedel
	 *
	 */
	static final class StatusDummy extends StatusMessage {
	
		public static final String DUMMY_TEXT = "";
		
		private final String dummyField = "42";
		
		/**
		 * Constructs a new StatusDummy
		 * 
		 */
		public StatusDummy() {
			super(DUMMY_TEXT);
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result
					+ ((this.dummyField == null) ? 0 : this.dummyField.hashCode());
			return result;
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			StatusDummy other = (StatusDummy) obj;
			if (this.dummyField == null) {
				if (other.dummyField != null)
					return false;
			} else if (!this.dummyField.equals(other.dummyField))
				return false;
			return true;
		}
		
		
		
	}

}
