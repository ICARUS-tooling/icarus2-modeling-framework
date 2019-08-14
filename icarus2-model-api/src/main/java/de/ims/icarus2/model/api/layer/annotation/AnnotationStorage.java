/**
 *
 */
package de.ims.icarus2.model.api.layer.annotation;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ValueSet;

/**
 * @author Markus Gärtner
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
	 */
	Object getValue(Item item, String key);

	default String getString(Item item, String key) {
		return (String) getValue(item, key);
	}

	int getInteger(Item item, String key);
	float getFloat(Item item, String key);
	double getDouble(Item item, String key);
	long getLong(Item item, String key);
	boolean getBoolean(Item item, String key);

	/**
	 * Removes from this annotation storage all annotations for
	 * items returned by the given source.
	 * <p>
	 * If the {@code source} returns a {@code null} value, the operation
	 * will stop.
	 *
	 *
	 * @param source the "stream" of items to remove annotation values for
	 * @throws ModelException with {@link ModelErrorCode#MODEL_CORRUPTED_STATE} in case one of the
	 * given {@link Item items} is missing from this storage.
	 */
	void removeAllValues(Supplier<? extends Item> source);

	/**
	 * Removes from this annotation storage all annotations for
	 * items returned by the given source.
	 *
	 * @param source
	 *
	 * @see #removeAllValues(Supplier)
	 */
	default void removeAllValues(Iterator<? extends Item> source) {
		removeAllValues(() -> {return source.hasNext() ? source.next() : null;});
	}


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

	default void setString(Item item, String key, String value) {
		setValue(item, key, value);
	}

	void setInteger(Item item, String key, int value);
	void setLong(Item item, String key, long value);
	void setFloat(Item item, String key, float value);
	void setDouble(Item item, String key, double value);
	void setBoolean(Item item, String key, boolean value);

	/**
	 * Tells whether or not there are any annotations available for this storage.
	 * Note that the scope of the returned value is limited to the current part of
	 * the surrounding {@link AnnotationLayer} that has already been loaded into
	 * memory.
	 * <p>
	 * The returned value is also only to be taken as an indicator! Depending on the
	 * storage implementation it could be rather expensive to do a deep analysis of
	 * the content or backing data structures.
	 *
	 * @return {@code true} iff this layer holds at least one valid annotation object.
	 */
	boolean hasAnnotations();
	/**
	 *
	 * @return {@code true} iff this layer holds at least one valid annotation object
	 * for the given {@code Item}.
	 * Note that this method does not differentiate between actual annotation values
	 * or marker values (cf. {@link AnnotationManifest#getNoEntryValue()}) signaling
	 * items that effectively have "no" annotation.
	 * <p>
	 * So effectively this method is there to check if the given {@code item} is at
	 * least "known" to the storage.
	 */
	boolean hasAnnotations(Item item);
}