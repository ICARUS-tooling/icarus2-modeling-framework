/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import de.ims.icarus2.model.manifest.api.Hierarchy;
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

		Hierarchy<ContainerManifest> hierarchy = mock(Hierarchy.class);
		when(hierarchy.atLevel(0)).thenReturn(rootManifest);
		when(hierarchy.atLevel(1)).thenReturn(containerManifest);

		when(layerManifest.getContainerHierarchy()).thenReturn(hierarchy);

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
