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

package net.sourceforge.transfile.network.exceptions;

/**
 * <p>Thrown when a {@link net.soruceforge.transfile.network.BilateralConnector} could not establish
 * a {@link net.sourceforge.transfile.network.Connection} to the remote host.</p>
 * 
 * <p>An attempt to establish a {@code Connection} through a {@code BilateralConnector} is considered 
 * unsuccessful only when both the attempt to establish a connection TO the remote host and the attempt
 * to accept a connection FROM the remote host have failed. Hence, this exception has two causes 
 * (one for each connection attempt).</p>
 *
 * @see net.sourceforge.transfile.network.BilateralConnector
 * @see net.sourceforge.transfile.network.Connection
 * @see net.sourceforge.transfile.network.OutboundConnection
 * @see net.sourceforge.transfile.network.InboundConnection
 * @author Martin Riedel
 *
 */
public class BilateralConnectException extends ConnectException {

	private static final long serialVersionUID = 3914418382908998827L;
	
	/*
	 * The Throwable that was thrown by the code attempting to establish a connection to the remote host
	 */
	private final Throwable causeOutbound;
	
	/*
	 * The Throwable that was thrown by the code attempting to accept a connection from the remote host
	 */
	private final Throwable causeInbound;

	
	/**
	 * Constructs a new instance
	 * 
	 * @param causeOutbound 
	 * <br />The reason why the attempt to establish a connection to the remote host failed
	 * <br />Should not be null
	 * @param causeInbound 
	 * <br />The reason why the attempt to accept a connection from the remote host failed
	 * <br />Should not be null
	 */
	public BilateralConnectException(final Throwable causeOutbound, final Throwable causeInbound) {
		this.causeOutbound = causeOutbound;
		this.causeInbound = causeInbound;
	}
		
	/**
	 * Returns the reason why the attempt to establish a connection to the remote host failed
	 * 
	 * @return the reason why the attempt to establish a connection to the remote host failed
	 */
	public final Throwable getOutboundCause() {
		return this.causeOutbound;
	}
	
	/**
	 * Returns the reason why the attempt to accept a connection from the remote host failed
	 * 
	 * @return the reason why the attempt to accept a connection from the remote host failed
	 */
	public final Throwable getInboundCause() {
		return this.causeInbound;
	}

}
