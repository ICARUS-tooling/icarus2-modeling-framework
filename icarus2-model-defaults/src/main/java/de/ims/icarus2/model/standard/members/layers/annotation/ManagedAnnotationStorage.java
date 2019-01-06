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
package de.ims.icarus2.model.standard.members.layers.annotation;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.annotations.OptionalMethod;

/**
 * Extends the basic {@code AnnotationStorage} by means of managing the
 * items inside. A managed storage should provide constructors that allow
 * client code to define certain parameters like initial capacity or whether
 * to store item references as strong or weak links. The initialization of
 * underlying buffer structures and layer specific settings should be delayed
 * until the call to {@link #addNotify(AnnotationLayer)}. In the same way
 * destruction or release of underlying resources is meant to be performed
 * by the {@link #removeNotify(AnnotationLayer)} method which tells the
 * storage that it is no longer required.
 *
 * @author Markus Gärtner
 *
 */
public interface ManagedAnnotationStorage extends AnnotationStorage {

	void addNotify(AnnotationLayer layer);
	void removeNotify(AnnotationLayer layer);

	/**
	 * Returns {@code true} if the specified item is present in this storage.
	 * This does not mean there are valid annotations stored for it, but rather
	 * that there is space reserved/allocated for it!
	 * <p>
	 * Use the {@link #hasAnnotations(Item)} method to check for the presence
	 * of valid annotations for a particular item.
	 *
	 * @param item
	 * @return
	 */
	boolean containsItem(Item item);

	/**
	 * Registers the given item with this storage and reserves/allocates the
	 * required buffer. Returns {@code true} if the item was unknown to this
	 * storage prior to calling this method.
	 *
	 * @param item
	 * @return
	 */
	@OptionalMethod
	boolean addItem(Item item);

	/**
	 * Removes the given item and all annotations present for it from this storage.
	 * Returns {@code true} if the item was present prior to calling this method and
	 * has been successfully removed.
	 *
	 * @param item
	 * @return
	 */
	@OptionalMethod
	boolean removeItem(Item item);
}
