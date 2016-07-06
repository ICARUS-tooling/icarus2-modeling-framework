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
 * $Revision: 457 $
 *
 */
package de.ims.icarus2.model.api.layer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.members.NamedCorpusMember;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 *
 * @author Markus Gärtner
 *
 */
public interface Layer extends NamedCorpusMember, Part<LayerGroup> {

//	/**
//	 * Returns a special integer id created by the framework that is
//	 * used for fast lookup or mapping operations involving layers. Note
//	 * that such an uid is only valid for the duration of the current session.
//	 * It is not guaranteed (in fact it is very unlikely) that a layer gets
//	 * assigned the same uid across multiple sessions, since the assignment
//	 * is performed on construction time of the layer, which happens when the
//	 * user starts to work with it. The returned value is always positive and not
//	 * {@code 0}!
//	 *
//	 * @return
//	 */
//	int getUID();

	/**
	 * Returns the name of the layer. This call is typically forwarded
	 * to the manifest that describes this layer.
	 *
	 * @see de.ims.icarus2.model.api.members.NamedCorpusMember#getName()
	 */
	@Override
	String getName();

	/**
	 * Returns the {@code Item} object that allows other members of
	 * the corpus framework to treat this layer as a regular markable.
	 * This feature can be used to assign annotations and/or highlightings
	 * on the layer level.
	 *
	 * @return
	 */
	Item getItemProxy();

	/**
	 * Returns the {@code Context} object that defines the physical
	 * source of this layer and provides information about other
	 * layers sharing the same origin.
	 * <p>
	 * Note that some layers do not require a host context (e.g. the
	 * virtual overlay layer each corpus provides in order to access
	 * its layers as markables). Therefore this method is allowed to
	 * return {@code null}. However, each layer that is hosted within
	 * a context (i.e. is accessible through it) {@code must} return
	 * that host context!
	 */
	Context getContext();

	LayerGroup getLayerGroup();

	/**
	 * Returns the {@code ItemLayer}s that this layer
	 * depends on. If the layer is independent of any other layers, it
	 * should simply return a shared empty {@code DataSet}.
	 *
	 * @return
	 */
	DataSet<ItemLayer> getBaseLayers();

	/**
	 * Returns the manifest object that describes the content of
	 * this layer. Subclasses should override this method to return
	 * a specific type of {@code LayerManifest}!
	 *
	 * @return The manifest describing the content of this layer
	 */
	LayerManifest getManifest();

	/**
	 * Defines the set of base layers to be used for this layer.
	 * @param baseLayers
	 *
	 * @throws NullPointerException iff the {@code baseLayers} argument is {@code null}
	 * @throws ModelException in case the base layers have already been set
	 */
	void setBaseLayers(DataSet<ItemLayer> baseLayers);

	//FIXME add boolean flags similar to those on Item interface!!
}
