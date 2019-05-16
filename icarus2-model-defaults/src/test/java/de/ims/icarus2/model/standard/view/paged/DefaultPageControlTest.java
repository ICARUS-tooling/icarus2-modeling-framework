/**
 *
 */
package de.ims.icarus2.model.standard.view.paged;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.paged.PageControlTest;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.standard.driver.virtual.VirtualItemLayerManager;

/**
 * @author Markus Gärtner
 *
 */
class DefaultPageControlTest implements PageControlTest<DefaultPageControl> {

	@SuppressWarnings("boxing")
	@Override
	public PagedCorpusView createView(boolean active) {
		ItemLayerManifest layerManifest = mock(ItemLayerManifest.class);
		when(layerManifest.getUID()).thenReturn(Integer.valueOf(1));

		ItemLayer primaryLayer = mock(ItemLayer.class);
		when((ItemLayerManifest)primaryLayer.getManifest()).thenReturn(layerManifest);

		Scope scope = mock(Scope.class);
		when(scope.getPrimaryLayer()).thenReturn(primaryLayer);

		PagedCorpusView view = mock(PagedCorpusView.class);
		when(view.getScope()).thenReturn(scope);
		when(view.isActive()).thenReturn(Boolean.valueOf(active));

		return view;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PageControlTest#createFilled(int, de.ims.icarus2.model.api.driver.indices.IndexSet[])
	 */
	@Override
	public DefaultPageControl createFilled(PagedCorpusView view, int pageSize, IndexSet... indices) {
		long size = IndexUtils.max(indices);

		assumeTrue(size<100_000, "Requested size would cause too many mocked items to be created: "+size);

		ItemLayer layer = view.getScope().getPrimaryLayer();

		VirtualItemLayerManager itemLayerManager = new VirtualItemLayerManager();
		itemLayerManager.addLayer(layer);

		for (int i = 0; i < size+1; i++) {
			itemLayerManager.addItem(layer, mockItem());
		}

		DefaultPageControl control = DefaultPageControl.newBuilder()
				.pageSize(pageSize)
				.indices(indices)
				.itemLayerManager(itemLayerManager)
				.build();

		control.addNotify(view);

		return control;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.paged.PageControlTest#createUnadded()
	 */
	@Override
	public DefaultPageControl createUnadded() {
		return DefaultPageControl.newBuilder()
				.pageSize(DEFAULT_PAGE_SIZE)
				.indices(DEFAULT_INDICES)
				.itemLayerManager(mock(ItemLayerManager.class))
				.build();
	}
}
