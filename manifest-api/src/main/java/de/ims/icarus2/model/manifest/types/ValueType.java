/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 454 $
 * $Date: 2016-02-10 12:44:15 +0100 (Mi, 10 Feb 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/types/ValueType.java $
 *
 * $LastChangedDate: 2016-02-10 12:44:15 +0100 (Mi, 10 Feb 2016) $
 * $LastChangedRevision: 454 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.types;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.Icon;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.eval.Expression;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.nio.ArrayByteStream;
import de.ims.icarus2.model.nio.ByteChannelCharacterSequence;
import de.ims.icarus2.model.util.IconWrapper;
import de.ims.icarus2.model.util.StringResource;
import de.ims.icarus2.util.classes.ClassUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 * @version $Id: ValueType.java 454 2016-02-10 11:44:15Z mcgaerty $
 *
 */
public abstract class ValueType implements StringResource {

	private final Class<?> baseClass;
	private final String xmlForm;
	private final boolean simple;
	private final boolean basic;

	private static Map<String, ValueType> xmlLookup = new HashMap<>();

	//TODO implement matrix type?
	public static ValueType parseValueType(String s) {
		ValueType result = xmlLookup.get(s);

		if(result==null) {
			int sepIdx = s.indexOf(VectorType.SIZE_SEPARATOR);
			if(sepIdx!=-1) {
				String typeName = s.substring(0, sepIdx);

				ValueType componentType = parseValueType(typeName);

				String sizeString = s.substring(sepIdx+1, s.length()-1);

				if(VectorType.WILDCARD_SIZE_STRING.equals(sizeString)) {
					result = new VectorType(componentType);
				} else {
					int size = Integer.parseInt(sizeString);

					result = new VectorType(componentType, size);
				}
			} else
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_TYPE,
						"Not a known value type definition: "+s); //$NON-NLS-1$
		}

		return result;
	}

	protected ValueType(String xmlForm, Class<?> baseClass, boolean simple) {
		this(xmlForm, baseClass, simple, false);
	}

	private ValueType(String xmlForm, Class<?> baseClass, boolean simple, boolean basic) {
		this.baseClass = baseClass;
		this.xmlForm = xmlForm;
		this.simple = simple;
		this.basic = basic;

		xmlLookup.put(xmlForm, this);
	}

	public CharSequence toChars(Object value) {
		return String.valueOf(value);
	}

	public abstract Object parse(String s, ClassLoader classLoader);

	/**
	 * Returns whether or not the type has a simple form that makes it
	 * easily serializable.
	 *
	 * @return
	 */
	public final boolean isSimpleType() {
		return simple;
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

	public final Class<?> getBaseClass() {
		return baseClass;
	}

	public static final ValueType UNKNOWN = new ValueType("unknown", Object.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}
	};

	// External
	public static final ValueType CUSTOM = new ValueType("custom", Object.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}
	};

	/**
	 * To reduce dependency we only store the extension's unique id, not the extension itself!
	 */
	public static final ValueType EXTENSION = new ValueType("extension", String.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
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

	public static final ValueType ENUM = new ValueType("enum", Enum.class, false, true) { //$NON-NLS-1$
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			String[] parts = s.split("@"); //$NON-NLS-1$
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

	// "Primitive"
	public static final ValueType STRING = new ValueType("string", CharSequence.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return s;
		}
	};

	public static final ValueType BOOLEAN = new ValueType("boolean", Boolean.class, true, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Boolean.parseBoolean(s);
		}
	};

	public static final ValueType INTEGER = new ValueType("integer", Integer.class, true, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Integer.parseInt(s);
		}
	};

	public static final ValueType LONG = new ValueType("long", Long.class, true, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Long.parseLong(s);
		}
	};

	public static final ValueType DOUBLE = new ValueType("double", Double.class, true, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Double.parseDouble(s);
		}
	};

	public static final ValueType FLOAT = new ValueType("float", Float.class, true, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Float.parseFloat(s);
		}
	};

	// Resource identifiers
	public static final ValueType URI = new ValueType("uri", URI.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			try {
				return new URI(s);
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
	public static final ValueType URL = new ValueType("url", Url.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			try {
				return new Url(s);
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
	public static final ValueType FILE = new ValueType("file", Path.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Paths.get(s);
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

	public static final ValueType URL_RESOURCE = new ValueType("url-resource", UrlResource.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new UnsupportedOperationException("Cannot parse data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			throw new UnsupportedOperationException("Cannot serialize data of type 'url-resource'"); //$NON-NLS-1$
		}
	};

	public static final ValueType LINK = new ValueType("link", Link.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new UnsupportedOperationException("Cannot parse data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}

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
	public static final ValueType IMAGE = new ValueType("image", Icon.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return new IconWrapper(s);
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			if(value instanceof IconWrapper) {
				return ((IconWrapper)value).getStringValue();
			} else
				throw new IllegalArgumentException("Cannot serialize icon: "+value); //$NON-NLS-1$
		}
	};

	public static final ValueType IMAGE_RESOURCE = new ValueType("image-resource", IconLink.class, false, true) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new UnsupportedOperationException("Cannot parse data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}

		/**
		 *
		 * @see de.ims.icarus2.model.manifest.types.ValueType#toChars(java.lang.Object)
		 */
		@Override
		public CharSequence toChars(Object value) {
			throw new UnsupportedOperationException("Cannot serialize data of type '"+getStringValue()+"'"); //$NON-NLS-1$
		}
	};

	public static final ValueType BINARY_STREAM = new ValueType("binary", SeekableByteChannel.class, false, true) {

		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return ArrayByteStream.fromChars(s);
		}

		@Override
		public CharSequence toChars(Object value) {
			SeekableByteChannel channel = (SeekableByteChannel) value;
			return new ByteChannelCharacterSequence(channel);
		}
	};

	protected static Class<?> extractClass(Object value) {
		Class<?> type = value.getClass();
		if(Expression.class.isAssignableFrom(type)) {
			type = ((Expression)value).getReturnType();

			// We need the (possible) wrapper type since expressions are allowed to
			// declare primitive return types and we deal with wrappers here!
			type = ClassUtils.wrap(type);
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
	public static Collection<ValueType> values() {
		return CollectionUtils.getCollectionProxy(xmlLookup.values());
	}

	public static Set<ValueType> basicValues() {
		return filterIncluding(ValueType::isBasicType);
	}

	/**
	 * Creates a set view that contains all available value types except the
	 * ones specified in the {@code exclusions} varargs parameter.
	 * @param exclusions
	 * @return
	 */
	public static Set<ValueType> filterWithout(ValueType...exclusions) {
		Set<ValueType> filter = new HashSet<>(xmlLookup.values());

		if(exclusions!=null) {
			for(ValueType type : exclusions) {
				filter.remove(type);
			}
		}

		return filter;
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
			for(ValueType type : values()) {
				if(p.test(type)) {
					filter.add(type);
				}
			}
		}

		return filter.getAsSet();
	}

	public Class<?> checkValue(Object value) {
		Class<?> type = extractClass(value);

		if(!isValidType(type))
			throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
					"Incompatible value type "+type.getName()+" for value-type "+xmlForm+" - expected "+baseClass.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return type;
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

	public static final class VectorType extends ValueType {

		public static final char SIZE_SEPARATOR = '[';
		public static final char ELEMENT_SEPARATOR = '|';
		public static final char ESCAPE_CHARACTER = '\\';
		public static final char WILDCARD_SIZE_CHARACTER = 'x';
		public static final String WILDCARD_SIZE_STRING = "x";

		private static final int UNDEFINED_SIZE = -1;

		private final int size;
		private final ValueType componentType;

		private final transient Object emptyArray;

		public VectorType(ValueType componentType, int size) {
			super(componentType.getStringValue()+SIZE_SEPARATOR+size+SIZE_SEPARATOR, Object.class, false, true);

			if(size<1)
				throw new IllegalArgumentException("Size has to be greater than 0: "+size); //$NON-NLS-1$

			this.size = size;
			this.componentType = componentType;

			emptyArray = createArray(componentType, 0);
		}

		public VectorType(ValueType componentType) {
			super(componentType.getStringValue()+SIZE_SEPARATOR+WILDCARD_SIZE_CHARACTER+SIZE_SEPARATOR, Object.class, false, true);

			this.size = UNDEFINED_SIZE;
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
		public CharSequence toChars(Object value) {
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

		private static int countElements(String s) {
			if(s.isEmpty()) {
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
			return Array.newInstance(ClassUtils.unwrap(componentType.getBaseClass()), size);
		}

		private static Object parseStaticSized(String s, ClassLoader classLoader, ValueType componentType, int size) {

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
						Object element = componentType.parse(buffer.toString(), classLoader);
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
				Object element = componentType.parse(buffer.toString(), classLoader);
				Array.set(array, elementIndex, element);
				elementIndex++;
			}

			if(elementIndex!=size)
				throw new ManifestException(GlobalErrorCode.DATA_ARRAY_SIZE,
						//TODO switch to Messages utility class for creating the error message!!!
						"Insufficient elements declared in input string: "+elementIndex+" - expected "+size); //$NON-NLS-1$ //$NON-NLS-2$

			return array;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.String, java.lang.ClassLoader)
		 */
		@Override
		public Object parse(String s, ClassLoader classLoader) {

			int size = this.size;

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
			return type.isArray() && componentType.isValidType(ClassUtils.wrap(type.getComponentType()));
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
			componentClass = ClassUtils.wrap(componentClass);

			if(!componentType.isValidType(componentClass))
				throw new ManifestException(ManifestErrorCode.MANIFEST_TYPE_CAST,
						"Incompatible array component type "+componentClass.getName()+" for vector-type "+getStringValue()+" - expected "+componentType.getBaseClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			return type;
		}
	}
}
