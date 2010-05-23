package net.sourceforge.transfile.ui.swing;

import static net.sourceforge.transfile.ui.swing.GUITools.rollover;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.sourceforge.transfile.tools.Tools;

/**
 * 
 * @author codistmonk (creation 2010-05-20)
 *
 */
public class OperationComponent extends JPanel {
	
	private final SelectionModel selectionModel;
	
	private final String fileName;
	
	/**
	 * 
	 * @param selectionModel 
	 * <br>Can be null
	 * <br>Shared parameter
	 * @param fileName
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public OperationComponent(final SelectionModel selectionModel, final String fileName) {
		this.selectionModel = selectionModel;
		this.fileName = fileName;
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public final void mousePressed(final MouseEvent event) {
				OperationComponent.this.select();
			}
			
		});
		
		this.setup();
	}
	
	@Override
	public final void paint(final Graphics graphics) {
		if (this.isSelected()) {
			this.setBackground(Color.BLUE);
		}
		else if (this.getParent() != null) {
			final int z = this.getParent().getComponentZOrder(this);
			
			this.setBackground(z % 2 == 0 ? DEFAULT_BACKGROUND_COLOR : ALTERNATE_BACKGROUND_COLOR);
		}
		else {
			this.setBackground(DEFAULT_BACKGROUND_COLOR);
		}
		
		super.paint(graphics);
	}
	
	/**
	 * Does nothing if the selection model is null.
	 */
	public final void deselect() {
		if (this.selectionModel != null) {
			this.selectionModel.setSelection(null);
		}
	}
	
	/**
	 * Does nothing if the selection model is null.
	 */
	public final void select() {
		if (this.selectionModel != null) {
			this.selectionModel.setSelection(this);
			
			if (this.getParent() != null) {
				this.getParent().repaint();
			}
		}
	}
	
	/**
	 * 
	 * @return {@code true} if the selection model is not null and {@code this} is selected.
	 */
	public final boolean isSelected() {
		return this.selectionModel != null && this.selectionModel.getSelection() == this;
	}
	
	private final void setup() {
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
		
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAXIMUM_HEIGHT));
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DEFAULT_BORDER_COLOR));
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
		
		result.setString(this.fileName + " (queued)");
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
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	private final JButton createStartButton() {
		return rollover(new JButton(this.new StartAction()), "start", false);
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-20)
	 *
	 */
	private class StartAction extends AbstractAction {
		
		StartAction() {
			super("");
		}
		
		@Override
		public final void actionPerformed(final ActionEvent event) {
			// TODO
			Tools.debugPrint("TODO");
			JOptionPane.showMessageDialog(OperationComponent.this, "Not implemented");
		}
		
		private static final long serialVersionUID = 2945622216824469468L;
		
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
	
	public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	
	public static final Color ALTERNATE_BACKGROUND_COLOR = Color.LIGHT_GRAY;
	
	public static final Color DEFAULT_BORDER_COLOR = Color.BLACK;
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-23)
	 *
	 */
	public static class SelectionModel {
		
		private OperationComponent selection;
		
		/**
		 * 
		 * @return
		 * <br>A possibly null value
		 * <br>A shared value
		 */
		public final OperationComponent getSelection() {
			return this.selection;
		}
		
		/**
		 * 
		 * @param selection
		 * <br>Can be null
		 * <br>Shared parameter
		 */
		public final void setSelection(final OperationComponent selection) {
			this.selection = selection;
		}
		
	}
	
}
