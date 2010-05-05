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

package transfile.gui;

import transfile.backend.ControllableBackend;

/**
 * TODO ...
 * 
 * All implementing classes are expected to not initialize any GUI components or depend on any backend functionality before
 * GUI.start() is called.
 * 
 * @author Martin Riedel
 *
 */
public interface GUI {
	
	public void setBackend(final ControllableBackend backend);
	
	public void start();
	
}
