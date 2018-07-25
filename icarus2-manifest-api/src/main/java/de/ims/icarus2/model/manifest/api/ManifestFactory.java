/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestFactory {

	default <M extends ManifestFragment> M create(ManifestType type) {
		return create(type, null, null);
	}

	default <M extends ManifestFragment> M create(ManifestType type, Object host) {
		return create(type, host, null);
	}

	<M extends ManifestFragment> M create(ManifestType type, Object host, Options options);

	ManifestLocation getManifestLocation();

	ManifestRegistry getRegistry();
}
