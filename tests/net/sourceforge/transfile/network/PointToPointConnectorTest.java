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

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import net.sourceforge.transfile.network.exceptions.ConnectException;

import org.junit.*;

/**
 * TODO doc
 *
 * @author Martin Riedel
 *
 */
public class PointToPointConnectorTest {

	Peer peerA;
	Peer peerB;
	
	PointToPointConnector connector1;
	PointToPointConnector connector2;
	
	
	@Before
	public void setup() throws Exception {
		this.peerA = new Peer("localhost", 42000);
		this.peerB = new Peer("localhost", 42001);
		
		this.connector1 = new PointToPointConnector(this.peerA, this.peerB);
		this.connector2 = new PointToPointConnector(this.peerB, this.peerA);
	}
	
	@After
	public void cleanup() {
		this.connector1 = null;
		this.connector2 = null;
		
		this.peerA = null;
		this.peerB = null;
	}

	
	@Test
	public void connect() throws InterruptedException, ExecutionException {
		try {
			
			final FutureTask<Connection> c2Task = new FutureTask<Connection>(new Callable<Connection>() {

				@Override
				public Connection call() throws Exception {
					return PointToPointConnectorTest.this.connector2.connect();
				}

			});

			(new Thread(c2Task)).start();

			final Connection c1 = this.connector1.connect();
			final Connection c2 = c2Task.get();

			assertTrue(c1.isConnected());		
			assertTrue(c2.isConnected());
			
		} catch (final ConnectException e) {
			
			fail("c1 failed: " + e);
			
		} catch (final ExecutionException e) {
			
			final Throwable cause = e.getCause();
			
			if (cause instanceof ConnectException)
				fail("c2 failed: " + cause);
			else
				throw e;
			
		}
	}
	
}
