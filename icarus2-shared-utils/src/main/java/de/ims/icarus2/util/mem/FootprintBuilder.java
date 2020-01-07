/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.mem;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.util.Filter;

/**
 * @author Markus Gärtner
 *
 */
public class FootprintBuilder {

	private static final Logger log = LoggerFactory
			.getLogger(FootprintBuilder.class);

	/**
	 * Weak-keys map so we don't unnecessarily pollute the heap space.
	 */
	private static Map<Object, FootprintBuilder> builderMap = new WeakHashMap<>();

	public static FootprintBuilder getSharedBuilder(Object context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		synchronized (builderMap) {
			FootprintBuilder builder = builderMap.get(context);

			if(builder==null) {
				builder = new FootprintBuilder();

				builderMap.put(context, builder);
			}

			return builder;
		}
	}

	private static final MemoryCalculator emptyCalculator = new MemoryCalculator() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return 0;
		}
	};

	private static final MemoryCalculator objectCalculator = new MemoryCalculator() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addObject();
		}
	};

//	private final MemoryCalculator stringCalculator = new GeneralCalculator(String.class) {
//
//
//		@Override
//		public long appendFootprint(Object obj, FootprintBuffer buffer,
//				ObjectCache cache) {
//			long footprint = super.appendFootprint(obj, buffer, cache);
//
//			buffer.addStringFootprint(footprint);
//
//			return footprint;
//		}
//	};

	private Set<Class<?>> whitelist = new HashSet<>();
	private Set<Class<?>> blacklist = new HashSet<>();
	// Act as exclusion classFilters
	private Set<Filter> classFilters = new HashSet<>();
	private Set<Filter> objectFilters = new HashSet<>();

	private boolean isIgnored(Class<?> clazz) {
		if(whitelist.contains(clazz)) {
			return false;
		}

		if(blacklist.contains(clazz)) {
			return true;
		}

		if(!classFilters.isEmpty()) {
			for(Filter filter : classFilters) {
				if(filter.accepts(clazz)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isIgnored(Object obj) {

		if(!objectFilters.isEmpty()) {
			for(Filter filter : objectFilters) {
				if(filter.accepts(obj)) {
					return true;
				}
			}
		}

		return false;
	}

	private Map<Class<?>, MemoryCalculator> calculators = new HashMap<>();

	private MemoryCalculator getCalculator(Object obj) {
		if (obj == null)
			throw new NullPointerException("Invalid obj"); //$NON-NLS-1$

		if(isIgnored(obj)) {
			return emptyCalculator;
		}

		return getCalculator(obj.getClass());
	}

	private MemoryCalculator getCalculator(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		if(clazz.isArray()) {
			return arrayCalculator;
		}

		MemoryCalculator calculator = calculators.get(clazz);

		if(calculator==null) {
			calculator = createCalculator(clazz);

			calculators.put(clazz, calculator);
		}

		return calculator;
	}

	private MemoryCalculator createCalculator(Class<?> clazz) {
		if(isIgnored(clazz) || clazz==Object.class) {
			return emptyCalculator;
		}

		Calculator calc = clazz.getAnnotation(Calculator.class);
		if(calc!=null) {
			try {
				return calc.value().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				log.error("Failed to instantiate memory calculator assigned to class: {}", clazz, e); //$NON-NLS-1$
				return emptyCalculator;
			}
		}


		Assessable assessable = clazz.getAnnotation(Assessable.class);

		if(assessable!=null) {
			return new HeapMemberCalculator(clazz);
		}

		return new GeneralCalculator(clazz);
	}

	public FootprintBuilder() {
//		calculators.put(String.class, stringCalculator);
		calculators.put(Object.class, objectCalculator);
	}

	public void addToWhitelist(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		whitelist.add(clazz);
	}

	public <E extends Object> void addToWhitelist(Collection<Class<E>> classes) {
		if (classes == null)
			throw new NullPointerException("Invalid classes"); //$NON-NLS-1$

		whitelist.addAll(classes);
	}

	public void addToBlacklist(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		blacklist.add(clazz);
	}

	public <E extends Object> void addToBlacklist(Collection<Class<E>> classes) {
		if (classes == null)
			throw new NullPointerException("Invalid classes"); //$NON-NLS-1$

		blacklist.addAll(classes);
	}

	public void addClassFilter(Filter filter) {
		if (filter == null)
			throw new NullPointerException("Invalid filter"); //$NON-NLS-1$

		classFilters.add(filter);
	}

	public synchronized MemoryFootprint calculateFootprint(Object root) {
		if (root == null)
			throw new NullPointerException("Invalid root"); //$NON-NLS-1$

		FootprintBuffer buffer = new FootprintBuffer(root);
		ObjectCache cache = new ObjectCache();

		buffer.start();

		getCalculator(root).appendFootprint(root, buffer, cache);

		buffer.finalizeFootprint();

		return buffer;
	}

	private class GeneralCalculator implements MemoryCalculator {

		private final Class<?> clazz;
		private final MemoryCalculator parent;

		private List<Class<?>> primitiveFields;
		private List<Field> complexFields;

		GeneralCalculator(Class<?> clazz) {
			if (clazz == null)
				throw new NullPointerException("Invalid clazz");  //$NON-NLS-1$

			this.clazz = clazz;

			Class<?> parentClazz = clazz.getSuperclass();
			MemoryCalculator parent = null;
			if(parentClazz!=null) {
				parent = getCalculator(parentClazz);
			}
			this.parent = parent;

			for(Field field : clazz.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				Class<?> type = field.getType();

				if(type.isPrimitive()) {
					if(primitiveFields==null) {
						primitiveFields = new ArrayList<>(5);
					}
					primitiveFields.add(type);
				} else {
					if(complexFields==null) {
						complexFields = new ArrayList<>(5);
					}
					field.setAccessible(true);
					complexFields.add(field);
				}
			}
		}

		/**
		 * @see de.ims.icarus2.util.mem.MemoryCalculator#appendFootprint(java.lang.Object, de.ims.icarus2.util.mem.FootprintBuffer, de.ims.icarus2.util.mem.ObjectCache)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {

			long footprint = 0;

			if(parent!=null) {
				footprint += parent.appendFootprint(obj, buffer, cache);
			}

			if(primitiveFields!=null) {
				for(int i=primitiveFields.size()-1; i>-1; i--) {
					footprint += buffer.addPrimitive(primitiveFields.get(i));
				}
			}

			if(complexFields!=null) {
				for(int i=complexFields.size()-1; i>-1; i--) {
					Field field = complexFields.get(i);
					try {
						Object value = field.get(obj);
						footprint += buffer.addReference();

						// If value is null or already cached only count reference
						if(value!=null && cache.addIfAbsent(value)) {
							long fp = 0;
							if(value.getClass().isArray()) {
								fp = addArray(value, buffer, cache);
							} else {
								fp = getCalculator(value).appendFootprint(value, buffer, cache);
							}
							buffer.addFootprint(value.getClass(), fp);
							footprint += fp;
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new IllegalStateException("Unable to calculate footprint of class: "+clazz, e); //$NON-NLS-1$
					}
				}
			}

			return footprint;
		}
	}

	private class HeapMemberCalculator implements MemoryCalculator {

		private final Class<?> clazz;
		private final MemoryCalculator parent;

		private List<Class<?>> primitiveFields;
		private List<FieldHandler> complexFields;

		HeapMemberCalculator(Class<?> clazz) {
			if (clazz == null)
				throw new NullPointerException("Invalid clazz");  //$NON-NLS-1$
			if(clazz.getAnnotation(Assessable.class)==null)
				throw new IllegalArgumentException("Missing @Assessable annotation on type: "+clazz); //$NON-NLS-1$

			this.clazz = clazz;

			Class<?> parentClazz = clazz.getSuperclass();
			MemoryCalculator parent = null;
			if(parentClazz!=null && parentClazz!=Object.class) {
				parent = getCalculator(parentClazz);
			}
			this.parent = parent;

			for(Field field : clazz.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				Class<?> type = field.getType();

				Primitive primitive = field.getAnnotation(Primitive.class);
				if(primitive!=null) {
					if(!type.isPrimitive())
						throw new IllegalArgumentException("Illegal @Primitive annotation on non-primitive field: "+field); //$NON-NLS-1$

					if(primitiveFields==null) {
						primitiveFields = new ArrayList<>(5);
					}
					primitiveFields.add(type);
					continue;
				}

				Reference reference = field.getAnnotation(Reference.class);
				if(reference!=null) {
					if(type.isPrimitive())
						throw new IllegalArgumentException("Illegal @Reference annotation on primitive field: "+field); //$NON-NLS-1$

					if(complexFields==null) {
						complexFields = new ArrayList<>(5);
					}
					complexFields.add(getDefaultLinkdHandler(reference.value()));
					continue;
				}

				Link link = field.getAnnotation(Link.class);
				if(link!=null) {
					if(type.isPrimitive())
						throw new IllegalArgumentException("Illegal @Link annotation on primitive field: "+field); //$NON-NLS-1$

					if(complexFields==null) {
						complexFields = new ArrayList<>(5);
					}

					field.setAccessible(true);
					complexFields.add(new DefaultLinkHandler(field, link));
					continue;
				}
			}
		}

		/**
		 * @see de.ims.icarus2.util.mem.MemoryCalculator#appendFootprint(java.lang.Object, de.ims.icarus2.util.mem.FootprintBuffer)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer, ObjectCache cache) {
			long footprint = 0;

			if(parent!=null) {
				footprint += parent.appendFootprint(obj, buffer, cache);
			}

			if(primitiveFields!=null) {
				for(int i=primitiveFields.size()-1; i>-1; i--) {
					footprint += buffer.addPrimitive(primitiveFields.get(i));
				}
			}

			if(complexFields!=null) {
				for(int i=complexFields.size()-1; i>-1; i--) {
					try {
						footprint += complexFields.get(i).appendFootprint(obj, buffer, cache);
					} catch (Exception e) {
						throw new IllegalStateException("Unable to calculate footprint of class: "+clazz, e); //$NON-NLS-1$
					}
				}
			}

			return footprint;
		}

	}

	private static FieldHandler getDefaultLinkdHandler(ReferenceType type) {
		switch (type) {
		case DOWNLINK:
			return downlinkHandler;

		case UPLINK:
			return uplinkHandler;

		default:
			return unknownLinkHandler;
		}
	}

	private long addArray(Object array, FootprintBuffer buffer, ObjectCache cache) {
		if (array == null)
			throw new NullPointerException("Invalid array"); //$NON-NLS-1$

		// Fetch base size of array structure
		long footprint = buffer.addArray(array);

		// If array contains 'real' objects traverse and collect their footprint recursively
		// NOTE: the memory used for references in the array is already included in the
		// footprint returned by buffer.addArray()
		if(!array.getClass().getComponentType().isPrimitive()) {
			int size = Array.getLength(array);
			for(int i=0; i<size; i++) {
				Object value = Array.get(array, i);

				if(value==null) {
					continue;
				}

				if(value.getClass().isArray()) {
					footprint += addArray(value, buffer, cache);
				} else if(cache.addIfAbsent(value)) {
					long fp = getCalculator(value).appendFootprint(value, buffer, cache);
					buffer.addFootprint(value.getClass(), fp);

					footprint += fp;
				}
			}
		}



		return footprint;
	}

	private MemoryCalculator arrayCalculator = new MemoryCalculator() {

		/**
		 * @see de.ims.icarus2.util.mem.MemoryCalculator#appendFootprint(java.lang.Object, de.ims.icarus2.util.mem.FootprintBuffer, de.ims.icarus2.util.mem.ObjectCache)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return addArray(obj, buffer, cache);
		}

	};

	private interface FieldHandler {

		long appendFootprint(Object obj, FootprintBuffer buffer, ObjectCache cache) throws Exception;
	}

	private class DefaultLinkHandler implements FieldHandler {

		private final Field field;
		private final Link link;

		DefaultLinkHandler(Field field, Link link) {
			if (field == null)
				throw new NullPointerException("Invalid field"); //$NON-NLS-1$
			if (link == null)
				throw new NullPointerException("Invalid link"); //$NON-NLS-1$

			this.field = field;
			this.link = link;
		}

		/**
		 * @see de.ims.icarus2.util.mem.FootprintBuilder.FieldHandler#appendFootprint(java.lang.Object, de.ims.icarus2.util.mem.FootprintBuffer, de.ims.icarus2.util.mem.ObjectCache)
		 */
		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) throws Exception {
			Object value = field.get(obj);

			// Always calculate at least footprint for reference and
			// tell buffer about link type!
			long footprint = getDefaultLinkdHandler(link.type()).appendFootprint(obj, buffer, cache);

			// Simply count reference in case the value is null or
			// already cached (given that caching is active)
			if(value!=null && (!link.cache() || cache.addIfAbsent(value))) {
				long fp = 0;
				if(value.getClass().isArray()) {
					fp = addArray(value, buffer, cache);
				} else {
					fp = getCalculator(value).appendFootprint(value, buffer, cache);
				}
				buffer.addFootprint(value.getClass(), fp);

				footprint += fp;
			}

			return footprint;
		}

	}

	private static final FieldHandler unknownLinkHandler = new FieldHandler() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addReference();
		}
	};

	private static final FieldHandler uplinkHandler = new FieldHandler() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addUplink();
		}
	};

	private static final FieldHandler downlinkHandler = new FieldHandler() {

		@Override
		public long appendFootprint(Object obj, FootprintBuffer buffer,
				ObjectCache cache) {
			return buffer.addDownlink();
		}
	};
}
