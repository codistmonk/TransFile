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

import net.sourceforge.transfile.operations.Connection.State;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-08)
 *
 */
public abstract class AbstractTestWithConnections {
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	protected abstract Connection[] createMatchingConnectionPair();
	
	/**
	 * TODO doc
	 * 
	 * @param connections
	 * <br>Should not be null
	 */
	protected final void waitUntilConnectionsAreReady(final Connection... connections) {
		try {
			Thread.sleep(this.getInactivityThreshold());
		} catch (final InterruptedException exception) {
			exception.printStackTrace();
		}
		
		for (final Connection connection : connections) {
			if (connection instanceof AbstractConnection) {
				while (System.currentTimeMillis() < ((AbstractConnection) connection).getLastMessageTime() + this.getInactivityThreshold()) {
					Thread.yield();
				}
			}
		}
	}
	
	/**
	 * TODO doc
	 * 
	 * @return a time in milliseconds
	 * <br>Range: {@code [0L .. Long.MAX_VALUE]}
	 */
	protected long getInactivityThreshold() {
		return 500L;
	}
	
	/**
	 * Time in milliseconds.
	 */
	public static final long TEST_TIMEOUT = 60000L;
	
	/**
	 * TODO doc
	 * 
	 * @param state
	 * <br>Can be null
	 * @param connections
	 * <br>Should not be null
	 */
	public static final void waitUntilState(final State state, final Connection... connections) {
		boolean wait = true;
		
		while (wait) {
			Thread.yield();
			
			wait = false;
			
			for (final Connection connection : connections) {
				wait |= connection.getState() != state;
			}
		}
	}
	
}
