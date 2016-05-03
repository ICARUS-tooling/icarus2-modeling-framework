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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/ContextManifest.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.manifest;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Predicate;

import de.ims.icarus2.model.api.access.AccessControl;
import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessPolicy;
import de.ims.icarus2.model.api.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 * @version $Id: ContextManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ContextManifest extends MemberManifest {

	public static final boolean DEFAULT_INDEPENDENT_VALUE = false;
	public static final boolean DEFAULT_EDITABLE_VALUE = false;

	@AccessRestriction(AccessMode.READ)
	CorpusManifest getCorpusManifest();

	@AccessRestriction(AccessMode.READ)
	DriverManifest getDriverManifest();

	boolean isLocalDriverManifest();

	@AccessRestriction(AccessMode.READ)
	void forEachPrerequisite(Consumer<? super PrerequisiteManifest> action);

	@AccessRestriction(AccessMode.READ)
	default void forEachLocalPrerequisite(Consumer<? super PrerequisiteManifest> action) {
		forEachPrerequisite(p -> {
			if(p.getContextManifest()==this) {
				action.accept(p);
			}
		});
	}

	@Override
	default public ManifestFragment getHost() {
		return getCorpusManifest();
	};

	/**
	 * Returns a list of prerequisites describing other layers a corpus
	 * has to host in order for this context to be operational. If this
	 * context does not depend on other layers the returned list is empty.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<PrerequisiteManifest> getPrerequisites() {
		LazyCollection<PrerequisiteManifest> result = LazyCollection.lazyList();

		forEachPrerequisite(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	default List<PrerequisiteManifest> getLocalPrerequisites() {
		LazyCollection<PrerequisiteManifest> result = LazyCollection.lazyList();

		forEachLocalPrerequisite(result);

		return result.getAsList();
	}

	/**
	 * Looks up the prerequisite manifest linked for the specified alias.
	 *
	 * @param alias
	 * @return the {@link PrerequisiteManifest} mapped to the specified {@code alias}
	 * or {@code null} if no such manifest exists.
	 */
	@AccessRestriction(AccessMode.READ)
	PrerequisiteManifest getPrerequisite(String alias);

	@AccessRestriction(AccessMode.READ)
	void forEachLayerManifest(Consumer<? super LayerManifest> action);

	@AccessRestriction(AccessMode.READ)
	default void forEachLocalLayerManifest(Consumer<? super LayerManifest> action) {
		forEachLayerManifest(l -> {
			if(l.getContextManifest()==this) {
				action.accept(l);
			}
		});
	}

	/**
	 * Returns the list of manifests that describe the layers in this context.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<LayerManifest> getLayerManifests() {
		LazyCollection<LayerManifest> result = LazyCollection.lazyList();

		forEachLayerManifest(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	default List<LayerManifest> getLocalLayerManifests() {
		LazyCollection<LayerManifest> result = LazyCollection.lazyList();

		forEachLocalLayerManifest(result);

		return result.getAsList();
	}

	@SuppressWarnings("unchecked")
	@AccessRestriction(AccessMode.READ)
	default <L extends LayerManifest> List<L> getLayerManifests(Predicate<? super LayerManifest> p) {
		LazyCollection<L> result = LazyCollection.lazyList();

		forEachLayerManifest(m -> {
			if(p.apply(m)) {
				result.add((L) m);
			}
		});

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	void forEachGroupManifest(Consumer<? super LayerGroupManifest> action);

	@AccessRestriction(AccessMode.READ)
	default void forEachLocalGroupManifest(Consumer<? super LayerGroupManifest> action) {
		forEachGroupManifest(g -> {
			if(g.getContextManifest()==this) {
				action.accept(g);
			}
		});
	}

	/**
	 * Returns the logical groups of layers in this context.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<LayerGroupManifest> getGroupManifests() {
		LazyCollection<LayerGroupManifest> result = LazyCollection.lazyList();

		forEachGroupManifest(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	default List<LayerGroupManifest> getLocalGroupManifests() {
		LazyCollection<LayerGroupManifest> result = LazyCollection.lazyList();

		forEachLocalGroupManifest(result);

		return result.getAsList();
	}

	/**
	 * Returns the layer on the top of this context's layer hierarchy.
	 *
	 * @throws IllegalStateException iff this context is missing a primary layer declaration
	 */
	@AccessRestriction(AccessMode.READ)
	ItemLayerManifest getPrimaryLayerManifest();

	boolean isLocalPrimaryLayerManifest();

	/**
	 * Returns the layer manifest that describes this context's atomic units
	 * or {@code null} if that layer resides outside of this context. Note that
	 * the layer that serves as a foundation layer of a context is not allowed
	 * to declare another foundation layer in turn (i.e. his {@link ItemLayerManifest#getFoundationLayerManifest()}
	 * method must return {@code null}!
	 */
	@AccessRestriction(AccessMode.READ)
	ItemLayerManifest getFoundationLayerManifest();

	boolean isLocalFoundationLayerManifest();

	/**
	 * Looks up the layer manifest accessible via the given {@code id}. Note that
	 * this method provides access to layers from both this context and all linked
	 * prerequisites. Note further, that layers targeted by prerequisite declarations
	 * will only be resolved when this context manifest is actually hosted within
	 * a corpus manifest, since otherwise it is impossible for the context to access
	 * foreign resources.
	 */
	@AccessRestriction(AccessMode.READ)
	LayerManifest getLayerManifest(String id);

	/**
	 * Returns the manifests that describes where the data for this context's
	 * layers is loaded from and how to access distributed data sources.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	List<LocationManifest> getLocationManifests();

	/**
	 * Tells whether or not a context depends on other resources besides the
	 * data contained in its own layers. Only a context that is independent of
	 * external data can be assigned as default context of a corpus!
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isIndependentContext();

	boolean isLocalIndependentContext();

	/**
	 * Signals that this context is hosted within a valid corpus and is assigned the
	 * root context for it.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isRootContext();

	/**
	 * Returns {@code true} if the context described by this manifest can
	 * be edited by the user. This covers both editability of annotations
	 * and logical/structural content.
	 * <p>
	 * A context that describes primary data should usually set this flag
	 * to {@code false}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isEditable();

	boolean isLocalEditable();

	// Modification methods

	void setEditable(Boolean editable);

	void setDriverManifest(DriverManifest driverManifest);

	void setPrimaryLayerId(String primaryLayerId);

	void setFoundationLayerId(String foundationLayerId);

	void setIndependentContext(Boolean isIndependent);

	PrerequisiteManifest addPrerequisite(String alias);

	void removePrerequisite(PrerequisiteManifest prerequisiteManifest);

	void addLayerGroup(LayerGroupManifest groupManifest);

	void removeLayerGroup(LayerGroupManifest groupManifest);

	void addLocationManifest(LocationManifest manifest);

	void removeLocationManifest(LocationManifest manifest);

	/**
	 * Abstract description of a layer object this context depends on.
	 * <p>
	 * Note that prerequisites are only used in templates. When a template
	 * is being instantiated, all the prerequisites will be resolved to actual
	 * layers and linked accordingly.
	 * <p>
	 * Depending on the return values of this interface's methods a prerequisite
	 * can be viewed as unresolved or resolved. In the latter case both the
	 * {@link #getContextId()} and {@link #getLayerId()} return a valid non-null
	 * id. Since dependencies within the same context are not expressed by
	 * dependency manifests, it is always necessary to provide a valid id of a
	 * foreign context to actually resolve the target layer. Once a prerequisite
	 * has been fully resolved, the target layer is accessible via the specified
	 * alias. It is possible to "hard bind" to a foreign layer by omitting the
	 * optional type id and specify context and layer id right from the start. This
	 * will bypass the regular binding process (possible involving the user to resolve
	 * ambiguities) but on the other hand sacrifices flexibility, for the target context
	 * cannot be changed (note that the first instance of a context template in a corpus
	 * will always carry the full and unchanged id of the template, making this type
	 * of "hard binding" possible in the first place).
	 *
	 * @author Markus Gärtner
	 * @version $Id: ContextManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface PrerequisiteManifest extends Lockable {

		/**
		 * Returns the {@code ContextManifest} this prerequisite was declared in.
		 * Note that it is perfectly legal for intermediate templates to host
		 * prerequisites originating from one of their ancestors. Only for live
		 * instances of a template it is mandatory to only host their own
		 * prerequisites, which by then must have been resolved to legal targets!
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		ContextManifest getContextManifest();

		/**
		 * Returns the id of the target layer or {@code null} if an exact id match
		 * is not required or the prerequisite has not yet been fully resolved.
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		String getLayerId();

		/**
		 * Returns the id of the context which should be used to resolve the required
		 * layer (specified by the {@link #getLayerId() method}) or {@code null} if no
		 * exact match is required or the prerequisite has not yet been fully resolved.
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		String getContextId();

		/**
		 * If this layer only requires <i>some</i> layer of a certain type to be present
		 * this method provides the mechanics to tell this. When the returned value is
		 * {@code non-null} it is considered to be the exact name of a previously
		 * defined layer type.
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		String getTypeId();

		/**
		 * Returns the id the required layer should be assigned once resolved. This links
		 * the result of an abstract prerequisite declaration to a boundary or base definition
		 * in a template. In addition a prerequisite's alias serves as its identifier. Therefore
		 * an alias must be unique within the same context!
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		String getAlias();

		/**
		 * If this prerequisite is in resolved state, it was created based on some unresolved
		 * prerequisite in a context template. In that case this method returns the original
		 * prerequisite manifest in unresolved form, or {@code null} otherwise.
		 * <p>
		 * Note that in case the prerequisite was declared using "hard binding" then this method
		 * will return also {@code null}!
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		PrerequisiteManifest getUnresolvedForm();

		/**
		 * Returns a brief description of this prerequisite, usable as a hint for user interfaces
		 * when asking the user to resolve ambiguous references.
		 * <p>
		 * This is an optional method.
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		String getDescription();

		// Modification methods

		void setDescription(String description);
		void setLayerId(String layerId);
		void setTypeId(String typeId);
		void setContextId(String contextId);
	}
}
