/**
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.function.LongUnaryOperator;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.manifest.util.Messages;

/**
 * @author Markus
 *
 */
public class VirtualIndexSet implements IndexSet {

	//TODO should model an index set that takes a starting index, a size and has a user-defined function that can create values for indices

	/**
	 * Offset to be added to index values passed to the
	 * {@link #indexAt(int)} method before forwarding
	 * to the {@link #func} instance.
	 */
	protected final long offset;

	/**
	 * Upper limit for index values passed to the
	 * {@link #indexAt(int)} method.
	 */
	protected final int size;

	protected final LongUnaryOperator func;

	protected final IndexValueType valueType;

	public VirtualIndexSet(long offset, int size, IndexValueType valueType, LongUnaryOperator func) {
		checkArgument("Offset must be >= 0", offset>=0);
		checkArgument(size==IndexSet.UNKNOWN_SIZE || size>=0);

		this.offset = offset;
		this.size = size;
		this.valueType = requireNonNull(valueType);
		this.func = requireNonNull(func);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		if(index<0 || index>=size)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.indexOutOfBoundsMessage("Invalid index value", 0, size-1, index));
		return valueType.checkValue(func.applyAsLong(offset+index));
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()
	 */
	@Override
	public IndexValueType getIndexValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#sort()
	 */
	@Override
	public boolean sort() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		return new VirtualIndexSet(fromIndex, fromIndex-toIndex+1, valueType, func);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		return this;
	}
}
