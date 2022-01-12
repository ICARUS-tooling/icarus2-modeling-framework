/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.container;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(ContainerEditVerifier.class)
public class SingletonContainerEditVerifier extends UnrestrictedContainerEditVerifier {

	/**
	 * @param source
	 */
	public SingletonContainerEditVerifier(Container source) {
		super(source);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.UnrestrictedContainerEditVerifier#isValidAddItemIndex(long)
	 */
	@Override
	protected boolean isValidAddItemIndex(long index) {
		return index==0L && getSource().getItemCount()==0L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.UnrestrictedContainerEditVerifier#isValidRemoveItemIndex(long)
	 */
	@Override
	protected boolean isValidRemoveItemIndex(long index) {
		return index==0L && getSource().getItemCount()==1L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.UnrestrictedContainerEditVerifier#canSwapItems(long, long)
	 */
	@Override
	public boolean canSwapItems(long index0, long index1) {
		return false;
	}
}