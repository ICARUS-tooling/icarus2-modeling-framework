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
