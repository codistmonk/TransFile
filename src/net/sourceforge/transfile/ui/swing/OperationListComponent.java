package net.sourceforge.transfile.ui.swing;

import javax.swing.Box;
import javax.swing.BoxLayout;

/**
 * 
 * @author codistmonk (creation 2010-05-20)
 *
 */
public class OperationListComponent extends Box {
	
	public OperationListComponent() {
		super(BoxLayout.Y_AXIS);
		
		this.add(Box.createGlue());
	}
	
	private static final long serialVersionUID = 5262152364325099513L;
	
}
