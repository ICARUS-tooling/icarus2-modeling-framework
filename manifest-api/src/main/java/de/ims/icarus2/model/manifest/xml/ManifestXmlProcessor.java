/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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

	protected void reset() {
		topLevelDelegates.values().forEach(ManifestXmlDelegate::reset);
		topLevelDelegates.clear();
	}
}
