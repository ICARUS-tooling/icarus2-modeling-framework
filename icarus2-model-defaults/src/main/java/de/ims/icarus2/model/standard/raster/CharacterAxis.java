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
package de.ims.icarus2.model.standard.raster;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.RasterAxis;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(RasterAxis.class)
public class CharacterAxis implements RasterAxis {

	private static final String id = "characterAxis";
	private static final String name = "Character Offset Axis";
	private static final String description = "Orders characters according to their appearance "
			+ "in the hosting character sequence.";

	/**
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	public Optional<String> getId() {
		return Optional.of(id);
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getName()
	 */
	@Override
	public Optional<String> getName() {
		return Optional.of(name);
	}

	/**
	 * @see de.ims.icarus2.util.id.Identity#getDescription()
	 */
	@Override
	public Optional<String> getDescription() {
		return Optional.of(description);
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getMaxValue()
	 */
	@Override
	public long getMaxValue() {
		return Long.MAX_VALUE;
	}

	/**
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getMinValue()
	 */
	@Override
	public long getMinValue() {
		return 0;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getRasterSize(de.ims.icarus2.model.api.members.item.Item, de.ims.icarus2.model.api.layer.FragmentLayer, java.lang.Object)
	 */
	@Override
	public long getRasterSize(Item item, FragmentLayer layer, @Nullable Object value) {
		requireNonNull(item);
		requireNonNull(layer);

		CharSequence s = (CharSequence) value;

		return s==null ? 0 : s.length();
	}

	/**
	 *
	 * @see de.ims.icarus2.model.api.raster.RasterAxis#getGranularity()
	 */
	@Override
	public long getGranularity() {
		return 1L;
	}

}