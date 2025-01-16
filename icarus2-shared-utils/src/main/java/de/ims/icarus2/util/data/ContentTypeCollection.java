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
