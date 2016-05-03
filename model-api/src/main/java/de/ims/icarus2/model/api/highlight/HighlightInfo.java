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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.api.highlight;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.HighlightLayer;
import de.ims.icarus2.model.api.manifest.AnnotationManifest;
import de.ims.icarus2.model.util.DataSet;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LazyMap;
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
 * @version $Id$
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
			links.forEachEntry(l -> result.add(l.getLayer()));
		}

		return result.getAsSet();
	}

	default Set<String> getAffectedAnnotationKeys(AnnotationLayer layer) {
		DataSet<AnnotationLink> links = getAffectedAnnotations();

		LazyCollection<String> result = LazyCollection.lazySet(links.entryCount());

		if(!links.isEmpty()) {
			links.forEachEntry(l -> {
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
	 * @version $Id$
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
