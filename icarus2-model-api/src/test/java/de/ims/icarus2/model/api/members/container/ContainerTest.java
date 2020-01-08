/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.members.container;

import de.ims.icarus2.model.api.members.ItemTest;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface ContainerTest<C extends Container> extends ItemTest<C> {

	/**
	 * @see de.ims.icarus2.model.api.members.MemberTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<C> apiGuard) {
		ItemTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("itemsComplete",
				Boolean.valueOf(Container.DEFAULT_ITEMS_COMPLETE));
	}

}
