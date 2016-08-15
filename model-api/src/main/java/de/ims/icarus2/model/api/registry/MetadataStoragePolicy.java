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
 */
package de.ims.icarus2.model.api.registry;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;

/**
 * Models management of {@link MetadataRegistry} instances for individual objects
 * like corpora, contexts, etc. which can have their own dedicated registries,
 * use multiple shared ones or access a single client wide one.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface MetadataStoragePolicy<O extends Object> {

	/**
	 * Returns the {@link MetadataRegistry} to be used for the given {@code target}.
	 * A return value of {@code null} means the caller should decide upon the registry
	 * instance to be used.
	 * <p>
	 * Implementations should throw {@link ModelException}s when they encounter a situation
	 * where they suspect corrupted settings or missing information.
	 *
	 * @param manager
	 * @param hostRegistry
	 * @param object
	 * @return
	 */
	MetadataRegistry registryFor(CorpusManager manager, MetadataRegistry hostRegistry, O target);

	/**
	 * A shared global policy for corpus metadata that always returns {@code null}
	 */
	public static final MetadataStoragePolicy<CorpusManifest> emptyCorpusMetadataStoragePolicy = (m,r,c) -> null;
	public static final MetadataStoragePolicy<ContextManifest> emptyContextMetadataStoragePolicy = (m,r,c) -> null;
}
