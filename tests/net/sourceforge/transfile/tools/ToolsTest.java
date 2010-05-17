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

package net.sourceforge.transfile.tools;

import static org.junit.Assert.*;

import java.util.Random;

import net.sourceforge.transfile.tools.Tools;

import org.junit.*;

/**
 * Tests ...transfile.tools.Tools
 *
 * @author Martin Riedel
 *
 */
public class ToolsTest {
	
	@Test
	public void cast() {
		assertNull(Tools.cast(A.class, null));
		
		A a = new A();
		assertSame(a, Tools.cast(A.class, a));
		
		B b = new B();
		assertSame(b, Tools.cast(A.class, b));
		
		assertNull(Tools.cast(B.class, new A()));
		
		C c = new C();
		assertSame(c, Tools.cast(A.class, c));
		
		assertNull(Tools.cast(A.class, new D()));
	}

	@Test
	public void toUpperCamelCase() {
		assertEquals("Hello, world!", Tools.toUpperCamelCase("hello, world!"));
		assertEquals("Hello, world!", Tools.toUpperCamelCase("Hello, world!"));
		assertEquals("Helloworld", Tools.toUpperCamelCase("helloworld"));
		assertEquals("HelloWorld", Tools.toUpperCamelCase("helloWorld"));
		assertEquals("HelloWorld", Tools.toUpperCamelCase("HelloWorld"));
	}

	@Test
	public void emptyIfNull() {
		assertEquals("", Tools.emptyIfNull(null));
		assertEquals("", Tools.emptyIfNull(""));
		assertEquals("Hello, world!", Tools.emptyIfNull("Hello, world!"));
	}

	@Test
	public void array() {
		assertArrayEquals(new Integer[] { 5, 3, 10 }, Tools.array(5, 3, 10));
		assertArrayEquals(new String[] { "hello", " ", "world", "!"}, Tools.array("hello", " ", "world", "!") );
		assertArrayEquals(new Object[] { }, Tools.array());

		// perform a randomized test 

		int[][] arrays = new int[10][];

		Random arraySizeRandomizer = new Random();
		Random arrayValuesRandomizer = new Random();

		for(int i = 0; i < 10; i++) {
			int numDoubles = arraySizeRandomizer.nextInt(90) + 10;

			arrays[i] = new int[numDoubles];

			for(int j = 0; j < numDoubles; j++) 
				arrays[i][j] = arrayValuesRandomizer.nextInt(Integer.MAX_VALUE);
		}

		assertTrue(java.util.Arrays.deepEquals(Tools.array(
				arrays[0], arrays[1], arrays[2], arrays[3], arrays[4], 
				arrays[5], arrays[6], arrays[7], arrays[8], arrays[9]), arrays));	
	}
	
	@Test
	public void throwRuntimeException() throws Throwable {
		Throwable originalThrowable = new RuntimeException();
		try {
			Tools.throwRuntimeException(originalThrowable);
		} catch(final RuntimeException caughtThrowable) {
			assertSame(originalThrowable, caughtThrowable);
		}

		originalThrowable = new Exception();
		try {
			Tools.throwRuntimeException(originalThrowable);
		} catch(final RuntimeException caughtThrowable) {
			assertNotNull(caughtThrowable.getCause());
			assertSame(originalThrowable, caughtThrowable.getCause());
		}
		
		originalThrowable = new Error();
		try {
			Tools.throwRuntimeException(originalThrowable);
		} catch(final Error caughtThrowable) {
			assertSame(originalThrowable, caughtThrowable);
		}
		
		originalThrowable = new Throwable();
		try {
			Tools.throwRuntimeException(originalThrowable);
		} catch(final Throwable caughtThrowable) {
			assertSame(originalThrowable, caughtThrowable.getCause());
		}
	}

	private static class A { 
		A() { /* dummy class constructor */ }
	}

	private static class B extends A { 
		B() { /* dummy class constructor */ } 
	}
	
	private static class C extends B { 
		C() { /* dummy class constructor */ } 
	}
	
	private static class D { 
		D() { /* dummy class constructor */ } 
	}

}
