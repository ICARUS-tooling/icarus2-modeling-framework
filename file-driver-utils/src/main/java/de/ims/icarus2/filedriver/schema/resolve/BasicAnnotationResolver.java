/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.filedriver.schema.resolve;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ValueVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.DoubleVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.FloatVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.IntVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.LongVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.ObjectVerifier;
import de.ims.icarus2.model.manifest.util.ValueVerifier.VerificationResult;
import de.ims.icarus2.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BasicAnnotationResolver implements Resolver {

	public static BasicAnnotationResolver forAnnotation(AnnotationLayer layer, String annotationKey) {
		AnnotationLayerManifest layerManifest = layer.getManifest();
		if(annotationKey==null) {
			annotationKey = layerManifest.getDefaultKey();
		}
		AnnotationManifest annotationManifest = layerManifest.getAnnotationManifest(annotationKey);
		AnnotationStorage annotationStorage = layer.getAnnotationStorage();

		ValueType valueType = annotationManifest.getValueType();

		switch (valueType.getName()) {
		case ValueType.INTEGER_TYPE_LABEL: return new IntAnnotationResolver(annotationStorage, annotationManifest);
		case ValueType.LONG_TYPE_LABEL: return new LongAnnotationResolver(annotationStorage, annotationManifest);
		case ValueType.FLOAT_TYPE_LABEL: return new FloatAnnotationResolver(annotationStorage, annotationManifest);
		case ValueType.DOUBLE_TYPE_LABEL: return new DoubleAnnotationResolver(annotationStorage, annotationManifest);
		case ValueType.BOOLEAN_TYPE_LABEL: return new BooleanAnnotationResolver(annotationStorage, annotationManifest);

		default:
			return new ObjectAnnotationResolver(annotationStorage, annotationManifest);
		}
	}

	protected final AnnotationStorage annotationStorage;
	protected final String annotationKey;
	protected final ValueType valueType;

	/**
	 * @param annotationStorage
	 * @param annotationKey
	 * @param valueType
	 */
	protected BasicAnnotationResolver(AnnotationStorage annotationStorage,
			AnnotationManifest annotationManifest) {
		this.annotationStorage = annotationStorage;
		this.annotationKey = annotationManifest.getKey();
		this.valueType = annotationManifest.getValueType();
	}

	public static class ObjectAnnotationResolver extends BasicAnnotationResolver {

		private final ValueVerifier.ObjectVerifier verifier;

		public ObjectAnnotationResolver(AnnotationStorage annotationStorage,
				AnnotationManifest annotationManifest) {
			super(annotationStorage, annotationManifest);

			verifier = ObjectVerifier.forAnnotation(annotationManifest);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(java.lang.String)
		 */
		@Override
		public Item process(ResolverContext context) {
			final Object value = valueType.parse(context.rawData(), BasicAnnotationResolver.class.getClassLoader());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verify(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			annotationStorage.setValue(context.currentItem(), annotationKey, value);

			return context.currentItem();
		}
	}

	public static class IntAnnotationResolver extends BasicAnnotationResolver {

		private final ValueVerifier.IntVerifier verifier;

		public IntAnnotationResolver(AnnotationStorage annotationStorage,
				AnnotationManifest annotationManifest) {
			super(annotationStorage, annotationManifest);

			verifier = IntVerifier.forAnnotation(annotationManifest);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueTyoe.INTEGER.parse() method to prevent boxing!
			final int value = StringPrimitives.parseInt(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyInt(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			annotationStorage.setIntegerValue(context.currentItem(), annotationKey, value);

			return context.currentItem();
		}
	}

	public static class LongAnnotationResolver extends BasicAnnotationResolver {

		private final ValueVerifier.LongVerifier verifier;

		public LongAnnotationResolver(AnnotationStorage annotationStorage,
				AnnotationManifest annotationManifest) {
			super(annotationStorage, annotationManifest);

			verifier = LongVerifier.forAnnotation(annotationManifest);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueTyoe.LONG.parse() method to prevent boxing!
			final long value = StringPrimitives.parseLong(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyLong(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			annotationStorage.setLongValue(context.currentItem(), annotationKey, value);

			return context.currentItem();
		}
	}

	public static class FloatAnnotationResolver extends BasicAnnotationResolver {

		private final ValueVerifier.FloatVerifier verifier;

		public FloatAnnotationResolver(AnnotationStorage annotationStorage,
				AnnotationManifest annotationManifest) {
			super(annotationStorage, annotationManifest);

			verifier = FloatVerifier.forAnnotation(annotationManifest);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueTyoe.FLOAT.parse() method to prevent boxing!
			final float value = StringPrimitives.parseFloat(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyFloat(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			annotationStorage.setFloatValue(context.currentItem(), annotationKey, value);

			return context.currentItem();
		}
	}

	public static class DoubleAnnotationResolver extends BasicAnnotationResolver {

		private final ValueVerifier.DoubleVerifier verifier;

		public DoubleAnnotationResolver(AnnotationStorage annotationStorage,
				AnnotationManifest annotationManifest) {
			super(annotationStorage, annotationManifest);

			verifier = DoubleVerifier.forAnnotation(annotationManifest);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueTyoe.DOUBLE.parse() method to prevent boxing!
			final double value = StringPrimitives.parseDouble(context.rawData());

			if(verifier!=null) {
				VerificationResult verificationResult = verifier.verifyDouble(value);
				if(verificationResult.isError()) {
					verificationResult.throwException(context.rawData().toString());
				}
			}

			annotationStorage.setDoubleValue(context.currentItem(), annotationKey, value);

			return context.currentItem();
		}
	}

	public static class BooleanAnnotationResolver extends BasicAnnotationResolver {

		public BooleanAnnotationResolver(AnnotationStorage annotationStorage,
				AnnotationManifest annotationManifest) {
			super(annotationStorage, annotationManifest);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.BasicAnnotationResolver#process(java.lang.CharSequence, de.ims.icarus2.model.api.members.item.Item)
		 */
		@Override
		public Item process(ResolverContext context) {
			// Bypassing ValueTyoe.BOOLEAN.parse() method to prevent boxing!
			annotationStorage.setBooleanValue(context.currentItem(), annotationKey,
					StringPrimitives.parseBoolean(context.rawData()));

			return context.currentItem();
		}
	}

}
