/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.awt.Container;

import de.ims.icarus2.util.Flag;
import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public enum AnnotationFlag implements StringResource, Flag {

	/**
	 * Returns whether or not search operations on this layer are supported.
	 * For most layers this method will returns {@code true} but there are types
	 * of data for which searching is a non-trivial task and not easily implemented.
	 * For example an annotation layer containing web links to wikipedia articles or
	 * audio recordings of human speakers would most likely decide to not support
	 * searches.
	 */
	SEARCHABLE("searchable"),

	/**
	 * Defines if it is possible to build an index for the content of a layer.
	 * This is of course only of importance if the layer in question actually
	 * supports search operations as defined via the {@link #isSearchable()}
	 * method.
	 * <p>
	 * Note that this optional restriction only applies to annotation layers,
	 * since {@link ItemLayer}s and derived versions are always indexable and rely
	 * on {@link MappingManifest} declarations to express what types of indices
	 * are available.
	 */
	INDEXABLE("indexable"),

	/**
	 * Defines whether a layer only provides annotations for the members
	 * of the respective {@link ItemLayer}'s direct container. If present
	 * indicates that a layer may be queried for annotations of nested
	 * containers or structures/edges.
	 * <p>
	 * This is a generalized version of {@link #EDGE_ANNOTATION} or {@link #ELEMENT_ANNOTATION}.
	 */
	DEEP_ANNOTATION("deep-annotation"),

	/**
	 * A special case of {@link #DEEP_ANNOTATION} only applicable when the
	 * target layer contains {@link Container} or {@link Structure} objects
	 * as top level targets for this annotation layer. The flag indicates that
	 * annotations are <i>only</i> meant for the direct elements of those
	 * containers!
	 * <p>
	 * Typically this is the case when targeting edges or nodes in a structure.
	 *
	 * @see #EDGE_ANNOTATION
	 * @see #NODE_ANNOTATION
	 * @see #DEEP_ANNOTATION
	 */
	ELEMENT_ANNOTATION("element-annotation"),

	/**
	 * A special case of {@link #ELEMENT_ANNOTATION} that signals that an
	 * annotation is only meant for the edges of a certain structure.
	 *
	 * @see #ELEMENT_ANNOTATION
	 * @see #NODE_ANNOTATION
	 * @see #DEEP_ANNOTATION
	 */
	EDGE_ANNOTATION("edge-annotation"),

	/**
	 * A special case of {@link #ELEMENT_ANNOTATION} that signals that an
	 * annotation is only meant for the nodes of a certain structure.
	 *
	 * @see #ELEMENT_ANNOTATION
	 * @see #EDGE_ANNOTATION
	 * @see #DEEP_ANNOTATION
	 */
	NODE_ANNOTATION("node-annotation"),

	/**
	 * Defines whether an {@code AnnotationLayer} derived from this manifest should
	 * be able to handle keys that have not been declared within a nested
	 * {@link AnnotationManifest}.
	 * <p>
	 * Note that when a format allows arbitrary properties on the annotation level
	 * and therefore decides to allow those <i>unknown keys</i> it loses some of
	 * the robustness a finite declaration of supported keys and their values provides!
	 */
	UNKNOWN_KEYS("unknown-keys"),

	;
	private final String xmlForm;

	private AnnotationFlag(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	private static LazyStore<AnnotationFlag, String> store = LazyStore.forStringResource(
			AnnotationFlag.class, true);

	public static AnnotationFlag parseAnnotationFlag(String s) {
		return store.lookup(s);
	}

}
