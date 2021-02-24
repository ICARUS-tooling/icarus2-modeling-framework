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
package de.ims.icarus2.model.util.stream;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Spliterator;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.stream.AbstractFencedSpliterator;

/**
 * @author Markus Gärtner
 *
 */
public class ContainerSpliterator extends AbstractFencedSpliterator<Item> {

	public static ContainerSpliterator spliterator(Container source, long pos, long fence) {
		requireNonNull(source);
		checkArgument(pos>=0L);
		checkArgument(fence>pos && fence<=source.getItemCount());

		return new ContainerSpliterator(source, pos, fence);
	}

	public static ContainerSpliterator spliterator(Container source) {
		requireNonNull(source);

		return new ContainerSpliterator(source, 0L, IcarusUtils.UNSET_LONG);
	}

	private final Container source;

	public ContainerSpliterator(Container source, long pos, long fence) {
		super(pos, fence);

		this.source = source;
	}

	/**
	 * @see de.ims.icarus2.util.stream.AbstractFencedSpliterator#updateFence()
	 */
	@Override
	protected void updateFence() {
		if(fence==IcarusUtils.UNSET_LONG) {
			fence = source.getItemCount();
		}
	}

	/**
	 * @see de.ims.icarus2.util.stream.AbstractFencedSpliterator#current()
	 */
	@Override
	protected Item current() {
		return source.getItemAt(pos);
	}

	/**
	 * @see de.ims.icarus2.util.stream.AbstractFencedSpliterator#split(long, long)
	 */
	@Override
	protected Spliterator<Item> split(long pos, long fence) {
		return new ContainerSpliterator(source, pos, fence);
	}

}
