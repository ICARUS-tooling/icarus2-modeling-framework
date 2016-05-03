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

 * $Revision: 422 $
 * $Date: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/annotation/DefaultAnnotationLayer.java $
 *
 * $LastChangedDate: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $LastChangedRevision: 422 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.annotation;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.standard.members.layers.AbstractLayer;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultAnnotationLayer.java 422 2015-08-19 13:38:58Z mcgaerty $
 *
 */
public class DefaultAnnotationLayer extends AbstractLayer<AnnotationLayerManifest> implements AnnotationLayer {

	private AnnotationStorage storage;

	/**
	 * @param manifest
	 * @param group
	 */
	public DefaultAnnotationLayer(AnnotationLayerManifest manifest, AnnotationStorage storage) {
		super(manifest);

		setAnnotationStorage(storage);
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer#getAnnotationStorage()
	 */
	@Override
	public AnnotationStorage getAnnotationStorage() {
		return storage;
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer#setAnnotationStorage(de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage)
	 */
	@Override
	public void setAnnotationStorage(AnnotationStorage storage) {
		checkNotNull(storage);

		if(this.storage!=null && this.storage instanceof ManagedAnnotationStorage) {
			((ManagedAnnotationStorage)this.storage).removeNotify(this);
		}

		this.storage = storage;

		if(this.storage instanceof ManagedAnnotationStorage) {
			((ManagedAnnotationStorage)this.storage).addNotify(this);
		}
	}
}
