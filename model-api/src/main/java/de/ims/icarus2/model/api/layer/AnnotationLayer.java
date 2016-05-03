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
 *
 * $Revision: 382 $
 * $Date: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/layer/AnnotationLayer.java $
 *
 * $LastChangedDate: 2015-04-09 16:23:50 +0200 (Do, 09 Apr 2015) $
 * $LastChangedRevision: 382 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.layer;

import java.util.function.Consumer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus2.model.api.manifest.ManifestOwner;
import de.ims.icarus2.model.api.manifest.ValueSet;
import de.ims.icarus2.model.api.members.Annotation;
import de.ims.icarus2.model.api.members.item.Item;

/**
 *
 * @author Markus Gärtner
 * @version $Id: AnnotationLayer.java 382 2015-04-09 14:23:50Z mcgaerty $
 *
 */
public interface AnnotationLayer extends Layer, ManifestOwner<AnnotationLayerManifest> {

	/**
	 * Returns the shared {@code AnnotationLayerManifest} that holds
	 * information about keys and possible values in this annotation layer.
	 *
	 * @return The manifest that describes this annotation layer
	 */
	@Override
	AnnotationLayerManifest getManifest();

	/**
	 * Returns the background storage that holds the actual annotation data in this
	 * layer and is directly managed by the driver that created this layer and its
	 * context.
	 *
	 * @return
	 */
	AnnotationStorage getAnnotationStorage();

	/**
	 * Defines the storage to be used for this layer.
	 *
	 * @param storage
	 *
	 * @throws NullPointerException iff the {@code storage} argument is {@code null}
	 */
	//TODO check whether to throw exception when storage is already set or allow client code modifications?
	void setAnnotationStorage(AnnotationStorage storage);

	/**
	 * @author Markus Gärtner
	 * @version $Id: AnnotationLayer.java 382 2015-04-09 14:23:50Z mcgaerty $
	 *
	 */
	public interface AnnotationStorage {

		/**
		 * Collects all the keys in this layer which are mapped to valid annotation values for
		 * the given item. This method returns {@code true} iff at least one key was added
		 * to the supplied {@code action}.
		 *
		 * @param item
		 * @param action
		 * @return
		 * @throws NullPointerException if any one of the two arguments is {@code null}
		 * @throws UnsupportedOperationException if this layer does not support additional keys
		 */
		boolean collectKeys(Item item, Consumer<String> action);

		/**
		 * Returns the annotation for a given item and key or {@code null} if that item
		 * has not been assigned an annotation value for the specified key in this layer.
		 * Note that the returned object can be either an actual value or an {@link Annotation}
		 * instance that wraps a value and provides further information.
		 *
		 * @param item
		 * @param key
		 * @return
		 * @throws NullPointerException if either the {@code item} or {@code key}
		 * is {@code null}
		 * @throws UnsupportedOperationException if this layer does not support additional keys
		 */
		Object getValue(Item item, String key);

		int getIntegerValue(Item item, String key);
		float getFloatValue(Item item, String key);
		double getDoubleValue(Item item, String key);
		long getLongValue(Item item, String key);
		boolean getBooleanValue(Item item, String key);

		/**
		 * Deletes all annotations in this layer
		 * <p>
		 * Note that this does include all annotations for all keys,
		 * not only those declared for the default annotation.
		 *
		 * @throws UnsupportedOperationException if the corpus
		 * is not editable
		 */
		void removeAllValues();

		/**
		 * Deletes in this layer all annotations for
		 * the given {@code key}.
		 *
		 * @param key The key for which annotations should be
		 * deleted
		 * @throws UnsupportedOperationException if this layer does not allow multiple keys
		 * @throws UnsupportedOperationException if the corpus
		 * is not editable
		 */
		void removeAllValues(String key);

//		/**
//		 * Removes from this layer all annotations for the given
//		 * item.
//		 * <p>
//		 * If the {@code recursive} parameter is {@code true} and the supplied
//		 * {@code item} is a {@link Container} or {@link Structure} then all
//		 * annotations defined for members of it should be removed as well.
//		 *
//		 * @param item the {@code Item} for which annotations should be removed
//		 * @param recursive if {@code true} removes all annotations defined for
//		 * elements ({@code Item}s and {@code Edge}s alike) in the supplied
//		 * {@code Item}
//		 * @throws NullPointerException if the {@code item} argument is {@code null}
//		 * @throws UnsupportedOperationException if the corpus
//		 * is not editable
//		 */
//		void removeAllValues(Item item, boolean recursive);


		/**
		 * Assigns the given {@code value} as new annotation for the specified
		 * {@code Item} and {@code key}, replacing any previously defined value.
		 * If the {@code value} argument is {@code null} any stored annotation
		 * for the combination of {@code item} and {@code key} will be deleted.
		 * <p>
		 * This is an optional method
		 *
		 * @param item The {@code Item} to change the annotation value for
		 * @param key the key for which the annotation should be changed
		 * @param value the new annotation value or {@code null} if the annotation
		 * for the given {@code item} and {@code key} should be deleted
		 *
		 * @throws NullPointerException if the {@code item} or {@code key}
		 * argument is {@code null}
		 * @throws ModelException if the supplied {@code value} is not
		 * contained in the {@link ValueSet} of this layer's manifest for the given {@code key}.
		 * This is only checked if the manifest actually defines such restrictions.
		 * @throws UnsupportedOperationException if the corpus is not editable
		 */
		void setValue(Item item, String key, Object value);
		void setIntegerValue(Item item, String key, int value);
		void setLongValue(Item item, String key, long value);
		void setFloatValue(Item item, String key, float value);
		void setDoubleValue(Item item, String key, double value);
		void setBooleanValue(Item item, String key, boolean value);

		/**
		 *
		 * @return {@code true} iff this layer holds at least one valid annotation object.
		 */
		boolean hasAnnotations();
		/**
		 *
		 * @return {@code true} iff this layer holds at least one valid annotation object
		 * for the given {@code Item}.
		 */
		boolean hasAnnotations(Item item);
	}
}
