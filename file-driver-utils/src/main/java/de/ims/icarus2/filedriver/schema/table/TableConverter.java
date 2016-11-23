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

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Map;
import java.util.function.Supplier;

import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.schema.SchemaBasedConverter;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.util.io.IOUtil;

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
		int bufferSize = getRecommendedBufferSize(layerManifest);
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

		// Reset buffer (better to do this here than to rely on a reset after every operation)
		clearCharacterBuffer();

		fillCharacterBuffer(cursor.getBlockChannel());

		// TODO Auto-generated method stub
		return null;
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

	protected static class InputResolverContext implements ResolverContext {

		public Container container;
		public int index;
		public Item item;
		public CharSequence data;

		private int lineNumber;

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

	}

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

	public abstract class AttributeHandler {

		private final AttributeSchema attributeSchema;

		protected AttributeHandler(AttributeSchema attributeSchema) {
			checkNotNull(attributeSchema);
			this.attributeSchema = attributeSchema;
		}

		/**
		 * Check the given {@link ResolverContext#rawData() line} against the internal pattern
		 * and optionally
		 * @param context
		 * @return
		 */
		public abstract ScanResult process(ResolverContext context);
	}

	public class EmptyLineDelimiter extends AttributeHandler {

		private final boolean multiline;

		public EmptyLineDelimiter(AttributeSchema attributeSchema, boolean multiline) {
			super(attributeSchema);

			this.multiline = multiline;
		}

		@Override
		public ScanResult process(ResolverContext context) {
			boolean empty = context.rawData().length()==0;

			if(empty) {
				return multiline ? ScanResult.PARTLY_MATCHED : ScanResult.MATCHED;
			} else {
				return ScanResult.FAILED;
			}
		}

	}

	public static final ContextProcessor<ScanResult> CONTENT_DETECTOR = (context) -> {
		return context.rawData().length()==0 ? ScanResult.FAILED : ScanResult.MATCHED;
	};


	public abstract class ColumnHandler {

		private final ColumnSchema columnSchema;
		private final Resolver resolver;
		private final int columnIndex;
	}

	public class BlockHandler {
		private final BlockHandler parent;

		private final BlockSchema blockSchema;
		private final ColumnHandler[] columnHandlers;

		private final Supplier<Container> containerFactory;
		private final Supplier<Item> itemFactory;
		private final Map<String, Item> namedSubstitues;

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
		private ContextProcessor<ScanResult> pendingAttributeHandler;

		public boolean isBeginLine(InputResolverContext context) {
			return beginDelimiter.process(context)!=ScanResult.FAILED;
		}

		public boolean isEndLine(InputResolverContext context) {
			return endDelimiter.process(context)!=ScanResult.FAILED;
		}

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

						// For multiline attribute save the handler
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

		public Container processContentLine(InputResolverContext context) {
			con
		}
	}
}
