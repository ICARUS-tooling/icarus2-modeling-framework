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

 * $Revision: 429 $
 * $Date: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/registry/DefaultCorpusMemberFactory.java $
 *
 * $LastChangedDate: 2015-10-07 17:08:17 +0200 (Mi, 07 Okt 2015) $
 * $LastChangedRevision: 429 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.registry;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.standard.corpus.DefaultContext;
import de.ims.icarus2.model.standard.members.layers.DefaultLayerGroup;
import de.ims.icarus2.model.standard.members.layers.annotation.AnnotationStorageFactory;
import de.ims.icarus2.model.standard.members.layers.annotation.DefaultAnnotationLayer;
import de.ims.icarus2.model.standard.members.layers.item.DefaultFragmentLayer;
import de.ims.icarus2.model.standard.members.layers.item.DefaultItemLayer;
import de.ims.icarus2.model.standard.members.layers.item.DefaultStructureLayer;
import de.ims.icarus2.model.standard.util.DefaultImplementationLoader;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultCorpusMemberFactory.java 429 2015-10-07 15:08:17Z mcgaerty $
 *
 */
public class DefaultCorpusMemberFactory implements CorpusMemberFactory {

	private final CorpusManager corpusManager;

	public DefaultCorpusMemberFactory(CorpusManager corpusManager) {
		this.corpusManager = corpusManager;
	}

	public CorpusManager getCorpusManager() {
		return corpusManager;
	}

	/**
	 * Creates a new instance of {@link DefaultContext}.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#createContext(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.model.manifest.api.ContextManifest, de.ims.icarus2.util.Options)
	 */
	@Override
	public Context createContext(Corpus corpus, ContextManifest manifest,
			Options options) {
//		return new DefaultContext(corpus, manifest);
		return new ContextFactory().createContext(corpus, manifest, options);
	}

	/**
	 * Creates a new instance of {@link DefaultLayerGroup}.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#createLayerGroup(de.ims.icarus2.model.manifest.api.LayerGroupManifest, de.ims.icarus2.util.Options)
	 */
	@Override
	public LayerGroup createLayerGroup(LayerGroupManifest groupManifest,
			Options options) {
		return new DefaultLayerGroup(groupManifest);
	}

	/**
	 * Creates a new instance of {@link DefaultAnnotationLayer}.
	 * <p>
	 * If there is an {@link AnnotationStorage} present in the given {@code options} and it is
	 * mapped to the {@link Manifest#getId() id} of the supplied {@code manifest}, it will be
	 * used as storage for the new layer. Otherwise creation of the storage will be forwarded
	 * to a new {@link AnnotationStorageFactory storage factory} instance.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#createAnnotationLayer(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.model.manifest.api.AnnotationLayerManifest, de.ims.icarus2.util.Options)
	 */
	@Override
	public AnnotationLayer createAnnotationLayer(Corpus corpus,
			AnnotationLayerManifest manifest, Options options) {

		AnnotationStorage storage = null;

		Object declaredStorage = options.get(manifest.getId());
		if(AnnotationStorage.class.isInstance(declaredStorage)) {
			storage = (AnnotationStorage) declaredStorage;
		}

		if(storage==null) {
			storage = new AnnotationStorageFactory(manifest, options).buildStorage();
		}

		return new DefaultAnnotationLayer(manifest, storage);
	}

	/**
	 * Creates a new instance of {@link DefaultItemLayer}.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#createItemLayer(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.model.manifest.api.ItemLayerManifest, de.ims.icarus2.util.Options)
	 */
	@Override
	public ItemLayer createItemLayer(Corpus corpus, ItemLayerManifest manifest,
			Options options) {
		return new DefaultItemLayer(manifest);
	}

	/**
	 * Creates a new instance of {@link DefaultStructureLayer}.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#createStructureLayer(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.model.manifest.api.StructureLayerManifest, de.ims.icarus2.util.Options)
	 */
	@Override
	public StructureLayer createStructureLayer(Corpus corpus,
			StructureLayerManifest manifest, Options options) {
		return new DefaultStructureLayer(manifest);
	}

	/**
	 * Creates a new instance of {@link DefaultFragmentLayer}.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#createFragmentLayer(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.model.manifest.api.FragmentLayerManifest, de.ims.icarus2.util.Options)
	 */
	@Override
	public FragmentLayer createFragmentLayer(Corpus corpus,
			FragmentLayerManifest manifest, Options options) {
		return new DefaultFragmentLayer(manifest);
	}

	/**
	 * Creates a new instance of {@link DefaultImplementationLoader}, using the
	 * {@link PluginRegistry} and {@link PluginManager} supplied at creation time.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#newImplementationLoader()
	 */
	@Override
	public ImplementationLoader<?> newImplementationLoader() {
		return new DefaultImplementationLoader(corpusManager);
	}
}
