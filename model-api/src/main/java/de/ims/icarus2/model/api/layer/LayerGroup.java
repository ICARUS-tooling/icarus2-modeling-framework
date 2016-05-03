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

 * $Revision: 392 $
 * $Date: 2015-04-29 12:56:11 +0200 (Mi, 29 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/layer/LayerGroup.java $
 *
 * $LastChangedDate: 2015-04-29 12:56:11 +0200 (Mi, 29 Apr 2015) $
 * $LastChangedRevision: 392 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.layer;

import java.util.Set;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.util.Part;

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
 * @version $Id: LayerGroup.java 392 2015-04-29 10:56:11Z mcgaerty $
 *
 */
public interface LayerGroup extends Part<Context> {

	Context getContext();

	Set<Layer> getLayers();

	LayerGroupManifest getManifest();

	ItemLayer getPrimaryLayer();

	Set<Dependency<LayerGroup>> getDependencies();
}
