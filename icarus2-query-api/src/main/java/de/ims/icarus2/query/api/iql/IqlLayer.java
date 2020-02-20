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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlLayer extends IqlAliasedReference {

	/**
	 * Indicates that this layer is meant to be used as a primary layer
	 * in the query result.
	 */
	@JsonProperty(IqlProperties.PRIMARY)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean primary = false;

	/**
	 * When this layer definition is used inside a {@link IqlScope}, effectively
	 * adds the entire member-subgraph of this layer to the scope.
	 *
	 * Note: This property is redundant when the layer is part of the regular
	 * {@link IqlQuery#layers} declaration, as in that case all member subgraphs
	 * for each layer are already being added to the global scope!
	 */
	@JsonProperty(IqlProperties.ALL_MEMBERS)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean allMembers = false;

	@Override
	public IqlType getType() { return IqlType.LAYER; }

	public boolean isPrimary() { return primary; }

	public boolean isAllMembers() { return allMembers; }

	public void setPrimary(boolean primary) { this.primary = primary; }

	public void setAllMembers(boolean allMembers) { this.allMembers = allMembers; }

}
