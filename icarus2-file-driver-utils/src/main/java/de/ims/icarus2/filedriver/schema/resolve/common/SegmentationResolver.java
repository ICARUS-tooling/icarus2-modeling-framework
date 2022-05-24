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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverOptions;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.Options;

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
	public static final String OPTION_AUTO_SEGMENT = "autoSegment";

	private ComponentSupplier componentSupplier;
	private ItemLayer segmentLayer;

	private MappingWriter writer;
	private Container segment;

	private String segmentBegin;
	private String segmentEnd;
	private boolean autoSegment;

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

		componentSupplier = context.getComponentSupplier(segmentLayer);

		segmentBegin = options.getString(OPTION_SEGMENT_BEGIN);
		segmentEnd = options.getString(OPTION_SEGMENT_END);
		autoSegment = options.getBoolean(OPTION_AUTO_SEGMENT);
		//TODO
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(de.ims.icarus2.filedriver.schema.resolve.ResolverContext)
	 */
	@Override
	public Item process(ResolverContext context) throws IcarusApiException {

		Item item = context.currentItem();

		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#close()
	 */
	@Override
	public void close() {
		componentSupplier.close();
		componentSupplier = null;
	}
}
