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
 */
package de.ims.icarus2.model.manifest.api;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

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

		forEachAnnotationManifest(m -> result.add(m.getKey()));

		return result.getAsSet();
	}

	@AccessRestriction(AccessMode.READ)
	default Set<String> getLocalAvailableKeys() {
		LazyCollection<String> result = LazyCollection.lazySet();

		forEachLocalAnnotationManifest(m -> result.add(m.getKey()));

		return result.getAsSet();
	}

	/**
	 * Returns the manifest responsible for describing the given annotation key.
	 *
	 * @param key
	 * @return
	 * @throws NullPointerException if the {@code key} argument is {@code null}
	 * @throws IllegalArgumentException if the given {@code key} is unknown to
	 * this manifest
	 */
	@AccessRestriction(AccessMode.READ)
	AnnotationManifest getAnnotationManifest(String key);

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
	 * this layer, the method should return {@code null}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getDefaultKey();

	boolean isLocalDefaultKey();

	@AccessRestriction(AccessMode.READ)
	boolean isAnnotationFlagSet(AnnotationFlag flag);

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

	// Modification methods

	void setDefaultKey(String key);

	void addAnnotationManifest(AnnotationManifest annotationManifest);

	void removeAnnotationManifest(AnnotationManifest annotationManifest);

	void setAnnotationFlag(AnnotationFlag flag, boolean active);
}
