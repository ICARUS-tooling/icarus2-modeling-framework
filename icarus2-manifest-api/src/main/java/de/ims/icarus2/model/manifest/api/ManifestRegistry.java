/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.ims.icarus2.model.manifest.api.events.ManifestEvents;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.events.EventManager;
import de.ims.icarus2.util.events.Events;

/**
 * Models the central <i>storage</i> space for all kinds of manifests.
 * The {@code CorpusRegistry} has essentially two tasks:
 * <p>
 * First is the storing of {@link Manifest templates}, {@link CorpusManifest live corpora manifests}
 * and {@link LayerType shared layer types}. Besides basic {@link #addTemplate(Manifest) add} and
 * {@link #removeTemplate(Manifest) remove} methods, the registry provides for a variety of lookup
 * functions to collect any of the above 3 types based on custom {@link Predicate predicates}.
 * <p>
 * The second task is to provide integer based <i>globally unique</i> identifiers ({@code uid}) for layers and other
 * framework members created at runtime. Those {@code uids} are required to be unique during the lifetime of the
 * registry or until {@link #resetUIDs() reset} manually.
 * <p>
 * Clients that wish to be notified about changes to the registry can subscribe by
 * {@link #addListener(de.ims.icarus2.util.events.SimpleEventListener, String) adding a listener}.
 * The registry will fire {@link Events#ADD add}, {@link Events#REMOVE remove} and {@link Events#CHANGE change} events
 * <b>before</b> the respective changes are made, giving subscribed listeners the chance to interrupt illegal changes
 * (typically a {@link CorpusManager manager} responsible for a registry will subscribe and intercept changes so that
 * live corpora or those in the process of transit to another state are prevented from getting removed).
 * The registry will fire {@link Events#ADDED added}, {@link Events#REMOVED removed} and {@link Events#CHANGED changed} events
 * <b>after</b> changes have successfully been made to the data.
 * For information on how the corresponding objects are being passed on by the events check the various
 * {@code addXX} and {@code removeXX} methods.
 *
 *
 * @author Markus Gärtner
 *
 */
public interface ManifestRegistry extends EventManager {

	/**
	 * Creates a new {@code unique id} that is guaranteed to be unique
	 * during the lifetime of this registry.
	 *
	 * @return
	 */
	int createUID();

	/**
	 * Resets whatever mechanism this registry is using to create {@code unique ids}
	 * and allows for the creation of fresh ids without conflicting with currently
	 * registered live corpora or templates.
	 *
	 */
	void resetUIDs();

	// Layer type methods

	void forEachLayerType(Consumer<? super LayerType> action);

	default Collection<LayerType> getLayerTypes() {
		LazyCollection<LayerType> result = LazyCollection.lazyList();

		forEachLayerType(result);

		return result.getAsList();
	}

	default Collection<LayerType> getLayerTypes(Predicate<? super LayerType> p) {
		requireNonNull(p);
		LazyCollection<LayerType> result = LazyCollection.lazyList();

		forEachLayerType(m -> {if(p.test(m)) result.add(m);});

		return result.getAsList();
	}

	Optional<LayerType> getLayerType(String name);

	void addLayerType(LayerType layerType);

	void removeLayerType(LayerType layerType);

	/**
	 * Returns the shared {@code LayerType} that describes overlay layers.
	 * @return
	 */
	LayerType getOverlayLayerType();

	// Corpus methods

	void addCorpusManifest(CorpusManifest manifest);

	void removeCorpusManifest(CorpusManifest manifest);

	Set<ManifestLocation> getCorpusSources();

	Optional<CorpusManifest> getCorpusManifest(String id);

	/**
	 * Returns {@code true} in case there are other manifests referencing the given one.
	 * Methods such as {@link #removeTemplate(Manifest)} will throw an exception in case
	 * the manifest to be removed is still required by others. This is to prevent inconsistencies
	 * in the registry. In addition many 'setter' methods of various manifest type will also
	 * check if a modification is actually legit by calling this method.
	 * <p>
	 * A manifest is supposed to be locked if at least one of the following conditions holds:
	 * <ul>
	 * <li>It is a {@link Manifest#isTemplate() template} and another manifest (template or live instance) is
	 * {@link Manifest#getTemplate() referencing it as a template}.</li>
	 * <li>It is an {@link OptionsManifest} and another {@link MemberManifest} is using it.</li>
	 * <li>It is a {@link LayerManifest} and referenced by a live {@link LayerType} as its
	 * {@link LayerType#getSharedManifest() shared manifest}.</li>
	 * <li>It is a {@link ContextManifest} actively used in a {@link CorpusManifest}.</li>
	 * <li>It is a {@link LayerManifest} and actively used in a {@link ContextManifest}.</li>
	 * <li>It is a {@link DriverManifest} and actively used in a {@link ContextManifest}.</li>
	 * </ul>
	 *
	 * @param manifest
	 * @return
	 */
	boolean isLocked(Manifest manifest);

	void forEachCorpus(Consumer<? super CorpusManifest> action);

	default Set<String> getCorpusIds() {
		LazyCollection<String> result = LazyCollection.lazySet();

		forEachCorpus(m -> result.add(m.getId().orElseThrow(Manifest.invalidId(
				"Corpus does not declare a valid id: "+ManifestUtils.getName(m)))));

		return result.getAsSet();
	}

	default Collection<CorpusManifest> getCorpusManifests() {
		LazyCollection<CorpusManifest> result = LazyCollection.lazyList();

		forEachCorpus(result);

		return result.getAsList();
	}

	default Collection<CorpusManifest> getCorpusManifests(Predicate<? super CorpusManifest> p) {
		requireNonNull(p);
		LazyCollection<CorpusManifest> result = LazyCollection.lazyList();

		forEachCorpus(m -> {if(p.test(m)) result.add(m);});

		return result.getAsList();
	}

	default Collection<CorpusManifest> getCorpusManifestsForSource(ManifestLocation manifestLocation) {
		requireNonNull(manifestLocation);
		return getCorpusManifests(m -> manifestLocation.equals(m.getManifestLocation()));
	}

	boolean hasTemplate(String id);

	<M extends Manifest> Optional<M> getTemplate(String id);


//	void addContextManifest(CorpusManifest corpus, ContextManifest context);

//	void removeContextManifest(CorpusManifest corpus, ContextManifest context);

	// Notification methods

	void corpusManifestChanged(CorpusManifest corpus);

	void contextManifestChanged(ContextManifest context);

	// Template methods

	void forEachTemplate(Consumer<? super Manifest> action);

	default Collection<Manifest> getTemplates() {
		LazyCollection<Manifest> result = LazyCollection.lazyList();

		forEachTemplate(result);

		return result.getAsList();
	}

	/**
	 * Returns all templates for which the given predicate returns {@code true}.
	 * @param p
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <M extends Manifest> Collection<M> getTemplates(Predicate<? super M> p) {
		requireNonNull(p);
		LazyCollection<M> result = LazyCollection.lazyList();

		forEachTemplate(m -> {if(p.test((M) m)) result.add((M) m);});

		return result.getAsList();
	}

	/**
	 * Returns all the {@code ContextManifest} templates added to this registry as templates.
	 * @return
	 *
	 * @see #getRootContextTemplates()
	 */
	default Collection<ContextManifest> getContextTemplates() {
		return getTemplates(m -> m.getManifestType()==ManifestType.CONTEXT_MANIFEST);
	}

	/**
	 * Returns all previously registered templates that are of the given
	 * {@code ManifestType}. Note that this method only returns templates
	 * implementing the {@link MemberManifest} interface! So for example
	 * it is not possible to collect templates for the {@link OptionsManifest}
	 * interface this way. Use the {@link #getTemplatesOfClass(Class)} for
	 * such cases.
	 *
	 * @throws NullPointerException if the {@code type} argument is {@code null}
	 * @see #getTemplatesOfClass(Class)
	 */
	default Collection<Manifest> getTemplatesOfType(ManifestType type) {
		requireNonNull(type);
		return getTemplates(m -> m.getManifestType()==type);
	}

	/**
	 * Returns all previously templates that derive from the given {@code Class}.
	 *
	 * @throws NullPointerException if the {@code clazz} argument is {@code null}
	 */
	default <M extends Manifest> Collection<M> getTemplatesOfClass(Class<M> clazz) {
		requireNonNull(clazz);
		return getTemplates(m -> clazz.isInstance(m));
	}

	default Collection<Manifest> getTemplatesForSource(ManifestLocation manifestLocation) {
		requireNonNull(manifestLocation);
		return getTemplates(m -> manifestLocation.equals(m.getManifestLocation()));
	}

	/**
	 * Returns a list of {@code ContextManifest} objects that can be used to
	 * create a new corpus by serving as the default context of that corpus.
	 * Suitability is checked by means of the {@link ContextManifest#isIndependentContext()}
	 * method returning {@code true}.
	 *
	 * @return
	 */
	default Collection<ContextManifest> getRootContextTemplates() {
		return getTemplates(m -> m.getManifestType()==ManifestType.CONTEXT_MANIFEST
				&& m.isIndependentContext());
	}

	Set<ManifestLocation> getTemplateSources();

	/**
	 * Adds a single template manifest to this registry and updates internal reference links
	 * that are used for determining {@link #isLocked(Manifest) locks}.
	 * Fires {@link ManifestEvents#ADD_TEMPLATE} and {@link ManifestEvents#ADDED_TEMPLATE}.
	 *
	 * @param template
	 */
	void addTemplate(Manifest template);

	/**
	 * Batch version of {@link #addTemplate(Manifest)} that ensures that all the internal
	 * steps are performed for the entire batch of template manifests at once.
	 * Fires the batch events {@link ManifestEvents#ADD_TEMPLATES} and {@link ManifestEvents#ADDED_TEMPLATES}.
	 * @param templates
	 */
	void addTemplates(Collection<? extends Manifest> templates);

	void removeTemplate(Manifest template);
}
