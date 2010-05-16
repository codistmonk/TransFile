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
		
		assertEquals(Tools.cast(A.class, null), null);
		
		assertTrue(Tools.cast(A.class, new B()) instanceof A);
		
		assertFalse(Tools.cast(B.class, new A()) instanceof B);
		assertEquals(Tools.cast(B.class, new A()), null);
		
		assertTrue(Tools.cast(A.class, new C()) instanceof A);
		
		assertFalse(Tools.cast(C.class, new D()) instanceof C);
		assertEquals(Tools.cast(C.class, new D()), null);
	}
	
	@Test(expected=NullPointerException.class)
	public void cast_to_null() {
		Tools.cast(null, new B());
	}
	
	public void cast_null_to_null() {
		assertEquals(Tools.cast(null, null), null);
	}

	@Test
	public void toUpperCamelCase() {
		assertTrue(Tools.toUpperCamelCase("hello, world!").equals("Hello, world!"));
		assertTrue(Tools.toUpperCamelCase("Hello, world!").equals("Hello, world!"));
		assertTrue(Tools.toUpperCamelCase("helloworld").equals("Helloworld"));
		assertTrue(Tools.toUpperCamelCase("helloWorld").equals("HelloWorld"));
		assertTrue(Tools.toUpperCamelCase("HelloWorld").equals("HelloWorld"));
	}

	@Test
	public void emptyIfNull() {
		assertTrue("".equals(Tools.emptyIfNull(null)));
		assertTrue("".equals(Tools.emptyIfNull("")));
		assertFalse("".equals(Tools.emptyIfNull("Hello, world!")));
	}

	@Test
	public void array() {
		assertArrayEquals(Tools.array(5, 3, 10), new Integer[] { 5, 3, 10 });
		assertArrayEquals(Tools.array("hello", " ", "world", "!"), new String[] { "hello", " ", "world", "!"} );
		assertArrayEquals(Tools.array(), new Object[] { });

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

	@Test(expected=IllegalStateException.class)
	public void throwRuntimeException_runtimeException() throws Throwable {
		try {
			Tools.throwRuntimeException(new IllegalStateException("the answer is 42"));
		} catch(Throwable e) {
			assertTrue(e instanceof IllegalStateException);
			assertTrue(e.getMessage().equals("the answer is 42"));
			throw e;
		}

	}

	@Test(expected=RuntimeException.class)
	public void throwRuntimeException_checkedException() throws Throwable {
		try {
			Tools.throwRuntimeException(new TestException("the answer is 42"));
		} catch(Throwable e) {
			assertTrue(e instanceof RuntimeException);
			assertTrue(e.getCause() != null);
			assertTrue(e.getCause() instanceof TestException);
			assertTrue(e.getCause().getMessage().equals("the answer is 42"));
			throw e;
		}
	}

	private static class TestException extends Exception {

		private static final long serialVersionUID = -8569090437805695602L;

		public TestException(String message) {
			super(message);
		}

	}

	private static class A { }

	private static class B extends A { }
	
	private static class C extends B { }
	
	private static class D extends B { }

}
