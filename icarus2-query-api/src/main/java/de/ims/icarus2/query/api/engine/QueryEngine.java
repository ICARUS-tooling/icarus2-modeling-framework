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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.query.api.iql.IqlUtils.createMapper;
import static de.ims.icarus2.query.api.iql.IqlUtils.fragment;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.query.api.Query;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.util.AbstractBuilder;

/**
 * Central entry point for querying corpora.
 *
 * @author Markus Gärtner
 *
 */
public class QueryEngine {

	public static Builder builder() {
		return new Builder();
	}

	private final ObjectMapper mapper;

	private final CorpusManager corpusManager;

	private QueryEngine(Builder builder) {
		builder.validate();

		mapper = builder.getMapper();
		corpusManager = builder.getCorpusManager();
	}

	public QueryJob query(Query rawQuery) {

		// Read and parse query content
		IqlQuery query = readQuery(rawQuery);
		new QueryProcessor().parseQuery(query);

		// Ensure we only ever consider validated queries
		query.checkIntegrity();

		throw new UnsupportedOperationException();
	}

	/**
	 * Parses a raw JSON query into an {@link IqlQuery} instance,
	 * but does <b>not</b> parse the embedded textual representations
	 * for query payload, grouping and query instructions.
	 */
	@VisibleForTesting
	IqlQuery readQuery(Query rawQuery) throws QueryException {
		requireNonNull(rawQuery);

		try {
			return mapper.readValue(rawQuery.getText(), IqlQuery.class);
		} catch (JsonProcessingException e) {
			throw new QueryException(QueryErrorCode.JSON_ERROR,
					"Failed to read query",
					fragment(rawQuery.getText(), e.getLocation()), e);
		}
	}

	public static class Builder extends AbstractBuilder<Builder, QueryEngine> {

		private ObjectMapper mapper;

		private CorpusManager corpusManager;

		private Builder() {
			// no-op
		}

		public Builder mapper(ObjectMapper mapper) {
			requireNonNull(mapper);
			checkState("Mapper already set", this.mapper==null);
			this.mapper = mapper;
			return this;
		}

		public Builder corpusManager(CorpusManager corpusManager) {
			requireNonNull(corpusManager);
			checkState("Corpus manager already set", this.corpusManager==null);
			this.corpusManager = corpusManager;
			return this;
		}

		public Builder useDefaultMapper() {
			return mapper(createMapper());
		}

		public ObjectMapper getMapper() {
			return mapper;
		}

		public CorpusManager getCorpusManager() {
			return corpusManager;
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			checkState("Mapper not set", mapper!=null);
			checkState("Corpus manager not set", corpusManager!=null);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected QueryEngine create() {
			return new QueryEngine(this);
		}

	}
}
