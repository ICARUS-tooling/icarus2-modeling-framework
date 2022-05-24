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

import java.util.function.Function;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.resolve.BatchResolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class SegmentationResolver implements BatchResolver {

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.filedriver.Converter.ReadMode, java.util.function.Function, de.ims.icarus2.util.Options)
	 */
	@Override
	public void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches,
			Options options) {
		// TODO Auto-generated method stub
		BatchResolver.super.prepareForReading(converter, mode, caches, options);
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(de.ims.icarus2.filedriver.schema.resolve.ResolverContext)
	 */
	@Override
	public Item process(ResolverContext context) throws IcarusApiException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.BatchResolver#beginBatch(de.ims.icarus2.filedriver.schema.resolve.ResolverContext)
	 */
	@Override
	public void beginBatch(ResolverContext context) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.BatchResolver#endBatch(de.ims.icarus2.filedriver.schema.resolve.ResolverContext)
	 */
	@Override
	public void endBatch(ResolverContext context) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		BatchResolver.super.close();
	}
}
