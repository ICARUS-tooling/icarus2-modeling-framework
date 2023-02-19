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

import de.ims.icarus2.query.api.engine.CorpusData.LayerRef;
import de.ims.icarus2.query.api.iql.IqlLane;

/**
 *
 * @author Markus Gärtner
 *
 */
public class LaneInfo {
	private final IqlLane lane;
	private final LayerRef layer;
	private final TypeInfo type;

	public LaneInfo(IqlLane lane, TypeInfo type, LayerRef layer) {
		this.lane = requireNonNull(lane);
		this.type = requireNonNull(type);
		this.layer = requireNonNull(layer);
	}

	/** The original lane declaration inside the query */
	public IqlLane getLane() { return lane; }
	/** Pointer to the raw layer in the corpus data */
	public LayerRef getLayer() { return layer; }
	/** Type of the layer, e.g. {@link TypeInfo#ITEM_LAYER} */
	public TypeInfo getType() { return type; }
}