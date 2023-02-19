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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;

import de.ims.icarus2.query.api.engine.CorpusData.LayerRef;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ElementInfo {
	private final IqlProperElement element;
	private final List<LayerRef> layers;
	private final TypeInfo type;

	public ElementInfo(IqlProperElement element, TypeInfo type, List<LayerRef> layers) {
		this.element = requireNonNull(element);
		this.type = requireNonNull(type);
		this.layers = new ObjectArrayList<>(requireNonNull(layers));
		checkArgument(!layers.isEmpty());
	}

	public IqlProperElement getElement() { return element; }
	public List<LayerRef> getLayers() { return layers; }
	public TypeInfo getType() { return type; }

	public boolean isEdge() { return element.getType()==IqlType.EDGE; }
}