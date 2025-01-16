/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
