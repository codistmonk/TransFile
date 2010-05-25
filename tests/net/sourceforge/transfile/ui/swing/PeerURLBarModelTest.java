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

package net.sourceforge.transfile.ui.swing;

import static org.junit.Assert.*;
import static net.sourceforge.transfile.tools.UnitTestingTools.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.MutableComboBoxModel;

import net.sourceforge.transfile.tools.MutableModelAdapter;

import org.junit.*;

/**
 * <p>Tests net.sourceforge.transfile.ui.swing.PeerURLBar.PeerURLBarModel.</p>
 * 
 * <p>Does not cover persistence or the firing of events at this time.</p>
 *
 * @author Martin Riedel
 *
 */
public class PeerURLBarModelTest {
	
	private static final int maxRetainedItems = 6;
	
	private PeerURLBar bar;
	private MutableComboBoxModel model;
	
	private final MutableModelAdapter modelAdapter = new MutableModelAdapter() {
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public <T> void addElement(final T newElement) {
			PeerURLBarModelTest.this.getModel().addElement(newElement);
		}
		
	};
	
	@Before
	public void setup() {
		this.bar = new PeerURLBar(maxRetainedItems);
		this.model = (MutableComboBoxModel) this.bar.getModel();
	}
	
	@After
	public void cleanup() {
		this.bar = null;
		this.model = null;
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void emptyOnInitialization() {
		assertEquals(0, this.model.getSize());
		assertNull(this.model.getSelectedItem());
		this.model.getElementAt(0);
	}
	
	@Test
	public void addElementObject() {
		final Object e = new Object();
		
		this.model.addElement(e);
		
		assertEquals(1, this.model.getSize());
		assertArrayEquals(new Object[] { e }, modelToArray(this.model));
		assertEquals(e, this.model.getElementAt(0));
		assertSame(e, this.model.getElementAt(0));
		assertNull(this.model.getSelectedItem());
	}
	
	@Test
	public void addElementInteger() {
		final Integer e = 42;
		
		this.model.addElement(e);
		
		assertEquals(1, this.model.getSize());
		assertArrayEquals(new Integer[] { e }, modelToArray(this.model));
		assertEquals(e, this.model.getElementAt(0));
		assertSame(e, this.model.getElementAt(0));
		assertNull(this.model.getSelectedItem());
	}
	
	@Test
	public void addElementString() {
		final String e = "The answer is 42.";
		
		this.model.addElement(e);
		
		assertEquals(1, this.model.getSize());
		assertArrayEquals(new String[] { e }, modelToArray(this.model));
		assertEquals(e, this.model.getElementAt(0));
		assertSame(e, this.model.getElementAt(0));
		assertNull(this.model.getSelectedItem());
	}
	
	@Test
	public void addExcessiveElement() {
		final List<Integer> elements = new LinkedList<Integer>(); 
		
		int i = 0;
		for(; i < maxRetainedItems; i++) {
			this.model.addElement(i);
			elements.add(i);
		}
		
		Collections.reverse(elements);
		
		assertEquals(maxRetainedItems, this.model.getSize());
		assertArrayEquals(elements.toArray(), modelToArray(this.model));
		
		this.model.addElement(i);
		elements.add(0, i);
		elements.remove(maxRetainedItems);
		
		assertEquals(maxRetainedItems, this.model.getSize());
		assertArrayEquals(elements.toArray(), modelToArray(this.model));
		
		i++;
		this.model.addElement(i);
		elements.add(0, i);
		elements.remove(maxRetainedItems);
		
		assertEquals(maxRetainedItems, this.model.getSize());
		assertArrayEquals(elements.toArray(), modelToArray(this.model));
	}
	
	@Test
	public void addElements() {
		final Random randomElements = new Random();
		final List<Integer> elements = new ArrayList<Integer>(maxRetainedItems + 1);
		
		for(int i = 0; i < new Random().nextInt(maxRetainedItems) + maxRetainedItems; i++) {
			Integer e = randomElements.nextInt(1000);
			
			if(!elements.contains(e))
				elements.add(e);
			
			this.model.addElement(e);
			
			while(elements.size() > maxRetainedItems)
				elements.remove(0);
		}
			
		Collections.reverse(elements);
		
		assertEquals(maxRetainedItems, this.model.getSize());
		assertArrayEquals(elements.toArray(), modelToArray(this.model));
	}
	
	@Test
	public void addElementAt() {
		try {
			this.model.addElement(1);
			this.model.addElement(3);
			this.model.insertElementAt(0, 2);
			this.model.addElement(4);
		} catch(final UnsupportedOperationException exception) {
			assertEquals(2, this.model.getSize());
			assertArrayEquals(new Integer[] { 3, 1 }, modelToArray(this.model));
			return;
		}
		
		fail("expected UnsupportedOperationException was not thrown");
	}
	
	@Test
	public void addDuplicateElement() {
		final Integer e1 = new Integer(4);
		final Integer e2 = new Integer(4);
		
		addElementsToModel(this.modelAdapter, 1, 2, 3, e1, e2, 5);
		
		assertEquals(5, this.model.getSize());
		assertArrayEquals(new Integer[] { 5, e1, 3, 2, 1 }, modelToArray(this.model));
		assertSame(e1, this.model.getElementAt(1));
		assertNotSame(e2, this.model.getElementAt(1));
		assertEquals(e1, this.model.getElementAt(1));
		assertEquals(e2, this.model.getElementAt(1));
	}
	
	@Test
	public void removeElement() {
		final Integer e = 42;
		
		this.model.addElement(e);
		this.model.addElement(24);
		
		try {
			this.model.removeElement(e);
		} catch(final UnsupportedOperationException exception) {
			assertEquals(2, this.model.getSize());
			assertSame(e, this.model.getElementAt(1));
			assertArrayEquals(new Integer[] { 24, 42 }, modelToArray(this.model));
			return;
		}
		
		fail("expected UnsupportedOperationException was not thrown");
	}
	
	@Test
	public void removeElementAt() {
		final String e = new String("test");
		
		this.model.addElement(e);
		
		try {
			this.model.removeElementAt(0);
			this.model.addElement("another test");
		} catch(final UnsupportedOperationException exception) {
			assertEquals(1, this.model.getSize());
			assertSame(e, this.model.getElementAt(0));
			assertArrayEquals(new String[] { e }, modelToArray(this.model));
			return;
		}
		
		fail("expected UnsupportedOperationException was not thrown");
	}
	
	@Test
	public void setExistingSelectedItem() {
		final String e = "e";
		
		addElementsToModel(this.modelAdapter, "a", "b", "c", "d", "e", "f");
		
		this.model.setSelectedItem(e);
		
		assertEquals(6, this.model.getSize());
		assertArrayEquals(new String[] { e, "f", "d", "c", "b", "a" }, modelToArray(this.model));
		assertSame(e, this.model.getElementAt(0));
		assertSame(e, this.model.getSelectedItem());
		assertEquals(e, this.model.getSelectedItem());
	}
	
	@Test
	public void setExistingSelectedItemAt0() {
		final Double e = 0.3;
		
		addElementsToModel(this.modelAdapter, 0.1, 0.2, 0.3);
		
		this.model.setSelectedItem(e);
		
		assertEquals(3, this.model.getSize());
		assertArrayEquals(new Double[] { e, 0.2, 0.1 }, modelToArray(this.model));
		assertSame(e, this.model.getSelectedItem());
		assertEquals(e, this.model.getSelectedItem());	
	}
	
	@Test
	public void reselectSelectedItem() {
		final Integer e = -2;
		
		addElementsToModel(this.modelAdapter, -1, -2, -3);
		
		this.model.setSelectedItem(e);
		
		assertEquals(3, this.model.getSize());
		assertArrayEquals(new Integer[] { e, -3, -1 }, modelToArray(this.model));
		assertSame(e, this.model.getElementAt(0));
		assertSame(e, this.model.getSelectedItem());
		assertEquals(e, this.model.getSelectedItem());
		
		this.model.setSelectedItem(e);

		assertEquals(3, this.model.getSize());
		assertArrayEquals(new Integer[] { -2, -3, -1 }, modelToArray(this.model));
		assertEquals(-2, this.model.getSelectedItem());
	}
	
	@Test
	public void setNewSelectedItem() {
		final Integer e = 2;
		
		addElementsToModel(this.modelAdapter, 1, 3, 4, 5);
		
		this.model.setSelectedItem(e);
		
		assertEquals(4, this.model.getSize());
		assertArrayEquals(new Integer[] { 5, 4, 3, 1 }, modelToArray(this.model));
	}
	
	@Test 
	public void clearSelection() {
		addElementsToModel(this.modelAdapter, 10, 20, 30, 40, 50);
		
		this.model.setSelectedItem(30);
		this.model.setSelectedItem(null);
		
		assertNull(this.model.getSelectedItem());
		assertEquals(5, this.model.getSize());
		assertArrayEquals(new Integer[] { 30, 50, 40, 20, 10 }, modelToArray(this.model));
	}
	
	MutableComboBoxModel getModel() {
		return this.model;
	}

}
