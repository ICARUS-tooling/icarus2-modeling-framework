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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.standard.members.container.DefaultContainer;
import de.ims.icarus2.model.standard.members.container.ListItemStorageInt;
import de.ims.icarus2.model.standard.members.layers.item.DefaultItemLayer;

/**
 * @author Markus Gärtner
 *
 */
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

		DefaultItemLayer layer = new DefaultItemLayer(layerManifest);
		layer.setIdManager(new IdManager.IdentityIdManager(layerManifest));

		Container root = mock(Container.class);
		when(root.getManifest()).thenReturn(rootManifest);
		when(root.getLayer()).thenReturn(layer);

		return root;
	}

    static Stream<Arguments> data() {
    	List<Arguments> data = new ArrayList<>();

    	// List container
    	DefaultContainer container1 = new DefaultContainer();
    	container1.setContainer(mockRoot(ContainerType.LIST));
    	container1.setItemStorage(new ListItemStorageInt());
    	data.add(Arguments.of(container1.getContainerType(), container1));

    	//TODO

    	return data.stream();
    }

	@ParameterizedTest(name= "run #{index}: Type={0} Impl= {1}")
	@MethodSource("data")
	public void testAppend(ContainerType type, Container container) throws Exception {

		ContainerEditVerifier editVerifier = container.createEditVerifier();

		Item item = mockItem(1);

		if(editVerifier.canAddItem(0, item)) {

			container.addItem(item);

			assertEquals(1, container.getItemCount());
			assertSame(item, container.getItemAt(0));
		}
	}

	@ParameterizedTest(name= "run #{index}: Type={0} Impl= {1}")
	@MethodSource("data")
	public void testAppendNull(ContainerType type, Container container) throws Exception {
		ContainerEditVerifier editVerifier = container.createEditVerifier();
		Item item = mockItem(1);
		// Need to make sure we don't fail for other reasons
		if(editVerifier.canAddItem(0, item)) {

			assertThrows(NullPointerException.class, () -> {
				container.addItem(null);
			});
		}
	}
}
