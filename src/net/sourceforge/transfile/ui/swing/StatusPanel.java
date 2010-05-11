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

import static net.sourceforge.transfile.ui.swing.SwingTranslator.Helpers.translate;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;

/**
 * A small panel whose sole purpose is to display textual status messages upon request 
 * 
 * @author Martin Riedel
 *
 */
public class StatusPanel extends TopLevelPanel {
	
	private static final long serialVersionUID = 63220329611742114L;

	/*
	 * Holds the status/error message shown
	 */
	private JLabel statusLabel;
	
	/**
	 * Creates a StatusPanel
	 * 
	 */
	public StatusPanel(final SwingGUI window) {
		super(window);	
	}
	
	/**
	 * Displays the provided status message, overwriting the one currently being shown
	 * 
	 * @param status the message to display
	 */
	public void setStatus(final String status) {
		statusLabel.setText(status);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setup() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
//		statusLabel = new JLabel(getStrings().getString("status_ready"));
		statusLabel = translate(new JLabel("status_ready"));
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(2, 5, 2, 5);
		add(statusLabel, c);
		
//		SwingTranslator.getDefaultTranslator().autotranslate(this);
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

}
