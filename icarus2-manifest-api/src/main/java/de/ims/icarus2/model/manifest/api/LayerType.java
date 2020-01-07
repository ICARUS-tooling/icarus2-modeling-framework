/**
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
package de.ims.icarus2.model.manifest.api;

import java.util.Optional;

/**
 * Implements a shared type descriptor for {@code Layer} objects. It is used to
 * group layers according to an abstract description of their content. Besides
 * serving as a mere identifier to that abstract description, a {@code LayerType}
 * optionally provides a {@link LayerManifest} that contains further specifications
 * on how the content might be structured or other informations. Each layer type
 * is globally identifiably by its unique id as defined by the {@link Category} contract.
 * <p>
 * Note that it is possible to provide category definitions both in this wrapper
 * and in the associated {@link #getSharedManifest() layer manifest}. In case of
 * concurrent category definitions the one defined locally by {@link #getId()}
 * takes priority over the one defined in the nested layer manifest!
 *
 * @author Markus Gärtner
 * @see LayerManifest
 *
 */
public interface LayerType extends Category {

	/**
	 * Returns the shared {@code LayerManifest} that further describes layers of
	 * this type or an empty {@link Optional} if this type only serves as a identifier without
	 * additional content restrictions.
	 * @return
	 */
	Optional<LayerManifest<?>> getSharedManifest();
}
