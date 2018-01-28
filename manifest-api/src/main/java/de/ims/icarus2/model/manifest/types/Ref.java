/**
 *
 */
package de.ims.icarus2.model.manifest.types;

/**
 * Generic wrapper for "linking" annotation values.
 * Annotations of type {@link ValueType#REF} can be used to
 * {@code point} to other parts of a corpus.
 * <p>
 * Note that the manifest framework only provides a model for
 * the metadata level and therefore does not "know" about the
 * content model parts described in the higher-level frameworks.
 * As a direct result, this interface is generic and the individual
 * layer interfaces will define adequate sub-interfaces for modelling
 * the type of linking they suppport.
 *
 * @author Markus
 *
 */
public interface Ref<O extends Object> {

	/**
	 * Tests whether or not this reference is currently pointing
	 * to a valid target.
	 *
	 * @return
	 */
	boolean hasTarget();

	/**
	 * Returns the target of this reference, possibly loading it
	 * if the resolution process is implemented in a lazy way.
	 * <p>
	 * If the target is no longer available, this method returns
	 * {@code null}.
	 *
	 * @return
	 */
	O getTarget();

	/**
	 * An empty reference that has {@link #hasTarget() no target}.
	 */
	public static final Ref<Object> EMPTY_REF = new Ref<Object>() {

		@Override
		public boolean hasTarget() {
			return false;
		}

		@Override
		public Object getTarget() {
			return null;
		}
	};

	/**
	 * Type-safe wrapper method to obtain the {@link #EMPTY_REF}
	 * instance.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> Ref<T> emptyRef() {
		return (Ref<T>) EMPTY_REF;
	}
}
