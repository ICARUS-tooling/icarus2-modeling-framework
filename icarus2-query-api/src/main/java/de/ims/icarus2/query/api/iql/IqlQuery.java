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

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	@JsonProperty(IqlProperties.DIALECT)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> dialect = Optional.empty();

	/**
	 * Import declarations for additional extensions or feature sets.
	 */
	@JsonProperty(IqlProperties.IMPORTS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlImport> imports = new ArrayList<>();

	/**
	 * Configuration of properties and/or switches.
	 */
	@JsonProperty(IqlProperties.SETUP)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlProperty> setup = new ArrayList<>();

	/**
	 * Basic definition of the corpora to be used for this query
	 */
	@JsonProperty(value=IqlProperties.CORPORA, required=true)
	private final List<IqlCorpus> corpora = new ArrayList<>();

	/**
	 * Vertical filtering via declaring a subset of the entire
	 * layer graph defined by the {@link #corpora} list.
	 */
	@JsonProperty(IqlProperties.LAYERS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlLayer> layers = new ArrayList<>();

	/**
	 * More fine-grained vertical filtering than using the
	 * {@link #layers} list. Takes priority over {@link #layers}
	 * when both are present.
	 */
	@JsonProperty(IqlProperties.SCOPES)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlScope> scopes = new ArrayList<>();

	//TODO add entries for pre and post processing of data (outside of result processing)

	/**
	 * The raw unprocessed query payload as provided by the user.
	 */
	@JsonProperty(value=IqlProperties.RAW_PAYLOAD, required=true)
	private final List<String> rawPayload = new ArrayList<>();

	/**
	 * The processed query payload after being parsed by the
	 * query engine.
	 */
	@JsonProperty(IqlProperties.PAYLOAD)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlPayload> payload = new ArrayList<>();

	/**
	 * The raw unprocessed grouping definitions if provided.
	 */
	@JsonProperty(IqlProperties.RAW_GROUPING)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> rawGrouping = Optional.empty();

	/**
	 * The processed grouping definitions if {@link #rawGrouping}
	 * was set.
	 */
	@JsonProperty(IqlProperties.GROUPING)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlGroup> grouping = new ArrayList<>();

	/**
	 * The raw result processing instructions if provided.
	 */
	@JsonProperty(IqlProperties.RAW_RESULT)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> rawResult = Optional.empty();

	@JsonProperty(IqlProperties.RESULT)
	private IqlResult result;

	/**
	 * Binary data required for the query. Each embedded data
	 * chunk is assigned a variable that can be used to access
	 * it from inside the query payload.
	 */
	@JsonProperty(IqlProperties.EMBEDDED_DATA)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlData> embeddedData = new ArrayList<>();

	@JsonIgnore
	private boolean processed = false;

	@Override
	public IqlType getType() { return IqlType.QUERY; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCollectionNotEmpty(corpora, "corpora");
		checkCondition(!rawPayload.isEmpty(), "rawPayload", "Must define at least 1 query payload");
		checkNestedNotNull(result, "result");

		checkOptionalStringNotEmpty(dialect, "dialect");
		checkOptionalStringNotEmpty(rawGrouping, "rawGrouping");
		checkOptionalStringNotEmpty(rawResult, "rawResult");
		checkCollection(payload);
		checkCollection(imports);
		checkCollection(setup);
		checkCollection(layers);
		checkCollection(scopes);
		checkCollection(embeddedData);
		checkCollection(grouping);
	}

	public boolean isProcessed() { return processed; }

	public void markProcessed() {
		checkState("ALready processed", !processed);
		processed = true;
	}


	public Optional<String> getDialect() { return dialect; }

	public List<IqlImport> getImports() { return CollectionUtils.unmodifiableListProxy(imports); }

	public List<IqlProperty> getSetup() { return CollectionUtils.unmodifiableListProxy(setup); }

	public List<IqlCorpus> getCorpora() { return CollectionUtils.unmodifiableListProxy(corpora); }

	public List<IqlLayer> getLayers() { return CollectionUtils.unmodifiableListProxy(layers); }

	public List<IqlScope> getScopes() { return CollectionUtils.unmodifiableListProxy(scopes); }

	public List<String> getRawPayload() { return CollectionUtils.unmodifiableListProxy(rawPayload); }

	public List<IqlPayload> getPayload() { return CollectionUtils.unmodifiableListProxy(payload); }

	public Optional<String> getRawGrouping() { return rawGrouping; }

	public List<IqlGroup> getGrouping() { return CollectionUtils.unmodifiableListProxy(grouping); }

	public List<IqlData> getEmbeddedData() { return CollectionUtils.unmodifiableListProxy(embeddedData); }

	public Optional<String> getRawResult() { return rawResult; }

	public IqlResult getResult() { return result; }


	public void setDialect(String dialect) { this.dialect = Optional.of(checkNotEmpty(dialect)); }

	public void addRawPayload(String rawPayload) { this.rawPayload.add(checkNotEmpty(rawPayload)); }

	public void addPayload(IqlPayload payload) { this.payload.add(requireNonNull(payload)); }

	public void setRawGrouping(String rawGrouping) { this.rawGrouping = Optional.of(checkNotEmpty(rawGrouping)); }

	public void addImport(IqlImport imp) { imports.add(requireNonNull(imp)); }

	public void addSetup(IqlProperty property) { setup.add(requireNonNull(property)); }

	public void addCorpus(IqlCorpus corpus) { corpora.add(requireNonNull(corpus)); }

	public void addLayer(IqlLayer layer) { layers.add(requireNonNull(layer)); }

	public void addScope(IqlScope scope) { scopes.add(requireNonNull(scope)); }

	public void addGrouping(IqlGroup group) { grouping.add(requireNonNull(group)); }

	public void addEmbeddedData(IqlData data) { embeddedData.add(requireNonNull(data)); }

	public void setRawResult(String rawResult) { this.rawResult = Optional.of(checkNotEmpty(rawResult)); }

	public void setResult(IqlResult result) { this.result = requireNonNull(result); }

	//TODO add the 'forEach' style methods for all list properties

	// Utility methods

	/** Returns whether this query is meant to evaluate multiple independent data streams. */
	public boolean isMultiStream() {
		return rawPayload.size()>1;
	}

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
	public boolean isSwitchSet(QuerySwitch qs) {
		return isSwitchSet(qs.getKey());
	}

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
