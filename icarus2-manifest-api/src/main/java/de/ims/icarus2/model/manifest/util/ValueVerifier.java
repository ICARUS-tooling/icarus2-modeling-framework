/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.types.ValueType;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ValueVerifier {

	public static ValueVerifier forAnnotation(AnnotationManifest annotationManifest) {
		if(annotationManifest.isAllowUnknownValues()) {
			return null;
		}

		ValueRange valueRange = annotationManifest.getValueRange();
		ValueSet valueSet = annotationManifest.getValueSet();

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
		 * THe value is missing in the
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
	public ValueVerifier(ValueRange valueRange, ValueSet valueSet) {
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

			return new ObjectVerifier(annotationManifest.getValueRange(), annotationManifest.getValueSet());
		}

		@SuppressWarnings("rawtypes")
		private final Comparable lower, upper;
		private final Set<Object> allowedValues;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		@SuppressWarnings("rawtypes")
		public ObjectVerifier(ValueRange valueRange, ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = valueRange==null ? null : (Comparable)valueRange.getLowerBound();
			upper = valueRange==null ? null : (Comparable)valueRange.getUpperBound();

			if(valueSet!=null) {
				allowedValues = new ReferenceOpenHashSet<>(valueSet.valueCount());
				valueSet.forEachValue(allowedValues::add);
			} else {
				allowedValues = null;
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier#verify(java.lang.Object)
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public VerificationResult verify(Object value) {
			if(allowedValues!=null && !allowedValues.contains(value)) {
				return VerificationResult.VALUE_NOT_IN_SET;
			}

			if(lower!=null && lower.compareTo((Comparable)value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(upper!=null && upper.compareTo((Comparable)value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			// No check about step length here since general Comparable objects don't have any inherent "precision"

			return VerificationResult.VALID;
		}
	}

	public static class IntVerifier extends ValueVerifier {

		public static IntVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.INTEGER)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for integer verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new IntVerifier(annotationManifest.getValueRange(), annotationManifest.getValueSet());
		}

		private final Number lower, upper, step;
		private final IntSet allowedValues;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public IntVerifier(ValueRange valueRange, ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = valueRange==null || valueRange.getLowerBound()==null ? null : (Number)valueRange.getLowerBound();
			upper = valueRange==null || valueRange.getUpperBound()==null ? null : (Number)valueRange.getUpperBound();
			step = valueRange==null || valueRange.getStepSize()==null ? null : (Number)valueRange.getStepSize();

			if(valueSet!=null) {
				allowedValues = new IntOpenHashSet(valueSet.valueCount());
				valueSet.forEachValue(v -> allowedValues.add(((Number)v).intValue()));
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

			if(lower!=null && Integer.compare(lower.intValue(), value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(upper!=null && Integer.compare(upper.intValue(), value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(step!=null && value%((Number)step).intValue()!=0) {
				return VerificationResult.PRECISION_MISMATCH;
			}

			return VerificationResult.VALID;
		}
	}

	public static class LongVerifier extends ValueVerifier {

		public static LongVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.LONG)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for long integer verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new LongVerifier(annotationManifest.getValueRange(), annotationManifest.getValueSet());
		}

		private final Number lower, upper, step;
		private final LongSet allowedValues;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public LongVerifier(ValueRange valueRange, ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = valueRange==null || valueRange.getLowerBound()==null ? null : (Number)valueRange.getLowerBound();
			upper = valueRange==null || valueRange.getUpperBound()==null ? null : (Number)valueRange.getUpperBound();
			step = valueRange==null || valueRange.getStepSize()==null ? null : (Number)valueRange.getStepSize();

			if(valueSet!=null) {
				allowedValues = new LongOpenHashSet(valueSet.valueCount());
				valueSet.forEachValue(v -> allowedValues.add(((Number)v).longValue()));
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

			if(lower!=null && Long.compare(lower.longValue(), value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(upper!=null && Long.compare(upper.longValue(), value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(step!=null && value%((Number)step).longValue()!=0) {
				return VerificationResult.PRECISION_MISMATCH;
			}

			return VerificationResult.VALID;
		}
	}

	public static class FloatVerifier extends ValueVerifier {

		public static FloatVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.FLOAT)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for float verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new FloatVerifier(annotationManifest.getValueRange(), annotationManifest.getValueSet());
		}

		private final Number lower, upper, step;
		private final FloatSet allowedValues;

		private final float stepInvert, accuracy;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public FloatVerifier(ValueRange valueRange, ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = valueRange==null || valueRange.getLowerBound()==null ? null : (Number)valueRange.getLowerBound();
			upper = valueRange==null || valueRange.getUpperBound()==null ? null : (Number)valueRange.getUpperBound();
			step = valueRange==null || valueRange.getStepSize()==null ? null : (Number)valueRange.getStepSize();
			accuracy = step==null ? 0F : step.floatValue() * 0.001F;
			stepInvert = step==null ? 0F : 1 / step.floatValue();

			if(valueSet!=null) {
				allowedValues = new FloatOpenHashSet(valueSet.valueCount());
				valueSet.forEachValue(v -> allowedValues.add(((Number)v).floatValue()));
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

			if(lower!=null && Float.compare(lower.floatValue(), value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(upper!=null && Float.compare(upper.floatValue(), value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(step!=null) {
				// Calculate the step and delta
				float steps = value * stepInvert;
				if((steps-Math.floor(steps))>accuracy) {
					// Precision mismatch is the case if delta exceeds accuracy
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}

	public static class DoubleVerifier extends ValueVerifier {

		public static DoubleVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.DOUBLE)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for double verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new DoubleVerifier(annotationManifest.getValueRange(), annotationManifest.getValueSet());
		}

		private final Number lower, upper, step;
		private final DoubleSet allowedValues;

		private final double stepInvert, accuracy;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public DoubleVerifier(ValueRange valueRange, ValueSet valueSet) {
			super(valueRange, valueSet);

			lower = valueRange==null || valueRange.getLowerBound()==null ? null : (Number)valueRange.getLowerBound();
			upper = valueRange==null || valueRange.getUpperBound()==null ? null : (Number)valueRange.getUpperBound();
			step = valueRange==null || valueRange.getStepSize()==null ? null : (Number)valueRange.getStepSize();
			accuracy = step==null ? 0D : step.doubleValue() * 0.000001;
			stepInvert = step==null ? 0D : 1 / step.doubleValue();

			if(valueSet!=null) {
				allowedValues = new DoubleOpenHashSet(valueSet.valueCount());
				valueSet.forEachValue(v -> allowedValues.add(((Number)v).doubleValue()));
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

			if(lower!=null && Double.compare(lower.doubleValue(), value)>lowerCompRequired) {
				return VerificationResult.LOWER_BOUNDARY_VIOLATION;
			}

			if(upper!=null && Double.compare(upper.doubleValue(), value)<upperCompRequired) {
				return VerificationResult.UPPER_BOUNDARY_VIOLATION;
			}

			if(step!=null) {
				// Calculate the step and delta
				double steps = value * stepInvert;
				if((steps-Math.floor(steps))>accuracy) {
					// Precision mismatch is the case if delta exceeds accuracy
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}
}
