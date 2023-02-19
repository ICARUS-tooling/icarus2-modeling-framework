/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.strings.ToStringBuilder;
import it.unimi.dsi.fastutil.Hash.Strategy;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQuantifier extends AbstractIqlQueryElement {

	public static IqlQuantifier all() {
		IqlQuantifier q = new IqlQuantifier();
		q.setQuantifierType(QuantifierType.ALL);
		return q;
	}

	public static IqlQuantifier none() {
		IqlQuantifier q = new IqlQuantifier();
		q.setQuantifierType(QuantifierType.EXACT);
		q.setValue(0);
		return q;
	}

	public static final Strategy<IqlQuantifier> EQUALITY = new Strategy<IqlQuantifier>() {

		@Override
		public int hashCode(IqlQuantifier q) {
			return Objects.hash(q.quantifierType, q.value, q.lowerBound, q.upperBound);
		}

		@Override
		public boolean equals(IqlQuantifier q1, IqlQuantifier q2) {
			if(q1==null || q2==null) {
				return q1==q2;
			}
			return Objects.equals(q1.quantifierType, q2.quantifierType)
					&& q1.quantifierModifier==q2.quantifierModifier
					&& q1.discontinuous==q2.discontinuous
					&& q1.value.equals(q2.value)
					&& q1.lowerBound.equals(q2.lowerBound)
					&& q1.upperBound.equals(q2.upperBound);
		}
	};

	@JsonProperty(value=IqlTags.QUANTIFIER_TYPE, required=true)
	private QuantifierType quantifierType;

	@JsonProperty(value=IqlTags.QUANTIFIER_MODIFIER)
	@JsonInclude(Include.NON_DEFAULT)
	private QuantifierModifier quantifierModifier = QuantifierModifier.GREEDY;

	@JsonProperty(IqlTags.VALUE)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalInt value = OptionalInt.empty();

	@JsonProperty(IqlTags.LOWER_BOUND)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalInt lowerBound = OptionalInt.empty();

	@JsonProperty(IqlTags.UPPER_BOUND)
	@JsonInclude(Include.NON_ABSENT)
	private OptionalInt upperBound = OptionalInt.empty();

	@JsonProperty(IqlTags.DISCONTINUOUS)
	@JsonInclude(Include.NON_DEFAULT)
	private boolean discontinuous = false;

	@Override
	public IqlType getType() { return IqlType.QUANTIFIER; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNotNull(quantifierType, IqlTags.QUANTIFIER_TYPE);
		switch (quantifierType) {
		case ALL:
			checkCondition(!discontinuous, IqlTags.DISCONTINUOUS,
					"Cannot set 'discontinuous' flag for universal quantification");
//			checkNotPresent(value, IqlTags.VALUE);
//			checkNotPresent(lowerBound, IqlTags.LOWER_BOUND);
//			checkNotPresent(upperBound, IqlTags.UPPER_BOUND);
			break;
		case EXACT:
		case AT_LEAST:
		case AT_MOST:
			checkPresent(value, IqlTags.VALUE);
//			checkNotPresent(lowerBound, IqlTags.LOWER_BOUND);
//			checkNotPresent(upperBound, IqlTags.UPPER_BOUND);
			break;
		case RANGE:
			checkPresent(lowerBound, IqlTags.LOWER_BOUND);
			checkPresent(upperBound, IqlTags.UPPER_BOUND);
//			checkNotPresent(value, IqlTags.VALUE);
			break;
		default:
			throw new IcarusRuntimeException(GlobalErrorCode.INTERNAL_ERROR,
					"Unknown quantifier type: "+quantifierType);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder b = ToStringBuilder.create(this);

		if(quantifierType==null) {
			b.add("??");
		} else {
			b.add(quantifierType.getLabel());

			switch (quantifierType) {
			case ALL: break;

			case AT_LEAST:
			case AT_MOST:
			case EXACT:
				b.add("value", value.getAsInt());
				break;

			case RANGE:
				b.add("min", lowerBound.getAsInt());
				b.add("max", upperBound.getAsInt());
				break;

			default:
				break;
			}
		}

		if(quantifierModifier!=null) {
			b.requireSpace().add(quantifierModifier.getLabel());
		}

		return b.toString();
	}

	private OptionalInt _set(int val) {
		checkArgument(val>=0);
		return OptionalInt.of(val);
	}

	public QuantifierType getQuantifierType() { return quantifierType; }

	public QuantifierModifier getQuantifierModifier() { return quantifierModifier; }

	public OptionalInt getValue() { return value; }

	public OptionalInt getLowerBound() { return lowerBound; }

	public OptionalInt getUpperBound() { return upperBound; }

	public boolean isDiscontinuous() { return discontinuous; }

	public void setQuantifierType(QuantifierType quantifierType) { this.quantifierType = requireNonNull(quantifierType); }

	public void setQuantifierModifier(QuantifierModifier quantifierModifier) { this.quantifierModifier = requireNonNull(quantifierModifier); }

	public void setValue(int value) { this.value = _set(value); }

	public void setLowerBound(int lowerBound) { this.lowerBound = _set(lowerBound); }

	public void setUpperBound(int upperBound) { this.upperBound = _set(upperBound); }

	public void setDiscontinuous(boolean discontinuous) { this.discontinuous = discontinuous; }

	// Utility

	public boolean isExistentiallyQuantified() {
		//TODO rework
		return (value.isPresent() && value.getAsInt()>0)
				|| (upperBound.isPresent() && upperBound.getAsInt()>0)
				|| (lowerBound.isPresent() && lowerBound.getAsInt()>0);
	}

	public boolean isExistentiallyNegated() {
		return quantifierType==QuantifierType.EXACT && value.getAsInt()==0;
	}

	public boolean isUniversallyQuantified() {
		return quantifierType==QuantifierType.ALL;
	}

	/**
	 * Basic types of quantifiers.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum QuantifierType {
		/** Universal quantification: * */
		ALL("all"),
		/** Explicit quantification: n */
		EXACT("exact"),
		/** Upper bound quantification: 1..n */
		AT_MOST("atMost"),
		/** Lower bound quantification: n+ */
		AT_LEAST("atLeast"),
		/** Ranged quantification: n..m */
		RANGE("range"),
		;

		private final String label;

		private QuantifierType(String label) {
			this.label = label;
		}

		@JsonValue
		public String getLabel() {
			return label;
		}
	}

	/**
	 * Eagerness of quantifiers that allow expansion.
	 * This includes {@link QuantifierType#AT_LEAST}, {@link QuantifierType#AT_MOST} and
	 * {@link QuantifierType#RANGE}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum QuantifierModifier {
		/** Match as many as possible while still considering subsequent constraints or nodes */
		GREEDY("greedy"),
		/** Match as few as possible, considering subsequent constraints or nodes */
		RELUCTANT("reluctanct"),
		/** Match as many as possible without consideration for subsequent constraints or nodes */
		POSSESSIVE("possessive"),
		;

		private final String label;

		private QuantifierModifier(String label) {
			this.label = label;
		}

		public int id() { return ordinal(); }

		@JsonValue
		public String getLabel() {
			return label;
		}

		private static final QuantifierModifier[] _values = values();

		public static QuantifierModifier forId(int id) { return _values[id]; }
	}

	/**
	 * Marks components of a query as able to receive quantification.
	 * This interface exists mainly to provide a convenient unification
	 * between {@link IqlElement.IqlGrouping} and the structural elements
	 * derived from {@link IqlElement.IqlNode}, such as {@link IqlElement.IqlTreeNode}.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface Quantifiable {

		boolean hasQuantifiers();

		/** Get all registered quantifiers or an empty list. */
		List<IqlQuantifier> getQuantifiers();

		/** Add a new non-null quantifier. */
		void addQuantifier(IqlQuantifier quantifier);

		/** Traverse quantifiers and apply given action to all of them. */
		void forEachQuantifier(Consumer<? super IqlQuantifier> action);


		default boolean isExistentiallyQuantified() {
			return !hasQuantifiers() || getQuantifiers()
					.stream()
					.filter(IqlQuantifier::isExistentiallyQuantified)
					.findAny()
					.isPresent();
		}

		default boolean isUniversallyQuantified() {
			return getQuantifiers()
					.stream()
					.filter(IqlQuantifier::isUniversallyQuantified)
					.findAny()
					.isPresent();
		}

		default boolean isExistentiallyNegated() {
			return getQuantifiers()
					.stream()
					.filter(IqlQuantifier::isExistentiallyNegated)
					.findAny()
					.isPresent();
		}
	}
}
