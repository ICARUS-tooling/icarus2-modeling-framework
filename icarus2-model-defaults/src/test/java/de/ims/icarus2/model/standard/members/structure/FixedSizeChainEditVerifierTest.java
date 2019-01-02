/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.standard.members.structure.FixedSizeChainStorage.FixedSizeChainEditVerifier;

/**
 * @author Markus Gärtner
 *
 */
class FixedSizeChainEditVerifierTest {

	@TestFactory
	Stream<DynamicTest> testEmptyChain() {
		FixedSizeChainEditVerifier verifier = new FixedSizeChainEditVerifier(storage, source)
	}
}
