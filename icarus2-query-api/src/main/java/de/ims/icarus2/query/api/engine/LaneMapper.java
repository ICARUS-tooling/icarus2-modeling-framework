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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public abstract class LaneMapper implements AutoCloseable {

	public static LaneMapper forMapping(MappingReader reader, int bufferSize) {
		return new MapperBacked(reader, bufferSize);
	}

	public static FixedBuilder fixedBuilder() { return new FixedBuilder(); }

	private long index = UNSET_LONG;

	public void reset(long index) throws InterruptedException {
		if(this.index==index) {
			return;
		}

		this.index = index;
		doReset();
	}

	protected long index() { return index; }

	protected abstract void doReset() throws InterruptedException;

	@Override
	public abstract void close();

	public abstract int size();
	public abstract long indexAt(int index);

	static class MapperBacked extends LaneMapper implements IndexCollector {
		private final MappingReader reader;
		private IndexBuffer buffer;

		MapperBacked(MappingReader reader, int bufferSize) {
			this.reader = requireNonNull(reader);
			buffer = new IndexBuffer(bufferSize);
		}

		@Override
		protected void doReset() throws InterruptedException {
			buffer.clear();

			reader.begin();
			try {
				reader.lookup(index(), this, RequestSettings.none());
			} finally {
				reader.end();
			}
		}

		@Override
		public void add(long index) {
			if(buffer.remaining()==0) {
				int newSize = CollectionUtils.growSize(buffer.capacity());
				buffer = IndexBuffer.copyOf(buffer, newSize);
				assert buffer.remaining()>0 : "growing buffer yielded no capacity gain";
			}
			buffer.add(index);
		}

		@Override
		public void close() {
			reader.close();
		}

		@Override
		public int size() { return buffer.size(); }

		@Override
		public long indexAt(int index) { return buffer.indexAt(index); }
	}

	static class Fixed extends LaneMapper {

		private final Long2ObjectMap<long[]> lookup;
		private long[] current;

		Fixed(Long2ObjectMap<long[]> lookup) {
			this.lookup = requireNonNull(lookup);
		}

		@Override
		public void close() {
			lookup.clear();
			current = null;
		}

		@Override
		protected void doReset() {
			current = lookup.get(index());
		}

		@Override
		public int size() { return current==null ? 0 : current.length; }

		@Override
		public long indexAt(int index) {
			checkState("no indices set", current!=null);
			return current[index];
		}
	}

	public static class FixedBuilder extends AbstractBuilder<FixedBuilder, LaneMapper> {
		private final Long2ObjectMap<long[]> lookup = new Long2ObjectOpenHashMap<>();

		private FixedBuilder() { /* no-op */ }

		public FixedBuilder map(long source, long...targets) {
			checkState("source index already has a mapping: "+source, !lookup.containsKey(source));
			checkArgument("target mapping is empty", targets.length>0);

			lookup.put(source, targets);
			return this;
		}

		public FixedBuilder mapIndividual(long from, long to, LongUnaryOperator mapper) {
			for(long source = from; source<=to; source++) {
				checkState("source index already has a mapping: "+source, !lookup.containsKey(source));
				long target = mapper.applyAsLong(source);

				lookup.put(source, new long[] { target });
			}
			return this;
		}

		public FixedBuilder mapBatch(long from, long to, LongFunction<long[]> mapper) {
			for(long source = from; source<=to; source++) {
				checkState("source index already has a mapping: "+source, !lookup.containsKey(source));
				long[] targets = mapper.apply(source);

				lookup.put(source, targets);
			}
			return this;
		}

		@Override
		protected void validate() {
			checkState("no mappings defined", !lookup.isEmpty());
		}

		@Override
		protected LaneMapper create() {
			return new Fixed(lookup);
		}

	}
}
