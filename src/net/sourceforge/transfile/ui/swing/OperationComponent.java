package net.sourceforge.transfile.ui.swing;

import static net.sourceforge.transfile.ui.swing.GUITools.rollover;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

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
	
	private final String fileName;
	
	/**
	 * 
	 * @param fileName
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public OperationComponent(final String fileName) {
		this.fileName = fileName;
		
		this.setup();
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
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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
	
}
