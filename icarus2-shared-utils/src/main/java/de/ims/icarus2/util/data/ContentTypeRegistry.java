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
 * $Revision: 429 $
 *
 */
package de.ims.icarus2.util.data;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.events.SimpleEventListener;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.Events;
import de.ims.icarus2.util.events.WeakEventSource;
import de.ims.icarus2.util.id.DuplicateIdentifierException;
import de.ims.icarus2.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 *
 */
public final class ContentTypeRegistry {

	private static volatile ContentTypeRegistry instance;

	/**
	 * Maps content type ids to their instances.
	 */
	private final Map<String, ContentType> contentTypes = new LinkedHashMap<>();

	/**
	 * Reverse mapping of classes to their directly defining
	 * content type instances.
	 */
	private final Map<String, ContentType> classMap = new HashMap<>();

	private final Map<ContentType, DataFlavor> dataFlavorMap = new HashMap<>();

	/**
	 * Maps results of {@link #findEnclosingType(Class)} calls to
	 * argument class.
	 */
	private Map<Class<?>, ContentType> classCache;

	private final WeakEventSource eventSource = new WeakEventSource(this);

	private ContentTypeRegistry() {
		// Object content type not supported?
		//addType0(new DefaultContentType(Object.class));

		// Java base types
		addType0(new DefaultContentType(Integer.class));
		addType0(new DefaultContentType(Boolean.class));
		addType0(new DefaultContentType(Float.class));
		addType0(new DefaultContentType(Double.class));
		addType0(new DefaultContentType(Short.class));
		addType0(new DefaultContentType(Long.class));
		addType0(new DefaultContentType(Date.class));
//		addType0(new DefaultContentType(String.class));

		// Utility and Common types
		addType0(new DefaultContentType(Exception.class));
	}

	public static ContentTypeRegistry getInstance() {
		if(instance==null) {
			synchronized (ContentTypeRegistry.class) {
				if(instance==null) {
					instance = new ContentTypeRegistry();
				}
			}
		}

		return instance;
	}

	public ContentType getType(String id) {
		ContentType type = contentTypes.get(id);
		if(type==null)
			throw new UnknownIdentifierException("No such content type: "+id); //$NON-NLS-1$

		return type;
	}

	public ContentType getTypeForClass(Object data) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		Class<?> clazz = data instanceof Class ? (Class<?>)data : data.getClass();
		String className = clazz.getName();

		ContentType type = classMap.get(className);
		if(type==null)
			throw new IllegalArgumentException("No type defined for class: "+className); //$NON-NLS-1$

		return type;
	}

	private ContentType findEnclosingType(Class<?> clazz) {
		// Skip generalization to object
		if(clazz==null || clazz==Object.class) {
			return null;
		}

		ContentType type = classMap.get(clazz.getName());

		// Check cache
		if(type==null && classCache!=null) {
			type = classCache.get(clazz);
			if(type!=null) {
				return type;
			}
		}

		// Check super type
		if(type==null) {
			type = findEnclosingType(clazz.getSuperclass());
		}

		// Traverse interfaces
		if(type==null) {
			for(Class<?> interfaceClazz : clazz.getInterfaces()) {
				type = findEnclosingType(interfaceClazz);
				if(type!=null) {
					break;
				}
			}
		}

		// Cache info
		if(type!=null) {
			if(classCache==null) {
				classCache = new HashMap<>();
			}
			classCache.put(clazz, type);
		}

		return type;
	}

	public ContentType getEnclosingType(Object data) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		Class<?> clazz = data instanceof Class ? (Class<?>)data : data.getClass();

		ContentType type = findEnclosingType(clazz);
		if(type==null)
			throw new IllegalArgumentException("No enclosing type defined for class: "+clazz.getName()); //$NON-NLS-1$

		return type;
	}

	public ContentTypeCollection getEnclosingTypes(Object data) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		ContentTypeCollection collection = new ContentTypeCollection();

		for(ContentType contentType : contentTypes.values()) {
			if(isCompatible(contentType, data)) {
				collection.addType(contentType);
			}
		}

		return collection;
	}

	/**
	 * Returns a collection of {@code ContentType} objects with declared
	 * content classes assignable to the content class of the {@code target}
	 * parameter. The returned collection does not contain the {@code target}
	 * parameter itself!
	 * <p>
	 * Note that this method does not use the {@link ContentType#accepts(Object)}
	 * filter method to determine assignability but relies on {@link Class#isAssignableFrom(Class)}
	 * with the content classes of the two content types being checked.
	 */
	public Collection<ContentType> getAssignableTypes(ContentType target) {
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$

		if(contentTypes.isEmpty()) {
			return Collections.emptyList();
		}

		List<ContentType> compatibleTypes = new ArrayList<>();

		for(ContentType type : contentTypes.values()) {
			if(type==target) {
				continue;
			}
			if(target.getContentClass().isAssignableFrom(type.getContentClass())) {
				compatibleTypes.add(type);
			}
		}

		return compatibleTypes;
	}

	/**
	 * Returns a collection of {@code ContentType}s that are compatible as
	 * per the {@link #isCompatible(ContentType, ContentType)} method.
	 */
	public Collection<ContentType> getCompatibleTypes(ContentType target) {
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$

		if(contentTypes.isEmpty()) {
			return Collections.emptyList();
		}

		List<ContentType> compatibleTypes = new ArrayList<>();

		for(ContentType type : contentTypes.values()) {
			if(type==target) {
				continue;
			}
			if(isCompatible(target, type)) {
				compatibleTypes.add(type);
			}
		}

		return compatibleTypes;
	}

	private static Class<?> getClass(Object obj) {
		if(obj instanceof ContentType) {
			obj = ((ContentType)obj).getContentClass();
		}
		return obj instanceof Class ? (Class<?>)obj : obj.getClass();
	}

	/**
	 * Checks whether the given {@code ContentType} accepts the {@code content}
	 * argument. This is done by using the {@link ContentType#accepts(Object)}
	 * method. If {@code content} is of type {@link Class} it is passed <i>as-is</i>
	 * otherwise the result of its {@code Object#getClass()} method is used as
	 * argument.
	 */
	public static boolean isCompatible(ContentType type, Object content) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		if(content==null)
			throw new NullPointerException("Invalid content"); //$NON-NLS-1$

		return type.accepts(getClass(content));
	}

	/**
	 * Checks whether {@code ContentType} {@code target} is compatible
	 * towards the {@code type} argument. This check is delegated to {@code type}'s
	 * {@link ContentType#accepts(Object)} method with the result of {@code target}'s
	 * {@link ContentType#getContentClass()}.
	 * <p>
	 * Note that the default {@code ContentType} implementations for common
	 * java data types do a pure {@code Object#equals(Object)} check on the
	 * two {@code Class} objects in question. Implementations of type
	 * {@code LazyExtensionContentType} honor the {@link ContentType#STRICT_INHERITANCE}
	 * property and either check for class equality or assignability via
	 * {@link Class#isAssignableFrom(Class)}. Custom implementations are free
	 * to use whatever mechanics they seem fit.
	 */
	public static boolean isCompatible(ContentType type, ContentType target) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$

		return type.accepts(target.getContentClass());
	}

	/**
	 * Fetches the {@code ContentType} identified by {@code typeId} and runs
	 * a call to {@link #isCompatible(ContentType, ContentType)} with the result.
	 */
	public static boolean isCompatible(String typeId, ContentType target) {
		if(typeId==null)
			throw new NullPointerException("Invalid type id"); //$NON-NLS-1$
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$

		ContentType type = getInstance().getType(typeId);
		return isCompatible(type, target);
	}

	public static boolean isCompatible(String typeId, Object content) {
		if(typeId==null)
			throw new NullPointerException("Invalid type id"); //$NON-NLS-1$
		if(content==null)
			throw new NullPointerException("Invalid content"); //$NON-NLS-1$

		ContentType type = getInstance().getType(typeId);
		return isCompatible(type, content);
	}

	public static boolean isCompatible(ContentTypeCollection collection, String typeId) {
		if(collection==null)
			throw new NullPointerException("Invalid collection"); //$NON-NLS-1$
		if(typeId==null)
			throw new NullPointerException("Invalid type id"); //$NON-NLS-1$

		ContentType type = getInstance().getType(typeId);
		return collection.isCompatibleTo(type);
	}

	public static boolean isStrictType(ContentType type) {
		return CollectionUtils.isTrue(type.getProperties(), ContentType.STRICT_INHERITANCE);
	}

	public void addListener(String eventName, SimpleEventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	public void removeListener(SimpleEventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	public void removeListener(SimpleEventListener listener) {
		eventSource.removeListener(listener);
	}

	private void addType0(ContentType type) {
		contentTypes.put(type.getId(), type);
		classMap.put(type.getContentClassName(), type);
	}

	public void addType(ContentType type) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		if(contentTypes.containsKey(type.getId()))
			throw new DuplicateIdentifierException("Content type id already in use: "+type.getId()); //$NON-NLS-1$

		String className = type.getContentClassName();
		if(classMap.containsKey(className))
			throw new IllegalArgumentException("Duplicate content type for class: "+className); //$NON-NLS-1$

		addType0(type);

		eventSource.fireEvent(new EventObject(Events.ADDED, "type", type)); //$NON-NLS-1$
	}

	public void addType(Class<?> clazz) {
		addType(new DefaultContentType(clazz));
	}

	public List<ContentType> availableTypes() {
		return new ArrayList<>(contentTypes.values());
	}

	public int availableTypesCount() {
		return contentTypes.size();
	}

	public DataFlavor getDataFlavor(ContentType contentType) {
		if (contentType == null)
			throw new NullPointerException("Invalid contentType"); //$NON-NLS-1$

		DataFlavor dataFlavor = null;

		synchronized (dataFlavorMap) {
			dataFlavor = dataFlavorMap.get(contentType);
			if(dataFlavor==null) {
				dataFlavor = new ContentTypeDataFlavor(contentType);
				dataFlavorMap.put(contentType, dataFlavor);
			}
		}

		return dataFlavor;
	}

	private static class DefaultContentType implements ContentType {

		private final Class<?> contentClass;

		public DefaultContentType(Class<?> contentClass) {
			this.contentClass = contentClass;
		}

		/**
		 * @see de.ims.icarus2.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return contentClass.getSimpleName()+"ContentType"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus2.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return contentClass.getSimpleName();
		}

		/**
		 * @see de.ims.icarus2.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return contentClass.getName();
		}

		/**
		 * @see de.ims.icarus2.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		/**
		 * @see de.ims.icarus2.util.data.ContentType#getContentClass()
		 */
		@Override
		public Class<?> getContentClass() {
			return contentClass;
		}

		/**
		 * @see de.ims.icarus2.util.data.ContentType#getProperties()
		 */
		@Override
		public Map<String, Object> getProperties() {
			return null;
		}

		/**
		 * @see de.ims.icarus2.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return getContentClass().equals(obj);
		}

		/**
		 * @see de.ims.icarus2.util.data.ContentType#getContentClassName()
		 */
		@Override
		public String getContentClassName() {
			return getContentClass().getName();
		}

	}
}
