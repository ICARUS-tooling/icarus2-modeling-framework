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
package de.ims.icarus2.model.api.members;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.model.api.corpus.Corpus;



/**
 *
 * @author Markus Gärtner
 *
 */
@Api
public interface CorpusMember {

//	/**
//	 * @return The globally unique id
//	 */
//	long getId();

	/**
	 * Returns the corpus this member is a part of.
	 * This call is usually forwarded to the host {@code CorpusView}
	 * @return The corpus this member is a part of
	 */
	Corpus getCorpus();

	/**
	 * Returns the type of this member. Note that the correct
	 * way of performing type specific operations on a {@code CorpusMember}
	 * is to query its type through this method and <b>not</b> by using the
	 * {@code instanceof} operator!
	 *
	 * @return The type of this member
	 */
	MemberType getMemberType();
}
