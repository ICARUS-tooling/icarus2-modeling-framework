/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
