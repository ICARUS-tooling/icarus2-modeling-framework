/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlScope extends IqlAliasedReference {

	/**
	 * Indicates that the primary layer of this scope is meant to be
	 * used as a primary layer in the query result.
	 */
	@JsonProperty(IqlProperties.PRIMARY)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean primary = false;

	/**
	 * Defines the members of this scope.
	 */
	@JsonProperty(IqlProperties.LAYERS)
	private List<IqlLayer> layers = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.SCOPE;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlAliasedReference#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCollectionNotEmpty(layers, IqlProperties.LAYERS);
	}

	public boolean isPrimary() { return primary; }

	public List<IqlLayer> getLayers() { return CollectionUtils.unmodifiableListProxy(layers); }

	public void setPrimary(boolean primary) { this.primary = primary; }

	public void addLayer(IqlLayer layer) { layers.add(requireNonNull(layer)); }

	public void forEachLayer(Consumer<? super IqlLayer> action) { layers.forEach(action); }
}
