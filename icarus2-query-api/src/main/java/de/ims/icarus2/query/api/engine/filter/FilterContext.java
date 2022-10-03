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
package de.ims.icarus2.query.api.engine.filter;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.AbstractBuilder;

/**
 * Encapsulates the information a {@link QueryFilter} needs to perform his
 * job in partially evaluating a query.
 *
 * @author Markus Gärtner
 *
 */
public class FilterContext {

	public static Builder builder() { return new Builder(); }

	private final Context context;
	private final IqlQuery query;
	private final IqlStream stream;
	private final CandidateSink sink;

	private FilterContext(Builder builder) {
		builder.validate();
		context = builder.context;
		query = builder.query;
		stream = builder.stream;
		sink = builder.sink;
	}

	/** The context associated with the driver hosting the {@link QueryFilter} */
	public Context getContext() { return context; }

	/** Entire source query, just for completeness */
	public IqlQuery getQuery() { return query; }

	/** The stream declaration corresponding to the the fitler's driver. Expected
	 * to be rewritten in case the filter is successful. */
	public IqlStream getStream() { return stream; }

	/** The sink to stream result candidates to. */
	public CandidateSink getSink() { return sink; }

	public static class Builder extends AbstractBuilder<Builder, FilterContext> {
		private Context context;
		private IqlQuery query;
		private IqlStream stream;
		private CandidateSink sink;

		private Builder() {
			// no-op
		}

		public Builder context(Context context) {
			requireNonNull(context);
			checkState("context already set", this.context == null);
			this.context = context;
			return this;
		}

		public Builder query(IqlQuery query) {
			requireNonNull(query);
			checkState("query already set", this.query == null);
			this.query = query;
			return this;
		}

		public Builder stream(IqlStream stream) {
			requireNonNull(stream);
			checkState("stream already set", this.stream == null);
			this.stream = stream;
			return this;
		}

		public Builder sink(CandidateSink sink) {
			requireNonNull(sink);
			checkState("sink already set", this.sink == null);
			this.sink = sink;
			return this;
		}

		@Override
		protected void validate() {
			checkState("context not set", context!=null);
			checkState("query not set", query!=null);
			checkState("stream not set", stream!=null);
			checkState("sink not set", sink!=null);
		}

		@Override
		protected FilterContext create() {
			return new FilterContext(this);
		}
	}

}
