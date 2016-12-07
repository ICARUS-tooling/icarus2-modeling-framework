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
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.util.AbstractBuilder;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractVirtualMapping implements Mapping {

	private final Driver driver;
	private final MappingManifest manifest;
	private final ItemLayerManifest sourceLayer;
	private final ItemLayerManifest targetLayer;

	protected AbstractVirtualMapping(Driver driver, MappingManifest manifest,
			ItemLayerManifest sourceLayer, ItemLayerManifest targetLayer) {
		checkNotNull(driver);
		checkNotNull(manifest);
		checkNotNull(sourceLayer);
		checkNotNull(targetLayer);

		this.driver = driver;
		this.manifest = manifest;
		this.sourceLayer = sourceLayer;
		this.targetLayer = targetLayer;
	}

	protected AbstractVirtualMapping(MappingBuilder<?, ?> builder) {
		checkNotNull(builder);

		driver = builder.getDriver();
		manifest = builder.getManifest();
		sourceLayer = builder.getSourceLayer();
		targetLayer = builder.getTargetLayer();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
		.append(getClass().getName()).append('[');
		sb.append("id=").append(manifest.getId());
		sb.append(" sourceLayer=").append(sourceLayer.getId());
		sb.append(" targetLayer=").append(targetLayer.getId());

		toString(sb);

		return sb.append(']').toString();
	}

	protected void toString(StringBuilder sb) {
		// for subclasses
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getDriver()
	 */
	@Override
	public Driver getDriver() {
		return driver;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getSourceLayer()
	 */
	@Override
	public ItemLayerManifest getSourceLayer() {
		return sourceLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getTargetLayer()
	 */
	@Override
	public ItemLayerManifest getTargetLayer() {
		return targetLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getManifest()
	 */
	@Override
	public MappingManifest getManifest() {
		return manifest;
	}

	/**
	 * The default implementation does nothing.
	 *
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <B>
	 * @param <M>
	 */
	public static abstract class MappingBuilder<B extends MappingBuilder<B, M>, M extends Mapping> extends AbstractBuilder<B, M> {
		private Driver driver;
		private MappingManifest manifest;
		private ItemLayerManifest sourceLayer, targetLayer;
		private IndexValueType valueType;

		public B driver(Driver driver) {
			checkNotNull(driver);
			checkState(this.driver==null);

			this.driver = driver;

			return thisAsCast();
		}

		public B manifest(MappingManifest manifest) {
			checkNotNull(manifest);
			checkState(this.manifest==null);

			this.manifest = manifest;

			return thisAsCast();
		}

		public B sourceLayer(ItemLayerManifest sourceLayer) {
			checkNotNull(sourceLayer);
			checkState(this.sourceLayer==null);

			this.sourceLayer = sourceLayer;

			return thisAsCast();
		}

		public B targetLayer(ItemLayerManifest targetLayer) {
			checkNotNull(targetLayer);
			checkState(this.targetLayer==null);

			this.targetLayer = targetLayer;

			return thisAsCast();
		}

		public B valueType(IndexValueType valueType) {
			checkNotNull(valueType);
			checkState(this.valueType==null);

			this.valueType = valueType;

			return thisAsCast();
		}

		public Driver getDriver() {
			return driver;
		}

		public MappingManifest getManifest() {
			return manifest;
		}

		public ItemLayerManifest getSourceLayer() {
			return sourceLayer;
		}

		public ItemLayerManifest getTargetLayer() {
			return targetLayer;
		}

		public IndexValueType getValueType() {
			return valueType;
		}

		public IndexBlockStorage getBlockStorage() {
			return IndexBlockStorage.forValueType(getValueType());
		}

		@Override
		protected void validate() {
			checkState("Missing driver", driver!=null);
			checkState("Missing manifest", manifest!=null);
			checkState("Missing target layer", targetLayer!=null);
			checkState("Missing source layer", sourceLayer!=null);
			checkState("Missing value type", valueType!=null);
		}
	}
}
