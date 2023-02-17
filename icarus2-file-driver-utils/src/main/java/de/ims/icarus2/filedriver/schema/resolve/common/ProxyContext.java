/**
 * 
 */
package de.ims.icarus2.filedriver.schema.resolve.common;

import java.util.function.ObjLongConsumer;

import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;

class ProxyContext implements ResolverContext {
	private ResolverContext source;
	private CharSequence data;

	@Override
	public ItemLayer getPrimaryLayer() {
		return source.getPrimaryLayer();
	}
	@Override
	public Container currentContainer() {
		return source.currentContainer();
	}
	@Override
	public long currentIndex() {
		return source.currentIndex();
	}
	@Override
	public Item currentItem() {
		return source.currentItem();
	}
	@Override
	public CharSequence rawData() {
		return data;
	}
	public void reset(ResolverContext source, CharSequence data) {
		this.source = source;
		this.data = data;
	}
	@Override
	public ObjLongConsumer<? super Item> getTopLevelAction() {
		return source.getTopLevelAction();
	}
	@Override
	public ComponentSupplier getComponentSupplier(ItemLayer layer) {
		return source.getComponentSupplier(layer);
	}
	@Override
	public InputCache getCache(ItemLayer layer) {
		return source.getCache(layer);
	}
}