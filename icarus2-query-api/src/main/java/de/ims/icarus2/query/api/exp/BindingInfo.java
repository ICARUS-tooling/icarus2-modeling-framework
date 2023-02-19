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
/**
 *
 */
package de.ims.icarus2.query.api.exp;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.query.api.engine.CorpusData.LayerRef;

/**
 *
 * @author Markus Gärtner
 *
 */
public class BindingInfo {
	private final LayerRef layer;
	private final TypeInfo type;

	public BindingInfo(LayerRef layer, TypeInfo type) {
		this.layer = requireNonNull(layer);
		this.type = requireNonNull(type);
	}

	/** The layer this binding refers to */
	public LayerRef getLayer() { return layer; }

	/** The element type (one of the member related ones) of the associated layer. */
	public TypeInfo getType() { return type; }

	/** In case of {@link StructureLayer}s, bindings can target edges */
	public boolean isEdges() { return type==TypeInfo.EDGE; }
}