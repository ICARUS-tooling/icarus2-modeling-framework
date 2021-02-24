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
package de.ims.icarus2.model.manifest.api;

import java.util.Optional;

/**
 * Models a basic unidirectional hierarchical relation between an embedded
 * manifest component and its host.
 * <p>
 * Implementation note: Any interface or class implementing or extending this
 * interface should use the {@link #getHost()} method as the singular point
 * of access to the surrounding host manifest. If another return type is
 * required, a proxy method can be declared or the method can be overridden
 * with a more specific type.
 *
 * @author Markus Gärtner
 *
 */
public interface Embedded {

	/**
	 * Returns the manifest hosting this element. Note that as a rule of
	 * consistency all other more type specific methods that return the same
	 * object under another signature must delegate to this method!
	 *
	 * @return
	 */
	<T extends TypedManifest> Optional<T> getHost();
}
