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
/**
 *
 */
package de.ims.icarus2.model.manifest.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.HierarchyImpl;
import de.ims.icarus2.model.manifest.standard.ImplementationManifestImpl;
import de.ims.icarus2.util.Options;

/**
 * Utility class that wraps around a {@link ManifestFactory} and provides
 * lookup-based construction methods to build fully linked hierarchies of
 * manifest objects.
 *
 * @author Markus Gärtner
 *
 */
public class ManifestBuilder implements AutoCloseable {

	private final ManifestFactory factory;

	private final Map<String, ManifestFragment> lookup;

	private TypedManifest lastManifest;

	public ManifestBuilder(ManifestFactory factory) {
		this.factory = requireNonNull(factory);
		lookup = new HashMap<>();
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		lookup.clear();
		lastManifest = null;
	}

	/**
	 * Creates an anonymous manifest without storing it.
	 *
	 * @param clazz
	 * @return
	 *
	 * @see ManifestFactory#create(Class)
	 */
	public <M extends TypedManifest> M create(Class<M> clazz) {
		return create(clazz, Options.NONE);
	}

	/**
	 * Creates an anonymous manifest without storing it.
	 *
	 * @param clazz
	 * @return
	 *
	 * @see ManifestFactory#create(Class)
	 */
	public <M extends TypedManifest> M create(Class<M> clazz, Options options) {
		requireNonNull(clazz);
		return markLast(factory.create(clazz, options));
	}

	private void assignId(TypedManifest manifest, String id) {
		if(manifest instanceof ModifiableIdentity) {
			((ModifiableIdentity)manifest).setId(id);
		} else
			throw new InternalError("Class needs to be handled: "+manifest.getClass());
	}

	private <M extends TypedManifest> M markLast(M manifest) {
		lastManifest = manifest;
		return manifest;
	}

	/**
	 * Creates a manifest with an explicit {@code id} and stores it for further
	 * lookups.
	 * The manifest will not be assigned a host.
	 *
	 * @param clazz
	 * @param id
	 * @throws IllegalStateException iff the given {@code id} has already been used
	 * for another manifest.
	 * @return
	 */
	public <M extends TypedManifest> M create(Class<M> clazz, String id) {
		return create(clazz, id, Options.NONE);
	}

	/**
	 * Creates a manifest with an explicit {@code id} and stores it for further
	 * lookups.
	 * The manifest will not be assigned a host.
	 *
	 * @param clazz
	 * @param id
	 * @throws IllegalStateException iff the given {@code id} has already been used
	 * for another manifest.
	 * @return
	 */
	public <M extends TypedManifest> M create(Class<M> clazz, String id, Options options) {
		requireNonNull(clazz);
		requireNonNull(id);

		checkArgument("Not a proper manifest fragment type: "+clazz,
				ManifestFragment.class.isAssignableFrom(clazz));
		checkArgument("Duplicate id: "+id, !lookup.containsKey(id));

		TypedManifest manifest = factory.create(clazz, options);
		assignId(manifest, id);

		lookup.put(id, (ManifestFragment) manifest);

		return markLast(clazz.cast(manifest));
	}

	/**
	 * Creates a manifest with an explicit {@code id} and a host identified by
	 * {@code hostId}.
	 *
	 * @param clazz
	 * @param id
	 * @param hostId
	 * @throws IllegalStateException iff the given {@code id} has already been used
	 * for another manifest.
	 * @throws IllegalStateException iff the given {@code hostId} is not mapped to
	 * a valid manifest.
	 * @return
	 */
	public <M extends TypedManifest> M create(Class<M> clazz, String id, String hostId) {
		requireNonNull(clazz);
		requireNonNull(id);
		requireNonNull(hostId);

		checkArgument("Not a proper manifest fragment type: "+clazz,
				ManifestFragment.class.isAssignableFrom(clazz));
		checkArgument("Duplicate id: "+id, !lookup.containsKey(id));

		TypedManifest host = fetch(hostId);

		ManifestFragment manifest = factory.create(ManifestType.forClass(clazz), host);
		assignId(manifest, id);
		lookup.put(id, manifest);

		return markLast(clazz.cast(manifest));
	}

	/**
	 * Creates a manifest without an explicit id but with a host identified by
	 * {@code hostId}.
	 *
	 * @param clazz
	 * @param hostId
	 * @throws IllegalStateException iff the given {@code hostId} is not mapped to
	 * a valid manifest.
	 * @return
	 */
	public <M extends TypedManifest> M createInternal(Class<M> clazz, String hostId) {
		requireNonNull(clazz);
		requireNonNull(hostId);

		TypedManifest host = fetch(hostId);

		ManifestFragment manifest = factory.create(ManifestType.forClass(clazz), host);

		return markLast(clazz.cast(manifest));
	}

	public ImplementationManifest live(Class<?> clazz) {
		return new ImplementationManifestImpl(last())
				.setSourceType(SourceType.DEFAULT)
				.setClassname(clazz.getName());
	}

	public Hierarchy<ContainerManifestBase<?>> containers() {
		return new HierarchyImpl<>();
	}

	@SuppressWarnings("unchecked")
	public <M extends ManifestFragment> M fetch(String id) {
		requireNonNull(id);
		ManifestFragment manifest = lookup.get(id);
		checkArgument("Unknown id: "+id, manifest!=null);
		return (M)manifest;
	}

	@SuppressWarnings("unchecked")
	public <M extends TypedManifest> M last() {
		checkState("No last manifest set", lastManifest!=null);
		return (M)lastManifest;
	}
}
