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
package de.ims.icarus2.model.standard.highlight;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import de.ims.icarus2.model.api.highlight.HighlightInfo;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.util.CompactProperties;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public class SimpleHighlightInfo implements HighlightInfo {

	private final HighlightLayer layer;
	private final Identity identity;
	private final DataSet<AnnotationLink> annotations;
	private final CompactProperties properties;

	public SimpleHighlightInfo(HighlightLayer layer, Identity identity) {
		this(layer, identity, null, null);
	}

	public SimpleHighlightInfo(HighlightLayer layer, Identity identity,
			DataSet<AnnotationLink> annotations) {
		this(layer, identity, annotations, null);
	}

	public SimpleHighlightInfo(HighlightLayer layer, Identity identity,
			DataSet<AnnotationLink> annotations, CompactProperties properties) {
		requireNonNull(layer);
		requireNonNull(identity);

		this.layer = layer;
		this.identity = identity;
		this.annotations = annotations;
		this.properties = properties;
	}

	/**
	 * @see de.ims.icarus2.util.id.Identifiable#getIdentity()
	 */
	@Override
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo#getAffectedAnnotations()
	 */
	@Override
	public DataSet<AnnotationLink> getAffectedAnnotations() {
		return annotations;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo#getLayer()
	 */
	@Override
	public HighlightLayer getLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String name) {
		return properties==null ? null : properties.get(name);
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo#forEachProperty(java.util.function.BiConsumer)
	 */
	@Override
	public void forEachProperty(BiConsumer<? super String, ? super Object> action) {
		if(properties!=null) {
			properties.forEachEntry(action);
		}
	}

	/**
	 * @return the properties
	 */
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> result = null;
		if(properties!=null) {
			result = properties.asMap();
		}

		if(result==null) {
			result = Collections.emptyMap();
		}

		return result;
	}
}
