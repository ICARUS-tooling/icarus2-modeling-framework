/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.types;

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._double;
import static de.ims.icarus2.util.lang.Primitives._float;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.Icon;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.EnumType;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.Wrapper;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.icon.IconWrapper;
import de.ims.icarus2.util.icon.ImageSerializer;
import de.ims.icarus2.util.lang.Primitives;
import de.ims.icarus2.util.nio.ByteArrayChannel;
import de.ims.icarus2.util.nio.ByteChannelCharacterSequence;
import de.ims.icarus2.util.strings.NamedObject;
import de.ims.icarus2.util.strings.StringPrimitives;
import de.ims.icarus2.util.strings.StringResource;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * Models type specifications/restrictions for the manifest framework.
 * <p>
 * Currently we do not support nesting of {@link VectorType} or {@link MatrixType}
 * definitions for use as component types in those two special families of
 * value types.
 *
 * @author Markus Gärtner
 *
 */
@EnumType(method="parseValueType", key="integer")
public class ValueType implements StringResource, NamedObject {

	private final Class<?> baseClass;
	private final String xmlForm;
	private final boolean serializable;
	private final boolean basic;

	private static Map<String, ValueType> xmlLookup = new HashMap<>();

	public static ValueType parseValueType(String s) {
		ValueType result = xmlLookup.get(s);

		if(result==null) {
			/*
			 * Try more complex value types next.
			 * This requires the internal checker methods to
			 * be tested first, since the parseXxxType methods
			 * react to invalid input with exceptions.
			 */
			if(MatrixType.isMatrixType(s)) { // matrix first as it is more specific
				result = MatrixType.parseMatrixType(s);
			} else if(VectorType.isVectorType(s)) {
				result = VectorType.parseVectorType(s);
			} else
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_TYPE,
						"Not a known value type definition: "+s); //$NON-NLS-1$
		}

		return result;
	}

	/**
	 * Constructor for external subclasses.
	 *
	 * @param xmlForm
	 * @param baseClass
	 * @param serializable
	 */
	protected ValueType(String xmlForm, Class<?> baseClass, boolean serializable) {
		this(xmlForm, baseClass, serializable, false);
	}

	/**
	 * Constructor for internally predefined types.
	 *
	 * @param xmlForm
	 * @param baseClass
	 * @param serializable
	 * @param basic
	 */
	private ValueType(String xmlForm, Class<?> baseClass, boolean serializable, boolean basic) {
		this(xmlForm, baseClass, true, serializable, basic);
	}

	/**
	 * Constructor used by compound types to decide whether or not they want to be cached
	 *
	 * @param xmlForm
	 * @param baseClass
	 * @param canCache
	 * @param serializable
	 * @param basic
	 */
	private ValueType(String xmlForm, Class<?> baseClass, boolean canCache, boolean serializable, boolean basic) {
		// Make sure every xmlForm is unique
		checkState("Duplicate XML-form of value type: "+xmlForm, !canCache || !xmlLookup.containsKey(xmlForm));

		this.baseClass = baseClass;
		this.xmlForm = xmlForm;
		this.serializable = serializable;
		this.basic = basic;

		if(canCache) {
			xmlLookup.put(xmlForm, this);
		}
	}

	/**
	 * Serializes the given {@code value} object into a {@link CharSequence}.
	 * The return type was chosen to provide more flexibility as opposed to
	 * forcing a return value of type {@link String}.
	 *
	 * @param value
	 * @return
	 */
	public CharSequence toChars(Object value) throws ValueConversionException {
		if(!isSerializable())
			throw new IllegalStateException("Cannot serialize data of type '"+getStringValue()+"'");

		return String.valueOf(value);
	}

	/**
	 * Transform the given {@link CharSequence} into an intermediate
	 * representation suitable for this value type. The returned value
	 * is <b>not</b> required to be persistent. If client code wishes to
	 * further use the returned object, it should use the {@link #persist(Object)}
	 * method to transform it into a persistent state.
	 * <p>
	 * The default implementation throws a {@link IllegalArgumentException}.
	 *
	 * @param s
	 * @param classLoader
	 * @return
	 */
	public Object parse(CharSequence s, ClassLoader classLoader) throws ValueConversionException {
		throw new IllegalStateException("Cannot parse data of type '"+getStringValue()+"'");

		//TODO make marker interface for setting a "parse" method (stati) for custom types?
	}

	/**
	 * Transforms the given intermediary representation in a valid
	 * persistent state that is unaffected by further changes to the
	 * given original {@code data} object.
	 * <p>
	 * The default implementation simply returns the {@code data}
	 * argument.
	 *
	 * @param data
	 * @return
	 */
	public Object persist(Object data) {
		return data;
	}

	/**
	 * Combination of {@link #parse(CharSequence, ClassLoader)} and a subsequent
	 * {@link #persist(Object)}.
	 *
	 * @param s
	 * @param classLoader
	 * @return
	 * @throws ValueConversionException if anything went wrong
	 *
	 * @see #parse(CharSequence, ClassLoader)
	 * @see #toChars(Object)
	 */
	public Object parseAndPersist(CharSequence s, ClassLoader classLoader) throws ValueConversionException {
		return persist(parse(s, classLoader));
	}

	/**
	 * Returns whether or not the type has a serializable form that makes it
	 * easily serializable.
	 *
	 * @return
	 */
	public final boolean isSerializable() {
		return serializable;
	}

	public final boolean isBasicType() {
		return basic;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public final String getStringValue() {
		return xmlForm;
	}

	@Override
	public final String getName() {
		return getStringValue();
	}

	public final Class<?> getBaseClass() {
		return baseClass;
	}

	/**
	 * A hook for value type implementations to provide support for
	 * {@link Comparable} features without their {@link #getBaseClass() base class}
	 * actually implementing that interface. We especially need this for the
	 * {@link #STRING} type, as the associated base class {@link CharSequence}
	 * does not implement {@link Comparable}, but the majority of instances
	 * for that type will be {@link String} objects.
	 * <p>
	 * The default implementation return {@code null}.
	 *
	 * @return a {@link Comparator} usable to sort objects for this type or {@code null}
	 * if this type does not support sorting or the underlying {@link #getBaseClass() base class}
	 * already implements {@link Comparable} directly.
	 */
	public Comparator<?> getComparator() {
		return null;
	}

	public static final String UNKNOWN_TYPE_LABEL = "unknwon";
	public static final ValueType UNKNOWN = new ValueType(UNKNOWN_TYPE_LABEL, Object.class, false, true);

	// External
	public static final String CUSTOM_TYPE_LABEL = "custom";
	public static final ValueType CUSTOM = new ValueType(CUSTOM_TYPE_LABEL, Object.class, false, true);

	public static final String EXTENSION_TYPE_LABEL = "extension";

	/**
	 * To reduce dependency we only store the extension's unique id, not the extension itself!
	 *
	 * @deprecated due to decoupling extension or plugin capabilities from the core modules, this is
	 * no longer supported on this level.
	 */
	@Deprecated
	public static final ValueType EXTENSION = new ValueType(EXTENSION_TYPE_LABEL, String.class, false, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return s;
		}

		//FIXME needs access to the corpusmanager and the extension interface
//		/**
//		 *
//		 * @see de.ims.icarus2.model.manifest.types.ValueType#toString(java.lang.Object)
//		 */
//		@Override
//		public String toString(Object value) {
//			Extension extension = (Extension) value;
//			return extension.getUniqueId();
//		}
	};

	public static final String REF_TYPE_LABEL = "ref";
	public static final ValueType REF = new ValueType(REF_TYPE_LABEL, Ref.class, false, false) {
		//FIXME implement (de)serialization of ref objects
	};

	public static final String ENUM_TYPE_LABEL = "enum";
	public static final ValueType ENUM = new ValueType(ENUM_TYPE_LABEL, Enum.class, true, true) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			String[] parts = s.toString().split("@"); //$NON-NLS-1$
			try {
				Class<?> clazz = classLoader.loadClass(parts[0]);

				return Enum.valueOf((Class<Enum>) clazz, parts[1]);
			} catch (ClassNotFoundException e) {
				throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
						"Unable to parse enum parameter: "+s, e); //$NON-NLS-1$
			}
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			Enum<?> enumType = (Enum<?>) value;

			return enumType.getDeclaringClass().getName()+"@"+enumType.name(); //$NON-NLS-1$
		}
	};

	private static Comparator<CharSequence> charSequenceComparator = StringUtil::compare;

	// "Primitive"
	public static final String STRING_TYPE_LABEL = "string";
	public static final ValueType STRING = new ValueType(STRING_TYPE_LABEL, CharSequence.class, true, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return s;
		}

		/**
		 * Uses the given {@link Object object's} {@link Object#toString() toString()} method
		 * to create a potentially new {@code String} instance.
		 *
		 * @param data
		 * @return
		 */
		@Override
		public Object persist(Object data) {
			return data.toString();
		}

		/**
		 * Returns a comparator that mimics {@link String#compareTo(String)}.
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#getComparator()
		 * @see StringUtil#compare(CharSequence, CharSequence)
		 */
		@Override
		public Comparator<?> getComparator() {
			return charSequenceComparator;
		}
	};

	public static final String BOOLEAN_TYPE_LABEL = "boolean";
	public static final ValueType BOOLEAN = new ValueType(BOOLEAN_TYPE_LABEL, Boolean.class, true, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return _boolean(StringPrimitives.parseBoolean(s));
		}
	};

	public static final String INTEGER_TYPE_LABEL = "integer";
	public static final ValueType INTEGER = new ValueType(INTEGER_TYPE_LABEL, Integer.class, true, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return _int(StringPrimitives.parseInt(s));
		}
	};

	public static final String LONG_TYPE_LABEL = "long";
	public static final ValueType LONG = new ValueType(LONG_TYPE_LABEL, Long.class, true, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return _long(StringPrimitives.parseLong(s));
		}
	};

	public static final String DOUBLE_TYPE_LABEL = "double";
	public static final ValueType DOUBLE = new ValueType(DOUBLE_TYPE_LABEL, Double.class, true, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return _double(StringPrimitives.parseDouble(s));
		}
	};

	public static final String FLOAT_TYPE_LABEL = "float";
	public static final ValueType FLOAT = new ValueType(FLOAT_TYPE_LABEL, Float.class, true, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return _float(StringPrimitives.parseFloat(s));
		}
	};

	// Resource identifiers
	public static final String URI_TYPE_LABEL = "uri";
	public static final ValueType URI = new ValueType(URI_TYPE_LABEL, URI.class, false, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			try {
				return new URI(s.toString());
			} catch (URISyntaxException e) {
				throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
						"Serialized form of uri is invalid: "+s, e); //$NON-NLS-1$
			}
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			URI uri = (URI) value;
			return uri.toString();
		}
	};

	// Resource links
	public static final String URL_TYPE_LABEL = "url";
	public static final ValueType URL = new ValueType(URL_TYPE_LABEL, Url.class, true, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			try {
				return new Url(s.toString());
			} catch (MalformedURLException e) {
				throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
						"Serialized form of url is invalid: "+s, e); //$NON-NLS-1$
			}
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			Url url = (Url) value;
			return url.getURL().toExternalForm();
		}
	};

	// Resource links
	public static final String FILE_TYPE_LABEL = "file";
	public static final ValueType FILE = new ValueType(FILE_TYPE_LABEL, Path.class, false, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return Paths.get(s.toString());
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			Path path = (Path) value;
			return path.toString();
		}
	};

	public static final String URL_RESOURCE_TYPE_LABEL = "url-resource";
	public static final ValueType URL_RESOURCE = new ValueType(URL_RESOURCE_TYPE_LABEL, UrlResource.class, false, true) {
		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			throw new UnsupportedOperationException("Cannot serialize data of type 'url-resource'"); //$NON-NLS-1$
		}
	};

	public static final String LINK_TYPE_LABEL = "link";
	public static final ValueType LINK = new ValueType(LINK_TYPE_LABEL, Link.class, false, true) {

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			throw new UnsupportedOperationException("Cannot serialize data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}
	};

	// Predefined images
	public static final String IMAGE_TYPE_LABEL = "image";
	public static final ValueType IMAGE = new ValueType(IMAGE_TYPE_LABEL, Icon.class, false, true) {
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			//TODO parse string to decide if it should be decoded into an Icon or wrapped
			return new IconWrapper(s.toString());
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			if(value instanceof IconWrapper) {
				return ((IconWrapper)value).getStringValue();
			} else {
				return ImageSerializer.icon2String((Icon) value);
			}
		}
	};

	public static final String IMAGE_RESOURCE_TYPE_LABEL = "image-resource";
	public static final ValueType IMAGE_RESOURCE = new ValueType(IMAGE_RESOURCE_TYPE_LABEL, IconLink.class, false, true) {
		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			throw new UnsupportedOperationException("Cannot serialize data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}
	};

	public static final String BINARY_STREAM_TYPE_LABEL = "binary";
	public static final ValueType BINARY_STREAM = new ValueType(BINARY_STREAM_TYPE_LABEL, SeekableByteChannel.class, true, true) {

		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {
			return ByteArrayChannel.fromChars(s);
		}

		@Override
		public CharSequence toChars(Object value) {
			SeekableByteChannel channel = (SeekableByteChannel) value;
			return new ByteChannelCharacterSequence(channel);
		}
	};

	/**
	 * As a fallback strategy and to reduce declarative workload for manifests
	 * we assume {@link #STRING} to be the default value type in cases where no
	 * explicit declaration is provided.
	 */
	public static ValueType DEFAULT_VALUE_TYPE = STRING;

	protected static Class<?> extractClass(Object value) {
		// Unpack wrapped data
		if(value instanceof Wrapper) {
			value = ((Wrapper<?>)value).get();
		}

		Class<?> type = value.getClass();

		// Honor possibility of dedicated return types for expressions
		if(Expression.class.isAssignableFrom(type)) {
			type = ((Expression)value).getReturnType();

			// We need the (possible) wrapper type since expressions are allowed to
			// declare primitive return types and we deal with wrappers here!
			type = Primitives.wrap(type);
		}

		return type;
	}

	public boolean isValidValue(Object value) {
		return value!=null && isValidType(extractClass(value));
	}

	public boolean isValidType(Class<?> type) {
		return baseClass.isAssignableFrom(type);
	}

	/**
	 * Returns a collection view on all the available value types
	 */
	public static Collection<ValueType> valueTypes() {
		return CollectionUtils.getCollectionProxy(xmlLookup.values());
	}

	public static Set<ValueType> basicValueTypes() {
		return filterIncluding(ValueType::isBasicType);
	}

	public static Set<ValueType> serializableValueTypes() {
		return filterIncluding(ValueType::isSerializable);
	}

	/**
	 * Creates a set view that contains all available value types except the
	 * ones specified in the {@code exclusions} varargs parameter.
	 * @param exclusions
	 * @return
	 */
	public static Set<ValueType> filterWithout(boolean basicOnly, ValueType...exclusions) {
		Set<ValueType> filter = new HashSet<>();

		if(basicOnly) {
			for(ValueType valueType : xmlLookup.values()) {
				if(valueType.isBasicType()) {
					filter.add(valueType);
				}
			}
		} else {
			filter.addAll(xmlLookup.values());
		}

		if(exclusions!=null) {
			for(ValueType type : exclusions) {
				filter.remove(type);
			}
		}

		return filter;
	}

	public static Set<ValueType> filterWithout(ValueType...exclusions) {
		return filterWithout(false, exclusions);
	}

	public static Set<ValueType> filterWithout(Predicate<? super ValueType> p) {
		LazyCollection<ValueType> filter = LazyCollection.lazySet();

		if(p!=null) {
			for(ValueType type : valueTypes()) {
				if(!p.test(type)) {
					filter.add(type);
				}
			}
		}

		return filter.getAsSet();
	}

	/**
	 * Creates a set view that contains only value types specified
	 * in the {@code exclusions} varargs parameter.
	 * @param inclusive
	 * @return
	 */
	public static Set<ValueType> filterIncluding(ValueType...inclusive) {
		LazyCollection<ValueType> filter = LazyCollection.lazySet();

		if(inclusive!=null) {
			for(ValueType type : inclusive) {
				filter.add(type);
			}
		}

		return filter.getAsSet();
	}

	public static Set<ValueType> filterIncluding(Predicate<? super ValueType> p) {
		LazyCollection<ValueType> filter = LazyCollection.lazySet();

		if(p!=null) {
			for(ValueType type : valueTypes()) {
				if(p.test(type)) {
					filter.add(type);
				}
			}
		}

		return filter.getAsSet();
	}

	/**
	 *
	 * @param value
	 * @return
	 *
	 * @throws ManifestException with type {@link ManifestErrorCode#MANIFEST_TYPE_CAST} if the value check fails
	 */
	public Class<?> checkValue(Object value) {
		Class<?> type = extractClass(value);

		if(!isValidType(type))
			throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
					"Incompatible value type "+type.getName()+" for value-type "+xmlForm+" - expected "+baseClass.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return type;
	}

	public void checkValues(Collection<?> values) {
		for(Object value : values) {
			checkValue(value);
		}
	}

	public void checkValues(Object[] values) {
		for(Object value : values) {
			checkValue(value);
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return xmlForm.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof ValueType) {
			return xmlForm.equals(((ValueType)obj).xmlForm);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ValueType@"+xmlForm; //$NON-NLS-1$
	}

	/**
	 * Utility method for the {@link VectorType} and {@link MatrixType} classes
	 * to verify valid types to be used as component type.
	 *
	 * @param type
	 */
	private static void checkComponentType(ValueType type) {
		if(type instanceof VectorType)
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE,
					"Vector types cannot be used as component type: "+type);
		if(type instanceof MatrixType)
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE,
					"Matrix types cannot be used as component type: "+type);
	}

	public static final class DelegatingType extends ValueType {

		/**
		 * Serialization method to transform a single value into
		 * {@link CharSequence}.
		 */
		private final Method toStringMethod;

		/**
		 * Deserialization method to transform a {@link CharSequence}
		 * into a single value object.
		 */
		private final Method parseMethod;

		/**
		 * @param xmlForm
		 * @param baseClass
		 * @param serializable
		 */
		private DelegatingType(String xmlForm, Class<?> baseClass, Method toStringMethod, Method parseMethod) {
			super(xmlForm, baseClass, true);

			this.toStringMethod = requireNonNull(toStringMethod);
			this.parseMethod = requireNonNull(parseMethod);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) throws ValueConversionException {
			Object result;
			try {
				result = Modifier.isStatic(toStringMethod.getModifiers()) ?
						toStringMethod.invoke(null, value)
						: toStringMethod.invoke(value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ValueConversionException("Failed to serialize value: "+value, e, this, value, true);
			}

			return CharSequence.class.cast(result);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.CharSequence, java.lang.ClassLoader)
		 */
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) throws ValueConversionException {
			Object result;
			try {
				result = parseMethod.invoke(null, s);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ValueConversionException("Failed to parse input: "+s, e, this, s, true);
			}
			return result;
		}
	}

	/**
	 *
	 * The serialized form of this value type adheres to the following format:<br>
	 * {@code <component_type>[<size>]}<br>
	 * Where {@code size} is either a valid positive integer defining the number
	 * of elements in the vector, or the <i>wildcard symbol</i> {@ x} to signal
	 * that vectors for this type can vary in size.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class VectorType extends ValueType {

		static boolean isVectorType(String s) {
			int last = s.length()-1;
			int open = s.indexOf(SIZE_OPEN);
			return open>0 && open<last && s.charAt(last)==SIZE_CLOSE;
		}

		public static VectorType parseVectorType(String s) {
			VectorType result;

			int openIdx = s.indexOf(SIZE_OPEN);
			if(openIdx!=-1) {
				if(s.charAt(s.length()-1)!=SIZE_CLOSE)
					throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_TYPE,
							"Invalid vector type definition: "+s);

				String typeName = s.substring(0, openIdx);

				ValueType componentType = parseValueType(typeName);

				String sizeString = s.substring(openIdx+1, s.length()-1);

				if(WILDCARD_SIZE_STRING.equals(sizeString)) {
					result = withUndefinedSize(componentType);
				} else {
					int size = Integer.parseInt(sizeString);

					result = withSize(componentType, size);
				}
			} else
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_TYPE,
						"Not a valid vector type definition: "+s); //$NON-NLS-1$

			return result;
		}

		public static VectorType withUndefinedSize(ValueType type) {
			requireNonNull(type);
			checkComponentType(type);

			return new VectorType(type, toXmlForm(type, UNDEFINED_SIZE), UNDEFINED_SIZE);
		}

		public static VectorType withSize(ValueType type, int size) {
			requireNonNull(type);
			checkComponentType(type);

			if(size<1)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Size has to be greater than 0: "+size); //$NON-NLS-1$

			return new VectorType(type, toXmlForm(type, size), size);
		}

		static final char SIZE_OPEN = '[';
		static final char SIZE_CLOSE = ']';
		static final char ELEMENT_SEPARATOR = '|';
		static final char ESCAPE_CHARACTER = '\\';
		static final char WILDCARD_SIZE_CHARACTER = 'x';
		static final String WILDCARD_SIZE_STRING = "x";

		private static final int UNDEFINED_SIZE = -1;

		private final int size;
		private final ValueType componentType;

		private final transient Object emptyArray;

		private static String toXmlForm(ValueType componentType, int size) {
			if(size==UNDEFINED_SIZE) {
				return componentType.getStringValue()+SIZE_OPEN+WILDCARD_SIZE_CHARACTER+SIZE_CLOSE;
			} else {
				return componentType.getStringValue()+SIZE_OPEN+size+SIZE_CLOSE;
			}
		}

		private VectorType(ValueType componentType, String xmlForm, int size) {
			super(xmlForm, Object.class, false, false, true);

			this.size = size;
			this.componentType = componentType;

			emptyArray = createArray(componentType, 0);
		}

		public int getSize() {
			return size;
		}

		public boolean isUndefinedSize() {
			return size==UNDEFINED_SIZE;
		}

		@Override
		public CharSequence toChars(Object value) throws ValueConversionException {
			StringBuilder sb = new StringBuilder();

			int length = Array.getLength(value);
			for(int i=0; i<length; i++) {
				if(i>0) {
					sb.append(ELEMENT_SEPARATOR);
				}

				Object element = Array.get(value, i);
				CharSequence s = componentType.toChars(element);

				for(int idx=0; idx <s.length(); idx++) {
					char c = s.charAt(idx);
					if(c==ELEMENT_SEPARATOR || c==ESCAPE_CHARACTER) {
						sb.append(ESCAPE_CHARACTER);
					}
					sb.append(ELEMENT_SEPARATOR);
				}
			}

			return sb.toString();
		}

		private static int countElements(CharSequence s) {
			if(s.length()==0) {
				return 0;
			}

			int count = 1;
			boolean escaped = false;

			for(int i=0; i<s.length(); i++) {
				char c = s.charAt(i);

				if(escaped) {
					escaped = false;
				} else {
					switch (c) {
					case ESCAPE_CHARACTER:
						escaped = true;
						break;

					case ELEMENT_SEPARATOR:
						count++;
						break;

					default:
						break;
					}
				}
			}

			return count;
		}

		private static Object createArray(ValueType componentType, int size) {
			return Array.newInstance(Primitives.unwrap(componentType.getBaseClass()), size);
		}

		private static Object parseStaticSized(CharSequence s, ClassLoader classLoader, ValueType componentType, int size)
				throws ValueConversionException {

			// Create array with unwrapped types
			Object array = createArray(componentType, size);

			// Traverse input string and load elements
			boolean escaped = false;
			StringBuilder buffer = new StringBuilder();
			int elementIndex = 0;
			for(int i=0; i<s.length(); i++) {
				char c = s.charAt(i);

				if(escaped) {
					escaped = false;
					buffer.append(c);
				} else {
					switch (c) {
					case ESCAPE_CHARACTER:
						escaped = true;
						break;

					case ELEMENT_SEPARATOR:
						// Not the fastest way to implement it, but should be sufficient

						Object element = componentType.parse(buffer, classLoader);
						buffer.setLength(0);
						Array.set(array, elementIndex, element);
						elementIndex++;
						break;

					default:
						buffer.append(c);
						break;
					}
				}

			}

			// Handle last element
			if(buffer.length()>0) {
				Object element = componentType.parse(buffer, classLoader);
				Array.set(array, elementIndex, element);
				elementIndex++;
			}

			if(elementIndex!=size)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						Messages.mismatch("Insufficient elements declared in input string", _int(size), _int(elementIndex)));

			return array;
		}

		/**
		 * @throws ValueConversionException
		 * @see de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.String, java.lang.ClassLoader)
		 */
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) throws ValueConversionException {

			int size = this.size;

			// 2-path strategy for unknown size: count elements first so we can use a single parsing method
			if(size==UNDEFINED_SIZE) {
				size = countElements(s);
			}

			if(size==0) {
				return emptyArray;
			}

			return parseStaticSized(s, classLoader, componentType, size);
		}

		/**
		 * Returns {@code true} iff the given {@code value} is an array with the correct
		 * length and a compatible component type.
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#isValidValue(java.lang.Object)
		 * @see #isValidType(Class)
		 */
		@Override
		public boolean isValidValue(Object value) {
			return super.isValidValue(value) && (isUndefinedSize() || Array.getLength(value)==size);
		}

		/**
		 * Returns {@code true} iff the given {@code type} is an array type
		 * and its component type is compatible with this vector's declared component
		 * type.
		 * <p>
		 * Note that primitive arrays will get their component type wrapped accordingly!
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#isValidType(java.lang.Class)
		 */
		@Override
		public boolean isValidType(Class<?> type) {
			return type.isArray() && componentType.isValidType(Primitives.wrap(type.getComponentType()));
		}

		@Override
		public Class<?> checkValue(Object value) {
			Class<?> type = extractClass(value);

			if(!type.isArray())
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Incompatible value type "+type.getName()+" for value-type "+getStringValue()+" - expected an array type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if(!isUndefinedSize() && Array.getLength(value)!=size)
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Mismatching component count "+Array.getLength(value)+" for value-type "+getStringValue()+" - expected "+size); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			Class<?> componentClass = type.getComponentType();

			// Again we need wrapping of primitive types
			componentClass = Primitives.wrap(componentClass);

			if(!componentType.isValidType(componentClass))
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Incompatible array component type "+componentClass.getName()+" for vector-type "+getStringValue()+" - expected "+componentType.getBaseClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			return type;
		}
	}

	/**
	 * Implements a matrix storage for arbitrary component types that uses an array to actually store the
	 * matrix data in.
	 * <p>
	 * Rows of the matrix will be concatenated into the buffer array.
	 * <p>
	 * The serialized form of this value type adheres to the following format:<br>
	 * {@code <component_type>[<rows>,<cols>]}
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class MatrixType extends ValueType {

		static boolean isMatrixType(String s) {
			int last = s.length()-1;
			int open = s.indexOf(VectorType.SIZE_OPEN);
			int sep = s.indexOf(SIZE_SEPARATOR, open);
			return open>0 && open<sep && sep<last
					&& s.charAt(last)==VectorType.SIZE_CLOSE;
		}

		public static MatrixType parseMatrixType(String s) {
			MatrixType result;

			int openIdx = s.indexOf(VectorType.SIZE_OPEN);
			if(openIdx!=-1) {
				if(s.charAt(s.length()-1)!=VectorType.SIZE_CLOSE)
					throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_TYPE,
							"Invalid vector type definition: "+s);

				int sepIdx = s.indexOf(SIZE_SEPARATOR, openIdx);

				String typeName = s.substring(0, openIdx);

				ValueType componentType = parseValueType(typeName);

				String rowSizeString = s.substring(openIdx+1, sepIdx);
				String colSizeString = s.substring(sepIdx+1, s.length()-1);

				int rowSize = Integer.parseInt(rowSizeString);
				int colSize = Integer.parseInt(colSizeString);

				result = withSize(componentType, rowSize, colSize);
			} else
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_TYPE,
						"Not a valid matrix value type definition: "+s); //$NON-NLS-1$

			return result;
		}

		static final char ROW_SEPARATOR = ';';
		static final char SIZE_SEPARATOR = ',';

		private final int rows, columns;
		private final ValueType componentType;

		private final transient Object emptyArray;

		public static MatrixType withSize(ValueType type, int rows, int columns) {
			requireNonNull(type);
			checkComponentType(type);

			if(rows<0)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Row count must not be negative: "+rows); //$NON-NLS-1$

			if(columns<0)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Column count must not be negative: "+columns); //$NON-NLS-1$

			return new MatrixType(type, toXmlForm(type, rows, columns), rows, columns);
		}

		private static String toXmlForm(ValueType componentType, int rows, int columns) {
			return componentType.getStringValue()+VectorType.SIZE_OPEN+rows+SIZE_SEPARATOR+columns+VectorType.SIZE_CLOSE;
		}

		private MatrixType(ValueType componentType, String xmlForm, int rows, int columns) {
			super(xmlForm, Object.class, false, false, true);

			this.rows = rows;
			this.columns = columns;
			this.componentType = componentType;

			emptyArray = createArray(componentType, 0);
		}

		public int getRows() {
			return rows;
		}

		public int getColumns() {
			return columns;
		}

		private int toIndex(int row, int col) {
			return row*columns + col;
		}

		public Object getCell(Object matrix, int row, int col) {
			return Array.get(matrix, toIndex(row, col));
		}

		public void setCell(Object matrix, Object value, int row, int col) {
			Array.set(matrix, toIndex(row, col), value);
		}

		@Override
		public CharSequence toChars(Object value) throws ValueConversionException {
			StringBuilder sb = new StringBuilder();

			int size = rows*columns;

			if(size!=Array.getLength(value))
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						Messages.sizeMismatch("Invalid size of matrix buffer array", size, Array.getLength(value)));

			for(int row=0; row<rows; row++) {
				for(int col=0; col<columns; col++) {

					Object element = getCell(value, row, col);
					CharSequence s = componentType.toChars(element);

					for(int idx=0; idx <s.length(); idx++) {
						char c = s.charAt(idx);
						if(c==VectorType.ELEMENT_SEPARATOR
								|| c==VectorType.ESCAPE_CHARACTER
								|| c==ROW_SEPARATOR) {
							sb.append(VectorType.ESCAPE_CHARACTER);
						}
						sb.append(VectorType.ELEMENT_SEPARATOR);
					}
				}

				sb.append(ROW_SEPARATOR);
			}

			return sb.toString();
		}

		private static Object createArray(ValueType componentType, int size) {
			return Array.newInstance(Primitives.unwrap(componentType.getBaseClass()), size);
		}

		/**
		 * @throws ValueConversionException
		 * @see de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.String, java.lang.ClassLoader)
		 */
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) throws ValueConversionException {

			int size = rows*columns;

			if(size==0) {
				return emptyArray;
			}


			// Create array with unwrapped types
			Object array = createArray(componentType, size);

			// Traverse input string and load elements
			boolean escaped = false;
			StringBuilder buffer = new StringBuilder();
			int row = 0;
			int col = 0;
			boolean rowBreak = false;
			for(int i=0; i<s.length(); i++) {
				char c = s.charAt(i);

				if(escaped) {
					escaped = false;
					buffer.append(c);
				} else {
					switch (c) {
					case VectorType.ESCAPE_CHARACTER:
						escaped = true;
						break;

					case ROW_SEPARATOR:
						rowBreak = true;
						break; //FIXME we had a fall-through here, was that intended?

					case VectorType.ELEMENT_SEPARATOR:
						Object element = componentType.parse(buffer, classLoader);
						buffer.setLength(0);
						setCell(array, element, row, col);
						col++;
						if(rowBreak) {
							//FIXME do some sanity check here regarding number of elements in row?
							row++;
							col = 0;
							rowBreak = false;
						}
						break;

					default:
						buffer.append(c);
						break;
					}
				}

			}

			// No need to handle last element, since ROW_SEPARATOR will appear as sentinel character there

			return array;
		}

		/**
		 * Returns {@code true} iff the given {@code value} is an array with the correct
		 * length and a compatible component type.
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#isValidValue(java.lang.Object)
		 * @see #isValidType(Class)
		 */
		@Override
		public boolean isValidValue(Object value) {
			return super.isValidValue(value) && Array.getLength(value)==rows*columns;
		}

		/**
		 * Returns {@code true} iff the given {@code type} is an array type
		 * and its component type is compatible with this vector's declared component
		 * type.
		 * <p>
		 * Note that primitive arrays will get their component type wrapped accordingly!
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#isValidType(java.lang.Class)
		 */
		@Override
		public boolean isValidType(Class<?> type) {
			return type.isArray() && componentType.isValidType(Primitives.wrap(type.getComponentType()));
		}

		@Override
		public Class<?> checkValue(Object value) {
			Class<?> type = extractClass(value);

			if(!type.isArray())
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Incompatible value type "+type.getName()+" for value-type "+getStringValue()+" - expected an array type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			int size = rows*columns;
			if(Array.getLength(value)!=size)
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Mismatching component count "+Array.getLength(value)+" for value-type "+getStringValue()+" - expected "+size); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			Class<?> componentClass = type.getComponentType();

			// Again we need wrapping of primitive types
			componentClass = Primitives.wrap(componentClass);

			if(!componentType.isValidType(componentClass))
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Incompatible array component type "+componentClass.getName()+" for vector-type "+getStringValue()+" - expected "+componentType.getBaseClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			return type;
		}
	}
}
