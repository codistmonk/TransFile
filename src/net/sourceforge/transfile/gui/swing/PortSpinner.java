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

package net.sourceforge.transfile.gui.swing;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import net.sourceforge.transfile.exceptions.LogicError;
import net.sourceforge.transfile.settings.Settings;

//TODO spin off an AsYouTypeSpinner and make it a base class of PortSpinner

//TODO color background, not foreground, when the current value is invalid

//TODO disable both buttons when the value is invalid and re-enable them properly when it becomes valid (with respect to extremes)

//TODO add tooltip explaining range of accepted values

//TODO implement functionality: setCaretPosition(getDocument().getLength() on the JFormattedTextField 
//     when it gains focus THROUGH TABBING, without interfering with the MouseListener's functionality

/**
 * A JSpinner containing a port number. Input checks are performed as the user types. The spinner's value
 * is guaranteed to represent the last valid port number the user has entered, and is updated as the user
 * types. The user does not have to commit his edit in any way (i.e. by pressing enter or unfocusing the text field).
 * This should cater to the user's intuitive expectations much more than the default JSpinner behaviour.
 *
 * @author Martin Riedel
 *
 */
class PortSpinner extends JSpinner {

	private static final long serialVersionUID = 1231652057161765899L;
	
	/*
	 * The maximum number of digits in a port. Since the highest legal port number is 65535, this
	 * constant shouldn't need changing.
	 */
	public static final int maxDigits = 5;
		
	/*
	 * The underlying Editor
	 */
	private final PortSpinnerEditor editor;
		
	/*
	 * Initial port (later to be overwritten by the user's last selected port, if present (loaded from the user's settings file)
	 */
	private static final int initialPort = Settings.getInstance().getDefaultInt("local_port");
	
	/*
	 * The minimum valid port (usually 1 or 1024)
	 */
	private static final int minPort = Settings.getInstance().getInt("local_port_min");
	
	/*
	 * The maximum valid port (usually 65535)
	 */
	private static final int maxPort = Settings.getInstance().getInt("local_port_max");
	
	/*
	 * The foreground (text) color of the text field at startup
	 */
	private final Color defaultForeground;

	
	/**
	 * Constructs a new PortSpinner
	 * 
	 */
	public PortSpinner() {
		super(new SpinnerNumberModel(initialPort,
				 					 minPort,
				 					 maxPort,
				 					 1));
				
		if(!(getEditor() instanceof NumberEditor))
			throw new LogicError("PortSpinner Editor is not a NumberEditor");
		
		editor = new PortSpinnerEditor(); 
		setEditor(editor);
		
		defaultForeground = editor.textField.getForeground();
	}
	
	/**
	 * A modified JSpinner.NumberEditor that gracefully handles changes in the SpinnerModel without
	 * resetting the cursor position.
	 *
	 * @author Martin Riedel
	 *
	 */
	private class PortSpinnerEditor extends NumberEditor {
		
		private static final long serialVersionUID = 371957247504918562L;
		
		/*
		 * The underlying JFormattedTextField 
		 */
		public final JFormattedTextField textField;
		
		/*
		 * The underlying Document
		 */
		public final AbstractDocument document;
		

		/**
		 * Constructs a PortSpinnereditor instance.
		 * 
		 */
		public PortSpinnerEditor() {
			// Initialize this NumberEditor.
			// Secound parameter is the number format as described for the DecimalFormat class.
			// 0 represents an integer value with no grouping
			super(PortSpinner.this, "0");
					
			textField = getTextField();
			
			if(!(textField.getDocument() instanceof AbstractDocument))
				throw new LogicError("PortSpinner JFormattedTextField Document is not an AbstractDocument");
			
			document = (AbstractDocument) textField.getDocument();
			
			textField.setHorizontalAlignment(JTextField.LEFT);
			
			// limit the text editor to a maximum of 5 columns
			//textField.setColumns(maxDigits);			
			
			// when focus is gained through a mouse click, set the cursor to where the click occurred
			textField.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mousePressed(final MouseEvent e) {
					
			        SwingUtilities.invokeLater(new Runnable() {
			        	
			            public void run() {
			                textField.setCaretPosition(textField.viewToModel(e.getPoint()));
			            }
			            
			        });
			    }
			});
			
			// add the DocumentListener that will commit any changes to the SpinnerModel immediately
			document.addDocumentListener(new PortSpinnerDocumentListener());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void stateChanged(final ChangeEvent e) {
			// update the text field accordingly
			textField.setText(getModel().getValue().toString());			
		}
		
		/**
		 * Invoked when a valid number was inserted/selected (be it by the user or by the application)
		 * 
		 */
		private void onValidEdit() {
			// check if the edit is actually invalid by checking for non-digits in the text field
			// this is a hacky but convenient solution to the problem that it is hard to combine
			// the following three aspects of the desired spinner behaviour:
			// - there should be a minimum and a maximum value at which the respective buttons turn grey and the respective up or down button stops working
			// - it should be possible to enter numbers exceeding these extremes, but it should be pointed out to the users that his choice is invalid
			// - it should not be possible to insert any non-digits into the text field, or at least the presence of non-digits
			//   should be identified and pointed out to the user
			// Since DocumentFilters just don't work on JSpinners for some mystical, undocumented reason (their event handlers just
			// get invoked) and for another, similarly mystical and undocumented reason, a NumberEditor formatted with a
			// DecimalFormat doesn't raise a ParseException when it encounters letters instead of digits, this seems like the 
			// easiest solution.
			for(char c: textField.getText().toCharArray()) {
				if(!(Character.isDigit(c))) {
					onInvalidEdit();
					return;
				}
			}
			
			textField.setForeground(defaultForeground);
		}
		
		/**
		 * Invoked when an invalid number was inserted/selected (be it by the user or by the application)
		 * 
		 */
		private void onInvalidEdit() {
			textField.setForeground(new Color(255, 0, 0, 255));
		}
		
		/**
		 * A DocumentListener that commits all changes made to the Document (in this case the spinner's text field)
		 * immediately, so that the SpinnerModel reflects the text field's state at all times (provided that the value
		 * in the text field is valid), which should be much more intuitive than the default JSpinner behaviour.
		 *
		 * @author Martin Riedel
		 *
		 */
		private class PortSpinnerDocumentListener implements DocumentListener {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void removeUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						try {
							commitEdit();
							onValidEdit();
						} catch (ParseException e1) {
							onInvalidEdit();
						}
					}

				});
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void insertUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						try {
							commitEdit();
							onValidEdit();
						} catch (ParseException e1) {
							onInvalidEdit();
						}
					}
					
				});
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void changedUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						try {
							commitEdit();
							onValidEdit();
						} catch (ParseException e1) {
							onInvalidEdit();
						}
					}
					
				});
			}
		}
		
	}

}
