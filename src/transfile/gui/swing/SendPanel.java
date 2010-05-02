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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 * 
 * 
 * @author Martin Riedel
 *
 */
public class SendPanel extends TopLevelPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3849684830598909661L;
	
	final JTextField fileText = new JTextField();

	public SendPanel() {
		super("Send");
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
		
		final JFileChooser fileChooser = new JFileChooser();
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(5, 5, 5, 5);
		add(fileText, c);
		
		final JButton fileButton = new JButton("Select File");
		fileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fileChooser.showOpenDialog(SendPanel.this) == JFileChooser.APPROVE_OPTION)
					setFile(fileChooser.getSelectedFile());
			}			
		});
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(5, 5, 5, 5);
		add(fileButton, c);
	}
	
	private void setFile(final File file) {
		try {
			fileText.setText(file.getCanonicalFile().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
