/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.data.ContentType;

/**
 * Describes a single annotation within an {@link AnnotationLayerManifest annotation layer}.
 * Such an annotation is always identified by exactly {@code 1} <i>key</i> that must be unique
 * within the surrounding layer. In addition an arbitrary number of <i>aliases</i> can be provided.
 * Unlike an annotation's primary key those alias keys are not required to be unique, but will be
 * prioritized lower than primary keys during a resolution process.
 * <p>
 * TODO outline value sets/ranges/defaults and content-type settings + inheritance!
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface AnnotationManifest extends MemberManifest<AnnotationManifest>, Embedded {

	public static final boolean DEFAULT_ALLOW_UNKNOWN_VALUES = false;

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	default ManifestType getManifestType() {
		return ManifestType.ANNOTATION_MANIFEST;
	}

	/**
	 * Returns the hosting layer manifest or an empty {@link Optional} if this manifest
	 * is a template.
	 *
	 * @return
	 */
	default <M extends AnnotationLayerManifest> Optional<M> getLayerManifest() {
		return getHost();
	}

	/**
	 * Returns the <i>base-name</i> of the key this manifest
	 * describes.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getKey();

	/**
	 * Returns {@code true} iff the primary key used for this annotation has
	 * been declared locally.
	 *
	 * @return
	 */
	boolean isLocalKey();

	/**
	 * Applies the given {@code action} to all aliases in this manifest,
	 * including both locally defined and inherited ones.
	 * <p>
	 * This method should first apply the action to inherited aliases
	 * before traversing local ones.
	 *
	 * @param action
	 */
	@AccessRestriction(AccessMode.READ)
	void forEachAlias(Consumer<? super String> action);

	/**
	 * Returns {@code true} if the given {@code alias} has been declared locally.
	 *
	 * @param alias
	 * @return
	 */
	boolean isLocalAlias(String alias);

	/**
	 *  Applies the given {@code action} only to locally defined aliases.
	 *
	 * @param action
	 */
	@AccessRestriction(AccessMode.READ)
	default void forEachLocalAlias(Consumer<? super String> action) {
		forEachAlias(a -> {
			if(isLocalAlias(a)) {
				action.accept(a);
			}
		});
	}

	/**
	 * Returns a list of supported aliases that can be used for this
	 * manifest's key. If the key does not have any aliases this method
	 * should return an empty list.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<String> getAliases() {
		LazyCollection<String> result = LazyCollection.lazyList();

		forEachAlias(result);

		return result.getAsList();
	}

	/**
	 * Returns a list only containing the aliases for this annotation which
	 * have been declared within this very manifest.
	 * If there are no local aliases defined for this manifest the method
	 * should return an empty list.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<String> getLocalAliases() {
		LazyCollection<String> result = LazyCollection.lazyList();

		forEachLocalAlias(result);

		return result.getAsList();
	}

	/**
	 * Tells whether or not this annotation accepts values outside the predefined
	 * ones obtainable via the following methods:
	 * <p>
	 * <ul>
	 * <li>{@link #getValueRange()}</li>
	 * <li>{@link #getValueSet()}</li>
	 * </ul>
	 * Note that in the case this method returns {@code false} <i>at least one</i>
	 * of those methods {@code must} return a valid object that describes
	 * the bounds of supported values.
	 * <p>
	 * This is <b>not</b> an inheritable property!
	 *
	 * @return {@code true} if and only if this annotation accepts values outside a
	 * predefined and limited set or range.
	 * @see #getValueRange()
	 * @see #getValueSet()
	 * @see #DEFAULT_ALLOW_UNKNOWN_VALUES
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isAllowUnknownValues();

	/**
	 * Returns an object that describes the set of available values for this annotation
	 * by means of a lower and upper bound or an empty {@link Optional} if this annotation is either
	 * unbounded or the values are wrapped into an iterator obtainable via the
	 * {@link #getValueSet()} method. Note that as a convention the {@code ValueRange}
	 * class should only wrap bound objects that implement the {@link Comparable} interface
	 * so that there is an easy way to actually use the bounds provided by the range object.
	 * Since the returned {@code ValueRange} only provides the boundary values, the
	 * {@link #getValueType()} method must be used to determine the type of those bounds.
	 *
	 * @return
	 * @see Comparable
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<ValueRange> getValueRange();

	boolean isLocalValueRange();

	/**
	 * Returns a new iterator to traverse possible values of this annotation or
	 * an empty {@link Optional} if the set of possible annotations is unbounded. Note that
	 * for very large sets of values (especially numerical), it is far cheaper to
	 * use the {@link #getValueRange()} method and return a {@code ValueRange}
	 * object that describes the collection of supported values by means of an
	 * lower and upper bound, instead of generating an iterator that traverses all
	 * the values one by one.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<ValueSet> getValueSet();

	boolean isLocalValueSet();

	/**
	 * Returns the type of this annotation.
	 *
	 * @throws ManifestException of type {@link GlobalErrorCode#ILLEGAL_STATE}
	 * if the value type is not set.
	 */
	@AccessRestriction(AccessMode.READ)
	ValueType getValueType();

	boolean isLocalValueType();

	/**
	 * For primitive annotation types ({@code int}, {@code float}, etc...) this method
	 * returns the wrapped primitive value that is to be treated as a hint for missing
	 * annotation values. For non-primitive annotations it typically returns
	 * an empty {@link Optional}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<Object> getNoEntryValue();

	boolean isLocalNoEntryValue();

	/**
	 * For annotations of type {@value ValueType#CUSTOM} this method returns the
	 * required {@code ContentType}. For all other value types, the returned value
	 * is an empty {@link Optional}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<ContentType> getContentType();

	boolean isLocalContentType();


	// Modification methods

	AnnotationManifest setKey(String key);

	AnnotationManifest addAlias(String alias);

	AnnotationManifest removeAlias(String alias);

	AnnotationManifest setValueRange(@Nullable ValueRange range);

	AnnotationManifest setValueSet(@Nullable ValueSet values);

	AnnotationManifest setValueType(ValueType valueType);

	AnnotationManifest setContentType(@Nullable ContentType contentType);

	AnnotationManifest setNoEntryValue(@Nullable Object noEntryValue);

	AnnotationManifest setAllowUnknownValues(boolean allowUnknownValues);
}
