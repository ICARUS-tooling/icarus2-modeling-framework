/**
 *
 */
package de.ims.icarus2.util;

import org.assertj.core.api.InstanceOfAssertFactory;

import de.ims.icarus2.util.collections.BlockingLongBatchQueue;
import de.ims.icarus2.util.collections.BlockingLongBatchQueueAssert;

/**
 * @author Markus Gärtner
 *
 */
public class UtilAssertions {

	// ASSERTIONS

	public static BlockingLongBatchQueueAssert assertThat(BlockingLongBatchQueue actual) {
		return new BlockingLongBatchQueueAssert(actual);
	}

	//TODO

	// Factory helpers

	public static final InstanceOfAssertFactory<BlockingLongBatchQueue, BlockingLongBatchQueueAssert> MATCH =
			new InstanceOfAssertFactory<>(BlockingLongBatchQueue.class, UtilAssertions::assertThat);
}
