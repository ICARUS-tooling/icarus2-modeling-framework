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
package de.ims.icarus2.model.manifest.api;

import java.util.Optional;

import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.strings.StringResource;

/**
 * Models the description of an indexable mapping in the form of either a
 * direct dependency or a bounding relation. The index maps from elements in
 * the source layer to elements in the target layer.
 *
 * Depending on the layer types of both the source and target layer, very specific
 * data structures are needed to model the actual mapping.
 *
 * TODO mention fragment layers
 *
 * Note that the indices described by this manifest are only about directly related
 * markable layers, not the contents of annotation layers!
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface MappingManifest extends ManifestFragment, Embedded {

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.MAPPING_MANIFEST;
	}

	/**
	 * Returns the surrounding driver manifest that hosts this mapping.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default <M extends DriverManifest> Optional<M> getDriverManifest() {
		return getHost();
	}

	/**
	 * Returns the {@code id} of the source layer for this mapping.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getSourceLayerId();

	@AccessRestriction(AccessMode.READ)
	Optional<String> getTargetLayerId();

	@AccessRestriction(AccessMode.READ)
	Optional<Relation> getRelation();

	@AccessRestriction(AccessMode.READ)
	Optional<Coverage> getCoverage();

	@AccessRestriction(AccessMode.READ)
	Optional<MappingManifest> getInverse();

	// Modification methods

	MappingManifest setSourceLayerId(String sourceLayerId);

	MappingManifest setTargetLayerId(String targetLayerId);

	MappingManifest setRelation(Relation relation);

	MappingManifest setCoverage(Coverage coverage);

	MappingManifest setInverseId(String inverseId);

	MappingManifest setId(String id);

	/**
	 * Models the actual quantitative relation type of a mapping, i.e. the relative number
	 * of elements on both sides of the mapping. The possible values are {@code one} and
	 * {@code many}, which leads to 4 different relation types with varying levels of
	 * complexity when it comes to implementing them.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum Relation implements StringResource {

		/**
		 * Elements from the source and target layer are mapped one to one. If the
		 * corresponding {@code Coverage} is {@link Coverage#TOTAL_MONOTONIC} this
		 * equals the identity function.
		 */
		ONE_TO_ONE("one-to-one"),

		/**
		 * A single element in the source layer may hold an arbitrary number of elements from
		 * the target layer. Typical examples are all kinds of aggregating markable layers that
		 * feature containers as top level elements. Possible lookup structures include span lists
		 * (begin- and end-index for each source element) for source layers that host span elements
		 * and complete content lists (a list of exact target indices) for non-monotonic source
		 * layer members. While span lists are fairly easy to map to memory chunks or arrays, content
		 * lists pose some serious drawbacks, potentially requiring an additional layer of indices to
		 * map source elements to their respective sublist in a data block.
		 * The corresponding index function is injective.
		 */
		ONE_TO_MANY("one-to-many"),

		/**
		 * An arbitrary number of (not necessarily monotonic) elements in the source layer map to
		 * a common member of the target layer.
		 *
		 * If the target elements are spans, than an efficient
		 * lookup can be created by dividing the source layer into chunks of markables and then save
		 * for each such chunk the first and last element in the target layer that is truly contained
		 * in this chunk (with respect to its begin- and end-offset). To lookup a target the algorithm
		 * then first determines the correct chunk according to the source elements index and then
		 * performs a binary search on the spans in that chunk to find the target element.
		 * Performance can be controlled by adjusting chunk size to a value that provides a good tradeoff
		 * between required memory space to store the index information and the speed incurred by the
		 * binary search (which serves as a constant cost factor in the performance formula).
		 *
		 * In the case of non-monotonic elements in the target layer (e.g. clusters of source markables)
		 * the above technique fails and it might be required to store a dedicated target index value for
		 * each source element.
		 */
		MANY_TO_ONE("many-to-one"),

		/**
		 * As the most complex relation version, this one maps an arbitrary number of source elements to
		 * an again arbitrary number of target elements/containers. As an example imagine entities in the
		 * source layer being grouped into category containers in the target layer, allowing each entity to
		 * be assigned many different categories at once.
		 *
		 * Depending on the container type of the target elements, this version gets easy or very expensive.
		 *
		 * If the target elements are spans, than it is possible to use the strategy proposed for the
		 * {@link #MANY_TO_ONE} relation with a slight addition: When the first target container is found
		 * using binary search within the chunk, then neighbors to both sides are added to the result collection,
		 * until containers are encountered for both ends, that do not contain the source element. The complexity
		 * in this case is limited by the maximum "nesting depth" of spans in the target layer, which remains
		 * to be evaluated as a proper upper bound. Looking in the neighborhood of the first successful match
		 * is possible due to the sorted nature of top-level layer elements and the sorting rules for spans
		 * (span locality).
		 *
		 * For non-monotonic target elements the rules for the {@link #ONE_TO_MANY} relation apply.
		 */
		MANY_TO_MANY("many-to-many");

		private final String xmlForm;

		private Relation(String xmlForm) {
			this.xmlForm = xmlForm;
		}

		/**
		 * Returns the inverse version of the current relation. This method is used to obtain the required
		 * information when an {@code MappingManifest}'s {@link MappingManifest#includeReverse()} method returns
		 * {@code true} and indicates the necessity of an index for the inverse relation.
		 * <p>
		 * Note that {@link #ONE_TO_ONE} and {@link #MANY_TO_MANY} relations do not change when inverted!
		 * @return
		 */
		public Relation invert() {
			switch (this) {
			case MANY_TO_ONE:
				return ONE_TO_MANY;
			case ONE_TO_MANY:
				return MANY_TO_ONE;

			default:
				return this;
			}
		}

		/**
		 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return xmlForm;
		}

		private static LazyStore<Relation, String> store = LazyStore.forStringResource(Relation.class);

		public static Relation parseRelation(String s) {
			return store.lookup(s);
		}
	}

	/**
	 * Describes the areal coverage of the target layer's index space. This information
	 * is intended to be exploited by {@code Mapping} implementations to optimize access
	 * and lookup times. Coverage is expressed as a combination of two boolean flags,
	 * <i>totality</i> and <i>monotonicity</i>:
	 * <p>
	 * <i>Totality</i> means that the available target space is completely covered by the
	 * elements in the source layer (i.e. for every index value in the target layer there
	 * is guaranteed to be at least one matching index value in the source layer, meaning
	 * the index implementation describes a surjective function). Note however that this does
	 * <b>not</b> equate to a full coverage of the source index space! There might still be
	 * source indices without matching elements in the target layer.
	 * {@code ∀t∈Target ∃ s∈Source: (s,t)∈Mapping}
	 * <p>
	 * <i>Monotonicity</i> of a mapping is given, when for any two distinct source indices the
	 * respective collections of target indices are also distinct (i.e. the index describes
	 * an injective function). In addition the index function preserves order relations on both
	 * index value spaces. For any two source index values {@code j} and {@code k}
	 * with <tt>j &lt; k</tt> the corresponding target {@code IndexSet}s {@code t(j)} and {@code t(k)}
	 * are also ordered <tt>t(j) &lt; t(k)</tt> according to {@link IndexSet#INDEX_SET_SORTER}.
	 * Note that this means target index collections for continuous source indices are again
	 * a continuous subset of the target value space.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum Coverage implements StringResource {

		/**
		 * The entire target index space is covered, but the mapped areas might overlap or be
		 * in a somewhat "random" fashion (surjective function):
		 * <p>
		 * {@code ∀t∈Target ∃ s∈Source: (s,t)∈Mapping}
		 */
		TOTAL("total", true, false),

		/**
		 * No exploitable patterns available in the way of index mapping.
		 */
		PARTIAL("partial", false, false),

		/**
		 * Mapped elements follow the same order as their respective source counterparts
		 * (strictly monotonic function):
		 * <p>
		 * {@code ∀s1,s2∈Source: m(s1)=m(s2) ⇒ s1=s2} <br>
		 * {@code ∀s1,s2∈Source, s1<s2: m(s1) < m(s2)}
		 */
		MONOTONIC("monotonic", false, true),

		/**
		 * Entire target space is covered and mapping is following the monotonicity rule:
		 * <p>
		 * {@code ∀t1,t2∈Target, t1<t2  ∃ s1,s2∈Source: t1=m(s1) ∧ t2=m(s2) ∧  s1<s2}
		 */
		TOTAL_MONOTONIC("total-monotonic", true, true),
		;

		private final boolean total, monotonic;
		private String xmlForm;

		private Coverage(String xmlForm, boolean total, boolean monotonic) {
			this.xmlForm = xmlForm;
			this.total = total;
			this.monotonic = monotonic;
		}

		/**
		 * <i>Totality</i> means that the available target space is completely covered by the
		 * elements in the source layer (i.e. for every index value in the target layer there
		 * is guaranteed to be at least one matching index value in the source layer, meaning
		 * the index implementation describes an surjective function).
		 *
		 * @return the total
		 */
		public boolean isTotal() {
			return total;
		}

		/**
		 * <i>Monotonicity</i> of an index is given, when for any two distinct source indices the
		 * respective collections of target indices are also distinct (i.e. the index describes
		 * an injective function). In addition the index function preserves order relations on both
		 * index value spaces. For any two source index values {@code j} and {@code k}
		 * with <tt>j &lt; k</tt> the corresponding target {@code IndexSet}s {@code t(j)} and {@code t(k)}
		 * are also ordered <tt>t(j) &lt; t(k)</tt> according to {@link IndexSet#INDEX_SET_SORTER}.
		 * Note that this means target index collections for continuous source indices are again
		 * a continuous subset of the target value space.
		 *
		 * @return the monotonic
		 */
		public boolean isMonotonic() {
			return monotonic;
		}

		/**
		 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return xmlForm;
		}

		private static LazyStore<Coverage, String> store = LazyStore.forStringResource(Coverage.class);

		public static Coverage parseCoverage(String s) {
			return store.lookup(s);
		}
	}
}
