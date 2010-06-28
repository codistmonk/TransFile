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

package net.sourceforge.transfile.operations;

import static net.sourceforge.transfile.operations.AbstractConnection.DEFAULT_LOCAL_PEER;
import static net.sourceforge.transfile.operations.AbstractConnection.DEFAULT_REMOTE_PEER;
import static net.sourceforge.jenerics.collections.CollectionsTools.array;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-15)
 *
 */
public class SimpleSocketConnectionTest extends AbstractConnectionTestBase {
	
	@Override
	protected final Connection createUnmatchedConnection() {
		return new SimpleSocketConnection();
	}
	
	@Override
	protected final Connection[] createMatchingConnectionPair() {
		return array(new SimpleSocketConnection(DEFAULT_LOCAL_PEER, DEFAULT_REMOTE_PEER), new SimpleSocketConnection(DEFAULT_REMOTE_PEER, DEFAULT_LOCAL_PEER));
	}
	
}
