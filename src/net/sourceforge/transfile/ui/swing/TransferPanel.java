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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JSplitPane;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-05-25)
 *
 */
public class TransferPanel extends TopLevelPanel {
	
	private final SendPanel sendPanel;
	
	private final ReceivePanel receivePanel;
	
	/**
	 * @param window
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public TransferPanel(final SwingGUI window) {
		super(window);
		this.sendPanel = new SendPanel(window);
		this.receivePanel = new ReceivePanel(window);
		
		this.setup();
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
	
	private final void setup() {
		this.setLayout(new BorderLayout());
		
		this.sendPanel.setPreferredSize(new Dimension(340, 150));
		
		this.receivePanel.setPreferredSize(new Dimension(340, 150));
		
		this.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				this.sendPanel,
				this.receivePanel
		));
	}
	
	private static final long serialVersionUID = 741764422630681769L;
	
}
