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
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.classes.ClassUtils._int;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
import java.util.function.LongFunction;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.schema.SchemaBasedConverter;
import de.ims.icarus2.filedriver.schema.resolve.BasicAnnotationResolver;
import de.ims.icarus2.filedriver.schema.resolve.BatchResolver;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverFactory;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.driver.BufferedItemManager;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.util.MutablePrimitives.MutableLong;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.strings.FlexibleSubSequence;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class TableConverter extends SchemaBasedConverter {

	private TableSchema tableSchema;

	private BlockHandler rootBlockHandler;
	private StringBuilder characterBuffer;
	private CharsetDecoder decoder;
	private ByteBuffer bb;
	private CharBuffer cb;

	private InputResolverContext inputContext;

	private final ResolverFactory RESOLVER_FACTORY = ResolverFactory.newInstance();

	private Resolver createResolver(ResolverSchema schema) {
		if(schema==null) {
			return null;
		}

		Resolver resolver = RESOLVER_FACTORY.createResolver(schema.getType());

		resolver.init(this, schema.getOptions());

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
		BlockSchema rootBlockSchema = rootBlockHandler.blockSchema;
		ItemLayerManifest layerManifest = (ItemLayerManifest) contextManifest.getLayerManifest(rootBlockSchema.getLayerId());
		int bufferSize = getRecommendedByteBufferSize(layerManifest);
		characterBuffer = new StringBuilder(bufferSize);

		Charset encoding = driver.getEncoding();
		decoder = encoding.newDecoder();

		bb = ByteBuffer.allocateDirect(IOUtil.DEFAULT_BUFFER_SIZE);
		cb = CharBuffer.allocate(IOUtil.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#scanFile(int, de.ims.icarus2.filedriver.mapping.chunks.ChunkIndexStorage)
	 */
	@Override
	public Report<ReportItem> scanFile(int fileIndex) throws IOException,
			InterruptedException {


		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#loadFile(int, de.ims.icarus2.model.standard.driver.ChunkConsumer)
	 */
	@Override
	public long loadFile(int fileIndex, ChunkConsumer action)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Item readItemFromCursor(DelegatingCursor cursor)
			throws IOException {

		// Read entire channel and decode into characters
		fillCharacterBuffer(cursor.getBlockChannel());

		try(BlockLineIterator lines = new BlockLineIterator(characterBuffer, 0)) {
			if(!lines.next())
				throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
						"Cannot create item from empty input");

			// Clear state of context
			inputContext.reset();

			// For now we assume a flat block hierarchy (meaning one container block hosting items)
			//TODO this should change in the future, allowing deeper nested blocks to be handled
			return readFlatBlock(rootBlockHandler, lines, inputContext);
		} finally {
			rootBlockHandler.reset();
		}
	}

	protected Item readFlatBlock(BlockHandler blockHandler, LineIterator lines, InputResolverContext context) {
		// Initialize with first raw line of content
		context.setData(lines.getLine());

		// Scan for beginning of block
		while(!blockHandler.isBeginLine(context)) {
			if(!lines.next())
				throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT, "Unable to find begin of block");
			context.setData(lines.getLine());
		}

		// Prepare block
		blockHandler.beginContent(context);

		// Read in content lines until a stop line is detected or the iterator runs out of lines
		while(!blockHandler.isEndLine(context)) {
			blockHandler.processContentLine(context);
			if(!lines.next()) {
				break;
			}
			context.setData(lines.getLine());
		}

		// Finalize block
		blockHandler.endContent(context);

		// Return the current top level container
		return context.currentContainer();
	}

	protected void clearCharacterBuffer() {
		characterBuffer.setLength(0);
	}

	/**
	 * Reads the entire content of the given {@code channel} into the internal
	 * {@link StringBuilder character-buffer}. The returned value is the total number
	 * of characters read.
	 *
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	protected int fillCharacterBuffer(ReadableByteChannel channel) throws IOException {

		clearCharacterBuffer();

		int count = 0;

	    for(;;) {
	        if(-1 == channel.read(bb)) {
	            decoder.decode(bb, cb, true);
	            decoder.flush(cb);
	        	cb.flip();
	        	count += cb.remaining();
	            break;
	        }
	        bb.flip();

	        CoderResult res = decoder.decode(bb, cb, false);
	        if(CoderResult.OVERFLOW == res) {
	        	cb.flip();
	        	count += cb.remaining();
	        	characterBuffer.append(cb);
	            cb.clear();
	        } else if (CoderResult.UNDERFLOW == res) {
	            bb.compact();
	        }
	    }

	    return count;
	}

	protected Layer findLayer(String layerId) {
		return getFileDriver().getContext().getLayer(layerId);
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
			checkNotNull(data);
			checkNotNull(target);
			checkNotNull(resolver);

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
	 *
	 * @author Markus Gärtner
	 *
	 */
	protected static class InputResolverContext implements ResolverContext {

		private Container container;
		private int index;
		private Item item;
		private Item pendingItem;
		private CharSequence data;

		private final Map<String, Item> namedSubstitues = new Object2ObjectOpenHashMap<>();

		private int lineNumber;
		private int columnIndex;

		private ObjectArrayList<Item> replacements = new ObjectArrayList<>();

		private List<UnresolvedAttribute> pendingAttributes = new ArrayList<>();

		void reset() {
			container = null;
			item = null;
			pendingItem = null;
			namedSubstitues.clear();
			replacements.clear();
			pendingAttributes.clear();
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
		public int currentIndex() {
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

		public void setData(CharSequence newData) {
			checkNotNull(newData);
			data = newData;
		}

		public int getColumnIndex() {
			return columnIndex;
		}

		public void setColumnIndex(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		public void replaceCurrentItem(Item newItem) {
			checkNotNull(newItem);

			replacements.push(item);
			item = newItem;
		}

		public void clearLastReplacement() {
			item = replacements.pop();
		}

		public void mapItem(String name, Item item) {
			checkNotNull(item);
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
			checkNotNull(pendingItem);
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
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <O> result type of the processing step
	 */
	@FunctionalInterface
	public interface ContextProcessor<O extends Object> {
		O process(InputResolverContext context);
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
		checkNotNull(attributeSchema);

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

	private class ItemSupplier implements LongFunction<Item> {
		private final ItemLayer layer;
		private final IndexSource indexSource;
		private final IdManager idManager;
		private final BufferedItemManager.InputCache cache;

		ItemSupplier(String layerId) {
			layer = (ItemLayer) findLayer(layerId);
		}

		/**
		 * @see java.util.function.LongFunction#apply(long)
		 */
		@Override
		public Item apply(long value) {
			if(cache==null) {
				cache = getCacheForLayer(layer);
			}

			long index = indexSource.indexAt(value);
			long id = idManager.getIdAt(index);
			Item item = getMemberFactory().newItem(host);

			item.setIndex(index);

			cache.offer(index, item);

			return item;
		}
	}

	private class ContainerSupplier implements Supplier<Container> {
		private final ItemLayer layer;
		private final ContainerManifest containerManifest;

		ContainerSupplier(String layerId) {
			layer = (ItemLayer) findLayer(layerId);
			containerManifest = layer.getManifest().getContainerManifest(1);
		}

		private LongSupplier idFactory;

		/**
		 * Lazily created cache
		 */
		private BufferedItemManager.InputCache cache;

		/**
		 * Active host container
		 */
		private Container host;

		/**
		 * @see java.util.function.Supplier#get()
		 */
		@Override
		public Container get() {
			if(cache==null) {
				cache = getCacheForLayer(layer);
			}

			long index = idFactory.getAsLong();
			Container container = getMemberFactory().newContainer(host, containerManifest);

			container.setIndex(index);

			cache.offer(index, container);

			return container;
		}

		public void setIdFactory(LongSupplier idFactory) {
			this.idFactory = idFactory;
		}

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
			checkNotNull(attributeSchema);

			this.attributeSchema = attributeSchema;
			resolver = createResolver(attributeSchema.getResolver());

			String pattern = attributeSchema.getPattern();

			if(!pattern.startsWith("$")) {
				pattern = "$"+pattern;
			}

			matcher = Pattern.compile(pattern).matcher("");
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
			checkNotNull(attributeSchema);

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
			checkNotNull(substituteSchema);
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
			checkNotNull(substituteSchema);
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
	interface LineIterator {
		boolean next();
		CharSequence getLine();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	private static class BlockLineIterator extends FlexibleSubSequence implements LineIterator {

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

		public BlockLineIterator(CharSequence source, int offset) {
			super(source, offset, 0);
		}

		public int getLineBreakCharacterCount() {
			return lineBreakCharacterCount;
		}

		/**
		 * Start at the specified offset and expand till a line break occurs.
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

			if(length>0) {
				setOffset(begin);
				setLength(length);
				lineReady = true;
			} else {
				endOfInput = true;
			}
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
	public static class SubstituteTargetHandler implements ContextProcessor<Item> {
		private final SubstituteSchema substituteSchema;
		private final String name;
		private final MemberType requiredType;

		public SubstituteTargetHandler(SubstituteSchema substituteSchema) {
			checkNotNull(substituteSchema);
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
			checkNotNull(blockHandler);
			checkNotNull(columnSchema);

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
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public class BlockHandler {
		private final BlockHandler parent;

		private final BlockSchema blockSchema;

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

		private final Supplier<? extends Item> componentFactory;

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
		private ContextProcessor<ScanResult> pendingAttributeHandler;

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

		public BlockHandler(BlockSchema blockSchema, BlockHandler parent) {
			checkNotNull(blockSchema);

			this.parent = parent;
			this.blockSchema = blockSchema;

			noEntryLabel = blockSchema.getNoEntryLabel();
			trimWhitespaces = blockSchema.getOptions().getBoolean("trimWhitespaces", false);

			// Process separator, which can either result in a single character or a complex Matcher instance
			Matcher separatorMatcher = null;
			char separatorChar = '\0';

			String separator = blockSchema.getSeparator();
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

			MemberSchema componentSchema = blockSchema.getContainerSchema();
			ItemSupplier itemFactory = new ItemSupplier(componentSchema.getLayerId());
			MutableLong itemIndices = new MutableLong(-1L);
			itemFactory.setIdFactory(itemIndices::increment); //FIXME needs proper linking to index mapping!
			this.componentFactory = itemFactory;

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

			//DEBUG
			attributeHandlers = null;
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
			if(columnIndex>=columnOffsets.length) {
				columnOffsets = Arrays.copyOf(columnOffsets, Math.max(columnOffsets.length<<1, columnIndex));
			}
			int idx = columnIndex<<1;
			columnOffsets[idx] = begin;
			columnOffsets[idx] = end;
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
						begin = separatorMatcher.end()+1;
					}

				} finally {
					// Clear matcher so we don't leave references to the raw content behind
					separatorMatcher.reset("");
				}
			}

			// Remaining stuff goes into a final column
			if(begin<=regionEnd) {
				int idx = columnCount<<1;
				columnOffsets[idx] = begin;
				columnOffsets[idx] = regionEnd;

				columnCount++;
			}

			return columnCount;
		}

		private void moveCursorToColumn(int columnIndex) {
			int idx = columnIndex<<1;
			characterCursor.setRange(columnOffsets[idx], columnOffsets[idx+1]);
		}

		public void processContentLine(InputResolverContext context) {
			final CharSequence rawContent = context.rawData();

			try {
				// Preparation pass: save split offsets for all columns
				final int columnCount = splitColumns(rawContent);
				if(requiredColumnCount>=columnCount)
					throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
							Messages.mismatchMessage("Insufficient columns", _int(requiredColumnCount), _int(columnCount)));

				// New payload item for current line
				context.item = componentFactory.get();

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

				context.container.addItem(context.item);

			} finally {
				context.setData(rawContent);
			}
		}

		public void reset() {
			//TODO
		}
	}
}
