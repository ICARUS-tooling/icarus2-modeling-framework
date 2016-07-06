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

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
public enum ManifestErrorCode implements ErrorCode {

	//**************************************************
	//       2xx  IMPLEMENTATION ERRORS
	//**************************************************

	/**
	 * A general error regarding a custom implementation declaration
	 */
	IMPLEMENTATION_ERROR(200),

	/**
	 * Instantiating a corpus member failed due to lack of a valid
	 * {@link ImplementationManifest} being present.
	 */
	IMPLEMENTATION_MISSING(201),

	/**
	 * The result of a member instantiation according to some foreign
	 * {@link ImplementationManifest} failed because the returned object
	 * was not assignment compatible with the required result class.
	 */
	IMPLEMENTATION_INCOMPATIBLE(202),

	/**
	 * A {@link ImplementationManifest} declared a class to be used for
	 * instantiation that could not be found. Remember that foreign
	 * implementations must be declared via extensions in a plugin manifest!
	 */
	IMPLEMENTATION_NOT_FOUND(203),

	/**
	 * A {@link ImplementationManifest} declared a class to be used for
	 * instantiation that was not accessible by the framework. Reasons
	 * might be the accidental restriction of the default constructor to
	 * have a {@code protected} modifier or not to provide a no-args
	 * constructor at all.
	 */
	IMPLEMENTATION_NOT_ACCESSIBLE(204),

	/**
	 * Delegating the actual instantiation process to a {@link ImplementationManifest.Factory factory}
	 * implementation failed. This error wraps all the exceptions declared by the factory's
	 * {@link ImplementationManifest.Factory#create(Class, de.ims.icarus2.model.manifest.api.MemberManifest) create}
	 * method.
	 */
	IMPLEMENTATION_FACTORY(205),

	//**************************************************
	//       3xx  PREREQUISITE ERRORS
	//**************************************************

	/**
	 * A general error regarding a prerequisite declaration
	 */
	PREREQUISITE_ERROR(300),

	/**
	 * Obtaining a layer (for example via {@link TargetLayerManifest#getResolvedLayerManifest()}
	 * failed, because the underlying {@link PrerequisiteManifest} has not yet been resolved to
	 * an actual target layer. This means that the prerequisite is lacking the required qualities
	 * (context and layer id) to be counted as resolved.
	 * <p>
	 * Note that for resolved prerequisites that contain invalid (i.e. non-existent) targets, the
	 * {@link #PREREQUISITE_INVALID} error should be used.
	 */
	PREREQUISITE_UNRESOLVED(301),

	/**
	 * A prerequisite presents the required qualities to count as resolved (this means it declared
	 * both a context and layer id) but one of those ids is invalid in the sense that it does not
	 * point to an existing target (e.g. the target context has been removed or a typo occurred when
	 * generating the context manifest manually).
	 */
	PREREQUISITE_INVALID(302),

	/**
	 * A prerequisite presents the required qualities to count as resolved (this means it declared
	 * both a context and layer id) but the target layer it got resolved to is of an incompatible type
	 * (e.g. it references an annotation layer but was meant to point to a markable layer).
	 * This error should be pretty rare, since it indicates a prior mistake in the framework when
	 * possible resolution targets have been collected.
	 */
	PREREQUISITE_INCOMPATIBLE(303),

	//**************************************************
	//       6xx  MANIFEST ERRORS
	//**************************************************

	/**
	 * A general error regarding a manifest
	 */
	MANIFEST_ERROR(600),

	/**
	 * Two or more elements in a manifest definition used the same id within a single namespace
	 */
	MANIFEST_DUPLICATE_ID(601),

	/**
	 * The reference via id to another resource is invalid due to the id being unknown.
	 */
	MANIFEST_UNKNOWN_ID(602),

	/**
	 * An attempt was made to declare an unsupported manifest template state.
	 * In certain situations a manifest will be expected to
	 */
	MANIFEST_ILLEGAL_TEMPLATE_STATE(603),

	/**
	 * A set of templates form a cyclic relation.
	 */
	MANIFEST_CYCLIC_TEMPLATE(604),

	/**
	 * A manifest is referencing a template of a foreign manifest type.
	 */
	MANIFEST_INCOMPATTIBLE_TEMPLATE(605),

	/**
	 * Signals that a given operation is not possible since the manifest in question requires another
	 * manifest instance surrounding it. For example certain manifests (like {@link ContextManifest},
	 * {@link LayerManifest}, etc..) cannot be used without their respective environments ({@link CorpusManifest},
	 * {@link LayerGroupManifest}, respectively) in a live state.
	 */
	MANIFEST_MISSING_ENVIRONMENT(606),

	/**
	 * A manifest that requires value type information for its content (like annotations) is missing
	 * that declaration.
	 */
	MANIFEST_MISSING_TYPE(607),

	/**
	 * A manifest that relies on external resources is missing the location declaration for those resources.
	 */
	MANIFEST_MISSING_LOCATION(608),

	/**
	 * A requested mapping between 2 layers could not be found
	 */
	MANIFEST_MISSING_MAPPING(609),

	/**
	 * Some value (annotation, property, option, ...) declared in a manifest is incompatible
	 * with the respective value type specified in the context of that value.
	 */
	MANIFEST_TYPE_CAST(620),

	/**
	 * A value type definition in a manifest cannot be resolved to an actual {@link ValueType} implementation.
	 */
	MANIFEST_UNKNOWN_TYPE(621),

	/**
	 * A value type definition in a manifest is considered illegal by the respective context of its declaration.
	 * Certain manifests might restrict the actual value types that are allowed to be used, e.g. a {@link ValueRange}
	 * typically cannot deal with types that do not implement the {@link Comparable} interface and therefore will
	 * throw an exception.
	 */
	MANIFEST_UNSUPPORTED_TYPE(622),

	/**
	 * A given manifest member was supposed to be hosted within another than his actual environment.
	 */
	MANIFEST_INVALID_ENVIRONMENT(623),

	/**
	 * TODO
	 */
	MANIFEST_CORRUPTED_STATE(624),

	/**
	 * A given {@code id} string is unfit to be used as an identifier for a manifest object, either by being
	 * {@code null} or not being conform to the basic id format as specified by {@link CorpusUtils#isValidId(String)}.
	 */
	MANIFEST_INVALID_ID(625),

	/**
	 * A given {@link Manifest} template is locked for modification due to being actively used in a live corpus.
	 */
	MANIFEST_LOCKED(626),

	/**
	 * A given {@link Manifest} does not support templating but was declared within a {@link ManifestLocation#isTemplate() template}
	 * {@link ManifestLocation location}.
	 */
	MANIFEST_INVALID_LOCATION(627),

	;

	private final int code;

	private ManifestErrorCode(int code) {
		this.code = code;
	}

	/**
	 * @see de.ims.icarus2.ErrorCode#code()
	 */
	@Override
	public int code() {
		return code;
	}
}
