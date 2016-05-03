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

 * $Revision: 412 $
 * $Date: 2015-06-30 16:15:08 +0200 (Di, 30 Jun 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/driver/mapping/AbstractMapping.java $
 *
 * $LastChangedDate: 2015-06-30 16:15:08 +0200 (Di, 30 Jun 2015) $
 * $LastChangedRevision: 412 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.driver.mapping;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import static de.ims.icarus2.model.util.Conditions.checkState;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.util.AbstractBuilder;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractMapping.java 412 2015-06-30 14:15:08Z mcgaerty $
 *
 */
public abstract class AbstractMapping implements Mapping {

	private Driver driver;
	private MappingManifest manifest;
	private ItemLayerManifest sourceLayer;
	private ItemLayerManifest targetLayer;

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
		.append(getClass().getName())
		.append("[id=").append(manifest.getId())
		.append(" sourceLayer=").append(sourceLayer.getId())
		.append(" targetLayer=").append(targetLayer.getId());

		toString(sb);

		return sb.append(']').toString();
	}

	protected void toString(StringBuilder sb) {
		// for subclasses
	}

	/**
	 * @param driver the driver to set
	 */
	void setDriver(Driver driver) {
		this.driver = driver;
	}

	/**
	 * @param manifest the manifest to set
	 */
	void setManifest(MappingManifest manifest) {
		this.manifest = manifest;
	}

	/**
	 * @param sourceLayer the sourceLayer to set
	 */
	void setSourceLayer(ItemLayerManifest sourceLayer) {
		this.sourceLayer = sourceLayer;
	}

	/**
	 * @param targetLayer the targetLayer to set
	 */
	void setTargetLayer(ItemLayerManifest targetLayer) {
		this.targetLayer = targetLayer;
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

		@Override
		protected void validate() {
			checkState("Missing driver", driver!=null);
			checkState("Missing manifest", manifest!=null);
			checkState("Missing source layer", sourceLayer!=null);
			checkState("Missing target layer", targetLayer!=null);
			checkState("Missing value type", valueType!=null);
		}

		protected void applyDefaults(AbstractMapping mapping) {
			mapping.setDriver(driver);
			mapping.setManifest(manifest);
			mapping.setSourceLayer(sourceLayer);
			mapping.setTargetLayer(targetLayer);
		}
	}
}
