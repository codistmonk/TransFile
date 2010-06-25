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

/** 
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-25)
 *
 */
public abstract class AbstractTopLevelPanel extends TopLevelPanel {
	
	/** 
	 *
	 * @param window
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public AbstractTopLevelPanel(final SwingGUI window) {
		super(window);
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
	
	private static final long serialVersionUID = 3284818274597268216L;
	
}