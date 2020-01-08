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
package de.ims.icarus2.model.manifest.xml;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Base class for readers or writers of manifest objects that make use of cached
 * {@link ManifestXmlDelegate} instances.
 *
 * @author Markus Gärtner
 *
 */
public abstract class ManifestXmlProcessor {

	private final Map<ManifestType, ManifestXmlDelegate<?>> topLevelDelegates = new Object2ObjectOpenHashMap<>();

	private final ManifestXmlDelegateFactory delegateFactory;

	protected ManifestXmlProcessor(ManifestXmlDelegateFactory delegateFactory) {
		requireNonNull(delegateFactory);

		this.delegateFactory = delegateFactory;
	}

	protected ManifestXmlDelegate<?> getDelegate(Manifest manifest) {
		ManifestType type = manifest.getManifestType();

		ManifestXmlDelegate<?> delegate = topLevelDelegates.get(type);

		if(delegate==null) {
			delegate = delegateFactory.newDelegate(manifest);

			topLevelDelegates.put(type, delegate);
		}

		return delegate;
	}

	/**
	 * Calls {@link ManifestXmlDelegate#reset()} on all delegates in the current
	 * map of top-level delegates and then {@link Map#clear() clears} that map.
	 */
	protected void resetDelegates() {
		topLevelDelegates.values().forEach(ManifestXmlDelegate::reset);
		topLevelDelegates.clear();
	}
}
