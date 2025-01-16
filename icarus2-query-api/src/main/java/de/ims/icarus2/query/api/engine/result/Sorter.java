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
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.function.IntFunction;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.exp.BinaryOperations.StringMode;
import de.ims.icarus2.query.api.exp.CodePointUtils;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.util.function.CharBiPredicate;
import de.ims.icarus2.util.function.IntBiPredicate;

/**
 * Models a single level of sorting inside the result handling framework.
 * Each {@link Sorter} is associated with a single data point in the
 * payload of a {@link ResultEntry}.
 * <p>
 * Sorting is supported for the following {@link TypeInfo types}:
 * <ul>
 * <li>{@link TypeInfo#INTEGER}</li>
 * <li>{@link TypeInfo#FLOATING_POINT}</li>
 * <li>{@link TypeInfo#TEXT}</li>
 * </ul>
 *
 * @author Markus Gärtner
 *
 */
public abstract class Sorter implements Comparator<ResultEntry> {

	public static final int SIGN_ASC = 1;
	public static final int SIGN_DESC = -1;

	/** Pointer into payload */
	private final int offset;
	/** 1 or -1 */
	private final int sign;
	/** Subsequent sorter in case 2 entries are considered equal by this one */
	private final Sorter next;

	protected Sorter(int offset, int sign, @Nullable Sorter next) {
		checkArgument("Offset must not be negative", offset>=0);
		checkArgument("Sign must be either 1 or -1", Math.abs(sign)==1);
		this.offset = offset;
		this.sign = sign;
		this.next = next;
	}

	protected abstract int compareRaw(ResultEntry e1, ResultEntry e2, int offset);

	@Override
	public int compare(ResultEntry e1, ResultEntry e2) {
		int result = sign * compareRaw(e1, e2, offset);
		if(result==0 && next!=null) {
			result = next.compare(e1, e2);
		}
		return result;
	}

	public static final class IntegerSorter extends Sorter {
		public IntegerSorter(int offset, int sign, @Nullable Sorter next) {
			super(offset, sign, next);
		}

		@Override
		protected int compareRaw(ResultEntry e1, ResultEntry e2, int offset) {
			return (int)(e1.payloadAt(offset) - e2.payloadAt(offset));
		}
	}

	public final static class FloatingPointSorter extends Sorter {
		public FloatingPointSorter(int offset, int sign, @Nullable Sorter next) {
			super(offset, sign, next);
		}

		@Override
		protected int compareRaw(ResultEntry e1, ResultEntry e2, int offset) {
			return Double.compare(
					Extractor.decode(e1.payloadAt(offset)),
					Extractor.decode(e2.payloadAt(offset)));
		}
	}

	public static final class AsciiSorter extends Sorter {
		private final IntFunction<CharSequence> decoder;
		private final CharBiPredicate comparator;

		public AsciiSorter(int offset, int sign, IntFunction<CharSequence> decoder,
				CharBiPredicate comparator, @Nullable Sorter next) {
			super(offset, sign, next);

			this.decoder = requireNonNull(decoder);
			this.comparator = requireNonNull(comparator);
		}

		@Override
		protected int compareRaw(ResultEntry e1, ResultEntry e2, int offset) {
			CharSequence left = decoder.apply(strictToInt(e1.payloadAt(offset)));
			CharSequence right = decoder.apply(strictToInt(e2.payloadAt(offset)));
			return CodePointUtils.compare(left, right, comparator);
		}
	}

	/**
	 * @see StringMode
	 * @see EvaluationContext#isSwitchSet(de.ims.icarus2.query.api.QuerySwitch)
	 * @see QuerySwitch#STRING_CASE_OFF
	 * @see QuerySwitch#STRING_UNICODE_OFF
	 */
	public static final class UnicodeSorter extends Sorter {
		private final IntFunction<CharSequence> decoder;
		private final IntBiPredicate comparator;

		public UnicodeSorter(int offset, int sign, IntFunction<CharSequence> decoder,
				IntBiPredicate comparator, @Nullable Sorter next) {
			super(offset, sign, next);

			this.decoder = requireNonNull(decoder);
			this.comparator = requireNonNull(comparator);
		}

		@Override
		protected int compareRaw(ResultEntry e1, ResultEntry e2, int offset) {
			CharSequence left = decoder.apply(strictToInt(e1.payloadAt(offset)));
			CharSequence right = decoder.apply(strictToInt(e2.payloadAt(offset)));
			return CodePointUtils.compareCodePoints(left, right, comparator);
		}
	}
}