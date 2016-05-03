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

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import de.ims.icarus2.util.Flag;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 * @version $Id$
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
	 * Returns whether this layer only provides annotations for the members
	 * of the respective {@link ItemLayer}'s direct container. A return
	 * value of {@code true} indicates that this layer may be queried for
	 * annotations of nested containers or structures/edges.
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

	private static Map<String, AnnotationFlag> xmlLookup;

	public static AnnotationFlag parseAnnotationFlag(String s) {
		if(xmlLookup==null) {
			Map<String, AnnotationFlag> map = new HashMap<>();
			for(AnnotationFlag type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}

}
