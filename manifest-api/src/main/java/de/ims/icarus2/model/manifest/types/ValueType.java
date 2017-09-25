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
 *
 */
package de.ims.icarus2.model.manifest.types;

import static de.ims.icarus2.util.classes.Primitives._boolean;
import static de.ims.icarus2.util.classes.Primitives._double;
import static de.ims.icarus2.util.classes.Primitives._float;
import static de.ims.icarus2.util.classes.Primitives._int;
import static de.ims.icarus2.util.classes.Primitives._long;

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
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.IconWrapper;
import de.ims.icarus2.util.classes.Primitives;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.nio.ByteArrayChannel;
import de.ims.icarus2.util.nio.ByteChannelCharacterSequence;
import de.ims.icarus2.util.strings.NamedObject;
import de.ims.icarus2.util.strings.StringPrimitives;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public class ValueType implements StringResource, NamedObject {

	private final Class<?> baseClass;
	private final String xmlForm;
	private final boolean simple;
	private final boolean basic;

	private static Map<String, ValueType> xmlLookup = new HashMap<>();

	//TODO implement matrix type?
	public static ValueType parseValueType(String s) {
		ValueType result = xmlLookup.get(s);

		if(result==null) {
			int sepIdx = s.indexOf(VectorType.SIZE_OPEN);
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

	/**
	 * Transform the given {@link CharSequence} into an intermediate
	 * representation suitable for this value type. The returned value
	 * is <b>not</b> required to be persistent. If client code wishes to
	 * further use the returned object, it should use the {@link #persist(Object)}
	 * method to transform it into a persistent state.
	 *
	 * @param s
	 * @param classLoader
	 * @return
	 */
	public Object parse(CharSequence s, ClassLoader classLoader) {
		throw new IllegalStateException("Cannot parse data of type '"+getStringValue()+"'"); //$NON-NLS-1$
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
	 */
	public Object parseAndPersist(CharSequence s, ClassLoader classLoader) {
		return persist(parse(s, classLoader));
	}

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

	@Override
	public final String getName() {
		return getStringValue();
	}

	public final Class<?> getBaseClass() {
		return baseClass;
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

	public static final String ENUM_TYPE_LABEL = "enum";
	public static final ValueType ENUM = new ValueType(ENUM_TYPE_LABEL, Enum.class, false, true) {
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

	// "Primitive"
	public static final String STRING_TYPE_LABEL = "string";
	public static final ValueType STRING = new ValueType(STRING_TYPE_LABEL, CharSequence.class, false, true) {
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
		};
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
	public static final ValueType URL = new ValueType(URL_TYPE_LABEL, Url.class, false, true) {
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
			} else
				//TODO implement base-64 serialization to embed binary image data
				throw new IllegalArgumentException("Cannot serialize icon: "+value); //$NON-NLS-1$
		}
	};

	public static final String IMAGE_RESOURCE_TYPE_LABEL = "image-resourc";
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

	public static final String BINARY_TYPE_LABEL = "binary";
	public static final ValueType BINARY_STREAM = new ValueType(BINARY_TYPE_LABEL, SeekableByteChannel.class, false, true) {

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

	protected static Class<?> extractClass(Object value) {
		Class<?> type = value.getClass();
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

		public VectorType(ValueType componentType, int size) {
			super(componentType.getStringValue()+SIZE_OPEN+size+SIZE_CLOSE, Object.class, false, true);

			if(size<1)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Size has to be greater than 0: "+size); //$NON-NLS-1$

			this.size = size;
			this.componentType = componentType;

			emptyArray = createArray(componentType, 0);
		}

		public VectorType(ValueType componentType) {
			super(componentType.getStringValue()+SIZE_OPEN+WILDCARD_SIZE_CHARACTER+SIZE_CLOSE, Object.class, false, true);

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

		private static Object parseStaticSized(CharSequence s, ClassLoader classLoader, ValueType componentType, int size) {

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
						Messages.mismatchMessage("Insufficient elements declared in input string", _int(size), _int(elementIndex)));

			return array;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.String, java.lang.ClassLoader)
		 */
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {

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
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static final class MatrixType extends ValueType {

		static final char ROW_SEPARATOR = ';';

		private final int rows, columns;
		private final ValueType componentType;

		private final transient Object emptyArray;

		private static String toXmlForm(ValueType componentType, int rows, int columns) {
			return (componentType==null ? "" : componentType.getStringValue())
					+VectorType.SIZE_OPEN+rows+VectorType.SIZE_CLOSE
					+VectorType.SIZE_OPEN+columns+VectorType.SIZE_CLOSE;
		}

		public MatrixType(ValueType componentType, int rows, int columns) {
			super(toXmlForm(componentType, rows, columns), Object.class, false, true);

			if(rows<0)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Row count must not be negative: "+rows); //$NON-NLS-1$

			if(columns<0)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT, "Column count must not be negative: "+columns); //$NON-NLS-1$

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
		public CharSequence toChars(Object value) {
			StringBuilder sb = new StringBuilder();

			int size = rows*columns;

			if(size!=Array.getLength(value))
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						Messages.sizeMismatchMessage("Invalid size of matrix buffer array", size, Array.getLength(value)));

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
		 * @see de.ims.icarus2.model.manifest.types.ValueType#parse(java.lang.String, java.lang.ClassLoader)
		 */
		@Override
		public Object parse(CharSequence s, ClassLoader classLoader) {

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
