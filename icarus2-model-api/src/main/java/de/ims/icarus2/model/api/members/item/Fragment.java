/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.members.item;

import javax.annotation.Nullable;

import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.util.ModelUtils;

/**
 * A {@code Fragment} allows for the definition of {@code Item} objects that
 * are not bound by the logical structure of an existing foundation layer.
 * A regular item references parts of other existing items, like pre-tokenized test
 * and/or split objects in a text that forms the actual corpus. With the use of
 * fragments it is possible to handle <i>raw</i> data (text, audio, video, etc...).
 * Note however, that using fragments is much more expensive than using
 * items, since a lookup structure has to be built for each item object that
 * contains fragments, in order to visualize or explore it.
 * <p>
 * As a precondition for the use of fragments, a <i>value</i> annotation-layer has to be
 * present. The annotations in that layer are then {@link Rasterizer rasterized} to create
 * a discrete address space for each raw item. In this discrete space it is then possible
 * to form well defined parts using {@link Position position pointers} as begin and end of
 * actual {@link Fragment} objects.
 *
 * @author Markus Gärtner
 *
 *@see Item
 */
public interface Fragment extends Item {

	@Override
	FragmentLayer getLayer();

	/**
	 * Returns the item this fragment is a part of.
	 *
	 * @return
	 */
	@Nullable Item getItem();

	/**
	 * Returns the position within the surrounding item of
	 * this fragment that denotes the actual begin of the fragment itself.
	 *
	 * @return
	 */
	@Nullable Position getFragmentBegin();

	/**
	 * Returns the position within the surrounding item of
	 * this fragment that denotes the actual end of the fragment itself.
	 *
	 * @return
	 */
	@Nullable Position getFragmentEnd();

	// Modification methods

	/**
	 * Changes the begin position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @throws ModelException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting item
	 *
	 * @see ModelUtils#checkFragmentPositions(Fragment, Position, Position)
	 */
	@Unguarded("Complex environment needed to evaluate position argument")
	void setFragmentBegin(Position position);

	/**
	 * Changes the end position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @throws ModelException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting item
	 *
	 * @see ModelUtils#checkFragmentPositions(Fragment, Position, Position)
	 */
	@Unguarded("Complex environment needed to evaluate position argument")
	void setFragmentEnd(Position position);
}
