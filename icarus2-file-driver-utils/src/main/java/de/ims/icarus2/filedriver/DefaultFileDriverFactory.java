/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriver.Builder;
import de.ims.icarus2.filedriver.io.sets.CompoundResourceSet;
import de.ims.icarus2.filedriver.io.sets.LazyResourceSet;
import de.ims.icarus2.filedriver.io.sets.ResourceSet;
import de.ims.icarus2.filedriver.io.sets.SingletonResourceSet;
import de.ims.icarus2.filedriver.resolver.DirectPathResolver;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.io.PathResolver;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.SubRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.util.DefaultImplementationLoader;
import de.ims.icarus2.util.io.resource.IOResource;
import de.ims.icarus2.util.io.resource.ReadOnlyStringResource;
import de.ims.icarus2.util.io.resource.ResourceProvider;

/**
 * Wraps a factory implementation around a {@link FileDriver#builder() Builder} to create a new
 * {@link FileDriver} instance.
 *
 * @author Markus Gärtner
 *
 */
public class DefaultFileDriverFactory implements Factory {

	private static final Logger log = LoggerFactory.getLogger(DefaultFileDriverFactory.class);

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
		}

		return loader.getEnvironment(Corpus.class);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ImplementationManifest.Factory#create(java.lang.Class, de.ims.icarus2.model.manifest.api.ImplementationManifest, de.ims.icarus2.model.manifest.api.ImplementationLoader)
	 */
	@Override
	public <T> T create(Class<T> resultClass, ImplementationManifest manifest,
			ImplementationLoader<?> loader) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException, ClassCastException {
		requireNonNull(resultClass);
		requireNonNull(manifest);
		requireNonNull(loader);

		if(!Driver.class.isAssignableFrom(resultClass))
			throw new ClassCastException(Messages.mismatch(
					"Requested result class is not compatible",
					FileDriver.class, resultClass));

		final DriverManifest driverManifest = ManifestUtils.requireHost(manifest);
		final Corpus corpus = getCorpus(loader);

		// Early sanity checks
		requireNonNull(corpus, "No corpus defined");
		validateDriverManifest(driverManifest);

		final MetadataRegistry registry = createMetadataRegistry(corpus, driverManifest);

		final ResourceProvider resourceProvider = createResourceProvider(corpus, driverManifest);

		final ContextManifest contextManifest = requireNonNull(loader.getEnvironment(ContextManifest.class),
				"No context manifest defined");

		final ResourceSet dataFiles = createResourceSet(corpus, resourceProvider, contextManifest.getLocationManifests());

		// use Builder and add utility method for creation of required parts

		Builder builder = createBuilder();

		setBuilder(builder);

		// Fill builder
		builder.manifest(driverManifest);
		builder.resourceProvider(resourceProvider);
		builder.metadataRegistry(registry);
		builder.dataFiles(dataFiles);

		FileDriver driver = builder.build();

		finish();

		return resultClass.cast(driver);
	}

	protected Builder createBuilder() {
		return new Builder();
	}

	/**
	 * Default implementation simply uses the {@link ResourceProvider} associated with the
	 * {@link CorpusManager manager} of the specified {@code corpus} argument.
	 *
	 * @param corpus
	 * @param driverManifest
	 * @return
	 */
	protected ResourceProvider createResourceProvider(Corpus corpus, DriverManifest driverManifest) {
		return corpus.getManager().getResourceProvider();
	}

	/**
	 * Default implementation just uses the global metadata registry of the corpus for the current
	 * building process and creates a {@link SubRegistry} with the surrounding context manifest's
	 * id as prefix.
	 * @return
	 */
	protected MetadataRegistry createMetadataRegistry(Corpus corpus, DriverManifest driverManifest) {
		MetadataRegistry baseRegistry = corpus.getMetadataRegistry();

		ContextManifest contextManifest = ManifestUtils.requireHost(driverManifest);
		String prefix = contextManifest.getId().orElseThrow(ManifestException.missing(
				contextManifest, "id"));

		return new SubRegistry(baseRegistry, prefix);
	}

	protected void validateDriverManifest(DriverManifest manifest) {
		//TODO
	}

	protected ResourceSet createResourceSet(Corpus corpus, ResourceProvider resourceProvider,
			List<LocationManifest> locationManifests) {
		List<ResourceSet> resourceSets = new ArrayList<>();

		// Collect all resources
		for(LocationManifest locationManifest : locationManifests) {
			resourceSets.add(toResourceSet(corpus, resourceProvider, locationManifest));
		}

		// Wrap collection of resources if needed
		if(resourceSets.size()==1) {
			return resourceSets.get(0);
		}

		assert !resourceSets.isEmpty() : "no resource sets";

		return new CompoundResourceSet(resourceSets);
	}

	protected ResourceSet toResourceSet(Corpus corpus, ResourceProvider resourceProvider,
			LocationManifest locationManifest) {


		if(locationManifest.isInline()) {
			// Special and easy case for inline data: just wrap it into a read-only resource
			URL location = locationManifest.getManifestLocation().getUrl();
			Path path = null;
			if(location!=null) {
				try {
					path = Paths.get(location.toURI());
				} catch (URISyntaxException e) {
					log.warn("Manifest location not resolvable to proper URI: {}", location.toExternalForm());
				}
			}
			if(path==null) {
				path = Paths.get(".");
			}
			IOResource resource = new ReadOnlyStringResource(
					path,
					locationManifest.getInlineData().orElseThrow(
							ManifestException.missing(locationManifest, "inline data")).toString(),
					// We simply use the encoding specified by the enclosing manifest
					locationManifest.getManifestLocation().getEncoding());
			return new SingletonResourceSet(resource);
		}

		PathType rootPathType = locationManifest.getRootPathType()
				.orElseThrow(ManifestException.missing(locationManifest, "root path type"));

		switch (rootPathType) {
		case FILE:
		case RESOURCE:
		case FOLDER: {
			PathResolver pathResolver = getResolverForLocation(corpus, resourceProvider, locationManifest);

			return new LazyResourceSet(corpus.getManager().getResourceProvider(), pathResolver);
		}

		default:
			//TODO implement handling of other path types
			throw new ModelException(corpus, GlobalErrorCode.NOT_IMPLEMENTED,
					"Currently no root path types other than FILE, RESOURCE or FOLDER are being supported: "+rootPathType);
		}
	}

	protected PathResolver getResolverForLocation(Corpus corpus, ResourceProvider resourceProvider,
			LocationManifest locationManifest) {
		PathResolverManifest pathResolverManifest = locationManifest.getPathResolverManifest().orElse(null);
		if(pathResolverManifest!=null) {
			// If our location specifies a custom path resolver -> delegate instantiation
			return corpus.getManager().newFactory().newImplementationLoader()
					.manifest(pathResolverManifest.getImplementationManifest()
							.orElseThrow(ManifestException.noElement(pathResolverManifest, "implementation")))
					.message("Path resolver for location "+locationManifest+" in corpus "+ManifestUtils.getName(corpus))
					.environment(Corpus.class, corpus)
					.instantiate(PathResolver.class);
		}

		ManifestRegistry registry = corpus.getManager().getManifestRegistry();

		return DirectPathResolver.forManifest(locationManifest, resourceProvider, registry.getVariableResolver());
	}
}
