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

import net.sourceforge.transfile.operations.Session;

/**
 * 
 * 
 * @author Martin Riedel
 * @author codistmonk (modifications since 2010-05-12)
 *
 */
class ReceivePanel extends AbstractOperationListPanel {
	
	/**
	 * 
	 * @param session
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public ReceivePanel(final Session session) {
		super(session);
		
		this.setup();
	}
	
	private final void setup() {
		this.setup("receive_list_title");
	}
	
	private static final long serialVersionUID = -3454758528985441231L;
	
}
