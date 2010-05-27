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

/**
 * TODO doc
 * 
 * <p>All {@link Connector}s extending {@code AbstractConnector} are thread-safe with respect to
 * {@link Connector#connect}.</p>
 *
 * @author Martin Riedel
 *
 */
public abstract class AbstractConnector implements Connector {

	/*
	 * TODO doc
	 */
	private final Peer localPeer;

	/*
	 * TODO doc
	 */
	private final Peer remotePeer;
	
	/*
	 * TODO doc
	 */
	private boolean executed = false;
	
	
	/**
	 * Constructs a new instance
	 * 
	 * TODO doc
	 * 
	 * @param localPeer
	 * @param remotePeer
	 */
	public AbstractConnector(final Peer localPeer, final Peer remotePeer) {
		this.localPeer = localPeer;
		this.remotePeer = remotePeer;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Peer getLocalPeer() {
		return this.localPeer;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public final Peer getRemotePeer() {
		return this.remotePeer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final synchronized Connection connect() throws IllegalStateException, ConnectException, InterruptedException {
		if(this.executed)
			throw new IllegalStateException();
		
		final Connection c = _connect();
		
		// if the flow reaches here, the implementing Connector has successfully established a connection
		this.executed = true;
		
		return c;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExecuted() {
		return this.executed;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract AbstractConnector clone()
		throws CloneNotSupportedException;
	
	/**
	 * TODO doc
	 * 
	 * @return
	 * @throws ConnectException
	 * @throws InterruptedException
	 */
	protected abstract Connection _connect()
		throws ConnectException, InterruptedException; 

}
