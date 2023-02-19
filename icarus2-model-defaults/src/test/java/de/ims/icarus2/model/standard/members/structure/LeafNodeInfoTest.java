/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.structure;

import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.standard.members.structure.NodeInfo.Type;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class LeafNodeInfoTest implements NodeInfoTest<LeafNodeInfo> {

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends LeafNodeInfo> getTestTargetClass() {
		return LeafNodeInfo.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public LeafNodeInfo createTestInstance(TestSettings settings) {
		return settings.process(new LeafNodeInfo());
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getExpectedType()
	 */
	@Override
	public Type getExpectedType() {
		return Type.LEAF;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getIncomingEquivalent()
	 */
	@Override
	public StructureType getIncomingEquivalent() {
		return StructureType.CHAIN;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.structure.NodeInfoTest#getOutgoingEquivalent()
	 */
	@Override
	public StructureType getOutgoingEquivalent() {
		return StructureType.SET;
	}

}
