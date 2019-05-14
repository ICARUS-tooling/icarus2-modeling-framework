/**
 *
 */
package de.ims.icarus2.model.standard.view.streamed;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.api.view.streamed.StreamOption;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusView;
import de.ims.icarus2.model.standard.view.AbstractCorpusView;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(StreamedCorpusView.class)
public class DefaultStreamedCorpusView extends AbstractCorpusView implements StreamedCorpusView {

	private static final Logger log = LoggerFactory.getLogger(DefaultStreamedCorpusView.class);

	public static Builder builder() {
		return new Builder();
	}

	protected final ItemLayerManager itemLayerManager;
	protected final Set<StreamOption> streamOptions = EnumSet.noneOf(StreamOption.class);
	protected final ItemStreamBuffer buffer;

	public static final int DEFAULT_BUFFER_CAPACITY = 10_000;

	/**
	 * @param builder
	 */
	protected DefaultStreamedCorpusView(Builder builder) {
		super(builder);

		itemLayerManager = builder.getItemLayerManager();
		streamOptions.addAll(builder.getStreamOptions());

		buffer = new ItemStreamBuffer(
				itemLayerManager,
				getScope().getPrimaryLayer(),
				builder.getBufferCapacity());
	}

	protected final void checkSkipSupported() {
		if(!streamOptions.contains(StreamOption.ALLOW_SKIP))
			throw new ModelException(ModelErrorCode.STREAM_SKIP_NOT_SUPPORTED,
					"Skip not supported - please use the "+StreamOption.ALLOW_SKIP
					+" option when constructing the stream to enable skip functionality");
	}

	protected final void checkMarkSupported() {
		if(!streamOptions.contains(StreamOption.ALLOW_MARK))
			throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SUPPORTED,
					"Mark not supported - please use the "+StreamOption.ALLOW_MARK
					+" option when constructing the stream to enable mark functionality");
	}

	protected final void checkHasItem() {
		if(!buffer.hasItem())
			throw new ModelException(ModelErrorCode.STREAM_NO_ITEM,
					"No item available");
	}

	protected final void checkHasMark() {
		if(!buffer.hasMark())
			throw new ModelException(ModelErrorCode.STREAM_MARK_NOT_SET,
					"No mark has been set");
	}

	@Override
	public long getSize() {
		return itemLayerManager.getItemCount(getScope().getPrimaryLayer());
	}

	@Override
	public Set<StreamOption> getOptions() {
		return Collections.unmodifiableSet(streamOptions);
	}

	@Override
	public boolean hasOption(StreamOption option) {
		requireNonNull(option);
		return streamOptions.contains(option);
	}

	@Override
	public boolean advance() {
		return buffer.advance();
	}

	@Override
	public boolean hasItem() {
		return buffer.hasItem();
	}

	@Override
	public Item currentItem() {
		checkHasItem();
		return buffer.currentItem();
	}

	/**
	 * @see de.ims.icarus2.model.api.view.streamed.StreamedCorpusView#skip(long)
	 */
	@Override
	public boolean skip(long n) {
		checkSkipSupported();

		buffer.skip(n);

		return true;
	}

	@Override
	public void flush() {
		buffer.flush();
	}

	@Override
	public boolean mark() {
		checkMarkSupported();
		checkHasItem();

		buffer.mark();

		return true;
	}

	@Override
	public boolean hasMark() {
		checkMarkSupported();
		return buffer.hasMark();
	}

	@Override
	public boolean clearMark() {
		checkMarkSupported();
		checkHasMark();

		buffer.clearMark();

		return true;
	}

	@Override
	public boolean reset() {
		checkMarkSupported();
		checkHasMark();

		buffer.reset();

		return true;
	}

	@Override
	public boolean wouldInvalidateMark() {
		checkMarkSupported();
		checkHasMark();

		return buffer.wouldInvalidateMark();
	}

	@Override
	protected void closeImpl() throws InterruptedException {
		buffer.close();
	}

	public static class Builder extends AbstractCorpusView.Builder<Builder, DefaultStreamedCorpusView> {
		private ItemLayerManager itemLayerManager;
		private final Set<StreamOption> streamOptions = EnumSet.noneOf(StreamOption.class);
		private Integer bufferCapacity;

		protected Builder() {
			// no-op
		}

		public Builder itemLayerManager(ItemLayerManager itemLayerManager) {
			requireNonNull(itemLayerManager);
			checkState(this.itemLayerManager==null);

			this.itemLayerManager = itemLayerManager;

			return thisAsCast();
		}

		public ItemLayerManager getItemLayerManager() {
			return itemLayerManager;
		}

		public Builder bufferCapacity(int bufferCapacity) {
			checkArgument(bufferCapacity>0);
			checkState(this.bufferCapacity==null);

			this.bufferCapacity = Integer.valueOf(bufferCapacity);

			return thisAsCast();
		}

		public int getBufferCapacity() {
			return bufferCapacity==null ? DEFAULT_BUFFER_CAPACITY : bufferCapacity.intValue();
		}

		/**
		 * Configures this builder to use the specified {@link StreamOption} values.
		 * If an option is not supported, this method will silently ignore it and send
		 * an appropriate message to the underlying logs.
		 *
		 * @param options
		 * @return
		 */
		public Builder streamOptions(StreamOption...options) {
			requireNonNull(options);
			checkArgument("Options must not be empty", options.length>0);

			for(StreamOption option : options) {
				if(isOptionSupported(option)) {
					streamOptions.add(option);
				} else {
					log.warn("Stream option not supported: "+option);
				}
			}

			return this;
		}

		/**
		 * Configures this builder to have all available {@link StreamOption}
		 * values set.
		 * @return
		 */
		public Builder withAllOptions() {
			return streamOptions(StreamOption.values());
		}

		/**
		 * Hook for subclasses to check whether the given {@link StreamOption}
		 * is supported.
		 *
		 * @param option
		 * @return
		 */
		protected boolean isOptionSupported(StreamOption option) {
			return true;
		}

		public Set<StreamOption> getStreamOptions() {
			return Collections.unmodifiableSet(streamOptions);
		}

		/**
		 * Streams <b>must</b> be created with read access!!!
		 * @see de.ims.icarus2.model.standard.view.AbstractCorpusView.Builder#isAccessModeSupported(de.ims.icarus2.util.AccessMode)
		 */
		@Override
		protected boolean isAccessModeSupported(AccessMode accessMode) {
			return accessMode.isRead();
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("Missing item layer manager", itemLayerManager!=null);
		}

		/**
		 * @see de.ims.icarus2.util.AbstractBuilder#create()
		 */
		@Override
		protected DefaultStreamedCorpusView create() {
			return new DefaultStreamedCorpusView(this);
		}

	}
}
