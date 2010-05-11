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

package net.sourceforge.transfile.backend;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;

import net.sourceforge.transfile.network.exceptions.ConnectTimeoutException;
import net.sourceforge.transfile.network.exceptions.LinkFailedException;
import net.sourceforge.transfile.network.exceptions.PeerURLFormatException;


/**
 * The application's backend, controllable by a user interface. 
 * 
 * @author Martin Riedel
 *
 */
public interface ControllableBackend {
	
	/**
	 * TODO doc
	 * 
	 * Responsible for persistence. Should be the last method called in the UI's quit method
	 */
	public void quit();

	/**
	 * 
	 * TODO doc
	 */
	public String findExternalAddress() throws MalformedURLException, IOException;
	
	/**
	 * 
	 * TODO doc
	 */
	public Set<String> findLocalAddresses() throws SocketException;
	
	/**
	 * 
	 * TODO doc
	 */
	public Set<String> findLocalAddresses(final boolean ipv4Only) throws SocketException;
	
	/**
	 * 
	 * TODO doc
	 */
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
			throws UnknownHostException, PeerURLFormatException, InterruptedException, LinkFailedException;
	
}
