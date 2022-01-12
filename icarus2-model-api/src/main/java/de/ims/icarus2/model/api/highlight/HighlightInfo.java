/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.highlight;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LazyMap;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.id.Identifiable;

/**
 * Implements a sharable meta information for highlighting that
 * provides descriptive information encapsulated in an {@link #getIdentity() identity}
 * object and a (optional) set of affected {@link #getAffectedAnnotations() annotations}.
 * <p>
 * In addition it can contain payload data created by client code that might offer
 * additional informations.
 *
 * @author Markus Gärtner
 *
 */
public interface HighlightInfo extends Identifiable {

	/**
	 * Returns all the annotations affected by this info.
	 * The returned {@link DataSet set} is allowed to be empty.
	 * @return
	 */
	DataSet<AnnotationLink> getAffectedAnnotations();

	default Set<AnnotationLayer> getAffectedAnnotationLayers() {
		DataSet<AnnotationLink> links = getAffectedAnnotations();

		LazyCollection<AnnotationLayer> result = LazyCollection.lazySet(links.entryCount());

		if(!links.isEmpty()) {
			links.forEach(l -> result.add(l.getLayer()));
		}

		return result.getAsSet();
	}

	default Set<String> getAffectedAnnotationKeys(AnnotationLayer layer) {
		DataSet<AnnotationLink> links = getAffectedAnnotations();

		LazyCollection<String> result = LazyCollection.lazySet(links.entryCount());

		if(!links.isEmpty()) {
			links.forEach(l -> {
				if(l.getLayer()==layer)
					result.add(l.getKey());
			});
		}

		return result.getAsSet();
	}

	/**
	 * Returns the host layer this info object originated from.
	 *
	 * @return
	 */
	HighlightLayer getLayer();

	Object getProperty(String name);

	void forEachProperty(BiConsumer<? super String, ? super Object> action);

	default Map<String, Object> getProperties() {
		LazyMap<String, Object> result = LazyMap.lazyHashMap();

		forEachProperty(result);

		return result.getAsMap();
	}

	default Set<String> getPropertyNames() {
		LazyCollection<String> result = LazyCollection.lazySet();

		forEachProperty((n, v) -> result.add(n));

		return result.getAsSet();
	}

	/**
	 * Models a link to a single annotation defined by its host
	 * {@link AnnotationLayer layer} and the associated unique
	 * {@link #getKey() key}. Note that such a link is only valid
	 * during the actual lifetime of the associated layer. The
	 * behavior is undefined once the driver responsible for
	 * the layer is closed.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface AnnotationLink {

		/**
		 * Returns the {@link AnnotationLayer layer} associated with this
		 * link. Implementations are free to model that association with
		 * any kind of weak, strong or indirect reference and may throw
		 * an exception in case the reference to the layer is no longer valid.
		 */
		AnnotationLayer getLayer();

		/**
		 * Returns the {@link AnnotationManifest#getKey() key} associated with
		 * this link.
		 */
		String getKey();
	}
}
