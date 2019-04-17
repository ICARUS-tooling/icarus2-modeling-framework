/**
 *
 */
package de.ims.icarus2.model.manifest.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.api.TypedManifest;

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

	private ManifestFragment lastManifest;

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
	public <M extends ManifestFragment> M create(Class<M> clazz) {
		requireNonNull(clazz);
		return markLast(factory.create(clazz));
	}

	private void assignId(ManifestFragment manifest, String id) {
		if(manifest instanceof ModifiableIdentity) {
			((ModifiableIdentity)manifest).setId(id);
		} else
			throw new InternalError("CLass needs to be handled: "+manifest.getClass());
	}

	private <M extends ManifestFragment> M markLast(M manifest) {
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
	public <M extends ManifestFragment> M create(Class<M> clazz, String id) {
		requireNonNull(clazz);
		requireNonNull(id);

		checkArgument("Duplicate id: "+id, !lookup.containsKey(id));

		ManifestFragment manifest = factory.create(clazz);
		assignId(manifest, id);

		lookup.put(id, manifest);

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
	public <M extends ManifestFragment> M create(Class<M> clazz, String id, String hostId) {
		requireNonNull(clazz);
		requireNonNull(id);
		requireNonNull(hostId);

		checkArgument("Duplicate id: "+id, !lookup.containsKey(id));

		TypedManifest host = fetch(hostId);

		ManifestFragment manifest = factory.create(ManifestType.forClass(clazz), host);
		assignId(manifest, id);
		lookup.put(id, manifest);

		return markLast(clazz.cast(manifest));
	}

	public ImplementationManifest live(Class<?> clazz) {
		return ManifestUtils.liveImplementation(last(), clazz);
	}

	@SuppressWarnings("unchecked")
	public <M extends ManifestFragment> M fetch(String id) {
		requireNonNull(id);
		ManifestFragment manifest = lookup.get(id);
		checkArgument("Unknown id: "+id, manifest!=null);
		return (M)manifest;
	}

	@SuppressWarnings("unchecked")
	public <M extends ManifestFragment> M last() {
		checkState("No last manifest set", lastManifest!=null);
		return (M)lastManifest;
	}
}
