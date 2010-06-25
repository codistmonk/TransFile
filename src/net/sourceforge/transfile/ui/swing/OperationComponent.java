package net.sourceforge.transfile.ui.swing;

import static net.sourceforge.transfile.ui.swing.GUITools.rollover;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.sourceforge.transfile.operations.Operation;
import net.sourceforge.transfile.operations.Operation.Controller;

/**
 * 
 * @author codistmonk (creation 2010-05-20)
 *
 */
public class OperationComponent extends JPanel {
	
	private final SelectionModel selectionModel;
	
	private final Operation operation;
	
	/**
	 * 
	 * @param selectionModel 
	 * <br>Can be null
	 * <br>Shared parameter
	 * @param operation
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public OperationComponent(final SelectionModel selectionModel, final Operation operation) {
		this.selectionModel = selectionModel;
		this.operation = operation;
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public final void mousePressed(final MouseEvent event) {
				OperationComponent.this.select();
			}
			
		});
		
		this.setup();
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final Operation getOperation() {
		return this.operation;
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
	
	final void remove() {
		final JComponent parent = (JComponent) this.getParent();
		
		if (parent != null) {
			parent.remove(this);
			
			// Update scroll pane
			parent.revalidate();
			
			parent.getRootPane().repaint();
		}
	}
	
	private final void setup() {
		final GridBagConstraints constraints = new GridBagConstraints();
		
		{
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;
			constraints.insets = new Insets(VERTICAL_INSET, LARGE_HORIZONTAL_INSET, VERTICAL_INSET, 0);
			constraints.fill = GridBagConstraints.NONE;
			
			GUITools.add(this, this.createFileIconLabel(), constraints);
		}
		{
			++constraints.gridx;
			constraints.weightx = 1.0;
			constraints.insets = new Insets(VERTICAL_INSET, LARGE_HORIZONTAL_INSET, VERTICAL_INSET, LARGE_HORIZONTAL_INSET);
			constraints.fill = GridBagConstraints.HORIZONTAL;
			
			GUITools.add(this, this.createProgressBar(), constraints);
		}
		{
			++constraints.gridx;
			constraints.weightx = 0.0;
			constraints.insets = new Insets(VERTICAL_INSET, 0, VERTICAL_INSET, SMALL_HORIZONTAL_INSET);
			constraints.fill = GridBagConstraints.NONE;
			
			GUITools.add(this, this.createStartPauseRetryButton(), constraints);
		}
		{
			++constraints.gridx;
			
			GUITools.add(this, this.createCancelRemoveButton(), constraints);
		}
		
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAXIMUM_HEIGHT));
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DEFAULT_BORDER_COLOR));
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	private final JLabel createFileIconLabel() {
		final Icon icon = getIconForFileName(this.getOperation().getFileName());
		final JLabel result = new JLabel(icon);
		
		if (icon.getIconWidth() < PREFERRED_FILE_ICON_SIZE) {
			final BufferedImage scaledImage = new BufferedImage(icon.getIconWidth() * FILE_ICON_SCALE, icon.getIconHeight() * FILE_ICON_SCALE, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D graphics = scaledImage.createGraphics();
			
			graphics.scale(FILE_ICON_SCALE, FILE_ICON_SCALE);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			
			icon.paintIcon(result, graphics, 0, 0);
			
			result.setIcon(new ImageIcon(scaledImage));
		}
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	private final JProgressBar createProgressBar() {
		final JProgressBar result = new JProgressBar();
		
		result.setString(this.getOperation().getFileName() + " (queued)");
		result.setStringPainted(true);
		
		this.getOperation().addOperationListener(new AbstractOperationListener() {
			
			@Override
			protected final void doStateChanged() {
				super.doStateChanged();
				
				final Operation operation = OperationComponent.this.getOperation();
				
				switch (operation.getState()) {
				case CANCELED:
				case DONE:
				case PAUSED:
				case PROGRESSING:
					this.setProgressingString(operation);
					break;
				case QUEUED:
					result.setString(operation.getFileName() + " (queued)");
					break;
				case REMOVED:
					break;
				}
				
			}
			
			@Override
			protected final void doProgressChanged() {
				super.doProgressChanged();
				
				final Operation operation = OperationComponent.this.getOperation();
				
				result.setValue(this.getProgressPercentage(operation));
				
				this.setProgressingString(operation);
			}
			
			/**
			 * TODO doc
			 * 
			 * @param operation
			 * <br>Should not be null
			 */
			private final void setProgressingString(final Operation operation) {
				result.setString(operation.getFileName() + " (" + this.getProgressPercentage(operation) + "%)");
			}
			
			/**
			 * TODO doc
			 * 
			 * @param operation
			 * <br>Should not be null
			 * @return
			 * <br>Range: {@code [0 .. 100]}
			 */
			private final int getProgressPercentage(final Operation operation) {
				return (int) (operation.getProgress() * 100.0);
			}
			
		});
		
		return result;
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	private final JButton createCancelRemoveButton() {
		final JButton result = rollover(new JButton(this.new CancelRemoveAction()), "remove", false);
		
		this.getOperation().addOperationListener(new AbstractOperationListener() {
			
			@Override
			protected final void doStateChanged() {
				super.doStateChanged();
				
				switch (OperationComponent.this.getOperation().getState()) {
				case PAUSED:
				case PROGRESSING:
					rollover(result, "cancel", false);
					break;
				case CANCELED:
				case DONE:
				case QUEUED:
				case REMOVED:
					rollover(result, "remove", false);
					break;
				}
			}
			
		});
		
		return result;
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	private final JButton createStartPauseRetryButton() {
		final JButton result = rollover(new JButton(this.new StartPauseAction()), "start", false);
		
		this.getOperation().addOperationListener(new AbstractOperationListener() {
			
			@Override
			protected final void doStateChanged() {
				super.doStateChanged();
				
				switch (OperationComponent.this.getOperation().getState()) {
				case PAUSED:
				case QUEUED:
					rollover(result, "start", false);
					break;
				case PROGRESSING:
					rollover(result, "pause", false);
					break;
				case CANCELED:
					rollover(result, "retry", false);
					break;
				case DONE:
					rollover(result, "done", false);
					break;
				case REMOVED:
					break;
				}
			}
			
		});
		
		return result;
	}
	
	/**
	 * 
	 * TODO doc
	 *
	 * @author codistmonk (creation 2010-06-14)
	 *
	 */
	private class AbstractOperationListener implements Operation.Listener {
		
		/**
		 * Protected default constructor to suppress visibility warnings.
		 */
		protected AbstractOperationListener() {
			// Do nothing
		}
		
		@Override
		public final void progressChanged() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public final void run() {
					AbstractOperationListener.this.doProgressChanged();
				}
				
			});
		}
		
		@Override
		public final void stateChanged() {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public final void run() {
					AbstractOperationListener.this.doStateChanged();
				}
				
			});
		}
		
		/**
		 * Executed in the AWT Event Dispatching Thread.
		 * 
		 * @throws IllegalStateException if the current thread is not the AWT Event Dispatching Thread
		 */
		protected void doProgressChanged() {
			GUITools.checkAWT();
		}
		
		/**
		 * Executed in the AWT Event Dispatching Thread.
		 * 
		 * @throws IllegalStateException if the current thread is not the AWT Event Dispatching Thread
		 */
		protected void doStateChanged() {
			GUITools.checkAWT();
		}
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-20)
	 *
	 */
	private class StartPauseAction extends AbstractAction {
		
		StartPauseAction() {
			super("");
		}
		
		@Override
		public final void actionPerformed(final ActionEvent event) {
			final Operation operation = OperationComponent.this.getOperation();
			final Controller controller = operation.getController();
			
			switch (operation.getState()) {
			case PAUSED:
			case QUEUED:
			case CANCELED:
				try {
					controller.start();
				} catch (final Throwable exception) {
					exception.printStackTrace();
				}
				break;
			case PROGRESSING:
				controller.pause();
				break;
			case DONE:
			case REMOVED:
				break;
			}
		}
		
		private static final long serialVersionUID = 2945622216824469468L;
		
	}
	
	/**
	 * 
	 * @author codistmonk (creation 2010-05-20)
	 *
	 */
	private class CancelRemoveAction extends AbstractAction {
		
		/**
		 * Package-private constructor to suppress visibility warnings.
		 */
		CancelRemoveAction() {
			// Do nothing
		}
		
		@Override
		public final void actionPerformed(final ActionEvent event) {
			final Operation operation = OperationComponent.this.getOperation();
			final Controller controller = operation.getController();
			
			switch (operation.getState()) {
			case PAUSED:
			case PROGRESSING:
				controller.cancel();
				break;
			case CANCELED:
			case DONE:
			case QUEUED:
			case REMOVED:
				controller.remove();
				OperationComponent.this.remove();
				break;
			}
		}
		
		private static final long serialVersionUID = -2137598910170961094L;
		
	}
	
	private static final long serialVersionUID = 195201935191732396L;
	
	public static final int MAXIMUM_HEIGHT = 72;
	
	public static final int LARGE_HORIZONTAL_INSET = 8;
	
	public static final int SMALL_HORIZONTAL_INSET = 4;
	
	public static final int VERTICAL_INSET = 16;
	
	public static final int PREFERRED_FILE_ICON_SIZE = 32;
	
	public static final int FILE_ICON_SCALE = 2;
	
	public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	
	public static final Color ALTERNATE_BACKGROUND_COLOR = Color.LIGHT_GRAY;
	
	public static final Color DEFAULT_BORDER_COLOR = Color.BLACK;
	
	/**
	 * Creates and returns a temporary file that will be deleted upon exit.
	 *
	 * @param prefix
	 * <br>Should not be null
	 * @param suffix
	 * <br>Should not be null
	 * @return
	 * <br>A possibly null value
	 * <br>A new value
	 */
	private static final File createTemporaryFile(final String prefix, final String suffix) {
		File result = null;
		
		try {
			result = File.createTempFile(prefix, suffix);
			
			result.deleteOnExit();
		} catch (final IOException exception) {
			exception.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Returns the default system icon for a file named {@code fileName}.
	 *
	 * @param fileName
	 * <br>Should not be null
	 * @return
	 * <br>A possibly null value
	 */
	private static final Icon getIconForFileName(final String fileName) {
		final File temporaryFile = createTemporaryFile("tmp", fileName);
		
		try {
			final Object shellFolder = Class.forName("sun.awt.shell.ShellFolder").getMethod("getShellFolder", File.class).invoke(null, temporaryFile);
			final Image iconImage = (Image) shellFolder.getClass().getMethod("getIcon", boolean.class).invoke(shellFolder, true);
			
			if (iconImage != null) {
				return new ImageIcon(iconImage);
			}
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		
		return new JFileChooser().getIcon(temporaryFile);
	}
	
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
