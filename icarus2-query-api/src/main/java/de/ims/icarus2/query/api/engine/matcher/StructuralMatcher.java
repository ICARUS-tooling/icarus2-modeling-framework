/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.structure.Structure;

/**
 * Provides the basic framework for matching sequences ({@link Container}),
 * trees ({@link Structure}) or graphs (also {@link Structure}}) according to
 * the IQL specification.
 *
 * @author Markus Gärtner
 *
 */
public class StructuralMatcher {

	//TODO once SequenceMatcher is done, extract common functionality into this super class
}
