/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.registry;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
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
import de.ims.icarus2.model.standard.members.layer.DefaultLayerGroup;
import de.ims.icarus2.model.standard.members.layer.annotation.AnnotationStorageFactory;
import de.ims.icarus2.model.standard.members.layer.annotation.DefaultAnnotationLayer;
import de.ims.icarus2.model.standard.members.layer.item.DefaultFragmentLayer;
import de.ims.icarus2.model.standard.members.layer.item.DefaultItemLayer;
import de.ims.icarus2.model.standard.members.layer.item.DefaultStructureLayer;
import de.ims.icarus2.model.standard.util.DefaultImplementationLoader;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(CorpusMemberFactory.class)
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
	 * This implementation delegates the task to a new instance of
	 * {@link ContextFactory} with the given parameters.
	 *
	 * @see de.ims.icarus2.model.api.registry.CorpusMemberFactory#createContext(de.ims.icarus2.model.api.corpus.Corpus, de.ims.icarus2.model.manifest.api.ContextManifest, de.ims.icarus2.util.Options)
	 */
	@Override
	public Context createContext(Corpus corpus, ContextManifest manifest,
			Options options) {
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

		//TODO check somehow if we can use packed annotations

		Object declaredStorage = options.get(manifest.getId().orElseThrow(
				Manifest.invalidId("Manifest does not declare an id")));
		if(AnnotationStorage.class.isInstance(declaredStorage)) {
			storage = (AnnotationStorage) declaredStorage;
		}

		if(storage==null) {
			storage = new AnnotationStorageFactory(manifest, options).buildStorage();
		}

		AnnotationLayer layer = new DefaultAnnotationLayer(manifest);
		layer.setAnnotationStorage(storage);

		return layer;
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
