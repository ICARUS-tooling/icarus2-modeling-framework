/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.mapping;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.strings.ToStringBuilder;

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractVirtualMapping implements Mapping {

	private final Driver driver;
	private final MappingManifest manifest;
	private final ItemLayerManifestBase<?> sourceLayer;
	private final ItemLayerManifestBase<?> targetLayer;

	protected AbstractVirtualMapping(AbstractMappingBuilder<?, ?> builder) {
		requireNonNull(builder);

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
		ToStringBuilder builder = ToStringBuilder.create(this)
				.add("id", manifest.getId())
				.add("sourceLayer", sourceLayer.getId())
				.add("targetLayer", targetLayer.getId());
		toString(builder);
		return builder.build();
	}

	protected void toString(ToStringBuilder builder) {
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
	public ItemLayerManifestBase<?> getSourceLayer() {
		return sourceLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mapping.Mapping#getTargetLayer()
	 */
	@Override
	public ItemLayerManifestBase<?> getTargetLayer() {
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
	public static abstract class AbstractMappingBuilder<B extends AbstractMappingBuilder<B, M>, M extends Mapping>
			extends AbstractBuilder<B, M> {
		private Driver driver;
		private MappingManifest manifest;
		private ItemLayerManifestBase<?> sourceLayer, targetLayer;
		private IndexValueType valueType;

		protected AbstractMappingBuilder() {
			// no-op
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B driver(Driver driver) {
			requireNonNull(driver);
			checkState(this.driver==null);

			this.driver = driver;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B manifest(MappingManifest manifest) {
			requireNonNull(manifest);
			checkState(this.manifest==null);

			this.manifest = manifest;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B sourceLayer(ItemLayerManifestBase<?> sourceLayer) {
			requireNonNull(sourceLayer);
			checkState(this.sourceLayer==null);

			this.sourceLayer = sourceLayer;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B targetLayer(ItemLayerManifestBase<?> targetLayer) {
			requireNonNull(targetLayer);
			checkState(this.targetLayer==null);

			this.targetLayer = targetLayer;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public B valueType(IndexValueType valueType) {
			requireNonNull(valueType);
			checkState(this.valueType==null);

			this.valueType = valueType;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public Driver getDriver() {
			return driver;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public MappingManifest getManifest() {
			return manifest;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ItemLayerManifestBase<?> getSourceLayer() {
			return sourceLayer;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ItemLayerManifestBase<?> getTargetLayer() {
			return targetLayer;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public IndexValueType getValueType() {
			return valueType;
		}

		protected IndexBlockStorage getBlockStorage() {
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
