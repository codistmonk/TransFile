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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.transfile.network.exceptions.PeerURLFormatException;


/**
 * <p>Represents a peer (fellow TransFile user) reachable via LAN or the Internet.</p>
 * 
 * <p>Textual PeerURL representations are in the "transfile://host:port" format.</p>
 * 
 * <p>Peer instances are immutable.</p>
 * 
 * @author Martin Riedel
 *
 */
public class Peer {
	
	/*
	 * The original internet address (hostname or ip address) string representation that
	 * was provided to this Peer as part of the PeerURL passed to the constructor
	 */
	private String inetAddrString;

	/*
	 * The peer's InetAddress
	 */
	private InetAddress inetAddr;
	
	/*
	 * The peer's port
	 */
	private int port;
	
	/*
	 * The protocol prefix prepended to all PeerURLs
	 */
	public static final String PROTOCOL_PREFIX = "transfile://";
	
	
	/**
	 * Creates a Peer object from the provided PeerURL string
	 * 
	 * @param peerURL
	 * <br />A PeerURL string representation
	 * <br />Should not be null
	 * 
	 * @throws PeerURLFormatException if the provided PeerURL string is invalid
	 * @throws UnknownHostException if the host referenced by the provided PeerURL string cannot be resolved
	 */
	public Peer(final String peerURL)
			throws PeerURLFormatException, UnknownHostException {
		Pattern p = Pattern.compile("^" + PROTOCOL_PREFIX + "(.+):([0-9]+)$");
		Matcher m = p.matcher(peerURL);
		
		if (!m.find())
			throw new PeerURLFormatException("Malformatted PeerURL: " + peerURL);
		
		setup(m.group(1), Integer.parseInt(m.group(2)));	
	}

	/**
	 * Creates a Peer object from the provided IP address or hostname string representation and port
	 * 
	 * @param inetAddrString
	 * <br />The string representation of hostname or IP address
	 * <br />Should not be null 
	 * @param port
	 * <br />The peer's port number
	 * <br />Should be bigger than 0 and smaller than 65536
	 * 
	 * @throws PeerURLFormatException if the provided PeerURL string is invalid
	 * @throws UnknownHostException if the host referenced by the provided PeerURL string cannot be resolved
	 */
	public Peer(final String inetAddrString, final int port)
			throws PeerURLFormatException, UnknownHostException {
		setup(inetAddrString, port);
	}
	
	/**
	 * 
	 * @return the peer's port and address in the form of an {@link InetSocketAddress}
	 */
	public InetSocketAddress toInetSocketAddress() {
		return new InetSocketAddress(this.inetAddr, this.port);
	}
	
	/**
	 * 
	 * @return the peer's address in the form of an {@link InetAddress}
	 */
	public InetAddress getInetAddress() {
		return this.inetAddr;
	}
	
	/**
	 * 
	 * @return the textual representation of the IP address or the hostname particle
	 * of the PeerURL string this Peer instance was created from
	 */
	public String getInetAddressString() {
		return this.inetAddrString;
	}
	
	/**
	 * 
	 * @return the peer's port
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * Computes and returns this Peer's string representation, a PeerURL
	 * 
	 * @return this Peer's string representation
	 */
	@Override
	public String toString() {
		return makePeerURL(this.inetAddrString, this.port);
	}
	
	/**
	 * Creates a PeerURL string from the address string (hostname or IP address) and port provided
	 * 
	 * @param address the address of the peer to be referenced by the PeerURL string
	 * @param port the port of the peer to be referenced by the PeerURL string
	 * @return PeerURL string for the provided port and address
	 */
	public static String makePeerURL(final String address, final int port) {
		return PROTOCOL_PREFIX + address + ":" + port;	
	}
	
	/**
	 * 
	 * TODO doc
	 * @param inetAddrString
	 * @param port
	 * @throws PeerURLFormatException
	 * @throws UnknownHostException
	 */
	private void setup(final String inetAddrString, final int port) 
		throws PeerURLFormatException, UnknownHostException {
		try {
			this.inetAddrString = inetAddrString;
			this.inetAddr = InetAddress.getByName(this.inetAddrString);
			this.port = port;		
		} catch (final NumberFormatException e) {
			throw new PeerURLFormatException(e);
		}
	}

}
