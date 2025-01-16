/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.registry;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
import de.ims.icarus2.model.api.registry.CorpusMemberFactory;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ImplementationLoader;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.corpus.DefaultContext;
import de.ims.icarus2.model.standard.members.layer.DefaultLayerGroup;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.collections.set.DataSets;

/**
 * @author Markus Gärtner
 *
 */
public class ContextFactory {

	private Producers producers = new Producers();

	public synchronized ContextFactory implementationLoaderSupplier(Supplier<ImplementationLoader<?>> implementationLoaderSupplier) {
		requireNonNull(implementationLoaderSupplier);
		checkState(producers.implementationLoaderSupplier==null);

		producers.implementationLoaderSupplier = implementationLoaderSupplier;

		return this;
	}

	public synchronized ContextFactory setMemberFactory(CorpusMemberFactory memberFactory) {
		requireNonNull(memberFactory);
		checkState(producers.memberFactory==null);

		producers.memberFactory = memberFactory;

		return this;
	}

	public synchronized ContextFactory annotationLayerProducer(
			Function<AnnotationLayerManifest, AnnotationLayer> annotationLayerProducer) {
		requireNonNull(annotationLayerProducer);
		checkState(producers.annotationLayerProducer==null);

		producers.annotationLayerProducer = annotationLayerProducer;

		return this;
	}

	public synchronized ContextFactory itemLayerProducer(
			Function<ItemLayerManifest, ItemLayer> itemLayerProducer) {
		requireNonNull(itemLayerProducer);
		checkState(producers.itemLayerProducer==null);

		producers.itemLayerProducer = itemLayerProducer;

		return this;
	}

	public synchronized ContextFactory structureLayerProducer(
			Function<StructureLayerManifest, StructureLayer> structureLayerProducer) {
		requireNonNull(structureLayerProducer);
		checkState(producers.structureLayerProducer==null);

		producers.structureLayerProducer = structureLayerProducer;

		return this;
	}

	public synchronized ContextFactory fragmentLayerProducer(
			Function<FragmentLayerManifest, FragmentLayer> fragmentLayerProducer) {
		requireNonNull(fragmentLayerProducer);
		checkState(producers.fragmentLayerProducer==null);

		producers.fragmentLayerProducer = fragmentLayerProducer;

		return this;
	}

	public synchronized ContextFactory layerGroupProducer(
			Function<LayerGroupManifest, LayerGroup> layerGroupProducer) {
		requireNonNull(layerGroupProducer);
		checkState(producers.layerGroupProducer==null);

		producers.layerGroupProducer = layerGroupProducer;

		return this;
	}

	protected synchronized Producers cloneAndGetProducers() {
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
		requireNonNull(corpus);
		requireNonNull(manifest);

		if(options==null) {
			options = Options.NONE;
		}

		// Fetch snapshot of producers and make sure we can create every possible member!
		Producers producers = cloneAndGetProducers();
		producers.ensureFactories(corpus);

		DefaultContext context = (DefaultContext) newContext(corpus, manifest, producers, options);
		List<LayerLinker> linkers = new ArrayList<>(manifest.getLayerManifests().size());

		// First pass, create layers
		for(LayerGroupManifest groupManifest : manifest.getGroupManifests()) {

			// Create group
			DefaultLayerGroup group = (DefaultLayerGroup) newLayerGroup(groupManifest, producers, options);

			context.addLayerGroup(group);

			// Create layers
			for(LayerManifest<?> layerManifest : groupManifest.getLayerManifests()) {
				LayerLinker linker = newLayer(corpus, layerManifest, producers, options);

				group.addLayer(linker.getLayer());
				context.addLayer(linker.getLayer());

				linkers.add(linker);
			}

			// Finally set primary layer (can only be a native layer!!!)
			groupManifest.getPrimaryLayerManifest().ifPresent(m -> group.setPrimaryLayer(
					(ItemLayer) context.getNativeLayer(ManifestUtils.requireId(m))));
		}

		// Intermediate linking

		// Set context wide primary layer (can be a foreign one)
		manifest.getPrimaryLayerManifest().ifPresent(m -> context.setPrimaryLayer(
				(ItemLayer) context.getLayer(m.getLayerId())));

		// Set context wide foundation layer (can be a foreign one)
		manifest.getFoundationLayerManifest().ifPresent(m -> context.setFoundationLayer(
				(ItemLayer) context.getLayer(m.getLayerId())));

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
		Object declaredGroup = options.get(ManifestUtils.requireId(groupManifest));
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
	protected LayerLinker newLayer(Corpus corpus, LayerManifest<?> manifest, Producers producers, Options options) {

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

		Object declaredLayer = options.get(ManifestUtils.requireId(manifest));

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

		Object declaredLayer = options.get(ManifestUtils.requireId(manifest));

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

		Object declaredLayer = options.get(ManifestUtils.requireId(manifest));

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

		Object declaredLayer = options.get(ManifestUtils.requireId(manifest));

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
			requireNonNull(corpus);
			requireNonNull(layer);
			requireNonNull(producers);

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
			LayerManifest<?> layerManifest = layer.getManifest();

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
			LayerManifest<?> targetManifest = target.getResolvedLayerManifest().orElseThrow(
					ManifestException.error("Unresolved target layer manifest: "+ManifestUtils.getName(target)));
			ContextManifest targetContextManifest = ManifestUtils.requireGrandHost(targetManifest);

			// IMPORTANT:
			// We cannot use the layer lookup provided by the corpus interface, since
			// the context we are currently creating has not yet been added to the corpus.
			// Therefore we need to check for each target layer, whether it is hosted in the
			// same context or accessible via a foreign one.
			Context targetContext = layer.getContext();
			if(!IcarusUtils.equals(layer.getManifest().getContextManifest(), targetContextManifest)) {
				// Foreign context, previously registered, so use corpus for lookup
				targetContext = layer.getCorpus().getContext(ManifestUtils.requireId(targetContextManifest));
			}

			// Resolve layer instance
			return (L) targetContext.getLayer(ManifestUtils.requireId(targetManifest));
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

			layer.getManifest().getBoundaryLayerManifest().ifPresent(m -> {
				// Resolve layer instance
				ItemLayer targetLayer = resolveTargetLayer(m);
				layer.setBoundaryLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.BOUNDARY));
				}
			});

			// For markable layers (and derived versions) we need to link the optional
			// foundation layer in addition!
			layer.getManifest().getFoundationLayerManifest().ifPresent(m -> {
				// Resolve layer instance
				ItemLayer targetLayer = resolveTargetLayer(m);
				layer.setFoundationLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.FOUNDATION));
				}
			});
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
			layer.getManifest().getValueLayerManifest().ifPresent(m -> {
				// Resolve layer instance
				AnnotationLayer targetLayer = resolveTargetLayer(m);
				layer.setValueLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.VALUE));
				}
			});

			// Finally instantiate the rasterizer for the fragment layer
			RasterizerManifest rasterizerManifest = layer.getManifest()
					.getRasterizerManifest().orElseThrow(
							() -> new ModelException(layer.getCorpus(), ManifestErrorCode.IMPLEMENTATION_MISSING,
						"Missing rasterizer manifest for fragment layer: "+getName(layer))); //$NON-NLS-1$
			if(!rasterizerManifest.getImplementationManifest().isPresent())
				throw new ModelException(layer.getCorpus(), ManifestErrorCode.IMPLEMENTATION_MISSING,
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
					.manifest(rasterizerManifest.getImplementationManifest().get())
					.environment(FragmentLayer.class, layer)
					.message("Rasterizer for layer '"+getName(layer)+"'")
					.instantiate(Rasterizer.class);

			layer.setRasterizer(rasterizer);
		}

	}

	public static class Producers implements Cloneable {
		public Supplier<ImplementationLoader<?>> implementationLoaderSupplier;
		/**
		 * Fallback to be used in case there's no specialized producer
		 * available for a certain layer member.
		 */
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
