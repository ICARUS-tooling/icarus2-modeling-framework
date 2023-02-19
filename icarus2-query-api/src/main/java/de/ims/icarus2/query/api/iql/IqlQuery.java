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
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQuery extends IqlUnique {

	/**
	 * Specifies the IQL dialect to be used. When not defined, the framework
	 * will default to {@value IqlConstants#DEFAULT_VERSION}.
	 */
	@JsonProperty(IqlTags.DIALECT)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> dialect = Optional.empty();

	/**
	 * Import declarations for additional extensions or feature sets.
	 */
	@JsonProperty(IqlTags.IMPORTS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlImport> imports = new ArrayList<>();

	/**
	 * Configuration of properties and/or switches.
	 */
	@JsonProperty(IqlTags.SETUP)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlProperty> setup = new ArrayList<>();

	/**
	 * Basic definition of the streams/corpora to be used for this query
	 */
	@JsonProperty(value=IqlTags.STREAMS, required=true)
	private final List<IqlStream> streams = new ArrayList<>();

	//TODO add entries for pre and post processing of data (outside of result processing)

	/**
	 * Binary data required for the query. Each embedded data
	 * chunk is assigned a variable that can be used to access
	 * it from inside the query payload.
	 * <p>
	 * Note that those variables are implicitly read-only!
	 */
	@JsonProperty(IqlTags.EMBEDDED_DATA)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlData> embeddedData = new ArrayList<>();

	@Override
	public IqlType getType() { return IqlType.QUERY; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCollectionNotEmpty(streams, IqlTags.STREAMS);

		checkOptionalStringNotEmpty(dialect, "dialect");
		checkCollection(imports);
		checkCollection(setup);
		checkCollection(embeddedData);
	}


	public Optional<String> getDialect() { return dialect; }

	public List<IqlImport> getImports() { return CollectionUtils.unmodifiableListProxy(imports); }

	public List<IqlProperty> getSetup() { return CollectionUtils.unmodifiableListProxy(setup); }

	public List<IqlStream> getStreams() { return CollectionUtils.unmodifiableListProxy(streams); }

	public List<IqlData> getEmbeddedData() { return CollectionUtils.unmodifiableListProxy(embeddedData); }


	public void setDialect(String dialect) { this.dialect = Optional.of(checkNotEmpty(dialect)); }

	public void addImport(IqlImport imp) { imports.add(requireNonNull(imp)); }

	public void addSetup(IqlProperty property) { setup.add(requireNonNull(property)); }

	public void addStream(IqlStream stream) { streams.add(requireNonNull(stream)); }

	public void addEmbeddedData(IqlData data) { embeddedData.add(requireNonNull(data)); }

	//TODO add the 'forEach' style methods for all list properties

	public void forEachImport(Consumer<? super IqlImport> action) { imports.forEach(action); }

	public void forEachSetup(Consumer<? super IqlProperty> action) { setup.forEach(action); }

	public void forEachStream(Consumer<? super IqlStream> action) { streams.forEach(action); }

	public void forEachEmbeddedData(Consumer<? super IqlData> action) { embeddedData.forEach(action); }

	// Utility methods

	/** Returns whether this query is meant to evaluate multiple independent data streams. */
	public boolean isMultiStream() { return streams.size()>1; }

	public Optional<IqlProperty> getProperty(String key) {
		checkNotEmpty(key);
		// We expect setup list to be rather small, so linear search shouldn't be an issue
		for (IqlProperty property : setup) {
			if(key.equals(property.getKey())) {
				return Optional.of(property);
			}
		}
		return Optional.empty();
	}

	/**
	 * Shorthand method for {@link #isSwitchSet(String)} using the {@link QuerySwitch#getKey() key}
	 * of the specified {@link QuerySwitch} enum.
	 * @param qs
	 * @return
	 */
	public boolean isSwitchSet(QuerySwitch qs) { return isSwitchSet(qs.getKey()); }

	/**
	 * Returns {@code true} iff a switch property with the specified {@link IqlProperty#getKey() key}
	 * is present in this query.
	 * @param key
	 * @return
	 * @throws QueryException of type {@link GlobalErrorCode#INVALID_INPUT} in case a property is
	 * present for the specified key but it is not a proper switch, i.e. it has an actual value
	 * associated with it.
	 */
	public boolean isSwitchSet(String key) {
		Optional<IqlProperty> property = getProperty(key);
		if(property.map(IqlProperty::getValue).isPresent())
			throw new QueryException(GlobalErrorCode.INVALID_INPUT,
					"Specified property is not a switch: "+key);
		return property.isPresent();
	}
}
