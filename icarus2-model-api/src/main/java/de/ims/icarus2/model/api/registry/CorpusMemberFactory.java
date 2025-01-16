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
package de.ims.icarus2.model.api.registry;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.util.Options;

/**
 * A factory to create various high level members of a corpus.
 * This includes all kinds of {@link Layer layers}, {@link LayerGroup layer groups} and the top
 * level type {@link Context}.
 * <p>
 * In addition this factory is meant to provide {@link ImplementationLoader} instances to load
 * custom or foreign implementations of things like drivers.
 * <p>
 * Unless otherwise noted, all {@link Options} parameters in the factory methods of this
 * class are allowed to be {@code null}.
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusMemberFactory {

	Context createContext(Corpus corpus, ContextManifest manifest, Options options);
	LayerGroup createLayerGroup(LayerGroupManifest groupManifest, Options options);
	AnnotationLayer createAnnotationLayer(Corpus corpus, AnnotationLayerManifest manifest, Options options);
	ItemLayer createItemLayer(Corpus corpus, ItemLayerManifest manifest, Options options);
	StructureLayer createStructureLayer(Corpus corpus, StructureLayerManifest manifest, Options options);
	FragmentLayer createFragmentLayer(Corpus corpus, FragmentLayerManifest manifest, Options options);


	/**
	 * Creates a loader suitable for loading and instantiating classes defined
	 * via {@link ImplementationManifest implementation manifests}.
	 *
	 * @return
	 */
	ImplementationLoader<?> newImplementationLoader();
}
