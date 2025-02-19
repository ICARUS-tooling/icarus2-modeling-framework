/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.driver.virtual;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.ChunkInfo;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.DriverListener;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.standard.driver.AbstractDriver;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.TestableImplementation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Wraps an {@link ItemLayerManager} implementation and decorates it with the additional
 * features of a {@link Driver}.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(Driver.class)
public class VirtualDriver extends AbstractDriver {

	public static Builder builder() {
		return new Builder();
	}

	private ItemLayerManager itemLayerManager;

	private final Int2ObjectMap<ItemLayer> idMap = new Int2ObjectOpenHashMap<>();

	private final Collection<DriverModule> modules = new ArrayList<>();

	private transient Builder builder;

	/**
	 * @param manifest
	 * @param corpus
	 * @throws ModelException
	 */
	protected VirtualDriver(Builder builder)
			throws ModelException {
		super(builder);

		modules.addAll(builder.getModules());

		this.builder = builder;
	}

	/**
	 * Creates a new {@link ItemLayerManager} for this driver by trying to use
	 * the following optional settings from the {@link Builder}
	 * supplied at constructor time (in the given order):
	 * <ol>
	 * <li>If a finished {@link ItemLayerManager} instance has been supplied, it will be used</li>
	 * <li>If a {@link Class} implementing the {@code ItemLayerManager} interface has been specified,
	 * it will be instantiated using the default no-args constructor. Note that this effectively makes
	 * the manager ignore any other settings on the builder!</li>
	 * <li>If a {@link Supplier} has been specified, it will be used to {@link Supplier#get() obtain} a single {@link ItemLayerManager}
	 * instance. Note that in this case builder settings can only be applied if the supplier argument is using
	 * an external reference to the builder!</li>
	 * <li>If a {@link Function} was provided that takes a {@link Driver} instance and returns an {@link ItemLayerManager}
	 * it will be called will be {@link Function#apply(Object) called} with this driver as argument. This is one of the only ways to
	 * have <i>some</i> access to settings of the original builder that have already been applied to this driver instance.</li>
	 * <li>If all the above attempts fail then the protected method {@link #defaultCreateItemLayerManager()} is called which allows
	 * the driver implementation to decide on the actual instantiation of a new manager.</li>
	 * </ol>
	 *
	 * @return
	 */
	protected ItemLayerManager createItemLayerManager() {

		// Try finished item layer manager
		final ItemLayerManager itemLayerManager = builder.getItemLayerManager();
		if(itemLayerManager!=null) {
			return itemLayerManager;
		}

		// Try explicit class definition
		Class<? extends ItemLayerManager> managerClass = builder.getManagerClass();
		if(managerClass!=null) {
			try {
				return managerClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ModelException(GlobalErrorCode.DELEGATION_FAILED,
						"Failed to instantiate item layer manager from class", e);
			}
		}

		// Try supplier
		Supplier<ItemLayerManager> supplier = builder.getSupplier();
		if(supplier!=null) {
			return supplier.get();
		}

		// Try Creator
		final Function<Driver, ItemLayerManager> creator = builder.getCreator();
		if(creator!=null) {
			return creator.apply(VirtualDriver.this);
		}

		return defaultCreateItemLayerManager();
	}

	/**
	 * Fallback method in the process of creating a new {@link ItemLayerManager} for this driver.
	 * This method is called only from within {@link #createItemLayerManager()} and then only in
	 * case all other ways of creating the manager have failed. For those alternatives check the
	 * setter methods in the {@link Builder} class.
	 *
	 * @see Builder
	 *
	 * @return
	 */
	protected ItemLayerManager defaultCreateItemLayerManager() {
		VirtualItemLayerManager itemLayerManager = new VirtualItemLayerManager();

		//FIXME change so that layers get copied over lazily
		itemLayerManager.addLayers(getContext());

		return itemLayerManager;
	}

	@Override
	public void forEachModule(Consumer<? super DriverModule> action) {
		modules.forEach(action);
	}

	public ItemLayerManager getItemLayerManager() {
		checkConnected();

		return itemLayerManager;
	}

	public ItemLayer getLayerForManifest(ItemLayerManifestBase<?> manifest) {
		ensureLayerMap();

		return idMap.get(keyForManifest(manifest));
	}

	protected int keyForManifest(LayerManifest<?> manifest) {
		return manifest.getUID();
	}

	protected final void ensureLayerMap() {
		if(idMap.isEmpty()) {
			for(Layer layer : getItemLayerManager().getItemLayers()) {
				idMap.put(keyForManifest(layer.getManifest()), (ItemLayer)layer);
			}
		}
	}

	protected final void registerLayer(ItemLayer layer) {
		int id = keyForManifest(layer.getManifest());
		if(idMap.containsKey(id))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Layer already registered: "+getName(layer));
		idMap.put(id, layer);
	}

	protected final ItemLayer getLayerForId(int id) {
		ensureLayerMap();

		return idMap.get(id);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItemCount(de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public long getItemCount(ItemLayer layer) {
		return getItemLayerManager().getItemCount(layer);
	}

	/**
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#getItem(de.ims.icarus2.model.api.layer.ItemLayer, long)
	 */
	@Override
	public Item getItem(ItemLayer layer, long index) {
		return getItemLayerManager().getItem(layer, index);
	}

	/**
	 * @throws IcarusApiException
	 * @see de.ims.icarus2.model.api.members.item.manager.ItemLayerManager#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	public void release(IndexSet[] indices, ItemLayer layer)
			throws InterruptedException, IcarusApiException {
		getItemLayerManager().release(indices, layer);
	}

	/**
	 * First creates the {@link ItemLayerManager} instance to be used for this driver
	 * and then {@link DriverModule#addNotify(Driver) notifies} all modules
	 * @throws IcarusApiException
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doConnect()
	 */
	@Override
	protected void doConnect() throws InterruptedException, IcarusApiException {

		super.doConnect();

		setItemLayerManager(createItemLayerManager());

		forEachModule(m -> m.addNotify(this));
	}

	protected final void setItemLayerManager(ItemLayerManager itemLayerManager) {
		requireNonNull(itemLayerManager);
		checkState(this.itemLayerManager==null);

		this.itemLayerManager = itemLayerManager;
	}

	@Override
	protected void verifyInternals() {
		super.verifyInternals();

		checkState("Missing item layer manager", itemLayerManager!=null);
	}

	/**
	 * First {@link DriverModule#removeNotify(Driver) notifies} all modules and
	 * then deletes the {@link ItemLayerManager} of this driver.
	 * Note that in case subclasses wish to perform maintenance work on the
	 * item layer manager while disconnecting, they should do it <b>prior</b> to
	 * calling this super method!
	 * @throws IcarusApiException
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#doDisconnect()
	 */
	@Override
	protected void doDisconnect() throws InterruptedException, IcarusApiException {

		for(DriverModule module : modules) {
			module.removeNotify(this);
		}

		itemLayerManager = null;

		super.doDisconnect();
	}

	@Override
	public long getItemCount(ItemLayerManifestBase<?> layer) {
		return (isReady() && isConnected()) ? getItemCount(getLayerForManifest(layer)) : IcarusUtils.UNSET_LONG;
	}

	/**
	 * The default implementation delegates to the {@link ItemLayerManager} that was specified
	 * at construction time. In addition it ensures that all registered
	 * {@link DriverListener driver listeners} are notified in addition to the given {@code action}
	 * argument (note however, that the {@code action} callback will always be called prior to
	 * the listener notification).
	 * <p>
	 * If subclasses decide to perform actual data loading in this method, they should either make sure
	 * to call registered listeners themselves or simply delegate to the super method after the
	 * actual loading of data chunks.
	 * @throws IcarusApiException
	 *
	 * @see de.ims.icarus2.model.standard.driver.AbstractDriver#loadPrimaryLayer(de.ims.icarus2.model.api.driver.indices.IndexSet[], de.ims.icarus2.model.api.layer.ItemLayer, java.util.function.Consumer)
	 * @see ItemLayerManager#load(IndexSet[], ItemLayer, Consumer)
	 */
	@Override
	public long load(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException, IcarusApiException {
		requireNonNull(indices);
		requireNonNull(layer);

		// Unlike the ItemLayerManager a driver also has independent listeners to satisfy
		Consumer<ChunkInfo> compoundAction = c -> {
			fireChunksLoaded(layer, c);
		};

		if(action!=null) {
			// Priority: the supplied action should always come before registered listeners
			compoundAction = action.andThen(compoundAction);
		}

		// Delegate to wrapped ItemLayerManager for actual loading
		return getItemLayerManager().load(indices, layer, compoundAction);
	}

	@Api(type=ApiType.BUILDER) //TODO add builder test annotations
	public static class Builder extends DriverBuilder<Builder, VirtualDriver> {

		private ItemLayerManager itemLayerManager;
		private Supplier<ItemLayerManager> supplier;
		private Function<Driver, ItemLayerManager> creator;
		private Class<? extends ItemLayerManager> managerClass;

		private final Collection<DriverModule> modules = new ArrayList<>();

		protected Builder() {
			// no-op
		}

		public Builder itemLayerManager(ItemLayerManager itemLayerManager) {
			requireNonNull(itemLayerManager);
			checkState(this.itemLayerManager==null);
			checkState(this.supplier==null);
			checkState(this.creator==null);
			checkState(this.managerClass==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		public ItemLayerManager getItemLayerManager() {
			return itemLayerManager;
		}

		public Builder supplier(Supplier<ItemLayerManager> supplier) {
			requireNonNull(supplier);
			checkState(this.supplier==null);
			checkState(this.itemLayerManager==null);
			checkState(this.creator==null);
			checkState(this.managerClass==null);

			this.supplier = supplier;

			return thisAsCast();
		}

		public Supplier<ItemLayerManager> getSupplier() {
			return supplier;
		}

		public Builder creator(Function<Driver, ItemLayerManager> creator) {
			requireNonNull(creator);
			checkState(this.creator==null);
			checkState(this.itemLayerManager==null);
			checkState(this.supplier==null);
			checkState(this.managerClass==null);

			this.creator = creator;

			return thisAsCast();
		}

		public Function<Driver, ItemLayerManager> getCreator() {
			return creator;
		}

		public Builder managerClass(Class<? extends ItemLayerManager> managerClass) {
			requireNonNull(managerClass);
			checkState(this.managerClass==null);
			checkState(this.creator==null);
			checkState(this.itemLayerManager==null);
			checkState(this.supplier==null);

			this.managerClass = managerClass;

			return thisAsCast();
		}

		public Class<? extends ItemLayerManager> getManagerClass() {
			return managerClass;
		}

		public void module(DriverModule module) {
			requireNonNull(module);
			checkState(!modules.contains(module));

			modules.add(module);
		}

		public void modules(Collection<? extends DriverModule> modules) {
			requireNonNull(modules);
			checkArgument(!modules.isEmpty());

			this.modules.addAll(modules);
		}

		public Collection<DriverModule> getModules() {
			return modules;
		}

		@Override
		public VirtualDriver create() {
			return new VirtualDriver(this);
		}
	}
}
