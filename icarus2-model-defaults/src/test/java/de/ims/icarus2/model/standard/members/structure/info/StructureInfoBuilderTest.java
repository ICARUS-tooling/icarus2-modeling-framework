/**
 *
 */
package de.ims.icarus2.model.standard.members.structure.info;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import de.ims.icarus2.test.annotations.PostponedTest;

/**
 * @author Markus Gärtner
 *
 */
class StructureInfoBuilderTest {

	@Nested
	class ForTrees {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.structure.info.StructureInfoBuilder#build(de.ims.icarus2.model.api.members.structure.Structure)}.
		 */
		@PostponedTest("need to figure out best way to provide example trees and stats in bulk")
		@ParameterizedTest
		@CsvFileSource(resources = {"StructureInfoBuilderTest_trees.csv"})
		void testBuild() {
			fail("Not yet implemented"); // TODO
		}
	}

}
