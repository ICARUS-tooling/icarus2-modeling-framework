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

import java.util.Optional;
import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ValueVerifier {

	public static ValueVerifier forAnnotation(AnnotationManifest annotationManifest) {
		if(annotationManifest.isAllowUnknownValues()) {
			return null;
		}

		ValueType valueType = annotationManifest.getValueType();

		switch (valueType.getName()) {
		case ValueType.BOOLEAN_TYPE_LABEL: return null; // no point in checking boolean results ?
		case ValueType.INTEGER_TYPE_LABEL: return IntVerifier.forAnnotation(annotationManifest);
		case ValueType.LONG_TYPE_LABEL: return LongVerifier.forAnnotation(annotationManifest);
		case ValueType.FLOAT_TYPE_LABEL: return FloatVerifier.forAnnotation(annotationManifest);
		case ValueType.DOUBLE_TYPE_LABEL: return DoubleVerifier.forAnnotation(annotationManifest);

		default:
			return ObjectVerifier.forAnnotation(annotationManifest);
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
		 * The value is missing in the
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

	protected final Optional<ValueRange> valueRange;
	protected final Optional<ValueSet> valueSet;

	protected final int lowerCompRequired, upperCompRequired;

	/**
	 * @param valueRange
	 * @param valueSet
	 */
	public ValueVerifier(AnnotationManifest manifest) {
		valueRange = manifest.getValueRange();
		valueSet = manifest.getValueSet();

		lowerCompRequired = valueRange.map(ValueRange::isLowerBoundInclusive)
				.orElse(Boolean.FALSE).booleanValue() ? 0 : -1;
		upperCompRequired = valueRange.map(ValueRange::isUpperBoundInclusive)
				.orElse(Boolean.FALSE).booleanValue() ? 0 : 1;
	}

	public abstract VerificationResult verify(Object value);


	public static class ObjectVerifier extends ValueVerifier {

		public static ObjectVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new ObjectVerifier(annotationManifest);
		}

		@SuppressWarnings("rawtypes")
		private final Optional<Comparable> lower, upper;
		private final Optional<Set<Object>> allowedValues;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		private ObjectVerifier(AnnotationManifest annotationManifest) {
			super(annotationManifest);

			lower = valueRange.flatMap(r -> r.getLowerBound());
			upper = valueRange.flatMap(r -> r.getUpperBound());

			allowedValues = valueSet.map(ValueSet::getValues);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier#verify(java.lang.Object)
		 */
		@Override
		public VerificationResult verify(Object value) {
			return tryVerify(value).orElseGet(() -> verifyStepSize(value));
		}

		protected Optional<VerificationResult> tryVerify(Object value) {
			if(allowedValues.isPresent() && !allowedValues.get().contains(value)) {
				return Optional.of(VerificationResult.VALUE_NOT_IN_SET);
			}

			if(lower.isPresent() && lower.get().compareTo((Comparable<?>)value)>lowerCompRequired) {
				return Optional.of(VerificationResult.LOWER_BOUNDARY_VIOLATION);
			}

			if(upper.isPresent() && upper.get().compareTo((Comparable<?>)value)<upperCompRequired) {
				return Optional.of(VerificationResult.UPPER_BOUNDARY_VIOLATION);
			}

			// No check about step length here since general Comparable objects don't have any inherent "precision"

			return Optional.empty();
		}

		protected VerificationResult verifyStepSize(Object value) {
			return VerificationResult.VALID;
		}
	}

	public static class IntVerifier extends ObjectVerifier {

		public static IntVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.INTEGER)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for integer verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new IntVerifier(annotationManifest);
		}

		private final Optional<Number> step;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		private IntVerifier(AnnotationManifest annotationManifest) {
			super(annotationManifest);

			// Takes care of casting the step size to proper Number
			step = valueRange.flatMap(ValueRange::getStepSize)
					.map(c -> (Number)c);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier#verifyStepSize(java.lang.Object)
		 */
		@Override
		protected VerificationResult verifyStepSize(Object value) {
			return step.map(s -> ((Number)value).intValue()%s.intValue()!=0 ?
					VerificationResult.PRECISION_MISMATCH : null).orElse(VerificationResult.VALID);
		}
	}

	public static class LongVerifier extends ObjectVerifier {

		public static LongVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.LONG)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for long integer verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new LongVerifier(annotationManifest);
		}

		private final Optional<Number>step;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		public LongVerifier(AnnotationManifest annotationManifest) {
			super(annotationManifest);

			// Takes care of casting the step size to proper Number
			step = valueRange.flatMap(ValueRange::getStepSize)
					.map(c -> (Number)c);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier#verifyStepSize(java.lang.Object)
		 */
		@Override
		protected VerificationResult verifyStepSize(Object value) {
			return step.map(s -> ((Number)value).longValue()%s.longValue()!=0 ?
					VerificationResult.PRECISION_MISMATCH : null).orElse(VerificationResult.VALID);
		}
	}

	public static class FloatVerifier extends ObjectVerifier {

		public static FloatVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.FLOAT)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for float verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new FloatVerifier(annotationManifest);
		}

		private final Optional<Number>step;

		private final float stepInvert, accuracy;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		private FloatVerifier(AnnotationManifest annotationManifest) {
			super(annotationManifest);

			// Takes care of casting the step size to proper Number
			step = valueRange.flatMap(ValueRange::getStepSize)
					.map(c -> (Number)c);

			accuracy = step.isPresent() ? step.get().floatValue() * 0.001F : 0F;
			stepInvert = step.isPresent() ?1 / step.get().floatValue() : 0F;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier#verifyStepSize(java.lang.Object)
		 */
		@Override
		protected VerificationResult verifyStepSize(Object value) {
			if(step.isPresent()) {
				// Calculate the step and delta
				float steps = ((Number)value).floatValue() * stepInvert;
				if((steps-Math.floor(steps))>accuracy) {
					// Precision mismatch is the case if delta exceeds accuracy
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}

	public static class DoubleVerifier extends ObjectVerifier {

		public static DoubleVerifier forAnnotation(AnnotationManifest annotationManifest) {
			if(annotationManifest.getValueType()!=ValueType.DOUBLE)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"Unsupported value type for double verifier: "+annotationManifest.getValueType());

			if(annotationManifest.isAllowUnknownValues()) {
				return null;
			}

			return new DoubleVerifier(annotationManifest);
		}

		private final Optional<Number> step;

		private final double stepInvert, accuracy;

		/**
		 * @param valueRange
		 * @param valueSet
		 */
		private DoubleVerifier(AnnotationManifest annotationManifest) {
			super(annotationManifest);

			// Takes care of casting the step size to proper Number
			step = valueRange.flatMap(ValueRange::getStepSize)
					.map(c -> (Number)c);

			accuracy = step.isPresent() ? step.get().floatValue() * 0.000001 : 0D;
			stepInvert = step.isPresent() ?1 / step.get().floatValue() : 0D;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier#verifyStepSize(java.lang.Object)
		 */
		@Override
		protected VerificationResult verifyStepSize(Object value) {
			if(step.isPresent()) {
				// Calculate the step and delta
				double steps = ((Number)value).doubleValue() * stepInvert;
				if((steps-Math.floor(steps))>accuracy) {
					// Precision mismatch is the case if delta exceeds accuracy
					return VerificationResult.PRECISION_MISMATCH;
				}
			}

			return VerificationResult.VALID;
		}
	}
}
