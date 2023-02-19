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
package de.ims.icarus2.model.standard.highlight;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * Implements an immutable {@link AnnotationLink} that maintains
 * a strong reference to the respective {@link AnnotationLayer}.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationLink.class)
public class SimpleAnnotationLink implements AnnotationLink {

	private final AnnotationLayer layer;
	private final String key;

	public SimpleAnnotationLink(AnnotationLayer layer, String key) {
		requireNonNull(layer);
		requireNonNull(key);

		this.layer = layer;
		this.key = key;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} else if(obj instanceof AnnotationLink) {
			AnnotationLink other = (AnnotationLink) obj;
			return getLayer()==other.getLayer() && getKey().equals(other.getKey());
		}

		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getLayer().hashCode()*getKey().hashCode();
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink#getLayer()
	 */
	@Override
	public AnnotationLayer getLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}

}
