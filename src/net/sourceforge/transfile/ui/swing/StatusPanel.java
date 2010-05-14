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

import static net.sourceforge.transfile.ui.swing.StatusService.StatusMessage;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * <p>A small panel whose sole purpose is to display textual status messages upon request.</p>
 * 
 * <p>Normally displays one message at a time but can be expanded to show more.</p>
 * 
 * @author Martin Riedel
 *
 */
public class StatusPanel extends TopLevelPanel {
	
	private static final long serialVersionUID = 63220329611742114L;
	
	/*
	 * The number of messages/lines to show in the expanded view without having to scroll 
	 */
	private static final int maxMessages = 5;
	
	/*
	 * True if the StatusPanel is in expanded mode
	 */
	private boolean isExpanded = false;
	/*
	 * The number of pixels to add to / subtract from the current size of the StatusPanel when expanding/un-expanding
	 */
	private static final int expandBy = 50;

	/*
	 * Custom JList holding the messages
	 */
	private StatusList statusList;
	
	/*
	 * "more" / "less" buttons
	 */
	private JButton expandButton;
	private JButton unexpandButton;
	
	
	/**
	 * Creates a StatusPanel
	 * 
	 */
	public StatusPanel(final SwingGUI window) {
		super(window);
		
		// listen for new status messages
		getWindow().getStatusService().addStatusListener(new StatusChangeListener());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setup() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		statusList = new StatusList(maxMessages);
		statusList.setLayoutOrientation(JList.VERTICAL);
		statusList.setVisibleRowCount(maxMessages);
		JScrollPane scrollPane = new JScrollPane(statusList,
												 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
												 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(0, 5, 2, 5);
		
		add(scrollPane, c);
		
		expandButton = new JButton("\u2193");
		unexpandButton = new JButton("\u2191");
		unexpandButton.setVisible(false);
		
		expandButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StatusPanel.this.expand();
			}
		});
		
		unexpandButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StatusPanel.this.unexpand();
			}
		});
		
		// make the two buttons the same size
		Dimension expandButtonSize = expandButton.getPreferredSize();
		Dimension unexpandButtonSize = unexpandButton.getPreferredSize();
		Dimension newSize = new Dimension(expandButtonSize.width > unexpandButtonSize.width ? expandButtonSize.width : unexpandButtonSize.width,
										  expandButtonSize.height > unexpandButtonSize.height ? expandButtonSize.height : unexpandButtonSize.height);
		expandButton.setPreferredSize(newSize);
		expandButton.setMinimumSize(newSize);
		expandButton.setMaximumSize(newSize);
		unexpandButton.setPreferredSize(newSize);
		unexpandButton.setMinimumSize(newSize);
		unexpandButton.setMaximumSize(newSize);
		
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.insets = new Insets(0, 0, 2, 5);

		add(expandButton, c);
		add(unexpandButton, c);
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
	protected void onQuit() {
		// do nothing
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
	 * Expands the StatusPanel (and thus the main window) in order to allow the display
	 * of multiple {@link StatusService.StatusMessage}s at the same time.
	 * 
	 * @see #unexpand()
	 */
	private void expand() {
		if(isExpanded)
			throw new IllegalStateException("already expanded");
		
		statusList.setVisibleRowCount(maxMessages);
		
		expandButton.setVisible(false);
		unexpandButton.setVisible(true);
		
		Dimension panelDimensions = getPreferredSize();
		panelDimensions.height += expandBy;
		setPreferredSize(panelDimensions);
		
		// tell the main window to update (StatusPanel is now bigger)
		getWindow().pack();
		
		isExpanded = true;
	}
	
	/**
	 * Reverts a previous expansion so that only one message is being shown at any given time,
	 * making the StatusPanel require less screen real estate.
	 * 
	 * @see #expand()
	 */
	private void unexpand() {
		if(!isExpanded)
			throw new IllegalStateException("not expanded");
		
		statusList.setVisibleRowCount(1);
		
		expandButton.setVisible(true);
		unexpandButton.setVisible(false);
		
		Dimension panelDimensions = getPreferredSize();
		panelDimensions.height -= expandBy;
		setPreferredSize(panelDimensions);
		
		// tell the main window to update (StatusPanel is now smaller)
		getWindow().pack();
		
		isExpanded = false;
	}
	
	/**
	 * Listens for new {@link StatusService.StatusMessage}s and adds them to the list.
	 *
	 * @author Martin Riedel
	 *
	 */
	private class StatusChangeListener implements StatusService.StatusChangeListener {

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public void newStatusMessage(final StatusMessage message) {
			statusList.addMessage(message);
		}
		
	}

}
