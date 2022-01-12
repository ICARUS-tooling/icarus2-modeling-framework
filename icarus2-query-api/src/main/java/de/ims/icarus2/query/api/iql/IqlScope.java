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
/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlScope extends IqlUnique {

	/**
	 * Defines the members of this scope.
	 */
	@JsonProperty(value=IqlTags.LAYERS, required=true)
	private List<IqlLayer> layers = new ArrayList<>();

	@Override
	public IqlType getType() { return IqlType.SCOPE; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCollectionNotEmpty(layers, IqlTags.LAYERS);
	}

	public List<IqlLayer> getLayers() { return CollectionUtils.unmodifiableListProxy(layers); }

	public void addLayer(IqlLayer layer) { layers.add(requireNonNull(layer)); }

	public void forEachLayer(Consumer<? super IqlLayer> action) { layers.forEach(action); }
}
