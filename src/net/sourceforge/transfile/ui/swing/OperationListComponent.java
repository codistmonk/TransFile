package net.sourceforge.transfile.ui.swing;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;

import net.sourceforge.transfile.ui.swing.OperationComponent.SelectionModel;

/**
 * 
 * @author codistmonk (creation 2010-05-20)
 *
 */
public class OperationListComponent extends Box {
	
	private final SelectionModel selectionModel;
	
	public OperationListComponent() {
		super(BoxLayout.Y_AXIS);
		this.selectionModel = new SelectionModel();
		
		this.setBackground(DEFAULT_BACKGROUND_COLOR);
		this.setBorder(BorderFactory.createLineBorder(OperationComponent.DEFAULT_BORDER_COLOR));
		
		this.add(Box.createGlue());
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final SelectionModel getSelectionModel() {
		return this.selectionModel;
	}
	
	private static final long serialVersionUID = 5262152364325099513L;
	
	public static final Color DEFAULT_BACKGROUND_COLOR = Color.GRAY;
	
}
