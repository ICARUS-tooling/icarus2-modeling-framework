/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * Models all the filtering definitions for querying a single data stream/corpus.
 *
 * @author Markus Gärtner
 *
 */
public class IqlStream extends IqlUnique {

	/**
	 * Indicates that the primary layer of this stream is meant to be
	 * used as a primary layer in the global query result.
	 */
	@JsonProperty(IqlTags.PRIMARY)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean primary = false;

	/**
	 * The source of data for this stream.
	 */
	@JsonProperty(value=IqlTags.CORPUS, required=true)
	private IqlCorpus corpus;

	/**
	 * Vertical filtering via declaring a subset of the entire
	 * layer graph defined by the {@link #corpora} list.
	 */
	@JsonProperty(IqlTags.LAYERS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlLayer> layers = new ArrayList<>();

	/**
	 * More fine-grained vertical filtering than using the
	 * {@link #layers} list. Takes priority over {@link #layers}
	 * when both are present.
	 */
	@JsonProperty(IqlTags.SCOPE)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<IqlScope> scope = Optional.empty();

	/**
	 * The raw unprocessed query payload as provided by the user.
	 */
	@JsonProperty(value=IqlTags.RAW_PAYLOAD)
	private Optional<String> rawPayload = Optional.empty();

	/**
	 * The processed query payload after being parsed by the query engine.
	 */
	@JsonProperty(IqlTags.PAYLOAD)
	@JsonInclude(Include.NON_EMPTY)
	private Optional<IqlPayload> payload = Optional.empty();

	/**
	 * The raw unprocessed grouping definitions if provided.
	 */
	@JsonProperty(IqlTags.RAW_GROUPING)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> rawGrouping = Optional.empty();

	/**
	 * The processed grouping definitions if {@link #rawGrouping}
	 * was set or if it was provided directly.
	 */
	@JsonProperty(IqlTags.GROUPING)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlGroup> grouping = new ArrayList<>();

	/**
	 * The raw result processing instructions if provided.
	 * <p>
	 * Note that all instructions that affect the size or sorting of a
	 * result can only be used for the primary stream in a query. Not
	 * honoring this limitation will cause an exception on evaluation
	 * time as those types of checks are outside the scope of this layer.
	 */
	@JsonProperty(IqlTags.RAW_RESULT)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> rawResult = Optional.empty();

	/**
	 * The processed collection of result parameters if {@link #rawResult}
	 * was set or if provided directly.
	 * The minimal result specification is a declaration of the desired
	 * result type.
	 */
	@JsonProperty(value=IqlTags.RESULT, required=true)
	private IqlResult result;

	/**
	 * Status marker used by the evaluation engine to signal that the
	 * query has already been processed from its raw state into fully
	 * parsed content.
	 */
	//TODO expose to API?
	@JsonIgnore
	private boolean processed = false;


	@Override
	public IqlType getType() { return IqlType.STREAM; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNestedNotNull(corpus, IqlTags.CORPUS);
		checkNestedNotNull(result, "result");

		checkCollection(layers);
		checkCollection(grouping);
		checkOptionalNested(scope);
		checkOptionalNested(payload);
		checkOptionalStringNotEmpty(rawPayload, IqlTags.RAW_PAYLOAD);
		checkOptionalStringNotEmpty(rawGrouping, IqlTags.RAW_GROUPING);
		checkOptionalStringNotEmpty(rawResult, IqlTags.RAW_RESULT);

		checkCondition(rawPayload.isPresent() || payload.isPresent(),
				IqlTags.PAYLOAD, "Must either define a raw payload or provide fully processed payload element");
	}

	public boolean isProcessed() { return processed; }

	public void markProcessed() {
		checkState("ALready processed", !processed);
		processed = true;
	}


	public boolean isPrimary() { return primary; }

	public IqlCorpus getCorpus() { return corpus; }

	public List<IqlLayer> getLayers() { return CollectionUtils.unmodifiableListProxy(layers); }

	public Optional<IqlScope> getScope() { return scope; }

	public Optional<String> getRawPayload() { return rawPayload; }

	public Optional<IqlPayload> getPayload() { return payload; }

	public Optional<String> getRawGrouping() { return rawGrouping; }

	public List<IqlGroup> getGrouping() { return CollectionUtils.unmodifiableListProxy(grouping); }

	public Optional<String> getRawResult() { return rawResult; }

	public IqlResult getResult() { return result; }


	public void setPrimary(boolean primary) { this.primary = primary; }

	public void setRawPayload(String rawPayload) { this.rawPayload = Optional.of(rawPayload); }

	public void setPayload(IqlPayload payload) { this.payload = Optional.of(payload); }

	public void setCorpus(IqlCorpus corpus) { this.corpus = requireNonNull(corpus); }

	public void addLayer(IqlLayer layer) { layers.add(requireNonNull(layer)); }

	public void setScope(IqlScope scope) { this.scope = Optional.of(scope); }

	public void setRawGrouping(String rawGrouping) { this.rawGrouping = Optional.of(checkNotEmpty(rawGrouping)); }

	public void addGrouping(IqlGroup group) { grouping.add(requireNonNull(group)); }

	public void setRawResult(String rawResult) { this.rawResult = Optional.of(checkNotEmpty(rawResult)); }

	public void setResult(IqlResult result) { this.result = requireNonNull(result); }

}
