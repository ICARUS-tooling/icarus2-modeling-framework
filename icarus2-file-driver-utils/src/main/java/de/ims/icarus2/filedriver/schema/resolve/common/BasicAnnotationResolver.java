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
package de.ims.icarus2.filedriver.schema.resolve.common;

import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._float;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.types.ValueConversionException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ValueVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.DoubleVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.FloatVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.IntVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.LongVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.VerificationResult;
import de.ims.icarus2.model.util.func.AnnotationConsumer;
import de.ims.icarus2.model.util.func.AnnotationConsumer.BooleanAnnotationConsumer;
import de.ims.icarus2.model.util.func.AnnotationConsumer.DoubleAnnotationConsumer;
import de.ims.icarus2.model.util.func.AnnotationConsumer.FloatAnnotationConsumer;
import de.ims.icarus2.model.util.func.AnnotationConsumer.IntAnnotationConsumer;
import de.ims.icarus2.model.util.func.AnnotationConsumer.LongAnnotationConsumer;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BasicAnnotationResolver<E extends Object> implements Resolver {

	private static AnnotationManifest resolveKey(AnnotationLayerManifest manifest, String key) {
		return manifest.getAnnotationManifest(key)
				.orElseThrow(ManifestException.missing(manifest, "annotation manifest for key "+key));
	}

	/**
	 * Creates a type-optimized instance of {@link BasicAnnotationResolver} that verifies
	 * the data it reads based on the given layer's {@link AnnotationManifest manifest} and
	 * directly stores it into the layer's {@link AnnotationStorage}.
	 *
	 * @param layer
	 * @param annotationKey
	 * @return
	 */
	public static BasicAnnotationResolver<?> forAnnotation(AnnotationLayer layer, String annotationKey) {
		requireNonNull(layer);
		requireNonNull(annotationKey);

		AnnotationLayerManifest layerManifest = layer.getManifest();
		AnnotationManifest annotationManifest = resolveKey(layerManifest, annotationKey);
		AnnotationStorage annotationStorage = layer.getAnnotationStorage();

		ValueType valueType = annotationManifest.getValueType();

		switch (valueType.getName()) {
		case ValueType.STRING_TYPE_LABEL: return new StringAnnotationResolver(
				annotationStorage::setString, annotationManifest, annotationKey);
		case ValueType.INTEGER_TYPE_LABEL: return new IntAnnotationResolver(
				annotationStorage::setInteger, annotationManifest, annotationKey);
		case ValueType.LONG_TYPE_LABEL: return new LongAnnotationResolver(
				annotationStorage::setLong, annotationManifest, annotationKey);
		case ValueType.FLOAT_TYPE_LABEL: return new FloatAnnotationResolver(
				annotationStorage::setFloat, annotationManifest, annotationKey);
		case ValueType.DOUBLE_TYPE_LABEL: return new DoubleAnnotationResolver(
				annotationStorage::setDouble, annotationManifest, annotationKey);
		case ValueType.BOOLEAN_TYPE_LABEL: return new BooleanAnnotationResolver(
				annotationStorage::setBoolean, annotationManifest, annotationKey);

		default:
			return new ObjectAnnotationResolver(annotationStorage::setValue, annotationManifest, annotationKey);
		}
	}

	/**
	 * Creates a type-optimized instance of {@link BasicAnnotationResolver} that verifies
	 * the data it reads based on the given layer's {@link AnnotationManifest manifest} and
	 * instead of storing it into the layer's {@link AnnotationStorage} uses the specified
	 * {@link AnnotationConsumer consumer}.
	 *
	 * @param layer
	 * @param annotationKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Object> BasicAnnotationResolver<?> forAnnotationWithConsumer(AnnotationLayer layer,
			String annotationKey, AnnotationConsumer<E> annotationConsumer) {
		requireNonNull(layer);
		requireNonNull(annotationKey);

		AnnotationLayerManifest layerManifest = layer.getManifest();
		AnnotationManifest annotationManifest = resolveKey(layerManifest, annotationKey);
		AnnotationStorage annotationStorage = layer.getAnnotationStorage();

		ValueType valueType = annotationManifest.getValueType();

		switch (valueType.getName()) {
		case ValueType.STRING_TYPE_LABEL: return new StringAnnotationResolver(
				(AnnotationConsumer<String>) annotationConsumer, annotationManifest, annotationKey);
		case ValueType.INTEGER_TYPE_LABEL: return new IntAnnotationResolver(
				(AnnotationConsumer<Integer>) annotationConsumer, annotationManifest, annotationKey);
		case ValueType.LONG_TYPE_LABEL: return new LongAnnotationResolver(
				(AnnotationConsumer<Long>) annotationConsumer, annotationManifest, annotationKey);
		case ValueType.FLOAT_TYPE_LABEL: return new FloatAnnotationResolver(
				(AnnotationConsumer<Float>) annotationConsumer, annotationManifest, annotationKey);
		case ValueType.DOUBLE_TYPE_LABEL: return new DoubleAnnotationResolver(
				(AnnotationConsumer<Double>) annotationConsumer, annotationManifest, annotationKey);
		case ValueType.BOOLEAN_TYPE_LABEL: return new BooleanAnnotationResolver(
				(AnnotationConsumer<Boolean>) annotationConsumer, annotationManifest, annotationKey);

		default:
			return new ObjectAnnotationResolver(annotationStorage::setValue, annotationManifest, annotationKey);
		}
	}

	protected final AnnotationConsumer<E> annotationConsumer;
	protected final String annotationKey;
	protected final ValueType valueType;

	/**
	 * @param annotationConsumer
	 * @param annotationKey
	 * @param valueType
	 */
	protected BasicAnnotationResolver(AnnotationConsumer<E> annotationConsumer,
			AnnotationManifest annotationManifest, String annotationKey) {
		this.annotationConsumer = requireNonNull(annotationConsumer);
		this.annotationKey = requireNonNull(annotationKey);
		this.valueType = requireNonNull(annotationManifest).getValueType();
	}

	public static class ObjectAnnotationResolver extends BasicAnnotationResolver<Object> {

		private final ValueVerifier.ObjectVerifier verifier;

		public ObjectAnnotationResolver(AnnotationConsumer<Object> annotationConsumer,
				AnnotationManifest annotationManifest, String annotationKey) {
			super(annotationConsumer, annotationManifest, annotationKey);

			verifier = ObjectVerifier.forAnnotation(annotationManifest);
		}

		/**
		 * @throws ValueConversionException
		 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(java.lang.String)
		 */
		@Override
		public Item process(ResolverContext context) throws ValueConversionException {
			Object value = valueType.parse(context.rawData(), BasicAnnotationResolver.class.getClassLoader());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verify(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			// Now that we have verified the correctness of our value we can use a persistent form
			value = valueType.persist(value);

			annotationConsumer.apply(context.currentItem(), annotationKey, value);

			return context.currentItem();
		}
	}

	public static class StringAnnotationResolver extends BasicAnnotationResolver<String> {

		private final ValueVerifier.ObjectVerifier verifier;

		public StringAnnotationResolver(AnnotationConsumer<String> annotationConsumer,
				AnnotationManifest annotationManifest, String annotationKey) {
			super(annotationConsumer, annotationManifest, annotationKey);

			verifier = ObjectVerifier.forAnnotation(annotationManifest);
		}

		/**
		 * @throws ValueConversionException
		 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(java.lang.String)
		 */
		@Override
		public Item process(ResolverContext context) throws ValueConversionException {
			Object value = valueType.parse(context.rawData(), BasicAnnotationResolver.class.getClassLoader());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verify(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			// Now that we have verified the correctness of our value we can use a persistent form
			value = valueType.persist(value);

			annotationConsumer.apply(context.currentItem(), annotationKey, (String)value);

			return context.currentItem();
		}
	}

	public static class IntAnnotationResolver extends BasicAnnotationResolver<Integer> {

		private final ValueVerifier.IntVerifier verifier;
		private final boolean isPrimitiveConsumer;

		public IntAnnotationResolver(AnnotationConsumer<Integer> annotationConsumer,
				AnnotationManifest annotationManifest, String annotationKey) {
			super(annotationConsumer, annotationManifest, annotationKey);

			verifier = IntVerifier.forAnnotation(annotationManifest);
			isPrimitiveConsumer = annotationConsumer instanceof IntAnnotationConsumer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.common.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueType.INTEGER.parse() method to prevent boxing!
			final int value = StringPrimitives.parseInt(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyInt(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			if(isPrimitiveConsumer) {
				((IntAnnotationConsumer)annotationConsumer).applyInt(context.currentItem(), annotationKey, value);
			} else {
				annotationConsumer.apply(context.currentItem(), annotationKey, _int(value));
			}

			return context.currentItem();
		}
	}

	public static class LongAnnotationResolver extends BasicAnnotationResolver<Long> {

		private final ValueVerifier.LongVerifier verifier;
		private final boolean isPrimitiveConsumer;

		public LongAnnotationResolver(AnnotationConsumer<Long> annotationConsumer,
				AnnotationManifest annotationManifest, String annotationKey) {
			super(annotationConsumer, annotationManifest, annotationKey);

			verifier = LongVerifier.forAnnotation(annotationManifest);
			isPrimitiveConsumer = annotationConsumer instanceof LongAnnotationConsumer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.common.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueType.LONG.parse() method to prevent boxing!
			final long value = StringPrimitives.parseLong(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyLong(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			if(isPrimitiveConsumer) {
				((LongAnnotationConsumer)annotationConsumer).applyLong(context.currentItem(), annotationKey, value);
			} else {
				annotationConsumer.apply(context.currentItem(), annotationKey, _long(value));
			}

			return context.currentItem();
		}
	}

	public static class FloatAnnotationResolver extends BasicAnnotationResolver<Float> {

		private final ValueVerifier.FloatVerifier verifier;
		private final boolean isPrimitiveConsumer;

		public FloatAnnotationResolver(AnnotationConsumer<Float> annotationConsumer,
				AnnotationManifest annotationManifest, String annotationKey) {
			super(annotationConsumer, annotationManifest, annotationKey);

			verifier = FloatVerifier.forAnnotation(annotationManifest);
			isPrimitiveConsumer = annotationConsumer instanceof FloatAnnotationConsumer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.common.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueType.FLOAT.parse() method to prevent boxing!
			final float value = StringPrimitives.parseFloat(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyFloat(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			if(isPrimitiveConsumer) {
				((FloatAnnotationConsumer)annotationConsumer).applyFloat(context.currentItem(), annotationKey, value);
			} else {
				annotationConsumer.apply(context.currentItem(), annotationKey, _float(value));
			}

			return context.currentItem();
		}
	}

	public static class DoubleAnnotationResolver extends BasicAnnotationResolver<Double> {

		private final ValueVerifier.DoubleVerifier verifier;
		private final boolean isPrimitiveConsumer;

		public DoubleAnnotationResolver(AnnotationConsumer<Double> annotationConsumer,
				AnnotationManifest annotationManifest, String annotationKey) {
			super(annotationConsumer, annotationManifest, annotationKey);

			verifier = DoubleVerifier.forAnnotation(annotationManifest);
			isPrimitiveConsumer = annotationConsumer instanceof DoubleAnnotationConsumer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.common.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueType.DOUBLE.parse() method to prevent boxing!
			final double value = StringPrimitives.parseDouble(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyDouble(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			if(isPrimitiveConsumer) {
				((DoubleAnnotationConsumer)annotationConsumer).applyDouble(context.currentItem(), annotationKey, value);
			} else {
				annotationConsumer.apply(context.currentItem(), annotationKey, _double(value));
			}

			return context.currentItem();
		}
	}

	public static class BooleanAnnotationResolver extends BasicAnnotationResolver<Boolean> {

		private final boolean isPrimitiveConsumer;

		public BooleanAnnotationResolver(AnnotationConsumer<Boolean> annotationConsumer,
				AnnotationManifest annotationManifest, String annotationKey) {
			super(annotationConsumer, annotationManifest, annotationKey);

			isPrimitiveConsumer = annotationConsumer instanceof BooleanAnnotationConsumer;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.common.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueType.BOOLEAN.parse() method to prevent boxing!
			boolean value = StringPrimitives.parseBoolean(context.rawData());

			if(isPrimitiveConsumer) {
				((BooleanAnnotationConsumer)annotationConsumer).applyBoolean(context.currentItem(), annotationKey, value);
			} else {
				annotationConsumer.apply(context.currentItem(), annotationKey, _boolean(value));
			}

			return context.currentItem();
		}
	}

}
