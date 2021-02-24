/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.ref.WeakReference;

import de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink;
import de.ims.icarus2.model.api.layer.AnnotationLayer;

/**
 * @author Markus Gärtner
 *
 */
public class WeakAnnotationLink extends WeakReference<AnnotationLayer> implements AnnotationLink {

	private final String key;

	/**
	 * @param layer
	 */
	public WeakAnnotationLink(AnnotationLayer layer, String key) {
		super(layer);

		requireNonNull(key);

		this.key = key;
	}

	/**
	 * @throws IllegalStateException in case the {@link AnnotationLayer layer} has already been
	 * gc'd.
	 *
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink#getLayer()
	 */
	@Override
	public AnnotationLayer getLayer() {
		AnnotationLayer layer = get();

		checkState("Layer invalid", layer!=null);

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
