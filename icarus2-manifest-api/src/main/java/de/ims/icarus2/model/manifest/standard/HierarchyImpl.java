/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.util.strings.StringUtil.getName;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;

/**
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class HierarchyImpl<E extends Object> extends AbstractLockable implements Hierarchy<E> {

	private final ReferenceList<E> items = new ReferenceArrayList<>();

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#getRoot()
	 */
	@Override
	public E getRoot() {
		return items.isEmpty() ? null : items.get(ROOT);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#getDepth()
	 */
	@Override
	public int getDepth() {
		return items.size();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#atLevel(int)
	 */
	@Override
	public E atLevel(int level) {
		return items.get(level);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#add(java.lang.Object)
	 */
	@Override
	public Hierarchy<E> add(E item) {
		checkNotLocked();

		insert0(item, -1, true);

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#remove(java.lang.Object)
	 */
	@Override
	public Hierarchy<E> remove(E item) {
		checkNotLocked();

		remove0(item);

		return this;
	}

	protected void remove0(E item) {
		requireNonNull(item);

		if(!items.remove(item))
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"Item not present: "+getName(item));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#insert(java.lang.Object, int)
	 */
	@Override
	public Hierarchy<E> insert(E item, int index) {
		checkNotLocked();

		insert0(item, index, false);

		return this;
	}

	protected void insert0(E item, int index, boolean adjustIndex) {
		requireNonNull(item);

		if(items.contains(item))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Item already present: "+getName(item));

		if(index==-1 && adjustIndex) {
			index = items.size();
		}

		items.add(index, item);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#levelOf(java.lang.Object)
	 */
	@Override
	public int levelOf(E item) {
		requireNonNull(item);
		return items.indexOf(item);
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return items.iterator();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Hierarchy@"+items.toString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return items.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof Hierarchy) {
			Hierarchy<?> other = (Hierarchy<?>) obj;
			return CollectionUtils.<Object>equals(items, other.getItems());
		}

		return false;
	}
}
