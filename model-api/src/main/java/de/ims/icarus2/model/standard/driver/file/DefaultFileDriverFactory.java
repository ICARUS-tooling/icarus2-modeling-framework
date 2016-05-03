/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.standard.driver.file;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import static de.ims.icarus2.model.util.Conditions.checkState;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.manifest.DriverManifest;
import de.ims.icarus2.model.api.manifest.ImplementationLoader;
import de.ims.icarus2.model.api.manifest.ImplementationManifest;
import de.ims.icarus2.model.api.manifest.ImplementationManifest.Factory;
import de.ims.icarus2.model.registry.MetadataRegistry;
import de.ims.icarus2.model.registry.SubRegistry;
import de.ims.icarus2.model.standard.driver.file.FileDriver.FileDriverBuilder;

/**
 * Wraps a factory implementation around a {@link FileDriverBuilder} to create a new
 * {@link FileDriver} instance.
 *
 * @author Markus Gärtner
 * @version $Id$
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
	 * @see de.ims.icarus2.model.api.manifest.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.api.manifest.ImplementationManifest, de.ims.icarus2.model.api.manifest.ImplementationLoader)
	 */
	@Override
	public <T> T create(Class<T> resultClass, ImplementationManifest manifest,
			ImplementationLoader<?> environment) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException, ClassCastException {

		final DriverManifest driverManifest = (DriverManifest) manifest.getHostManifest();
		final Corpus corpus = environment.getCorpus();

		// Early sanity checks
		checkNotNull("No corpus defined", corpus!=null);
		validateDriverManifest(driverManifest);

		// use FileDriverBuilder and add utility method for creation of required parts

		FileDriverBuilder builder = createBuilder();

		setBuilder(builder);

		// Fill builder
		builder.manifest(driverManifest);

		// Registry for maintenance data
		builder.metadataRegistry(createMetadataRegistry(corpus));

		FileDriver driver = builder.build();

		finish();

		return resultClass.cast(driver);
	}

	protected FileDriverBuilder createBuilder() {
		return new FileDriverBuilder();
	}

	/**
	 * Default implementation just uses the global metadata registry of the corpus for the current
	 * building process and creates a {@link SubRegistry} with the context manifest's id as prefix
	 * (i.e. the context manifest hosting the given driver).
	 * @return
	 */
	protected MetadataRegistry createMetadataRegistry(Corpus corpus) {
		MetadataRegistry baseRegistry = corpus.getMetadataRegistry();

		String prefix = getBuilder().getManifest().getContextManifest().getId();

		return new SubRegistry(baseRegistry, prefix);
	}

	protected void validateDriverManifest(DriverManifest manifest) {
		//TODO
	}
}
