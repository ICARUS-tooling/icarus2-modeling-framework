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
package de.ims.icarus2.filedriver.schema.tabular;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._int;
import static de.ims.icarus2.util.lang.Primitives._long;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.filedriver.AbstractConverter;
import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.ElementFlag;
import de.ims.icarus2.filedriver.FileDataStates;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.FileDriver.LockableFileObject;
import de.ims.icarus2.filedriver.analysis.Analyzer;
import de.ims.icarus2.filedriver.analysis.AnnotationAnalyzer;
import de.ims.icarus2.filedriver.analysis.DefaultItemLayerAnalyzer;
import de.ims.icarus2.filedriver.analysis.DefaultStructureLayerAnalyzer;
import de.ims.icarus2.filedriver.analysis.ItemLayerAnalyzer;
import de.ims.icarus2.filedriver.schema.SchemaBasedConverter;
import de.ims.icarus2.filedriver.schema.resolve.BatchResolver;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverFactory;
import de.ims.icarus2.filedriver.schema.resolve.ResolverOptions;
import de.ims.icarus2.filedriver.schema.resolve.common.BasicAnnotationResolver;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteType;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.model.api.driver.indices.standard.SingletonIndexSet;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.Item.ManagedItem;
import de.ims.icarus2.model.api.members.item.manager.ItemLookup;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.standard.members.container.AbstractImmutableContainer;
import de.ims.icarus2.model.util.Graph;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LazyMap;
import de.ims.icarus2.util.collections.Pool;
import de.ims.icarus2.util.collections.WeakHashSet;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.collections.set.DataSets;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.strings.FlexibleSubSequence;
import de.ims.icarus2.util.strings.StringUtil;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(Converter.class)
public class TableConverter extends AbstractConverter implements SchemaBasedConverter {

	private TableSchema tableSchema;

	private LayerMemberFactory memberFactory;

	private final ResolverFactory RESOLVER_FACTORY = ResolverFactory.newInstance();

	private Charset encoding;
	private int characterChunkSize;

	private final Pool<BlockHandler> blockHandlerPool = new Pool<>(this::createRootBlockHandler, 10);

	public TableConverter() {
		// no-op
	}

	public TableConverter(TableSchema tableSchema) {
		requireNonNull(tableSchema);

		this.tableSchema = tableSchema;
	}

	private Resolver createResolver(ResolverSchema schema) {
		if(schema==null) {
			return null;
		}

		return RESOLVER_FACTORY.createResolver(schema.getType());
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#addNotify(de.ims.icarus2.model.api.driver.Driver)
	 */
	@Override
	public void addNotify(Driver owner) {
		super.addNotify(owner);

		FileDriver driver = (FileDriver) owner;

		ContextManifest contextManifest = driver.getManifest().getContextManifest()
				.orElseThrow(ManifestException.noHost(driver.getManifest()));

		//TODO fetch and process TableSchema from driver settings [redundant to constructor?]

		/*
		 * Initialize our character buffer with sufficient capacity to hold a single block.
		 * If the informed estimation fails the buffer will be initialized with a default
		 * capacity. While having a sufficient capacity from the start is nice, we can live
		 * with the cost of resizing the buffer a few times.
		 */
		String layerId = tableSchema.getRootBlock().getLayerId();
		ItemLayerManifestBase<?> layerManifest = (ItemLayerManifestBase<?>) contextManifest.getLayerManifest(layerId)
				.orElseThrow(ManifestException.error("No such layer: "+layerId));
		// We use the recommended size for byte buffers here to be on the safe side for our character buffer
		characterChunkSize = getRecommendedByteBufferSize(layerManifest);

		encoding = driver.getEncoding();
		memberFactory = driver.newMemberFactory();

	}

	/**
	 * @see de.ims.icarus2.model.api.driver.mods.DriverModule#readManifest(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest)
	 */
	@Override
	public void readManifest(ModuleManifest manifest) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#removeNotify(de.ims.icarus2.model.api.driver.Driver)
	 */
	@Override
	public void removeNotify(Driver owner) {
		encoding = null;
		characterChunkSize = -1;

		super.removeNotify(owner);
	}

	@Override
	public TableSchema getSchema() {
		checkAdded();
		return tableSchema;
	}

	public LayerMemberFactory getSharedMemberFactory() {
		return memberFactory;
	}

	/**
	 * Used for our {@link Pool} of {@link BlockHandler block handlers} as a {@link Supplier}.
	 *
	 * @return
	 */
	protected BlockHandler createRootBlockHandler() {
		// Fail if closed
		checkAdded();

		return new BlockHandler(tableSchema.getRootBlock());
	}

	private static final int DEFAULT_TEMP_CHUNK_COUNT = 500;

	private static List<LayerGroup> getGroups(BlockHandler blockHandler) {
		List<LayerGroup> layerGroups = list(blockHandler.getItemLayer().getLayerGroup());
		CollectionUtils.feedItems(layerGroups, blockHandler.getExternalGroups());
		return layerGroups;
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#scanFile(int, de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage)
	 */
	@Override
	public Report<ReportItem> scanFile(int fileIndex) throws IOException,
			InterruptedException {
		checkAdded();

		@SuppressWarnings("resource")
		BlockHandler blockHandler = blockHandlerPool.get();

		List<LayerGroup> layerGroups = getGroups(blockHandler);

		Map<Layer, Analyzer> analyzers = createAnalyzers(layerGroups, fileIndex);

		Map<ItemLayer, ItemLookup> lookups = new Object2ObjectOpenHashMap<>();

		// For each item layer create a wrapped analyzer that also cleans up related annotation content
		BiFunction<ItemLayer, Graph<Layer>, InputCache> cacheGen = (layer, graph) -> {

			// Make sure a cleanup will remove all connected annotations
			Consumer<InputCache> cleanupAction = new AnnotationCleaner()
					.addStoragesFromLayers(graph.incomingNodes(layer));

			/*
			 * Contract with the createAnalyzers() method:
			 *
			 * For ItemLayer objects the returned analyzer will always be an
			 * instance of ItemLayerAnalyzer.
			 */
			ItemLayerAnalyzer analyzer = (ItemLayerAnalyzer) analyzers.getOrDefault(layer, ItemLayerAnalyzer.EMPTY_ANALYZER);

			AnalyzingInputCache cache = new AnalyzingInputCache(analyzer, cleanupAction);

			lookups.put(layer, cache);

			return cache;
		};

		/*
		 *  Allow for the default facility to handle decision what layers will
		 *  receive caches, but override the actual implementation with our "empty"
		 *  cache that only holds the data until an analysis is done.
		 */
		final Map<ItemLayer, InputCache> caches = createCaches(layerGroups, cacheGen);

		/*
		 * We only need bare component suppliers without back-end storage
		 */
		final Map<ItemLayer, ComponentSupplier> componentSuppliers = new ComponentSuppliersFactory(this)
			.rootBlockHandler(blockHandler)
			.fileIndex(fileIndex)
			.memberFactory(getSharedMemberFactory())
			.mode(ReadMode.SCAN)
			.consumers(layer -> caches.get(layer)::offer)
			.lookups(lookups)
			.build();

		final ItemLayer primaryLayer = blockHandler.getItemLayer();

		// Reduce overhead by only purging our back-end storage every so often
		final MutableInteger chunksTillPurge = new MutableInteger(DEFAULT_TEMP_CHUNK_COUNT);

		ObjLongConsumer<Item> topLevelItemAction = (item, index) -> {
			/*
			 *  It's the drivers responsibility to set proper flags on an item, so we do it here.
			 *
			 *  Our assumption is that subclasses or factory code that gets called from within
			 *  the creation process already assigns other flags than ALIVE. Therefore this is
			 *  the only flag we need to worry about here. It is also the only one that MUST be
			 *  set in order for a blank item to get assigned VALID chunk state!
			 */
			if(item instanceof ManagedItem) {
				((ManagedItem)item).setAlive(true);
			}

			/*
			 *  We "abuse" caches to collect all the items created for a single chunk here.
			 *
			 *  Committing the caches will trigger the nested analyzers to collect metadata
			 *  and then discard the content of the cache.
			 */
			if(item.getLayer()==primaryLayer && chunksTillPurge.decrementAndGet()<=0) {
				chunksTillPurge.setInt(DEFAULT_TEMP_CHUNK_COUNT);
				caches.values().forEach(InputCache::commit);
				caches.values().forEach(InputCache::reset);
			}
		};

		InputResolverContext inputContext = new InputResolverContext(componentSuppliers, topLevelItemAction);
		long index = 0L;

		LockableFileObject fileObject = getDriver().getFileObject(fileIndex);

		// Notify handler stack about incoming SCAN operation
		blockHandler.prepareForReading(this, ReadMode.SCAN, caches::get);

		// Use default report builder mechanism and link analyzers to it
		ReportBuilder<ReportItem> reportBuilder = ReportBuilder.builder(tableSchema);
		analyzers.values().forEach(a -> a.init(reportBuilder));

		// Flag to keep track whether or not we encountered a "real" error that causes the scan to stop
		boolean encounteredFatalErrors = false;

		try(ReadableByteChannel channel = fileObject.getResource().getReadChannel()) {

			LineIterator lines = new BufferedLineIterator(channel, encoding, characterChunkSize);

			/*
			 *  Continue as long as there's still content in the file.
			 *  We need both checks since it's possible that while reading the
			 *  previous block we had to do some lookahead but didn't consume the
			 *  current line.
			 */
			scan_loop : while(lines.hasLine() || lines.next()) {

				// Clear state of handler and context
				blockHandler.reset();
				inputContext.reset();

				// Point the context to the layer's proxy container and requested index
				inputContext.setContainer(primaryLayer.getProxyContainer());
				inputContext.setIndex(index);

				/*
				 *  Technically speaking using exceptions for control flow here isn't
				 *  the best strategy, but with the call depth redesigning all involved
				 *  facilities (and duplicating) might be overkill in comparison.
				 */
				try {
					// Delegate actual conversion work (which will advance lines on its own)
					blockHandler.readChunk(lines, inputContext);
				} catch(RuntimeException e) {
					// Only care about "soft" errors here (they won't terminate the scanning process)

					ErrorCode errorCode = ErrorCode.forException(e, GlobalErrorCode.UNKNOWN_ERROR);

					// Report any "soft" problem as error and include physical location
					reportBuilder.addError(errorCode,
							"Invalid content in chunk {} in line {}: {}",
							_long(index), _long(lines.getLineNumber()), e.getMessage());
					//FIXME this is bad: manifest errors can keep the scan in an endless loop

					// For now we also abort here
//					break scan_loop;
					throw e;
				} catch(Exception e) {
					// ""Real" errors will break the scanning process

					reportBuilder.addError(ModelErrorCode.DRIVER_ERROR,
							"Fatal error in chunk {} in line {}: {}",
							_long(index), _long(lines.getLineNumber()), e.getMessage());

					// Make sure surrounding code knows about the error condition
					encounteredFatalErrors = true;

					// Potentially corrupted or unusable data, so stop scanning
					break scan_loop;
				}

				// Make sure we advance 1 line in case the last one has been consumed as content
				if(inputContext.isDataConsumed()) {
					lines.next();
				}

				// Next chunk
				index++;
			}

			// If nothing went wrong we still need to make sure that pending data is properly analyzed
			caches.values().forEach(InputCache::commit);

			// At last make sure all analyzers persist their gathered information
			analyzers.values().forEach(Analyzer::finish);

		} finally {
			blockHandler.close();
			blockHandlerPool.recycle(blockHandler);

			// Make sure we commit/discard all "cached" data
			if(encounteredFatalErrors) {
				caches.values().forEach(InputCache::discard);
			}
		}

		//TODO access caches and write into states information about number of elements for each layer and begin+end

		Report<ReportItem> report = reportBuilder.build();

		// Assign respective flag to file
		ElementFlag flagForFile = report.hasErrors() ? ElementFlag.UNUSABLE : ElementFlag.SCANNED;
		getDriver().getFileStates().getFileInfo(fileIndex).setFlag(flagForFile);

		return report;
	}

	/**
	 * Creates analyzers for the specified {@code group}.
	 * <p>
	 * The following relations are mandatory for the returned map:
	 * <ul>
	 * <li>If the provided layer is an {@link ItemLayer}, the returned analyzer will be an implementation of {@link ItemLayerAnalyzer}</li>
	 * <li>If the provided layer is an {@link StructureLayer}, the returned analyzer will be an implementation of {@link DefaultStructureLayerAnalyzer}</li>
	 * <li>If the provided layer is an {@link AnnotationLayer}, the returned analyzer will be an implementation of {@link AnnotationAnalyzer}</li>
	 * </ul>
	 *
	 * For now this implementation only produces analyzers for item and structure layers.
	 */
	protected Map<Layer, Analyzer> createAnalyzers(List<LayerGroup> groups, int fileIndex) {
		LazyMap<Layer, Analyzer> result = LazyMap.lazyHashMap();
		FileDataStates states = getDriver().getFileStates();

		for(LayerGroup group : groups) {
			group.forEachLayer(layer -> {
				if(ModelUtils.isStructureLayer(layer.getManifest())) {
					result.add(layer, new DefaultStructureLayerAnalyzer(states, (StructureLayer) layer, fileIndex));
				} else if(ModelUtils.isItemLayer(layer)) {
					result.add(layer, new DefaultItemLayerAnalyzer(states, (ItemLayer) layer, fileIndex));
				}

				//TODO also handle annotation layers
			});
		}

		return result.getAsMap();
	}

	/**
	 * @throws IcarusApiException
	 * @see de.ims.icarus2.filedriver.Converter#loadFile(int, de.ims.icarus2.model.standard.driver.ChunkConsumer)
	 */
	@Override
	public LoadResult loadFile(final int fileIndex, @Nullable final ChunkConsumer action)
			throws IOException, InterruptedException, IcarusApiException {
		checkAdded();

		@SuppressWarnings("resource")
		BlockHandler blockHandler = blockHandlerPool.get();

		List<LayerGroup> layerGroups = getGroups(blockHandler);

		// Collect basic caches
		Map<ItemLayer, InputCache> caches = createCaches(layerGroups, null);

		/*
		 * We need component suppliers that are connected to the caches, do not use mapping
		 * but instead rely on metadata information to get index values
		 */
		Map<ItemLayer, ComponentSupplier> componentSuppliers = new ComponentSuppliersFactory(this)
			.rootBlockHandler(blockHandler)
			.fileIndex(fileIndex)
			.memberFactory(getSharedMemberFactory())
			.mode(ReadMode.FILE)
			.consumers(layer -> caches.get(layer)::offer)
			.build();

		DynamicLoadResult loadResult = new SimpleLoadResult(caches.values());

		final ObjLongConsumer<Item> topLevelItemAction = (item, index) -> {

			/*
			 *  It's the drivers responsibility to set proper flags on an item, so we do it here.
			 *
			 *  Our assumption is that subclasses or factory code that gets called from within
			 *  the creation process already assigns other flags than ALIVE. Therefore this is
			 *  the only flag we need to worry about here. It is also the only one that MUST be
			 *  set in order for a blank item to get assigned VALID chunk state!
			 */
			if(item instanceof ManagedItem) {
				((ManagedItem)item).setAlive(true);
			}

			ChunkState state = ChunkState.forItem(item);
			loadResult.accept(index, item, state);

			// Report loaded item and state (if required)
			if(action!=null) {
				action.accept(index, item, state);
			}
		};

		InputResolverContext inputContext = new InputResolverContext(componentSuppliers, topLevelItemAction);
		ItemLayer primaryLayer = blockHandler.getItemLayer();
		long index = 0L;

		LockableFileObject fileObject = getDriver().getFileObject(fileIndex);

		// Notify stack about incoming read operation
		blockHandler.prepareForReading(this, ReadMode.FILE, caches::get);

		try(ReadableByteChannel channel = fileObject.getResource().getReadChannel()) {

			LineIterator lines = new BufferedLineIterator(channel, encoding, characterChunkSize);

			/*
			 *  Continue as long as there's still content in the file.
			 *  We need both checks since it's possible that while reading the
			 *  previous block we had to do some lookahead but didn't consume the
			 *  current line.
			 */
			while(lines.hasLine() || lines.next()) {

				// Clear state of handler and context
				blockHandler.reset();
				inputContext.reset();

				// Point the context to the layer's proxy container and requested index
				inputContext.setContainer(primaryLayer.getProxyContainer());
				inputContext.setIndex(index);

				// Delegate actual conversion work (which will advance lines on its own)
				blockHandler.readChunk(lines, inputContext);

				// Make sure we advance 1 line in case the last one has been consumed as content
				if(inputContext.isDataConsumed()) {
					lines.next();
				}

				index = inputContext.currentIndex();

				// Next chunk
				index++;
			}
		} finally {
			blockHandler.close();
			blockHandlerPool.recycle(blockHandler);
		}

		return loadResult;
	}

	@Override
	protected Item readItemFromCursor(DelegatingCursor<?> cursor)
			throws IOException, InterruptedException, IcarusApiException {

		DelegatingTableCursor tableCursor = (DelegatingTableCursor) cursor;
		tableCursor.fillCharacterBuffer();
		BlockHandler blockHandler = tableCursor.getRootBlockHandler();

		try {
			InputResolverContext inputContext = tableCursor.getContext();
			LineIterator lines = tableCursor.getLineIterator();
			if(!lines.next())
				throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
						"Cannot create item from empty input");

			// Clear state of context
			inputContext.reset();

			// Point the context to the layer's proxy container and requested index
			inputContext.setContainer(tableCursor.getLayer().getProxyContainer());
			inputContext.setIndex(tableCursor.getCurrentIndex());

			blockHandler.readChunk(lines, inputContext);

			return inputContext.currentItem();
		} finally {
			blockHandler.reset();
		}
	}

	protected <L extends Layer> L findLayer(String layerId) {
		return getDriver().getContext().getLayer(layerId);
	}

	private static void advanceLine(LineIterator lines, InputResolverContext context) {
		if(!lines.next())
			throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT, "Unexpected end of input");
		context.setData(lines.getLine());
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#createDelegatingCursor(int, de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	protected DelegatingTableCursor createDelegatingCursor(int fileIndex,
			ItemLayer itemLayer) {
		BlockHandler blockHandler = blockHandlerPool.get();

		if(itemLayer!=blockHandler.getItemLayer())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.mismatch("Unexpected layer",
							ManifestUtils.getName(itemLayer),
							ManifestUtils.getName(blockHandler.getItemLayer())));

		// Collect basic caches
		Map<ItemLayer, InputCache> caches = createCaches(getGroups(blockHandler), null);

		/*
		 * We need component suppliers that are connected to the caches, use proper mapping,
		 * do not query metadata to determine index values and which delegate to the default
		 * member factory.
		 */
		Map<ItemLayer, ComponentSupplier> componentSuppliers = new ComponentSuppliersFactory(this)
			.rootBlockHandler(blockHandler)
			.memberFactory(getSharedMemberFactory())
			.fileIndex(fileIndex)
			.mode(ReadMode.CHUNK)
			.consumers(layer -> caches.get(layer)::offer)
			.build();

		InputResolverContext inputContext = new InputResolverContext(componentSuppliers, null); //TODO verify that we don't need a dedicated action for top-level items here!

		// Result instance that is linked to our caches
		DynamicLoadResult loadResult = new SimpleLoadResult(caches.values());

		return new TableCursorBuilder(this)
				.file(getDriver().getFileObject(fileIndex))
				.rootBlockHandler(blockHandler)
				.primaryLayer(itemLayer)
				.context(inputContext)
				.loadResult(loadResult)
				.encoding(encoding)
				.characterChunkSize(characterChunkSize)
				.caches(caches::get)
				.chunkIndex(getDriver().getChunkIndex(itemLayer))
				.build();
	}

	/**
	 * Creates a lookup map to fetch caches for every item layer that is either contained in the specified
	 * layer group or (indirectly) referenced by layers in the group and which is also a member of the same
	 * context.
	 *
	 * @param group
	 * @param cacheGen
	 * @return
	 */
	protected Map<ItemLayer, InputCache> createCaches(List<LayerGroup> groups, BiFunction<ItemLayer, Graph<Layer>, InputCache> cacheGen) {

		final Map<ItemLayer, InputCache> caches = new Object2ObjectOpenHashMap<>();

		for(LayerGroup group : groups) {
			final Context context = group.getContext();

			// Find all (indirect) dependencies of the group within entire context
			final Graph<Layer> graph = Graph.layerGraph(group, Graph.layersForContext(context));

			graph.walkGraph(group.getLayers(), false, layer -> {

				// Only care about item layers that are located within our current context
				if(layer.getContext()==context && (ModelUtils.isItemLayer(layer))) {
					ItemLayer itemLayer = (ItemLayer)layer;
					InputCache cache;

					/*
					 *  If we're supplied a cache generator function -> use it
					 *
					 *  Otherwise we'll create a link to related annotation
					 *  layers (storages) that should get cleaned when a cache
					 *  gets discarded before committing.
					 */
					if(cacheGen==null) {
						Consumer<InputCache> cleanupAction = new AnnotationCleaner()
								.addStoragesFromLayers(graph.incomingNodes(itemLayer));

						/*
						 * Let driver component decide on actual cache implementation.
						 *
						 * We request optimistic caches here so that the linking of freshly
						 * loaded items/containers can be done directly.
						 */
						cache = getDriver().getLayerBuffer(itemLayer).newCache(cleanupAction, true);
					} else {
						cache = cacheGen.apply(itemLayer, graph);
					}
					caches.put(itemLayer, cache);

				}

				return true;
			});
		}

		return caches;
	}

	public static class AnnotationCleaner implements Consumer<InputCache> {
		private final Set<AnnotationStorage> storages = new WeakHashSet<>();

		public AnnotationCleaner() {
			// no-op
		}

		public AnnotationCleaner(Collection<? extends AnnotationStorage> storages) {
			this.storages.addAll(storages);
		}

		public AnnotationCleaner addStoragesFromLayers(Collection<? extends Layer> layers) {
			for(Layer layer : layers) {
				if(ModelUtils.isAnnotationLayer(layer)) {
					addStorage(((AnnotationLayer)layer).getAnnotationStorage());
				}
			}
			return this;
		}

		public AnnotationCleaner addStorage(AnnotationStorage storage) {
			storages.add(storage);
			return this;
		}

		/**
		 * @see java.util.function.Consumer#accept(java.lang.Object)
		 */
		@Override
		public void accept(InputCache cache) {
			for(AnnotationStorage storage : storages) {
				storage.removeAllValues(cache.pendingItemIterator());
			}
		}
	}

	public static class AnalyzingInputCache implements InputCache, ItemLookup {

		private final List<Item> items = new ObjectArrayList<>(100);
		private final LongList indices = new LongArrayList(100);

		private final ItemLayerAnalyzer analyzer;

		private final Consumer<? super InputCache> cleanupAction;

		/**
		 * @param analyzer
		 */
		public AnalyzingInputCache(ItemLayerAnalyzer analyzer, Consumer<? super InputCache> cleanupAction) {
			requireNonNull(analyzer);

			this.analyzer = analyzer;
			this.cleanupAction = cleanupAction;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#offer(de.ims.icarus2.model.api.members.item.Item, long)
		 */
		@Override
		public void offer(Item item, long index) {
			items.add(item);
			indices.add(index);
		}

		@Override
		public long getItemCount() {
			return indices.isEmpty() ? 0L : indices.getLong(indices.size()-1) + 1;
		}

		@Override
		public Item getItemAt(long index) {
			int localIndex = indices.indexOf(index);
			if(localIndex==UNSET_INT)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Item alread purged from cache or invalid index: "+index);

			return items.get(localIndex);
		}

		@Override
		public long indexOfItem(Item item) {
			int localIndex = items.indexOf(item);
			if(localIndex==UNSET_INT)
				throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Item alread purged from cache or unknown item: "+item);
			return indices.getLong(localIndex);
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#hasPendingEntries()
		 */
		@Override
		public boolean hasPendingEntries() {
			return !items.isEmpty();
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#forEach(java.util.function.ObjLongConsumer)
		 */
		@Override
		public void forEach(ObjLongConsumer<Item> action) {
			int size = items.size();
			for(int i=0; i<size; i++) {
				action.accept(items.get(i), indices.getLong(i));
			}
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#pendingItemIterator()
		 */
		@Override
		public Iterator<Item> pendingItemIterator() {
			return items.iterator();
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#discard()
		 */
		@Override
		public int discard() {
			int size = items.size();

//			System.out.printf("discarding items %s for indices %s in cache %s%n", items, indices, this);

			try {
				if(cleanupAction!=null) {
					cleanupAction.accept(this);
				}
			} finally {
				items.clear();
				indices.clear();
			}

			return size;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#commit()
		 */
		@Override
		public int commit() {
			// Perform analysis
			forEach(analyzer);
			return items.size();
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#reset()
		 */
		@Override
		public int reset() {
			int size = items.size();
			items.clear();
			indices.clear();
			return size;
		}
	}

	/**
	 * Specialized version of a delegating cursor. This implementation manages the
	 * decoding and buffering of byte chunks in the raw data to a buffer of characters
	 * accessible to the actual conversion process.
	 * <p>
	 * Each cursor of this class has its own instance of {@link InputResolverContext}
	 * that carries the main information and state during conversion.
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected static class DelegatingTableCursor extends DelegatingCursor<TableConverter> {

		// Decoding process

		private final CharsetDecoder decoder;
		private final ByteBuffer bb;
		private final CharBuffer cb;
		private final StringBuilder characterBuffer;

		// "Parsing" process
		private final SubSequenceLineIterator lineIterator;

		// Conversion controller
		private final BlockHandler rootBlockHandler;

		// Externalized state of the conversion process
		private final InputResolverContext context;

		public DelegatingTableCursor(TableCursorBuilder builder) {
			super(builder);

			rootBlockHandler = builder.getRootBlockHandler();
			context = builder.getContext();

			characterBuffer = new StringBuilder(builder.getCharacterChunkSize());
			decoder = builder.getEncoding().newDecoder();

			bb = ByteBuffer.allocateDirect(IOUtil.DEFAULT_BUFFER_SIZE);
			cb = CharBuffer.allocate(IOUtil.DEFAULT_BUFFER_SIZE);

			lineIterator = new SubSequenceLineIterator(characterBuffer);

			rootBlockHandler.prepareForReading(builder.getConverter(), ReadMode.CHUNK, builder.getCaches());
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.DelegatingCursor#doClose()
		 */
		@Override
		protected void doClose() throws IOException {
			super.doClose();

			lineIterator.close();
			context.close();

			rootBlockHandler.close();
			getConverter().blockHandlerPool.recycle(rootBlockHandler);
		}

		public void clearCharacterBuffer() {
			characterBuffer.setLength(0);
		}

		/**
		 * Reads the entire content of the internal channel into the
		 * {@link StringBuilder character-buffer}. The returned value is the total number
		 * of characters read.
		 *
		 * @param channel
		 * @return
		 * @throws IOException
		 */
		public int fillCharacterBuffer() throws IOException {

			clearCharacterBuffer();
			int count = 0;
			boolean eos = false;

		    while(!eos) {
		    	cb.clear();
				eos = (-1 == getBlockChannel().read(bb));

			    bb.flip();
			    CoderResult res = decoder.decode(bb, cb, eos);
			    if(res.isError())
			    	res.throwException();

			    if(res.isUnderflow()) {
			    	bb.compact();
			    	if(eos) {
			    		decoder.flush(cb);
			    	}
			    }
			    cb.flip();
	        	count += cb.remaining();
	        	characterBuffer.append(cb);

//		        if(-1 == getBlockChannel().read(bb)) {
//		            decoder.decode(bb, cb, true);
//		            decoder.flush(cb);
//		        	cb.flip();
//		        	count += cb.remaining();
//		            break;
//		        }
//		        bb.flip();
//
//		        CoderResult res = decoder.decode(bb, cb, false);
//		        if(res.isError())
//		        	res.throwException();
//
//		        if(res.isUnderflow()) {
//		        	bb.compact();
//		        }
//
//	        	cb.flip();
//	        	count += cb.remaining();
//	        	characterBuffer.append(cb);
//	            cb.clear();
		    }

		    lineIterator.reset();

		    return count;
		}

		public StringBuilder getCharacterBuffer() {
			return characterBuffer;
		}

		public CharsetDecoder getDecoder() {
			return decoder;
		}

		public SubSequenceLineIterator getLineIterator() {
			return lineIterator;
		}

		public InputResolverContext getContext() {
			return context;
		}

		public BlockHandler getRootBlockHandler() {
			return rootBlockHandler;
		}
	}

	public static class TableCursorBuilder extends CursorBuilder<TableCursorBuilder, DelegatingTableCursor> {

		private BlockHandler rootBlockHandler;
		private InputResolverContext context;
		private Charset encoding;
		private int characterChunkSize=0;
		private Function<ItemLayer, InputCache> caches;

		public TableCursorBuilder(TableConverter converter) {
			super(converter);
		}

		public TableCursorBuilder rootBlockHandler(BlockHandler rootBlockHandler) {
			requireNonNull(rootBlockHandler);
			checkState("Root block handler already set", this.rootBlockHandler==null);

			this.rootBlockHandler = rootBlockHandler;

			return thisAsCast();
		}

		public TableCursorBuilder context(InputResolverContext context) {
			requireNonNull(context);
			checkState("Context already set", this.context==null);

			this.context = context;

			return thisAsCast();
		}

		public TableCursorBuilder encoding(Charset encoding) {
			requireNonNull(encoding);
			checkState("Encoding already set", this.encoding==null);

			this.encoding = encoding;

			return thisAsCast();
		}

		public TableCursorBuilder characterChunkSize(int characterChunkSize) {
			checkArgument("Character chunk size must be positive", characterChunkSize>0);
			checkState("Character chunk size already set", this.characterChunkSize==0);

			this.characterChunkSize = characterChunkSize;

			return thisAsCast();
		}

		public TableCursorBuilder caches(Function<ItemLayer, InputCache> caches) {
			requireNonNull(caches);
			checkState("Caches already set", this.caches==null);

			this.caches = caches;

			return thisAsCast();
		}

		public BlockHandler getRootBlockHandler() {
			checkState("Root block handler missing", rootBlockHandler!=null);

			return rootBlockHandler;
		}

		public InputResolverContext getContext() {
			checkState("Context missing", context!=null);

			return context;
		}

		public Charset getEncoding() {
			checkState("Encoding missing", encoding!=null);

			return encoding;
		}

		public int getCharacterChunkSize() {
			checkState("Character chunk size missing", characterChunkSize>0);

			return characterChunkSize;
		}

		public Function<ItemLayer, InputCache> getCaches() {
			checkState("Caches missing", caches!=null);

			return caches;
		}

		/**
		 * @see de.ims.icarus2.filedriver.AbstractConverter.CursorBuilder#validate()
		 */
		@Override
		protected void validate() {
			super.validate();

			checkState("Root block handler missing", rootBlockHandler!=null);
			checkState("Context missing", context!=null);
			checkState("Encoding missing", encoding!=null);
			checkState("Character chunk size missing", characterChunkSize>0);
			checkState("Caches missing", caches!=null);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected DelegatingTableCursor create() {
			return new DelegatingTableCursor(this);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected static final class UnresolvedAttribute {
		private final String data;
		private final AttributeTarget target;
		private final Resolver resolver;

		public UnresolvedAttribute(String data, AttributeTarget target, Resolver resolver) {
			requireNonNull(data);
			requireNonNull(target);
			requireNonNull(resolver);

			this.data = data;
			this.target = target;
			this.resolver = resolver;
		}

		public String getData() {
			return data;
		}

		public AttributeTarget getTarget() {
			return target;
		}

		public Resolver getResolver() {
			return resolver;
		}
	}

	/**
	 * Models a task-specific environment for the conversion process.
	 * In addition of implementing the {@link ResolverContext} interface
	 * this class stores {@link ComponentSupplier} instances to be used for
	 * each layer. Since depending on the {@link ReadMode mode} of a load
	 * operation we require different ways of handling things like component
	 * creation, caching or analysis.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class InputResolverContext implements ResolverContext, AutoCloseable {

		private Container container;
		private long index;
		private Item item;
		private Item pendingItem;
		private CharSequence data;

		private boolean consumed;

		private final Map<String, Item> namedSubstitues = new Object2ObjectOpenHashMap<>();

		private int columnIndex;

		private ObjectArrayList<Item> replacements = new ObjectArrayList<>();

		private List<UnresolvedAttribute> pendingAttributes = new ArrayList<>();

		private final Map<ItemLayer, ComponentSupplier> componentSuppliers;

		private final ObjLongConsumer<? super Item> topLevelAction;

		public InputResolverContext(Map<ItemLayer, ComponentSupplier> componentSuppliers, ObjLongConsumer<? super Item> topLevelAction) {
			this.componentSuppliers = requireNonNull(componentSuppliers);
			this.topLevelAction = topLevelAction;
		}

		void reset() {
			container = null;
			item = null;
			pendingItem = null;
			namedSubstitues.clear();
			replacements.clear();
			pendingAttributes.clear();
		}

		/**
		 * @see java.lang.AutoCloseable#close()
		 */
		@Override
		public void close() {
			reset();

			componentSuppliers.values().forEach(ComponentSupplier::close);
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.ResolverContext#currentContainer()
		 */
		@Override
		public Container currentContainer() {
			return container;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.ResolverContext#currentIndex()
		 */
		@Override
		public long currentIndex() {
			return index;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.ResolverContext#currentItem()
		 */
		@Override
		public Item currentItem() {
			return item;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.ResolverContext#rawData()
		 */
		@Override
		public CharSequence rawData() {
			return data;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.resolve.ResolverContext#consumeData()
		 */
		@Override
		public void consumeData() {
			consumed = true;
		}

		public void setData(CharSequence newData) {
			requireNonNull(newData);
			data = newData;
			consumed = false;
		}

		public void setContainer(Container container) {
			requireNonNull(container);
			this.container = container;
		}

		public void setIndex(long index) {
			this.index = index;
		}

		public void setItem(Item item) {
			requireNonNull(item);

			this.item = item;
			replacements.clear();
		}

		public int getColumnIndex() {
			return columnIndex;
		}

		public void setColumnIndex(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		public void replaceCurrentItem(Item newItem) {
			requireNonNull(newItem);

			replacements.push(item);
			item = newItem;
		}

		public void clearLastReplacement() {
			item = replacements.pop();
		}

		public void mapItem(String name, Item item) {
			requireNonNull(item);
			namedSubstitues.put(name, item);
		}

		public Item getItem(String name) {
			Item item = namedSubstitues.get(name);
			if(item==null)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"No item stored for name: "+name);

			return item;
		}

		public Item getPendingItem() {
			return pendingItem;
		}

		public void setPendingItem(Item pendingItem) {
			requireNonNull(pendingItem);
			checkState(this.pendingItem==null);
			this.pendingItem = pendingItem;
		}

		public void clearPendingItem() {
			pendingItem = null;
		}

		public void clearSubstitutes() {
			namedSubstitues.clear();
		}

		public void addUnresolvedAttribute(String data, AttributeTarget target, Resolver resolver) {
			pendingAttributes.add(new UnresolvedAttribute(data, target, resolver));
		}

		public List<UnresolvedAttribute> getUnresolvedAttributes(AttributeTarget target) {
			LazyCollection<UnresolvedAttribute> result = LazyCollection.lazyList();

			pendingAttributes.forEach(a -> {
				if(a.getTarget()==target) {
					result.add(a);
				}
			});

			return result.getAsList();
		}

		public boolean isDataConsumed() {
			return consumed;
		}

		public ComponentSupplier getComponentSupplier(ItemLayer layer) {
			return componentSuppliers.get(layer);
		}

		@Override
		public ObjLongConsumer<? super Item> getTopLevelAction() {
			return topLevelAction;
		}
	}

	/**
	 * Basic processor for conversion process that is able to read
	 * content from a given {@link InputResolverContext context} and
	 * produces a result.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <O> result type of the processing step
	 */
	@FunctionalInterface
	public interface ContextProcessor<O extends Object> extends Closeable {
		O process(InputResolverContext context) throws IcarusApiException;

		default void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches) {
			// no-op
		}

		@Override
		default void close() {
			// no-op
		}
	}

	/**
	 * Results of a line scan.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public enum ScanResult {

		/**
		 * Line was a successful match and the matcher isn't expecting any more input.
		 */
		MATCHED,

		/**
		 * Line was a successful match, but the matcher could consume more input.
		 */
		PARTLY_MATCHED,

		/**
		 * Line did not match. In the case of a previously returned {@link #PARTLY_MATCHED}
		 * result this indicates that the last line indeed was the end matched by
		 * the matcher.
		 */
		FAILED,
		;
	}

	private ContextProcessor<ScanResult> createProcessor(AttributeSchema attributeSchema) {
		requireNonNull(attributeSchema);

		ContextProcessor<ScanResult> result = null;

		switch (attributeSchema.getPattern()) {
		case AttributeSchema.DELIMITER_EMPTY_LINE:
			result = new EmptyLineDelimiter(attributeSchema, false);
			break;

		case AttributeSchema.DELIMITER_EMPTY_LINES:
			result = new EmptyLineDelimiter(attributeSchema, true);
			break;

		default:
			break;
		}

		if(result==null) {
			result = new AttributeHandler(attributeSchema);
		}

		return result;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class AttributeHandler implements ContextProcessor<ScanResult> {

		private final AttributeSchema attributeSchema;

		private final Resolver resolver;
		private final Matcher matcher;

		public AttributeHandler(AttributeSchema attributeSchema) {
			requireNonNull(attributeSchema);

			this.attributeSchema = attributeSchema;
			resolver = createResolver(attributeSchema.getResolver());

			String pattern = attributeSchema.getPattern();

			// The pattern is meant to be matched against the beginning of an input line
			if(!pattern.startsWith("$")) {
				pattern = "$"+pattern;
			}

			matcher = Pattern.compile(pattern).matcher("");
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.ContextProcessor#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.filedriver.Converter.ReadMode, java.util.function.Function)
		 */
		@Override
		public void prepareForReading(Converter converter, ReadMode mode,
				Function<ItemLayer, InputCache> caches) {
			if(resolver!=null) {
				resolver.prepareForReading(converter, mode, caches, attributeSchema.getResolver().getOptions());
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.ContextProcessor#close()
		 */
		@Override
		public void close() {
			if(resolver!=null) {
				resolver.close();
			}
		}

		/**
		 * Check the given {@link ResolverContext#rawData() line} against the internal pattern
		 * and optionally send it to the resolver if present.
		 * @param context
		 * @return
		 */
		@Override
		public ScanResult process(InputResolverContext context) throws IcarusApiException {
			try {
				matcher.reset(context.rawData());
				if(matcher.find()) {
					if(resolver!=null) {
						resolver.process(context);
					}

					context.consumeData();

					return ScanResult.MATCHED;
				}

			} finally {
				// Don't hold references to foreign content
				matcher.reset("");
			}

			return ScanResult.FAILED;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return matcher.pattern().pattern();
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class EmptyLineDelimiter implements ContextProcessor<ScanResult> {

		private final AttributeSchema attributeSchema;
		private final boolean multiline;

		public EmptyLineDelimiter(AttributeSchema attributeSchema, boolean multiline) {
			requireNonNull(attributeSchema);

			this.attributeSchema = attributeSchema;
			this.multiline = multiline;
		}

		@Override
		public ScanResult process(InputResolverContext context) {
			boolean empty = context.rawData().length()==0;

			if(empty) {
				return multiline ? ScanResult.PARTLY_MATCHED : ScanResult.MATCHED;
			}

			return ScanResult.FAILED;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getClass().getSimpleName()+"@" + (multiline ?
					AttributeSchema.DELIMITER_EMPTY_LINES : AttributeSchema.DELIMITER_EMPTY_LINE);
		}
	}

	/**
	 * A scanner that reports {@link ScanResult#MATCHED} for all non-empty input
	 * and {@link ScanResult#FAILED} otherwise.
	 */
	public static final ContextProcessor<ScanResult> CONTENT_DETECTOR = (context) -> {
		return context.rawData().length()==0 ? ScanResult.FAILED : ScanResult.MATCHED;
	};

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class SubstituteAddHandler implements ContextProcessor<Item> {
		private final SubstituteSchema substituteSchema;
		private final String name;
		private final MemberType requiredType;

		public SubstituteAddHandler(SubstituteSchema substituteSchema) {
			requireNonNull(substituteSchema);
			checkArgument(substituteSchema.getType()==SubstituteType.ADDITION);

			this.substituteSchema = substituteSchema;

			name = substituteSchema.getName();
			requiredType = substituteSchema.getMemberType();
		}

		public SubstituteSchema getSubstituteSchema() {
			return substituteSchema;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.ContextProcessor#process(de.ims.icarus2.filedriver.schema.tabular.TableConverter.InputResolverContext)
		 */
		@Override
		public Item process(InputResolverContext context) {
			Item newItem = context.getPendingItem();
			if(newItem==null)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Missing new item");
			if(newItem.getMemberType()!=requiredType)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						Messages.mismatch("Invalid member-type of new item", requiredType, newItem.getMemberType()));

			context.mapItem(name, newItem);

			return newItem;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class SubstituteReplaceHandler implements ContextProcessor<Item> {
		private final SubstituteSchema substituteSchema;
		private final MemberType requiredType;

		public SubstituteReplaceHandler(SubstituteSchema substituteSchema) {
			requireNonNull(substituteSchema);
			checkArgument(substituteSchema.getType()==SubstituteType.REPLACEMENT);

			this.substituteSchema = substituteSchema;

			requiredType = substituteSchema.getMemberType();
		}

		public SubstituteSchema getSubstituteSchema() {
			return substituteSchema;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.ContextProcessor#process(de.ims.icarus2.filedriver.schema.tabular.TableConverter.InputResolverContext)
		 */
		@Override
		public Item process(InputResolverContext context) {
			Item newItem = context.getPendingItem();
			if(newItem==null)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						"Missing new item");
			if(newItem.getMemberType()!=requiredType)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						Messages.mismatch("Invalid member-type of new item", requiredType, newItem.getMemberType()));

			context.replaceCurrentItem(newItem);

			return newItem;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class SubstituteTargetHandler implements ContextProcessor<Item> {
		private final SubstituteSchema substituteSchema;
		private final String name;
		private final MemberType requiredType;

		public SubstituteTargetHandler(SubstituteSchema substituteSchema) {
			requireNonNull(substituteSchema);
			checkArgument(substituteSchema.getType()==SubstituteType.TARGET);

			this.substituteSchema = substituteSchema;

			name = substituteSchema.getName();
			requiredType = substituteSchema.getMemberType();
		}

		public SubstituteSchema getSubstituteSchema() {
			return substituteSchema;
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.ContextProcessor#process(de.ims.icarus2.filedriver.schema.tabular.TableConverter.InputResolverContext)
		 */
		@Override
		public Item process(InputResolverContext context) {
			Item item = context.getItem(name);
			// Item can't be null due to contract of getItem(String)
			if(item.getMemberType()!=requiredType)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						Messages.mismatch("Invalid member-type of stored item", requiredType, item.getMemberType()));

			return item;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	interface LineIterator {

		public static final String LINE_BREAK_1 = "\n";
		public static final String LINE_BREAK_2 = "\r\n";

		/**
		 * Tries to advance to the next line of text.
		 *
		 * @return
		 */
		boolean next();

		/**
		 * Returns the current line of text if available.
		 *
		 * @return
		 */
		CharSequence getLine();

		/**
		 * Returns {@code true} iff the most recent call to {@link #next()}
		 * yielded a success.
		 *
		 * @return
		 */
		boolean hasLine();

		/**
		 * Returns the number of characters that occurred in the last line-break.
		 * <p>
		 * Since this iterator recognizes 2 different types of actual line-breaks, there
		 * are in total 3 different possible return values:
		 * <table border="1">
		 * <tr><th>Count</th><th>Meaning</th></tr>
		 * <tr><td>0</td><td>No line-break, but end of character sequence reached</td></tr>
		 * <tr><td>1</td><td>Simple line-feed {@code \n}</td></tr>
		 * <tr><td>2</td><td>Carriage-return followed by line-feed {@code \r\n}</td></tr>
		 * </table>
		 * When scanning data this information should be sufficient to determine
		 * the number of bytes that was read (usually equal to the number of line-break
		 * characters).
		 *
		 * @return
		 */
		int getLineBreakCharacterCount();

		/**
		 * Optionally returns the current line number if the line iterator is able and configured
		 * to track lines. The result should either be {@code -1} (to indicate that no line has been
		 * read so far or tracking of lines is not supported) or an actual line number starting from
		 * {@code 1}.
		 *
		 * @return
		 */
		default long getLineNumber() {
			return -1L;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static class SubSequenceLineIterator extends FlexibleSubSequence implements LineIterator {

		/**
		 * Flag to signal the end of input sequence
		 */
		private boolean endOfInput = false;

		/**
		 * Flag to tell the {@link #hasNext()} method to advance a step
		 */
		private boolean lineReady = false;

		/**
		 * Number of characters involved in the last detected line break.
		 * Will be either {@code 1} or {@code 2}, depending on whether the
		 * last break only consisted of a line-feed character ({@code \n}) or
		 * the combination of a carriage-return followed directly by a
		 * line-feed character ({@code \r\n}).
		 */
		private int lineBreakCharacterCount;

		public SubSequenceLineIterator(CharSequence source) {
			setSource(source);
		}

		public void reset() {
			lineBreakCharacterCount = 0;
			endOfInput = false;
			lineReady = false;

			setRange(0, getSource().length()-1);
		}

		@Override
		public int getLineBreakCharacterCount() {
			return lineBreakCharacterCount;
		}

		/**
		 * Start at the specified offset and expand till a line-break occurs or the
		 * end of the underlying character sequence is reached..
		 * The number of characters involved in that line-break can be queried using
		 * {@link #getLineBreakCharacterCount()}.
		 */
		private void scan(int begin) {
			lineBreakCharacterCount = 0;

			final CharSequence cs = getSource();
			final int totalLength = cs.length();
			int cursor = begin;
			int length = 0;

			boolean expectLF = false;

			char_loop : while(cursor<totalLength) {
				char c = cs.charAt(cursor);

				switch (c) {
				case '\n': {
					lineBreakCharacterCount++;
					break char_loop;
				}

				case '\r': {
					expectLF = true;
					lineBreakCharacterCount++;
				} break;

				default:
					if(expectLF)
						throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
								"Unfinished line-break sequence - missing \\n after \\r character");

					length++;
					break;
				}

				cursor++;
			}

			// To allow empty lines we check for the total number of characters we saw
			if(length+lineBreakCharacterCount>0) {
				setOffset(begin);
				setLength(length);
				lineReady = true;
			} else {
				endOfInput = true;
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.LineIterator#hasLine()
		 */
		@Override
		public boolean hasLine() {
			return !endOfInput && lineReady;
		}

		@Override
		public CharSequence getLine() {
			if(endOfInput || !lineReady)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
						"End of input reached or cursor not advanced previously");

			return this;
		}

		@Override
		public boolean next() {
			if(!endOfInput) {
				int nextBegin = getOffset()+getLength()+lineBreakCharacterCount;
				scan(nextBegin);
			}

			return !endOfInput;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static class BufferedLineIterator implements LineIterator {

		/**
		 * Keeps track of number of read lines.
		 * Will be incremented every time we scan over a line break.
		 */
		private long lineNumber = -1L;

		private final ReadableByteChannel channel;

		private final CharsetDecoder decoder;

		private final CharBuffer cb;
		private final ByteBuffer bb;

		private final StringBuilder characterBuffer;

		/**
		 * Flag to signal the end of input character sequence
		 */
		private boolean endOfInput = false;

		/**
		 * Flag to tell the {@link #hasNext()} method to advance a step
		 */
		private boolean lineReady = false;

		/**
		 * Flag to signal end the end of underlying channel
		 */
		private boolean eos = false;

		/**
		 * Number of characters used to signal the last line-break.
		 */
		private int lineBreakCharacterCount;

		public BufferedLineIterator(ReadableByteChannel channel, Charset encoding, int characterChunkSize) {
			requireNonNull(channel);
			requireNonNull(encoding);
			checkArgument(characterChunkSize>0);

			this.channel = channel;

			characterBuffer = new StringBuilder(characterChunkSize);
			decoder = encoding.newDecoder();

			bb = ByteBuffer.allocateDirect(IOUtil.DEFAULT_BUFFER_SIZE);
			cb = CharBuffer.allocate(IOUtil.DEFAULT_BUFFER_SIZE);

			cb.flip();
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.LineIterator#getLineNumber()
		 */
		@Override
		public long getLineNumber() {
			return lineNumber==-1L ? -1L : lineNumber+1;
		}

		private boolean hasMoreCharacters() {
			if(!eos && !cb.hasRemaining()) {
				cb.clear();
		        try {
					if(-1 == channel.read(bb)) {
					    eos = true;
					}

				    bb.flip();
				    CoderResult res = decoder.decode(bb, cb, eos);
				    if(res.isError())
				    	res.throwException();

				    if(res.isUnderflow()) {
				    	bb.compact();
				    	if(eos) {
				    		decoder.flush(cb);
				    	}
				    }
				} catch (IOException e) {
					throw new ModelException(GlobalErrorCode.IO_ERROR, "Failed to read from channel", e);
				}
	        	cb.flip();
			}

			return cb.hasRemaining();
		}

		@Override
		public int getLineBreakCharacterCount() {
			return lineBreakCharacterCount;
		}

		/**
		 * Expands the internal {@link StringBuilder character buffer} till a line-break occurs or the
		 * end of the underlying character sequence is reached.
		 * The number of characters involved in that line-break can be queried using
		 * {@link #getLineBreakCharacterCount()}.
		 */
		private void scan() {
			lineBreakCharacterCount = 0;
			characterBuffer.setLength(0);

			boolean expectLF = false;

			char_loop : while(hasMoreCharacters()) {
				char c = cb.get();

				switch (c) {
				case '\n': {
					lineBreakCharacterCount++;
					break char_loop;
				}

				case '\r': {
					expectLF = true;
					lineBreakCharacterCount++;
				} break;

				default:
					if(expectLF)
						throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
								"Unfinished line-break sequence - missing \\n after \\r character");

					characterBuffer.append(c);
					break;
				}
			}

			// To allow empty lines we check for the total number of characters we saw
			if(characterBuffer.length()+lineBreakCharacterCount>0) {
				lineReady = true;
				lineNumber++;
			} else {
				endOfInput = true;
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.LineIterator#hasLine()
		 */
		@Override
		public boolean hasLine() {
			return !endOfInput && lineReady;
		}

		@Override
		public CharSequence getLine() {
			if(endOfInput || !lineReady)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE,
						"End of input reached or cursor not advanced previously");

			return characterBuffer;
		}

		@Override
		public boolean next() {
			if(!endOfInput) {
				scan();
//				if(hasLine())
//				System.out.printf("%d: %s\n",lineNumber+1,characterBuffer);
			}

			return !endOfInput;
		}

	}

	private static class ContainerSupplierProxy implements Supplier<Container> {

		private Supplier<Container> supplier;

		@Override
		public Container get() { return supplier.get(); }

		void setSupplier(Supplier<Container> supplier) {
			checkState("Supplier already set", this.supplier==null);
			this.supplier = requireNonNull(supplier);
		}
	}

	private static class BaseContainerSupplier implements Supplier<DataSet<Container>> {
		private final Supplier<Container>[] containerSuppliers;

		public BaseContainerSupplier(Supplier<Container>[] containerSuppliers) {
			this.containerSuppliers = containerSuppliers;
		}

		@Override
		public DataSet<Container> get() {
			Container[] containers = new Container[containerSuppliers.length];
			for (int i = 0; i < containers.length; i++) {
				containers[i] = containerSuppliers[i].get();
			}
			return DataSets.createDataSet(containers);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class ComponentSuppliersFactory extends AbstractBuilder<ComponentSuppliersFactory, Map<ItemLayer, ComponentSupplier>> {

		//FIXME change final mapping from block-id to manifest-UID and include ALL relevant layers, so that resolvers can dock on caches

		private Function<ItemLayer, ObjLongConsumer<Item>> consumers;

		private ReadMode mode;

		private LayerMemberFactory memberFactory;

		private Map<ItemLayer, ItemLookup> lookups;

		private BlockHandler rootBlockHandler;

		private int fileIndex = -1;

		private final TableConverter converter;

		public ComponentSuppliersFactory(TableConverter converter) {
			requireNonNull(converter);

			this.converter = converter;
		}

		public ComponentSuppliersFactory consumers(Function<ItemLayer, ObjLongConsumer<Item>> consumers) {
			requireNonNull(consumers);
			checkState("Consumers map already set", this.consumers==null);

			this.consumers = consumers;

			return thisAsCast();
		}

		public ComponentSuppliersFactory mode(ReadMode mode) {
			requireNonNull(mode);
			checkState("Mode already set", this.mode==null);

			this.mode = mode;

			return thisAsCast();
		}

		public ComponentSuppliersFactory fileIndex(int fileIndex) {
			checkArgument(fileIndex>=0);
			checkState("File index already set", this.fileIndex==-1);

			this.fileIndex = fileIndex;

			return thisAsCast();
		}

		public ComponentSuppliersFactory memberFactory(LayerMemberFactory memberFactory) {
			requireNonNull(memberFactory);
			checkState("Member factory already set", this.memberFactory==null);

			this.memberFactory = memberFactory;

			return thisAsCast();
		}

		public ComponentSuppliersFactory rootBlockHandler(BlockHandler rootBlockHandler) {
			requireNonNull(rootBlockHandler);
			checkState("Root block handler already set", this.rootBlockHandler==null);

			this.rootBlockHandler = rootBlockHandler;

			return thisAsCast();
		}

		public ComponentSuppliersFactory lookups(Map<ItemLayer, ItemLookup> lookups) {
			requireNonNull(lookups);
			checkState("Lookup already set", this.lookups==null);

			this.lookups = lookups;

			return thisAsCast();
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#constructor(java.util.function.Function)
		 */
		@Override
		public ComponentSuppliersFactory constructor(
				Function<ComponentSuppliersFactory, Map<ItemLayer, ComponentSupplier>> constructor) {
			throw new ModelException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Cannot accept constructor for array");
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#validate()
		 */
		@Override
		protected void validate() {
			checkState("Missing mode", mode!=null);
			checkState("Missing root block handler", rootBlockHandler!=null);
			checkState("Missing member factory", memberFactory!=null);

			if(mode==ReadMode.SCAN) {
				checkState("Missing lookup", lookups!=null);
			}
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected Map<ItemLayer, ComponentSupplier> create() {
			Map<ItemLayer, ComponentSupplier> componentSuppliers = new Object2ObjectOpenHashMap<>();
			Map<ItemLayer, Supplier<Container>> containerSuppliers = new Object2ObjectOpenHashMap<>();

			collectComponentSuppliers0(rootBlockHandler, componentSuppliers, containerSuppliers);

			return componentSuppliers;
		}

		private void collectComponentSuppliers0(BlockHandler blockHandler,
				Map<ItemLayer, ComponentSupplier> componentSuppliers,
				Map<ItemLayer, Supplier<Container>> containerSuppliers) {

			BlockHandler[] nestedHandlers = blockHandler.nestedBlockHandlers;
			if(nestedHandlers!=null) {
				for(BlockHandler nestedHandler : nestedHandlers) {
					collectComponentSuppliers0(nestedHandler, componentSuppliers, containerSuppliers);
				}
			}

			ItemLayer layer = blockHandler.getItemLayer();

			ComponentSupplier.Builder builder = ComponentSupplier.builder();
			builder.componentLayer(layer);
			builder.componentType(blockHandler.getSchema().getComponentSchema().getMemberType());
			builder.memberFactory(memberFactory);

			ObjLongConsumer<Item> consumer = consumers==null ? null : consumers.apply(layer);
			if(consumer!=null) {
				builder.componentConsumer(consumer);
			}

			if(!layer.getBaseLayers().isEmpty()) {
				/** Creates a supplier for elements of the specified layer */
				// Currently we only supply top-level elements, so the root container is sufficient
				//TODO add parameters to control whether nested containers are desired
				final Function<ItemLayer, Supplier<Container>> baseContainerCreator = itemLayer -> {
					final Container container;
					if(mode==ReadMode.SCAN) {
						// When scanning, items don't get actually stored persistently
						container = new TempContainer(lookups.get(itemLayer));
					} else {
						// For live reading we can already rely on the underlying storage to work
						container = itemLayer.getProxyContainer();
					}
					return () -> container;
				};

				@SuppressWarnings("unchecked")
				Supplier<Container>[] suppliers = new Supplier[layer.getBaseLayers().entryCount()];
				boolean dynamic = false;
				for (int i = 0; i < suppliers.length; i++) {
					Supplier<Container> supplier = containerSuppliers.computeIfAbsent(layer.getBaseLayers().entryAt(i), baseContainerCreator);
					suppliers[i] = supplier;
					dynamic |= supplier instanceof ContainerSupplierProxy;
				}

				if(dynamic) {
					// Requires dynamic construction of base containers
					builder.baseContainerSupplier(new BaseContainerSupplier(suppliers));
				} else {
					// Compute base layers once and then reuse the result
					final DataSet<Container> baseContainers = new BaseContainerSupplier(suppliers).get();
					builder.baseContainerSupplier(() -> baseContainers);
				}
			}

			// In SCAN mode we only read items, analyze and then immediately discard them
			if(mode==ReadMode.CHUNK) {

				BlockHandler parent = blockHandler.getParent();

				if(parent!=null) {
					ItemLayer sourceLayer = parent.getItemLayer();
					ItemLayer targetLayer = layer;
					Mapping mapping = converter.getDriver().getMapping(sourceLayer, targetLayer);
					if(mapping==null)
						throw new ModelException(ModelErrorCode.DRIVER_ERROR,
								"Missing mapping from "+ModelUtils.getUniqueId(sourceLayer)+" to "+ModelUtils.getUniqueId(targetLayer));
					/*
					 *  NOTE: this fetches the largest container size for the parent layer.
					 *  This might be significantly higher than the buffer sizer we need for
					 *  the given combination (parentLayer -> baseLayer) but is an easy upper
					 *  boundary and the overhead should be manageable.
					 */
					int bufferSize = converter.getRecommendedIndexBufferSize(sourceLayer.getManifest());

					builder.mapping(mapping);
					builder.bufferSize(bufferSize);
				}
			}

			// Outside of CHUNK mode we can use metadata to determine begin indices for our continuous item streams
			if(fileIndex!=-1 && mode!=ReadMode.CHUNK) {
				FileDataStates states = converter.getDriver().getFileStates();

				long firstIndex;

				FileInfo fileInfo = states.getFileInfo(fileIndex);
				firstIndex = fileInfo.getBeginIndex(layer.getManifest());
				if(firstIndex==IcarusUtils.UNSET_LONG && fileIndex>0) {
					FileInfo previousInfo = states.getFileInfo(fileIndex-1);
					firstIndex = previousInfo.getEndIndex(layer.getManifest()) + 1;
				}

				if(firstIndex==IcarusUtils.UNSET_LONG) {
					firstIndex = 0L;
				}


				if(mode==ReadMode.SCAN) {
					// In scan mode we can't predict max index
					MutableLong index = new MutableLong(firstIndex);
					builder.indexSupplier(index::getAndIncrement);
				} else {
					builder.firstIndex(firstIndex);
					// If available we use information about last index in file
					long lastIndex = fileInfo.getEndIndex(layer.getManifest());
					if(lastIndex!=IcarusUtils.UNSET_LONG) {
						builder.lastIndex(lastIndex);
					}
				}
			}

			ComponentSupplier componentSupplier = builder.build();
			componentSupplier.setHost(layer.getProxyContainer());

			componentSuppliers.put(blockHandler.getItemLayer(), componentSupplier);
		}

	}

	private static class TempContainer extends AbstractImmutableContainer {

		private final ItemLookup lookup;

		TempContainer(ItemLookup lookup) {
			this.lookup = requireNonNull(lookup);
		}

		@Override
		public ContainerType getContainerType() {
			return ContainerType.LIST;
		}

		@Override
		public ContainerManifestBase<?> getManifest() {
			return null;
		}

		@Override
		public DataSet<Container> getBaseContainers() {
			return DataSet.emptySet();
		}

		@Override
		public Container getBoundaryContainer() {
			return null;
		}

		@Override
		public boolean isItemsComplete() {
			return true;
		}

		@Override
		public long getItemCount() {
			return lookup.getItemCount();
		}

		@Override
		public Item getItemAt(long index) {
			return lookup.getItemAt(index);
		}

		@Override
		public long indexOfItem(Item item) {
			return lookup.indexOfItem(item);
		}

		@Override
		public Container getContainer() {
			return null;
		}

		@Override
		public long getIndex() {
			return UNSET_LONG;
		}

		@Override
		public long getId() {
			return UNSET_LONG;
		}

		@Override
		public boolean isAlive() {
			return true;
		}

		@Override
		public boolean isLocked() {
			return false;
		}

		@Override
		public boolean isDirty() {
			return false;
		}

		@Override
		public boolean isProxy() {
			return true;
		}


	}

	private class MappingHandler implements BatchResolver, IndexSet {

		private WritableMapping mapping, reverseMapping;
		private MappingWriter writer, reverseWriter;
		private final IndexBuffer buffer;
		private long sourceIndex;

		MappingHandler(WritableMapping mapping, WritableMapping reverseMapping) {
			this.mapping = mapping;
			this.reverseMapping = reverseMapping;
			buffer = new IndexBuffer(1024); //TODO better starting size?
		}

		public void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches) {
			if(mapping!=null) {
				writer = mapping.newWriter();
				writer.begin();
			}
			if(reverseMapping!=null) {
				reverseWriter = reverseMapping.newWriter();
				reverseWriter.begin();
			}
		}

		@Override
		public Item process(ResolverContext context) throws IcarusApiException {
			long targetIndex = context.currentItem().getIndex();
			buffer.add(targetIndex);
//			System.out.printf("adding target index: %d%n",_long(targetIndex));
			return null;
		}

		@Override
		public void beginBatch(ResolverContext context) {
			sourceIndex = context.currentContainer().getIndex();
//			System.out.printf("assigned source index: %d%n",_long(sourceIndex));
		}

		@Override
		public void endBatch(ResolverContext context) {
//			System.out.printf("mapping %s to %s%n",this, buffer);
			if(reverseWriter!=null) {
				reverseWriter.map(buffer, this);
			}
			if(writer!=null) {
				writer.map(this, buffer);
			}
			buffer.clear();
		}

		@Override
		public String toString() {
			return String.valueOf(sourceIndex);
		}

		@Override
		public void close() {
			if(reverseWriter!=null) {
				reverseWriter.end();
				reverseWriter.close();
				reverseWriter = null;
			}
			if(writer!=null) {
				writer.end();
				writer.close();
				writer = null;
			}
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public long indexAt(int index) {
			checkArgument(index==0);
			return sourceIndex;
		}

		@Override
		public IndexValueType getIndexValueType() {
			return IndexValueType.LONG;
		}

		@Override
		public boolean isSorted() {
			return true;
		}

		@Override
		public boolean sort() {
			return true;
		}

		@Override
		public IndexSet subSet(int fromIndex, int toIndex) {
			return this;
		}

		@Override
		public IndexSet externalize() {
			return new SingletonIndexSet(sourceIndex);
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class ColumnHandler implements ContextProcessor<Void> {

		private final BlockHandler blockHandler;
		private final ColumnSchema columnSchema;
		private final Resolver resolver;
		private Resolver replacementResolver;
		private final int columnIndex;

		/**
		 * Handler to be called on the provided item <b>before</b> the actual
		 * processing of raw data takes place.
		 * <p>
		 * Only possible type here is {@link SubstituteType#TARGET} in order to
		 * change the target for nested resolvers.
		 */
		private final ContextProcessor<Item> preprocessingHandler;

		/**
		 * Handler to be called on the result of {@link Resolver#process(ResolverContext)}
		 * <p>
		 * Can represent either {@link SubstituteType#ADDITION} or {@link SubstituteType#REPLACEMENT}
		 *
		 */
		private final ContextProcessor<Item> postprocessingHandler;

		/**
		 * Column-private label that signals an "empty" entry. Only relevant if
		 * the surrounding {@link BlockSchema block} has no global {@link BlockSchema#getNoEntryLabel() one}
		 * declared.
		 */
		private final String noEntryLabel;

		public ColumnHandler(BlockHandler blockHandler, ColumnSchema columnSchema, int columnIndex) {
			requireNonNull(blockHandler);
			requireNonNull(columnSchema);

			this.blockHandler = blockHandler;
			this.columnSchema = columnSchema;
			this.columnIndex = columnIndex;

			// Per default try to instantiate nested resolver based on schema definition
			Resolver resolver = createResolver(columnSchema.getResolver());

			if(resolver==null) {
				// No nested resolver declared -> use layerId and annotationKey info
				AnnotationLayer layer = findLayer(columnSchema.getLayerId());
				String key = columnSchema.getAnnotationKey();
				if(key==null) {
					key = layer.getManifest().getDefaultKey().orElseThrow(
							ModelException.create(ModelErrorCode.DRIVER_ERROR, "Must define either a custom resolver, "
									+ "annotation key or the designated layer needs a defaultKey assigned"));
				}
				resolver = BasicAnnotationResolver.forAnnotation(layer, key);
			}

			this.resolver = resolver;

			ContextProcessor<Item> preprocessingHandler = null;
			ContextProcessor<Item> postprocessingHandler = null;
			SubstituteSchema substituteSchema;

			if((substituteSchema = columnSchema.getSubstitute(SubstituteType.TARGET))!=null) {
				preprocessingHandler = new SubstituteTargetHandler(substituteSchema);
			}

			if((substituteSchema = columnSchema.getSubstitute(SubstituteType.REPLACEMENT))!=null) {
				postprocessingHandler = new SubstituteReplaceHandler(substituteSchema);
			} else if((substituteSchema = columnSchema.getSubstitute(SubstituteType.ADDITION))!=null) {
				postprocessingHandler = new SubstituteAddHandler(substituteSchema);
			}

			this.preprocessingHandler = preprocessingHandler;
			this.postprocessingHandler = postprocessingHandler;

			noEntryLabel = columnSchema.getNoEntryLabel();
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.ContextProcessor#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.filedriver.Converter.ReadMode, java.util.function.Function)
		 */
		@Override
		public void prepareForReading(Converter converter, ReadMode mode,
				Function<ItemLayer, InputCache> caches) {
			resolver.prepareForReading(converter, mode, caches, resolverOptions(columnSchema.getResolver()));
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.tabular.TableConverter.ContextProcessor#close()
		 */
		@Override
		public void close() {
			resolver.close();
		}

		public boolean isBatchHandler() {
			return (resolver instanceof BatchResolver);
		}

		@Override
		public Void process(InputResolverContext context) throws IcarusApiException {
			if(noEntryLabel==null || !StringUtil.equals(noEntryLabel, context.rawData())) {
				// Original item
				Item providedItem = context.currentItem();

				// Flag for reversing temporary target changes
				boolean clearReplacementAfterResolver = false;

				if(preprocessingHandler!=null) {
					// Replacement item (e.g. change of target)
					Item preprocessedItem = preprocessingHandler.process(context);
					if(preprocessedItem!=providedItem) {
						context.replaceCurrentItem(preprocessedItem);
						clearReplacementAfterResolver = true;
						providedItem = preprocessedItem;
					}
				}

				// Delegate actual work to foreign resolver
				Item processedItem = resolver.process(context);

				// If we had a change of target earlier we need to reverse that
				if(clearReplacementAfterResolver) {
					context.clearLastReplacement();
				}

				// Opportunity to add new items to context
				if(postprocessingHandler!=null && processedItem!=providedItem) {
					context.setPendingItem(processedItem);
					try {
						processedItem = postprocessingHandler.process(context);
					} finally {
						context.clearPendingItem();
					}
				}
			}

			// Mandatory null return so we can have this class be an implementation of the ContextProcessor interface
			return null;
		}

		public void startBatch(InputResolverContext context) {
			((BatchResolver)resolver).beginBatch(context);
		}

		public void endBatch(InputResolverContext context) {
			((BatchResolver)resolver).endBatch(context);
		}

		public int getColumnIndex() {
			return columnIndex;
		}

		public BlockHandler getBlockHandler() {
			return blockHandler;
		}

		public ColumnSchema getColumnSchema() {
			return columnSchema;
		}

		private Options resolverOptions(ResolverSchema resolver) {
			Options options = new Options();
			if(resolver!=null) {
				options.putAll(resolver.getOptions());
			}
			options.put(ResolverOptions.LAYER, findLayer(columnSchema.getLayerId()));
			return options;
		}
	}

	private static String getSeparator(BlockHandler blockSchema, TableSchema tableSchema) {
		String separator = null;

		while(blockSchema!=null && (separator = blockSchema.getSchema().getSeparator())==null) {
			blockSchema = blockSchema.getParent();
		}

		if(separator==null) {
			separator = tableSchema.getSeparator();
		}

		return separator;
	}

	private static void closeProcessor(ContextProcessor<?> processor) {
		if(processor!=null) {
			processor.close();
		}
	}

	private static void prepareProcessorForReading(ContextProcessor<?> processor,
			Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches) {
		if(processor!=null) {
			processor.prepareForReading(converter, mode, caches);
		}
	}

	/**
	 * Controller
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class BlockHandler implements Closeable {
		private final int blockId;

		private final BlockHandler parent;

		private final BlockSchema blockSchema;

		private final ItemLayer itemLayer;

		private final LayerGroup[] externalGroups;

		private final BlockHandler[] nestedBlockHandlers;

		/**
		 * All the column handlers in the order of their respective
		 * {@link ColumnSchema} declarations.
		 * <p>
		 * NOTE:
		 * Handlers for columns that declare substitutes with a type
		 * other than {@link SubstituteType#TARGET} will be executed
		 * <b>before</b> any of the regular ones!
		 * Also columns that are declared to be ignored by the converter
		 * are not included in this array.
		 */
		private final ColumnHandler[] columnHandlers;
		private final int requiredColumnCount;

		private final ContextProcessor<Void> fallbackHandler;

		private final MappingHandler mappingHandler;

		/**
		 * Subset of column handlers that work in batch mode
		 */
		private final ColumnHandler[] batchHandlers;

		/**
		 * Any return value other than {@link ScanResult#FAILED} will
		 * signal a valid content line.
		 */
		private final ContextProcessor<ScanResult> beginDelimiter;

		/**
		 * Any return value other than {@link ScanResult#FAILED} will
		 * signal a non-content line that is to be ignored
		 */
		private final ContextProcessor<ScanResult> endDelimiter;

		/**
		 * Any return value other than {@link ScanResult#FAILED} will
		 * signal a non-content line that contained metadata or additional
		 * attributes.
		 */
		private final ContextProcessor<ScanResult>[] attributeHandlers;

		/**
		 * Temporarily saved handler that produced the last partial match.
		 * Will be granted priority when checking for attribute lines.
		 */
		private ContextProcessor<ScanResult> pendingAttributeHandler; //FIXME

		/**
		 * Cursor to delegate parts of the line's raw data to nested column handlers.
		 */
		private final FlexibleSubSequence characterCursor = new FlexibleSubSequence();

		/**
		 * Matcher used to detect splits between column content
		 */
		private final Matcher separatorMatcher;
		private final char separatorChar;

		private final boolean trimWhitespaces;
		private final String noEntryLabel;

		/**
		 * For every column stores the character based begin and end offset as
		 * reported by the separatorMatcher splitting.
		 * <p>
		 * Will be initialized with a size twice the number of columns declared for
		 * the block. In case there is a fallback column declaration, the buffer is
		 * gradually increased to fit additional offsets.
		 */
		private int[] columnOffsets;

		public BlockHandler(BlockSchema blockSchema) {
			this(blockSchema, null, new MutableInteger(0));
		}

		private BlockHandler(BlockSchema blockSchema, @Nullable BlockHandler parent, MutableInteger idGen) {
			requireNonNull(blockSchema);

			this.parent = parent;
			this.blockSchema = blockSchema;

			final ColumnSchema[] columnSchemas = blockSchema.getColumns();
			final BlockSchema[] nestedBlockSchemas = blockSchema.getNestedBlocks();

			blockId = idGen.getAndIncrement();

			itemLayer = findLayer(blockSchema.getLayerId());

			final String[] externalGroupIds = blockSchema.getExternalGroupIds();
			if(externalGroupIds.length>0) {
				Map<String, LayerGroup> groups = getDriver().getContext().getLayerGroups().stream()
						.collect(Collectors.toMap(g -> ManifestUtils.requireId(g.getManifest()), g -> g));
				externalGroups = Stream.of(blockSchema.getExternalGroupIds())
						.map(groups::get)
						.toArray(LayerGroup[]::new);
			} else {
				externalGroups = new LayerGroup[0];
			}

			noEntryLabel = blockSchema.getNoEntryLabel();
			trimWhitespaces = blockSchema.getOptions().getBoolean("trimWhitespaces", false);

			// Process separator, which can either result in a single character or a complex Matcher instance
			Matcher separatorMatcher = null;
			char separatorChar = '\0';

			String separator = getSeparator(this, tableSchema);

			if(separator==null) {
				if(columnSchemas.length>0)
					throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Missing separator specification");
			} else {
				if(separator.length()==1) {
					separatorChar = separator.charAt(0);
				} else {
					switch (separator) {
					case TableSchema.SEPARATOR_SPACE:
						separatorChar = ' ';
						break;

					case TableSchema.SEPARATOR_TAB:
						separatorChar = '\t';
						break;

					case TableSchema.SEPARATOR_WHITESPACE:
						separator = "\\s";
						break;

					case TableSchema.SEPARATOR_WHITESPACES:
						separator = "\\s+";
						break;

					default:
						break;
					}

					if(separatorChar=='\0') {
						separatorMatcher = Pattern.compile(separator).matcher("");
					}
				}
			}

			this.separatorMatcher = separatorMatcher;
			this.separatorChar = separatorChar;

			// Begin delimiter is allowed to be omitted, in which case use the general CONTENT_DETECTOR to find non-empty lines
			if(blockSchema.getBeginDelimiter()==null) {
				beginDelimiter = CONTENT_DETECTOR;
			} else {
				beginDelimiter = createProcessor(blockSchema.getBeginDelimiter());
			}
			// End delimiter must always be declared explicitly
			endDelimiter = createProcessor(blockSchema.getEndDelimiter());

			// Column related stuff
			requiredColumnCount = columnSchemas.length;

			// Make sure we have enough space in our column offset buffer
			columnOffsets = new int[requiredColumnCount*2];

			List<ColumnHandler> regularHandlers = new ArrayList<>();
			List<ColumnHandler> batchHandlers = new ArrayList<>();

			mappingHandler = createMappingHandler(this, parent);

			for(int i=0; i<columnSchemas.length; i++) {
				ColumnSchema columnSchema = columnSchemas[i];
				if(columnSchema.isIgnoreColumn()) {
					continue;
				}

				ColumnHandler handler = new ColumnHandler(this, columnSchema, i);
				if(handler.isBatchHandler()) {
					batchHandlers.add(handler);
				}
				regularHandlers.add(handler);
			}

			if(!blockSchema.isColumnOrderFixed()) {
				//	TODO sort regular handlers based on whether they declare substitutes etc
			}
			columnHandlers = regularHandlers.toArray(new ColumnHandler[regularHandlers.size()]);
			this.batchHandlers = batchHandlers.isEmpty() ? null : CollectionUtils.toArray(batchHandlers, ColumnHandler[]::new);

			if(blockSchema.getFallbackColumn()!=null) {
				fallbackHandler = new ColumnHandler(this, blockSchema.getFallbackColumn(), -1);
			} else {
				fallbackHandler = null;
			}

			if(nestedBlockSchemas.length>0) {
				nestedBlockHandlers = new BlockHandler[nestedBlockSchemas.length];
				for(int i=0; i<nestedBlockHandlers.length; i++) {
					nestedBlockHandlers[i] = new BlockHandler(nestedBlockSchemas[i], this, idGen);
				}
			} else {
				nestedBlockHandlers = null;
			}

			/*
			 * IMPORTANT
			 *
			 * Potential solution to the linking problem:
			 *
			 * Do NOT link to the actual backend storage here, but have converters only
			 * produce raw members and let the driver worry about a 2nd linking pass!
			 * This has the added advantage of unifying the scan and load processes when
			 * for the former there is no mapping available yet.
			 */

			/*
			 * DEBUG
			 *
			 * For now we do not support arbitrary attribute handler outside of
			 * regular column content. Needs to be implemented on the next higher
			 * level of input processing.
			 */
			//TODO allow attribute handlers
			attributeHandlers = null;
		}

		private MappingHandler createMappingHandler(BlockHandler block, @Nullable BlockHandler parent) {
			if(parent==null) {
				return null;
			}

			Mapping mapping = getDriver().getMapping(parent.itemLayer, block.itemLayer);
			if(!WritableMapping.class.isInstance(mapping)) {
				mapping = null;
			}

			Mapping reverseMapping = getDriver().getMapping(block.itemLayer, parent.itemLayer);
			if(!WritableMapping.class.isInstance(reverseMapping)) {
				reverseMapping = null;
			}

			if(mapping==null && reverseMapping==null) {
				return null;
			}

			return new MappingHandler((WritableMapping)mapping, (WritableMapping)reverseMapping);
		}

		public BlockHandler getParent() {
			return parent;
		}

		public BlockSchema getSchema() {
			return blockSchema;
		}

		public ItemLayer getItemLayer() {
			return itemLayer;
		}

		public LayerGroup[] getExternalGroups() {
			return externalGroups;
		}

		public void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches) {
			CollectionUtils.forEach(columnHandlers, c -> c.prepareForReading(converter, mode, caches));
			CollectionUtils.forEach(nestedBlockHandlers, b -> b.prepareForReading(converter, mode, caches));
			CollectionUtils.forEach(attributeHandlers, a -> a.prepareForReading(converter, mode, caches));

			prepareProcessorForReading(beginDelimiter, converter, mode, caches);
			prepareProcessorForReading(endDelimiter, converter, mode, caches);
			prepareProcessorForReading(fallbackHandler, converter, mode, caches);

			if(mappingHandler!=null) {
				mappingHandler.prepareForReading(converter, mode, caches);;
			}
		}

		public void reset() {
			//TODO
		}

		/**
		 * @see java.io.Closeable#close()
		 */
		@Override
		public void close() {
			reset();

			CollectionUtils.forEach(columnHandlers, ColumnHandler::close);
			CollectionUtils.forEach(nestedBlockHandlers, BlockHandler::close);
			CollectionUtils.forEach(attributeHandlers, ContextProcessor::close);

			closeProcessor(beginDelimiter);
			closeProcessor(endDelimiter);
			closeProcessor(fallbackHandler);

			if(mappingHandler!=null) {
				mappingHandler.close();
			}
		}

		/**
		 * Returns {@code true} iff the current {@link ResolverContext#rawData() line}
		 * denotes the beginning of actual content for this block.
		 *
		 * @param context
		 * @return
		 */
		public boolean isBeginLine(InputResolverContext context) throws IcarusApiException {
			return beginDelimiter.process(context)!=ScanResult.FAILED;
		}

		/**
		 * Returns {@code true} iff the current {@link ResolverContext#rawData() line}
		 * denotes the end of content for this block.
		 *
		 * @param context
		 * @return
		 */
		public boolean isEndLine(InputResolverContext context) throws IcarusApiException {
			return endDelimiter.process(context)!=ScanResult.FAILED;
		}

		/**
		 * Returns {@code true} iff the current {@link ResolverContext#rawData() line}
		 * does not denote actual content, but additional (metadata) attributes for this
		 * block.
		 *
		 * @param context
		 * @return
		 */
		public boolean isAttributeLine(InputResolverContext context) throws IcarusApiException {
			if(attributeHandlers!=null) {
				ContextProcessor<ScanResult> pendingAttributeHandler = this.pendingAttributeHandler;
				this.pendingAttributeHandler = null;

				// If we have a multiline attribute let the pending handler have a shot first
				if(pendingAttributeHandler==null || pendingAttributeHandler.process(context)==ScanResult.FAILED) {
					for(int i=0; i<attributeHandlers.length; i++) {
						ContextProcessor<ScanResult> attributeHandler = attributeHandlers[i];

						// Skip the pending handler since it already did a check
						if(attributeHandler==pendingAttributeHandler) {
							continue;
						}

						ScanResult scanResult = attributeHandler.process(context);
						switch (scanResult) {

						// For multiline attributes save the handler
						case PARTLY_MATCHED: {
							this.pendingAttributeHandler = attributeHandler;
							return true;
						}
						case MATCHED: return true;

						default:
							break;
						}
					}
				}
			}

			return false;
		}

		public void beginContent(InputResolverContext context) {
			if(batchHandlers!=null) {
				for(int i=0; i<batchHandlers.length; i++) {
					batchHandlers[i].startBatch(context);
				}
			}
			if(mappingHandler!=null) {
				mappingHandler.beginBatch(context);
			}
		}

		public void endContent(InputResolverContext context) {
			//TODO check for pending attributes and complain

			if(batchHandlers!=null) {
				for(int i=0; i<batchHandlers.length; i++) {
					batchHandlers[i].endBatch(context);
				}
			}
			if(mappingHandler!=null) {
				mappingHandler.endBatch(context);
			}

			//TODO maybe clean up container and item?
		}

		private void saveColumnOffsets(int columnIndex, int begin, int end) {
			int idx = columnIndex<<1;
			if(idx+1>=columnOffsets.length) {
				columnOffsets = Arrays.copyOf(columnOffsets, Math.max(columnOffsets.length<<1, idx+1));
			}
			columnOffsets[idx] = begin;
			columnOffsets[idx+1] = end;

//			System.out.printf("saving column col=%d begin=%d end=%d\n", columnIndex, begin, end);
		}

		private int splitColumns(CharSequence content) {
			int regionBegin = 0;
			int regionEnd = content.length()-1;

			boolean needsAdjustedRegion = false;
			if(trimWhitespaces) {
				while(regionBegin<=regionEnd && Character.isWhitespace(content.charAt(regionBegin))) regionBegin++;
				while(regionEnd>=regionBegin && Character.isWhitespace(content.charAt(regionEnd))) regionEnd--;

				// If the entire line consists of whitespaces there'll be no point in trying to split
				if(regionBegin>regionEnd) {
					return 0;
				}

				needsAdjustedRegion = regionBegin>0 || regionEnd<content.length()-1;
			}

			int columnCount = 0;
			int begin = regionBegin;

			if(separatorMatcher==null) {
				int offset;
				while((offset = StringUtil.indexOf(content, separatorChar, begin, regionEnd))!=-1) {
					saveColumnOffsets(columnCount, begin, offset-1);
					begin = offset+1;

					columnCount++;
				}
			} else {
				try {
					separatorMatcher.reset(content);
					if(needsAdjustedRegion) {
						separatorMatcher.region(regionBegin, regionEnd); // Does a 2nd internal reset() call for the matcher
					}

					while(separatorMatcher.find()) {
						saveColumnOffsets(columnCount, begin, separatorMatcher.start()-1);
						begin = separatorMatcher.end();
						columnCount++;
					}

				} finally {
					// Clear matcher so we don't leave references to the raw content behind
					separatorMatcher.reset("");
				}
			}

			// Remaining stuff goes into a final column
			if(begin<=regionEnd) {
				saveColumnOffsets(columnCount, begin, regionEnd);
				columnCount++;
			}

			return columnCount;
		}

		private void moveCursorToColumn(int columnIndex) {
			int idx = columnIndex<<1;
			characterCursor.setRange(columnOffsets[idx], columnOffsets[idx+1]);
		}

		/**
		 * {@link TableConverter#advanceLine(LineIterator, InputResolverContext) advances}
		 * lines till a line indicating a valid {@link #isBeginLine(InputResolverContext) begin}
		 * of content data is found.
		 * <p>
		 * If in the process the last processed line is consumed, this method will advance an
		 * additional line.
		 *
		 * @param lines
		 * @param context
		 */
		private void scanBegin(LineIterator lines, InputResolverContext context) throws IcarusApiException {
			while(!isBeginLine(context)) {
				advanceLine(lines, context);
			}

			if(context.isDataConsumed()) {
				advanceLine(lines, context);
			}
		}

		/**
		 * {@link TableConverter#advanceLine(LineIterator, InputResolverContext) advances}
		 * lines till a line indicating a valid {@link #isEndLine(InputResolverContext) end}
		 * of content data is found.
		 * <p>
		 * If in the process the last processed line is consumed, this method will try to advance an
		 * additional line.
		 *
		 * @param lines
		 * @param context
		 */
		private void scanEnd(LineIterator lines, InputResolverContext context) throws IcarusApiException {
			while(!isEndLine(context)) {
				advanceLine(lines, context);
			}

			if(context.isDataConsumed() && lines.next()) {
				context.setData(lines.getLine());
			}
		}

		/**
		 * Reads a collection of {@code lines} for the respective layer.
		 * <p>
		 * TODO
		 *
		 * @param lines
		 * @param context
		 * @throws InterruptedException
		 */
		public void readChunk(LineIterator lines, InputResolverContext context)
				throws IcarusApiException, InterruptedException {

			long index = context.currentIndex();
			Container container = context.currentContainer();

			try {
				// Initialize with current raw line of content
				context.setData(lines.getLine());

				// Scan for beginning of content
				scanBegin(lines, context);

				// NOTE: we can either have column content OR nested blocks
				if(!tryReadNestedBlocks(lines, context)) {
					// Only when no nested blocks could be used will we parse actual column data
					readColumnLines(lines, context);
					// No need for scanEnd() since the column reading method takes care of it
				}
			} finally {
				context.setIndex(index);
				context.setContainer(container);
			}
		}

		/**
		 * Creates and adds a new {@link Container} and uses it to host {@link Item items}
		 * produced by nested blocks.
		 *
		 * @param lines
		 * @param context
		 * @return
		 * @throws InterruptedException
		 * @throws IcarusApiException
		 */
		private boolean tryReadNestedBlocks(LineIterator lines, InputResolverContext context) throws InterruptedException, IcarusApiException {
			if(nestedBlockHandlers!=null) {

				long index = context.currentIndex();
				final Container host = context.currentContainer();
				final boolean isProxyContainer = host.isProxy();
				final ObjLongConsumer<? super Item> topLevelAction = context.getTopLevelAction();

				ComponentSupplier componentSupplier = context.getComponentSupplier(getItemLayer());
				componentSupplier.reset(index);
				if(!componentSupplier.next())
					throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Failed to produce container for block");
				Container container = (Container) componentSupplier.currentItem();

				if(isProxyContainer) {
					if(topLevelAction!=null) {
						topLevelAction.accept(container, componentSupplier.currentIndex());
					}
				} else {
					// Only for non-top-level items do we need to manually add them to their host container
					host.addItem(container);
				}

				// Allow all nested block handlers to fully read their content
				for(int i=0; i<nestedBlockHandlers.length; i++) {
					// We need to set the current container for every nested block since they can change the field in context
					context.setContainer(container);
					context.setIndex(index);

					//TODO evaluate if we should support "empty" blocks that do not need to match actual content
					nestedBlockHandlers[i].readChunk(lines, context);
				}

				// Make sure we also scan till the end of our content
				scanEnd(lines, context);

				// Finally push our new container so that surrounding code can pick it up
				context.setItem(container);

				return true;
			}

			return false;
		}

		/**
		 * For each content line creates a new {@link Item} and uses it as a target for
		 * {@link ColumnHandler} instances.
		 *
		 * @param lines
		 * @param context
		 * @throws InterruptedException
		 * @throws IcarusApiException
		 */
		private void readColumnLines(LineIterator lines, InputResolverContext context) throws InterruptedException, IcarusApiException {

			final long hostIndex = context.currentIndex();
			Container host = context.currentContainer();
			final boolean isProxyContainer = host.isProxy();
			final ObjLongConsumer<? super Item> topLevelAction = context.getTopLevelAction();

			ComponentSupplier componentSupplier = context.getComponentSupplier(getItemLayer());
			componentSupplier.reset(hostIndex);

			// Prepare for beginning of content (notify batch resolvers)
			beginContent(context);

			long index = 0;

			// Read in content lines until a stop line is detected or the iterator runs out of lines
			while(!isEndLine(context)) {
				// Create new nested item
				if(!componentSupplier.next())
					throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Failed to produce nested item");
				Item item = componentSupplier.currentItem();

				// Link context to new item
				context.setItem(item);
				context.setIndex(index);
				if(isProxyContainer) {
					if(topLevelAction!=null) {
						topLevelAction.accept(item, componentSupplier.currentIndex());
					}
				} else {
					// Only for non-top-level items do we need to add them to their host container
					host.addItem(item);
				}

				if(mappingHandler!=null) {
					mappingHandler.process(context);
				}

				processColumns(lines, context);

				if(!lines.next()) {
					break;
				}
				context.setData(lines.getLine());

				index++;
			}

			context.setIndex(hostIndex);

			// Finalize content (notify batch resolvers)
			endContent(context);

			// Ensure that we advance in case the final line got consumed (this should usually happen when reading a content line)
			if(context.isDataConsumed() && lines.next()) {
				context.setData(lines.getLine());
			}
		}

		private void processColumns(LineIterator lines, InputResolverContext context) throws IcarusApiException {
			final CharSequence rawContent = context.rawData();

			Item item = context.currentItem();

			try {
				// Preparation pass: save split offsets for all columns
				final int columnCount = splitColumns(rawContent);
				if(requiredColumnCount>columnCount)
					throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
							Messages.mismatch("Insufficient columns", _int(requiredColumnCount), _int(columnCount)));

				// Re-link so the context uses our smaller cursor
				characterCursor.setSource(rawContent);
				context.setData(characterCursor);

				// Only traverse columns that do not ignore their content
				for(ColumnHandler columnHandler : columnHandlers) {
					int columnIndex = columnHandler.getColumnIndex();

					// Point the cursor to the respective offsets
					moveCursorToColumn(columnIndex);

					// Skip "empty" columns
					if(noEntryLabel!=null && StringUtil.equals(noEntryLabel, characterCursor)) {
						continue;
					}

					// Delegate actual work to column handler
					columnHandler.process(context);
				}

				// In case we have too many columns try to handle them via the fallback solution
				if(columnCount>requiredColumnCount && fallbackHandler!=null) {
					for(int columnIndex=requiredColumnCount; columnIndex<columnCount; columnIndex++) {
						context.setColumnIndex(columnIndex);
						fallbackHandler.process(context);
					}
				}

			} finally {
				context.setData(rawContent);
				context.setItem(item);
				context.consumeData();
			}
		}

		public int getId() {
			return blockId;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return blockId;
		}
	}
}
