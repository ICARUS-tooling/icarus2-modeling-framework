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

import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.strings.StringUtil.getName;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriver.Builder;
import de.ims.icarus2.filedriver.io.sets.CompoundResourceSet;
import de.ims.icarus2.filedriver.io.sets.LazyResourceSet;
import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.filedriver.io.sets.SingletonResourceSet;
import de.ims.icarus2.filedriver.resolver.DirectPathResolver;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.io.PathResolver;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.model.api.io.resources.ReadOnlyStringResource;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.model.standard.util.DefaultImplementationLoader;

/**
 * Wraps a factory implementation around a {@link Builder} to create a new
 * {@link FileDriver} instance.
 *
 * @author Markus Gärtner
 *
 */
public class DefaultFileDriverFactory implements Factory {

	private Builder builder;

	private void setBuilder(Builder builder) {
		requireNonNull(builder);
		checkState(this.builder==null);

		this.builder = builder;
	}

	protected Builder getBuilder() {
		checkState(builder!=null);
		return builder;
	}

	protected void finish() {
		builder = null;
	}

	private Corpus getCorpus(ImplementationLoader<?> loader) {
		if(loader instanceof DefaultImplementationLoader) {
			return ((DefaultImplementationLoader)loader).getCorpus();
		} else {
			Object environment = loader.getEnvironment();

			if(environment instanceof Corpus) {
				return (Corpus) environment;
			}

			return null;
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
	 */
	@Override
	public <T> T create(Class<T> resultClass, ImplementationManifest manifest,
			ImplementationLoader<?> loader) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException, ClassCastException {

		final DriverManifest driverManifest = (DriverManifest) manifest.getHostManifest();
		final Corpus corpus = getCorpus(loader);

		// Early sanity checks
		Objects.requireNonNull(corpus, "No corpus defined");
		validateDriverManifest(driverManifest);

		final MetadataRegistry registry = createMetadataRegistry(corpus, driverManifest);

		final ResourceSet dataFiles = createResourceSet(corpus, driverManifest.getContextManifest().getLocationManifests());

		// use Builder and add utility method for creation of required parts

		Builder builder = createBuilder();

		setBuilder(builder);

		// Fill builder
		builder.manifest(driverManifest);

		// Registry for maintenance data
		builder.metadataRegistry(registry);

		// Links to all corpus files
		builder.dataFiles(dataFiles);

		FileDriver driver = builder.build();

		finish();

		return resultClass.cast(driver);
	}

	protected Builder createBuilder() {
		return new Builder();
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

	protected ResourceSet createResourceSet(Corpus corpus, List<LocationManifest> locationManifests) {
		List<ResourceSet> resourceSets = new ArrayList<>();

		// Collect all resources
		for(LocationManifest locationManifest : locationManifests) {
			resourceSets.add(toResourceSet(corpus, locationManifest));
		}

		// Wrap collection of resources if needed
		if(resourceSets.size()==1) {
			return resourceSets.get(0);
		} else {
			return new CompoundResourceSet(resourceSets);
		}
	}

	protected ResourceSet toResourceSet(Corpus corpus, LocationManifest locationManifest) {


		if(locationManifest.isInline()) {
			// Special and easy case for inline data: just wrap it into a read-only resource
			IOResource resource = new ReadOnlyStringResource(locationManifest.getInlineData().toString(), StandardCharsets.UTF_8); //TODO fetch correct encoding from manifest?
			return new SingletonResourceSet(resource);
		} else {
			PathType rootPathType = locationManifest.getRootPathType();

			if(rootPathType==PathType.FILE || rootPathType==PathType.FOLDER) {
				PathResolver pathResolver = getResolverForLocation(corpus, locationManifest);

				return new LazyResourceSet(pathResolver);
			} else
				//TODO implement handling of other path types
				throw new ModelException(corpus, GlobalErrorCode.NOT_IMPLEMENTED,
						"Currently no path types other than FILE or FOLDER are being supported");
		}
	}

	protected PathResolver getResolverForLocation(Corpus corpus, LocationManifest locationManifest) {
		PathResolverManifest pathResolverManifest = locationManifest.getPathResolverManifest();
		if(pathResolverManifest!=null) {
			// If our location specifies a custom path resolver -> delegate instantiation
			return corpus.getManager().newFactory().newImplementationLoader()
					.manifest(pathResolverManifest.getImplementationManifest())
					.message("Path resolver for location "+locationManifest+" in corpus "+getName(corpus))
					.environment(corpus)
					.instantiate(PathResolver.class);
		}

		return DirectPathResolver.forManifest(locationManifest);
	}
}
