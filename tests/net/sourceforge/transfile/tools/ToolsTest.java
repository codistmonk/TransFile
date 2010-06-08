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

import static net.sourceforge.transfile.tools.Tools.array;
import static net.sourceforge.transfile.tools.Tools.arrayConcat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Random;

import org.junit.Test;

/**
 * Automated tests using JUnit 4 for {@link Tools}.
 * {@link Tools#debug(int, Object...)} and {@link Tools#debugPrint(Object...)} are not tested
 * because their outputs depend on the source location of the caller.
 * 
 * @author Martin Riedel
 * @author codistmonk (modifications since 2010-05-19) 
 */
public class ToolsTest {
	
	@Test
	public final void testCast() {
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
	public final void testEquals() {
		final Object object = "42";
		
		assertTrue(Tools.equals(null, null));
		assertFalse(Tools.equals(object, null));
		assertTrue(Tools.equals(object, object));
		assertTrue(Tools.equals(new Integer(6 * 7).toString(), object));
		assertFalse(Tools.equals(object, 42));
	}
	
	@Test
	public final void testHashcode() {
		final Object object = "42";
		
		assertEquals(0, Tools.hashCode(null));
		assertEquals(object.hashCode(), Tools.hashCode(object));
	}
	
	@Test
	public final void testToUpperCamelCase() {
		assertEquals("Hello, world!", Tools.toUpperCamelCase("hello, world!"));
		assertEquals("Hello, world!", Tools.toUpperCamelCase("Hello, world!"));
		assertEquals("Helloworld", Tools.toUpperCamelCase("helloworld"));
		assertEquals("HelloWorld", Tools.toUpperCamelCase("helloWorld"));
		assertEquals("HelloWorld", Tools.toUpperCamelCase("HelloWorld"));
	}

	@Test
	public final void testEmptyIfNull() {
		assertEquals("", Tools.emptyIfNull(null));
		assertEquals("", Tools.emptyIfNull(""));
		assertEquals("Hello, world!", Tools.emptyIfNull("Hello, world!"));
	}

	@Test
	public final void testArray() {
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
	public final void testThrowRuntimeException() {
		Throwable originalThrowable = new RuntimeException();
		try {
			Tools.throwUnchecked(originalThrowable);
		} catch(final RuntimeException caughtThrowable) {
			assertSame(originalThrowable, caughtThrowable);
		}

		originalThrowable = new Exception();
		try {
			Tools.throwUnchecked(originalThrowable);
		} catch(final RuntimeException caughtThrowable) {
			assertNotNull(caughtThrowable.getCause());
			assertSame(originalThrowable, caughtThrowable.getCause());
		}
		
		originalThrowable = new Error();
		try {
			Tools.throwUnchecked(originalThrowable);
		} catch(final Error caughtThrowable) {
			assertSame(originalThrowable, caughtThrowable);
		}
		
		originalThrowable = new Throwable();
		try {
			Tools.throwUnchecked(originalThrowable);
		} catch(final Throwable caughtThrowable) {
			assertSame(originalThrowable, caughtThrowable.getCause());
		}
	}
	
	@Test
	public final void testGetCallerClass() {
		assertEquals(this.getClass(), callGetCallerClass());
	}
	
	@Test
	public final void testGetCallerMethodName() {
		assertEquals("testGetCallerMethodName", callGetCallerMethodName());
	}
	
	@Test
	public final void testGetGetter() {
		final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();
		
		{
			final Method getter = Tools.getGetter(objectWithArbitraryProperties, "intProperty");
			
			assertNotNull(getter);
			assertEquals("getIntProperty", getter.getName());
		}
		{
			final Method getter = Tools.getGetter(objectWithArbitraryProperties, "booleanProperty1");
			
			assertNotNull(getter);
			assertEquals("isBooleanProperty1", getter.getName());
		}
		{
			final Method getter = Tools.getGetter(objectWithArbitraryProperties, "booleanProperty2");
			
			assertNotNull(getter);
			assertEquals("hasBooleanProperty2", getter.getName());
		}
		{
			final Method getter = Tools.getGetter(objectWithArbitraryProperties, "booleanProperty3");
			
			assertNotNull(getter);
			assertEquals("getBooleanProperty3", getter.getName());
		}
		{
			final Method getter = Tools.getGetter(objectWithArbitraryProperties, "packagePrivateStringProperty");
			
			assertNotNull(getter);
			assertEquals("getPackagePrivateStringProperty", getter.getName());
		}
	}
	
	@Test
	public final void testGetGetterFailure() {
		final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();
		{
			try {
				// Missing property
				final Method getter = Tools.getGetter(objectWithArbitraryProperties, "missingProperty");
				
				fail("getGetter() should have failed but instead returned " + getter);
			} catch (final RuntimeException expectedException) {
				// Do nothing
			}
		}
		{
			try {
				// Bad casing
				final Method getter = Tools.getGetter(objectWithArbitraryProperties, "INTPROPERTY");
				
				fail("getGetter() should have failed but instead returned " + getter);
			} catch (final RuntimeException expectedException) {
				// Do nothing
			}
		}
	}
	
	@Test
	public final void testGetSetter() {
		final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();
		
		{
			final Method setter = Tools.getSetter(objectWithArbitraryProperties, "intProperty", int.class);
			
			assertNotNull(setter);
			assertEquals("setIntProperty", setter.getName());
		}
		{
			final Method setter = Tools.getSetter(objectWithArbitraryProperties, "booleanProperty1", boolean.class);
			
			assertNotNull(setter);
			assertEquals("setBooleanProperty1", setter.getName());
		}
		{
			final Method setter = Tools.getSetter(objectWithArbitraryProperties, "packagePrivateStringProperty", String.class);
			
			assertNotNull(setter);
			assertEquals("setPackagePrivateStringProperty", setter.getName());
		}
	}
	
	@Test
	public final void testGetSetterFailure() {
		final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();
		
		{
			try {
				// Missing property
				final Method setter = Tools.getGetter(objectWithArbitraryProperties, "missingProperty");
				
				fail("getSetter() should have failed but instead returned " + setter);
			} catch (final RuntimeException expectedException) {
				// Do nothing
			}
			
		}
		{
			try {
				// Bad casing
				final Method setter = Tools.getSetter(objectWithArbitraryProperties, "INTPROPERTY", int.class);
				
				fail("getSetter() should have failed but instead returned " + setter);
			} catch (final RuntimeException expectedException) {
				// Do nothing
			}
		}
		{
			try {
				// Mismatching parameter type
				final Method setter = Tools.getSetter(objectWithArbitraryProperties, "intProperty", boolean.class);
				
				fail("getSetter() should have failed but instead returned " + setter);
			} catch (final RuntimeException expectedException) {
				// Do nothing
			}
		}
	}
	
	@Test
	public final void testIso88591ToUTF8() throws UnsupportedEncodingException {
		{
			final String iso88591 = new String(new byte[] { (byte) 0xCE, (byte) 0xA9 }, "ISO-8859-1");
			final String convertedString = Tools.iso88591ToUTF8(iso88591);
			final String expectedResult = "Ω";
			
			assertNotNull(convertedString);
			assertNotSame(convertedString, iso88591);
			assertEquals("Î©", iso88591);
			assertFalse(expectedResult.equals(iso88591));
			assertEquals(expectedResult, convertedString);
		}
		{
			final String iso88591 = new String(new byte[] { (byte) 0xC3, (byte) 0xA9, (byte) 0xE2, (byte) 0x80, (byte) 0x99 }, "ISO-8859-1");
			final String convertedString = Tools.iso88591ToUTF8(iso88591);
			final String expectedResult = "é’";
			
			assertNotNull(convertedString);
			assertNotSame(convertedString, iso88591);
			assertFalse(expectedResult.equals(iso88591));
			assertEquals(expectedResult, convertedString);
		}
	}
	
	@Test
	public final void testGetLoggerForThisMethod() {
		assertTrue(Tools.getLoggerForThisMethod().getName().endsWith("testGetLoggerForThisMethod"));
	}
	
	@Test
	public final void testGetUserApplicationDirectory() {
		assertNotNull(Tools.getUserApplicationDirectory());
	}
	
	@Test
	public final void testArrayConcat2Arrays() {
		final Integer[] array1 = array(2);
		final Integer[] array2 = array(3, 5);
		final Integer[] array3 = array(7, 11, 13);
		
		{
			final Object[] result = arrayConcat(array(), array());
			
			assertNotNull(result);
			assertArrayEquals(array(), result);
		}
		{
			final Object[] result = arrayConcat(array2, array());
			
			assertNotNull(result);
			assertSame(array2, result);
		}
		{
			final Object[] result = arrayConcat(array(), array3);
			
			assertNotNull(result);
			assertSame(array3, result);
		}
		{
			final Object[] result = arrayConcat(array1, array2);
			
			assertNotNull(result);
			assertArrayEquals(array(2, 3, 5), result);
		}
		{
			final Object[] result = arrayConcat(array3, array1);
			
			assertNotNull(result);
			assertArrayEquals(array(7, 11, 13, 2), result);
		}
	}
	
	@Test
	public final void testArrayConcatElementArray() {
		final Integer element = 42;
		final Integer[] array1 = array(2);
		final Integer[] array3 = array(3, 5, 7);
		
		{
			final Object[] result = arrayConcat(element, array());
			
			assertNotNull(result);
			assertArrayEquals(array(element), result);
		}
		{
			final Object[] result = arrayConcat(element, array1);
			
			assertNotNull(result);
			assertArrayEquals(array(element, 2), result);
		}
		{
			final Object[] result = arrayConcat(element, array3);
			
			assertNotNull(result);
			assertArrayEquals(array(element, 3, 5, 7), result);
		}
	}
	
	@Test
	public final void testArrayConcatArrayElement() {
		final Integer element = 42;
		final Integer[] array1 = array(2);
		final Integer[] array3 = array(3, 5, 7);
		
		{
			final Object[] result = arrayConcat(array(), element);
			
			assertNotNull(result);
			assertArrayEquals(array(element), result);
		}
		{
			final Object[] result = arrayConcat(array1, element);
			
			assertNotNull(result);
			assertArrayEquals(array(2, element), result);
		}
		{
			final Object[] result = arrayConcat(array3, element);
			
			assertNotNull(result);
			assertArrayEquals(array(3, 5, 7, element), result);
		}
	}
	
	/**
	 * Helper method to test {@link Tools#getCallerClass()}.
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	private static final Class<?> callGetCallerClass() {
		return Tools.getCallerClass();
	}
	
	/**
	 * Helper method to test {@link Tools#getCallerMethodName()}.
	 * 
	 * @return
	 * <br>A possibly null value
	 */
	private static final String callGetCallerMethodName() {
		return Tools.getCallerMethodName();
	}
	
	/*
	 * The following classes are used in some tests.
	 * They have the following hierarchy:
	 * A <- B <- C
	 * D (independent)
	 */
	
	/**
	 * 
	 * @author Martin Riedel
	 *
	 */
	private static class A { 
		A() { /* dummy class constructor */ }
	}
	
	/**
	 * 
	 * @author Martin Riedel
	 *
	 */
	private static class B extends A { 
		B() { /* dummy class constructor */ } 
	}
	
	/**
	 * 
	 * @author Martin Riedel
	 *
	 */
	private static class C extends B { 
		C() { /* dummy class constructor */ } 
	}
	
	/**
	 * 
	 * @author Martin Riedel
	 *
	 */
	private static class D { 
		D() { /* dummy class constructor */ } 
	}
	
	/**
	 * This class is package-private to suppress visibility and usage warnings.
	 * 
	 * @author codistmonk (creation 2010-05-19)
	 *
	 */
	static class ObjectWithArbitraryProperties {
		
		private int intProperty;
		
		private boolean booleanProperty1;
		
		private boolean booleanProperty2;
		
		private boolean booleanProperty3;
		
		private String packagePrivateStringProperty;
		
		/**
		 * 
		 * @return
		 * <br>Range: Any integer
		 */
		public final int getIntProperty() {
			return this.intProperty;
		}
		
		/**
		 * 
		 * @param intProperty an arbitrary integer
		 * <br>Range: Any integer
		 */
		public final void setIntProperty(final int intProperty) {
			this.intProperty = intProperty;
		}
		
		public final boolean isBooleanProperty1() {
			return this.booleanProperty1;
		}
		
		/**
		 * 
		 * @param booleanProperty1 an arbitrary boolean
		 */
		public final void setBooleanProperty1(final boolean booleanProperty1) {
			this.booleanProperty1 = booleanProperty1;
		}
		
		public final boolean hasBooleanProperty2() {
			return this.booleanProperty2;
		}
		
		/**
		 * 
		 * @param booleanProperty2 an arbitrary boolean
		 */
		public final void setBooleanProperty2(final boolean booleanProperty2) {
			this.booleanProperty2 = booleanProperty2;
		}
		
		public final boolean getBooleanProperty3() {
			return this.booleanProperty3;
		}
		
		/**
		 * 
		 * @param booleanProperty3 an arbitrary boolean
		 */
		public final void setBooleanProperty3(final boolean booleanProperty3) {
			this.booleanProperty3 = booleanProperty3;
		}
		
		/**
		 * 
		 * @return
		 * <br>A possibly null value
		 * <br>A shared value
		 */
		final String getPackagePrivateStringProperty() {
			return this.packagePrivateStringProperty;
		}
		
		/**
		 * 
		 * @param packagePrivateStringProperty
		 * <br>Can be null
		 * <br>Shared parameter
		 */
		final void setPackagePrivateStringProperty(final String packagePrivateStringProperty) {
			this.packagePrivateStringProperty = packagePrivateStringProperty;
		}
		
	}
	
}
