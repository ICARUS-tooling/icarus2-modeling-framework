/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.matcher;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.function.Consumer;

import de.ims.icarus2.query.api.engine.matcher.StructurePattern.StructureMatcher;
import de.ims.icarus2.util.function.IntBiConsumer;

/**
 * Provides the basic interface for processing search results from a single
 * {@link StructurePattern} instance. The overall handling process is split
 * into tree methods, two of which can be overridden by external subclasses.
 *
 * @author Markus Gärtner
 *
 */
public abstract class ResultHandler implements Consumer<StructureMatcher> {

	protected void matchBegin() { /* no-op */ }
	protected void matchEnd() { /* no-op */ }
	protected abstract void matchContent(StructureMatcher state);

	@Override
	public final void accept(StructureMatcher matcher) {
		matchBegin();

		matchContent(matcher);

		matchEnd();
	}

	/**
	 * Implements a {@link ResultHandler} that wraps each result into a
	 * new {@link Match} instance and then passes the match to the
	 * internal {@code sink}. Note that this is a fairly expensive
	 * handler, as it can generate a huge number of {@link Match}
	 * objects during its lifetime.
	 */
	public static class MatchConsumerBridge extends ResultHandler {

		private final Consumer<? super Match> sink;

		public MatchConsumerBridge(Consumer<? super Match> sink) {
			this.sink = requireNonNull(sink);
		}

		@Override
		protected void matchContent(StructureMatcher matcher) {
			int size = matcher.entry;
			sink.accept(new Match(matcher.index,
					Arrays.copyOf(matcher.m_node, size),
					Arrays.copyOf(matcher.m_index, size)));
		}
	}

	/**
	 * Implements a {@link ResultHandler} that wraps each result into a
	 * new {@link Match} instance and then passes the match to the
	 * internal {@code sink}. Note that this is a fairly expensive
	 * handler, as it can generate a huge number of {@link Match}
	 * objects during its lifetime.
	 */
	public static class MappingConsumerBridge extends ResultHandler {

		private final IntBiConsumer sink;

		public MappingConsumerBridge(IntBiConsumer sink) {
			this.sink = requireNonNull(sink);
		}

		@Override
		protected void matchContent(StructureMatcher matcher) {
			final int size = matcher.entry;
			for (int i = 0; i < size; i++) {
				sink.accept(matcher.m_node[i], matcher.m_index[i]);
			}
		}
	}
}
