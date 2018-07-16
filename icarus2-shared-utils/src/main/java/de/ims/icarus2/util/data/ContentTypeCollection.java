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
 * $Revision: 380 $
 *
 */
package de.ims.icarus2.util.data;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Markus Gärtner
 *
 */
public class ContentTypeCollection {

	private final Set<ContentType> contentTypes = new LinkedHashSet<>();

	public static ContentTypeCollection asCollection(ContentType...contentTypes) {
		ContentTypeCollection result = new ContentTypeCollection();
		for(ContentType contentType : contentTypes) {
			result.addType(contentType);
		}

		return result;
	}

	public static ContentTypeCollection asCollection(Collection<? extends ContentType> contentTypes) {
		return new ContentTypeCollection(contentTypes);
	}

	ContentTypeCollection() {
		// no-op
	}

	ContentTypeCollection(Collection<? extends ContentType> types) {
		contentTypes.addAll(types);
	}

	void addType(ContentType type) {
		contentTypes.add(type);
	}

	void addTypes(Collection<ContentType> types) {
		contentTypes.addAll(types);
	}

	public boolean contains(ContentType type) {
		return contentTypes.contains(type);
	}

	public ContentType[] getContentTypes() {
		return contentTypes.toArray(new ContentType[contentTypes.size()]);
	}

	public int size() {
		return contentTypes.size();
	}

	public boolean isEmpty() {
		return contentTypes.isEmpty();
	}


	/**
	 * Returns {@code true} if and only if this collection contains
	 * a {@code ContentType} that is compatible towards the {@code target}
	 * parameter as defined by {@link ContentTypeRegistry#isCompatible(ContentType, ContentType)}
	 */
	public boolean isCompatibleTo(ContentType target) {
		for(ContentType type : contentTypes) {
			if(ContentTypeRegistry.isCompatible(type, target)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns {@code true} if and only if this collection contains
	 * a {@code ContentType} the {@code target} parameter is considered
	 * to be compatible towards as measured by
	 * {@link ContentTypeRegistry#isCompatible(ContentType, ContentType)}
	 */
	public boolean isCompatibleType(ContentType target) {
		for(ContentType type : contentTypes) {
			if(ContentTypeRegistry.isCompatible(target, type)) {
				return true;
			}
		}
		return false;
	}
}
