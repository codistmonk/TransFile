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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventObject;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import net.sourceforge.transfile.settings.Settings;

/**
 * Preferences frame that allows the user to configure the application variables.
 * <br>These variables can be found in net.sourceforge.transfile.settings.defaults.properties.
 * <br>When the user clicks "Ok", the changes are committed to the Settings singleton instance
 * and Settings.save() is invoked.
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
		this.tableModel = createTableModel(loadDefaultProperties());
		
		this.setLayout(new BorderLayout());
		
		this.add(new JScrollPane(this.createTable()), BorderLayout.CENTER);
		this.add(horizontalBox(
				new JButton(this.new OkAction()),
				new JButton(this.new CancelAction())
		), BorderLayout.SOUTH);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(owner);
	}
	
	final void saveSettings() {
		this.putTableDataIntoSettings();
		Settings.getInstance().save();
	}
	
	private final void putTableDataIntoSettings() {
		for (int i = 0; i < this.tableModel.getRowCount(); ++i) {
			final Object key = this.tableModel.getValueAt(i, 0);
			final Object value = this.tableModel.getValueAt(i, 1);
			
			Settings.getInstance().put(key, value);
			
			if ("locale".equals(key)) {
				SwingTranslator.getDefaultTranslator().setLocale((Locale) value);
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
				this.currentCellEditor = new DefaultCellEditor(new JComboBox(SwingTranslator.getDefaultTranslator().getAvailableLocales()));
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
	
	private static final String DEFAULT_PROPERTIES = "net/sourceforge/transfile/settings/defaults.properties";
	
	private static final long serialVersionUID = 4747810436442554432L;
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 * @throws RuntimeException if the properties file could not be loaded
	 */
	private static final PropertyResourceBundle loadDefaultProperties() {
		try {
			return new PropertyResourceBundle(getResourceAsStream(DEFAULT_PROPERTIES));
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	/**
	 * Creates a new table model with 2 columns "key" and "value", initialized with data from the {@link Settings}
	 * if they are defined.
	 * <br>Otherwise, data from {@code defaultProperties} are used.
	 * <br>The keys are extracted from {@code defaultProperties}.
	 * @param defaultProperties
	 * <br>Should not be null
	 * @return
	 * <br>A new value
	 * <br>A non-null value
	 */
	private static final DefaultTableModel createTableModel(final PropertyResourceBundle defaultProperties) {
		final DefaultTableModel result = new DefaultTableModel(new Object[] { "key", "value" }, 0) {
			
			@Override
			public final boolean isCellEditable(final int row, final int column) {
				return column == 1;
			}
			
			private static final long serialVersionUID = 8437021152739655775L;
			
		};
		
		for (final String key : defaultProperties.keySet()) {
			final String defaultValue = defaultProperties.getObject(key).toString();
			
			result.addRow(new Object[] { key, Settings.getInstance().getProperty(key, defaultValue) });
		}
		
		result.addRow(new Object[] { "locale", Settings.getInstance().get("locale") });
		
		return result;
	}
	
	/*
	 * TODO move the following methods elsewhere
	 * These methods are usually part of utility classes I use in my projects.
	 * I put them here because I didn't want to create too much classes at first.
	 */
	
    /**
     *
     * @param components
     * <br>Should not be null
     * @return
     * <br>A new value
     * <br>A non-null value
     */
    public static final JPanel horizontalBox(final Component... components) {
    	final JPanel result = new JPanel(new FlowLayout());

        for (final Component component : components) {
            result.add(component);
        }

        return result;
    }
    
    /**
     * Creates an input stream from a resource on the project path.
     * @param resourcePath the path to the resource, relative to the classes or jar root.
     * <br>Should not be null
     * @return {@code null} if the resource could not be found
     * <br>A new value
     * <br>A possibly null value
     */
	public static final InputStream getResourceAsStream(final String resourcePath) {
		return PreferencesFrame.class.getClassLoader().getResourceAsStream(resourcePath);
	}
	
    /**
     * Concatenates the source location of the call and the string representations
     * of the parameters separated by spaces.
     * <br>This is method helps to perform console debugging using System.out or System.err.
     * @param stackIndex 1 is the source of this method, 2 is the source of the call, 3 is the source of the call's caller, and so forth
     * <br>Range: {@code [O .. Integer.MAX_VALUE]}
     * @param objects
     * <br>Should not be null
     * @return
     * <br>A new value
     * <br>A non-null value
     * @throws IndexOutOfBoundsException if {@code stackIndex} is invalid
     */
    public static final String debug(final int stackIndex, final Object... objects) {
        final StringBuilder builder = new StringBuilder(Thread.currentThread().getStackTrace()[stackIndex].toString());

        for (final Object object : objects) {
            builder.append(" ").append(object);
        }

        return builder.toString();
    }
    
    /**
     * Prints on the standard output the concatenation of the source location of the call
     * and the string representations of the parameters separated by spaces.
     * @param objects
     * <br>Should not be null
     */
    public static final void debugPrint(final Object... objects) {
        System.out.println(debug(3, objects));
    }
    
}
