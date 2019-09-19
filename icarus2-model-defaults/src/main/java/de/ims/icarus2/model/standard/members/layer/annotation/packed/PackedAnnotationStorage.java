/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.mem.ByteAllocator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationStorage.class)
public class PackedAnnotationStorage extends AbstractPart<AnnotationLayer>
		implements ManagedAnnotationStorage {

	/**
	 * The (potentially shared) storage for our annotations.
	 */
	private final Supplier<? extends PackedDataManager<Item,AnnotationStorage>> dataManager;

	private Map<String, PackageHandle> handles = new Object2ObjectOpenHashMap<>();

	public PackedAnnotationStorage(Supplier<? extends PackedDataManager<Item,AnnotationStorage>> dataManager) {
		this.dataManager = requireNonNull(dataManager);
	}

	/** Package private for testing purposes */
	PackedDataManager<Item,AnnotationStorage> dataManager() {
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
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"Unable to obtain package handle - unknown annotation key: "+key);
		return handle;
	}

	/**
	 * Registers with the underlying {@link PackedDataManager} via
	 * the {@link Part#addNotify(Object)} method and then fills
	 * the internal lookup for {@link PackageHandle} object from
	 * the {@link PackedDataManager#lookupHandles(Set) mapping} in
	 * the manager.
	 *
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#addNotify(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		// Register with manager
		PackedDataManager<Item,AnnotationStorage> dataManager = dataManager();
		dataManager.addNotify(this);

		// Fetch handles
		Set<AnnotationManifest> manifests = layer.getManifest().getAnnotationManifests();
		Map<AnnotationManifest, PackageHandle> lookup = dataManager.lookupHandles(manifests);

		for(AnnotationManifest manifest : manifests) {
			String key = manifest.getKey().orElseThrow(
					ModelException.create(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Missing key declaration"));
			PackageHandle handle = lookup.get(manifest);
			if(handle==null)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
						"Missing handle for annotation: "+ModelUtils.getName(manifest));
			handles.put(key, handle);
		}
	}

	/**
	 * Unregisters from the underlying {@link PackedDataManager} via
	 * the {@link Part#removeNotify(Object)} method and resets the
	 * internal lookup for {@link PackageHandle} objects.
	 *
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#removeNotify(de.ims.icarus2.model.api.layer.AnnotationLayer)
	 */
	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		dataManager().removeNotify(this);
		handles.clear();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)
	 */
	@Override
	public boolean collectKeys(Item item, @Nullable Consumer<String> action) {
		// Just forwards the request with a customized action that translates handles into annotation keys
		Consumer<PackageHandle> wrapper = action ==null ? null :
			handle -> action.accept(((AnnotationManifest)handle.getSource()).getKey().get());
		return dataManager().collectUsedHandles(item, handles.values(), wrapper);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public Object getValue(Item item, String key) {
		requireNonNull(item);
		return dataManager().getValue(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getInteger(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public int getInteger(Item item, String key) {
		requireNonNull(item);
		return dataManager().getInteger(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getFloat(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public float getFloat(Item item, String key) {
		requireNonNull(item);
		return dataManager().getFloat(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getDouble(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public double getDouble(Item item, String key) {
		requireNonNull(item);
		return dataManager().getDouble(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getLong(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public long getLong(Item item, String key) {
		requireNonNull(item);
		return dataManager().getLong(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getBoolean(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int)
	 */
	@Override
	public boolean getBoolean(Item item, String key) {
		requireNonNull(item);
		return dataManager().getBoolean(item, handleForKey(key));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)
	 */
	@Override
	public void removeAllValues(Supplier<? extends Item> source) {
		PackageHandle[] handles = this.handles.values().toArray(new PackageHandle[0]);
		dataManager().clear(source, handles);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, @Nullable Object value) {
		requireNonNull(item);
		dataManager().setValue(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setInteger(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, int)
	 */
	@Override
	public void setInteger(Item item, String key, int value) {
		requireNonNull(item);
		dataManager().setInteger(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setLong(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, long)
	 */
	@Override
	public void setLong(Item item, String key, long value) {
		requireNonNull(item);
		dataManager().setLong(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setFloat(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, float)
	 */
	@Override
	public void setFloat(Item item, String key, float value) {
		requireNonNull(item);
		dataManager().setFloat(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setDouble(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, double)
	 */
	@Override
	public void setDouble(Item item, String key, double value) {
		requireNonNull(item);
		dataManager().setDouble(item, handleForKey(key), value);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setBoolean(de.ims.icarus2.model.api.members.item.Item, ByteAllocator, int, boolean)
	 */
	@Override
	public void setBoolean(Item item, String key, boolean value) {
		requireNonNull(item);
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
		return dataManager().collectUsedHandles(item, handles.values(), null);
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
