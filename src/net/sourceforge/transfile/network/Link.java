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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;

import net.sourceforge.transfile.network.exceptions.LinkFailedException;
import net.sourceforge.transfile.network.exceptions.PeerURLFormatException;
import net.sourceforge.transfile.settings.Settings;


/**
 * A Link is a monolateral or bilateral connection between two peers. An attempt to both
 * establish a connection from the local host to the remote host and to accept an incoming connection 
 * from the remote host. If at least one of those attempts succeeds, the Link is established.
 * 
 * @author Martin Riedel
 *
 */
public class Link {
	
	/*
	 * PeerURL representing the peer this Link connects to
	 */
	private final PeerURL peer;
	
	/*
	 * The Connection representing the connection localhost established to the remote peer
	 */
	private ConnectionToPeer connectionToPeer;
	
	/*
	 * The Connection representing the connection localhost accepted from the remote peer
	 */
	private ConnectionFromPeer connectionFromPeer;

	
	/**
	 * 
	 * TODO doc
	 */
	public Link(final String peerURL, final int localPort) 
			throws PeerURLFormatException, UnknownHostException, InterruptedException, LinkFailedException {
		peer = new PeerURL(peerURL);
		
		connectionToPeer = new ConnectionToPeer(peer);
		connectionFromPeer = new ConnectionFromPeer(peer, localPort);
		
		establishLink();
	}
	
	/**
	 * 
	 * TODO doc
	 */
	public static String findExternalAddress() throws MalformedURLException, IOException {
		URL siteURL = new URL(Settings.getInstance().getProperty("external_ip_site"));  
		   
		HttpURLConnection siteConnection = (HttpURLConnection) siteURL.openConnection();  
		BufferedReader bufferedSiteReader = new BufferedReader(new InputStreamReader(siteConnection.getInputStream()));  
		   
		return bufferedSiteReader.readLine();  		
	}
	
	/**
	 * 
	 * TODO doc
	 */
	public static Set<String> findLocalAddresses(final boolean ipv4Only) throws SocketException {
		Set<String> localAddresses = new HashSet<String>();
		
		// iterate through all local network interfaces
		for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
			NetworkInterface iface = ifaces.nextElement();
			// iterate through all IP addresses of the current local network interface
			for(Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses.hasMoreElements(); ) {
				InetAddress address = addresses.nextElement();
				
				if(ipv4Only) {
					// At the current state of the Java Networking API an InetAddress is either
					// implemented by Inet4Adress or Inet6Address
					if(address instanceof Inet4Address) {
						localAddresses.add(address.getHostAddress());
					}
				} else {
					localAddresses.add(address.toString());
				}
			}
		}
		
		return localAddresses;	
	}
	
	/**
	 * TODO doc
	 * 
	 * @throws LinkFailedException if the link could not be established
	 */
	private void establishLink() throws InterruptedException, LinkFailedException  {		
		connectionFromPeer.establishInBackground();
		
		// make these members and define getters as well as other Link state
		Exception connectionToPeerError = null;
		Exception connectionFromPeerError = null;
		
		try {
			connectionToPeer.establish();
		} catch(InterruptedException e) {
			// if establishing a Link to the peer has been interrupted, make sure to interrupt
			// both connection attempts (both outgoing and incoming) by interrupting connectionFromPeer
			connectionFromPeer.interruptBackgroundTask();
			throw e;
		} catch(Exception e) {
			connectionToPeerError = e;
		}
		
		try {
			connectionFromPeer.establish();
		} catch(CancellationException e) {
			throw new InterruptedException();
		} catch(Exception e) {
			connectionFromPeerError = e;
		}

		System.out.println("Connection to peer: " + (connectionToPeer.isConnected() == true ? "established" : "failed"));
		System.out.println("Connection from peer: " + (connectionFromPeer.isConnected() == true ? "established" : "failed"));
		
		if(!(connectionToPeer.isConnected() || connectionFromPeer.isConnected()))
			throw new LinkFailedException(connectionToPeerError, connectionFromPeerError);
		
	}
	
}
