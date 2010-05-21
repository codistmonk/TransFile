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

import static net.sourceforge.transfile.ui.swing.GUITools.horizontalFlow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import net.sourceforge.transfile.i18n.Translator;
import net.sourceforge.transfile.settings.Settings;

/**
 * Preferences frame that allows the user to configure the application variables.
 * <br>These variables can be found in {@link net.sourceforge.transfile.settings.Settings}.
 * <br>When the user clicks "Ok", the changes are committed to the Preferences API via {@link Settings#getPreferences}.
 *
 * @author codistmonk (creation 2010-05-09)
 *
 */
public class PreferencesFrame extends JDialog {
	
	private final TableModel tableModel;
	
	/**
	 * 
	 * @param owner
	 * <br>Should not be null
	 * <br>Reference parameter
	 */
	public PreferencesFrame(final JFrame owner) {
		super(owner, "TransFile Preferences");
		// I decided to use a table to edit the properties because I thought it was easier
		// for both the developers and the user; but that can be changed if necessary
		// TODO add specific editors and data validation for each property
		this.tableModel = createTableModel();
		
		this.setLayout(new BorderLayout());
		
		this.add(new JScrollPane(this.createTable()), BorderLayout.CENTER);
		this.add(horizontalFlow(
				new JButton(this.new OkAction()),
				new JButton(this.new CancelAction())
		), BorderLayout.SOUTH);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(owner);
	}
	
	final void saveSettings() {
		for (int i = 0; i < this.tableModel.getRowCount(); ++i) {
			final Object key = this.tableModel.getValueAt(i, 0);
			final Object value = this.tableModel.getValueAt(i, 1);
			
			Settings.getPreferences().put(key.toString(), value.toString());
			
			if ("locale".equals(key)) {
				Translator.getDefaultTranslator().setLocale(Translator.createLocale(value.toString()));
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	private final JTable createTable() {
		final JTable result = new JTable(this.tableModel);
		
		result.setToolTipText("Double-click to edit a value");
		result.getTableHeader().setReorderingAllowed(false);
		result.setDefaultEditor(Object.class, new CellEditor(result.getDefaultEditor(Object.class)));
		
		return result;
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-05-11)
	 *
	 */
	private static final class CellEditor implements TableCellEditor {
		
		private final TableCellEditor defaultCellEditor;
		
		private TableCellEditor currentCellEditor;
		
		/**
		 * 
		 * @param defaultCellEditor
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public CellEditor(final TableCellEditor defaultCellEditor) {
			this.defaultCellEditor = defaultCellEditor;
			this.currentCellEditor = this.defaultCellEditor;
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final Component getTableCellEditorComponent(final JTable table,
				final Object value, final boolean isSelected, final int row, final int column) {
			if ("locale".equals(table.getValueAt(row, 0))) {
				this.currentCellEditor = new DefaultCellEditor(new JComboBox(Translator.getDefaultTranslator().getAvailableLocales()));
			}
			else {
				this.currentCellEditor = this.defaultCellEditor;
			}
			
			return this.currentCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final Object getCellEditorValue() {
			return this.currentCellEditor.getCellEditorValue();
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void addCellEditorListener(final CellEditorListener listener) {
			this.currentCellEditor.addCellEditorListener(listener);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void cancelCellEditing() {
			this.currentCellEditor.cancelCellEditing();
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final boolean isCellEditable(final EventObject event) {
			return this.currentCellEditor.isCellEditable(event);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final void removeCellEditorListener(final CellEditorListener listener) {
			this.currentCellEditor.removeCellEditorListener(listener);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final boolean shouldSelectCell(final EventObject event) {
			return this.currentCellEditor.shouldSelectCell(event);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public final boolean stopCellEditing() {
			return this.currentCellEditor.stopCellEditing();
		}
		
		private static final long serialVersionUID = -3204827079123577279L;
		
	}
	
	/**
	 * Saves the settings and closes the preference frame.
	 * @author codistmonk (creation 2010-05-09)
	 *
	 */
	private final class OkAction extends AbstractAction {
		
		public OkAction() {
			super("Ok");
		}
		
		@Override
		public final void actionPerformed(final ActionEvent event) {
			PreferencesFrame.this.saveSettings();
			PreferencesFrame.this.dispose();
		}
		
		private static final long serialVersionUID = -7051752040024110888L;
		
	}
	
	/**
	 * Closes the preference frame without saving the settings.
	 * @author codistmonk (creation 2010-05-09)
	 *
	 */
	private final class CancelAction extends AbstractAction {
		
		public CancelAction() {
			super("Cancel");
		}
		
		@Override
		public final void actionPerformed(final ActionEvent event) {
			PreferencesFrame.this.dispose();
		}
		
		private static final long serialVersionUID = -8284492092950897347L;
		
	}
	
	private static final long serialVersionUID = 4747810436442554432L;
		
	/**
	 * Creates a new table model with 2 columns "key" and "value", initialized with data from the {@link Settings}
	 * if they are defined.
	 * <br>Otherwise, data from {@code defaultProperties} are used.
	 * <br>The keys are extracted from {@code defaultProperties}.
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	@SuppressWarnings("unchecked")
	private static final DefaultTableModel createTableModel() {
		final DefaultTableModel result = new DefaultTableModel(new Object[] { "key", "value" }, 0) {
			
			@Override
			public final boolean isCellEditable(final int row, final int column) {
				return column == 1;
			}
			
			private static final long serialVersionUID = 8437021152739655775L;
			
		};
		
		for (final String fieldName : Settings.getConstantFieldNames()) {
			final String key = fieldName.toLowerCase();
			result.addRow(new Object[] { key, Settings.getPreferences().get(key, Settings.getConstantAsString(fieldName)) });
		}
		
		Collections.sort(result.getDataVector(), new Comparator<Vector<String>>() {
			
			@Override
			public final int compare(final Vector<String> row1, final Vector<String> row2) {
				return row1.get(0).compareTo(row2.get(0));
			}
			
		});
		
		return result;
	}
    
}
