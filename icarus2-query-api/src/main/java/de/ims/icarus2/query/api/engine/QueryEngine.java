/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.query.api.iql.IqlUtils.createMapper;
import static de.ims.icarus2.query.api.iql.IqlUtils.fragment;
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

	private final ObjectMapper mapper = createMapper();

	private QueryEngine(Builder builder) {
		//TODO
	}

	public QueryJob query(Query query) {

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

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected QueryEngine create() {
			return new QueryEngine(this);
		}

	}
}
