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
package de.ims.icarus2.model.util.stream;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Spliterator;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.stream.AbstractFencedSpliterator;

/**
 * @author Markus Gärtner
 *
 */
public class ContainerSpliterator extends AbstractFencedSpliterator<Item> {

	private final Container source;

	public ContainerSpliterator(Container source, long pos, long fence) {
		super(pos, fence);
		requireNonNull(source);
		checkArgument(fence<=source.getItemCount());

		this.source = source;
	}

	public ContainerSpliterator(Container source) {
		super(0L, source.getItemCount());
		this.source = source;
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
