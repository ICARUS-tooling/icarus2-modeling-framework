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
package de.ims.icarus2.query.api.engine.matcher.mark;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.matcher.mark.Marker.RangeMarker;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.util.LazyStore;

/**
 * @author Markus Gärtner
 *
 */
public abstract class HorizontalMarker {

	public static Marker.RangeMarker of(String name, Number...arguments) {
		requireNonNull(name);
		requireNonNull(arguments);

		if(!Type.isValidName(name))
			throw EvaluationUtils.forUnknownIdentifier(name, "marker");

		Type ntype = Type.parseName(name);
		Position[] pos = arguments.length==0 ? NO_ARGS : toPos(arguments);
		return ntype.instantiate(pos);
	}

	public static boolean isValidName(String name) { return Type.isValidName(name); }
	public static boolean isSequenceName(String name) { return Type.isSequenceName(name); }
	public static boolean isTreeHierarchyName(String name) { return Type.isTreeHierarchyName(name); }

	private static final Position[] NO_ARGS = {};

	private static Position[] toPos(Number[] numbers) {
		return Stream.of(numbers).map(Position::of).toArray(Position[]::new);
	}

	private static Position checkNotRelative(Position pos) {
		checkArgument("Relative position not supported", !pos.isRelative());
		return pos;
	}

	private static boolean isRelativeOrReverse(Position...positions) {
		for(Position position : positions) {
			if(position.isRelative() || position.isReverse()) {
				return true;
			}
		}
		return false;
	}

	public static enum Type {
		FIRST("IsFirst", "IsFirstChild", 0) {
			@Override
			public RangeMarker instantiate(Position[] positions) { return new IsFirst(); }
		},
		LAST("IsLast", "IsLastChild", 0) {
			@Override
			public RangeMarker instantiate(Position[] positions) { return new IsLast(); }
		},

		AT("IsAt", "IsChildAt", 1) {
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsAt(posAt(positions, 0));
			}
		},
		NOT_AT("IsNotAt", "IsNotChildAt", 1) {
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsNotAt(posAt(positions, 0));
			}
		},

		AFTER("IsAfter", "IsChildAfter", 1){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsAfter(posAt(positions, 0));
			}},
		BEFORE("IsBefore", "IsChildBefore", 1){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsBefore(posAt(positions, 0));
			}},

		INSIDE("IsInside", "IsChildInside", 2){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsInside(posAt(positions, 0), posAt(positions, 1));
			}},
		OUTSIDE("IsOutside", "IsChildOutside", 2){
			@Override
			public RangeMarker instantiate(Position[] positions) {
				return new IsOutside(posAt(positions, 0), posAt(positions, 1));
			}},
		;

		private final String sequenceLabel, treeHierarchyLabel;
		private final int argCount;

		private Type(String sequenceLabel, String treeHierarchyLabel, int argCount) {
			this.sequenceLabel = checkNotEmpty(sequenceLabel);
			this.treeHierarchyLabel = requireNonNull(treeHierarchyLabel);
			checkArgument(argCount>=0);
			this.argCount = argCount;
		}

		protected Position posAt(Position[] positions, int index) {
			if(index>=positions.length)
				throw new QueryException(GlobalErrorCode.INVALID_INPUT,
						String.format("No position available for index %d - %s needs %d arguments, %d provided",
								_int(index), sequenceLabel, _int(argCount), _int(positions.length)));
			return positions[index];
		}

		public abstract Marker.RangeMarker instantiate(Position[] positions);

		public int getArgCount() { return argCount; }

		public String getSequenceLabel() { return sequenceLabel; }

		public String getTreeHierarchyLabel() { return treeHierarchyLabel; }

		private static final LazyStore<Type, String> sequenceStore
				= new LazyStore<>(Type.class, Type::getSequenceLabel, String::toLowerCase);
		private static final LazyStore<Type, String> treeHierarchyStore
			= new LazyStore<>(Type.class, Type::getTreeHierarchyLabel, String::toLowerCase);

		private static final Function<String, RuntimeException> ERROR
			= name -> EvaluationUtils.forUnknownIdentifier(name, "marker");

		public static Type parseName(String s) {
			Type type = sequenceStore.tryLookup(s);
			if(type==null) {
				type = treeHierarchyStore.lookup(s, ERROR);
			}
			return type;
		}

		public static boolean isValidName(String s) {
			return sequenceStore.hasKey(s) || treeHierarchyStore.hasKey(s);
		}

		public static boolean isSequenceName(String s) {
			return sequenceStore.hasKey(s);
		}

		public static boolean isTreeHierarchyName(String s) {
			return treeHierarchyStore.hasKey(s);
		}
	}

	@VisibleForTesting
	static abstract class MarkerBase implements Marker.RangeMarker {

		private final Type name;
		private final boolean dynamic;
		private final int intervalCount;
		protected int index = UNSET_INT;

		private MarkerBase(Type name, boolean dynamic, int intervalCount) {
			checkArgument(intervalCount>=1);
			this.name = requireNonNull(name);
			this.dynamic = dynamic;
			this.intervalCount = intervalCount;
		}

		@Override
		public void setIndex(int index) { this.index = index; }

		@VisibleForTesting
		Type getRawName() { return name; }

		@Override
		public MarkerType getType() { return MarkerType.SEQUENCE; }

		@Override
		public String getName() { return name.getSequenceLabel(); }

		@Override
		public boolean isDynamic() { return dynamic; }

		@Override
		public int intervalCount() { return intervalCount; }

	}

	private static final class IsFirst extends MarkerBase {
		IsFirst() { super(Type.FIRST, false, 1); }

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv = intervals[index];
			iv.from = iv.to = 0;
			return true;
		}
	}

	private static final class IsLast extends MarkerBase {
		IsLast() { super(Type.LAST, true, 1); }

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv = intervals[index];
			iv.from = iv.to = size-1;
			return true;
		}
	}

	private static final class IsAt extends MarkerBase {
		private final Position pos;

		IsAt(Position position) {
			super(Type.AT, position.isReverse(), 1);
			requireNonNull(position);
			this.pos = checkNotRelative(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv = intervals[index];
			iv.from = iv.to = pos.asPosition(size);
			return !iv.isEmpty() && size>iv.from;
		}
	}

	private static final class IsNotAt extends MarkerBase {
		private final Position pos;

		IsNotAt(Position position) {
			super(Type.NOT_AT, true, 2);
			requireNonNull(position);
			this.pos = checkNotRelative(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv1 = intervals[index];
			Interval iv2 = intervals[index+1];
			iv1.from = 0;
			iv1.to = pos.asUpperBound(size, false);
			iv2.from = pos.asLowerBound(size, false);
			iv2.to = size-1;
			// We're good if at least 1 interval is not empty
			return !iv1.isEmpty() || !iv2.isEmpty();
		}
	}

	private static final class IsAfter extends MarkerBase {
		private final Position pos;

		IsAfter(Position position) {
			super(Type.AFTER, true, 1);
			this.pos = requireNonNull(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv = intervals[index];
			iv.from = pos.asLowerBound(size, false);
			iv.to = size-1;
			return !iv.isEmpty();
		}
	}

	private static final class IsBefore extends MarkerBase {
		private final Position pos;

		IsBefore(Position position) {
			super(Type.BEFORE, isRelativeOrReverse(position), 1);
			this.pos = requireNonNull(position);
		}

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv = intervals[index];
			iv.from = 0;
			iv.to = pos.asUpperBound(size, false);
			return !iv.isEmpty();
		}
	}

	private static final class IsInside extends MarkerBase {
		private final Position start, end;

		IsInside(Position start, Position end) {
			super(Type.INSIDE, isRelativeOrReverse(start, end), 1);
			this.start = requireNonNull(start);
			this.end = requireNonNull(end);
		}

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv = intervals[index];
			iv.from = start.asLowerBound(size, true);
			iv.to = end.asUpperBound(size, true);
			return !iv.isEmpty();
		}
	}

	private static final class IsOutside extends MarkerBase {
		private final Position start, end;

		IsOutside(Position start, Position end) {
			super(Type.OUTSIDE, true, 2);
			this.start = requireNonNull(start);
			this.end = requireNonNull(end);
		}

		@Override
		public boolean adjust(Interval[] intervals, int size) {
			Interval iv1 = intervals[index];
			Interval iv2 = intervals[index+1];
			iv1.from = 0;
			iv1.to = start.asUpperBound(size, false);
			iv2.from = end.asLowerBound(size, false);
			iv2.to = size-1;
			return !iv1.isEmpty() || !iv2.isEmpty();
		}
	}
}
