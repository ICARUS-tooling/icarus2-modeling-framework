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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.model.util.ModelUtils.getName;
import static de.ims.icarus2.query.api.iql.IqlUtils.fragment;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.ScopeBuilder;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.util.Graph;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.query.api.Query;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.engine.QueryProcessor.Option;
import de.ims.icarus2.query.api.engine.ext.EngineExtension;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContextBuilder;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.ExpressionFactory;
import de.ims.icarus2.query.api.exp.env.ConstantsEnvironment;
import de.ims.icarus2.query.api.iql.IqlCorpus;
import de.ims.icarus2.query.api.iql.IqlData;
import de.ims.icarus2.query.api.iql.IqlImport;
import de.ims.icarus2.query.api.iql.IqlLayer;
import de.ims.icarus2.query.api.iql.IqlProperty;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.util.AbstractBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Central entry point for querying corpora.
 *
 * @author Markus Gärtner
 *
 */
public class QueryEngine {

	public static Builder builder() {
		return new Builder();
	}

	private final ObjectMapper mapper;

	private final CorpusManager corpusManager;

	private QueryEngine(Builder builder) {
		builder.validate();

		mapper = builder.getMapper();
		corpusManager = builder.getCorpusManager();
	}

	public QueryJob evaluateQuery(Query rawQuery) throws InterruptedException {

		// Read and parse query content
		IqlQuery query = readQuery(rawQuery);
		// Ensure we only ever consider validated queries
		query.checkIntegrity();

		// Resolves imports and embedded data
		QueryContext queryContext = new QueryContext(query);

		List<IqlStream> streams = query.getStreams();
		if(streams.size()>1)
			throw new QueryException(QueryErrorCode.UNSUPPORTED_FEATURE,
					"Queries on multiple corpus streams not yet supported");

		return new StreamProcessor(queryContext, streams.get(0)).process();
	}

	/**
	 * Parses a raw JSON query into an {@link IqlQuery} instance,
	 * but does <b>not</b> parse the embedded textual representations
	 * for query payload, grouping and query instructions.
	 */
	@VisibleForTesting
	IqlQuery readQuery(Query rawQuery) throws QueryException {
		requireNonNull(rawQuery);

		try {
			return mapper.readValue(rawQuery.getText(), IqlQuery.class);
		} catch (JsonProcessingException e) {
			throw new QueryException(QueryErrorCode.JSON_ERROR,
					"Failed to read query",
					fragment(rawQuery.getText(), e.getLocation()), e);
		}
	}

	/**
	 * Helper class that contains resolved extensions and decoded data for a query
	 * that can be shared amongst multiple stream matchers.
	 *
	 * @author Markus Gärtner
	 */
	private class QueryContext {
		private final IqlQuery query;
		private final List<EngineExtension> extensions;
		private final Map<String, Object> embeddedData;

		QueryContext(IqlQuery query) {
			this.query = requireNonNull(query);

			// Resolve extensions
			List<IqlImport> imports = query.getImports();
			if(imports.isEmpty()) {
				extensions = Collections.emptyList();
			} else {
				extensions = imports.stream()
						.map(imp -> resolveExtension(imp.getName(), imp.isOptional()))
						.collect(Collectors.toList());
			}

			// Decode embedded data
			List<IqlData> payload = query.getEmbeddedData();
			if(payload.isEmpty()) {
				embeddedData = Collections.emptyMap();
			} else {
				embeddedData = new Object2ReferenceOpenHashMap<>();
				for(IqlData data : payload) {
					String name = data.getName();
					if(embeddedData.containsKey(name))
						throw EvaluationUtils.forIncorrectUse("Duplicate name for embedded data: %s", name);

					embeddedData.put(name, decode(data));
				}
			}
		}

		public IqlQuery getQuery() { return query; }

		public List<EngineExtension> getExtensions() { return extensions; }

		public Map<String, Object> getEmbeddedData() { return embeddedData; }
	}

	private EngineExtension resolveExtension(String name, boolean optional) {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	private Object decode(IqlData data) {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	private class StreamProcessor {
		private final QueryContext queryContext;
		private final IqlStream stream;
		private final boolean ignoreWarnings;

		StreamProcessor(QueryContext queryContext, IqlStream stream) {
			this.queryContext = requireNonNull(queryContext);
			this.stream = requireNonNull(stream);
			ignoreWarnings = queryContext.getQuery().isSwitchSet(QuerySwitch.WARNINGS_OFF);
		}

		QueryJob process() throws InterruptedException {
			Set<Option> options = new HashSet<>();
			if(ignoreWarnings) {
				options.add(Option.IGNORE_WARNINGS);
			}
			// Now process the single stream content into full IQL elements
			new QueryProcessor(options).parseStream(stream);
			// Intermediate sanity check against missed settings
			stream.checkIntegrity();

			CorpusManifest corpusManifest = resolveCorpus(stream.getCorpus());
			//TODO do we actually need to perform the corpus connection here already?
			Corpus corpus = corpusManager.connect(corpusManifest);

			// Now build context for our single stream
			RootContextBuilder contextBuilder = EvaluationContext.rootBuilder();
			contextBuilder.corpus(corpus);

			// Apply various aspects of configuration
			applySettings(contextBuilder, queryContext.getQuery());
			applyLayers(contextBuilder, corpus, stream);
			applyEmbeddedData(contextBuilder, queryContext.getEmbeddedData());
			applyExtensions(contextBuilder, queryContext.getExtensions()); // partly outside our control

			EvaluationContext rootContext = contextBuilder.build();
			ExpressionFactory expressionFactory = new ExpressionFactory(rootContext);
			//TODO build the structural matchers and parse all the expressions

			throw new UnsupportedOperationException();
		}

		private CorpusManifest resolveCorpus(IqlCorpus source) {
			//TODO provide a mechanism to resolve PID values in IqlCorpus instances
			return corpusManager.getManifestRegistry().getCorpusManifest(source.getName())
					.orElseThrow(() -> new QueryException(QueryErrorCode.UNKNOWN_IDENTIFIER,
							"Unable to resolve corpus name: "+source.getName()));
		}

		private void applySettings(RootContextBuilder builder, IqlQuery query) {
			for(IqlProperty property : query.getSetup()) {
				if(property.getValue().isPresent()) {
					builder.setProperty(property.getKey(), property.getValue().get());
				} else {
					builder.setSwitch(property.getKey(), true);
				}
			}
		}

		private void applyEmbeddedData(RootContextBuilder builder, Map<String, Object> data) {
			if(data.isEmpty()) {
				return;
			}

			builder.addEnvironment(ConstantsEnvironment.forMapping(data));
		}

		private void applyExtensions(RootContextBuilder builder, List<EngineExtension> extensions) {
			if(extensions.isEmpty()) {
				return;
			}

			for(EngineExtension extension : extensions) {
				extension.configureContext(builder);
			}
		}

		/**
		 * Reads layer information from the given stream and prepares the specified builder.
		 */
		private void applyLayers(RootContextBuilder builder, Corpus corpus, IqlStream stream) {

			List<IqlLayer> rawLayers;
			boolean addTransitive = false;
			Map<String, Layer> aliasedLayers = new Object2ObjectOpenHashMap<>();
			Set<Layer> allLayers = new ReferenceOpenHashSet<>();
			ItemLayer primaryLayer = null;
			Set<Layer> pendingLayers = new ReferenceOpenHashSet<>();

			if(stream.getScope().isPresent()) {
				rawLayers = stream.getScope().get().getLayers();
			} else {
				addTransitive = true;
				rawLayers = stream.getLayers();
			}

			for (IqlLayer rawLayer : rawLayers) {
				Layer layer = resolveLayer(rawLayer, corpus);
				if(!allLayers.add(layer))
					throw new QueryException(QueryErrorCode.INCORRECT_USE,
							"Duplicate layer for scope: "+getName(layer));

				// Mark layer for delayed expansion if needed
				if(addTransitive || rawLayer.isAllMembers()) {
					pendingLayers.add(layer);
				}

				if(rawLayer.getAlias().isPresent()) {
					String alias = rawLayer.getAlias().get();
					if(aliasedLayers.containsKey(alias))
						throw new QueryException(QueryErrorCode.INCORRECT_USE,
								"Alias already in use: "+alias);
					aliasedLayers.put(alias, layer);
				}

				if(rawLayer.isPrimary()) {
					if(!ModelUtils.isItemLayer(layer))
						throw new QueryException(QueryErrorCode.INCORRECT_USE,
								"Designated primary layer is not a valid item layer: "+getName(layer));
					if(primaryLayer!=null)
						throw new QueryException(QueryErrorCode.INCORRECT_USE,
								"Primary layer already designated: "+getName(layer));
					primaryLayer = (ItemLayer)layer;
				}
			}

			// Populate the scope further via transitive dependencies
			if(!pendingLayers.isEmpty()) {
				Graph<Layer> graph = Graph.layerGraph(pendingLayers, Graph.acceptAll());
				graph.forEachNode(allLayers::add);
			}

			builder.scope(createScope(corpus, allLayers, primaryLayer));
			aliasedLayers.forEach(builder::namedLayer);
		}

		private Scope createScope(Corpus corpus, Set<Layer> layers, ItemLayer primaryLayer) {
			ScopeBuilder builder = ScopeBuilder.of(corpus);
			builder.addContexts(layers.stream()
					.map(Layer::getContext)
					.distinct()
					.collect(Collectors.toList()));
			builder.addLayers(new ArrayList<>(layers));
			builder.setPrimaryLayer(primaryLayer);
			return builder.build();
		}

		/**
		 * Resolves the specified layer within the given corpus. This only resolves
		 * 'native' layers, so no internal aliases or dependencies are taken into account!
		 *
		 * @see Corpus#getLayer(String, boolean)
		 */
		private Layer resolveLayer(IqlLayer source, Corpus corpus) {
			String name = source.getName();
			return corpus.getLayer(name, true);
		}
	}

	/**
	 * Helper class to store registered extensions, codecs and other globally
	 * usable utility components. Anything not resolvable from this registry
	 * will cause an exception when the {@link QueryEngine} prepares the matcher
	 * automaton for a new query.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static class ExtensionRegistry {


	}

	public static class Builder extends AbstractBuilder<Builder, QueryEngine> {

		private ObjectMapper mapper;

		private CorpusManager corpusManager;

		private final ExtensionRegistry extensionRegistry = new ExtensionRegistry();

		private Builder() {
			// no-op
		}

		public Builder mapper(ObjectMapper mapper) {
			requireNonNull(mapper);
			checkState("Mapper already set", this.mapper==null);
			this.mapper = mapper;
			return this;
		}

		public Builder corpusManager(CorpusManager corpusManager) {
			requireNonNull(corpusManager);
			checkState("Corpus manager already set", this.corpusManager==null);
			this.corpusManager = corpusManager;
			return this;
		}

		public Builder useDefaultMapper() {
			return mapper(IqlUtils.createMapper());
		}

		public ObjectMapper getMapper() {
			return mapper;
		}

		public CorpusManager getCorpusManager() {
			return corpusManager;
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			checkState("Mapper not set", mapper!=null);
			checkState("Corpus manager not set", corpusManager!=null);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected QueryEngine create() {
			return new QueryEngine(this);
		}

	}
}
