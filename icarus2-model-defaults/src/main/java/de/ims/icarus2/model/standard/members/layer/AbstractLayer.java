/**
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
package de.ims.icarus2.model.standard.members.layer;

import static java.util.Objects.requireNonNull;

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
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractLayer<M extends LayerManifest<?>>
		extends AbstractPart<LayerGroup> implements Layer {

	private DataSet<ItemLayer> baseLayers = DataSet.emptySet();

	private final M manifest;
	private final Item itemProxy;

	public AbstractLayer(M manifest) {
		requireNonNull(manifest);

		this.manifest = manifest;

		itemProxy = new ProxyItem();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.Layer#getManifest()
	 */
	@Override
	public M getManifest() {
		return manifest;
	}

	/**
	 * @param baseLayer the baseLayers to set
	 */
	@Override
	public void setBaseLayers(DataSet<ItemLayer> baseLayers) {
		requireNonNull(baseLayers);

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
		return getManifest().getUniqueId();
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
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getIndex()
		 */
		@Override
		public long getIndex() {
			return IcarusUtils.UNSET_LONG;
		}

		/**
		 * @see de.ims.icarus2.model.api.members.item.Item#getId()
		 */
		@Override
		public long getId() {
			return IcarusUtils.UNSET_LONG;
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
