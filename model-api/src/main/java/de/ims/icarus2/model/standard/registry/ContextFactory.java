/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 451 $
 * $Date: 2016-02-03 12:33:06 +0100 (Mi, 03 Feb 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/registry/ContextFactory.java $
 *
 * $LastChangedDate: 2016-02-03 12:33:06 +0100 (Mi, 03 Feb 2016) $
 * $LastChangedRevision: 451 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.registry;

import static de.ims.icarus2.model.standard.util.CorpusUtils.getName;
import static de.ims.icarus2.model.util.Conditions.checkNotNull;
import static de.ims.icarus2.model.util.Conditions.checkState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.Dependency;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.registry.CorpusMemberFactory;
import de.ims.icarus2.model.standard.corpus.DefaultContext;
import de.ims.icarus2.model.standard.members.layers.DefaultLayerGroup;
import de.ims.icarus2.model.standard.sets.DataSets;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: ContextFactory.java 451 2016-02-03 11:33:06Z mcgaerty $
 *
 */
public class ContextFactory {

	private Producers producers = new Producers();

	public synchronized ContextFactory implementationLoaderSupplier(Supplier<ImplementationLoader<?>> implementationLoaderSupplier) {
		checkNotNull(implementationLoaderSupplier);
		checkState(producers.implementationLoaderSupplier==null);

		producers.implementationLoaderSupplier = implementationLoaderSupplier;

		return this;
	}

	public synchronized ContextFactory setMemberFactory(CorpusMemberFactory memberFactory) {
		checkNotNull(memberFactory);
		checkState(producers.memberFactory==null);

		producers.memberFactory = memberFactory;

		return this;
	}

	public synchronized ContextFactory annotationLayerProducer(
			Function<AnnotationLayerManifest, AnnotationLayer> annotationLayerProducer) {
		checkNotNull(annotationLayerProducer);
		checkState(producers.annotationLayerProducer==null);

		producers.annotationLayerProducer = annotationLayerProducer;

		return this;
	}

	public synchronized ContextFactory itemLayerProducer(
			Function<ItemLayerManifest, ItemLayer> itemLayerProducer) {
		checkNotNull(itemLayerProducer);
		checkState(producers.itemLayerProducer==null);

		producers.itemLayerProducer = itemLayerProducer;

		return this;
	}

	public synchronized ContextFactory structureLayerProducer(
			Function<StructureLayerManifest, StructureLayer> structureLayerProducer) {
		checkNotNull(structureLayerProducer);
		checkState(producers.structureLayerProducer==null);

		producers.structureLayerProducer = structureLayerProducer;

		return this;
	}

	public synchronized ContextFactory fragmentLayerProducer(
			Function<FragmentLayerManifest, FragmentLayer> fragmentLayerProducer) {
		checkNotNull(fragmentLayerProducer);
		checkState(producers.fragmentLayerProducer==null);

		producers.fragmentLayerProducer = fragmentLayerProducer;

		return this;
	}

	public synchronized ContextFactory layerGroupProducer(
			Function<LayerGroupManifest, LayerGroup> layerGroupProducer) {
		checkNotNull(layerGroupProducer);
		checkState(producers.layerGroupProducer==null);

		producers.layerGroupProducer = layerGroupProducer;

		return this;
	}

	protected synchronized Producers getProducers() {
		return producers.clone();
	}

	/**
	 * Critical assumptions: layer groups created must be of assignment compatible to
	 * {@link DefaultLayerGroup} and the context instance must be compatible to
	 * {@link DefaultContext}!
	 *
	 * @param corpus
	 * @param manifest
	 * @param options
	 * @return
	 */
	public Context createContext(Corpus corpus, ContextManifest manifest, Options options) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus");  //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(options==null) {
			options = Options.emptyOptions;
		}

		// Fetch snapshot of producers and make sure we can create every possible member!
		Producers producers = getProducers();
		producers.ensureFactories(corpus);

		DefaultContext context = (DefaultContext) newContext(corpus, manifest, producers, options);
		List<LayerLinker> linkers = new ArrayList<>(manifest.getLayerManifests().size());

		// First pass, create layers
		for(LayerGroupManifest groupManifest : manifest.getGroupManifests()) {

			// Create group
			DefaultLayerGroup group = (DefaultLayerGroup) newLayerGroup(groupManifest, producers, options);

			context.addLayerGroup(group);

			// Create layers
			for(LayerManifest layerManifest : groupManifest.getLayerManifests()) {
				LayerLinker linker = newLayer(corpus, layerManifest, producers, options);

				group.addLayer(linker.getLayer());
				context.addLayer(linker.getLayer());

				linkers.add(linker);
			}

			// Finally set primary layer (can only be a native layer!!!)
			ItemLayerManifest primaryManifest = groupManifest.getPrimaryLayerManifest();
			if(primaryManifest!=null) {
				group.setPrimaryLayer((ItemLayer) context.getNativeLayer(primaryManifest.getId()));
			}
		}

		// Intermediate linking

		// Set context wide primary layer (can only be a native layer!!!)
		ItemLayerManifest primaryManifest = manifest.getPrimaryLayerManifest();
		if(primaryManifest!=null) {
			context.setPrimaryLayer((ItemLayer) context.getNativeLayer(primaryManifest.getId()));
		}

		// Set context wide foundation layer (this one might be a foreign one)
		ItemLayerManifest foundationManifest = manifest.getFoundationLayerManifest();
		if(foundationManifest!=null) {
			context.setFoundationLayer((ItemLayer) context.getLayer(foundationManifest.getId()));
		}

		// Second pass, link dependencies and attach groups
		// This task is delegated to the linker implementations
		for(LayerLinker linker : linkers) {
			linker.link();
		}

		return context;
	}

	protected Context newContext(Corpus corpus, ContextManifest manifest, Producers producers, Options options) {
		return new DefaultContext(corpus, manifest);
	}

	protected LayerGroup newLayerGroup(LayerGroupManifest groupManifest, Producers producers, Options options) {
		LayerGroup group = null;
		Object declaredGroup = options.get(groupManifest.getId());
		if(declaredGroup instanceof DefaultLayerGroup) {
			group = (DefaultLayerGroup) declaredGroup;
		} else if(producers.layerGroupProducer!=null) {
			group = producers.layerGroupProducer.apply(groupManifest);
		} else {
			group = producers.memberFactory.createLayerGroup(groupManifest, options);
		}

		return group;
	}

	//*********************************************
	//			LAYER CREATION
	//*********************************************

	/**
	 * Instantiates a layer according to the given {@link LayerManifest} and adds it to
	 * the supplied layer group.
	 *
	 * @param manifest
	 * @return
	 */
	protected LayerLinker newLayer(Corpus corpus, LayerManifest manifest, Producers producers, Options options) {

		switch (manifest.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST:
			return newAnnotationLayer(corpus, (AnnotationLayerManifest) manifest, producers, options);

		case ITEM_LAYER_MANIFEST:
			return newItemLayer(corpus, (ItemLayerManifest) manifest, producers, options);

		case STRUCTURE_LAYER_MANIFEST:
			return newStructureLayer(corpus, (StructureLayerManifest) manifest, producers, options);

		case FRAGMENT_LAYER_MANIFEST:
			return newFragmentLayer(corpus, (FragmentLayerManifest) manifest, producers, options);

		default:
			throw new IllegalArgumentException("Unsupported manifest type for layer: "+manifest.getManifestType()); //$NON-NLS-1$
		}
	}

	protected LayerLinker newAnnotationLayer(Corpus corpus, AnnotationLayerManifest manifest, Producers producers, Options options) {
		AnnotationLayer layer = null;

		Object declaredLayer = options.get(manifest.getId());

		if(declaredLayer instanceof AnnotationLayer) {
			layer = (AnnotationLayer) declaredLayer;
		} else if(producers.annotationLayerProducer!=null) {
			layer = producers.annotationLayerProducer.apply(manifest);
		} else {
			layer = producers.memberFactory.createAnnotationLayer(corpus, manifest, options);
		}

		return new LayerLinker(corpus, layer, producers);
	}

	protected LayerLinker newItemLayer(Corpus corpus, ItemLayerManifest manifest, Producers producers, Options options) {
		ItemLayer layer = null;

		Object declaredLayer = options.get(manifest.getId());

		if(declaredLayer instanceof ItemLayer) {
			layer = (ItemLayer) declaredLayer;
		} else if(producers.itemLayerProducer!=null) {
			layer = producers.itemLayerProducer.apply(manifest);
		} else {
			layer = producers.memberFactory.createItemLayer(corpus, manifest, options);
		}

		return new ItemLayerLinker(corpus, layer, producers);
	}

	protected LayerLinker newStructureLayer(Corpus corpus, StructureLayerManifest manifest, Producers producers, Options options) {
		StructureLayer layer = null;

		Object declaredLayer = options.get(manifest.getId());

		if(declaredLayer instanceof StructureLayer) {
			layer = (StructureLayer) declaredLayer;
		} else if(producers.itemLayerProducer!=null) {
			layer = producers.structureLayerProducer.apply(manifest);
		} else {
			layer = producers.memberFactory.createStructureLayer(corpus, manifest, options);
		}

		return new ItemLayerLinker(corpus, layer, producers);
	}

	protected LayerLinker newFragmentLayer(Corpus corpus, FragmentLayerManifest manifest, Producers producers, Options options) {
		FragmentLayer layer = null;

		Object declaredLayer = options.get(manifest.getId());

		if(declaredLayer instanceof FragmentLayer) {
			layer = (FragmentLayer) declaredLayer;
		} else if(producers.itemLayerProducer!=null) {
			layer = producers.fragmentLayerProducer.apply(manifest);
		} else {
			layer = producers.memberFactory.createFragmentLayer(corpus, manifest, options);
		}

		return new FragmentLayerLinker(corpus, layer, producers);
	}

	public static class LayerLinker {

		private final Layer layer;
		private final Corpus corpus;
		private final Producers producers;

		public LayerLinker(Corpus corpus, Layer layer, Producers producers) {
			checkNotNull(corpus);
			checkNotNull(layer);
			checkNotNull(producers);

			this.layer = layer;
			this.corpus = corpus;
			this.producers = producers;
		}

		public final Corpus getCorpus() {
			return corpus;
		}

		public final Layer getLayer() {
			return layer;
		}

		public final Producers getProducers() {
			return producers;
		}

		/**
		 * Callback to perform linking operations as soon as all layers are instantiated.
		 * The default implementation adds all necessary base layers. In addition it assumes that layer groups used for
		 * supplied layers (not those of foreign contexts!) will be of type {@link DefaultLayerGroup}.
		 */
		public void link() {
			LayerManifest layerManifest = layer.getManifest();

			// Link base layers
			List<TargetLayerManifest> baseManifests = layerManifest.getBaseLayerManifests();
			if(!baseManifests.isEmpty()) {

				List<ItemLayer> buffer = new ArrayList<>(baseManifests.size());

				for(TargetLayerManifest target : baseManifests) {

					ItemLayer targetLayer = resolveTargetLayer(target);

					// Add layer to base set
					buffer.add(targetLayer);

					// If foreign layer, add inter-group dependency
					DefaultLayerGroup group = (DefaultLayerGroup) layer.getLayerGroup();
					if(targetLayer.getLayerGroup()!=group) {
						group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.STRONG));
					}
				}


				layer.setBaseLayers(DataSets.createDataSet(buffer));
			}
		}

		@SuppressWarnings("unchecked")
		public <L extends Layer> L resolveTargetLayer(TargetLayerManifest target) {
			LayerManifest targetManifest = target.getResolvedLayerManifest();
			ContextManifest targetContextManifest = targetManifest.getContextManifest();

			// IMPORTANT:
			// We cannot use the layer lookup provided by the corpus interface, since
			// the context we are currently creating has not yet been added to the corpus.
			// Therefore we need to check for each target layer, whether it is hosted in the
			// same context or accessible via a foreign one.
			Context targetContext = layer.getContext();
			if(targetContextManifest!=layer.getManifest().getContextManifest()) {
				// Foreign context, previously registered, so use corpus for lookup
				targetContext = layer.getCorpus().getContext(targetContextManifest.getId());
			}

			// Resolve layer instance
			return (L) targetContext.getLayer(targetManifest.getId());
		}
	}

	public static class ItemLayerLinker extends LayerLinker {

		public ItemLayerLinker(Corpus corpus, ItemLayer layer, Producers producers) {
			super(corpus, layer, producers);
		}

		/**
		 * First calls the {@code #link()} method of the super implementation. Then casts
		 * the internal layer to {@link ItemLayer} and attaches a boundary and foundation
		 * layer if required.
		 *
		 * @see de.ims.icarus2.model.standard.registry.ContextFactory.LayerLinker#link()
		 */
		@Override
		public void link() {
			// Allow regular base layer linking to perform as usual
			super.link();

			// Now resolve and add boundary layer if required

			// For markable layers (and derived versions) we need to link the optional
			// boundary layer in addition!
			ItemLayer layer = (ItemLayer) getLayer();
			DefaultLayerGroup group = (DefaultLayerGroup) layer.getLayerGroup();

			TargetLayerManifest boundaryManifest = layer.getManifest().getBoundaryLayerManifest();
			if(boundaryManifest!=null) {
				// Resolve layer instance
				ItemLayer targetLayer = resolveTargetLayer(boundaryManifest);
				layer.setBoundaryLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.BOUNDARY));
				}
			}


			// For markable layers (and derived versions) we need to link the optional
			// foundation layer in addition!
			TargetLayerManifest foundationManifest = layer.getManifest().getFoundationLayerManifest();
			if(foundationManifest!=null) {
				// Resolve layer instance
				ItemLayer targetLayer = resolveTargetLayer(foundationManifest);
				layer.setFoundationLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.FOUNDATION));
				}
			}
		}

	}

	public static class FragmentLayerLinker extends ItemLayerLinker {

		public FragmentLayerLinker(Corpus corpus, FragmentLayer layer, Producers producers) {
			super(corpus, layer, producers);
		}

		/**
		 * First calls the {@code #link()} method of the super implementation. Then casts
		 * the internal layer to {@link FragmentLayer} and links the correct annotation
		 * layer used for value rasterization. In addition the rasterizer used for the layer
		 * is instantiated.
		 *
		 * @see de.ims.icarus2.model.standard.registry.ContextFactory.LayerLinker#link()
		 */
		@Override
		public void link() {
			// Allow regular base layer linking to perform as usual
			super.link();

			// Now resolve and add value annotation layer

			FragmentLayer layer = (FragmentLayer) getLayer();
			DefaultLayerGroup group = (DefaultLayerGroup) layer.getLayerGroup();
			TargetLayerManifest target = layer.getManifest().getValueLayerManifest();
			if(target!=null) {
				// Resolve layer instance
				AnnotationLayer targetLayer = resolveTargetLayer(target);
				layer.setValueLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.VALUE));
				}
			}

			// Finally instantiate the rasterizer for the fragment layer
			RasterizerManifest rasterizerManifest = layer.getManifest().getRasterizerManifest();

			// No default implementation available, therefore notify with exception
			if(rasterizerManifest==null)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.IMPLEMENTATION_MISSING,
						"Missing rasterizer manifest for fragment layer: "+getName(layer)); //$NON-NLS-1$
			if(rasterizerManifest.getImplementationManifest()==null)
				throw new ModelException(layer.getCorpus(), ModelErrorCode.IMPLEMENTATION_MISSING,
						"Missing rasterizer implementation manifest for fragment layer: "+getName(layer)); //$NON-NLS-1$

			// Instantiate rasterizer and assign to layer
			Producers producers = getProducers();
			ImplementationLoader<?> loader = null;
			if(producers.implementationLoaderSupplier!=null) {
				loader = producers.implementationLoaderSupplier.get();
			} else {
				loader = producers.memberFactory.newImplementationLoader();
			}

			Rasterizer rasterizer = loader
					.manifest(rasterizerManifest.getImplementationManifest())
					.corpus(getCorpus())
					.environment(layer)
					.message("Rasterizer for layer '"+getName(layer)+"'")
					.instantiate(Rasterizer.class);

			layer.setRasterizer(rasterizer);
		}

	}

	public static class Producers implements Cloneable {
		public Supplier<ImplementationLoader<?>> implementationLoaderSupplier;
		public CorpusMemberFactory memberFactory;
		public Function<AnnotationLayerManifest, AnnotationLayer> annotationLayerProducer;
		public Function<ItemLayerManifest, ItemLayer> itemLayerProducer;
		public Function<StructureLayerManifest, StructureLayer> structureLayerProducer;
		public Function<FragmentLayerManifest, FragmentLayer> fragmentLayerProducer;
		public Function<LayerGroupManifest, LayerGroup> layerGroupProducer;

		public void ensureFactories(Corpus corpus) {
			if(memberFactory==null) {
				boolean needsFactory = implementationLoaderSupplier==null
						|| layerGroupProducer==null
						|| annotationLayerProducer==null
						|| itemLayerProducer==null
						|| structureLayerProducer==null
						|| fragmentLayerProducer==null;

				if(needsFactory) {
					memberFactory = corpus.getManager().newFactory();
				}
			}
		}

		@Override
		public Producers clone() {
			try {
				return (Producers) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new IllegalStateException();
			}
		}
	}
}
