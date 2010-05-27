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
 * <p>{@link Connection} factory</p>
 * 
 * <p>{@link #connect} may only be called once per {@code Connector} instance. Clone the {@code Connector}
 * if you need to (re-)connect the same local peer with the same remote peer again.</p>
 * 
 * <p>{@code Connector}s that extend {@link AbstractConnector} are thread-safe with respect to
 * {@link #connect}.</p>
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
	 * 
	 * @return true iff this instance has successfully created a {@link Connection} object
	 */
	public abstract boolean isExecuted();
	
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
	 * <p>Factory method for {@link Connection}.</p>
	 * 
	 * <p>May only be called once per {@code Connector} instance. Clone the {@code Connector}
	 * if you need to (re-)connect the same local peer with the same remote peer again.</p>
	 * 
	 * <p>Must be implemented in a thread-safe manner.</p>
	 * 
	 * @param remotePeer
	 * 
	 * @throws IllegalStateException if this {@code Connector} has already established a connection
	 * 
	 * @return
	 */
	public abstract Connection connect() 
		throws IllegalStateException, ConnectException, InterruptedException;
	
	/**
	 * <p>Clones this instance, returning a new, ready to be executed {@code Connector}.</p>
	 * 
	 * <p>The result is <strong>not</strong> necessarily identical to {@code this}. While the local
	 * and remote peers (and, at the discretion of implementing classes, possibly other members) are copied,
	 * new clones are always ready to be executed ({@link #isExecuted()} will return {@code false}). This is
	 * intended behaviour, making it possible to (re-)connect to a host after an earlier, successful
	 * connection attempt.</p> 
	 * 
	 * @return
	 * <br />A clone of this instance
	 * <br />Never null
	 */
	public abstract Connector clone()
		throws CloneNotSupportedException;
	
}
