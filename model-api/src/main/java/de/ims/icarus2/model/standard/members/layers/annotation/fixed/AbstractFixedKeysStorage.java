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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/annotation/fixed/AbstractFixedKeysStorage.java $
 *
 * $LastChangedDate: 2015-07-23 22:36:36 +0200 (Do, 23 Jul 2015) $
 * $LastChangedRevision: 419 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.layers.annotation.AbstractObjectMapStorage;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractFixedKeysStorage.java 419 2015-07-23 20:36:36Z mcgaerty $
 *
 */
public abstract class AbstractFixedKeysStorage<B extends Object> extends AbstractObjectMapStorage<B> {

	private IndexLookup indexLookup;
	private B noEntryValues;

	public AbstractFixedKeysStorage(boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);
	}

	@Override
	public void addNotify(AnnotationLayer layer) {
		super.addNotify(layer);

		indexLookup = createIndexLookup(layer);
		noEntryValues = createNoEntryValues(layer, indexLookup);
	}

	protected abstract B createNoEntryValues(AnnotationLayer layer, IndexLookup indexLookup);

	@Override
	public void removeNotify(AnnotationLayer layer) {
		super.removeNotify(layer);

		indexLookup = null;
		noEntryValues = null;
	}

	protected B getNoEntryValues() {
		return noEntryValues;
	}

	protected IndexLookup createIndexLookup(AnnotationLayer layer) {
		return IndexLookup.defaultCreateIndexLookup(layer);
	}

	public IndexLookup getIndexLookup() {
		return indexLookup;
	}

	protected int checkKeyAndGetIndex(String key) {
		int index = indexLookup.indexOf(key);

		if(index==-1)
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					Messages.mismatchMessage("Unknown key", indexLookup.getAvailableKeysString(), key));

		return index;
	}

	public int getKeyCount() {
		return indexLookup.keyCount();
	}
}
