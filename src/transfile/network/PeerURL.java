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

package transfile.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transfile.network.exceptions.PeerURLFormatException;

/**
 * Represents a peer (fellow TransFile user) reachable via LAN or the Internet.
 * 
 * Textual PeerURL representations are in the "transfile://host:port" format.
 * 
 * @author Martin Riedel
 *
 */
public class PeerURL {
	
	/*
	 * The original internet address (hostname or ip address) string representation that
	 * was provided to this PeerURL as part of the PeerURL string representation passed to the constructor
	 */
	private final String inetAddrString;

	/*
	 * The InetAddress of the peer this PeerURL represents
	 */
	private final InetAddress inetAddr;
	
	/*
	 * The port of the peer this PeerURL represents
	 */
	private final int port;
	
	/*
	 * The protocol prefix prepended to all PeerURLs
	 */
	public static final String protocolPrefix = "transfile://";
	
	
	/**
	 * Creates a PeerURL object from the provided PeerURL string representation
	 * 
	 * @param peerURLString a PeerURL string representation
	 * @throws PeerURLFormatException if the provided PeerURL string representation is invalid
	 * @throws UnknownHostException if the host referenced by the provided PeerURL string representation cannot be resolved
	 */
	public PeerURL(final String peerURLString) throws PeerURLFormatException, UnknownHostException {
		Pattern p = Pattern.compile("^" + protocolPrefix + "(.+):([0-9]+)$");
		Matcher m = p.matcher(peerURLString);
		
		if(!m.find())
			throw new PeerURLFormatException("Malformatted PeerURL: " + peerURLString);
		
		try {
			inetAddrString = m.group(1);
			inetAddr = InetAddress.getByName(inetAddrString);
			port = Integer.parseInt(m.group(2));		
		} catch(NumberFormatException e) {
			throw new PeerURLFormatException(e);
		}			
	}
	
	/**
	 * 
	 * @return the peer's port and address in the form of an InetSocketAddress
	 */
	public InetSocketAddress toInetSocketAddress() {
		return new InetSocketAddress(inetAddr, port);
	}
	
	/**
	 * 
	 * @return the InetAddress of the peer this PeerURL represents
	 */
	public InetAddress getInetAddress() {
		return inetAddr;
	}
	
	/**
	 * 
	 * @return the textual representation of the IP address or the hostname particle of the PeerURL string this PeerURL was created from
	 */
	public String getInetAddressString() {
		return inetAddrString;
	}
	
	/**
	 * 
	 * @return the port of the peer this PeerURL represents
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Computes and returns this PeerURL's string representation
	 * 
	 * @return this PeerURL's string representation
	 */
	public String toString() {
		return makePeerURLString(inetAddrString, port);
	}
	
	/**
	 * Creates a PeerURL string representation from the address (hostname or IP address) and port provided
	 * 
	 * @param address the address of the peer to be referenced by the PeerURL string
	 * @param port the port of the peer to be referenced by the PeerURL string
	 * @return string representation of the PeerURL for the provided port and address
	 */
	public static String makePeerURLString(final String address, final int port) {
		return protocolPrefix + address + ":" + port;	
	}

}
