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
package de.ims.icarus2.model.util;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.DependencyType;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public class ModelGraph {

	// CONTEXTS factories

	public static Predicate<LayerGroup> groupsForContext(Context context)  {
		requireNonNull(context);

		return group -> group.getContext()==context;
	}

	public static Predicate<Layer> layersForContext(Context context)  {
		requireNonNull(context);

		return layer -> layer.getContext()==context;
	}

	public static Predicate<Layer> layersForGroup(LayerGroup group)  {
		requireNonNull(group);

		return layer -> layer.getLayerGroup()==group;
	}

	public static Graph<Context> contextGraph(Context rootContext, Predicate<? super Context> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Context.class, Collections.singleton(rootContext), contextMapper(toLookup(dependencies)), filter);
	}

	public static Graph<Context> contextGraph(Collection<? extends Context> rootContexts, Predicate<? super Context> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Context.class, rootContexts, contextMapper(toLookup(dependencies)), filter);
	}

	public static Graph<Context> contextGraph(Corpus corpus, Predicate<? super Context> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Context.class, corpus.getRootContexts(), contextMapper(toLookup(dependencies)), filter);
	}



	// GROUPS factories

	public static Graph<LayerGroup> groupGraph(LayerGroup rootGroup, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		return Graph.genericGraph(LayerGroup.class, Collections.singleton(rootGroup), groupMapper(toLookup(dependencies)), filter);
	}

	public static Graph<LayerGroup> groupGraph(Collection<? extends LayerGroup> rootGroups, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		return Graph.genericGraph(LayerGroup.class, rootGroups, groupMapper(toLookup(dependencies)), filter);
	}

	public static Graph<LayerGroup> groupGraph(Context rootContext, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		return Graph.genericGraph(LayerGroup.class, rootContext.getLayerGroups(), groupMapper(toLookup(dependencies)), filter);
	}

	public static Graph<LayerGroup> groupGraph(Corpus corpus, Predicate<? super LayerGroup> filter, DependencyType...dependencies) {
		LazyCollection<LayerGroup> roots = LazyCollection.lazySet();
		Consumer<Context> collector = c -> roots.add(c.getPrimaryLayer().getLayerGroup()); //TODO do we rly want to limit groups to a subset?

		corpus.forEachRootContext(collector);
		corpus.forEachVirtualContext(collector);

		return Graph.genericGraph(LayerGroup.class, roots.getAsSet(), groupMapper(toLookup(dependencies)), filter);
	}



	// LAYERS factories

	public static Graph<Layer> layerGraph(Layer rootLayer, Predicate<? super Layer> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Layer.class, Collections.singleton(rootLayer), layerMapper(toLookup(dependencies)), filter);
	}

	public static Graph<Layer> layerGraph(Collection<? extends Layer> rootLayers, Predicate<? super Layer> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Layer.class, rootLayers, layerMapper(toLookup(dependencies)), filter);
	}

	public static Graph<Layer> layerGraph(LayerGroup rootGroup, Predicate<? super Layer> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Layer.class, rootGroup.getLayers(), layerMapper(toLookup(dependencies)), filter);
	}

	public static Graph<Layer> layerGraph(Context rootContext, Predicate<? super Layer> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Layer.class, rootContext.getLayers(), layerMapper(toLookup(dependencies)), filter);

	}

	public static Graph<Layer> layerGraph(Corpus rootCorpus, Predicate<? super Layer> filter, DependencyType...dependencies) {
		return Graph.genericGraph(Layer.class, rootCorpus.getLayers(), layerMapper(toLookup(dependencies)), filter);
	}

	/**
	 * Converts an array of {@link DependencyType} instances into a lookup
	 * {@link Set}. If the provided array is {@code null} or empty, the returned
	 * set will contain all possible type {@link DependencyType#values() values}.
	 *
	 * @param dependencies
	 * @return
	 */
	private static Set<DependencyType> toLookup(DependencyType[] dependencies) {
		if(dependencies==null || dependencies.length==0) {
			return EnumSet.allOf(DependencyType.class);
		}

		Set<DependencyType> result = EnumSet.noneOf(DependencyType.class);
		CollectionUtils.feedItems(result, dependencies);

		return result;
	}

	public static BiConsumer<Layer, Consumer<? super Layer>> layerMapper(Set<DependencyType> dependencies) {
		return (source, action) -> {
			if(dependencies.contains(DependencyType.STRONG)) {
				source.getBaseLayers().forEach(action);
			}

			if(ModelUtils.isItemLayer(source)) {
				ItemLayer itemLayer = (ItemLayer) source;
				ItemLayer foundationLayer = itemLayer.getFoundationLayer();

				if(foundationLayer!=null && dependencies.contains(DependencyType.FOUNDATION)) {
					action.accept(foundationLayer);
				}

				ItemLayer boundaryLayer = itemLayer.getBoundaryLayer();

				if(boundaryLayer!=null && dependencies.contains(DependencyType.BOUNDARY)) {
					action.accept(boundaryLayer);
				}

				if(ModelUtils.isFragmentLayer(source)) {
					FragmentLayer fragmentLayer = (FragmentLayer) source;
					AnnotationLayer valueLayer = fragmentLayer.getValueLayer();

					if(valueLayer!=null && dependencies.contains(DependencyType.VALUE)) {
						action.accept(valueLayer);
					}
				}
			}
		};
	}

	public static BiConsumer<LayerGroup, Consumer<? super LayerGroup>> groupMapper(Set<DependencyType> dependencies) {
		return (source, action) -> {
			source.forEachDependency(d -> {
				if(dependencies.contains(d.getType())) {
					action.accept(d.getTarget());
				}
			});
		};
	}

	public static BiConsumer<Context, Consumer<? super Context>> contextMapper(Set<DependencyType> dependencies) {
		return (source, action) -> {
			source.forEachLayerGroup(g -> {
				g.forEachDependency(d -> {
					Context target = d.getTarget().getContext();
					if(source!=target && dependencies.contains(d.getType())) {
						action.accept(target);
					}
				});
			});
		};
	}

}
