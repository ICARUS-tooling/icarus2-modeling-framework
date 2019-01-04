/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.Mutable.MutableObject;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * Describes a layer that adds <i>content</i> in the form of annotations to
 * another item, structure or fragment layer.
 *
 * In addition it defines whether or not a layer can be
 * accessed for searching and if so, whether it can be indexed to speed up a
 * search operation. Note that those two flags are fixed properties of the
 * layer manifest and therefore not modifiable by the user. Not being able to
 * search a layer does however {@code not} imply it can't be used by the user at
 * all. It simply means the possible interactions besides looking at the visualized
 * form are restricted to manual operations like annotating or exploring it without
 * the help of the search engine.
 * <p>
 * Side note on indexing:
 * <br>
 * For an actual index to be constructed for a given layer, itself and <b>all</b> the layers
 * it depends on (even indirectly) have to be indexable. For the simple case of indexing
 * an annotation layer this is trivial, since most annotations will refer to basic
 * item or structure layers which are always indexable. Therefore the annotation layer
 * makes the choice regarding the option of indexing being available.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface AnnotationLayerManifest extends LayerManifest {

	@AccessRestriction(AccessMode.READ)
	void forEachAnnotationManifest(Consumer<? super AnnotationManifest> action);

	@AccessRestriction(AccessMode.READ)
	void forEachLocalAnnotationManifest(Consumer<? super AnnotationManifest> action);

	/**
	 * Returns the available (predefined) keys that can be used for annotation.
	 * The returned set is always non-null but might be empty.
	 *
	 * @return An immutable {@code Set} containing all the available keys used
	 * for annotations.
	 */
	@AccessRestriction(AccessMode.READ)
	default Set<String> getAvailableKeys() {
		LazyCollection<String> result = LazyCollection.lazySet();

		forEachAnnotationManifest(
				m -> result.add(m.getKey().orElseThrow(
						ManifestException.error(GlobalErrorCode.ILLEGAL_STATE,
								"Missing key for annotation layer "+ManifestUtils.getName(m)))));

		return result.getAsSet();
	}

	@AccessRestriction(AccessMode.READ)
	default Set<String> getLocalAvailableKeys() {
		LazyCollection<String> result = LazyCollection.lazySet();

		forEachLocalAnnotationManifest(
				m -> result.add(m.getKey().orElseThrow(
						ManifestException.error(GlobalErrorCode.ILLEGAL_STATE,
								"Missing key for annotation layer "+ManifestUtils.getName(m)))));

		return result.getAsSet();
	}

	/**
	 * Returns the manifest responsible for describing the given annotation key.
	 *
	 * @param key
	 * @return
	 * @throws NullPointerException if the {@code key} argument is {@code null}
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<AnnotationManifest> getAnnotationManifest(String key);

	@AccessRestriction(AccessMode.READ)
	default Set<AnnotationManifest> getAnnotationManifests() {
		LazyCollection<AnnotationManifest> result = LazyCollection.lazyLinkedSet();

		forEachAnnotationManifest(result);

		return result.getAsSet();
	}

	@AccessRestriction(AccessMode.READ)
	default Set<AnnotationManifest> getLocalAnnotationManifests() {
		LazyCollection<AnnotationManifest> result = LazyCollection.lazyLinkedSet();

		forEachLocalAnnotationManifest(result);

		return result.getAsSet();
	}

	/**
	 * Returns the (optional) default key for this annotation layer. This is only
	 * important for user interfaces as a hint for selecting the key which is to
	 * be presented first to the user. If no dedicated default key is defined for
	 * this layer, the method should return an empty {@link Optional}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getDefaultKey();

	boolean isLocalDefaultKey();

	@AccessRestriction(AccessMode.READ)
	boolean isAnnotationFlagSet(AnnotationFlag flag);

	@AccessRestriction(AccessMode.READ)
	boolean isLocalAnnotationFlagSet(AnnotationFlag flag);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveAnnotationFlag(Consumer<? super AnnotationFlag> action);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveLocalAnnotationFlag(Consumer<? super AnnotationFlag> action);

	default Set<AnnotationFlag> getActiveAnnotationFlags() {
		EnumSet<AnnotationFlag> result = EnumSet.noneOf(AnnotationFlag.class);

		forEachActiveAnnotationFlag(result::add);

		return result;
	}

	default Set<AnnotationFlag> getLocalActiveAnnotationFlags() {
		EnumSet<AnnotationFlag> result = EnumSet.noneOf(AnnotationFlag.class);

		forEachActiveLocalAnnotationFlag(result::add);

		return result;
	}

	@AccessRestriction(AccessMode.READ)
	void forEachReferenceLayerManifest(Consumer<? super TargetLayerManifest> action);

	@AccessRestriction(AccessMode.READ)
	void forEachLocalReferenceLayerManifest(Consumer<? super TargetLayerManifest> action);

	/**
	 * Returns the list of (resolved) reference layers for this annotation layer manifest.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<TargetLayerManifest> getReferenceLayerManifests() {
		LazyCollection<TargetLayerManifest> result = LazyCollection.lazyList();

		forEachReferenceLayerManifest(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	default List<TargetLayerManifest> getLocalReferenceLayerManifests() {
		LazyCollection<TargetLayerManifest> result = LazyCollection.lazyList();

		forEachLocalReferenceLayerManifest(result);

		return result.getAsList();
	}

	// Modification methods

	AnnotationLayerManifest setDefaultKey(String key);

	AnnotationLayerManifest addAnnotationManifest(AnnotationManifest annotationManifest);

	AnnotationLayerManifest removeAnnotationManifest(AnnotationManifest annotationManifest);

	AnnotationLayerManifest setAnnotationFlag(AnnotationFlag flag, boolean active);

	default TargetLayerManifest addAndGetReferenceLayerId(String referenceLayerId) {
		MutableObject<TargetLayerManifest> result = new MutableObject<>();
		addReferenceLayerId(referenceLayerId, result::set);
		return result.get();
	}

	AnnotationLayerManifest addReferenceLayerId(String referenceLayerId, Consumer<? super TargetLayerManifest> action);

	AnnotationLayerManifest removeReferenceLayerId(String referenceLayerId);
}
