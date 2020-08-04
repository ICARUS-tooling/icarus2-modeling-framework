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
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.matcher.mark.Marker.RangeMarker;
import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public abstract class SequenceMarker {

	public static Marker.RangeMarker of(String name, Number...arguments) {
		requireNonNull(name);
		requireNonNull(arguments);

		Name n = Name.parseName(name);
		Position[] pos = arguments.length==0 ? NO_ARGS : toPos(arguments);
		return n.instantiate(pos);
	}

	private static final Position[] NO_ARGS = {};

	private static Position[] toPos(Number[] numbers) {
		return Stream.of(numbers).map(Position::of).toArray(Position[]::new);
	}

	private static abstract class MarkerBase implements Marker.RangeMarker {

		private final Name name;
		private final boolean dynamic;
		private final int intervalCount;

		/** No defensive copying of the 'positions' array */
		private MarkerBase(Name name, boolean dynamic, int intervalCount) {
			checkArgument(intervalCount>=1);
			this.name = requireNonNull(name);
			this.dynamic = dynamic;
			this.intervalCount = intervalCount;
		}

		@Override
		public MarkerType getType() { return MarkerType.SEQUENCE; }

		@Override
		public String getName() { return name.getLabel(); }

		@Override
		public boolean isDynamic() { return dynamic; }

		@Override
		public int intervalCount() { return intervalCount; }

	}

	private static final class IsFirst extends MarkerBase {
		static final IsFirst INSTANCE = new IsFirst();

		IsFirst() {
			super(Name.FIRST, false, 1);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv = intervals[index];
			iv.from = iv.to = 0;
			return true;
		}
	}

	private static final class IsLast extends MarkerBase {
		static final IsLast INSTANCE = new IsLast();

		IsLast() {
			super(Name.LAST, false, 1);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv = intervals[index];
			iv.from = iv.to = size-1;
			return true;
		}
	}

	private static final class IsAt extends MarkerBase {
		private final Position pos;

		IsAt(Position position) {
			super(Name.AT, false, 1);
			this.pos = requireNonNull(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv = intervals[index];
			iv.from = iv.to = pos.translate(size);
			return !iv.isEmpty() && size>iv.from;
		}
	}

	private static final class IsNotAt extends MarkerBase {
		private final Position pos;

		IsNotAt(Position position) {
			super(Name.AT, true, 2);
			this.pos = requireNonNull(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv1 = intervals[index];
			Interval iv2 = intervals[index+1];
			int v = pos.translate(size);
			iv1.from = 0;
			iv1.to = v-1;
			iv2.from = v+1;
			iv2.to = size-1;
			// We're good if at least 1 interval is not empty
			return !iv1.isEmpty() || !iv2.isEmpty();
		}
	}

	private static final class IsAfter extends MarkerBase {
		private final Position pos;

		IsAfter(Position position) {
			super(Name.AFTER, false, 1);
			this.pos = requireNonNull(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv = intervals[index];
			iv.from = pos.translate(size)+1;
			iv.to = size-1;
			return !iv.isEmpty();
		}
	}

	private static final class IsBefore extends MarkerBase {
		private final Position pos;

		IsBefore(Position position) {
			super(Name.BEFORE, false, 1);
			this.pos = requireNonNull(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv = intervals[index];
			iv.from = 0;
			iv.to = pos.translate(size)-1;
			return !iv.isEmpty();
		}
	}

	private static final class IsInside extends MarkerBase {
		private final Position start, end;

		IsInside(Position start, Position end) {
			super(Name.INSIDE, false, 1);
			this.start = requireNonNull(start);
			this.end = requireNonNull(end);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv = intervals[index];
			iv.from = start.translate(size);
			iv.to = end.translate(size);
			return !iv.isEmpty();
		}
	}

	private static final class IsOutside extends MarkerBase {
		private final Position start, end;

		IsOutside(Position start, Position end) {
			super(Name.OUTSIDE, false, 2);
			this.start = requireNonNull(start);
			this.end = requireNonNull(end);
		}

		@Override
		public boolean adjust(Interval[] intervals, int index, int size) {
			Interval iv1 = intervals[index];
			Interval iv2 = intervals[index+1];
			iv1.from = 0;
			iv1.to = start.translate(size)-1;
			iv2.from = end.translate(size)+1;
			iv2.to = size-1;
			return !iv1.isEmpty() || !iv2.isEmpty();
		}
	}

	public static enum Name implements StringResource {
		FIRST("IsFirst", 0) {
			@Override
			public RangeMarker instantiate(Position[] positions) { return IsFirst.INSTANCE; }
		},
		LAST("IsLast", 0) {
			@Override
			public RangeMarker instantiate(Position[] positions) { return IsLast.INSTANCE; }
		},

		AT("IsAt", 1) {
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsAt(posAt(positions, 0));
			}
		},
		NOT_AT("IsNotAt", 1) {
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsNotAt(posAt(positions, 0));
			}
		},

		AFTER("After", 1){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsAfter(posAt(positions, 0));
			}},
		BEFORE("Before", 1){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsBefore(posAt(positions, 0));
			}},

		INSIDE("Inside", 2){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsInside(posAt(positions, 0), posAt(positions, 1));
			}},
		OUTSIDE("Outside", 2){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsOutside(posAt(positions, 0), posAt(positions, 1));
			}},
		;

		private final String label;
		private final int argCount;

		private Name(String label, int argCount) {
			this.label = checkNotEmpty(label);
			checkArgument(argCount>=0);
			this.argCount = argCount;
		}

		protected Position posAt(Position[] positions, int index) {
			if(index>=positions.length)
				throw new QueryException(GlobalErrorCode.INVALID_INPUT,
						String.format("No position available for index %d - %s needs %d arguments, %d provided",
								_int(index), label, _int(argCount), _int(positions.length)));
			return positions[index];
		}

		public abstract Marker.RangeMarker instantiate(Position[] positions);

		public int getArgCount() { return argCount; }

		public String getLabel() { return label; }

		@Override
		public String getStringValue() { return label; }

		private static final LazyStore<Name, String> store
				= LazyStore.forStringResource(Name.class);

		public static Name parseName(String s) {
			return store.lookup(s);
		}
	}
}
