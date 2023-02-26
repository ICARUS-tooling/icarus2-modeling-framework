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
package de.ims.icarus2.model.manifest.util;

import static de.ims.icarus2.util.IcarusUtils.UNSET_DOUBLE;
import static de.ims.icarus2.util.IcarusUtils.UNSET_FLOAT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;

import java.util.Comparator;
import java.util.Set;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.Wrapper;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Implements a convenient way of using {@link ValueRange} and {@link ValueSet}
 * objects and verify given values against them.
 *
 * @author Markus Gärtner
 *
 */
public abstract class ValueVerifier {

	public static ValueVerifier forAnnotation(AnnotationManifest annotationManifest) {
		if(annotationManifest.isAllowUnknownValues()) {
			return null;
		}

		ValueRange valueRange = annotationManifest.getValueRange().orElse(null);
		ValueSet valueSet = annotationManifest.getValueSet().orElse(null);

		ValueType valueType = annotationManifest.getValueType();

		switch (valueType.getName()) {
		case ValueType.BOOLEAN_TYPE_LABEL: return null; // no point in checking boolean results ?
		case ValueType.INTEGER_TYPE_LABEL: return new IntVerifier(valueRange, valueSet);
		case ValueType.LONG_TYPE_LABEL: return new LongVerifier(valueRange, valueSet);
		case ValueType.FLOAT_TYPE_LABEL: return new FloatVerifier(valueRange, valueSet);
		case ValueType.DOUBLE_TYPE_LABEL: return new DoubleVerifier(valueRange, valueSet);

		default:
			return new ObjectVerifier(valueRange, valueSet);
		}
	}

	public enum VerificationResult {

		/**
		 * The value is legal and does not violate any restrictive constraints.
		 */
		VALID(false) {
			@Override
			public void throwException(String value) {
				throw new ManifestException(GlobalErrorCode.INTERNAL_ERROR,
						"This result describes a valid value - client code should not try to let it throw an exception!");
			}
		},

		/**
		 * The value lies below the lower boundary, i.e. it is too small.
		 */
		LOWER_BOUNDARY_VIOLATION(true) {
			@Override
			public void throwException(String value) {
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Value below lower boundary: "+value);
			}
		},

		/**
		 * The value lies above the upper boundary, i.e. it is too large.
		 */
		UPPER_BOUNDARY_VIOLATION(true) {
			@Override
			public void throwException(String value) {
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Value above upper boundary: "+value);
			}
		},

		/**
		 * The value does not match the expected precision or step length.
		 */
		PRECISION_MISMATCH(true) {
			@Override
			public void throwException(String value) {
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Value precision too high or not honoring step length: "+value);
			}
		},

		/**
		 * The value is missing in the supplied set.
		 */
		VALUE_NOT_IN_SET(true) {
			@Override
			public void throwException(String value) {
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Value not present in set of allowed values: "+value);
			}
		},
		;

		private final boolean error;

		private VerificationResult(boolean error) {
			this.error = error;
		}

		public boolean isError() {
			return error;
		}

		/**
		 *
		 * @param value
		 * @throws ManifestException
		 */
		public abstract void throwException(String value);
	}

	protected final ValueRange valueRange;
	protected final ValueSet valueSet;

	protected final int lowerCompRequired, upperCompRequired;

	/**
	 * @param valueRange
	 * @param valueSet
	 */
	public ValueVerifier(@Nullable ValueRange valueRange, @Nullable ValueSet valueSet) {
		this.valueRange = valueRange;
		this.valueSet = valueSet;

		lowerCompRequired = (valueRange!=null && valueRange.isLowerBoundInclusive()) ? 0 : -1;
		upperCompRequired = (valueRange!=null && valueRange.isUpperBoundInclusive()) ? 0 : 1;
	}

	public abstract VerificationResult verify(Object value);

	public static class ObjectVerifier extends ValueVerifier {

		public static ObjectVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new ObjectVerifier(
					annotationManifest.getValueRange().orElse(null),
					annotationManifest.getValueSet().orElse(null));
		}

		private final Object lower, upper;
		private final Set<Object> allowedValues;
		@SuppressWarnings("rawtypes")
		private final Comparator comparator;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ObjectVerifier(ValueRange valueRange, ValueSet valueSet) {
			super(valueRange, valueSet);

			if(valueRange!=null) {
				Comparator comp = valueRange.getValueType().getComparator();
				if(comp==null) {
					comp = (c1, c2) -> ((Comparable)c1).compareTo(c2);
				}
				comparator = comp;
				lower = valueRange.getLowerBound().orElse(null);
				upper = valueRange.getUpperBound().orElse(null);
			} else {
				comparator = null;
				lower = upper = null;
			}

			if(valueSet!=null) {
				allowedValues = new ObjectOpenHashSet<>(valueSet.valueCount());
				valueSet.forEach(v -> allowedValues.add(unpack(v)));
			} else {
				allowedValues = null;
			}
		}

		private Object unpack(Object obj) {
			if(obj instanceof Wrapper) {
				obj = ((Wrapper<?>)obj).get();
			}
			return obj;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier#verify(java.lang.Object)
		 */
		@SuppressWarnings({ "unchecked" })
		@Override
		public VerificationResult verify(Object value) {
			if(allowedValues!=null && !allowedValues.contains(value)) {
				return VerificationResult.VALUE_NOT_IN_SET;
			}

			if(lower!=null && comparator.compare(lower, value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(upper!=null && comparator.compare(upper, value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}


			// No check about step length here since general Comparable objects don't have any inherent "precision"

			return VerificationResult.VALID;
		}
	}

	private static void checkStepAndBounds(boolean hasStep, boolean hasLower, boolean hasUpper) {
		if(hasStep && !hasLower && !hasUpper)
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Missing lower or upper bound definition"
					+ " - step value meaningless without at least one of them");
	}

	private static String stepAndBoundsMismatch(Object step, Object lower, Object upper) {
		return String.format("Inconsistent boundaries definition"
				+ " - difference of min [%s] and max [%s] is not a multiple of step size [%s]",
				lower, upper, step);
	}

	public abstract static class NumberVerifier extends ValueVerifier {

		protected final boolean hasLower;
		protected final boolean hasUpper;
		protected final boolean hasStep;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		protected NumberVerifier(@Nullable ValueRange valueRange, @Nullable ValueSet valueSet) {
			super(valueRange, valueSet);

			hasLower = valueRange!=null && valueRange.getLowerBound().isPresent();
			hasUpper = valueRange!=null && valueRange.getUpperBound().isPresent();
			hasStep = valueRange!=null && valueRange.getStepSize().isPresent();

			checkStepAndBounds(hasStep, hasLower, hasUpper);
		}
	}

	public static class IntVerifier extends NumberVerifier {

		public static IntVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.INTEGER)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for integer verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new IntVerifier(annotationManifest.getValueRange().orElse(null),
					annotationManifest.getValueSet().orElse(null));
		}

		private final int lower, upper, step;
		private final IntSet allowedValues;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public IntVerifier(@Nullable ValueRange valueRange, @Nullable ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = hasLower ? ((Number)valueRange.getLowerBound().get()).intValue() : UNSET_INT;
			upper = hasUpper ? ((Number)valueRange.getUpperBound().get()).intValue() : UNSET_INT;
			step = hasStep ? ((Number)valueRange.getStepSize().get()).intValue() : UNSET_INT;

			if(hasStep && hasLower && hasUpper
					&& (upper-lower)%step!=0)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						stepAndBoundsMismatch(Integer.valueOf(step),
								Integer.valueOf(lower), Integer.valueOf(upper)));

			if(valueSet!=null) {
				allowedValues = new IntOpenHashSet(valueSet.valueCount());
				valueSet.forEach(v -> allowedValues.add(((Number)v).intValue()));
			} else {
				allowedValues = null;
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier#verify(java.lang.Object)
		 */
		@Override
		public VerificationResult verify(Object value) {
			return verifyInt(((Number)value).intValue());
		}

		public VerificationResult verifyInt(int value) {

			if(allowedValues!=null && !allowedValues.contains(value)) {
				return VerificationResult.VALUE_NOT_IN_SET;
			}

			if(hasLower && Integer.compare(lower, value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(hasUpper && Integer.compare(upper, value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(hasStep) {
				if((hasLower && (value-lower)%step!=0)
						|| (hasUpper && (upper-value)%step!=0)) {
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}

	public static class LongVerifier extends NumberVerifier {

		public static LongVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.LONG)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for long integer verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new LongVerifier(annotationManifest.getValueRange().orElse(null),
					annotationManifest.getValueSet().orElse(null));
		}

		private final long lower, upper, step;
		private final LongSet allowedValues;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public LongVerifier(@Nullable ValueRange valueRange, @Nullable ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = hasLower ? ((Number)valueRange.getLowerBound().get()).longValue() : UNSET_LONG;
			upper = hasUpper ? ((Number)valueRange.getUpperBound().get()).longValue() : UNSET_LONG;
			step = hasStep ? ((Number)valueRange.getStepSize().get()).longValue() : UNSET_LONG;

			if(hasStep && hasLower && hasUpper
					&& (upper-lower)%step!=0)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						stepAndBoundsMismatch(Long.valueOf(step),
								Long.valueOf(lower), Long.valueOf(upper)));

			if(valueSet!=null) {
				allowedValues = new LongOpenHashSet(valueSet.valueCount());
				valueSet.forEach(v -> allowedValues.add(((Number)v).longValue()));
			} else {
				allowedValues = null;
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier#verify(java.lang.Object)
		 */
		@Override
		public VerificationResult verify(Object value) {
			return verifyLong(((Number)value).longValue());
		}

		public VerificationResult verifyLong(long value) {

			if(allowedValues!=null && !allowedValues.contains(value)) {
				return VerificationResult.VALUE_NOT_IN_SET;
			}

			if(hasLower && Long.compare(lower, value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(hasUpper && Long.compare(upper, value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(hasStep) {
				if((hasLower && (value-lower)%step!=0)
						|| (hasUpper && (upper-value)%step!=0)) {
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}

	public static class FloatVerifier extends NumberVerifier {

		public static FloatVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.FLOAT)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for float verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new FloatVerifier(annotationManifest.getValueRange().orElse(null),
					annotationManifest.getValueSet().orElse(null));
		}

		private final float lower, upper, step;
		private final FloatSet allowedValues;

		private final float stepInvert, accuracy;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public FloatVerifier(@Nullable ValueRange valueRange, @Nullable ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = hasLower ? ((Number)valueRange.getLowerBound().get()).floatValue() : UNSET_FLOAT;
			upper = hasUpper ? ((Number)valueRange.getUpperBound().get()).floatValue() : UNSET_FLOAT;
			step = hasStep ? ((Number)valueRange.getStepSize().get()).floatValue() : UNSET_FLOAT;

			accuracy = hasStep ? step * 0.001F : UNSET_FLOAT;
			stepInvert = hasStep ? 1 / step : UNSET_FLOAT;

			if(hasStep && hasLower && hasUpper) {
				float steps = (upper-lower) * stepInvert;
				if((steps-Math.floor(steps))>accuracy)
					throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
							stepAndBoundsMismatch(Float.valueOf(step),
									Float.valueOf(lower), Float.valueOf(upper)));
			}

			if(valueSet!=null) {
				allowedValues = new FloatOpenHashSet(valueSet.valueCount());
				valueSet.forEach(v -> allowedValues.add(((Number)v).floatValue()));
			} else {
				allowedValues = null;
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier#verify(java.lang.Object)
		 */
		@Override
		public VerificationResult verify(Object value) {
			return verifyFloat(((Number)value).floatValue());
		}

		public VerificationResult verifyFloat(float value) {

			if(allowedValues!=null && !allowedValues.contains(value)) {
				return VerificationResult.VALUE_NOT_IN_SET;
			}

			if(hasLower && Float.compare(lower, value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(hasUpper && Float.compare(upper, value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(hasStep) {
				// Calculate the step and delta
				float steps = (hasLower ? (value-lower) : (upper-value)) * stepInvert;
				if((steps-Math.floor(steps))>accuracy) {
					// Precision mismatch is the case if delta exceeds accuracy
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}

	public static class DoubleVerifier extends NumberVerifier {

		public static DoubleVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.DOUBLE)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for double verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new DoubleVerifier(annotationManifest.getValueRange().orElse(null),
					annotationManifest.getValueSet().orElse(null));
		}

		private final double lower, upper, step;
		private final DoubleSet allowedValues;

		private final double stepInvert, accuracy;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public DoubleVerifier(@Nullable ValueRange valueRange, @Nullable ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = hasLower ? ((Number)valueRange.getLowerBound().get()).doubleValue() : UNSET_DOUBLE;
			upper = hasUpper ? ((Number)valueRange.getUpperBound().get()).doubleValue() : UNSET_DOUBLE;
			step = hasStep ? ((Number)valueRange.getStepSize().get()).doubleValue() : UNSET_DOUBLE;

			accuracy = hasStep ? step * 0.000001 : UNSET_DOUBLE;
			stepInvert = hasStep ? 1 / step : UNSET_DOUBLE;

			if(hasStep && hasLower && hasUpper) {
				double steps = (upper-lower) * stepInvert;
				if((steps-Math.floor(steps))>accuracy)
					throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
							stepAndBoundsMismatch(Double.valueOf(step),
									Double.valueOf(lower), Double.valueOf(upper)));
			}

			if(valueSet!=null) {
				allowedValues = new DoubleOpenHashSet(valueSet.valueCount());
				valueSet.forEach(v -> allowedValues.add(((Number)v).doubleValue()));
			} else {
				allowedValues = null;
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier#verify(java.lang.Object)
		 */
		@Override
		public VerificationResult verify(Object value) {
			return verifyDouble(((Number)value).doubleValue());
		}

		public VerificationResult verifyDouble(double value) {

			if(allowedValues!=null && !allowedValues.contains(value)) {
				return VerificationResult.VALUE_NOT_IN_SET;
			}

			if(hasLower && Double.compare(lower, value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(hasUpper && Double.compare(upper, value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(hasStep) {
				// Calculate the step and delta
				double steps = (hasLower ? (value-lower) : (upper-value)) * stepInvert;
				if((steps-Math.floor(steps))>accuracy) {
					// Precision mismatch is the case if delta exceeds accuracy
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}
}
