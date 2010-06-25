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
import javax.swing.SwingUtilities;

import net.sourceforge.transfile.operations.ReceiveOperation;
import net.sourceforge.transfile.operations.SendOperation;
import net.sourceforge.transfile.operations.Session;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-05-25)
 *
 */
public class TransferPanel extends AbstractTopLevelPanel {
	
	private final SendPanel sendPanel;
	
	private final ReceivePanel receivePanel;
	
	/**
	 * @param window
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public TransferPanel(final SwingGUI window) {
		super(window);
		this.sendPanel = new SendPanel(window.getSession());
		this.receivePanel = new ReceivePanel(window.getSession());
		
		this.setup();
		
		this.getWindow().getSession().addSessionListener(this.new NewOperationHandler());
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final SendPanel getSendPanel() {
		return this.sendPanel;
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A shared value
	 */
	public final ReceivePanel getReceivePanel() {
		return this.receivePanel;
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
	
	/**
	 * TODO doc
	 *
	 * @author codistmonk (2010-06-13)
	 *
	 */
	private class NewOperationHandler implements Session.Listener {
		
		/**
		 * Package-private default constructor to suppress visibility warnings.
		 */
		NewOperationHandler() {
			// Do nothing
		}
		
		@Override
		public final void sendOperationAdded(final SendOperation sendOperation) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public final void run() {
					TransferPanel.this.getSendPanel().addOperation(sendOperation);
				}
				
			});
		}
		
		@Override
		public final void receiveOperationAdded(final ReceiveOperation receiveOperation) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public final void run() {
					TransferPanel.this.getReceivePanel().addOperation(receiveOperation);
				}
				
			});
		}
		
	}
	
	private static final long serialVersionUID = 741764422630681769L;
	
}
