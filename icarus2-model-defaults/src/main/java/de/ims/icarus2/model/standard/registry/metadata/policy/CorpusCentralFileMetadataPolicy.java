/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.registry.metadata.policy;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.io.DefaultFileStructure;
import de.ims.icarus2.model.standard.registry.metadata.JAXBMetadataRegistry;
import de.ims.icarus2.model.standard.registry.metadata.PlainMetadataRegistry;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * Implements a set of metadata policies that use a new file-based metadata registry for every target object.
 * <p>
 * The strategy is to create one folder below the root folder for every corpus. This folder will use the
 * {@link DefaultFileStructure#CORPUS_PREFIX} and the corpus' {@link CorpusManifest#getId()} as name.
 * <br>
 * Below this corpus-individual root folder there will be one metadata registry file for the corpus itself and
 * then one for every context/driver.
 * <p>
 * Exact naming schemes:
 * <table border="1">
 * <tr><th>Target type</th><th>Path</th></tr>
 * <tr>
 * <td>corpus-folder</td>
 * <td>/corpus_{@link CorpusManifest#getId() &lt;corpus-id&gt;}</td>
 * </tr>
 * <tr>
 * <td>{@link CorpusManifest}</td>
 * <td>/corpus{@link PlainMetadataRegistry#DEFAULT_FILE_ENDING .mdini}  <br>  /corpus{@link JAXBMetadataRegistry#DEFAULT_FILE_ENDING .mdxml}</td>
 * </tr>
 * <tr>
 * <td>{@link DriverManifest}</td>
 * <td>/context_{@link ContextManifest#getId() &lt;context-id&gt;}{@link PlainMetadataRegistry#DEFAULT_FILE_ENDING .mdini} <br>
 * /context_{@link ContextManifest#getId() &lt;context-id&gt;}{@link JAXBMetadataRegistry#DEFAULT_FILE_ENDING .mdxml}
 * </td>
 * </tr>
 * </table>
 *
 * @author Markus Gärtner
 *
 */
public abstract class CorpusCentralFileMetadataPolicy<O extends Object> implements MetadataStoragePolicy<O> {

	public static final String CORPUS_FILENAME = "corpus";

	public enum Format {
		XML(JAXBMetadataRegistry.DEFAULT_FILE_ENDING),
		PLAIN(PlainMetadataRegistry.DEFAULT_FILE_ENDING),
		;

		private final String fileSuffix;

		private Format(String fileSuffix) {
			this.fileSuffix = fileSuffix;
		}

		public String getFileSuffix() {
			return fileSuffix;
		}
	}

	private final Format format;

	private final Path rootFolder;

	protected CorpusCentralFileMetadataPolicy(Path rootFolder, Format format) {
		requireNonNull(rootFolder);
		requireNonNull(format);

		this.rootFolder = rootFolder;
		this.format = format;
	}

	protected Path getCorpusFolder(CorpusManifest manifest) {
		return rootFolder.resolve(DefaultFileStructure.CORPUS_PREFIX+manifest.getId());
	}

	protected String getMetadataFileName(CorpusManifest manifest) {
		return CORPUS_FILENAME+format.getFileSuffix();
	}

	protected String getMetadataFileName(ContextManifest manifest) {
		return DefaultFileStructure.CONTEXT_PREFIX+manifest.getId()+format.getFileSuffix();
	}

	protected MetadataRegistry createRegistry(IOResource resource) {
		switch (format) {
		case PLAIN: return new PlainMetadataRegistry(resource);
		case XML: return new JAXBMetadataRegistry(resource);
		default:
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Unknown format type: "+format);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@TestableImplementation(MetadataStoragePolicy.class)
	public static class CorpusPolicy extends CorpusCentralFileMetadataPolicy<CorpusManifest> {

		/**
		 * @param rootFolder
		 * @param format
		 */
		public CorpusPolicy(Path rootFolder, Format format) {
			super(rootFolder, format);
		}

		/**
		 * @see de.ims.icarus2.model.api.registry.MetadataStoragePolicy#registryFor(de.ims.icarus2.model.api.registry.CorpusManager, de.ims.icarus2.model.api.registry.MetadataRegistry, java.lang.Object)
		 */
		@Override
		public MetadataRegistry registryFor(CorpusManager manager,
				MetadataRegistry hostRegistry, CorpusManifest manifest) {

			Path corpusFolder = getCorpusFolder(manifest);

			Path metadataFile = corpusFolder.resolve(getMetadataFileName(manifest));

			IOResource resource;
			try {
				resource = manager.getResourceProvider().getResource(metadataFile);
			} catch (IOException e) {
				throw new ModelException(GlobalErrorCode.IO_ERROR,
						"Failed to fetch metadata resource: "+metadataFile, e);
			}

			return createRegistry(resource);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@TestableImplementation(MetadataStoragePolicy.class)
	public static class ContextPolicy extends CorpusCentralFileMetadataPolicy<ContextManifest> {

		/**
		 * @param rootFolder
		 * @param format
		 */
		public ContextPolicy(Path rootFolder, Format format) {
			super(rootFolder, format);
		}

		/**
		 * @see de.ims.icarus2.model.api.registry.MetadataStoragePolicy#registryFor(de.ims.icarus2.model.api.registry.CorpusManager, de.ims.icarus2.model.api.registry.MetadataRegistry, java.lang.Object)
		 */
		@Override
		public MetadataRegistry registryFor(CorpusManager manager,
				MetadataRegistry hostRegistry, ContextManifest manifest) {

			Path corpusFolder = getCorpusFolder(ManifestUtils.requireHost(manifest));

			Path metadataFile = corpusFolder.resolve(getMetadataFileName(manifest));

			IOResource resource;
			try {
				resource = manager.getResourceProvider().getResource(metadataFile);
			} catch (IOException e) {
				throw new ModelException(GlobalErrorCode.IO_ERROR,
						"Failed to fetch metadata resource: "+metadataFile, e);
			}

			return createRegistry(resource);
		}
	}
}
