/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision: 335 $
 * $Date: 2015-01-21 11:34:39 +0100 (Mi, 21 Jan 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/member/LookupListTest.java $
 *
 * $LastChangedDate: 2015-01-21 11:34:39 +0100 (Mi, 21 Jan 2015) $
 * $LastChangedRevision: 335 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Markus Gärtner
 * @version $Id: LookupListTest.java 335 2015-01-21 10:34:39Z mcgaerty $
 *
 */
public class LookupListTest {

	private static Object dummy(int id) {
		return "Dummy"+id; //$NON-NLS-1$
	}

	private static final Object dummy1 = dummy(1);
	private static final Object dummy2 = dummy(2);
	private static final Object dummy3 = dummy(3);

	private LookupList<Object> list;

	@Before
	public void prepare() {
		list = new LookupList<>();
	}

	@Test
	public void testInit() throws Exception {
		assertEquals(list.size(), 0);

		list = new LookupList<>(100);

		assertEquals(list.size(), 0);
	}

	@Test
	public void testBasic() throws Exception {
		// Test size

		list.add(dummy1);
		assertEquals("Size after adding 1 item must be 1", list.size(), 1); //$NON-NLS-1$

		list.add(dummy2);
		assertEquals("Size after adding 2 items must be s", list.size(), 2); //$NON-NLS-1$

		// Test indexOf()

		assertEquals("Index of dummy1 must be 0", list.indexOf(dummy1), 0); //$NON-NLS-1$
		assertEquals("Index of dummy2 must be 1", list.indexOf(dummy2), 1); //$NON-NLS-1$
		assertEquals("Index of dummy3 must be -1", list.indexOf(dummy3), -1); //$NON-NLS-1$

		// Test contains()

		assertTrue("List must contain dummy1", list.contains(dummy1)); //$NON-NLS-1$
		assertTrue("List must contain dummy2", list.contains(dummy2)); //$NON-NLS-1$
		assertTrue("List must not contain dummy3", !list.contains(dummy3)); //$NON-NLS-1$

		// Test insert

		list.add(1, dummy3);

		assertEquals("Index of dummy3 must be 1", list.indexOf(dummy3), 1); //$NON-NLS-1$
		assertEquals("Index of dummy2 must be 2", list.indexOf(dummy2), 2); //$NON-NLS-1$
		assertTrue("List must contain dummy3", list.contains(dummy3)); //$NON-NLS-1$

		// Test clear
		list = new LookupList<>();
		list.add(dummy1);
		list.add(dummy2);
		list.add(dummy3);

		assertEquals(list.size(), 3);
		list.clear();
		assertTrue("List must be empty after clearing", list.isEmpty()); //$NON-NLS-1$

		// Test remove

		list = new LookupList<>();
		list.add(dummy1);

		list.remove(dummy1);
		assertFalse("List must not contain dummy1 after removal", list.contains(dummy1)); //$NON-NLS-1$

		list.add(dummy2);
		list.remove(0);
		assertFalse("List must not contain dummy2 after removal", list.contains(dummy2)); //$NON-NLS-1$
	}

	@Test
	public void testIterator() throws Exception {
		list.add(dummy1);
		list.add(dummy2);
		assertEquals(list.size(), 2);

		Iterator<Object> it = list.iterator();
		assertNotNull(it);
		assertTrue(it.hasNext());

		Object item = it.next();
		assertEquals(item, dummy1);
		assertTrue(it.hasNext());

		item = it.next();
		assertEquals(item, dummy2);
		assertFalse(it.hasNext());
	}

	@Test
	public void testLookup() throws Exception {
		Object[] items = new Object[20];
		for(int i=0; i<items.length; i++) {
			items[i] = dummy(i+1);
			list.add(items[i]);
		}

		assertEquals(list.size(), items.length);

		for(int i=0; i<items.length; i++) {
			assertTrue(list.contains(items[i]));
			assertEquals(i, list.indexOf(items[i]));
		}
	}
}
