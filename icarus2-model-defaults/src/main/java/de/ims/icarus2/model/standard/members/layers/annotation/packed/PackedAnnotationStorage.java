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
package de.ims.icarus2.model.standard.members.layers.annotation.packed;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackedDataManager.PackageHandle;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Part;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class PackedAnnotationStorage implements ManagedAnnotationStorage {

	/**
	 * The (potentially shared) storage for our annotations.
	 */
	private final Supplier<? extends PackedDataManager<Item,AnnotationStorage>> dataManager;

	private Map<String, PackageHandle> handles = new Object2ObjectOpenHashMap<>();

	public PackedAnnotationStorage(Supplier<? extends PackedDataManager<Item,AnnotationStorage>> dataManager) {
		this.dataManager = requireNonNull(dataManager);
	}

	private PackedDataManager<Item,AnnotationStorage> dataManager() {
		PackedDataManager<Item,AnnotationStorage> dataManager = this.dataManager.get();
		checkState("Manager for packed data missing", dataManager!=null);
		return dataManager;
	}

	/**
	 * Fetch handle for given key and throw {@link GlobalErrorCode#INVALID_INPUT}
	 * if no handle available.
	 */
	private PackageHandle handleForKey(String key) {
		requireNonNull(key);
		PackageHandle handle = handles.get(key);
		if(handle == null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Unable to obtain package handle - unknown annotation key: "+key);
		return handle;
	}

	/**
	 * Registers with the underlying {@link PackedDataManager} via
	 * the {@link Part#addNotify(Object)} method and then fills
	 * the internal lookup for {@link PackageHandle} object from
	 * the {@link PackedDataManager#lookupHandles(Set) mapping} in
	 * the manager.
	 *
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#addNotify(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public void addNotify(AnnotationLayer layer) {

		// Register with manager
		PackedDataManager<Item,AnnotationStorage> dataManager = dataManager();
		dataManager.addNotify(this);

		// Fetch handles
		Set<AnnotationManifest> manifests = layer.getManifest().getAnnotationManifests();
		Map<AnnotationManifest, PackageHandle> lookup = dataManager.lookupHandles(manifests);

		for(AnnotationManifest manifest : manifests) {
			PackageHandle handle = lookup.get(manifest);
			if(handle==null)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Missing handle for annotation: "+ModelUtils.getName(manifest));
			handles.put(manifest.getKey(), handle);
		}
	}

	/**
	 * Unregisters from the underlying {@link PackedDataManager} via
	 * the {@link Part#removeNotify(Object)} method and resets the
	 * internal lookup for {@link PackageHandle} objects.
	 *
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#removeNotify(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public void removeNotify(AnnotationLayer layer) {
		dataManager().removeNotify(this);
		handles.clear();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)
	 */
	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		// Just forwards the request with a customized action that translates handles into annotation keys
		return dataManager().collectHandles(item, handles.values(),
				handle -> action.accept(handle.getManifest().getKey()));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		return dataManager().getValue(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getIntegerValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public int getIntegerValue(Item item, String key) {
		return dataManager().getInteger(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getFloatValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public float getFloatValue(Item item, String key) {
		return dataManager().getFloat(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getDoubleValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public double getDoubleValue(Item item, String key) {
		return dataManager().getDouble(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getLongValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public long getLongValue(Item item, String key) {
		return dataManager().getLong(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getBooleanValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)
	 */
	@Override
	public boolean getBooleanValue(Item item, String key) {
		return dataManager().getBoolean(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)
	 */
	@Override
	public void removeAllValues(Supplier<? extends Item> source) {
		throw new ModelException(GlobalErrorCode.DEPRECATED, "Can't clear buffered annotation storage");
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		dataManager().setValue(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setIntegerValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)
	 */
	@Override
	public void setIntegerValue(Item item, String key, int value) {
		dataManager().setInteger(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setLongValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)
	 */
	@Override
	public void setLongValue(Item item, String key, long value) {
		dataManager().setLong(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setFloatValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)
	 */
	@Override
	public void setFloatValue(Item item, String key, float value) {
		dataManager().setFloat(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setDoubleValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)
	 */
	@Override
	public void setDoubleValue(Item item, String key, double value) {
		dataManager().setDouble(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setBooleanValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)
	 */
	@Override
	public void setBooleanValue(Item item, String key, boolean value) {
		dataManager().setBoolean(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()
	 */
	@Override
	public boolean hasAnnotations() {
		return dataManager().hasValues();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	public boolean hasAnnotations(Item item) {
		return dataManager().hasValues(item);
	}

	@Override
	public boolean containsItem(Item item) {
		return dataManager().isRegistered(item);
	}

	@Override
	public boolean addItem(Item item) {
		return dataManager().register(item);
	}

	@Override
	public boolean removeItem(Item item) {
		return dataManager().unregister(item);
	}

}
