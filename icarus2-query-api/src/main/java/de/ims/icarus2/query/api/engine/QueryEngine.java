/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.registry.CorpusManager;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.ScopeBuilder;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusView;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.util.Graph;
import de.ims.icarus2.model.util.ModelGraph;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.query.api.Query;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.engine.EngineSettings.IntField;
import de.ims.icarus2.query.api.engine.QueryProcessor.Option;
import de.ims.icarus2.query.api.engine.ext.EngineExtension;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern;
import de.ims.icarus2.query.api.engine.matcher.StructurePattern.Role;
import de.ims.icarus2.query.api.engine.result.QueryOutputFactory;
import de.ims.icarus2.query.api.engine.result.ResultSink;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.LaneContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContext;
import de.ims.icarus2.query.api.exp.EvaluationContext.RootContextBuilder;
import de.ims.icarus2.query.api.exp.EvaluationUtils;
import de.ims.icarus2.query.api.exp.env.ConstantsEnvironment;
import de.ims.icarus2.query.api.iql.IqlCorpus;
import de.ims.icarus2.query.api.iql.IqlData;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlImport;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlLayer;
import de.ims.icarus2.query.api.iql.IqlPayload;
import de.ims.icarus2.query.api.iql.IqlPayload.QueryType;
import de.ims.icarus2.query.api.iql.IqlProperty;
import de.ims.icarus2.query.api.iql.IqlQuery;
import de.ims.icarus2.query.api.iql.IqlScope;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.collections.Substitutor;
import de.ims.icarus2.util.lang.Lazy;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * Central entry point for querying corpora.
 *
 * @author Markus Gärtner
 *
 */
public class QueryEngine implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(QueryEngine.class);

	public static Builder builder() {
		return new Builder();
	}

	/** Query parser for deserializing textual queries */
	private final ObjectMapper mapper;
	/** Source of physical corpora */
	private final CorpusManager corpusManager;
	/** Performance-related settings for the engine */
	private final EngineSettings settings;

	private QueryEngine(Builder builder) {
		builder.validate();

		mapper = builder.getMapper();
		corpusManager = builder.getCorpusManager();
		settings = builder.getSettings().clone();
	}

	public ObjectMapper getMapper() { return mapper; }

	public EngineSettings getSettings() { return settings; }

	@Override
	public void close() {
		// TODO cleanup extension registry and buffers
	}

	public QueryJob evaluateQuery(Query rawQuery, ResultSink resultSink) throws InterruptedException {
		requireNonNull(rawQuery);
		requireNonNull(resultSink);

		// Read and parse query content
		IqlQuery query = readQuery(rawQuery);

		return evaluateQuery(query, resultSink);
	}

	public QueryJob evaluateQuery(IqlQuery query, ResultSink resultSink) throws InterruptedException {
		// Ensure we only ever consider validated queries
		query.checkIntegrity();

		// Resolves imports and embedded data
		QueryContext queryContext = new QueryContext(query, resultSink);

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
		private final ResultSink resultSink;

		QueryContext(IqlQuery query, ResultSink resultSink) {
			this.query = requireNonNull(query);
			this.resultSink = requireNonNull(resultSink);

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

		public ResultSink getResultSink() { return resultSink; }
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
			Set<Option> options = EnumSet.noneOf(Option.class);
			if(ignoreWarnings) {
				options.add(Option.IGNORE_WARNINGS);
			}
			// Now process the single stream content into full IQL elements
			new QueryProcessor(options).parseStream(stream);
			// Intermediate sanity check against missed settings
			stream.checkIntegrity();

			final IqlPayload payload = stream.getPayload().orElseThrow(
					() -> EvaluationUtils.forInternalError("Failed to construct payload"));

			if(payload.getQueryType()==QueryType.ALL)
				//TODO provide a simple job implementation that just forwards all the corpus data
				throw new UnsupportedOperationException("query type 'ALL' not implemented");

			/* From here on we should be good at the formal query side, only issues now
			 * can stem from errors in expressions/constraints or when interacting with
			 * the live corpus resource.
			 */

			final CorpusManifest corpusManifest = resolveCorpus(stream.getCorpus());
			final String corpusId = ManifestUtils.requireId(corpusManifest);
			// We need fully connected corpus for the context builder below
			final Corpus corpus = corpusManager.connect(corpusManifest);
			if(corpus==null)
				throw new QueryException(QueryErrorCode.CORPUS_UNREACHABLE,
						String.format("Failed to conenct to corpus: %s", corpusId));

			final Scope scope = createScope(corpus, stream);

			final CorpusData corpusData = CorpusData.CorpusBacked.builder()
					.scope(scope)
					.build();

			final RootContext rootContext = createContext(corpusData, payload);

			final List<IqlLane> lanes = payload.getLanes();
			final List<StructurePattern> patterns = new ObjectArrayList<>();

			if(lanes.isEmpty()) {
				// Plain query without any lanes or structural constraints
				patterns.add(createPlainPattern(payload, rootContext));
			} else {
				createNodeMapping(lanes);
				// At least one proper (proxy) lane definition available
				createLanePatterns(payload, rootContext, patterns::add);
			}

			assert !patterns.isEmpty();
			assert patterns.size()>=lanes.size();

			final Lazy<Substitutor<CharSequence>> substitutor = Lazy.create(
					() -> new Substitutor<>(settings.getInt(IntField.INITIAL_SECONDARY_BUFFER_SIZE)));
			final QueryOutput output = new QueryOutputFactory(rootContext)
					.encoder(() -> substitutor.value()::applyAsInt)
					.decoder(() -> substitutor.value()::apply)
					.applyFromStream(stream)
					.settings(settings)
					.resultSink(queryContext.getResultSink())
					.createOutput();

			SingleStreamJob.Builder builder = SingleStreamJob.builder()
					.addPatterns(patterns)
					.batchSize(settings.getInt(IntField.BATCH_SIZE))
					.query(queryContext.getQuery())
					.input(QueryUtils.streamedInput(createStream(scope)))
					.output(output);

			// Only register substitutor if the output actually used it
			substitutor.optional().ifPresent(builder::addCloseable);

			return builder.build();
		}

		/** Collect all the nodes in our query that need mapping and ensure unique mapping ids for all of them */
		private void createNodeMapping(List<IqlLane> lanes) {
			List<IqlNode> mappedNodes = new ObjectArrayList<>();
			Int2ObjectMap<IqlNode> nodeLookup = new Int2ObjectOpenHashMap<>();

			Consumer<IqlNode> action = node -> {
				if(EvaluationUtils.needsMapping(node)) {
					mappedNodes.add(node);
				}
			};

			for(IqlLane lane : lanes) {
				EvaluationUtils.visitNodes(lane.getElement(), action);
			}

			// No mappings required... rare but possible
			if(mappedNodes.isEmpty()) {
				return;
			}

			List<IqlNode> pendingNodes = new ObjectArrayList<>();

			// Honor all the pre-configured mapping ids
			for(IqlNode node : mappedNodes) {
				if(node.getMappingId() == UNSET_INT) {
					pendingNodes.add(node);
					continue;
				}
				if(nodeLookup.putIfAbsent(node.getMappingId(), node) != null)
					throw EvaluationUtils.forIncorrectUse("Duplicate mapping id: %d", _int(node.getMappingId()));
			}

			// All unique pre-configured mapping ids are done, now process those with undefined mapping ids
			if(!pendingNodes.isEmpty()) {
				int mappingId = 0;
				for(IqlNode node : pendingNodes) {
					// Find the first unused mapping id
					while(nodeLookup.containsKey(mappingId)) {
						mappingId++;
					}
					node.setMappingId(mappingId);
					nodeLookup.put(mappingId, node);
					mappingId++; // Saves us one iteration on the next node
				}
			}
		}

		private RootContext createContext(CorpusData corpusData, IqlPayload payload) {
			// Now build context for our single stream
			RootContextBuilder contextBuilder = EvaluationContext.rootBuilder(corpusData);

			// Apply various aspects of configuration
			applySettings(contextBuilder, queryContext.getQuery());
			applyEmbeddedData(contextBuilder, queryContext.getEmbeddedData());
			applyExtensions(contextBuilder, queryContext.getExtensions()); // partly outside our control
			payload.getBindings().forEach(contextBuilder::bind);

			return contextBuilder.build();
		}

		private StructurePattern createPlainPattern(IqlPayload payload, EvaluationContext context) {

			StructurePattern.Builder builder = StructurePattern.builder()
					// Use lane index as id for new pattern
					.id(0)
					// Raw root environment for creating expressions etc...
					.context(context)
					// For simplicity the SINGLETON role can be used here (instead of defining a NONE proxy)
					.role(Role.SINGLETON);

			payload.getFilter().ifPresent(builder::filterConstraint);
			payload.getConstraint().ifPresent(builder::globalConstraint);

			final StructurePattern pattern = builder.build();
			assert pattern.getId()==0;

			return pattern;
		}

		private void createLanePatterns(IqlPayload payload, RootContext rootContext,
				Consumer<? super StructurePattern> action) {
			final List<IqlLane> lanes = payload.getLanes();
			final int last = lanes.size()-1;
			for (int i = 0; i <= last; i++) {
				final IqlLane lane = lanes.get(i);
				final boolean isFirst = i==0;
				final boolean isLast = i==last;

				final LaneContext laneContext = rootContext.derive()
						.lane(lane)
						.build();

				StructurePattern.Builder builder = StructurePattern.builder()
						// Use lane index as id for new pattern
						.id(i)
						// Environment for creating expressions etc...
						.context(laneContext)
						// Actual structural root element (lane)
						.source(lane)
						// Role/Position of the lane
						.role(Role.of(isFirst, isLast));

				// Local hit limit and match flags
				lane.getFlags().forEach(builder::flag);
				lane.getLimit().ifPresent(builder::limit);

				// If this is the first lane, try to apply filter constraints
				if(isFirst) {
					payload.getFilter().ifPresent(builder::filterConstraint);
				}
				// If this is the last lane, try to apply global constraints
				if(isLast) {
					payload.getConstraint().ifPresent(builder::globalConstraint);
				}

				final StructurePattern pattern = builder.build();
				assert pattern.getId()==i;
				action.accept(pattern);
			}
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

		private Scope createScope(Corpus corpus, IqlStream stream) {

			List<IqlLayer> rawLayers;
			boolean addTransitive = false;
			Map<String, Layer> aliasedLayers = new Object2ObjectOpenHashMap<>();
			Set<Layer> allLayers = new ReferenceOpenHashSet<>();
			ItemLayer primaryLayer = null;
			Set<Layer> pendingLayers = new ReferenceOpenHashSet<>();

			if(stream.getScope().isPresent()) {
				IqlScope scope = stream.getScope().get();
				if(scope.isFull()) {
					return corpus.createCompleteScope();
				}

				rawLayers = scope.getLayers();
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
				Graph<Layer> graph = ModelGraph.layerGraph(pendingLayers, Graph.acceptAll());
				graph.forEachNode(allLayers::add);
			}

			ScopeBuilder builder = ScopeBuilder.of(corpus);
			builder.addContexts(allLayers.stream()
					.map(Layer::getContext)
					.distinct()
					.collect(Collectors.toList()));
			builder.addLayers(new ArrayList<>(allLayers));
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

		private StreamedCorpusView createStream(Scope scope) throws InterruptedException {
			try {
				return scope.getCorpus().createStream(scope, AccessMode.READ, Options.none());
			} catch (IcarusApiException e) {
				throw new QueryException(QueryErrorCode.CORPUS_UNREACHABLE, "Failed to create stream for corpus", e);
			}
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

		//TODO add actual methods for data resolution
	}

	public static class Builder extends AbstractBuilder<Builder, QueryEngine> {

		private ObjectMapper mapper;

		private CorpusManager corpusManager;

		private EngineSettings settings;

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

		/** Use a {@link ObjectMapper} with default settings, obtained via {@link IqlUtils#createMapper()}. */
		public Builder useDefaultMapper() { return mapper(IqlUtils.createMapper()); }

		public Builder settings(EngineSettings settings) {
			requireNonNull(settings);
			checkState("Settings already set", this.settings==null);
			this.settings = settings;
			return this;
		}

		/** Use a {@link EngineSettings} instance with default values. */
		public Builder useDefaultSettings() { return settings(new EngineSettings()); }

		public ObjectMapper getMapper() { return mapper; }
		public CorpusManager getCorpusManager() { return corpusManager; }
		public EngineSettings getSettings() { return settings; }

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			checkState("Mapper not set", mapper!=null);
			checkState("Corpus manager not set", corpusManager!=null);
			checkState("Settings not set", settings!=null);
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
