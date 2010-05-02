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

package transfile.gui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;

/**
 * 
 * 
 * @author Martin Riedel
 *
 */
class ReceivePanel extends TopLevelPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3454758528985441231L;

	public ReceivePanel() {
		super("Receive");	
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	void onQuit() {
		// do nothing
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	void onInit() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	void onHide() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void onShow() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setup() {
		setLayout(new GridBagLayout());
		
		final JButton receiveButton = new JButton("Receive");
		final GridBagConstraints receiveButtonConstraints = new GridBagConstraints();
		receiveButtonConstraints.gridx = 0;
		receiveButtonConstraints.gridy = 0;
		add(receiveButton, receiveButtonConstraints);
	}	
}
