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

import static net.sourceforge.transfile.ui.swing.GUITools.rollover;
import static net.sourceforge.transfile.ui.swing.GUITools.scrollable;
import static net.sourceforge.transfile.ui.swing.GUITools.titleBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.sourceforge.transfile.tools.Tools;

/**
 * 
 * 
 * @author Martin Riedel
 * @author codistmonk (modifications since 2010-05-20)
 *
 */
public class SendPanel extends TopLevelPanel {
	
	private OperationListComponent operationListComponent;
	
	/**
	 * 
	 * @param window
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public SendPanel(final SwingGUI window) {
		super(window);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onQuit() {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onInit() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onHide() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onShow() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void setup() {
		this.operationListComponent = this.createOperationListComponent();
		
		this.setLayout(new BorderLayout());
		
		this.add(titleBorder("Send", scrollable(this.operationListComponent)), BorderLayout.CENTER);
		
		this.new FileDropHandler(this);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadState() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveState() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * TODO doc
	 * @param file
	 * <br>Should not be null
	 */
	final void addOperation(final File file) {
		if (file.isFile()) {
			this.operationListComponent.add(new OperationComponent(file.getName()), this.operationListComponent.getComponentCount() - 2);
			
			// Update scroll pane
			this.operationListComponent.revalidate();
			
			this.repaint();
		}
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	private final OperationListComponent createOperationListComponent() {
		final OperationListComponent result = new OperationListComponent();
		
		result.add(this.createAddButton(), 0);
		
		return result;
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
		
		result.setMaximumSize(new Dimension(Integer.MAX_VALUE, OperationComponent.MAXIMUM_HEIGHT));
		result.setPreferredSize(new Dimension(0, OperationComponent.MAXIMUM_HEIGHT));
		result.setAlignmentX(0.5F);
		
		return result;
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-21)
	 *
	 */
	private class AddAction extends AbstractAction {

		/**
		 * Package-private constructor to suppress visibility warnings.
		 */
		AddAction() {
			super("Add");
		}
		
		@Override
		public final void actionPerformed(final ActionEvent event) {
			final JFileChooser fileChooser = new JFileChooser();
			
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(SendPanel.this)) {
				SendPanel.this.addOperation(fileChooser.getSelectedFile());
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
				SendPanel.this.addOperation(file);
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
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-20)
	 *
	 */
	private static class OperationListComponent extends Box {
		
		public OperationListComponent() {
			super(BoxLayout.Y_AXIS);
			
			this.add(Box.createGlue());
		}
		
		private static final long serialVersionUID = 5262152364325099513L;
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-20)
	 *
	 */
	private static class OperationComponent extends JPanel {
		
		private final String fileName;
		
		/**
		 * 
		 * @param fileName
		 * <br>Should not be null
		 * <br>Shared parameter
		 */
		public OperationComponent(final String fileName) {
			this.fileName = fileName;
			
			this.initializeComponents();
			
			this.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAXIMUM_HEIGHT));
		}
		
		private final void initializeComponents() {
			final GridBagConstraints constraints = new GridBagConstraints();
			
			{
				constraints.gridx = 0;
				constraints.gridy = 0;
				constraints.weightx = 1.0;
				constraints.weighty = 0.0;
				constraints.ipadx = 1;
				constraints.ipady = VERTICAL_PADDING;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				
				GUITools.add(this, this.createProgressBar(), constraints);
			}
			{
				constraints.gridx = 1;
				constraints.gridy = 0;
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;
				constraints.ipadx = 1;
				constraints.ipady = VERTICAL_PADDING;
				constraints.fill = GridBagConstraints.NONE;
				
				GUITools.add(this, createStartButton(), constraints);
			}
			{
				constraints.gridx = 2;
				constraints.gridy = 0;
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;
				constraints.ipadx = 1;
				constraints.ipady = VERTICAL_PADDING;
				constraints.fill = GridBagConstraints.NONE;
				
				GUITools.add(this, this.createRemoveButton(), constraints);
			}
		}
		
		/**
		 * 
		 * TODO doc
		 * @return
		 * <br>A non-null value
		 * <br>A new value
		 */
		private final JProgressBar createProgressBar() {
			final JProgressBar result = new JProgressBar();
			
			result.setString(this.fileName + "queued");
			result.setStringPainted(true);
			
			return result;
		}
		
		/**
		 * 
		 * TODO doc
		 * @return
		 * <br>A non-null value
		 * <br>A new value
		 */
		private final JButton createRemoveButton() {
			return rollover(new JButton(this.new RemoveAction()), "remove", false);
		}
		
		/**
		 * 
		 * @author codistmonk (creation 2010-05-20)
		 *
		 */
		private class RemoveAction extends AbstractAction {
			
			/**
			 * Package-private constructor to suppress visibility warnings.
			 */
			RemoveAction() {
				// Do nothing
			}
			
			@Override
			public final void actionPerformed(final ActionEvent event) {
				final JComponent parent = (JComponent) OperationComponent.this.getParent();
				
				if (parent != null) {
					parent.remove(OperationComponent.this);
					
					// Update scroll pane
					parent.revalidate();
					
					parent.getRootPane().repaint();
				}
			}
			
			private static final long serialVersionUID = -2137598910170961094L;
			
		}
		
		private static final long serialVersionUID = 195201935191732396L;
		
		public static final int MAXIMUM_HEIGHT = 48;
		
		public static final int VERTICAL_PADDING = 16;
		
		/**
		 * 
		 * TODO doc
		 * @return
		 * <br>A non-null value
		 * <br>A new value
		 */
		private static final JButton createStartButton() {
			return rollover(new JButton(new StartAction()), "start", false);
		}
		
		/**
		 * 
		 * @author codistmonk (creation 2010-05-20)
		 *
		 */
		private static class StartAction extends AbstractAction {
			
			StartAction() {
				super("");
			}
			
			@Override
			public final void actionPerformed(final ActionEvent event) {
				// TODO
				Tools.debugPrint("TODO");
				JOptionPane.showMessageDialog(null, "Not implemented");
			}
			
			private static final long serialVersionUID = 2945622216824469468L;
			
		}
		
	}
	
}
