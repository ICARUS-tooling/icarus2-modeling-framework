/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 */
package de.ims.icarus2.model.standard.members;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;

/**
 * @author Markus Gärtner
 *
 */
@RunWith(Parameterized.class)
public class ContainerTest {

	@SuppressWarnings("boxing")
	public static Item mockItem(long id) {
		Item item = mock(Item.class);
		when(item.getIndex()).thenReturn(id);
		when(item.getBeginOffset()).thenReturn(id);
		when(item.getEndOffset()).thenReturn(id);

		return item;
	}

	public static Container mockRoot(ContainerType containerType) {
		ItemLayerManifest layerManifest = mock(ItemLayerManifest.class);

		ContainerManifest rootManifest = mock(ContainerManifest.class);
		when(rootManifest.getLayerManifest()).thenReturn(layerManifest);

		ContainerManifest containerManifest = mock(ContainerManifest.class);
		when(containerManifest.getLayerManifest()).thenReturn(layerManifest);
		when(containerManifest.getContainerType()).thenReturn(containerType);

		when(layerManifest.getContainerManifest(0)).thenReturn(rootManifest);
		when(layerManifest.getContainerManifest(1)).thenReturn(containerManifest);

		Container root = mock(Container.class);
		when(root.getManifest()).thenReturn(rootManifest);

		return root;
	}

	@Parameters(name= "{index}: Type={0} Impl={1}") // Use implementing class as name
    public static Iterable<Object[]> data() {
    	List<Object[]> result = new ArrayList<>();

    	//

    	return result;
    }

	protected final Supplier<Container> producer;
	protected final ContainerType containerType;

	// Instantiated per test
	private Container container;
	private ContainerEditVerifier editVerifier;

	public ContainerTest(ContainerType containerType, Supplier<Container> producer) {
		this.containerType = containerType;
		this.producer = producer;
	}

	@Before
	public void prepare() {
		container = producer.get();
		editVerifier = container.createEditVerifier();
	}

	@Test
	public void testAppend() throws Exception {

		Item item = mockItem(1);

		if(editVerifier.canAddItem(0, item)) {

			container.addItem(item);

			assertEquals(1, container.getItemCount());
			assertSame(item, container.getItemAt(0));
		}
	}

	@Test(expected=NullPointerException.class)
	public void testAppendNull() throws Exception {

		Item item = mockItem(1);
		// Need to make sure we don't fail for other reasons
		if(editVerifier.canAddItem(0, item)) {
			container.addItem(null);
		}
	}
}
