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
package de.ims.icarus2.model.standard.members.layers;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractLayer<M extends LayerManifest> extends AbstractPart<LayerGroup> implements Layer {

	private final M manifest;
	private DataSet<ItemLayer> baseLayers = DataSet.emptySet();

//	private final int uid;

	private final Item itemProxy;

	public AbstractLayer(M manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest");  //$NON-NLS-1$

		this.manifest = manifest;

//		uid = manifest.getRegistry().createUID();

		itemProxy = new ProxyItem();
	}

	/**
	 * @param baseLayer the baseLayers to set
	 */
	@Override
	public void setBaseLayers(DataSet<ItemLayer> baseLayers) {
		if (baseLayers == null)
			throw new NullPointerException("Invalid baseLayers"); //$NON-NLS-1$

//		checkNotSet("Base layers", this.baseLayers, baseLayers);

		this.baseLayers = baseLayers;
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContext().getCorpus();
	}

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.LAYER;
	}

	/**
	 * Returns the {@link Manifest#getId() id} of the manifest that describes this layer
	 *
	 * @see de.ims.icarus2.model.api.layer.Layer#getName()
	 */
	@Override
	public String getName() {
		return getManifest().getId();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.Layer#getContext()
	 */
	@Override
	public Context getContext() {
		return getOwner().getContext();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.Layer#getBaseLayer()
	 */
	@Override
	public DataSet<ItemLayer> getBaseLayers() {
		return baseLayers;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.Layer#getManifest()
	 */
	@Override
	public M getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.Layer#getItemProxy()
	 */
	@Override
	public Item getItemProxy() {
		return itemProxy;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.Layer#getLayerGroup()
	 */
	@Override
	public LayerGroup getLayerGroup() {
		return getOwner();
	}

//	/**
//	 * @see de.ims.icarus2.model.api.layer.Layer#getUID()
//	 */
//	@Override
//	public int getUID() {
//		return uid;
//	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Layer@"+getManifest().getId();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class ProxyItem implements Item {

		/**
		 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return AbstractLayer.this.getCorpus();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.ITEM;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getContainer()
		 */
		@Override
		public Container getContainer() {
			return getCorpus().getOverlayContainer();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getLayer()
		 */
		@Override
		public ItemLayer getLayer() {
			return getCorpus().getOverlayLayer();
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getBeginOffset()
		 */
		@Override
		public long getBeginOffset() {
			return NO_INDEX;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return NO_INDEX;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
		 */
		@Override
		public long getIndex() {
			return NO_INDEX;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#setIndex(long)
		 */
		@Override
		public void setIndex(long newIndex) {
			throw new UnsupportedOperationException("Proxy markables cannot have index values assigned"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#isAlive()
		 */
		@Override
		public boolean isAlive() {
			return true;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#isLocked()
		 */
		@Override
		public boolean isLocked() {
			return false;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#isDirty()
		 */
		@Override
		public boolean isDirty() {
			return false;
		}

	}
}
