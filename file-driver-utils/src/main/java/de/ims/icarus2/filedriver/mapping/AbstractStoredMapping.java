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
 */
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.io.IOException;

import de.ims.icarus2.filedriver.io.BufferedIOResource;
import de.ims.icarus2.filedriver.mapping.AbstractMapping.MappingBuilder;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest;

/**
 * Abstract base class for {@code Mapping} implementations that store mapping data.
 *
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractStoredMapping extends BufferedIOResource implements WritableMapping {

	public static final int DEFAULT_CACHE_SIZE = 100;

	private Driver driver;
	private MappingManifest manifest;
	private ItemLayerManifest sourceLayer;
	private ItemLayerManifest targetLayer;

	protected AbstractStoredMapping(IOResource resource, BlockCache cache, int cacheSize) {
		super(resource, cache, cacheSize);
	}

	protected AbstractStoredMapping(StoredMappingBuilder<?,?> builder) {
		super(builder.getResource(), builder.getBlockCache(), builder.getCacheSize());
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append(" id=").append(manifest.getId())
		.append(" sourceLayer=").append(sourceLayer.getId())
		.append(" targetLayer=").append(targetLayer.getId());
	}

	protected static void checkInterrupted() throws InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException();
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
	 * Returns an accessor for write operations on this index.
	 *
	 * @return
	 */
	@Override
	public abstract MappingWriter newWriter();

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
	 * Allows subclasses to perform compression or other means of
	 * storage optimization. This method should only be called once
	 * an index has been completely filled with mapping data!
	 * <p>
	 * It is advised for subclasses that override this method that
	 * they implement the optimization step in a such a way that
	 * aborting the process does not invalidate the current data
	 * stored in the mapping.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void optimize() throws IOException, InterruptedException {
		// for subclasses
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <B>
	 * @param <M>
	 */
	public static abstract class StoredMappingBuilder<B extends MappingBuilder<B, M>, M extends Mapping> extends MappingBuilder<B, M> {
		private Integer cacheSize;
		private IOResource resource;
		private BlockCache blockCache;

		public B cacheSize(int cacheSize) {
			checkArgument(cacheSize>0);
			checkState(this.cacheSize==null);

			this.cacheSize = Integer.valueOf(cacheSize);

			return thisAsCast();
		}

		public B resource(IOResource resource) {
			checkNotNull(resource);
			checkState(this.resource==null);

			this.resource = resource;

			return thisAsCast();
		}

		public B blockCache(BlockCache blockCache) {
			checkNotNull(blockCache);
			checkState(this.blockCache==null);

			this.blockCache = blockCache;

			return thisAsCast();
		}

		public int getCacheSize() {
			return cacheSize==null ? DEFAULT_CACHE_SIZE : cacheSize.intValue();
		}

		public IOResource getResource() {
			return resource;
		}

		public BlockCache getBlockCache() {
			return blockCache;
		}

		protected void applyDefaults(AbstractStoredMapping mapping) {
			mapping.setDriver(getDriver());
			mapping.setManifest(getManifest());
			mapping.setSourceLayer(getSourceLayer());
			mapping.setTargetLayer(getTargetLayer());
		}

		@Override
		protected void validate() {
			super.validate();
			checkState("Missing resource", resource!=null);
			checkState("Missing block cache", blockCache!=null);
		}
	}
}
