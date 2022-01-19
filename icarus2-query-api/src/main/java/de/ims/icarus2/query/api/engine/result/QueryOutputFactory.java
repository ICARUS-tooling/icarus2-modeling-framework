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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.QuerySwitch;
import de.ims.icarus2.query.api.engine.EngineSettings;
import de.ims.icarus2.query.api.engine.EngineSettings.IntField;
import de.ims.icarus2.query.api.engine.QueryOutput;
import de.ims.icarus2.query.api.exp.BinaryOperations.StringMode;
import de.ims.icarus2.query.api.exp.EvaluationContext;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.ExpressionFactory;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.query.api.iql.IqlExpression;
import de.ims.icarus2.query.api.iql.IqlGroup;
import de.ims.icarus2.query.api.iql.IqlResult;
import de.ims.icarus2.query.api.iql.IqlResult.ResultType;
import de.ims.icarus2.query.api.iql.IqlSorting;
import de.ims.icarus2.query.api.iql.IqlSorting.Order;
import de.ims.icarus2.query.api.iql.IqlStream;
import de.ims.icarus2.util.collections.ArrayUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.function.CharBiPredicate;
import de.ims.icarus2.util.function.IntBiPredicate;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class QueryOutputFactory {

	/*
	 * Payload info:
	 *
	 * We have the following types to handle here for sorting:
	 * Integer -> long
	 * FloatingPoint -> double
	 * Text -> CharSequence
	 *
	 */

	private final Set<ResultType> resultTypes = EnumSet.noneOf(ResultType.class);

	private final List<IqlSorting> sortings = new ObjectArrayList<>();
	private final List<IqlGroup> groups = new ObjectArrayList<>();

	// Externally modifiable fields

	private EngineSettings settings = new EngineSettings();

	private Boolean first;
	private Long limit;

	/** Root context used for the entire query. */
	private final EvaluationContext context;

	/** External sink to send final matches or result entries to */
	private ResultSink resultSink;

	private Supplier<ToIntFunction<CharSequence>> encoder;
	private Supplier<IntFunction<CharSequence>> decoder;

	// Internally populated fields

	private Sorter sorter;
	private final List<Extractor> extractors = new ObjectArrayList<>();
	private final Object2IntMap<String> exp2PayloadMap = new Object2IntOpenHashMap<>();

	public QueryOutputFactory(EvaluationContext context) {
		 this.context = requireNonNull(context);
	}

	private ToIntFunction<CharSequence> encoder() {
		checkState("No encoder defined", encoder!=null);
		return encoder.get();
	}
	private IntFunction<CharSequence> decoder() {
		checkState("No decoder defined", decoder!=null);
		return decoder.get();
	}
	private ResultSink resultSink() {
		checkState("No result sink defined", resultSink!=null);
		return resultSink;
	}

	private static String key(IqlExpression expression) {
		String key = checkNotEmpty(expression.getContent());
		Class<?> resultType = expression.getReturnType().orElse(null);
		if(resultType!=null) {
			key = "("+resultType.getTypeName()+")"+key;
		}
		return key;
	}

	private int getPayloadOffset(IqlExpression expression) {
		requireNonNull(expression);
		String key = key(expression);
		int index = exp2PayloadMap.getOrDefault(key, UNSET_INT);
		if(index==UNSET_INT)
			throw new IllegalArgumentException("unknown expression: "+key);
		return index;
	}

	private Extractor ensureExtractor(IqlExpression expression) {
		requireNonNull(expression);
		TypeInfo resultTypeOverride = expression.getReturnType().map(TypeInfo::of).orElse(null);
		String key = key(expression);
		int offset = exp2PayloadMap.getOrDefault(key, UNSET_INT);
		if(offset==UNSET_INT) {
			ExpressionFactory expressionFactory = new ExpressionFactory(context);
			final Expression<?> exp;
			if(resultTypeOverride!=null) {
				exp = expressionFactory.process(expression.getContent(), resultTypeOverride);
			} else {
				exp = expressionFactory.process(expression.getContent());
			}
			offset = extractors.size();
			Extractor extractor = createExtractor(exp);
			extractors.add(extractor);
			exp2PayloadMap.put(key, offset);
		}
		return extractors.get(offset);
	}

	private Extractor createExtractor(Expression<?> expression) {
		if(expression.isBoolean()) {
			return new Extractor.BooleanExtractor(expression);
		} else if(expression.isInteger()) {
			return new Extractor.IntegerExtractor(expression);
		} else if(expression.isFloatingPoint()) {
			return new Extractor.FloatingPointExtractor(expression);
		} else if(expression.isText()) {
			return new Extractor.TextExtractor(expression, encoder());
		}

		throw new QueryException(QueryErrorCode.INCORRECT_USE,
				"Result type not supported for extraction: "+expression.getResultType());
	}

	private static boolean getBool(Boolean b) { return b!=null && b.booleanValue(); }

	private static long getLong(Long i) { return i==null ? UNSET_LONG : i.longValue(); }

	private <B extends ResultBuffer.BuilderBase<B, T, R>,T,R extends ResultBuffer<T>> void initBase(
			ResultBuffer.BuilderBase<B, T, R> builder) {
		builder.collectorBufferSize(settings.getInt(IntField.COLLECTOR_BUFFER_SIZE));
		builder.initialGlobalSize(settings.getInt(IntField.INITIAL_MAIN_BUFFER_SIZE));
	}

	/** Create {@link Match} based buffer without sorting or extraction */
	private ResultBuffer<Match> createPlainBuffer() {
		// Explicitly set limit
		final int intLimit = strictToInt(getLong(limit));
		if(intLimit > 0) {
			ResultBuffer.Limited.Builder<Match> builder = ResultBuffer.Limited.builder(Match.class);
			initBase(builder);
			builder.limit(intLimit);
			return builder.build();
		}
		// Use unlimited buffer
		ResultBuffer.Unlimited.Builder<Match> builder = ResultBuffer.Unlimited.builder(Match.class);
		initBase(builder);
		return builder.build();
	}

	/** Create {@link ResultEntry} based buffer without sorting, but with extraction */
	private ResultBuffer<ResultEntry> createExtractingBuffer() {
		// Explicitly set limit
		final int intLimit = strictToInt(getLong(limit));
		if(intLimit > 0) {
			ResultBuffer.Limited.Builder<ResultEntry> builder = ResultBuffer.Limited.builder(ResultEntry.class);
			initBase(builder);
			builder.limit(intLimit);
			return builder.build();
		}
		// Use unlimited buffer
		ResultBuffer.Unlimited.Builder<ResultEntry> builder = ResultBuffer.Unlimited.builder(ResultEntry.class);
		initBase(builder);
		return builder.build();
	}

	private <B extends ResultBuffer.SortableBase.SortableBuilderBase<B,R>, R extends ResultBuffer<ResultEntry>> void initSortable(
			ResultBuffer.SortableBase.SortableBuilderBase<B,R> builder) {
		initBase(builder);
		builder.initialTmpSize(settings.getInt(IntField.INITIAL_SECONDARY_BUFFER_SIZE));
		if(sorter!=null) {
			builder.sorter(sorter);
		}
	}

	/** Create {@link ResultEntry} based buffer with sorting (and thereby extraction) */
	private ResultBuffer<ResultEntry> createSortingBuffer() {
		// Explicitly set limit
		final int intLimit = strictToInt(getLong(limit));
		if(intLimit > 0) {
			// First N
			if(getBool(first)) {
				ResultBuffer.FirstN.Builder builder = ResultBuffer.FirstN.builder();
				initSortable(builder);
				builder.limit(intLimit);
				return builder.build();
			}

			// Best-N
			ResultBuffer.BestN.Builder builder = ResultBuffer.BestN.builder();
			initSortable(builder);
			builder.limit(intLimit);
			return builder.build();
		}
		// Use unlimited buffer
		ResultBuffer.Sorted.Builder builder = ResultBuffer.Sorted.builder();
		initSortable(builder);
		return builder.build();
	}

	private void createExtractors() {
		for(IqlSorting sorting : sortings) {
			ensureExtractor(sorting.getExpression());
		}
		for(IqlGroup group : groups) {
			ensureExtractor(group.getGroupBy());
			group.getFilterOn().ifPresent(this::ensureExtractor);
		}
		//TODO process expressions inside result instructions
	}

	private Sorter createSorter(IqlSorting sorting, @Nullable Sorter next) {
		int offset = getPayloadOffset(sorting.getExpression());
		int sign = sorting.getOrder()==Order.ASCENDING ? Sorter.SIGN_ASC : Sorter.SIGN_DESC;
		Expression<?> expression = extractors.get(offset).getExpression();

		if(expression.isInteger()) {
			return new Sorter.IntegerSorter(offset, sign, next);
		} else if(expression.isFloatingPoint()) {
			return new Sorter.FloatingPointSorter(offset, sign, next);
		} else if(expression.isText()) {
			StringMode stringMode = context.isSwitchSet(QuerySwitch.STRING_CASE_OFF) ?
					StringMode.IGNORE_CASE : StringMode.DEFAULT;

			if(context.isSwitchSet(QuerySwitch.STRING_UNICODE_OFF)) {
				CharBiPredicate comparator = stringMode.getCharComparator();
				return new Sorter.AsciiSorter(offset, sign, decoder(), comparator, next);
			}

			IntBiPredicate comparator = stringMode.getCodePointComparator();
			return new Sorter.UnicodeSorter(offset, sign, decoder(), comparator, next);
		}

		throw new QueryException(QueryErrorCode.INCORRECT_USE,
				"Result type not supported for sorting: "+expression.getResultType());
	}

	private void createSorter() {
		if(!sortings.isEmpty()) {
			Sorter current = null;
			for (int i = sortings.size()-1; i >= 0; i--) {
				current = createSorter(sortings.get(i), current);
			}
			sorter = current;
		}
	}

	private PayloadReader createPayloadReader(Extractor[] extractors) {
		TypeInfo[] types = Stream.of(extractors)
				.map(Extractor::getExpression)
				.map(Expression::getResultType)
				.toArray(TypeInfo[]::new);
		// Only grab a decoder if we actually need it!
		IntFunction<CharSequence> decoder = ArrayUtils.contains(types, TypeInfo.TEXT) ?
				decoder() : null;
		return new DefaultPayloadReader(types, decoder);
	}

	private boolean needsBuffering() {
		// Could check 'first' flag, but that one is meaningless without 'limit'
		return getLong(limit)>0 || !sortings.isEmpty();
	}

	private boolean needsExtraction() {
		return !groups.isEmpty() || !sortings.isEmpty() || resultTypes.contains(ResultType.CUSTOM);
	}

	public QueryOutput createOutput() {
		final boolean needsBuffering = needsBuffering();
		final boolean needsExtraction = needsExtraction();

		if(needsExtraction) {
			createExtractors();

			final Extractor[] extractors = CollectionUtils.toArray(this.extractors, Extractor[]::new);
			final PayloadReader payloadReader = createPayloadReader(extractors);

			ResultBuffer<ResultEntry> buffer = null;

			if(!groups.isEmpty()) {
				//TODO replace/set resultConsumer with a grouping storage
			}

			if(!sortings.isEmpty()) {
				createSorter();
				// Sorting is always buffered
				buffer = createSortingBuffer();
			} else if(needsBuffering) {
				buffer = createExtractingBuffer();
			}

			// If we constructed a result buffer, we need the matching output wrapper
			if(buffer!=null) {
				return BufferedOutput.extracting(buffer, resultSink(), payloadReader, extractors);
			}

			// Otherwise go unbuffered
			return UnbufferedOutput.extracting(resultSink(), payloadReader, extractors);
		}

		// Limit set, so needs buffer, even without extraction
		if(needsBuffering) {
			ResultBuffer<Match> buffer = createPlainBuffer();
			return BufferedOutput.nonExtracting(buffer, resultSink());
		}

		// No fancy extras, just forward the matches
		return UnbufferedOutput.nonExtracting(resultSink());
	}

	// Setup methods

	public QueryOutputFactory resultTypes(ResultType...resultTypes) {
		requireNonNull(sortings);
		checkArgument("result types array empty", resultTypes.length>0);
		CollectionUtils.feedItems(this.resultTypes, resultTypes);
		return this;
	}

	public QueryOutputFactory sortings(IqlSorting...sortings) {
		requireNonNull(sortings);
		checkArgument("sortings array empty", sortings.length>0);
		CollectionUtils.feedItems(this.sortings, sortings);
		return this;
	}

	public QueryOutputFactory groups(IqlGroup...groups) {
		requireNonNull(groups);
		checkArgument("groups array empty", groups.length>0);
		CollectionUtils.feedItems(this.groups, groups);
		return this;
	}

	public QueryOutputFactory settings(EngineSettings settings) {
		this.settings = requireNonNull(settings);
		return this;
	}

	public QueryOutputFactory first(boolean first) {
		checkState("'first' flag already set", this.first==null);
		this.first = Boolean.valueOf(first);
		return this;
	}

	public QueryOutputFactory limit(long limit) {
		checkState("limit already set", this.limit==null);
		this.limit = Long.valueOf(limit);
		return this;
	}

	/** External sink to send final matches or result entries to */
	public QueryOutputFactory resultSink(ResultSink resultSink) {
		requireNonNull(resultSink);
		checkState("result sink already set", this.resultSink==null);
		this.resultSink = resultSink;
		return this;
	}

	/** Lazy access to the underlying encoder. Repeated calls should return the same object! */
	public QueryOutputFactory encoder(Supplier<ToIntFunction<CharSequence>> encoder) {
		requireNonNull(encoder);
		checkState("encoder already set", this.encoder==null);
		this.encoder = encoder;
		return this;
	}

	/** Lazy access to the underlying decoder. Repeated calls should return the same object! */
	public QueryOutputFactory decoder(Supplier<IntFunction<CharSequence>> decoder) {
		requireNonNull(decoder);
		checkState("decoder already set", this.decoder==null);
		this.decoder = decoder;
		return this;
	}

	public QueryOutputFactory applyFromResult(IqlResult result) {
		requireNonNull(result);
		result.getLimit().ifPresent(this::limit);
		first(result.isFirst());

		Set<ResultType> resultTypes = result.getResultTypes();
		if(!resultTypes.isEmpty()) {
			resultTypes(CollectionUtils.toArray(resultTypes, ResultType[]::new));
		}

		List<IqlSorting> sortings = result.getSortings();
		if(!sortings.isEmpty()) {
			sortings(CollectionUtils.toArray(sortings, IqlSorting[]::new));
		}

		return this;
	}

	public QueryOutputFactory applyFromStream(IqlStream stream) {
		requireNonNull(stream);

		List<IqlGroup> groups = stream.getGrouping();
		if(!groups.isEmpty()) {
			groups(CollectionUtils.toArray(groups, IqlGroup[]::new));
		}

		applyFromResult(stream.getResult());

		return this;
	}
}
