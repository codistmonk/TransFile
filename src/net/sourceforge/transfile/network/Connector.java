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

package net.sourceforge.transfile.network;

import net.sourceforge.transfile.network.exceptions.ConnectException;
import net.sourceforge.transfile.settings.Settings;

/**
 * TODO doc
 *
 * <p>{@link Connection} Factory</p>
 *
 * @author Martin Riedel
 *
 */
public interface Connector {
	
	/*
	 * The amount of time in milliseconds after which a connection attempt is considered timed out
	 */
	public static final long CONNECT_TIMEOUT = Settings.getPreferences().getLong("connect_timeout", Settings.CONNECT_TIMEOUT);
	
	/*
	 * The maximum time in milliseconds for which both attempts to establish a connection from localhost to the remote peer
	 * and attempts to accept a connection from the remote peer to the local host block in between checking
	 * whether their respective threads have been interrupted
	 */
	public static final int CONNECT_INTERVAL_TIMEOUT = Settings.getPreferences().getInt("connect_interval_time", Settings.CONNECT_INTERVAL_TIME);
	
	
	/**
	 * 
	 * TODO doc
	 * @return
	 */
	public abstract Peer getLocalPeer();
	
	/**
	 * 
	 * TODO doc
	 * @return
	 */
	public abstract Peer getRemotePeer();
	
	/**
	 * TODO doc
	 * 
	 * <p>Factory method for {@link Connection}</p>
	 * 
	 * @param remotePeer
	 * @return
	 */
	public abstract Connection connect() 
		throws ConnectException, InterruptedException;
	
}
