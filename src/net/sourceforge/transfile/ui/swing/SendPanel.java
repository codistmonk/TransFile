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

import static net.sourceforge.transfile.i18n.Translator.Helpers.translate;
import static net.sourceforge.transfile.ui.swing.GUITools.rollover;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * 
 * 
 * @author Martin Riedel
 * @author codistmonk (modifications since 2010-05-21)
 *
 */
public class SendPanel extends AbstractOperationListPanel {
	
	/**
	 * 
	 * @param window
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public SendPanel(final SwingGUI window) {
		super(window);
		
		this.setup();
	}
	
	@Override
	protected final OperationListComponent createOperationListComponent() {
		final OperationListComponent result = super.createOperationListComponent();
		
		result.add(this.createAddButton(), 0);
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @param file
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	final void offerFile(final File file) {
		this.getWindow().getSession().offerFile(file);
	}
	
	private final void setup() {
		this.setup("send_list_title");
		
		this.new FileDropHandler(this);
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	private final JButton createAddButton() {
		final JButton result = rollover(new JButton(this.new AddAction()), "add", true);
		
		translate(result);
		result.setMaximumSize(new Dimension(Integer.MAX_VALUE, ADD_BUTTON_HEIGHT));
		result.setPreferredSize(new Dimension(0, ADD_BUTTON_HEIGHT));
		result.setAlignmentX(0.5F);
		result.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OperationComponent.DEFAULT_BORDER_COLOR));
		
		return result;
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-21)
	 *
	 */
	private class AddAction extends AbstractAction {
		
		AddAction() {
			super("add_button_text");
		}
		
		@Override
		public final void actionPerformed(final ActionEvent event) {
			final JFileChooser fileChooser = new JFileChooser();
			
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(SendPanel.this) && fileChooser.getSelectedFile() != null) {
				SendPanel.this.offerFile(fileChooser.getSelectedFile());
			}
		}
		
		private static final long serialVersionUID = 3394221460483136760L;
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-14)
	 *
	 */
	private class FileDropHandler extends DropTargetAdapter {
		
		private final JComponent componentToHighlight;
		
		/**
		 * 
		 * @param componentToHighlight
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		FileDropHandler(final JComponent componentToHighlight) {
			this.componentToHighlight = componentToHighlight;
			
			new DropTarget(componentToHighlight, this);
		}
		
		@Override
		public final void drop(final DropTargetDropEvent event) {
			this.componentToHighlight.setBorder(null);
			
			for (final File file : GUITools.getFiles(event)) {
				SendPanel.this.offerFile(file);
			}
		}
		
		@Override
		public final void dragEnter(final DropTargetDragEvent even) {
			this.componentToHighlight.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
		}
		
		@Override
		public final void dragExit(final DropTargetEvent event) {
			this.componentToHighlight.setBorder(null);
		}
		
	}
	
	private static final long serialVersionUID = -3849684830598909661L;
	
	private static final int ADD_BUTTON_HEIGHT = OperationComponent.MAXIMUM_HEIGHT - 10;
	
}
