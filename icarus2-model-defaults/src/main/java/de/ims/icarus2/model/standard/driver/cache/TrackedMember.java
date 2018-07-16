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
 *
 */
package de.ims.icarus2.model.standard.driver.cache;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.container.DefaultContainer;
import de.ims.icarus2.model.standard.members.item.DefaultFragment;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.DefaultStructure;

/**
 * Provides simple extensions of the default implementations for {@link Item items},
 * {@link Fragment fragments}, {@link Container containers} and {@link Structure structures}.
 * <p>
 * The base for those extensions is an interface that models a <i>usage counter</> resembling
 * a count of the current working references to the member in question.
 * <p>
 * <s>Since only top level members of a layer can effectively be cached by the framework, this interface
 * is additionally being used to model the ability to directly store the layer reference in the member.</s>
 *
 * @author Markus Gärtner
 *
 */
public interface TrackedMember<L extends ItemLayer> {

	/**
	 * Increments and returns the current use counter of the member
	 *
	 * @return the use counter of this member after it has been incremented
	 */
	int incrementUseCounter();

	/**
	 * Decrements and returns the current use counter of the member
	 *
	 * @return the use counter of this member after it has been decremented
	 */
	int decrementUseCounter();

	/**
	 * Returns the current use counter of the member
	 *
	 * @return the use counter of this member
	 */
	int getUseCounter();

//	/**
//	 * Changes the members's host layer to the given one
//	 * @param layer the new host layer or {@code null} (e.g. in case the member is being
//	 * prepared for recycling)
//	 */
//	void setLayer(L layer);
//
//	/**
//	 * Returns the members host layer
//	 *
//	 * @return
//	 */
//	L getLayer();

	public static class TrackedItem extends DefaultItem implements TrackedMember<ItemLayer> {

		private int useCounter;

//		private ItemLayer layer;

//		@Override
//		public ItemLayer getLayer() {
//			return layer;
//		}

//		@Override
//		public void setLayer(ItemLayer layer) {
//			this.layer = layer;
//		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}

	public static class TrackedFragment extends DefaultFragment implements TrackedMember<FragmentLayer> {

		private int useCounter;

//		private FragmentLayer layer;
//
//		@Override
//		public FragmentLayer getLayer() {
//			return layer;
//		}
//
//		@Override
//		public void setLayer(FragmentLayer layer) {
//			this.layer = layer;
//		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}

	public static class TrackedContainer extends DefaultContainer implements TrackedMember<ItemLayer> {

		private int useCounter;

//		private ItemLayer layer;
//
//		@Override
//		public ItemLayer getLayer() {
//			return layer;
//		}
//
//		@Override
//		public void setLayer(ItemLayer layer) {
//			this.layer = layer;
//		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}

	public static class TrackedStructure extends DefaultStructure implements TrackedMember<StructureLayer> {

		private int useCounter;

//		private StructureLayer layer;
//
//		@Override
//		public StructureLayer getLayer() {
//			return layer;
//		}
//
//		@Override
//		public void setLayer(StructureLayer layer) {
//			this.layer = layer;
//		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}
}