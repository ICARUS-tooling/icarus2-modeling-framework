/**
 *
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.util.strings.StringUtil.getName;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ManifestException;

/**
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class HierarchyImpl<E extends Object> extends AbstractLockable implements Hierarchy<E> {

	private final ArrayList<E> items = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#getRoot()
	 */
	@Override
	public E getRoot() {
		return items.isEmpty() ? null : items.get(0);
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
	public void add(E item) {
		checkNotLocked();

		insert0(item, -1, true);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.Hierarchy#remove(java.lang.Object)
	 */
	@Override
	public void remove(E item) {
		checkNotLocked();

		remove0(item);
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
	public void insert(E item, int index) {
		checkNotLocked();

		insert0(item, index, false);
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
}
