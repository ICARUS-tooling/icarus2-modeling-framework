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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import de.ims.icarus2.filedriver.FileDriver.FileDriverBuilder;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory;

/**
 * Wraps a factory implementation around a {@link FileDriverBuilder} to create a new
 * {@link FileDriver} instance.
 *
 * @author Markus Gärtner
 *
 */
public class DefaultFileDriverFactory implements Factory {

	private FileDriverBuilder builder;

	private void setBuilder(FileDriverBuilder builder) {
		checkNotNull(builder);
		checkState(this.builder==null);

		this.builder = builder;
	}

	protected FileDriverBuilder getBuilder() {
		checkState(builder!=null);
		return builder;
	}

	protected void finish() {
		builder = null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
	 */
	@Override
	public <T> T create(Class<T> resultClass, ImplementationManifest manifest,
			ImplementationLoader<?> loader) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException, ClassCastException {

		final DriverManifest driverManifest = (DriverManifest) manifest.getHostManifest();
		final Corpus corpus = ((CorpusMember)loader.getEnvironment()).getCorpus();

		// Early sanity checks
		checkNotNull("No corpus defined", corpus!=null);
		validateDriverManifest(driverManifest);

		final MetadataRegistry registry = createMetadataRegistry(corpus, driverManifest);

		// use FileDriverBuilder and add utility method for creation of required parts

		FileDriverBuilder<?> builder = createBuilder();

		setBuilder(builder);

		// Fill builder
		builder.manifest(driverManifest);

		// Registry for maintenance data
		builder.metadataRegistry(registry);

		FileDriver driver = builder.build();

		finish();

		return resultClass.cast(driver);
	}

	protected FileDriverBuilder<?> createBuilder() {
		return null;
//		return new FileDriverBuilder();
		//FIXME needs to be changed to determine actual correct (!!) builder implementation
	}

	/**
	 * Default implementation just uses the global metadata registry of the corpus for the current
	 * building process and creates a {@link SubRegistry} with the surrounding context manifest's
	 * id as prefix.
	 * @return
	 */
	protected MetadataRegistry createMetadataRegistry(Corpus corpus, DriverManifest driverManifest) {
		MetadataRegistry baseRegistry = corpus.getMetadataRegistry();

		String prefix = driverManifest.getContextManifest().getId();

		return new SubRegistry(baseRegistry, prefix);
	}

	protected void validateDriverManifest(DriverManifest manifest) {
		//TODO
	}
}
