/*
 * Copyright (c) 2010 The Codist Monk
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package net.sourceforge.transfile.ui.swing;

import static net.sourceforge.transfile.ui.swing.GUITools.rollover;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * 
 * @author codistmonk (creation 2010-05-16)
 *
 */
@SuppressWarnings("serial")
public class FoldableComponent extends JPanel {
	
	private final JToggleButton toggleButton;
	
	private final Component topComponent;
	
	private final Component bottomComponent;
	
	/**
	 * 
	 * @param topComponent
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param bottomComponent
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public FoldableComponent(final Component topComponent, final Component bottomComponent) {
		super(new GridBagLayout());
		this.toggleButton = rollover(new JToggleButton(), "expand", true);
		this.topComponent = topComponent;
		this.bottomComponent = bottomComponent;
		
		final GridBagLayout layout = (GridBagLayout) this.getLayout();
		final GridBagConstraints constraints = new GridBagConstraints();
		
		{
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;
			constraints.fill = GridBagConstraints.NONE;
			
			layout.setConstraints(this.toggleButton, constraints);
			
			this.toggleButton.setSelected(false);
			this.toggleButton.setFocusable(false);
			
			this.add(this.toggleButton);
		}
		{
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.weightx = 1.0;
			constraints.weighty = 0.0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			
			this.topComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
			
			layout.setConstraints(this.topComponent, constraints);
			
			this.add(this.topComponent);
		}
		{
			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.fill = GridBagConstraints.BOTH;
			
			this.bottomComponent.setVisible(this.toggleButton.isSelected());
			
			layout.setConstraints(this.bottomComponent, constraints);
			
			this.add(this.bottomComponent);
		}
		
		this.toggleButton.addActionListener(new ActionListener() {
			
			@Override
			public final void actionPerformed(final ActionEvent event) {
				final JToggleButton toggleButton = (JToggleButton) event.getSource();
				
				rollover(toggleButton, toggleButton.isSelected() ? "collapse" : "expand", true);
				
				bottomComponent.setVisible(toggleButton.isSelected());
				FoldableComponent.this.revalidate();
				
				final Window window = (Window) getRootPane().getParent();
				final Dimension size = window.getSize();
				
				window.setMinimumSize(null);
				window.pack();
				window.setMinimumSize(window.getSize());
				
				if (bottomComponent.isVisible()) {
					window.setSize(size);
				}
			}
			
		});
	}
	
}
