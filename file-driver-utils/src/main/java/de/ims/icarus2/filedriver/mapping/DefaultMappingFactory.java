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

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.FileDriverUtils;
import de.ims.icarus2.filedriver.io.BufferedIOResource.BlockCache;
import de.ims.icarus2.filedriver.io.RUBlockCache;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.io.resources.IOResource;
import de.ims.icarus2.model.api.io.resources.VirtualIOResource;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.classes.ClassUtils;

/**
 *
 * <table border="1">
 * <tr><th>Property</th><th>Aliases</th><th>Description</th></tr>
 * <tr><td>unaryFunction</td><td>function, unary</td><td>{@link LongUnaryOperator} for mapping single index values</td></tr>
 * <tr><td>batchFunction</td><td>batch</td><td>{@link UnaryOperator}, parameterized with {@link IndexSet} to map complex collections of index values</td></tr>
 * <tr><td>resource</td><td>storage</td><td>{@link IOResource} implementation to be used for a mapping that relies on storage</td></tr>
 * <tr><td>capacity</td><td>-</td><td>Initial capacity used for an {@link VirtualIOResource} that will be instantiated in case no custom {@link IOResource} is specified</td></tr>
 * <tr><td>valueType</td><td>indexValueType</td><td>{@link IndexValueType} to be used for the mapping</td></tr>
 * <tr><td>cacheSize</td><td>-</td><td>Size of the cache a mapping should use</td></tr>
 * <tr><td>blockCache</td><td>cache</td><td>{@link BlockCache} implementation for a mapping that relies on storage</td></tr>
 * <tr><td>blockPower</td><td>-</td><td>exponent to be used for calculating the number of data points in a single storage block</td></tr>
 * <tr><td>groupPower</td><td>-</td><td>exponent to be used for calculating the number of index values that should be grouped together for inverse mappings like {@link MappingImplSpanManyToOne}</td></tr>
 * <tr><td>inverseMapping</td><td>-</td><td>An existing {@link Mapping} implementation that should be used as inverse mapping</td></tr>
 * </table>
 *
 * @author Markus Gärtner
 *
 */
public class DefaultMappingFactory implements MappingFactory {

	private final Driver driver;

	private Map<MappingManifest, Mapping> instanceLookup = new IdentityHashMap<>();

	public DefaultMappingFactory(Driver driver) {
		requireNonNull(driver);

		this.driver = driver;
	}

	public Driver getDriver() {
		return driver;
	}

	@Override
	public Mapping createMapping(MappingManifest manifest, Options options) {
		requireNonNull(manifest);

		if(options==null) {
			options = Options.emptyOptions;
		}

		Mapping mapping = createFunctionMapping(manifest, options);

		if(mapping==null) {
			switch (manifest.getRelation()) {
			case ONE_TO_ONE:
				mapping = createOneToOneMapping(manifest, options);
				break;

			case ONE_TO_MANY:
				mapping = createOneToManyMapping(manifest, options);
				break;

			case MANY_TO_ONE:
				mapping = createManyToOneMapping(manifest, options);
				break;

			case MANY_TO_MANY:
				mapping = createManyToManyMapping(manifest, options);
				break;

			default:
				throw new IllegalStateException("Invalid mapping relation: "+manifest.getRelation());
			}
		}

		if(mapping==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"Could not create mapping: "+getName(manifest));

		instanceLookup.put(manifest, mapping);

		return mapping;
	}

	protected Mapping lookupInverse(MappingManifest manifest) {
		MappingManifest inverseManifest = manifest.getInverse();
		if(inverseManifest==null)
			throw new ModelException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"Provided manifest does not declare an inverse mapping: "+getName(manifest));

		Mapping mapping = instanceLookup.get(inverseManifest);
		if(mapping==null)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
					"No mapping instance created by this factory for given manifest: "+getName(inverseManifest));

		return mapping;
	}

	@SuppressWarnings("unchecked")
	protected Mapping createFunctionMapping(MappingManifest manifest, Options options) {
		Object unaryFunc = FileDriverUtils.MappingProperty.UNARY_FUNCTION.getValue(options);

		if(!LongUnaryOperator.class.isInstance(unaryFunc)) {
			return null;
		}

		Object batchFunc = FileDriverUtils.MappingProperty.BATCH_FUNCTION.getValue(options);

		if(!UnaryOperator.class.isInstance(batchFunc)) { //TODO maybe use type argument check to make sure the operator can handle IndexSet instances

			// Don't use batch function if it's not compatible
			batchFunc = null;
		}

		MappingImplFunctionOneToOne.Builder builder = new MappingImplFunctionOneToOne.Builder();

		initMappingBuilder(builder, manifest, options);

		builder.unaryFunction((LongUnaryOperator) unaryFunc);
		if(batchFunc!=null) {
			builder.batchFunction((UnaryOperator<IndexSet>) batchFunc);
		}

		return builder.build();
	}

	protected IOResource getResource(Options options) {
		Object resource = FileDriverUtils.MappingProperty.RESOURCE.getValue(options);

		if(!IOResource.class.isInstance(resource)) {
			int capacity = options.getInteger(FileDriverUtils.MappingProperty.CAPACITY.key(), 1024*1024);

			resource = new VirtualIOResource(capacity);
		}

		return (IOResource) resource;
	}

	protected Mapping createOneToOneMapping(MappingManifest manifest, Options options) {
		Coverage coverage = manifest.getCoverage();
		if(coverage.isTotal() && coverage.isMonotonic()) {
			return createIdentityMapping(manifest, options);
		} else {
			MappingImplOneToOne.Builder builder = new MappingImplOneToOne.Builder();

			initStoredMappingBuilder(builder, manifest, options);

			int blockPower = options.getInteger(FileDriverUtils.MappingProperty.BLOCK_POWER.key(), -1);
			if(blockPower!=-1) {
				builder.blockPower(blockPower);
			}

			return builder.build();
		}
	}

	protected MappingImplIdentity createIdentityMapping(MappingManifest manifest, Options options) {
		ContextManifest contextManifest = driver.getManifest().getContextManifest();

		ItemLayerManifest sourceLayer = (ItemLayerManifest) contextManifest.getLayerManifest(manifest.getSourceLayerId());
		ItemLayerManifest targetLayer = (ItemLayerManifest) contextManifest.getLayerManifest(manifest.getTargetLayerId());

		return new MappingImplIdentity(driver, manifest, sourceLayer, targetLayer);
	}

	protected <B extends AbstractVirtualMapping.MappingBuilder<B, ?>> B initMappingBuilder(B builder, MappingManifest manifest, Options options) {
		builder.driver(driver);
		builder.manifest(manifest);

		ContextManifest contextManifest = driver.getManifest().getContextManifest();

		ItemLayerManifest sourceLayer = (ItemLayerManifest) contextManifest.getLayerManifest(manifest.getSourceLayerId());
		ItemLayerManifest targetLayer = (ItemLayerManifest) contextManifest.getLayerManifest(manifest.getTargetLayerId());

		builder.sourceLayer(sourceLayer);
		builder.targetLayer(targetLayer);

		builder.valueType(getValueType(sourceLayer, targetLayer, options));

		return builder;
	}

	protected IndexValueType getValueType(ItemLayerManifest source, ItemLayerManifest target, Options options) {

		// Try direct type declared in options
		Object declaredValueType = FileDriverUtils.MappingProperty.VALUE_TYPE.getValue(options);
		if(IndexValueType.class.isInstance(declaredValueType)) {
			return (IndexValueType) declaredValueType;
		}

		IndexValueType sourceType = IndexValueType.forValue(driver.getItemCount(source));
		IndexValueType targetType = IndexValueType.forValue(driver.getItemCount(target));

		IndexValueType result = null;

		if(sourceType==null) {
			result = targetType;
		} else if(targetType==null) {
			result = sourceType;
		} else {
			result = ClassUtils.min(sourceType, targetType);
		}

		if(result==null) {
			result = IndexValueType.LONG;
		}

		return result;
	}

	protected <B extends AbstractStoredMapping.StoredMappingBuilder<B, ?>> B initStoredMappingBuilder(B builder, MappingManifest manifest, Options options) {
		initMappingBuilder(builder, manifest, options);

		builder.resource(getResource(options));
		builder.blockCache(getBlockCache(options));

		int cacheSize = options.getInteger(FileDriverUtils.MappingProperty.CACHE_SIZE.key(), -1);
		if(cacheSize!=-1) {
			builder.cacheSize(cacheSize);
		}

		return builder;
	}

	protected BlockCache getBlockCache(Options options) {
		Object declaredBlockCache = FileDriverUtils.MappingProperty.BLOCK_CACHE.getValue(options);
		if(BlockCache.class.isInstance(declaredBlockCache)) {
			return (BlockCache) declaredBlockCache;
		}

		return RUBlockCache.newLeastRecentlyUsedCache();
	}

	protected Mapping createOneToManyMapping(MappingManifest manifest, Options options) {
		MappingImplSpanOneToMany.Builder builder = new MappingImplSpanOneToMany.Builder();

		initStoredMappingBuilder(builder, manifest, options);

		int blockPower = options.getInteger(FileDriverUtils.MappingProperty.BLOCK_POWER.key(), -1);
		if(blockPower!=-1) {
			builder.blockPower(blockPower);
		}

		return builder.build();
	}

	protected Mapping createManyToOneMapping(MappingManifest manifest, Options options) {

		Mapping inverseMapping = lookupInverse(manifest);

		Relation inverseRelation = inverseMapping.getManifest().getRelation();
		if(inverseRelation!=Relation.ONE_TO_MANY)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.mismatchMessage("Invalid relation type for inverse mapping", Relation.ONE_TO_MANY, inverseRelation));

		MappingImplSpanManyToOne.Builder builder = new MappingImplSpanManyToOne.Builder();

		initStoredMappingBuilder(builder, manifest, options);

		int blockPower = options.getInteger(FileDriverUtils.MappingProperty.BLOCK_POWER.key(), -1);
		if(blockPower!=-1) {
			builder.blockPower(blockPower);
		}

		int groupPower = options.getInteger(FileDriverUtils.MappingProperty.GROUP_POWER.key(), -1);
		if(groupPower!=-1) {
			builder.groupPower(groupPower);
		}

		builder.inverseMapping(inverseMapping);

		return builder.build();
	}

	protected Mapping createManyToManyMapping(MappingManifest manifest, Options options) {
		throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, "Not yet implemented");
	}

	public Mapping createCompoundMapping(ItemLayerManifest sourceLayer, ItemLayerManifest targetLayer) {
		throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, "Not yet implemented");
	}
}
