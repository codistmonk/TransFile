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

package transfile.backend;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;

import transfile.network.exceptions.ConnectTimeoutException;
import transfile.network.exceptions.PeerURLFormatException;

/**
 * 
 * 
 * @author Martin Riedel
 *
 */
public interface ControllableBackend {
	
	public void quit();
	
	public String findExternalAddress() throws MalformedURLException, IOException;
	
	public Set<String> findLocalAddresses() throws SocketException;
	
	public Set<String> findLocalAddresses(final boolean ipv4Only) throws SocketException;
	
	public String makePeerURLString(final String address, final int port);
	
	/**
	 * TODO ...
	 * 
	 * MUST support interruption
	 * 
	 * @param remoteURL
	 * @throws UnknownHostException
	 * @throws PeerURLFormatException
	 * @throws IOException
	 * @throws ConnectTimeoutException 
	 * @throws InterruptedException 
	 */
	public void connect(final String remoteURL, final int localPort) 
			throws UnknownHostException, PeerURLFormatException, IOException, ConnectTimeoutException, InterruptedException;
	
}
