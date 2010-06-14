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
import static net.sourceforge.transfile.ui.swing.GUITools.scrollable;
import static net.sourceforge.transfile.ui.swing.GUITools.titleBorder;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.sourceforge.transfile.operations.Operation;
import net.sourceforge.transfile.tools.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-13)
 *
 */
public abstract class AbstractOperationListPanel extends TopLevelPanel {
	
	private OperationListComponent operationListComponent;
	
	/**
	 * 
	 * @param window
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public AbstractOperationListPanel(final SwingGUI window) {
		super(window);
	}
	
	/**
	 * TODO doc
	 * 
	 * @param operation
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final void addOperation(final Operation operation) {
		GUITools.checkAWT();
		
		final OperationComponent operationComponent = new OperationComponent(this.getOperationListComponent().getSelectionModel(), operation);
		
		this.getOperationListComponent().add(operationComponent, this.getOperationListComponent().getComponentCount() - this.getInsertionIndexFromBottom());
		
		// Update scroll pane
		this.getOperationListComponent().revalidate();
		
		this.repaint();
	}
	
	/**
	 * 
	 * @return
	 * <br>A possibly null value
	 * <br>A shared value
	 */
	public final OperationListComponent getOperationListComponent() {
		if (this.operationListComponent == null) {
			this.operationListComponent = this.createOperationListComponent();
		}
		
		return this.operationListComponent;
	}
	
	protected final void setup(final String titleTranslationKey) {
		this.setLayout(new BorderLayout());
		
		final JPanel titledPanel = titleBorder(titleTranslationKey, scrollable(this.getOperationListComponent()));
		
		translate(titledPanel.getBorder());
		
		this.add(titledPanel, BorderLayout.CENTER);
		
		this.setVisible(true);
	}
	
	/**
	 * 
	 * TODO doc
	 * @return
	 * <br>Range: {@code [0 .. this.getOperationListComponent().getComponentCount()]}
	 */
	protected int getInsertionIndexFromBottom() {
		return 1;
	}
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	protected OperationListComponent createOperationListComponent() {
		return new OperationListComponent();
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
	protected void onHide() {
		// TODO Auto-generated method stub

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
	protected void onQuit() {
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
	protected void saveState() {
		// TODO Auto-generated method stub

	}
	
	private static final long serialVersionUID = 1308159956345271935L;
	
}
