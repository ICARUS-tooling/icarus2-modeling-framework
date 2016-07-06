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
 *
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
 * Unless otherwise noted, all {@link Options options} parameters in the factory methods of this
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
	 * via {@link ImplementationManifest manifests}.
	 *
	 * @return
	 */
	ImplementationLoader<?> newImplementationLoader();
}
