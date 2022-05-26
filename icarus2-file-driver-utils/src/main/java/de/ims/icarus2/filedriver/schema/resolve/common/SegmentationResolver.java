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
/**
 *
 */
package de.ims.icarus2.filedriver.schema.resolve.common;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.ObjLongConsumer;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverOptions;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.indices.standard.MutableSingletonIndexSet;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.strings.StringUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Creates a concurrent segmentation based on annotations for the
 * items of the shared foundation layer.
 *
 * @author Markus Gärtner
 *
 */
public class SegmentationResolver implements Resolver {

	public static final String OPTION_SEGMENT_BEGIN = "segmentBegin";
	public static final String OPTION_SEGMENT_END = "segmentEnd";
	public static final String OPTION_SEGMENT_SINGLETON = "segmentSingleton";
	public static final String OPTION_AUTO_SEGMENT = "autoSegment";

	private ComponentSupplier componentSupplier;
	private ItemLayer segmentLayer;
	private Driver driver;

	/** Layer manifest for probing */
	private ItemLayerManifestBase<?> expectedLayerManifest;

	private MappingWriter writer, reverseWriter;

	private ObjLongConsumer<Item> segmentSaveAction;
	private final IndexBuffer targetIndices;
	private final MutableSingletonIndexSet sourceIndex;
	private final List<Item> items;

	private String segmentBegin;
	private String segmentEnd;
	private String segmentSingleton;
	private boolean autoSegment;

	private Strategy strategy;
	private boolean probing;
	private boolean segmentActive = false;


	public SegmentationResolver() {
		targetIndices = new IndexBuffer(1024 * 32); //TODO better size estimation?
		sourceIndex = new MutableSingletonIndexSet();
		items = new ObjectArrayList<>();
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.filedriver.Converter.ReadMode, java.util.function.Function, de.ims.icarus2.util.Options)
	 */
	@Override
	public void prepareForReading(Converter converter, ReadMode mode, ResolverContext context,
			Options options) {
		segmentLayer = (StructureLayer) options.get(ResolverOptions.LAYER);
		if(segmentLayer==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					"No layer assigned to this resolver "+getClass());

		driver = converter.getDriver();
		componentSupplier = context.getComponentSupplier(segmentLayer);

		segmentBegin = options.getString(OPTION_SEGMENT_BEGIN);
		segmentEnd = options.getString(OPTION_SEGMENT_END);
		segmentSingleton = options.getString(OPTION_SEGMENT_SINGLETON);
		autoSegment = options.getBoolean(OPTION_AUTO_SEGMENT);

		if(autoSegment) {
			if(segmentBegin!=null)
				throw new ModelException(converter.getDriver().getCorpus(), ModelErrorCode.DRIVER_ERROR,
						String.format("%s does not support '%s' when '%s' is set to 'true'", getClass().getName(),
								OPTION_SEGMENT_BEGIN, OPTION_AUTO_SEGMENT));
			if(segmentEnd!=null)
				throw new ModelException(converter.getDriver().getCorpus(), ModelErrorCode.DRIVER_ERROR,
						String.format("%s does not support '%s' when '%s' is set to 'true'", getClass().getName(),
								OPTION_SEGMENT_END, OPTION_AUTO_SEGMENT));
			if(segmentEnd!=null)
				throw new ModelException(converter.getDriver().getCorpus(), ModelErrorCode.DRIVER_ERROR,
						String.format("%s does not support '%s' when '%s' is set to 'true'", getClass().getName(),
								OPTION_SEGMENT_SINGLETON, OPTION_AUTO_SEGMENT));

			strategy = new Alternating();
		} else if(segmentBegin!=null && segmentEnd!=null) {
			strategy = new Discontinuous();
		} else if(segmentBegin!=null) {
			strategy = new Beginning();
		} else if(segmentEnd!=null) {
			strategy = new Ending();
		} else
			throw new ModelException(converter.getDriver().getCorpus(), ModelErrorCode.DRIVER_ERROR,
					String.format("%s requires either '%s' roperty to be set to 'true' or either one of"
							+ " '%s' or '%s' to be provided with a non-empty string", getClass().getName(),
							OPTION_AUTO_SEGMENT, OPTION_SEGMENT_BEGIN, OPTION_SEGMENT_END));

		// Link with back-end storage
		InputCache cache = context.getCache(segmentLayer);
		segmentSaveAction = cache::offer;

		probing = true;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(de.ims.icarus2.filedriver.schema.resolve.ResolverContext)
	 */
	@Override
	public Item process(ResolverContext context) throws IcarusApiException {

		Item item = context.currentItem();

		if(probing) {
			fetchMappingWriters(item);
			probing = false;
		}

		CharSequence currentValue = context.rawData();

		strategy.process(item, currentValue);

		return null;
	}

	private void fetchMappingWriters(Item item) {
		ItemLayer targetLayer = item.getLayer();

		if(targetLayer.getManifest()!=expectedLayerManifest)
			throw new ModelException(ModelErrorCode.DRIVER_ERROR, Messages.mismatch(
					"",
					getName(expectedLayerManifest), getName(targetLayer.getManifest())));

		Mapping mapping = driver.getMapping(segmentLayer, targetLayer);
		if(mapping instanceof WritableMapping) {
			writer = ((WritableMapping) mapping).newWriter();
			writer.begin();
		}

		Mapping reverseMapping = driver.getMapping(targetLayer, segmentLayer);
		if(reverseMapping instanceof WritableMapping) {
			reverseWriter = ((WritableMapping) reverseMapping).newWriter();
			reverseWriter.begin();
		}

	}

	private void beginSegment(Item item) {
		requireNonNull(item);
		assert !segmentActive() : "segment already active";

		// We only mark segment as active and store item temporarilly
		segmentActive = true;
		items.add(item);
	}

	private void endSegmentIfActive() {
		if(segmentActive()) {
			endSegment(null);
		}
	}

	private void endSegment(@Nullable Item item) {
		assert segmentActive() : "no segment active";

		if(item!=null) {
			items.add(item);
		}

		int size = items.size();
		assert size>0 : "no items stored for segment";

		// Now finally create segment, fill it, map it and commit it
		if(!componentSupplier.next())
			throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Failed to produce container for segmentation");
		Container segment = (Container) componentSupplier.currentItem();
		sourceIndex.setIndex(segment.getIndex());

		for (int i = 0; i < size; i++) {
			Item element = items.get(i);
			segment.addItem(element);
			targetIndices.add(element.getIndex());
		}

		if(writer!=null) {
			writer.map(sourceIndex, targetIndices);
		}
		if(reverseWriter!=null) {
			reverseWriter.map(targetIndices, sourceIndex);
		}

		targetIndices.clear();
		items.clear();

		segmentSaveAction.accept(segment, sourceIndex.getIndex());

		segmentActive = false;
	}

	private void intermediateElement(Item item) {
		assert segmentActive() : "no segment active";
		items.add(item);
	}

	private boolean segmentActive() {
		return segmentActive;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#close()
	 */
	@Override
	public void close() {
		componentSupplier.close();
		componentSupplier = null;
		strategy = null;

		if(reverseWriter!=null) {
			reverseWriter.close();
			reverseWriter = null;
		}
		if(writer!=null) {
			writer.close();
			writer = null;
		}
	}

	@Override
	public void complete() {
		strategy.complete();
		if(reverseWriter!=null) {
			reverseWriter.end();
		}
		if(writer!=null) {
			writer.end();
		}
	}

	interface Strategy {
		void process(Item item, CharSequence value);

		default void complete() { /* no-op */ }
	}

	/** Treat any change in annotations as boundary for segments */
	class Alternating implements Strategy {
		private String previousValue;

		@Override
		public void process(Item item, CharSequence value) {
			assert value!=null;
			// Compare current with previous value and on every change start a new segment
			if(previousValue==null) {
				beginSegment(item);
				previousValue = value.toString();
			} else if(!StringUtil.equals(previousValue, value)) {
				endSegment(null); // terminate segment with last item before current
				beginSegment(item);
				previousValue = value.toString();
			} else {
				// No reason to change stored value
				intermediateElement(item);
			}
		}

		@Override
		public void complete() {
			endSegmentIfActive();
		}
	}

	/** Use designated begin and end markers. Might leave items not belonging to any segment */
	class Discontinuous implements Strategy {
		@Override
		public void process(Item item, CharSequence value) {
			if(segmentSingleton!=null && StringUtil.equals(segmentSingleton, value)) {
				assert !segmentActive();
				beginSegment(item);
				endSegment(null);
			} else if(StringUtil.equals(segmentBegin, value)) {
				assert !segmentActive();
				beginSegment(item);
			} else if(StringUtil.equals(segmentEnd, value)) {
				assert segmentActive();
				endSegment(item);
			} else if(segmentActive()) {
				intermediateElement(item);
			}
		}
	}

	/** Use only a start marker. A new marker implicitly ends the previous segment. */
	class Beginning implements Strategy {
		@Override
		public void process(Item item, CharSequence value) {
			if(segmentSingleton!=null && StringUtil.equals(segmentSingleton, value)) {
				assert !segmentActive();
				beginSegment(item);
				endSegment(null);
			} else if(StringUtil.equals(segmentBegin, value)) {
				assert !segmentActive();
				beginSegment(item);
			} else if(segmentActive()) {
				intermediateElement(item);
			}
		}

		@Override
		public void complete() {
			endSegmentIfActive();
		}
	}

	/** Use only an end marker. Any "dangling" item implicitly starts a new segment. */
	class Ending implements Strategy {
		@Override
		public void process(Item item, CharSequence value) {
			if(segmentSingleton!=null && StringUtil.equals(segmentSingleton, value)) {
				assert !segmentActive();
				beginSegment(item);
				endSegment(null);
			} else if(StringUtil.equals(segmentEnd, value)) {
				assert !segmentActive();
				beginSegment(item);
			} else if(segmentActive()) {
				intermediateElement(item);
			} else {
				assert !segmentActive();
				beginSegment(item);
			}
		}

		@Override
		public void complete() {
			endSegmentIfActive();
		}
	}
}
