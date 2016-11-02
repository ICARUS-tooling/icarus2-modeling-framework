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
package de.ims.icarus2.model.standard.registry.metadata.policy;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.nio.file.Path;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.MetadataRegistry;
import de.ims.icarus2.model.api.registry.MetadataStoragePolicy;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.standard.io.DefaultFileStructure;
import de.ims.icarus2.model.standard.registry.metadata.JAXBMetadataRegistry;
import de.ims.icarus2.model.standard.registry.metadata.PlainMetadataRegistry;

/**
 * Implements a set of metadata policies that use a new file-based metadata registry for every target object.
 * <p>
 * The strategy is to create one folder below the root folder for every corpus. This folder will use the
 * {@link #CORPUS_PREFIX} and the corpus' {@link CorpusManifest#getId()} as name.
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
		checkNotNull(rootFolder);
		checkNotNull(format);

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

	protected MetadataRegistry createRegistry(Path file) {
		switch (format) {
		case PLAIN: return PlainMetadataRegistry.getSharedRegistry(file);
		case XML: return JAXBMetadataRegistry.getSharedRegistry(file);
		default:
			throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Unknown format type: "+format);
		}
	}

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

			return createRegistry(metadataFile);
		}
	}

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

			Path corpusFolder = getCorpusFolder(manifest.getCorpusManifest());

			Path metadataFile = corpusFolder.resolve(getMetadataFileName(manifest));

			return createRegistry(metadataFile);
		}
	}
}
