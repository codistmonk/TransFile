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

import net.sourceforge.transfile.i18n.Translator;

/**
 * Receives new status messages and stores them in a last-in first-out fashion
 *
 * @author Martin Riedel
 *
 */
interface StatusService extends Iterable<StatusService.StatusMessage> {
	
	/**
	 * Posts the provided status message, making it the newest one
	 * 
	 * @param message 
	 * <br />the status message to post
	 * <br />should not be null
	 * <br />should not be empty
	 */
	public void postStatusMessage(final StatusMessage message);
	
	/**
	 * Adds a {@link StatusChangeListener} that will be informed about new status messages
	 * 
	 * @param listener
	 * <br />the listener to inform about new status messages
	 * <br />should not be null
	 */
	public void addStatusListener(final StatusChangeListener listener);
	
	/**
	 * Listens for new status messages
	 *
	 * @author Martin Riedel
	 *
	 */
	public interface StatusChangeListener {
		
		/**
		 * Invoked after a new status message is posted
		 * 
		 * @param message the new status message
		 */
		public void newStatusMessage(final StatusMessage message);

	}
	
	/**
	 * A mutable (and translatable) status message
	 *
	 * @author Martin Riedel
	 *
	 */
	public static class StatusMessage {
		
		/*
		 * The actual status message.
		 * DO NOT RENAME THIS PROPERTY as it used is reflectively by SwingTranslator
		 */
		private String text;
		
		/**
		 * Constructs a new StatusMessage.
		 * 
		 * @param text
		 * <br />the status message text
		 * <br />should be a translation key
		 * <br />should not be null
		 */
		public StatusMessage(final String text) {
			this.text = text;
		}
		
		/**
		 * <p>Returns the message text.</p>
		 * 
		 * <p>DO NOT RENAME THIS GETTER as it used is reflectively by {@link Translator}.</p>
		 * 
		 * @return the message 
		 */
		public String getText() {
			return this.text;
		}
		
		/**
		 * <p>Sets the message text.</p>
		 * 
		 * <p><b>DO NOT USE THIS SETTER.</b> It is meant for reflective use by {@link Translator} only.</p>
		 * 
		 * <p><b>DO NOT RENAME THIS SETTER</b> as it used is reflectively by {@link Translator}.</p>
		 * 
		 * @param text
		 * <br />should not be null
		 */
		public void setText(final String text) {
			this.text = text;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return this.getText();
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.text == null) ? 0 : this.text.hashCode());
			return result;
		}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StatusMessage other = (StatusMessage) obj;
			if (this.text == null) {
				if (other.text != null)
					return false;
			} else if (!this.text.equals(other.text))
				return false;
			return true;
		}
		
		
	}

}
