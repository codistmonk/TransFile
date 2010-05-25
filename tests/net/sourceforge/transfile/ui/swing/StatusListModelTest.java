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
import static net.sourceforge.transfile.tools.Tools.*;
import static net.sourceforge.transfile.tools.UnitTestingTools.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.sourceforge.transfile.tools.MutableModelAdapter;
import net.sourceforge.transfile.ui.swing.StatusList.StatusListModel;
import net.sourceforge.transfile.ui.swing.StatusService.StatusMessage;

import org.junit.*;

/**
 * <p>Tests net.sourceforge.transfile.ui.swing.StatusList.StatusListModel.</p>
 * 
 * <p>Does not cover locale changes / autotranslation or the firing of events at this time.</p>
 *
 * @author Martin Riedel
 *
 */
public class StatusListModelTest {
	
	private final static int minRows = 5;
	
	private StatusListModel model;
	
	private final MutableModelAdapter modelAdapter = new MutableModelAdapter() {
		
		@Override
		public <T> void addElement(T newElement) {
			if(!(newElement instanceof StatusMessage))
				throw new Error("StatusListModel exclusively stores StatusMessages");
			
			StatusListModelTest.this.getModel().addMessage((StatusMessage) newElement);
		}
		
	};

	
	@Before
	public void setup() {
		this.model = new StatusListModel(minRows);
	}
	
	@After
	public void cleanup() {
		this.model = null;
	}
	
	@Test
	public void equalDummies() {
		final StatusList.StatusDummy a = new StatusList.StatusDummy();
		final StatusList.StatusDummy b = new StatusList.StatusDummy();
		
		assertEquals(a, b);
		assertEquals(b, a);
		assertEquals(a.hashCode(), b.hashCode());
	}
	
	@Test
	public void dummyVsMessage() {
		final StatusList.StatusDummy dummy = new StatusList.StatusDummy();
		final StatusMessage message = new StatusMessage(StatusList.StatusDummy.DUMMY_TEXT);
		
		assertFalse(dummy.equals(message));
		assertFalse(message.equals(dummy));
		assertFalse(dummy.hashCode() == message.hashCode());
	}
	
	@Test
	public void emptyOnInitialization() {
		assertEquals(minRows, this.model.getSize());
		assertArrayEquals(makeDummyArray(minRows), modelToArray(this.model));
	}
	
	@Test
	public void addMessage() {
		final StatusMessage e = new StatusMessage("test");
		
		this.model.addMessage(e);
		
		assertEquals(minRows, this.model.getSize());
		assertArrayEquals(arrayConcat(e, makeDummyArray(minRows - 1)), modelToArray(this.model));
		assertSame(e, this.model.getElementAt(0));
	}
	
	@Test
	public void addDuplicateMessage() {
		final StatusMessage e = new StatusMessage("The Answer is 42");
		
		addElementsToModel(this.modelAdapter, e, e);
		
		assertEquals(Math.max(minRows, 2), this.model.getSize());
		assertArrayEquals(arrayConcat(new StatusMessage[] { e, e }, makeDummyArray(minRows - 2)), modelToArray(this.model));
		assertSame(e, this.model.getElementAt(0));
		assertSame(e, this.model.getElementAt(1));
	}
	
	@Test
	public void addMessages() {
		final List<StatusMessage> elements = new LinkedList<StatusMessage>();
		final Random randomElements = new Random();
		
		for(int i = 0; i <= minRows; i++) {
			final StatusMessage e = new StatusMessage(Integer.toString(randomElements.nextInt())); 
			elements.add(e);
			this.model.addMessage(e);
		}
		
		Collections.reverse(elements);
		
		assertEquals(elements.size(), this.model.getSize());
		assertArrayEquals(elements.toArray(), modelToArray(this.model));
	}
		
	StatusListModel getModel() {
		return this.model;
	}
	
	/**
	 * Creates an array of {@link StatusList.StatusDummy}s.
	 * 
	 * @param numDummies
	 * <br />The number of dummies to put in the array
	 * <br />May be any integer (see below)
	 * <br />Should not be null
	 * @return
	 * An empty array of {@code StatusDummy}s if {@code numDummies} is smaller than 1<br />
	 * Otherwise an array of {@code numDummies} {@code StatusDummy}ss
	 * 
	 */
	private static StatusList.StatusDummy[] makeDummyArray(final int numDummies) {
		if(numDummies < 1)
			return new StatusList.StatusDummy[] { };
		
		StatusList.StatusDummy[] dummies = new StatusList.StatusDummy[numDummies];
		
		for(int i = 0; i < dummies.length; i++)
			dummies[i] = new StatusList.StatusDummy();
		
		return dummies;
	}
	
}
