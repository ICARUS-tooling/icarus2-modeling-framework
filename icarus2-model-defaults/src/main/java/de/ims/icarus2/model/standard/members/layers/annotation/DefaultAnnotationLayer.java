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
package de.ims.icarus2.model.standard.members.layers.annotation;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.standard.members.layers.AbstractLayer;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultAnnotationLayer extends AbstractLayer<AnnotationLayerManifest> implements AnnotationLayer {

	private AnnotationStorage storage;
	private DataSet<AnnotationLayer> referenceLayers = DataSet.emptySet();

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
		requireNonNull(storage);

		if(this.storage!=null && this.storage instanceof ManagedAnnotationStorage) {
			((ManagedAnnotationStorage)this.storage).removeNotify(this);
		}

		this.storage = storage;

		if(this.storage instanceof ManagedAnnotationStorage) {
			((ManagedAnnotationStorage)this.storage).addNotify(this);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer#getReferenceLayers()
	 */
	@Override
	public DataSet<AnnotationLayer> getReferenceLayers() {
		return referenceLayers;
	}

	/**
	 * @param referenceLayers the referenceLayers to set
	 */
	@Override
	public void setReferenceLayers(DataSet<AnnotationLayer> referenceLayers) {
		requireNonNull(referenceLayers);

		this.referenceLayers = referenceLayers;
	}
}
