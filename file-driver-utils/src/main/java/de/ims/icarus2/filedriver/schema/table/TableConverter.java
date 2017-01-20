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
 */
package de.ims.icarus2.filedriver.schema.table;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.classes.ClassUtils._int;
import static de.ims.icarus2.util.strings.StringUtil.getName;
import static java.util.Objects.requireNonNull;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.filedriver.ComponentSupplier;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.ElementFlag;
import de.ims.icarus2.filedriver.FileDataStates;
import de.ims.icarus2.filedriver.FileDataStates.FileInfo;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.FileDriver.LockableFileObject;
import de.ims.icarus2.filedriver.analysis.Analyzer;
import de.ims.icarus2.filedriver.schema.SchemaBasedConverter;
import de.ims.icarus2.filedriver.schema.resolve.BatchResolver;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverFactory;
import de.ims.icarus2.filedriver.schema.resolve.standard.BasicAnnotationResolver;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType;
import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.Item.ManagedItem;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AccumulatingException;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.Pool;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.strings.FlexibleSubSequence;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class TableConverter extends SchemaBasedConverter implements ModelConstants {

	private TableSchema tableSchema;

	private LayerMemberFactory memberFactory;

	private final ResolverFactory RESOLVER_FACTORY = ResolverFactory.newInstance();

	private Charset encoding;
	private int characterChunkSize;

	private volatile boolean open;

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

		Resolver resolver = RESOLVER_FACTORY.createResolver(schema.getType());

//		resolver.prepareForReading(this, schema.getOptions());
		//FIXME move to init method of blockhandlers

		return resolver;
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#init(de.ims.icarus2.filedriver.FileDriver)
	 */
	@Override
	public void init(FileDriver driver) {
		super.init(driver);

		ContextManifest contextManifest = driver.getManifest().getContextManifest();

		//TODO fetch and process TableSchema from driver settings

		/*
		 * Initialize our character buffer with sufficient capacity to hold a single block.
		 * If the informed estimation fails the buffer will be initialized with a default
		 * capacity. While having a sufficient capacity from the start is nice, we can live
		 * with the cost of resizing the buffer a few times.
		 */
		String layerId = tableSchema.getRootBlock().getLayerId();
		ItemLayerManifest layerManifest = (ItemLayerManifest) contextManifest.getLayerManifest(layerId);
		// We use the recommended size for byte buffers here to be on the safe side for our character buffer
		characterChunkSize = getRecommendedByteBufferSize(layerManifest);

		encoding = driver.getEncoding();
		memberFactory = driver.newMemberFactory();

		open = true;
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#close()
	 */
	@Override
	public void close() throws AccumulatingException {
		encoding = null;
		characterChunkSize = -1;

		// Delegate to default implementation last
		super.close();
	}

	public TableSchema getSchema() {
		checkOpen();
		return tableSchema;
	}

	public LayerMemberFactory getSharedMemberFactory() {
		return memberFactory;
	}

	private void checkOpen() {
		if(!open)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Converter not open");
	}

	/**
	 * Used for our {@link Pool} of {@link BlockHandler block handlers} as a {@link Supplier}.
	 *
	 * @return
	 */
	protected BlockHandler createRootBlockHandler() {
		// Fail if closed
		checkOpen();

		return new BlockHandler(tableSchema.getRootBlock());
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#scanFile(int, de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage)
	 */
	@Override
	public Report<ReportItem> scanFile(int fileIndex) throws IOException,
			InterruptedException {
		checkOpen();

		@SuppressWarnings("resource")
		BlockHandler blockHandler = blockHandlerPool.get();

		Map<Layer, Analyzer> analyzers = createAnalyzers(blockHandler.getItemLayer().getLayerGroup());

		//TODO implement custom InputCache instances that act as analyzers and can be "abused" as component consumers
		Map<ItemLayer, InputCache> caches = new Object2ObjectOpenHashMap<>(analyzers.size());

		/*
		 * We only need bare component suppliers without back-end storage
		 */
		Map<ItemLayer, ComponentSupplier> componentSuppliers = new ComponentSuppliersFactory(this)
			.rootBlockHandler(blockHandler)
			.fileIndex(fileIndex)
			.memberFactory(getSharedMemberFactory())
			.mode(ReadMode.SCAN)
			.consumers(layer -> caches.get(layer)::offer)
			.build();

		InputResolverContext inputContext = new InputResolverContext(componentSuppliers);
		ItemLayer primaryLayer = blockHandler.getItemLayer();
		long index = 0L;

		LockableFileObject fileObject = getFileDriver().getFileObject(fileIndex);

		// Notify stack about incoming read operation
		blockHandler.prepareForReading(this, ReadMode.SCAN, caches::get);

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

				/*
				 *  Technically speaking using exceptions for control flow here isn't
				 *  the best strategy, but with the call depth redesigning all involved
				 *  facilities (and duplicating) might be overkill in comparison.
				 */
				try {
					// Delegate actual conversion work (which will advance lines on its own)
					blockHandler.readChunk(lines, inputContext);

					// Fetch item and verify state
					Item item = inputContext.currentItem();

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
					caches.values().forEach(InputCache::commit);
				} catch(ManifestException e) {
					//TODO add to report
				}

				// Make sure we advance 1 line in case the last one has been consumed as content
				if(inputContext.isDataConsumed()) {
					lines.next();
				}

				// Next chunk
				index++;
			}
		} finally {
			blockHandler.close();
			blockHandlerPool.recycle(blockHandler);

			// Make sure we discard all "cached" data
			caches.values().forEach(InputCache::discard);
		}

		//DEBUG
		getFileDriver().getFileStates().getFileInfo(fileIndex).setFlag(ElementFlag.SCANNED);

		// TODO Auto-generated method stub
		return null;
	}

	private Map<Layer, Analyzer> createAnalyzers(LayerGroup group) {

	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#loadFile(int, de.ims.icarus2.model.standard.driver.ChunkConsumer)
	 */
	@Override
	public LoadResult loadFile(final int fileIndex, final ChunkConsumer action)
			throws IOException, InterruptedException {
		checkOpen();

		@SuppressWarnings("resource")
		BlockHandler blockHandler = blockHandlerPool.get();

		// Collect basic caches
		Map<ItemLayer, InputCache> caches = createCaches(blockHandler.getItemLayer().getLayerGroup(), null);

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

		InputResolverContext inputContext = new InputResolverContext(componentSuppliers);
		ItemLayer primaryLayer = blockHandler.getItemLayer();
		long index = 0L;

		DynamicLoadResult loadResult = new SimpleLoadResult(caches.values());

		LockableFileObject fileObject = getFileDriver().getFileObject(fileIndex);

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

				// Fetch item and verify state
				Item item = inputContext.currentItem();

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

				// Make sure we advance 1 line in case the last one has been consumed as content
				if(inputContext.isDataConsumed()) {
					lines.next();
				}

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
	protected Item readItemFromCursor(DelegatingCursor cursor)
			throws IOException, InterruptedException {

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

	protected Layer findLayer(String layerId) {
		return getFileDriver().getContext().getLayer(layerId);
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
	protected DelegatingCursor createDelegatingCursor(int fileIndex,
			ItemLayer itemLayer) {
		BlockHandler blockHandler = blockHandlerPool.get();

		if(itemLayer!=blockHandler.getItemLayer())
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.mismatchMessage("Unexpected layer", getName(itemLayer), getName(blockHandler.getItemLayer())));

		// Collect basic caches
		Map<ItemLayer, InputCache> caches = createCaches(itemLayer.getLayerGroup(), null);

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

		InputResolverContext inputContext = new InputResolverContext(componentSuppliers);

		// Result instance that is linked to our caches
		DynamicLoadResult loadResult = new SimpleLoadResult(caches.values());

		//TODO shift oversized parameter list of constructor to builder or config object
		return new DelegatingTableCursor(getFileDriver().getFileObject(fileIndex),
				blockHandlerPool.get(), inputContext, loadResult, encoding, characterChunkSize, caches::get);
	}

//	private Map<ItemLayer, InputCache> createCaches(BlockHandler rootBlockHandler) {
//
//		Map<ItemLayer, InputCache> caches = new Object2ObjectOpenHashMap<>();
//		forEachBlock(rootBlockHandler, blockHandler -> {
//			ItemLayer itemLayer = blockHandler.getItemLayer();
//			InputCache cache = getFileDriver().getLayerBuffer(itemLayer).newCache();
//			caches.put(itemLayer, cache);
//		});
//
//		return caches;
//	}

	/**
	 * Creates a lookup map to fetch caches for every item layer that is either contained in the specified
	 * layer group or (indirectly) referenced by layers in the group and which is also a member of the same
	 * context.
	 *
	 * @param group
	 * @param cacheGen
	 * @return
	 */
	private Map<ItemLayer, InputCache> createCaches(LayerGroup group, Function<ItemLayer, InputCache> cacheGen) {

		final Map<ItemLayer, InputCache> caches = new Object2ObjectOpenHashMap<>();

		final Context context = group.getContext();

		// No recursion: we use a stack to buffer pending layers in

		final Stack<Layer> pendingLayers = new Stack<>();

		group.forEachLayer(pendingLayers::push);

		while(!pendingLayers.isEmpty()) {
			Layer layer = pendingLayers.pop();

			if(layer.getContext()!=context) {
				continue;
			}

			if(ModelUtils.isItemLayer(layer)) {
				ItemLayer itemLayer = (ItemLayer)layer;
				InputCache cache;
				if(cacheGen==null) {
					cache = getFileDriver().getLayerBuffer(itemLayer).newCache();
				} else {
					cache = cacheGen.apply(itemLayer);
				}
				caches.put(itemLayer, cache);
			}

			layer.getBaseLayers().forEachEntry(pendingLayers::push);
		}

		return caches;
	}

	public static class AnalyzingInputCache implements InputCache{

		private final List<Item> items = new ObjectArrayList<>(100);
		private final LongList indices = new LongArrayList(100);

		private final Analyzer analyzer;

		/**
		 * @param analyzer
		 */
		public AnalyzingInputCache(Analyzer analyzer) {
			requireNonNull(analyzer);

			this.analyzer = analyzer;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#offer(de.ims.icarus2.model.api.members.item.Item, long)
		 */
		@Override
		public void offer(Item item, long index) {
			items.add(item);
			indices.add(index);
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
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#discard()
		 */
		@Override
		public int discard() {
			int size = items.size();

			items.clear();
			indices.clear();

			return size;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache#commit()
		 */
		@Override
		public int commit() {
			forEach(analyzer);

			return discard();
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
	protected class DelegatingTableCursor extends DelegatingCursor {

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

		public DelegatingTableCursor(LockableFileObject file, BlockHandler rootBlockHandler, InputResolverContext context,
				DynamicLoadResult loadResult, Charset encoding, int characterChunkSize, Function<ItemLayer, InputCache> caches) {
			super(file, rootBlockHandler.getItemLayer(), loadResult);

			requireNonNull(rootBlockHandler);
			requireNonNull(context);
			requireNonNull(encoding);
			checkArgument(characterChunkSize>0);

			this.rootBlockHandler = rootBlockHandler;
			this.context = context;

			characterBuffer = new StringBuilder(characterChunkSize);
			decoder = encoding.newDecoder();

			bb = ByteBuffer.allocateDirect(IOUtil.DEFAULT_BUFFER_SIZE);
			cb = CharBuffer.allocate(IOUtil.DEFAULT_BUFFER_SIZE);

			lineIterator = new SubSequenceLineIterator(characterBuffer);

			rootBlockHandler.prepareForReading(TableConverter.this, ReadMode.CHUNK, caches);
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
			blockHandlerPool.recycle(rootBlockHandler);
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
	protected static class InputResolverContext implements ResolverContext, AutoCloseable {

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

		public InputResolverContext(Map<ItemLayer, ComponentSupplier> componentSuppliers) {
			requireNonNull(componentSuppliers);

			this.componentSuppliers = componentSuppliers;
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

		public ComponentSupplier getComponentSupplier(BlockHandler blockHandler) {
			return componentSuppliers.get(blockHandler.getItemLayer());
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <O> result type of the processing step
	 */
	@FunctionalInterface
	public interface ContextProcessor<O extends Object> extends Closeable {
		O process(InputResolverContext context);

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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.ContextProcessor#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.filedriver.Converter.ReadMode, java.util.function.Function)
		 */
		@Override
		public void prepareForReading(Converter converter, ReadMode mode,
				Function<ItemLayer, InputCache> caches) {
			if(resolver!=null) {
				resolver.prepareForReading(converter, mode, caches, attributeSchema.getResolver().getOptions());
			}
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.ContextProcessor#close()
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
		public ScanResult process(InputResolverContext context) {
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
			} else {
				return ScanResult.FAILED;
			}
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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.ContextProcessor#process(de.ims.icarus2.filedriver.schema.table.TableConverter.InputResolverContext)
		 */
		@Override
		public Item process(InputResolverContext context) {
			Item newItem = null;
			try {
				newItem = context.getPendingItem();
				if(newItem==null || newItem.getMemberType()!=requiredType)
					throw new ModelException(ModelErrorCode.DRIVER_ERROR,
							Messages.mismatchMessage("Invalid member-type of new item", requiredType, newItem.getMemberType()));

				context.mapItem(name, newItem);
			} finally {
				context.clearPendingItem();
			}
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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.ContextProcessor#process(de.ims.icarus2.filedriver.schema.table.TableConverter.InputResolverContext)
		 */
		@Override
		public Item process(InputResolverContext context) {
			Item newItem = null;
			try {
				newItem = context.getPendingItem();
				if(newItem==null || newItem.getMemberType()!=requiredType)
					throw new ModelException(ModelErrorCode.DRIVER_ERROR,
							Messages.mismatchMessage("Invalid member-type of new item", requiredType, newItem.getMemberType()));

				context.replaceCurrentItem(newItem);
			} finally {
				context.clearPendingItem();
			}
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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.ContextProcessor#process(de.ims.icarus2.filedriver.schema.table.TableConverter.InputResolverContext)
		 */
		@Override
		public Item process(InputResolverContext context) {
			Item item = context.getItem(name);
			// Item can't be null due to contract of getItem(String)
			if(item.getMemberType()!=requiredType)
				throw new ModelException(ModelErrorCode.DRIVER_ERROR,
						Messages.mismatchMessage("Invalid member-type of stored item", requiredType, item.getMemberType()));

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
		 * to track lines.
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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.LineIterator#hasLine()
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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.LineIterator#getLineNumber()
		 */
		@Override
		public long getLineNumber() {
			return lineNumber;
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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.LineIterator#hasLine()
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
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected Map<ItemLayer, ComponentSupplier> create() {
			Map<ItemLayer, ComponentSupplier> map = new Object2ObjectOpenHashMap<>();

			collectComponentSuppliers0(rootBlockHandler, map);

			return map;
		}

		private void collectComponentSuppliers0(BlockHandler blockHandler, Map<ItemLayer, ComponentSupplier> map) {

			ItemLayer layer = blockHandler.getItemLayer();

			ComponentSupplier.Builder builder = new ComponentSupplier.Builder();
			builder.componentLayer(layer);
			builder.componentType(blockHandler.getSchema().getComponentSchema().getMemberType());
			builder.memberFactory(memberFactory);

			ObjLongConsumer<Item> consumer = consumers==null ? null : consumers.apply(layer);
			if(consumer!=null) {
				builder.componentConsumer(consumer);
			}

			// In SCAN mode we only read items, analyze and then immediately discard them
			if(mode==ReadMode.CHUNK) {

				BlockHandler parent = blockHandler.getParent();

				if(parent!=null) {
					ItemLayer sourceLayer = parent.getItemLayer();
					ItemLayer targetLayer = layer;
					Mapping mapping = converter.getFileDriver().getMapping(sourceLayer, targetLayer);
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
				FileDataStates states = converter.getFileDriver().getFileStates();

				long firstIndex;

				FileInfo fileInfo = states.getFileInfo(fileIndex);
				firstIndex = fileInfo.getBeginIndex(layer.getManifest());
				if(firstIndex==NO_INDEX && fileIndex>0) {
					FileInfo previousInfo = states.getFileInfo(fileIndex-1);
					firstIndex = previousInfo.getBeginIndex(layer.getManifest());
				}

				if(firstIndex==NO_INDEX) {
					firstIndex = 0L;
				}


				if(mode==ReadMode.SCAN) {
					MutableLong index = new MutableLong(firstIndex);
					builder.indexSupplier(index::getAndIncrement);
				} else {
					builder.firstIndex(firstIndex);
					long lastIndex = fileInfo.getEndIndex(layer.getManifest());
					if(lastIndex!=NO_INDEX) {
						builder.lastIndex(lastIndex);
					}
				}
			}

			map.put(blockHandler.getItemLayer(), builder.build());

			BlockHandler[] nestedHandlers = blockHandler.nestedBlockHandlers;
			if(nestedHandlers!=null) {
				for(BlockHandler nestedHandler : nestedHandlers) {
					collectComponentSuppliers0(nestedHandler, map);
				}
			}
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
				AnnotationLayer layer = (AnnotationLayer) findLayer(columnSchema.getLayerId());
				resolver = BasicAnnotationResolver.forAnnotation(layer, columnSchema.getAnnotationKey());
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
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.ContextProcessor#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.filedriver.Converter.ReadMode, java.util.function.Function)
		 */
		@Override
		public void prepareForReading(Converter converter, ReadMode mode,
				Function<ItemLayer, InputCache> caches) {
			resolver.prepareForReading(converter, mode, caches, columnSchema.getResolver().getOptions());
		}

		/**
		 * @see de.ims.icarus2.filedriver.schema.table.TableConverter.ContextProcessor#close()
		 */
		@Override
		public void close() {
			resolver.close();
		}

		public boolean isBatchHandler() {
			return (resolver instanceof BatchResolver);
		}

		@Override
		public Void process(InputResolverContext context) {
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
					processedItem = postprocessingHandler.process(context);
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
	}

	private static String getSeparator(BlockHandler blockSchema, TableSchema tableSchema) {
		String separator = blockSchema.getSchema().getSeparator();

		while(separator==null && blockSchema!=null) {
			blockSchema = blockSchema.getParent();
		}

		if(separator==null) {
			separator = tableSchema.getSeparator();
		}

		if(separator==null)
			throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Missing separator specification");

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

		private BlockHandler(BlockSchema blockSchema, BlockHandler parent, MutableInteger idGen) {
			requireNonNull(blockSchema);

			this.parent = parent;
			this.blockSchema = blockSchema;

			blockId = idGen.getAndIncrement();

			itemLayer = (ItemLayer) findLayer(blockSchema.getLayerId());

			noEntryLabel = blockSchema.getNoEntryLabel();
			trimWhitespaces = blockSchema.getOptions().getBoolean("trimWhitespaces", false);

			// Process separator, which can either result in a single character or a complex Matcher instance
			Matcher separatorMatcher = null;
			char separatorChar = '\0';

			String separator = getSeparator(this, tableSchema);

			if(separator.length()==1) {
				separatorChar = separator.charAt(0);
			} else {
				switch (separator) {
				case TableSchema.DELIMITER_SPACE:
					separatorChar = ' ';
					break;

				case TableSchema.DELIMITER_TAB:
					separatorChar = '\t';
					break;

				case TableSchema.DELIMITER_WHITESPACES:
					separator = "\\s+";

					//$FALL-THROUGH$
				default:
					separatorMatcher = Pattern.compile(separator).matcher("");
					break;
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
			ColumnSchema[] columnSchemas = blockSchema.getColumns();
			requiredColumnCount = columnSchemas.length;

			// Make sure we have enough space in our column offset buffer
			columnOffsets = new int[requiredColumnCount*2];

			List<ColumnHandler> regularHandlers = new ArrayList<>();
			List<ColumnHandler> batchHandlers = new ArrayList<>();

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
			this.batchHandlers = batchHandlers.isEmpty() ? null : batchHandlers.toArray(new ColumnHandler[batchHandlers.size()]);

			if(blockSchema.getFallbackColumn()!=null) {
				fallbackHandler = new ColumnHandler(this, blockSchema.getFallbackColumn(), -1);
			} else {
				fallbackHandler = null;
			}

			BlockSchema[] nestedBlockSchemas = blockSchema.getNestedBlocks();
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

		public BlockHandler getParent() {
			return parent;
		}

		public BlockSchema getSchema() {
			return blockSchema;
		}

		public ItemLayer getItemLayer() {
			return itemLayer;
		}

		public void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches) {
			CollectionUtils.forEach(columnHandlers, c -> c.prepareForReading(converter, mode, caches));
			CollectionUtils.forEach(nestedBlockHandlers, b -> b.prepareForReading(converter, mode, caches));
			CollectionUtils.forEach(attributeHandlers, a -> a.prepareForReading(converter, mode, caches));

			prepareProcessorForReading(beginDelimiter, converter, mode, caches);
			prepareProcessorForReading(endDelimiter, converter, mode, caches);
			prepareProcessorForReading(fallbackHandler, converter, mode, caches);
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
		}

		/**
		 * Returns {@code true} iff the current {@link ResolverContext#rawData() line}
		 * denotes the beginning of actual content for this block.
		 *
		 * @param context
		 * @return
		 */
		public boolean isBeginLine(InputResolverContext context) {
			return beginDelimiter.process(context)!=ScanResult.FAILED;
		}

		/**
		 * Returns {@code true} iff the current {@link ResolverContext#rawData() line}
		 * denotes the end of content for this block.
		 *
		 * @param context
		 * @return
		 */
		public boolean isEndLine(InputResolverContext context) {
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
		public boolean isAttributeLine(InputResolverContext context) {
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
							pendingAttributeHandler = attributeHandler;
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
		}

		public void endContent(InputResolverContext context) {
			//TODO check for pending attributes and complain

			if(batchHandlers!=null) {
				for(int i=0; i<batchHandlers.length; i++) {
					batchHandlers[i].endBatch(context);
				}
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
		private void scanBegin(LineIterator lines, InputResolverContext context) {
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
		private void scanEnd(LineIterator lines, InputResolverContext context) {
			while(!isEndLine(context)) {
				advanceLine(lines, context);
			}

			if(context.isDataConsumed() && lines.next()) {
				context.setData(lines.getLine());
			}
		}

		/**
		 * Reads a collection of {@code lines} for the respective layer.
		 *
		 * @param lines
		 * @param context
		 * @throws InterruptedException
		 */
		public void readChunk(LineIterator lines, InputResolverContext context) throws InterruptedException {

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
		 */
		private boolean tryReadNestedBlocks(LineIterator lines, InputResolverContext context) throws InterruptedException {
			if(nestedBlockHandlers!=null) {

				ComponentSupplier componentSupplier = context.getComponentSupplier(this);
				long index = context.currentIndex();
				componentSupplier.reset(index);
				if(!componentSupplier.next())
					throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Failed to produce container for block");
				Container container = (Container) componentSupplier.currentItem();

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
		 */
		private void readColumnLines(LineIterator lines, InputResolverContext context) throws InterruptedException {

			long index = context.currentIndex();
			Container container = context.currentContainer();
			final boolean isProxyContainer = container.isProxy();

			ComponentSupplier componentSupplier = context.getComponentSupplier(this);
			componentSupplier.reset(index);
			componentSupplier.setHost(container);

			// Prepare for beginning of content (notify batch resolvers)
			beginContent(context);

			// Read in content lines until a stop line is detected or the iterator runs out of lines
			while(!isEndLine(context)) {

				// Create new nested item
				if(!componentSupplier.next())
					throw new ModelException(ModelErrorCode.DRIVER_ERROR, "Failed to produce nested item");
				Item item = componentSupplier.currentItem();

				// Link context to new item
				context.setItem(item);
				if(!isProxyContainer) {
					// Only for non-top-level items do we need to add them to their host container
					container.addItem(item);
				}

				processColumns(lines, context);

				if(!lines.next()) {
					break;
				}
				context.setData(lines.getLine());
			}

			// Finalize content (notify batch resolvers)
			endContent(context);

			// Ensure that we advance in case the final line got consumed (this should usually happen when reading a content line)
			if(context.isDataConsumed() && lines.next()) {
				context.setData(lines.getLine());
			}
		}

		private void processColumns(LineIterator lines, InputResolverContext context) {
			final CharSequence rawContent = context.rawData();

			Item item = context.currentItem();

			try {
				// Preparation pass: save split offsets for all columns
				final int columnCount = splitColumns(rawContent);
				if(requiredColumnCount>columnCount)
					throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
							Messages.mismatchMessage("Insufficient columns", _int(requiredColumnCount), _int(columnCount)));

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
