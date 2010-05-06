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
 * Thrown when a {@link net.soruceforge.transfile.network.Link} to the remote host could not be established.
 * An attempt to establish a Link is considered unsuccessful only when both the attempt to establish a
 * connection TO the remote host and the attempt to accept a connection FROM the remote host have failed.
 * Hence, this exception has two causes (one for each connection attempt).
 *
 * @see net.sourceforge.transfile.network.Link
 * @author Martin Riedel
 *
 */
public class LinkFailedException extends Exception {

	private static final long serialVersionUID = 3914418382908998827L;
	
	/*
	 * The Throwable that was thrown by the code attempting to establish a connection to the remote host
	 */
	private final Throwable causeConnectionToPeer;
	
	/*
	 * The Throwable that was thrown by the code attempting to accept a connection from the remote host
	 */
	private final Throwable causeConnectionFromPeer;

	
	/**
	 * Constructs a new LinkFailedException
	 * 
	 * @param causeConnectionToPeer the reason why the attempt to establish a connection to the remote host failed
	 * @param causeConnectionFromPeer the reason why the attempt to accept a connection from the remote host failed
	 */
	public LinkFailedException(Throwable causeToPeer, Throwable causeFromPeer) {
		this.causeConnectionToPeer = causeToPeer;
		this.causeConnectionFromPeer = causeFromPeer;
	}
	
	/**
	 * Returns the reason why the attempt to establish a connection to the remote host failed
	 * 
	 * @return the reason why the attempt to establish a connection to the remote host failed
	 */
	public Throwable getCauseConnectionToPeer() {
		return causeConnectionToPeer;
	}
	
	/**
	 * Returns the reason why the attempt to accept a connection from the remote host failed
	 * 
	 * @return the reason why the attempt to accept a connection from the remote host failed
	 */
	public Throwable getCauseConnectionFromPeer() {
		return causeConnectionFromPeer;
	}

}
