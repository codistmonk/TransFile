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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import net.sourceforge.transfile.tools.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-05-20)
 *
 */
public final class GUITools {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private GUITools() {
		// Do nothing
	}
	
	private static final Map<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
	
	public static final String IMAGES_BASE = "images/";
	
	/**
	 * 
	 * @param resourceName
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 * @throws RuntimeException if the resource cannot be loaded
	 */
	public static final ImageIcon getIcon(final String resourceName) {
		try {
			final ImageIcon cachedIcon = iconCache.get(resourceName);
			
			if (cachedIcon != null) {
				return cachedIcon;
			}
			
			final ImageIcon icon = new ImageIcon(ImageIO.read(GUITools.class.getClassLoader().getResourceAsStream(IMAGES_BASE + resourceName)));
			
			iconCache.put(resourceName, icon);
			
			return icon;
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	/**
	 * 
	 * @param resourceName
	 * <br>Should not be null
	 * @return
	 * <br>A possibly null value
	 * <br>A new value
	 */
	public static final ImageIcon getIconOrNull(final String resourceName) {
		try {
			return getIcon(resourceName);
		} catch (final Exception exception) {
			return null;
		}
	}
	
	public static final void useSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param event
	 * <br>IN_OUT
	 * <br>NOT_NULL
	 * @return
	 * <br>MAYBE_NEW
	 * <br>NOT_NULL
	 */
	@SuppressWarnings("unchecked")
	public static final List<File> getFiles(final DropTargetDropEvent event) {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		
		try {
			if (event.getCurrentDataFlavorsAsList().contains(DataFlavor.javaFileListFlavor)) {
				return (List<File>)event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			}
			
			if (event.getCurrentDataFlavorsAsList().contains(DataFlavor.stringFlavor)) {
				return Arrays.asList(new File((String)event.getTransferable().getTransferData(DataFlavor.stringFlavor)));
			}
			
			return Collections.emptyList();
		} catch (final Exception exception) {
			return Tools.throwUnchecked(exception);
		}
	}
	
	/**
	 * 
	 * @param container
	 * <br>Should not be null
	 * <br>Input-output parameter
	 * @param component
	 * <br>Should not be null
	 * <br>Input-output parameter
	 * <br>Shared parameter
	 * @param constraints
	 * <br>Should not be null
	 */
	public static final void add(final Container container, final Component component, final GridBagConstraints constraints) {
		checkAWT();
		
		if (!(container.getLayout() instanceof GridBagLayout)) {
			container.setLayout(new GridBagLayout());
		}
		
		final GridBagLayout layout = (GridBagLayout) container.getLayout();
		
		layout.setConstraints(component, constraints);
		
		container.add(component);
	}
	
    /**
     *
     * @param <T> the actual type of {@code button}
     * @param button
     * <br>Should not be null
     * <br>Input-output parameter
     * <br>Shared parameter
     * @param imageName
     * <br>Should not be null
     * @param borderPainted if {@code false}, then the preferred size is set to the size of the image,
     * and the background and border are not drawn; if {@true}, {@code button} is left in its current state
     * @return {@code button}
     * <br>A non-null value
     * <br>A shared value
     */
    public static final <T extends AbstractButton> T rollover(final T button, final String imageName, final boolean borderPainted) {
		checkAWT();
		
        button.setRolloverEnabled(true);
        button.setDisabledIcon(getIconOrNull(imageName + "_disabled.png"));
        button.setIcon(getIconOrNull(imageName + ".png"));
        button.setSelectedIcon(getIconOrNull(imageName + "_selected.png"));
        button.setRolloverIcon(getIconOrNull(imageName + "_rollover.png"));
        button.setRolloverSelectedIcon(getIconOrNull(imageName + "_rollover_selected.png"));
        
        if (!borderPainted) {
        	button.setPreferredSize(new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
        	button.setBorderPainted(false);
        }
        
        return button;
    }
	
	/**
	 * 
	 * @throws IllegalStateException if the current thread is not the AWT Event Dispatching Thread
	 */
	public static final void checkAWT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("This section must be executed in the AWT Event Dispatching Thread");
		}
	}
	
	/**
	 * 
	 * @throws IllegalStateException if the current thread is the AWT Event Dispatching Thread
	 */
	public static final void checkNotAWT() {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("This section must not be executed in the AWT Event Dispatching Thread");
		}
	}
	
    /**
     *
     * @param components
     * <br>Should not be null
     * @return
     * <br>A new value
     * <br>A non-null value
     */
    public static final JPanel horizontalFlow(final Component... components) {
    	checkAWT();
    	
    	final JPanel result = new JPanel(new FlowLayout());

        for (final Component component : components) {
            result.add(component);
        }

        return result;
    }
	
	/**
	 * 
	 * Encloses {@code component} in a scroll pane.
	 * 
	 * @param component
	 * <br>IN_OUT
	 * <br>NOT_NULL
	 * @return
	 * <br>NEW
	 * <br>NOT_NULL
	 */
	public static final JScrollPane scrollable(final Component component) {
		checkAWT();
		
		return new JScrollPane(component);
	}
	
	/**
	 * Encloses {@code component} in a panel with a titled border.
	 * 
	 * @param title
	 * <br>NOT_NULL
	 * @param component
	 * <br>IN_OUT
	 * <br>NOT_NULL
	 * @return
	 * <br>NEW
	 * <br>NOT_NULL
	 */
	public static final JPanel titleBorder(final String title, final JComponent component) {
		checkAWT();
		
		final JPanel result = new JPanel(new GridLayout(1, 1));
		
		result.setBorder(new TitledBorder(title));
		
		result.add(component);
		
		return result;
	}
	
}
