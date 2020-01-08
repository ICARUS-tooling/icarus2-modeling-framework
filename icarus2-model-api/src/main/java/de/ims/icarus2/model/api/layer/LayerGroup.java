/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.layer;

import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * Groups several layers, so that they are guaranteed to be loaded together.
 * The idea behind layer groups is to bundle layers that are so closely tight together
 * in the respective physical storage, that it is practically impossible to load them
 * separately. For example in a text based data format like the format of the
 * <i>CoNLL 2012 Shared Task</i> which falls into the <i>item centric</i> category,
 * loading sentences without moving the content of underlying tokens into memory is not
 * feasible. While for the purpose of inspecting or visualizing data there might be the
 * motivation to provide a finer granularity than layers grouped by format design,
 * drivers and member caches of a corpus cannot efficiently handle such fine grained
 * data chunks.
 *
 * @author Markus Gärtner
 *
 */
public interface LayerGroup extends Part<Context> {

	Context getContext();

	Set<Layer> getLayers();

	LayerGroupManifest getManifest();

	ItemLayer getPrimaryLayer();

	default Set<Dependency<LayerGroup>> getDependencies() {
		LazyCollection<Dependency<LayerGroup>> result = LazyCollection.lazySet();
		forEachDependency(result);
		return result.getAsSet();
	}

	void forEachDependency(Consumer<? super Dependency<LayerGroup>> action);

	void forEachLayer(Consumer<? super Layer> action);
}
