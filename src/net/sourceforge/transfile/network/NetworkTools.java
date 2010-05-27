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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.transfile.settings.Settings;

/**
 * Provides networking helper functions
 *
 * @author Martin Riedel
 *
 */
public final class NetworkTools {

	/**
	 * 
	 * TODO doc
	 */
	public static String findExternalAddress() throws MalformedURLException, IOException {
		URL siteURL = new URL(Settings.getPreferences().get("external_ip_site", Settings.EXTERNAL_IP_SITE));  
		   
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
					// InetAddress is implemented by either Inet4Adress or Inet6Address
					if(address instanceof Inet4Address)
						localAddresses.add(address.getHostAddress());
				} else {
					localAddresses.add(address.getHostAddress());
				}
			}
		}
		
		return localAddresses;	
	}
	
}
