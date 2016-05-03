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

 * $Revision: 419 $
 * $Date: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/func/AbstractIndexSetProcessor.java $
 *
 * $LastChangedDate: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $LastChangedRevision: 419 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices.func;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractIndexSetProcessor.java 419 2015-07-23 20:36:36Z mcgaerty $
 *
 */
public abstract class AbstractIndexSetProcessor {

	protected final List<IndexSet> buffer = new ArrayList<>();

	protected long estimatedResultSize;

	protected abstract void refreshEstimatedResultSize(IndexSet indexSet);

	public void add(IndexSet...indices) {
		for(IndexSet set : indices) {
			IndexUtils.checkSorted(set);
			buffer.add(set);
			refreshEstimatedResultSize(set);
		}
	}

	public void add(Collection<? extends IndexSet> indices) {
		for(IndexSet set : indices) {
			IndexUtils.checkSorted(set);
			buffer.add(set);
			refreshEstimatedResultSize(set);
		}
	}

	public long getEstimatedResultSize() {
		return estimatedResultSize;
	}

}
