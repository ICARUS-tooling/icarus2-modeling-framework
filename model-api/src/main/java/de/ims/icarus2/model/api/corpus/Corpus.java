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
 *
 * $Revision: 453 $
 *
 */
package de.ims.icarus2.model.api.corpus;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context.VirtualContext;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.edit.CorpusEditManager;
import de.ims.icarus2.model.api.edit.CorpusUndoListener;
import de.ims.icarus2.model.api.edit.CorpusUndoManager;
import de.ims.icarus2.model.api.edit.UndoableCorpusEdit;
import de.ims.icarus2.model.api.events.CorpusAdapter;
import de.ims.icarus2.model.api.events.CorpusEventManager;
import de.ims.icarus2.model.api.events.CorpusListener;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.meta.MetaData;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.data.ContentType;
import de.ims.icarus2.util.events.EventListener;
import de.ims.icarus2.util.id.DuplicateIdentifierException;
import de.ims.icarus2.util.id.UnknownIdentifierException;

/**
 * A {@code Corpus} object is the top-most member of the corpus framework and the
 * root object of the tree that represents a complex corpus structure. It hosts
 * an arbitrary number of {@code Layer} objects of various types which are grouped
 * into several {@code Context} implementations. A {@code Context} is the abstract
 * representation of a <i>source</i> of layers. Commonly used formats to physically
 * represent corpus data usually contain multiple annotation and/or structure/grouping
 * layers. All these layers, originating from the same source, that belong to the same
 * <i>format</i> are grouped into one {@code Context}.
 *
 * A corpus provides two levels of changes that can be performed:
 * <ol>
 * <li>Content-based changes within layers, containers or structures. These changes
 * usually originate from user actions and will be wrapped into {@code UndoableCorpusEdit}
 * objects which in turn are then stored in the corpus' shared {@code CorpusUndoManager}.
 * Note that these changes are only reflected in the physical storage of the corpus once
 * it gets saved. If a corpus is not declared to be editable then all attempts to mutate
 * its content will result in exceptions being thrown!</li>
 * <li>Descriptor-based changes affecting the layout of a corpus and its identity (or that
 * of its members). These changes are managed on the manifest level and persistent storing
 * of those informations is performed by the {@code CorpusRegistry}. Unlike content-based
 * changes they do <b>not</b> trigger undoable edits. The can however affect the undo history
 * by simply discarding all changes (e.g. removing a context will invalidate the current
 * undo history). A reason for this policy is the fact, that keeping track of changes on that
 * scale requires to keep a backup copy of the entire affected data (in the aforementioned case
 * an entire context) in memory to allow the user to undo his action.</li>
 * </ol>
 * Both levels are handled by different managers which both allow for listeners to be registered
 * for various events:
 *
 * For <i>content-based</i> changes the {@link CorpusEditManager} returned by {@link #getEditManager()}
 * provides the methods to programmatically initialize an edit process, to add changes to an
 * active edit and to finally commit all pending changes to a single undoable edit. While an
 * edit is in progress the model will fire several events whenever the update-level is increased or
 * decreased (via the {@link CorpusEditManager#beginUpdate()} or {@link CorpusEditManager#endUpdate()}
 * methods) and when the final edit is committed. These events are forwarded to common {@link EventListener}
 * instances, while the final edit is also delegated to all the registered {@link CorpusUndoListener}s,
 * including the shared {@link CorpusUndoManager} of the corpus affected by the changes.
 *
 * All <i>descriptor-based</i> changes on the other hand are propagated by the {@link CorpusEventManager} of
 * the corpus as obtained via {@link #getEventManager()}. The listeners supported by this manager are
 * solely of type {@link CorpusListener} and a special callback method is defined for each type of change.
 * (Note that there exists the {@link CorpusAdapter} class that implements the {@code CorpusListener}
 * interface with empty methods so that a new listener implementation only needs to define the methods
 * it actually requires).
 *
 * @author Markus Gärtner
 *
 */
//FIXME change the "root context" mechanisms so that we can model parallel primary data within a single corpus
public interface Corpus extends ManifestOwner<CorpusManifest> {

	CorpusManager getManager();

	/**
	 * Returns the lock object that should be used when performing <i>write</i>
	 * operations on this corpus. Especially when attempting to add a new layer
	 * and generating a unique name using the {@link #getUniqueName(String)} method
	 * is it absolutely crucial to perform the entire process while holding the
	 * write lock. Not doing so could mean that another layer might be registered
	 * with the exact same <i>unique</i> name and render the new layer invalid.
	 *
	 * @return the <i>write-lock</i> of this corpus object to be used for <i>descriptor-based</i> changes
	 */
	Lock getLock();

	/**
	 * Returns the {@code CorpusEditManager} that is used to model changes the user
	 * made to this corpus or {@code null} if no edit model is implemented.
	 *
	 * @return
	 */
	CorpusEditManager getEditManager();

	/**
	 * Returns the shared undo-manager that keeps track of changes of this corpus
	 * or {@code null} if no edit model is implemented by the corpus and therefore no
	 * undo-manager is present.
	 *
	 * @return
	 */
	CorpusUndoManager getUndoManager();

	/**
	 * Returns the control end-point for interaction with the <i>generation</i> of a corpus,
	 * that is, a kind of version tracker linked to the {@link #getEditManager() edit manager}.
	 * Whenever the content of a corpus experiences a modification in the form of a
	 * {@link UndoableCorpusEdit} the generation id (or "age") of the corpus will be modified.
	 *
	 * @return
	 */
	GenerationControl getGenerationControl();

	/**
	 * Creates a new {@link Scope} that contains all the layers of this corpus and
	 * uses the {@link Context#getPrimaryLayer() primary layer} of the
	 * {@link #getRootContext() root context} as {@code primary layer}.
	 *
	 * @return
	 */
	default Scope createCompleteScope() {
		ScopeBuilder builder = new ScopeBuilder(this);

		forEachCustomContext(c -> builder.addContext(c));
		forEachLayer(l -> builder.addLayer(l));

		builder.setPrimaryLayer(getPrimaryLayer());

		return builder.build();
	}

	/**
	 * Creates a new corpus view object that provides a filtered view on the sub-corpus
	 * defined by the given scope and indices. If the {@code indices} argument is {@code null}
	 * it means that <b>all</b> items in the scope's {@link Scope#getPrimaryLayer() primary layer}
	 * are to be included. In either way, if the total number of index values is very large
	 * it is up to the corpus implementation to decide on a paging policy of the view or ask the user
	 * if accessing a potentially very large data set is truly desired.
	 * <p>
	 * This method is intended to be executed on a background thread, since it has the potential
	 * for involving very expensive calculations!
	 *
	 * @param scope the <i>vertical filtering</i> to be applied to retrieve the new view
	 * @param indices the index values of all the scope's {@link Scope#getPrimaryLayer() primary layer's} items (need not be sorted)
	 * that should be included or {@code null} in case all items in that layer are to be used. This constitutes the
	 * <i>horizontal</i> filtering to be applied in order to retrieve the new view
	 * @param mode access mode the view is meant to support. Note that at most one view with write access can be
	 * open for a single corpus at any given time!
	 * @param options additional optional parameters the semantics of which are implementation specific
	 * @return
	 * @throws ModelException if the given {@code mode} requires write access and another view in write mode
	 * is already open.
	 *
	 * @see {@link ModelErrorCode#VIEW_ALREADY_OPENED}
	 */
	CorpusView createView(Scope scope, IndexSet[] indices, CorpusAccessMode mode, Options options) throws InterruptedException;

	default CorpusView createFullView(CorpusAccessMode mode, Options options) throws InterruptedException {
		Scope scope = createCompleteScope();
		return createView(scope, null, mode, options);
	}

	void forEachView(Consumer<? super CorpusView> action);

	default Set<CorpusView> getViews(Predicate<? super CorpusView> p) {
		LazyCollection<CorpusView> buffer = LazyCollection.lazySet();

		forEachView(v -> {if(p.test(v))buffer.add(v);});

		return buffer.getAsSet();
	}

	/**
	 * Returns the {@code Context} object all the default members of
	 * this corpus have been added to. If the default context has not yet been instantiated
	 * this method will load the appropriate driver implementation and construct the context.
	 *
	 * @return The {@code Context} hosting all the default members of the corpus
	 */
	default Context getRootContext() {
		return getRootContexts().get(0);
	}

	/**
	 * Looks up the context mapped to the given id.
	 *
	 * @param id
	 * @return
	 *
	 * @throws UnknownIdentifierException if no context is registered for the given id
	 */
	Context getContext(String id);

	VirtualContext getVirtualContext(String id);

	Driver getDriver(String id);

	/**
	 * Registers the given listener to the internal list of registered
	 * listeners. Does nothing if the provided listener is {@code null}.
	 * Note that implementations should make sure that no listener is
	 * registered more than once. Typically this means doubling the cost
	 * of registration. Since it is not to be expected that registrations
	 * occur extremely frequent, this increase in cost can be ignored.
	 *
	 * @param listener The listener to be registered, may be {@code null}
	 */
	void addCorpusListener(CorpusListener listener);

	/**
	 * Unregisters the given listener from the internal list of registered
	 * listeners. Does nothing if the provided listener is {@code null}.
	 * @param listener The listener to be unregistered, may be {@code null}
	 */
	void removeCorpusListener(CorpusListener listener);

	/**
	 * Returns the manifest that describes this corpus.
	 *
	 * @return The {@code CorpusManifest} for this corpus.
	 */
	@Override
	CorpusManifest getManifest();

	/**
	 * Returns the {@link Context#getPrimaryLayer() primary layer} of this context's
	 * {@link #getRootContext() root context}.
	 *
	 * @see Context#getPrimaryLayer()
	 */
	default ItemLayer getPrimaryLayer() {
		return getRootContext().getPrimaryLayer();
	}

	default List<ItemLayer> getPrimaryLayers() {
		LazyCollection<ItemLayer> result = LazyCollection.lazyList();

		forEachRootContext(c -> result.add(c.getPrimaryLayer()));

		return result.getAsList();
	}

	/**
	 * @see Context#getFoundationLayer()
	 */
	default ItemLayer getFoundationLayer() {
		return getRootContext().getFoundationLayer();
	}

	default List<ItemLayer> getFoundationLayers() {
		LazyCollection<ItemLayer> result = LazyCollection.lazyList();

		forEachRootContext(c -> result.add(c.getFoundationLayer()));

		return result.getAsList();
	}

	/**
	 * Returns all the {@code Context} object that are registered implicitly
	 * by adding layers. This list includes the <i>root context</i> as
	 * returned by {@link #getRootContext()} (therefore the returned {@code Set}
	 * is never empty).
	 *
	 * @return All the contexts available for this corpus.
	 */
	default Set<Context> getContexts() {
		Set<Context> result = new THashSet<>();

		result.add(getRootContext());

		Consumer<Context> action = c -> result.add(c);

		forEachCustomContext(action);
		forEachVirtualContext(action);

		return result;
	}

	/**
	 * Returns the virtual overlay layer that gives a layer-style access to
	 * the containers defined in other {@code ItemLayer} objects registered
	 * to this corpus.
	 *
	 * @return
	 */
	ItemLayer getOverlayLayer();

	Container getOverlayContainer();
	void forEachCustomContext(Consumer<? super Context> action);
	void forEachRootContext(Consumer<? super Context> action);
	void forEachVirtualContext(Consumer<? super VirtualContext> action);

	default void forEachLayer(Consumer<? super Layer> action) {
		Consumer<Context> action2 = c -> c.forEachLayer(action);
		forEachRootContext(action2);
		forEachCustomContext(action2);
		forEachVirtualContext(action2);
	}

	default Collection<Layer> getLayers(Predicate<? super Layer> p) {
		LazyCollection<Layer> buffer = LazyCollection.lazyList();

		forEachLayer(l -> {
			if(p.test(l)) buffer.add(l);
		});

		return buffer.getAsList();
	}

	default List<Context> getRootContexts() {
		LazyCollection<Context> buffer = LazyCollection.lazyList();

		forEachRootContext(c -> buffer.add(c));

		return buffer.getAsList();
	}

	default Collection<Context> getRootContexts(Predicate<? super Context> p) {
		LazyCollection<Context> buffer = LazyCollection.lazyList();

		forEachRootContext(c -> {if(p.test(c))buffer.add(c);});

		return buffer.getAsList();
	}

	default List<Context> getCustomContexts() {
		LazyCollection<Context> buffer = LazyCollection.lazyList();

		forEachCustomContext(c -> buffer.add(c));

		return buffer.getAsList();
	}

	default Collection<Context> getCustomContexts(Predicate<? super Context> p) {
		LazyCollection<Context> buffer = LazyCollection.lazyList();

		forEachCustomContext(c -> {if(p.test(c))buffer.add(c);});

		return buffer.getAsList();
	}

	default List<VirtualContext> getVirtualContexts() {
		LazyCollection<VirtualContext> buffer = LazyCollection.lazyList();

		forEachVirtualContext(c -> buffer.add(c));

		return buffer.getAsList();
	}

	default Collection<VirtualContext> getVirtualContexts(Predicate<? super VirtualContext> p) {
		LazyCollection<VirtualContext> buffer = LazyCollection.lazyList();

		forEachVirtualContext(c -> {if(p.test(c))buffer.add(c);});

		return buffer.getAsList();
	}

	default Collection<Context> getAllContexts(Predicate<? super Context> p) {
		LazyCollection<Context> buffer = LazyCollection.lazyList();

		Consumer<Context> action = c ->  {
			if(p.test(c))
				buffer.add(c);
		};

		forEachRootContext(action);
		forEachCustomContext(action);
		forEachVirtualContext(action);

		return buffer.getAsList();
	}

	/**
	 * Returns all layers registered for this corpus in the order of their
	 * registration. If this corpus does not yet host any layers the returned
	 * list is empty. Either way the returned list should be immutable.
	 * <p>
	 * Note that the returned list does <b>not</b> contain the virtual overlay layer!
	 *
	 * @return A list containing all the layers currently hosted within this corpus.
	 */
	default Collection<Layer> getLayers() {
		return getLayers(l -> true);
	}

	/**
	 * Returns all the layers in this corpus that are of the given type as defined
	 * by their {@link Layer#getLayerType()} method. If this corpus does not yet host any layers the returned
	 * list is empty. Either way the returned list should be immutable.
	 *
	 * @param type The desired type (e.g. "lemma")
	 * @return A list view of all the layers of the given type within this corpus
	 * @throws NullPointerException if the {@code type} argument is {@code null}.
	 */
	default Collection<Layer> getLayers(LayerType type) {
		return getLayers(l -> type.equals(l.getManifest().getLayerType()));
	}

	void addVirtualContext(VirtualContext context);

	void removeVirtualContext(VirtualContext context);

	/**
	 * Adds the given layer to this corpus and notifies listeners.
	 * <p>
	 * This method is intended to be called by {@link Context} instances hosted within
	 * this corpus to signal the creation and addition of new layers. A corpus should
	 * check whether or not the context surrounding the given {@code layer} is already
	 * known and throw an {@link ModelException exception} otherwise.
	 *
	 * @param layer the layer to be added
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws DuplicateIdentifierException if this corpus already contains a layer
	 * with the same {@code ID} as defined by {@link Layer#getId()}.
	 * @throws IllegalArgumentException if the layer is already part of another corpus
	 * or if the layer implementation violates the contract of the {@code Layer} interface
	 * and returns an invalid {@code Context} or {@code MemberManifest} object.
	 * @throws IllegalStateException if one or more of the layer's prerequisites cannot
	 * be fulfilled (i.e. one of the required underlying layers is missing).
	 */
	void addLayer(Layer layer);

	/**
	 * Removes the given layer from this corpus and notifies listeners.
	 *
	 * @param layer the layer to be added
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws IllegalArgumentException if the layer is not part of this corpus
	 * @throws IllegalStateException if one or more other layers defined this
	 * layer as prerequisite
	 */
	void removeLayer(Layer layer);

	/**
	 * Returns the registry where metadata for this corpus should be stored.
	 * Note that also the same term {@code metadata} is used, this refers to
	 * maintenance data used and created by the framework itself, not external data
	 * that can be added by any of the {@link #addMetaData(ContentType, Layer, MetaData) add}
	 * or {@link #removeMetaData(ContentType, Layer, MetaData) remove} methods intended
	 * for "real" {@link MetaData} objects!
	 * <p>
	 * Usually client code will have no business with the returned registry and should
	 * never attempt to write to it!
	 *
	 * @return
	 */
	MetadataRegistry getMetadataRegistry();

	/**
	 * Adds the given meta-data to this corpus and links it with the optionally
	 * specified layer. The {@code contentType} argument is used to group meta-data
	 * objects besides their layer.
	 *
	 * @param type The {@link ContentType} describing the meta-data object
	 * @param layer The layer the meta-data should be linked with or {@code null} if
	 * the meta-data should be assigned to the entire corpus
	 * @param data The meta-data itself, must not be {@code null}
	 * @throws NullPointerException if either one of {@code type} or {@code data}
	 * is {@code null}
	 * @throws IllegalArgumentException if the specified layer is not a part of
	 * this corpus
	 */
	void addMetaData(ContentType type, Layer layer, MetaData data);

	void removeMetaData(ContentType type, Layer layer, MetaData data);

	void forEachMetaData(BiPredicate<Layer, ContentType> filter, Consumer<MetaData> action);

	/**
	 * Returns all the previously registered meta-data objects for the given
	 * combination of {@link ContentType} and {@link Layer}. If the {@code layer}
	 * argument is {@code null} then only meta-data objects assigned to the
	 * entire corpus will be returned. If the {@code type} argument is {@code null}
	 * then all meta-data registered for the specified layer (or the entire corpus)
	 * is returned.
	 *
	 * Note that the returned collection of meta-data items is unordered. The corpus
	 * does not keep track of the order of insertion! It is advised that decisions
	 * required to select one out of many meta-data objects of the same type be delegated
	 * to the user.
	 *
	 * @param type The {@link ContentType} describing the meta-data object or {@code null}
	 * if all meta-data registered for the {@code layer} argument should be returned
	 * @param layer The {@link Layer} for which to fetch meta-data or {@code null} if only
	 * meta-data assigned to the entire corpus should be returned.
	 * @return A {@link Set} holding meta-data objects for the given combination of
	 * {@link ContentType} and {@link Layer}
	 * @throws IllegalArgumentException if the {@code layer} argument is non-{@code null}
	 * and the layer is not part of this corpus
	 */
	default Set<MetaData> getMetaData(ContentType type, Layer layer) {
		LazyCollection<MetaData> result = LazyCollection.lazyLinkedSet();

		forEachMetaData((l, t) -> l==layer && t==type, c -> result.add(c));

		return result.getAsSet();
	}

	default Set<MetaData> getMetaData(ContentType type) {
		LazyCollection<MetaData> result = LazyCollection.lazyLinkedSet();

		forEachMetaData((l, t) -> t==type, c -> result.add(c));

		return result.getAsSet();
	}

	default Set<MetaData> getMetaData(Layer layer) {
		LazyCollection<MetaData> result = LazyCollection.lazyLinkedSet();

		forEachMetaData((l, t) -> l==layer, c -> result.add(c));

		return result.getAsSet();
	}

	default Set<MetaData> getAllMetaData() {
		LazyCollection<MetaData> result = LazyCollection.lazyLinkedSet();

		forEachMetaData((l, t) -> true, c -> result.add(c));

		return result.getAsSet();
	}

	/**
	 * Called by the framework when the corpus gets unloaded to signal that
	 * it should free all internal resources and close drivers and other utility modules.
	 * @throws AccumulatingException
	 */
	void close() throws InterruptedException, AccumulatingException;
}
