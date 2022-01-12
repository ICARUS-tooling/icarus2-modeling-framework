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
package de.ims.icarus2.model.standard.members.layer.annotation;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.standard.members.layer.AbstractLayer;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(AnnotationLayer.class)
public class DefaultAnnotationLayer extends AbstractLayer<AnnotationLayerManifest> implements AnnotationLayer {

	private AnnotationStorage storage;
	private DataSet<AnnotationLayer> referenceLayers = DataSet.emptySet();

	/**
	 * @param manifest
	 * @param group
	 */
	public DefaultAnnotationLayer(AnnotationLayerManifest manifest) {
		super(manifest);
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer#getAnnotationStorage()
	 */
	@Override
	public @Nullable AnnotationStorage getAnnotationStorage() {
		return storage;
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.layer.AnnotationLayer#setAnnotationStorage(de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage)
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
