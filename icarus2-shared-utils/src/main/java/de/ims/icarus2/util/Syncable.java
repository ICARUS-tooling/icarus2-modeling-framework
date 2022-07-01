/**
 *
 */
package de.ims.icarus2.util;

public interface Syncable<T> {

	void syncTo(T target);

	void syncFrom(T source);
}