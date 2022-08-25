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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;
import java.util.function.LongFunction;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.query.api.engine.matcher.Matcher;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.MatchAccumulator;
import de.ims.icarus2.query.api.engine.result.MatchBuilder;
import de.ims.icarus2.query.api.engine.result.MatchCollector;
import de.ims.icarus2.query.api.engine.result.MatchSink;
import de.ims.icarus2.query.api.engine.result.MatchSource;
import de.ims.icarus2.util.AbstractBuilder;

/**
 * Bridges between matchers for multiple lanes and manages caching of
 * results.
 *
 * @author Markus Gärtner
 *
 */
public abstract class LaneBridge implements MatchCollector, Matcher<Container>,
		AutoCloseable, MatchSource {
	private final ThreadVerifier threadVerifier;
	private final Matcher<Container> matcher;
	private final Matcher<Container> next;
	private final LaneMapper laneMapper;
	private final LongFunction<Container> itemLookup;

	protected LaneBridge(BuilderBase<?,?> builder) {
		threadVerifier = builder.threadVerifier();
		matcher = builder.matcherGen().apply(threadVerifier, this);
		next = builder.next();
		laneMapper = builder.laneMapper();
		itemLookup = builder.itemLookup();
	}

	private long index = UNSET_LONG;
	private boolean renewMapping = false;

	@Override
	public int id() { return matcher.id(); }

	protected final Matcher<Container> matcher() { return matcher; }

	@Override
	public final boolean matches(long index, Container target) {
		if(Tripwire.ACTIVE) {
			threadVerifier.checkThread();
		}

		boolean isNew = this.index!=index;
		renewMapping = isNew;
		this.index = index;
		return matchImpl(index, target, isNew);
	}

	protected abstract boolean matchImpl(long index, Container target, boolean isNew);

	@Override
	public boolean collect(MatchSource source) {
		if(Tripwire.ACTIVE) {
			threadVerifier.checkThread();
		}

		source.drainTo(sink());

		return nextMatches();
	}

	protected final boolean nextMatches() {
		assert index!=UNSET_LONG : "no index set";

		if(renewMapping) {
			renewMapping = false;
			try {
				laneMapper.reset(index);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}

		boolean result = false;

		final int targetCount = laneMapper.size();
		for (int i = 0; i < targetCount; i++) {
			final long index = laneMapper.indexAt(i);
			assert index!=UNSET_LONG;
			final Container target = itemLookup.apply(index);
			assert target!=null;

			result |= next.matches(index, target);
		}

		return result;
	}

	protected abstract MatchSink sink();

	/**
	 * Closes the underlying matcher and mapper and then delegates to subclasses to
	 * also close any internal helpers if applicable.
	 *
	 * @see de.ims.icarus2.query.api.engine.matcher.Matcher#close()
	 */
	@Override
	public final void close() {
		if(Tripwire.ACTIVE) {
			threadVerifier.checkThread();
		}

		matcher.close();
		laneMapper.close();

		closeImpl();
	}

	protected void closeImpl() { /* no-op */ }

	public static final class Uncached extends LaneBridge {

		public static Builder builder() { return new Builder(); }

		private final MatchBuilder buffer;

		private Uncached(Builder builder) {
			super(builder);
			buffer = new MatchBuilder(builder.bufferSize());
		}

		@Override
		protected boolean matchImpl(long index, Container target, boolean isNew) {
			return matcher().matches(index, target);
		}

		@Override
		protected MatchSink sink() { return buffer; }

		@Override
		public Match toMatch() { return buffer.toMatch(); }

		@Override
		public void drainTo(MatchSink sink) { buffer.drainTo(sink); }

		public static class Builder extends BuilderBase<Builder, Uncached> {

			private Integer bufferSize;

			private Builder() { /* no-op */ }

			public Builder bufferSize(int bufferSize) {
				checkState("buffer size already set", this.bufferSize==null);
				this.bufferSize = Integer.valueOf(bufferSize);
				return this;
			}

			int bufferSize() { return bufferSize.intValue(); }

			@Override
			protected void validate() {
				super.validate();
				checkState("buffer size not set", bufferSize!=null);
			}

			@Override
			protected Uncached create() { return new Uncached(this); }
		}
	}

	public static final class Cached extends LaneBridge {

		public static Builder builder() { return new Builder(); }

		private final MatchAccumulator accumulator;

		private Cached(Builder builder) {
			super(builder);
			accumulator = builder.accumulator();
		}

		@Override
		protected void closeImpl() {
			accumulator.close();
		}

		@Override
		protected boolean matchImpl(long index, Container target, boolean isNew) {

			// Run one raw pass to populate the cache
			if(isNew) {
				return matcher().matches(index, target);
			}

			return iterateCachedMatches();
		}

		private boolean iterateCachedMatches() {
			boolean result = false;

			final int matchCount = accumulator.getMatchCount();
			for (int i = 0; i < matchCount; i++) {
				accumulator.goToMatch(i);
				result |= nextMatches();
			}

			return result;
		}

		@Override
		protected MatchSink sink() { return accumulator; }

		@Override
		public Match toMatch() { return accumulator.toMatch(); }

		@Override
		public void drainTo(MatchSink sink) { accumulator.drainTo(sink); }

		public static class Builder extends BuilderBase<Builder, Cached> {

			private MatchAccumulator accumulator;

			private Builder() { /* no-op */ }

			public Builder accumulator(MatchAccumulator accumulator) {
				checkState("match accumulator already set", this.accumulator==null);
				this.accumulator = requireNonNull(accumulator);
				return this;
			}

			MatchAccumulator accumulator() { return accumulator; }

			@Override
			protected void validate() {
				super.validate();
				checkState("accumulator not set", accumulator!=null);
			}

			@Override
			protected Cached create() { return new Cached(this); }
		}
	}

	public static abstract class BuilderBase<B extends BuilderBase<B,LB>, LB> extends AbstractBuilder<B, LB> {

		private ThreadVerifier threadVerifier;
		private BiFunction<ThreadVerifier, MatchCollector, Matcher<Container>> matcherGen;
		private Matcher<Container> next;
		private LaneMapper laneMapper;
		private LongFunction<Container> itemLookup;

		/** Sets the thread verifier to be used for the bridge (and all associated resources). */
		public B threadVerifier(ThreadVerifier threadVerifier) {
			checkState("thread verifier already set", this.threadVerifier==null);
			this.threadVerifier = requireNonNull(threadVerifier);
			return thisAsCast();
		}

		/** Sets the given {@link StructurePattern} as source for matcher creation in this bridge. */
		public B pattern(StructurePattern pattern) {
			checkState("matcher generator already set - cannot apply pattern", this.matcherGen==null);
			requireNonNull(pattern);
			this.matcherGen = (threadVerifier, collector)  -> pattern.matcherBuilder()
					.threadVerifier(threadVerifier)
					.matchCollector(collector)
					.build();
			return thisAsCast();
		}
		ThreadVerifier threadVerifier() { return threadVerifier; }

		@VisibleForTesting
		B matcherGen(BiFunction<ThreadVerifier, MatchCollector, Matcher<Container>> matcherGen) {
			checkState("matcher generator already set", this.matcherGen==null);
			this.matcherGen = requireNonNull(matcherGen);
			return thisAsCast();
		}

		BiFunction<ThreadVerifier, MatchCollector, Matcher<Container>> matcherGen() { return matcherGen; }

		/** Sets the matcher to be used for containers in the target lane
		 * after matching in the source lane and mapping of indices. */
		public B next(Matcher<Container> next) {
			checkState("next matcher already set", this.next==null);
			this.next = requireNonNull(next);
			return thisAsCast();
		}

		Matcher<Container> next() { return next; }

		/** Sets the mapper to be used to translate from source to target lane. */
		public B laneMapper(LaneMapper laneMapper) {
			checkState("lane mapper already set", this.laneMapper==null);
			this.laneMapper = requireNonNull(laneMapper);
			return thisAsCast();
		}

		LaneMapper laneMapper() { return laneMapper; }

		/** Sets the lookup function to be used to turn mapped index values into
		 * {@link Container} instances of the target lane. */
		public B itemLookup(LongFunction<Container> itemLookup) {
			checkState("item lookup already set", this.itemLookup==null);
			this.itemLookup = requireNonNull(itemLookup);
			return thisAsCast();
		}

		LongFunction<Container> itemLookup() { return itemLookup; }

		@Override
		protected void validate() {
			checkState("thread verifier not set", threadVerifier!=null);
			checkState("matcher generator not set", matcherGen!=null);
			checkState("next matcher not set", next!=null);
			checkState("lane mapper not set", laneMapper!=null);
			checkState("item lookup not set", itemLookup!=null);
		}
	}
}
