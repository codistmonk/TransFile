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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transfile.network.exceptions.ConnectFailedToSetTimeoutException;
import transfile.network.exceptions.ConnectSocketFailedToCloseException;
import transfile.network.exceptions.ConnectIOException;
import transfile.network.exceptions.ConnectSecurityException;
import transfile.network.exceptions.ConnectTimeoutException;
import transfile.network.exceptions.PeerURLFormatException;
import transfile.network.exceptions.ServerFailedToBindException;
import transfile.network.exceptions.ServerFailedToCloseException;
import transfile.settings.Settings;

/**
 * 
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

	public Link(final String peerURL, final int localPort) 
			throws PeerURLFormatException, IOException, InterruptedException, ConnectTimeoutException {
		peer = new PeerURL(peerURL);
		
		connectionToPeer = new ConnectionToPeer(peer);
		connectionFromPeer = new ConnectionFromPeer(peer, localPort);
		
		establishBilateralConnection();
	}
	
	public static String findExternalAddress() throws MalformedURLException, IOException {
		URL siteURL = new URL(Settings.getInstance().getProperty("external_ip_site"));  
		   
		HttpURLConnection siteConnection = (HttpURLConnection) siteURL.openConnection();  
		BufferedReader bufferedSiteReader = new BufferedReader(new InputStreamReader(siteConnection.getInputStream()));  
		   
		return bufferedSiteReader.readLine();  		
	}
	
	public static Set<String> findLocalAddresses(final boolean ipv4Only) throws SocketException {
		Set<String> localAddresses = new HashSet<String>();
		
		// iterate through all local network interfaces
		for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
			NetworkInterface iface = ifaces.nextElement();
			// iterate through all IP addresses of the current local network interface
			for(Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses.hasMoreElements(); ) {
				InetAddress address = addresses.nextElement();
				String addressString = address.toString();
				
				if(ipv4Only) {
					// regular expression that matches 4 blocks of 1-3 digits each, separated by dots
					Pattern p = Pattern.compile("^/(([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3}))$");
					Matcher m = p.matcher(addressString);

					// checks if all 4 blocks are <= 255 as decimal integers
					if(m.find()) {
						if(Integer.parseInt(m.group(2)) < 256 && 
								Integer.parseInt(m.group(3)) < 256 && 
								Integer.parseInt(m.group(4)) < 256 && 
								Integer.parseInt(m.group(5)) < 256)
							// if all the above applies, the current IP address should be an IPv4 address
							localAddresses.add(m.group(1));
					}
				} else {
					localAddresses.add(addressString);
				}
			}
		}
		
		return localAddresses;	
	}
	
	private void establishBilateralConnection() throws InterruptedException  {
		connectionFromPeer.establishInBackground();
		
		try {
			connectionToPeer.establish();
		} catch (ConnectIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// if establishing a Link to the peer has been interrupted, make sure to interrupt
			// both connection attempts (both outgoing and incoming) by interrupting connectionFromPeer
			connectionFromPeer.interruptBackgroundTask();
			throw e;
		} catch (ConnectTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectSocketFailedToCloseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectFailedToSetTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			connectionFromPeer.establish();
		} catch (ConnectTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectSocketFailedToCloseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerFailedToCloseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerFailedToBindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectFailedToSetTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Connection to peer: " + (connectionToPeer.isConnected() == true ? "established" : "failed"));
		System.out.println("Connection from peer: " + (connectionFromPeer.isConnected() == true ? "established" : "failed"));
		
	}
	
}
