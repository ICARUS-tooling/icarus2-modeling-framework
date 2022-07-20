/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.standard.driver.mods;

import static de.ims.icarus2.model.manifest.util.ManifestUtils.requireId;
import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.driver.mods.DriverModule.PreloadedModule;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.Multiplicity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class ModuleManager {

	public static Builder builder() { return new Builder(); }

	private final Driver driver;
	private final ModuleMonitor fallbackMonitor;
	private final Function<ModuleManifest, ImplementationLoader<?>> loaderSource;

	/**
	 * Implementation note:
	 *
	 * All the lookup maps except 'module2Entry' are populated at constructor
	 * time, so they don't actually need any synchronization. Individual operations
	 * on Entry objects should be synchronized individually. For batch operations
	 * such as loadModules(ModuleSpec, ModuleMonotor) the global lock should be
	 * used to ensure no intervention from other batch calls.
	 *
	 * Any modifications on the 'live' map for reverse module lookups should be
	 * fully synchronized on that map object!
	 */

	/** Map all specs to sets of their manifests and instantiated modules */
	private final Map<Object, Set<Entry>> spec2Entries = new Reference2ObjectOpenHashMap<>();
	/** Key for the {@link #spec2Entries} map to collect modules that have no spec */
	private final Object NO_SPEC_KEY = "no_spec";
	/** Map individual module manifests to their instance */
	private final Map<ModuleManifest, Entry> manifest2Entry = new Reference2ObjectOpenHashMap<>();
	/** Map live modules to their manifest as backup */
	private final Map<DriverModule, Entry> module2Entry = new Reference2ObjectOpenHashMap<>();
	/** Stores all the encountered specs */
	private final List<ModuleSpec> specs = new ObjectArrayList<>();
	/** Lock for batch operations */
	private final Lock lock = new ReentrantLock();

	private ModuleManager(Builder builder) {
		builder.validate();

		fallbackMonitor = builder.getFallbackMonitor();
		driver = builder.getDriver();
		loaderSource = builder.getLoaderSource();

		ClassLoader classLoader = driver.getManifest().getManifestLocation().getClassLoader();

		// Populate all the non-live maps from manifest
		driver.getManifest().forEachModuleManifest(m -> {
			Entry e = fromManifest(m, classLoader);
			Object key = NO_SPEC_KEY;
			if(e.spec!=null) {
				specs.add(e.spec);
				key = e.spec;
			}
			spec2Entries.computeIfAbsent(key, s -> new ReferenceLinkedOpenHashSet<>()).add(e);
			manifest2Entry.put(m, e);
		});

		for(ModuleSpec spec : specs) {
			Set<Entry> entries = spec2Entries.get(spec);
			Multiplicity mult = spec.getMultiplicity();
			if(!mult.isLegalCount(entries.size()))
				throw new ModelException(ManifestErrorCode.MANIFEST_MULTIPLICITY_VIOLATION,
						String.format("Invalid number of modules registered for spec '%s': expected %d to %s - got %d",
								getName(spec), _int(mult.getRequiredMinimum()), mult.getAllowedMaximum()==UNSET_INT ?
										"<unlimited>" : _int(mult.getAllowedMaximum()), _int(entries.size())));
		}
	}

	private Entry fromManifest(ModuleManifest manifest, ClassLoader classLoader) {
		Class<?> type = resolveModuleClass(manifest.getModuleSpec().orElse(null), classLoader);
		return new Entry(manifest, type);
	}

	private @Nullable Class<?> resolveModuleClass(@Nullable ModuleSpec spec, ClassLoader classLoader) {
		if(spec==null) {
			return null;
		}

		String name = spec.getModuleClassName().orElse(null);
		if(name==null) {
			return null;
		}

		try {
			return classLoader.loadClass(name);
		} catch (ClassNotFoundException e) {
			throw new ModelException(ManifestErrorCode.IMPLEMENTATION_NOT_FOUND,
					String.format("Failed to load base class of module-spec '%s': %s", spec.getUniqueId(), name), e);
		}
	}

	/** If given monitor is null, returns the fallback monitor, otherwise uses the given one */
	private ModuleMonitor monitor(@Nullable ModuleMonitor monitor) {
		return monitor==null ? fallbackMonitor : monitor;
	}

	public ModuleManifest getManifest(DriverModule module) {
		requireNonNull(module);
		return requireEntry(module).manifest;
	}

	public void forEachModule(Consumer<? super DriverModule> action) {
		spec2Entries.values().stream()
			.flatMap(Set::stream)
			.filter(Entry::hasModule)
			.forEach(e -> action.accept(e.module));
	}

	public Optional<DriverModule> getModule(ModuleManifest manifest) {
		requireNonNull(manifest);

		return Optional.ofNullable(manifest2Entry.get(manifest)).map(Entry::getModule);
	}

	public Collection<DriverModule> getModules(ModuleSpec spec) {
		requireNonNull(spec);

		Set<Entry> entries = spec2Entries.get(spec);
		if(entries==null) {
			return Collections.emptySet();
		}
		return entries.stream()
				.map(Entry::getModule)
				.collect(Collectors.toSet());
	}

	public Optional<DriverModule> getModule(ModuleSpec spec) {
		requireNonNull(spec);

		Set<Entry> entries = spec2Entries.get(spec);
		checkState("More than 1 module registered for spec: "+spec.getUniqueId(), entries.size()<=1);

		return entries.stream().findFirst().map(Entry::getModule);
	}

	/** Must be called under synchronization on the entry object */
	private boolean prepare(Entry entry, ModuleMonitor monitor) throws InterruptedException {
		DriverModule module = entry.module;
		requireNonNull(module, "module not loaded yet: "+getName(entry.manifest));
		if(module.isReady() || module.isBusy()) {
			return false;
		}
		entry.module.prepare(entry.manifest, monitor);
		return true;
	}

	private void maybePreloadModule(Entry entry, ModuleMonitor monitor) throws InterruptedException {
		if(entry.module instanceof PreloadedModule) {
			assert !entry.module.isReady();
			prepare(entry, monitor);
		}
	}

	private void validateModule(Entry entry, DriverModule module) {
		if(entry.type!=null && !entry.type.isInstance(module))
			throw new ModelException(ManifestErrorCode.IMPLEMENTATION_INCOMPATIBLE,
					Messages.mismatch("Incompatible module implementation", entry.type, module.getClass()));
	}

	/** Must be called under synchronization on the entry object */
	private void load(Entry entry, @Nullable ModuleMonitor monitor) throws InterruptedException {
		checkArgument("module already instantiated", entry.module==null);

		ImplementationLoader<?> loader = loaderSource.apply(entry.manifest);
		requireNonNull(loader, "failed to obtain implementation loader");

		DriverModule module = loader.instantiate(DriverModule.class);

		validateModule(entry, module);

		entry.module = module;
		synchronized (module2Entry) {
			module2Entry.put(module, entry);
		}

		// Immediately notify the module about being added
		module.addNotify(driver);

		// Give specially marked modules the chance to be prepared immediately
		maybePreloadModule(entry, monitor);
	}

	public boolean loadModule(ModuleManifest manifest, @Nullable ModuleMonitor monitor) throws InterruptedException {
		requireNonNull(manifest);

		Entry entry = requireNonNull(manifest2Entry.get(manifest), "unknown manifest: "+requireId(manifest));

		synchronized (entry) {
			if(entry.module==null) {
				load(entry, monitor(monitor));

				return true;
			}
		}

		return false;
	}

	public int loadModules(ModuleSpec spec, @Nullable ModuleMonitor monitor) throws InterruptedException {
		requireNonNull(spec);

		Set<Entry> entries = spec2Entries.get(spec);
		if(entries.isEmpty()) {
			return 0;
		}

		int loaded = 0;
		monitor = monitor(monitor);

		lock.lock();
		try {
			for(Entry entry : entries) {
				synchronized (entry) {
					if(entry.module==null) {
						load(entry, monitor);
						loaded++;
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return loaded;
	}

	public <T extends DriverModule> int loadModules(Class<T> type, @Nullable ModuleMonitor monitor) throws InterruptedException {
		requireNonNull(type);

		int loaded = 0;
		monitor = monitor(monitor);

		lock.lock();
		try {
			for(Set<Entry> entries : spec2Entries.values()) {
				for(Entry entry : entries) {
					if(entry.type!=null && type.isAssignableFrom(entry.type)) {
						synchronized (entry) {
							if(entry.module==null) {
								load(entry, monitor);
								loaded++;
							}
						}
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return loaded;
	}

	public int loadAllModules(@Nullable ModuleMonitor monitor) throws InterruptedException {
		int loaded = 0;
		monitor = monitor(monitor);

		lock.lock();
		try {
			for(Set<Entry> entries : spec2Entries.values()) {
				for(Entry entry : entries) {
					synchronized (entry) {
						if(entry.module==null) {
							load(entry, monitor);
							loaded++;
						}
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return loaded;
	}

	private Entry requireEntry(DriverModule module) {
		Entry entry = module2Entry.get(module);
		if(entry==null)
			throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Unknown module: "+getName(module));
		return entry;
	}

	public boolean prepareModule(DriverModule module, @Nullable ModuleMonitor monitor) throws InterruptedException {
		requireNonNull(module);

		Entry entry = requireEntry(module);
		synchronized (entry) {
			return prepare(entry, monitor(monitor));
		}
	}

	public int prepareModules(ModuleSpec spec, @Nullable ModuleMonitor monitor) throws InterruptedException {
		requireNonNull(spec);

		Set<Entry> entries = spec2Entries.get(spec);
		if(entries.isEmpty()) {
			return 0;
		}

		int prepared = 0;
		monitor = monitor(monitor);

		lock.lock();
		try {
			for(Entry entry : entries) {
				synchronized (entry) {
					if(prepare(entry, monitor)) {
						prepared++;
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return prepared;
	}

	public int prepareAllModules(@Nullable ModuleMonitor monitor) throws InterruptedException {
		int prepared = 0;
		monitor = monitor(monitor);

		lock.lock();
		try {
			for(Set<Entry> entries : spec2Entries.values()) {
				for(Entry entry : entries) {
					synchronized (entry) {
						if(prepare(entry, monitor)) {
							prepared++;
						}
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return prepared;
	}

	/** Must be called under synchronization on the entry object */
	private boolean reset(Entry entry, ModuleMonitor monitor) throws InterruptedException {
		DriverModule module = entry.module;
		if (module.isBusy() || !module.isReady()) {
			return false;
		}
		entry.module.reset(monitor);
		return true;
	}

	public boolean resetModule(DriverModule module, @Nullable ModuleMonitor monitor) throws InterruptedException {
		requireNonNull(module);

		Entry entry = requireEntry(module);
		synchronized (entry) {
			return reset(entry, monitor(monitor));
		}
	}

	public int resetAllModules(@Nullable ModuleMonitor monitor) throws InterruptedException {
		int resetted = 0;
		monitor = monitor(monitor);

		lock.lock();
		try {
			for(Set<Entry> entries : spec2Entries.values()) {
				for(Entry entry : entries) {
					synchronized (entry) {
						if(reset(entry, monitor)) {
							resetted++;
						}
					}
				}
			}
		} finally {
			lock.unlock();
		}

		return resetted;
	}

	/** Encapsulates all the info for a single module */
	private static class Entry {
		final ModuleManifest manifest;
		final ModuleSpec spec;
		final Class<?> type;

		DriverModule module;

		public Entry(ModuleManifest manifest, Class<?> type) {
			this.manifest = requireNonNull(manifest);
			this.spec = manifest.getModuleSpec().orElse(null);
			this.type = type;
		}

		public ModuleManifest getManifest() { return manifest; }

		public ModuleSpec getSpec() { return spec; }

		public DriverModule getModule() { return module; }

		public void setModule(DriverModule module) {
			//TODO do we want verification here against redundant assignment?
			this.module = module;
		}

		public boolean hasModule() { return module!=null; }
	}

	public static class Builder extends AbstractBuilder<Builder, ModuleManager> {

		private Driver driver;
		private ModuleMonitor fallbackMonitor;
		private Function<ModuleManifest, ImplementationLoader<?>> loaderSource;

		private Builder() { /* no-op */ }

		public Builder driver(Driver driver) {
			requireNonNull(driver);
			checkState("driver already set", this.driver==null);

			this.driver = driver;

			 return this;
		}

		public Builder fallbackMonitor(ModuleMonitor fallbackMonitor) {
			requireNonNull(driver);
			checkState("fallback monitor already set", this.fallbackMonitor==null);

			this.fallbackMonitor = fallbackMonitor;

			return this;
		}

		public Builder loaderSource(Function<ModuleManifest, ImplementationLoader<?>> loaderSource) {
			requireNonNull(driver);
			checkState("loader source already set", this.loaderSource==null);

			this.loaderSource = loaderSource;

			return this;
		}

		public Driver getDriver() { return driver; }

		public ModuleMonitor getFallbackMonitor() { return fallbackMonitor; }

		public Function<ModuleManifest, ImplementationLoader<?>> getLoaderSource() { return loaderSource; }



		@Override
		protected void validate() {
			super.validate();

			checkState("no driver set", driver!=null);
			checkState("no loader source set", loaderSource!=null);
		}

		@Override
		protected ModuleManager create() { return new ModuleManager(this); }

	}
}
