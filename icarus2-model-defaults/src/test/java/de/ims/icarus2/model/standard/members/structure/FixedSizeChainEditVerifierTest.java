/**
 *
 */
package de.ims.icarus2.model.standard.members.structure;

import static de.ims.icarus2.test.util.Pair.longPair;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.members.structure.StructureEditVerifierTestBuilder;
import de.ims.icarus2.model.standard.members.container.ItemStorage;
import de.ims.icarus2.model.standard.members.container.ListItemStorageInt;
import de.ims.icarus2.model.standard.members.structure.FixedSizeChainStorage.FixedSizeChainEditVerifier;

/**
 * @author Markus Gärtner
 *
 */
class FixedSizeChainEditVerifierTest {

	private ItemStorage itemStorage;
	private FixedSizeChainStorage edgeStorage;
	private FixedSizeChainEditVerifier verifier;
	private Structure structure;

	@BeforeEach
	void prepare() {
		itemStorage = new ListItemStorageInt();
		edgeStorage = new FixedSizeChainStorage();
		structure = new DefaultStructure(itemStorage, edgeStorage);

		verifier = (FixedSizeChainEditVerifier) structure.createEditVerifier();
	}

	@AfterEach
	void cleanup() {
		verifier = null;
		structure = null;
		edgeStorage = null;
		itemStorage = null;
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	public Stream<DynamicTest> testEmptyStructure() {

		return new StructureEditVerifierTestBuilder(verifier)
				.addSingleIllegal(-1, 0, 1)
				.addBatchIllegal(-1, 0, 1)
				.removeSingleIllegal(-1, 0, 1)
				.removeBatchIllegal(longPair(0, 0), longPair(0,  1))
				.swapSingleIllegal(longPair(0,  0), longPair(0,  1))
				//TODO add illegal values for terminal changes and edge creation!!!
				.createTests();
	}
}
