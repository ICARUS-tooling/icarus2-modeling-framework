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
package de.ims.icarus2.filedriver;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.AbstractConverter.IndexSource;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.RequestSettings;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.OptionalMethod;

/**
 *
 * @author Markus Gärtner
 *
 */
public interface ComponentSupplier extends AutoCloseable, ModelConstants {

	/**
	 * Returns the layer that {@code hostIndex} parameters passed
	 * to {@link #reset(long)} refer to. If this supplier provides
	 * {@link Item#isTopLevel() top-level} items than this method
	 * returns {@code null}.
	 *
	 * @return
	 */
	ItemLayer getHostLayer();

	/**
	 * Returns the layer that items {@link #nextItem() created} by this
	 * supplier belong to.
	 *
	 * @return
	 */
	ItemLayer getComponentLayer();

	/**
	 * Refreshes this supplier so that it returns items suitable
	 * for members of the host container denoted by the given
	 * {@code hostIndex}.
	 * <p>
	 * This is an optional method and implementations are free to
	 * ignore it.
	 *
	 * @param hostIndex
	 * @throws InterruptedException
	 */
	@OptionalMethod
	default void reset(long hostIndex) throws InterruptedException {
		// no-op
	}

	/**
	 * Release internal resources held by this supplier.
	 *
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	void close();

	/**
	 * Returns the number of items currently available from this supplier.
	 * This is used by converter implementations to determine the required size
	 * of buffer objects when reading in data.
	 * <p>
	 * A return value of {@code -1} indicates that the number of index values
	 * is not known or that it is unlimited.
	 *
	 * @return
	 */
	long available();

	/**
	 * Advances to the next index position.
	 *
	 * @return
	 */
	boolean next();

	/**
	 * If necessary creates a new item and then returns it.
	 *
	 * @return
	 */
	Item currentItem();

	/**
	 * Returns the index to be used for the current item.
	 *
	 * @return
	 */
	long currentIndex();

	/**
	 * Returns the id to be used for the current item.
	 *
	 * @return
	 */
	long currentId();


	static abstract class AbstractComponentSupplier implements ComponentSupplier {
		private final ItemLayer hostLayer;
		private final ItemLayer componentLayer;
		private final LongUnaryOperator index2IdMapper;
		private final LayerMemberFactory memberFactory;
		private final MemberType componentType;
		private final ObjLongConsumer<Item> componentConsumer;

		private long id;
		private Item item;
		private Container host;

		/**
		 * Creates a component supplier for nested elements.
		 *
		 * @param hostLayer
		 * @param componentLayer
		 * @param componentIdManager
		 */
		protected AbstractComponentSupplier(Builder builder) {
			checkNotNull(builder);

			hostLayer = builder.getHostLayer();
			componentLayer = builder.getComponentLayer();
			index2IdMapper = builder.getIndex2IdMapper();
			memberFactory = builder.getMemberFactory();
			componentType = builder.getComponentType();
			componentConsumer = builder.getComponentConsumer();
		}

		@Override
		public ItemLayer getHostLayer() {
			return hostLayer;
		}

		@Override
		public ItemLayer getComponentLayer() {
			return componentLayer;
		}

		public LongUnaryOperator getIndex2IdMapper() {
			return index2IdMapper;
		}

		public LayerMemberFactory getMemberFactory() {
			return memberFactory;
		}

		public MemberType getComponentType() {
			return componentType;
		}

		public ObjLongConsumer<? extends Item> getComponentConsumer() {
			return componentConsumer;
		}

		@Override
		public void close() {
			item = null;
			host = null;
			id = NO_INDEX;
		}

		@Override
		public Item currentItem() {
			if(item==null) {
				// Will fail if invalid current index, so no need for additional checks here
				long id = currentId();
				item = newComponent(host, id);

				// Only if we actually got some callback to backend storage delegate item and index
				if(componentConsumer!=null) {
					componentConsumer.accept(item, currentIndex());
				}
			}
			return item;
		}

		@Override
		public long currentId() {
			if(id==NO_INDEX) {
				long index = currentIndex();
				if(index==NO_INDEX)
					throw new ModelException(ModelErrorCode.DRIVER_ERROR, "No more items available");

				// If no mapper for index -> id, just use identity mapping
				id = index2IdMapper==null ? index : index2IdMapper.applyAsLong(index);
			}
			return id;
		}

		@Override
		public boolean next() {
			// Reset "iterator" state
			id = NO_INDEX;
			item = null;

			// Delegate to subclass implementation
			tryAdvance();

			// If we have valid current index, advance was a success
			return currentIndex()!=NO_INDEX;
		}

		protected abstract void tryAdvance();

		/**
		 * Creates a new member of the given {@code host} container
		 * based on the internal {@link MemberType component-type}.
		 * The returned member will be assigned the given {@code id}.
		 *
		 * @param host
		 * @param id
		 * @return
		 */
		protected Item newComponent(Container host, long id) {
			switch (componentType) {
			case CONTAINER:
				return memberFactory.newContainer(componentLayer.getManifest().getRootContainerManifest(), host, id);
			case STRUCTURE:
				return memberFactory.newStructure(((StructureLayer)componentLayer).getManifest().getRootStructureManifest(), host, id);
			case ITEM:
				return memberFactory.newItem(host, id);

			default:
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Current component type not supported: "+componentType);
			}
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class RootComponentSupplier extends AbstractComponentSupplier {

		private long index;
		private boolean consumed;

		public RootComponentSupplier(Builder builder) {
			super(builder);
		}

		@Override
		public void reset(long hostIndex) {
			index = hostIndex;
			consumed = false;
		}

		@Override
		public void close() {
			super.close();
			index = NO_INDEX;
		}

		@Override
		public long available() {
			return index==NO_INDEX ? 0 : 1;
		}

		/**
		 * @see de.ims.icarus2.filedriver.ComponentSupplier.AbstractComponentSupplier#tryAdvance()
		 */
		@Override
		protected void tryAdvance() {
			// no-op
		}

		/**
		 * @see de.ims.icarus2.filedriver.ComponentSupplier#currentIndex()
		 */
		@Override
		public long currentIndex() {
			long result = NO_INDEX;
			if(!consumed) {
				result = index;
				consumed = true;
			}
			return result;
		}

	}

	/**
	 * Implements an {@link IndexSource} that produces a continuous stream of index values
	 * starting from a specified {@code begin index}. If can have an optional {@code length}
	 * defined which acts as a limit for arguments passed to the {@link #indexAt(long)} method.
	 * If no length is defined at construction time this source will produce up to
	 * {@link Long#MAX_VALUE} distinct index values.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class ContinuousComponentSupplier extends AbstractComponentSupplier {

		private final long beginIndex;
		private final long endIndex;

		private long index = NO_INDEX;
		private boolean eos = false;

		public ContinuousComponentSupplier(Builder builder) {
			super(builder);

			beginIndex = builder.getFirstIndex();
			endIndex = builder.getLastIndex();
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#close()
		 */
		@Override
		public void close() {
			super.close();

		}

		@Override
		public long available() {
			return endIndex==NO_INDEX ? NO_INDEX : (endIndex-beginIndex+1);
		}

		@Override
		public long currentIndex() {
			return index;
		}

		@Override
		protected void tryAdvance() {
			if(eos) {
				return;
			}

			index++;

			if(endIndex!=NO_INDEX && index>endIndex) {
				index = NO_INDEX;
				eos = true;
			}
		}
	}

	public static class MappedComponentSupplier extends AbstractComponentSupplier {

		private final Mapping mapping;
		private final boolean useSpanMapping;

		// GENERAL MAPPING SUPPORT
		private final MappingReader mappingReader;
		private final IndexBuffer buffer;

		// SPAN SUPPORT
		private long spanBegin = NO_INDEX;
		private long spanEnd = NO_INDEX;

		private long cursor = NO_INDEX;
		private boolean eos = false;

		/**
		 * @param layerManifest
		 */
		public MappedComponentSupplier(Builder builder) {
			super(builder);

			mapping = builder.getMapping();

			MappingManifest mappingManifest = mapping.getManifest();
			checkArgument("Mapping relation no supported: "+mappingManifest.getRelation(),
					mappingManifest.getRelation()==Relation.ONE_TO_ONE || mappingManifest.getRelation()==Relation.ONE_TO_MANY);

			/*
			 * Important optimization step is to determine whether we can use a span-based
			 * mapping approach.
			 */

			MappingReader mappingReader = null;
			IndexBuffer buffer = null;

			useSpanMapping = (mappingManifest.getRelation()==Relation.ONE_TO_ONE
					|| mappingManifest.getCoverage()==Coverage.TOTAL_MONOTONIC);

			if(!useSpanMapping) {
				mappingReader = mapping.newReader();
				buffer = new IndexBuffer(builder.getBufferSize());
			}

			this.mappingReader = mappingReader;
			this.buffer = buffer;
		}

		public Mapping getMapping() {
			return mapping;
		}

		/**
		 * @throws InterruptedException
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#reset(long)
		 */
		@Override
		public void reset(long hostIndex) throws InterruptedException {
			try {
				mappingReader.begin();

				if(useSpanMapping) {
					spanBegin = mappingReader.getBeginIndex(hostIndex, RequestSettings.emptySettings);
					spanEnd = mappingReader.getEndIndex(hostIndex, RequestSettings.emptySettings);
				} else {
					buffer.clear();
					mappingReader.lookup(hostIndex, buffer, RequestSettings.emptySettings);
				}

				cursor = NO_INDEX;
				eos = false;
			} finally {
				mappingReader.end();
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.IndexSource#close()
		 */
		@Override
		public void close() {
			super.close();

			if(mappingReader!=null) {
				mappingReader.close();
			}
		}

		@Override
		public long available() {
			return useSpanMapping ? (spanEnd-spanBegin+1) : buffer.size();
		}

		@Override
		public long currentIndex() {
			long result = NO_INDEX;

			if(cursor!=NO_INDEX) {
				if(useSpanMapping) {
					result = spanBegin+cursor;
				} else {
					result = buffer.indexAt(IcarusUtils.ensureIntegerValueRange(cursor));
				}
			}

			return result;
		}

		@Override
		protected void tryAdvance() {
			if(eos) {
				return;
			}

			cursor++;

			if(cursor>=available()) {
				cursor = NO_INDEX;
				eos = true;
			}
		}

	}

	static class Builder extends AbstractBuilder<Builder, ComponentSupplier> {

		/**
		 * Abstract transformer to get ids for our index values.
		 * If it remains {@link null} we should use identity mapping
		 * from index values to their id values.
		 */
		private LongUnaryOperator index2IdMapper;

		/**
		 * Size of the buffer to be used for a mapped supplier
		 */
		private Integer bufferSize;

		/**
		 * Type of components to be created (only accept Item, Container or Structure)
		 */
		private MemberType componentType;

		/**
		 * Outsourced creation process of actually instantiating the items (optional)
		 */
		private LayerMemberFactory memberFactory;

		/**
		 * Layer of the host container
		 */
		private ItemLayer hostLayer;

		/**
		 * Layer of components returned by the supplier
		 */
		private ItemLayer componentLayer;

		/**
		 * For span-based index sources this defines the lower bound
		 * of index values to be returned.
		 */
		private Long firstIndex;

		/**
		 * For span-based index sources this defines the upper bound
		 * of index values to be returned.
		 */
		private Long lastIndex;

		/**
		 * Stream-like source of index values.
		 */
		private LongSupplier indexSupplier;

		/**
		 * Mapping to be used for translation of a host index into
		 * a set of mapped index values for components.
		 */
		private Mapping mapping;

		/**
		 * Backend consumer that new components will be sent to.
		 */
		private ObjLongConsumer<Item> componentConsumer;

		public int getBufferSize() {
			return bufferSize==null ? 0 : bufferSize.intValue();
		}

		public Builder bufferSize(int bufferSize) {
			checkArgument(bufferSize>0);
			checkState(this.bufferSize==null);

			this.bufferSize = Integer.valueOf(bufferSize);

			return thisAsCast();
		}

		public long getFirstIndex() {
			return firstIndex==null ? NO_INDEX : firstIndex.longValue();
		}

		public Builder firstIndex(long firstIndex) {
			checkArgument(firstIndex>=0L);
			checkState(this.firstIndex==null);

			this.firstIndex = Long.valueOf(firstIndex);

			return thisAsCast();
		}

		public long getLastIndex() {
			return lastIndex==null ? NO_INDEX : lastIndex.longValue();
		}

		public Builder lastIndex(long lastIndex) {
			checkArgument(lastIndex>=0L);
			checkState(this.lastIndex==null);

			this.lastIndex = Long.valueOf(lastIndex);

			return thisAsCast();
		}

		public MemberType getComponentType() {
			return componentType;
		}

		public Builder componentType(MemberType componentType) {
			checkNotNull(componentType);
			checkState(this.componentType==null);

			this.componentType = componentType;

			return thisAsCast();
		}

		public LayerMemberFactory getMemberFactory() {
			return memberFactory;
		}

		public Builder memberFactory(LayerMemberFactory memberFactory) {
			checkNotNull(memberFactory);
			checkState(this.memberFactory==null);

			this.memberFactory = memberFactory;

			return thisAsCast();
		}

		public ItemLayer getHostLayer() {
			return hostLayer;
		}

		public Builder hostLayer(ItemLayer hostLayer) {
			checkNotNull(hostLayer);
			checkState(this.hostLayer==null);

			this.hostLayer = hostLayer;

			return thisAsCast();
		}

		public ItemLayer getComponentLayer() {
			return componentLayer;
		}

		public Builder componentLayer(ItemLayer componentLayer) {
			checkNotNull(componentLayer);
			checkState(this.componentLayer==null);

			this.componentLayer = componentLayer;

			return thisAsCast();
		}

		public LongSupplier getIndexSupplier() {
			return indexSupplier;
		}

		public Builder indexSupplier(LongSupplier indexSupplier) {
			checkNotNull(indexSupplier);
			checkState(this.indexSupplier==null);

			this.indexSupplier = indexSupplier;

			return thisAsCast();
		}

		public LongUnaryOperator getIndex2IdMapper() {
			return index2IdMapper;
		}

		public Builder index2IdMapper(LongUnaryOperator index2IdMapper) {
			checkNotNull(index2IdMapper);
			checkState(this.index2IdMapper==null);

			this.index2IdMapper = index2IdMapper;

			return thisAsCast();
		}

		public Mapping getMapping() {
			return mapping;
		}

		public Builder mapping(Mapping mapping) {
			checkNotNull(mapping);
			checkState(this.mapping==null);

			this.mapping = mapping;

			return thisAsCast();
		}

		public ObjLongConsumer<Item> getComponentConsumer() {
			return componentConsumer;
		}

		public Builder componentConsumer(ObjLongConsumer<Item> componentConsumer) {
			checkNotNull(componentConsumer);
			checkState(this.componentConsumer==null);

			this.componentConsumer = componentConsumer;

			return thisAsCast();
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			super.validate();

			checkState("Missing component layer", componentLayer!=null);
			checkState("Missing member factory", memberFactory!=null);
			if(mapping!=null) {
				checkState("Missing host layer (required since mapping was defined)", hostLayer!=null);
			}
			if(lastIndex!=null) {
				checkState("Lower bound 'firstIndex' required when upper bound 'lastIndex' is defined ", firstIndex!=null);
			}
			checkState("Must specify at least one method for obtaining index values (mapping, stream or span)",
					(mapping!=null || indexSupplier!=null || firstIndex!=null) || hostLayer==null);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected ComponentSupplier create() {
			if(mapping!=null) {
				return new MappedComponentSupplier(this);
			} else if(firstIndex!=null) {
				return new ContinuousComponentSupplier(this);
			} else if(indexSupplier!=null) {
				throw new ModelException(GlobalErrorCode.NOT_IMPLEMENTED, "TODO");
			} else if(hostLayer==null) {
				return new RootComponentSupplier(this);
			}

			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Insufficient content in builder to construct component supplier");
		}

	}
}