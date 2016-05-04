/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.manifest.api;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
 * {@link #addListener(String, de.ims.icarus2.util.events.EventListener) adding a listener}.
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
 * @version $Id$
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
	 * and allows for the creation of fresh ids even if they have been used prior to
	 * the reset. Note that this method should be used very carefully and only when
	 * there are absolutely no live corpora instantiated and active! Ignoring this
	 * constraint might result in unstable behavior when multiple members of the
	 * framework happen to be assigned the same {@code uid}.
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
		LazyCollection<LayerType> result = LazyCollection.lazyList();

		forEachLayerType(m -> {if(p.test(m)) result.add(m);});

		return result.getAsList();
	}

	LayerType getLayerType(String name);

	void addLayerType(LayerType layerType);

	void removeLayerType(LayerType layerType);

	/**
	 * Returns the shared {@code LayerType} that describes overlay layers.
	 * @return
	 */
	LayerType getOverlayLayerType();

	// Corpus methods

	void addCorpus(CorpusManifest manifest);

	void removeCorpus(CorpusManifest manifest);

	Set<ManifestLocation> getCorpusSources();

	CorpusManifest getCorpus(String id);

	/**
	 * Returns {@code true} in case there are other manifests referencing the given one.
	 * Methods such as {@link #removeTemplate(Manifest)} will throw an exception in case
	 * the manifest to be removed is still required by others. This is to prevent inconsistencies
	 * in the registry. In addition many 'setter' methods of various manifest type will also
	 * check if a modification is actually legit by calling this method.
	 *
	 * @param manifest
	 * @return
	 */
	boolean isLocked(Manifest manifest);

	void forEachCorpus(Consumer<? super CorpusManifest> action);

	default Set<String> getCorpusIds() {
		LazyCollection<String> result = LazyCollection.lazySet();

		forEachCorpus(m -> result.add(m.getId()));

		return result.getAsSet();
	}

	default Collection<CorpusManifest> getCorpora() {
		LazyCollection<CorpusManifest> result = LazyCollection.lazyList();

		forEachCorpus(result);

		return result.getAsList();
	}

	default Collection<CorpusManifest> getCorpora(Predicate<? super CorpusManifest> p) {
		LazyCollection<CorpusManifest> result = LazyCollection.lazyList();

		forEachCorpus(m -> {if(p.test(m)) result.add(m);});

		return result.getAsList();
	}

	default Collection<CorpusManifest> getCorporaForSource(ManifestLocation manifestLocation) {
		return getCorpora(m -> manifestLocation.equals(m.getManifestLocation()));
	}

	boolean hasTemplate(String id);

	Manifest getTemplate(String id);


	void addContext(CorpusManifest corpus, ContextManifest context);

	void removeContext(CorpusManifest corpus, ContextManifest context);

	// Notification methods

	void corpusChanged(CorpusManifest corpus);

	void contextChanged(ContextManifest context);

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
		return getTemplates(m -> m.getManifestType()==type);
	}

	/**
	 * Returns all previously templates that derive from the given {@code Class}.
	 *
	 * @throws NullPointerException if the {@code clazz} argument is {@code null}
	 */
	default <M extends Manifest> Collection<M> getTemplatesOfClass(Class<M> clazz) {
		return getTemplates(m -> clazz.isInstance(m));
	}

	default Collection<Manifest> getTemplatesForSource(ManifestLocation manifestLocation) {
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
				&& ((ContextManifest)m).isIndependentContext());
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
