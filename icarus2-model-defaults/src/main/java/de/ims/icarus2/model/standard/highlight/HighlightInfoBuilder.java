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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import de.ims.icarus2.model.api.highlight.HighlightInfo;
import de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.util.CompactProperties;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.collections.set.DataSets;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class HighlightInfoBuilder {

	private final HighlightLayer layer;

	// Identity builder part
	private String id, name, description;

	// Fixed identity
	private Identity identity;

	// Annotations builder part
	private final List<AnnotationLink> annotationLinks = new ArrayList<>();

	// Fixed annotations
	private DataSet<AnnotationLink> annotations;

	// Properties builder part
	private final Map<String, Object> propertyMap = new Object2ObjectOpenHashMap<>();

	// Fixed properties
	private CompactProperties properties;

	private final EnumSet<HighlightInfoBuilderOption> options;

	public HighlightInfoBuilder(HighlightLayer layer, EnumSet<HighlightInfoBuilderOption> options) {
		requireNonNull(layer);

		this.layer = layer;

		if(options==null) {
			options = EnumSet.noneOf(HighlightInfoBuilderOption.class);
		}

		this.options = options;
	}

	public HighlightInfoBuilder(HighlightLayer layer, HighlightInfoBuilderOption...options) {
		requireNonNull(layer);

		this.layer = layer;

		EnumSet<HighlightInfoBuilderOption> set = EnumSet.noneOf(HighlightInfoBuilderOption.class);

		if(options!=null && options.length>0) {
			for(HighlightInfoBuilderOption policy : options) {
				set.add(policy);
			}
		}

		this.options = set;
	}

	public HighlightInfoBuilder(HighlightLayer layer) {
		this(layer, (HighlightInfoBuilderOption[])null);
	}

	// direct getters/setters

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Identity getIdentity() {
		return identity;
	}

	public DataSet<AnnotationLink> getAnnotations() {
		return annotations;
	}

	public CompactProperties getProperties() {
		return properties;
	}

	public HighlightInfoBuilder id(String id) {
		this.id = id;

		return this;
	}

	public HighlightInfoBuilder name(String name) {
		this.name = name;

		return this;
	}

	public HighlightInfoBuilder description(String description) {
		this.description = description;

		return this;
	}

	public HighlightInfoBuilder identity(Identity identity) {
		this.identity = identity;

		return this;
	}

	public HighlightInfoBuilder annotations(DataSet<AnnotationLink> annotations) {
		this.annotations = annotations;

		return this;
	}

	public HighlightInfoBuilder properties(CompactProperties properties) {
		this.properties = properties;

		return this;
	}

	// incremental modifiers

	public HighlightInfoBuilder addAnnotation(AnnotationLink annotation) {
		annotationLinks.add(annotation);

		return this;
	}

	public HighlightInfoBuilder addAnnotation(AnnotationLayer annotationLayer, String key) {
		annotationLinks.add(new SimpleAnnotationLink(annotationLayer, key));

		return this;
	}

	public HighlightInfoBuilder addAnnotations(List<? extends AnnotationLink> annotations) {
		annotationLinks.addAll(annotations);

		return this;
	}

	public HighlightInfoBuilder removeAnnotation(AnnotationLink annotation) {
		annotationLinks.remove(annotation);

		return this;
	}

	public HighlightInfoBuilder removeAnnotations(List<? extends AnnotationLink> annotations) {
		annotationLinks.removeAll(annotations);

		return this;
	}

	public HighlightInfoBuilder addProperty(String key, Object value) {
		propertyMap.put(key, value);

		return this;
	}

	public HighlightInfoBuilder removeProperty(String key) {
		propertyMap.remove(key);

		return this;
	}

	public HighlightInfoBuilder addProperties(Map<? extends String, ? extends Object> map) {
		propertyMap.putAll(map);

		return this;
	}

	public HighlightInfoBuilder removeProperties(Map<? extends String, ? extends Object> map) {
		propertyMap.keySet().removeAll(map.keySet());

		return this;
	}

	public HighlightInfoBuilder removeProperties(Collection<? extends String> keys) {
		propertyMap.keySet().removeAll(keys);

		return this;
	}

	public void reset() {
		if(!options.contains(HighlightInfoBuilderOption.RETAIN_IDENTITY)) {
			identity = null;
		}
		if(!options.contains(HighlightInfoBuilderOption.RETAIN_ANNOTATIONS)) {
			annotations = null;
		}
		if(!options.contains(HighlightInfoBuilderOption.RETAIN_PROPERTIES)) {
			properties = null;
		}
		if(options.contains(HighlightInfoBuilderOption.CLEAR_RAW_IDENTITY)) {
			clearRawIdentity();
		}
		if(options.contains(HighlightInfoBuilderOption.CLEAR_RAW_ANNOTATIONS)) {
			clearAnnotations();
		}
		if(options.contains(HighlightInfoBuilderOption.CLEAR_RAW_PROPERTIES)) {
			clearProperties();
		}
	}

	public HighlightInfoBuilder clearRawIdentity() {
		id = name = description = null;

		return this;
	}

	public HighlightInfoBuilder clearAnnotations() {
		annotationLinks.clear();

		return this;
	}

	public HighlightInfoBuilder clearProperties() {
		propertyMap.clear();

		return this;
	}

	private Identity buildIdentity() {
		Identity identity = this.identity;

		if(identity==null) {
			identity = new StaticIdentity(id, name, description);
		}

		return identity;
	}

	private DataSet<AnnotationLink> buildAnnotations() {
		DataSet<AnnotationLink> annotations = this.annotations;

		if(annotations==null && !annotationLinks.isEmpty()) {
			annotations = DataSets.createDataSet(annotationLinks);
		}

		return annotations;
	}

	private CompactProperties buildProperties() {
		CompactProperties properties = this.properties;

		if(properties==null && !propertyMap.isEmpty()) {
			properties = new CompactProperties(propertyMap);
		}

		return properties;
	}

	public HighlightInfoBuilder fixIdentity() {
		identity = buildIdentity();

		return this;
	}

	public HighlightInfoBuilder fixAnnotations() {
		annotations = buildAnnotations();

		return this;
	}

	public HighlightInfoBuilder fixProperties() {
		properties = buildProperties();

		return this;
	}

	public HighlightInfo build() {
		return build(buildIdentity(), buildAnnotations(), buildProperties());
	}

	public HighlightInfo build(Identity identity) {
		return build(identity, buildAnnotations(), buildProperties());
	}

	public HighlightInfo build(Identity identity, DataSet<AnnotationLink> annotations) {
		return build(identity, annotations, buildProperties());
	}

	public HighlightInfo build(Identity identity, DataSet<AnnotationLink> annotations, CompactProperties properties) {
		HighlightInfo info = new SimpleHighlightInfo(layer, identity, annotations, properties);

		reset();

		return info;
	}

	public enum HighlightInfoBuilderOption {
		RETAIN_IDENTITY,
		CLEAR_RAW_IDENTITY,
		RETAIN_ANNOTATIONS,
		CLEAR_RAW_ANNOTATIONS,
		RETAIN_PROPERTIES,
		CLEAR_RAW_PROPERTIES,
		;
	}
}
