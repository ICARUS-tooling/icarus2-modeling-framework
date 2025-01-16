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
/**
 *
 */
package de.ims.icarus2.common.formats.conll;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.Report.ReportItemCollector;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.filedriver.AbstractConverter;
import de.ims.icarus2.filedriver.ElementFlag;
import de.ims.icarus2.filedriver.FileDriver;
import de.ims.icarus2.filedriver.FileDriver.LockableFileObject;
import de.ims.icarus2.filedriver.analysis.Analyzer;
import de.ims.icarus2.filedriver.analysis.AnnotationAnalyzer;
import de.ims.icarus2.filedriver.analysis.DefaultItemLayerAnalyzer;
import de.ims.icarus2.filedriver.analysis.DefaultStructureLayerAnalyzer;
import de.ims.icarus2.filedriver.analysis.ItemLayerAnalyzer;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.driver.ChunkState;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingWriter;
import de.ims.icarus2.model.api.driver.mapping.WritableMapping;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.Item.ManagedItem;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.standard.driver.ChunkConsumer;
import de.ims.icarus2.model.standard.members.structure.builder.StructureBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.collections.set.DataSets;
import de.ims.icarus2.util.function.TriConsumer;
import de.ims.icarus2.util.id.StaticIdentity;
import de.ims.icarus2.util.io.IOUtil;
import de.ims.icarus2.util.nio.FastChannelReader;
import de.ims.icarus2.util.strings.CharLineBuffer;
import de.ims.icarus2.util.strings.Splitable;
import de.ims.icarus2.util.strings.StringPrimitives;
import de.ims.icarus2.util.strings.StringUtil;
import de.ims.icarus2.util.strings.ToStringBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class CoNLL2009Converter extends AbstractConverter {


	public static final String KEY_EMPTY_LABEL = "de.ims.icarus2.conll09.empty.label";
	public static final String KEY_FEATURES_EXPAND = "de.ims.icarus2.conll09.features.expand";
	public static final String KEY_FEATURES_DELIMITER = "de.ims.icarus2.conll09.features.delimiter";
	public static final String KEY_FEATURES_ASSIGN = "de.ims.icarus2.conll09.features.assign";

	@VisibleForTesting
	static class LayerConfig {
		public final String propertyKey, defaultValue;

		public LayerConfig(String propertyKey, String defaultValue) {
			this.propertyKey = checkNotEmpty(propertyKey);
			this.defaultValue = checkNotEmpty(defaultValue);
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(this)
					.add("defaultValue", defaultValue)
					.add("propertyKey", propertyKey)
					.build();
		}
	}

	@VisibleForTesting
	static class CoNLL09Config {
		private static final String prefix = "conll09.layer.";
		public static final LayerConfig TOKEN = new LayerConfig(prefix+"token", "token");
		public static final LayerConfig SENTENCE = new LayerConfig(prefix+"sentence", "sentence");
		public static final LayerConfig FORM = new LayerConfig(prefix+"form", "form");
		public static final LayerConfig GOLD_SYNTAX = new LayerConfig(prefix+"gold.dependency", "goldDependency");
		public static final LayerConfig PREDICTED_SYNTAX = new LayerConfig(prefix+"predicted.dependency", "predictedDependency");
		public static final LayerConfig GOLD_LEMMA = new LayerConfig(prefix+"gold.lemma", "goldLemma");
		public static final LayerConfig PREDICTED_LEMMA = new LayerConfig(prefix+"predicted.lemma", "predictedLemma");
		public static final LayerConfig GOLD_POS = new LayerConfig(prefix+"gold.pos", "goldPos");
		public static final LayerConfig PREDICTED_POS = new LayerConfig(prefix+"predicted.pos", "predictedPos");
		public static final LayerConfig GOLD_FEATURES = new LayerConfig(prefix+"gold.features", "goldFeatures");
		public static final LayerConfig PREDICTED_FEATURES = new LayerConfig(prefix+"predicted.features", "predictedFeatures");
		public static final LayerConfig GOLD_DEPENDENCY_RELATION = new LayerConfig(prefix+".gold.dependency.relation", "goldDependencyRelation");
		public static final LayerConfig PREDICTED_DEPENDENCY_RELATION = new LayerConfig(prefix+"predicted.dependency.relation", "predictedDependencyRelation");
	}

	private Charset encoding;
	private String emptyLabel;
	private boolean expandFeatures = false;
	private char featuresDelimiter = '\0', featuresAssign = '\0';
	private LayerMemberFactory memberFactory;
	private Matcher m_feats = null;

	private static final char DEFAULT_FEATURE_ASSIGN = '=';
	private static final char DEFAULT_FEATURE_DELIMITER = '|';

	private ItemRef<ItemLayer, Item> i_token;
	private ItemRef<ItemLayer, Container> i_sentence;
	private StructureRef s_syntax;
	private StructureRef s_psyntax;
	private AnnotationRef a_form;
	private AnnotationRef a_lemma;
	private AnnotationRef a_plemma;
	private AnnotationRef a_pos;
	private AnnotationRef a_ppos;
	private AnnotationRef a_feat;
	private AnnotationRef a_pfeat;
	private AnnotationRef a_deprel;
	private AnnotationRef a_pdeprel;

	private Reference2ObjectMap<LayerConfig, Layer> layerLookup = new Reference2ObjectOpenHashMap<>();

	@SuppressWarnings("unchecked")
	@VisibleForTesting
	<T extends Layer> T getLayer(LayerConfig config) {
		Layer layer = layerLookup.get(config);
		checkArgument("No layer resolved for "+config, layer!=null);
		return (T) layer;
	}

	private static char extractChar(Optional<String> source, char def) {
		if(source.isPresent()) {
			String s = source.get();
			checkArgument("Delimiter and assignment must consist of a single symbol only: "+s, s.length()==1);
			return s.charAt(0);
		}
		return def;
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#addNotify(de.ims.icarus2.model.api.driver.Driver)
	 */
	@Override
	public void addNotify(Driver owner) {
		super.addNotify(owner);

		FileDriver driver = (FileDriver) owner;

		encoding = driver.getEncoding();
		memberFactory = driver.newMemberFactory();

		Context context = driver.getContext();
		checkState("no driver", context!=null);

		DriverManifest manifest = driver.getManifest();

		emptyLabel = manifest.<String>getPropertyValue(KEY_EMPTY_LABEL).orElse(US);
		expandFeatures = manifest.<Boolean>getPropertyValue(KEY_FEATURES_EXPAND)
				.orElse(Boolean.FALSE)
				.booleanValue();
		featuresDelimiter = extractChar(manifest.getPropertyValue(KEY_FEATURES_DELIMITER), DEFAULT_FEATURE_DELIMITER);
		featuresAssign = extractChar(manifest.getPropertyValue(KEY_FEATURES_ASSIGN), DEFAULT_FEATURE_ASSIGN);

		if(expandFeatures) {
			@SuppressWarnings("boxing")
			String pattern = String.format("([^\\%s])\\%s([^\\%s])\\%s?", featuresAssign, featuresAssign, featuresDelimiter, featuresDelimiter);
			m_feats = Pattern.compile(pattern).matcher("");
		}
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#removeNotify(de.ims.icarus2.model.api.driver.Driver)
	 */
	@Override
	public void removeNotify(Driver owner) {
		// TODO Auto-generated method stub
		super.removeNotify(owner);
	}

	private <L extends Layer> L resolveLayer(Context ctx, ModuleManifest manifest, LayerConfig config) {
		String idToUse = manifest.<String>getPropertyValue(config.propertyKey).orElse(config.defaultValue);
		L layer = ctx.getLayer(idToUse);
		if(layerLookup.putIfAbsent(config, layer)!=null)
			throw new ModelException(getDriver().getCorpus(), GlobalErrorCode.INTERNAL_ERROR,
					"Layer already resolved for config: "+config);
		return layer;
	}

	private <T extends Item> LongFunction<T> makeAlive(LongFunction<T> orig_gen) {
		return index -> {
			T result = orig_gen.apply(index);
			if(result instanceof ManagedItem) {
				((ManagedItem)result).setAlive(true);
			}
			return result;
		};
	}

	private ItemRef<ItemLayer, Item> resolveToken(Context ctx, ModuleManifest manifest, LayerConfig config) {
		final ItemLayer layer = resolveLayer(ctx, manifest, config);
		final Container host = layer.getProxyContainer();
		final LongFunction<Item> supplier = makeAlive(index -> memberFactory.newItem(host, index));
		final FileDriver driver = getDriver();
		final IntFunction<ItemLayerAnalyzer> gen_analyzer = fileIndex
				-> new DefaultItemLayerAnalyzer(driver.getFileStates(), layer, fileIndex);
		return new ItemRef<>(layer, supplier, gen_analyzer);
	}

	private ItemRef<ItemLayer, Container> resolveSentence(Context ctx, ModuleManifest manifest, LayerConfig config) {
		final ItemLayer layer = resolveLayer(ctx, manifest, config);
		final ItemLayerManifestBase<?> layerManifest = layer.getManifest();
		final ContainerManifestBase<?> mf = layerManifest.getRootContainerManifest().orElseThrow(
				ManifestException.missing(layerManifest, "root container"));
		final DataSet<Container> baseContainers = DataSets.createDataSet(i_token.layer.getProxyContainer());
		final Container host = layer.getProxyContainer();
		final LongFunction<Container> supplier = makeAlive(index -> memberFactory.newContainer(mf, host, baseContainers, null, index));
		final FileDriver driver = getDriver();
		final IntFunction<ItemLayerAnalyzer> gen_analyzer = fileIndex
				-> new DefaultItemLayerAnalyzer(driver.getFileStates(), layer, fileIndex);
		return new ItemRef<ItemLayer, Container>(layer, supplier, gen_analyzer);
	}

	private StructureRef resolveStructure(Context ctx, ModuleManifest manifest, LayerConfig config) {
		final StructureLayer layer = resolveLayer(ctx, manifest, config);
		final StructureLayerManifest layerManifest = layer.getManifest();
		final StructureManifest mf = layerManifest.getRootStructureManifest().orElseThrow(
				ManifestException.missing(layerManifest, "root structure"));

		final StructureBuilder builder = StructureBuilder.builder(mf);
		builder.augmented(false);
		builder.host(layer.getProxyContainer());
		builder.memberFactory(memberFactory);
		final FileDriver driver = getDriver();
		final IntFunction<ItemLayerAnalyzer> gen_analyzer = fileIndex
				-> new DefaultStructureLayerAnalyzer(driver.getFileStates(), layer, fileIndex);
		return new StructureRef(layer, builder, gen_analyzer);
	}

	private AnnotationRef resolveAnnotation(Context ctx, ModuleManifest manifest, LayerConfig config) {
		String idToUse = manifest.<String>getPropertyValue(config.propertyKey).orElse(config.defaultValue);
		final String key;
		final AnnotationLayer layer;
		int split = idToUse.indexOf('$');
		if(split!=-1) {
			key = checkNotEmpty(idToUse.substring(split+1));
			layer = ctx.getLayer(idToUse.substring(0, split));
		} else {
			layer = resolveLayer(ctx, manifest, config);
			key = layer.getManifest().getDefaultKey().orElseThrow(
					ManifestException.missing(layer.getManifest(), "defaultKey"));
		}
		final IntFunction<AnnotationAnalyzer<String>> gen_analyzer = fileIndex -> (item,annotationKey,value) -> { /* no-op */};
		return new AnnotationRef(layer, key, gen_analyzer);
	}

	/**
	 * @see de.ims.icarus2.model.standard.driver.mods.AbstractDriverModule#doPrepare(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest, de.ims.icarus2.model.api.driver.mods.ModuleMonitor)
	 */
	@Override
	protected boolean doPrepare(ModuleManifest manifest, ModuleMonitor monitor) throws InterruptedException {
		Context context = getDriver().getContext();

		i_token = resolveToken(context, manifest, CoNLL09Config.TOKEN);
		i_sentence = resolveSentence(context, manifest, CoNLL09Config.SENTENCE);
		s_syntax = resolveStructure(context, manifest, CoNLL09Config.GOLD_SYNTAX);
		s_psyntax = resolveStructure(context, manifest, CoNLL09Config.PREDICTED_SYNTAX);
		a_form = resolveAnnotation(context, manifest, CoNLL09Config.FORM);
		a_lemma = resolveAnnotation(context, manifest, CoNLL09Config.GOLD_LEMMA);
		a_plemma = resolveAnnotation(context, manifest, CoNLL09Config.PREDICTED_LEMMA);
		a_pos = resolveAnnotation(context, manifest, CoNLL09Config.GOLD_POS);
		a_ppos = resolveAnnotation(context, manifest, CoNLL09Config.PREDICTED_POS);
		a_feat = resolveAnnotation(context, manifest, CoNLL09Config.GOLD_FEATURES);
		a_pfeat = resolveAnnotation(context, manifest, CoNLL09Config.PREDICTED_FEATURES);
		a_deprel = resolveAnnotation(context, manifest, CoNLL09Config.GOLD_DEPENDENCY_RELATION);
		a_pdeprel = resolveAnnotation(context, manifest, CoNLL09Config.PREDICTED_DEPENDENCY_RELATION);

		return true;
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#scanFile(int)
	 */
	@Override
	public Report<ReportItem> scanFile(int fileIndex) throws IOException, InterruptedException, IcarusApiException {

		final FileDriver driver = getDriver();
		final ReportBuilder<ReportItem> reportBuilder = ReportBuilder.builder(
				new StaticIdentity(ManifestUtils.requireId(driver.getManifest())));
		final LockableFileObject file = driver.getFileObject(fileIndex);

		LoadResult loadResult = readFile(fileIndex, reportBuilder, null, ReadMode.SCAN);
		long loaded = loadResult.loadedChunkCount();

		if(loaded==0) {
			reportBuilder.addError(ModelErrorCode.DRIVER_ERROR, "File %s at index %d is empty", file.getResource().getPath(), _int(fileIndex));
		} else if(loadResult.chunkCount(ChunkState.CORRUPTED)>0) {
			reportBuilder.addError(ModelErrorCode.DRIVER_ERROR, "File %s at index%d contains invalid/corrupted corpus data",
					file.getResource().getPath(), _int(fileIndex));
		}

		long discarded = loadResult.discard();
		if(discarded==0) {
			reportBuilder.addError(ModelErrorCode.DRIVER_ERROR, "Inconsistency when discarding chunks ");
		}

		final Report<ReportItem> report = reportBuilder.build();

		// Assign respective flag to file
		ElementFlag flagForFile = report.hasErrors() ? ElementFlag.UNUSABLE : ElementFlag.SCANNED;
		driver.getFileStates().getFileInfo(fileIndex).setFlag(flagForFile);

		return report;
	}

	/**
	 * @see de.ims.icarus2.filedriver.Converter#loadFile(int, de.ims.icarus2.model.standard.driver.ChunkConsumer)
	 */
	@Override
	public LoadResult loadFile(int fileIndex, @Nullable ChunkConsumer action)
			throws IOException, InterruptedException, IcarusApiException {

		// No post-processing of result, that's the surrounding driver's job
		return readFile(fileIndex, null, action, ReadMode.FILE);
	}

	@SuppressWarnings("unused")
	private static final int ID09 = 0;
	private static final int FORM09 = 1;
	private static final int LEMMA09 = 2;
	private static final int PLEMMA09 = 3;
	private static final int POS09 = 4;
	private static final int PPOS09 = 5;
	private static final int FEAT09 = 6;
	private static final int PFEAT09 = 7;
	private static final int HEAD09 = 8;
	private static final int PHEAD09 = 9;
	private static final int DEPREL09 = 10;
	private static final int PDEPREL09 = 11;
	@SuppressWarnings("unused")
	private static final int FILLPRED09 = 12;
	@SuppressWarnings("unused")
	private static final int PRED09 = 13;

	// APREDs from index 14 on

	// Number of columns of interest (skip APREDs)
	private final static int COL_LIMIT09 = 12;
	private static final String US = "_";
	private static final String DELIMITER = "\\s+";
	private static final String EMPTY = "";

	private int readHead(Splitable row, int index) {
		Splitable cursor = row.getSplitCursor(index);

		int value = UNSET_INT;

		if(!cursor.isEmpty() && !StringUtil.equals(cursor, emptyLabel)) {
			value = StringPrimitives.parseInt(cursor);
		}

		cursor.recycle();

		return value;
	}

	private String readCol(Splitable row, int index) {
		Splitable cursor = row.getSplitCursor(index);
		String s = EMPTY;
		if(!cursor.isEmpty() && !StringUtil.equals(cursor, emptyLabel)) {
			s = cursor.toString().trim();
		}
		cursor.recycle();
		return s;
	}

	private void readCol(AnnotationRef ref, Splitable row, int index, @Nullable String def, Item item) {
		Splitable cursor = row.getSplitCursor(index);
		String s = null;
		if(cursor.isEmpty() || StringUtil.equals(cursor, emptyLabel)) {
			s = def;
		} else {
			s = cursor.toString().trim();

			// expanding the features means we need to parks key-value pairs and store them in the features layer
			if(expandFeatures && (ref==a_feat || ref==a_pfeat)) {
				assert m_feats!=null : "no feature matcher constructed";

				m_feats.reset(s);
				while(m_feats.find()) {
					String key = m_feats.group(1);
					String value = m_feats.group(2);
					ref.unknownSetter.accept(item, key, value);
				}
			}
		}
		cursor.recycle();

		if(s!=null && !s.isEmpty()) {
			ref.setter.accept(item, s);
		}
	}

	private Structure makeStructure(Container sentence, StructureBuilder builder,
			AnnotationRef depRelAnno, IntList heads, List<String> depRels) {
		assert heads.size()==depRels.size();
		int size = heads.size();
		for (int i = 0; i < size; i++) {
			final int head = heads.getInt(i);
			if(head<0) {
				continue;
			}
			final Item target = sentence.getItemAt(i);
			final Item source;
			if(head==0) {
				source = builder.getRoot();
			} else {
				source = sentence.getItemAt(head-1);
			}
			final Edge edge = builder.newEdge(target.getId(), source, target);
			builder.addEdge(edge);
			depRelAnno.setter.accept(edge, depRels.get(i));
		}
		Structure structure = builder.build();
		if(structure instanceof ManagedItem) {
			((ManagedItem)structure).setAlive(true);
		}
		return structure;
	}

	private InputCache cache(LayerRef<? extends ItemLayer, ?> ref) {
		return getDriver().getLayerBuffer(ref.layer).newCache(IcarusUtils.DO_NOTHING(), true);
	}

	private @Nullable <T extends Analyzer> T analyzer(LayerRef<?, T> ref, int fileIndex, ReadMode mode, ReportItemCollector log) {
		if(mode==ReadMode.SCAN) {
			T analyzer = ref.gen_analyzer.apply(fileIndex);
			analyzer.init(log);
			return analyzer;
		}
		return null;
	}

	private <A extends ItemLayerAnalyzer, T extends Item> void analyze(T item, A analyzer) {
		if(analyzer!=null) {
			analyzer.accept(item, item.getId());
		}
	}

	private void commit(Analyzer...analyzers) {
		for(Analyzer analyzer : analyzers) {
			if(analyzer!=null) {
				analyzer.finish();
			}
		}
	}

	private MappingWriter writer(Mapping mapping) {
		if(mapping instanceof WritableMapping) {
			return ( (WritableMapping)mapping).newWriter();
		}
		return null;
	}

	private void beginOrEnd(boolean begin, MappingWriter...writers) {
		for (int i = 0; i < writers.length; i++) {
			MappingWriter writer = writers[i];
			if(writer==null) {
				continue;
			} else if(begin) {
				writer.begin();
			} else {
				writer.end();
			}
		}
	}

	private void map(Container sentence, MappingWriter w_sent2tok, MappingWriter w_tok2sent) {
		long sentenceIndex = sentence.getId();
		long firstTokenIndex = sentence.getBeginOffset();
		long lastTokenIndex = sentence.getEndOffset();

		w_sent2tok.map(sentenceIndex, sentenceIndex, firstTokenIndex, lastTokenIndex);
		w_tok2sent.map(firstTokenIndex, lastTokenIndex, sentenceIndex, sentenceIndex);
	}

	private LoadResult readFile(int fileIndex, @Nullable ReportBuilder<ReportItem> reportBuilder,
			@Nullable ChunkConsumer action, ReadMode mode) throws IOException {

		final FileDriver driver = getDriver();
		final LockableFileObject fileObject = driver.getFileObject(fileIndex);

		final StructureBuilder b_syntax = s_syntax.builder;
		final StructureBuilder b_psyntax = s_psyntax.builder;
		final LongFunction<Container> gen_sentence = i_sentence.gen_member;
		final LongFunction<Item> gen_token = i_token.gen_member;

		final InputCache c_token = cache(i_token);
		final InputCache c_sentence = cache(i_sentence);
		final InputCache c_syntax = cache(s_syntax);
		final InputCache c_psyntax = cache(s_psyntax);

		final ItemLayerAnalyzer a_token = analyzer(i_token, fileIndex, mode, reportBuilder);
		final ItemLayerAnalyzer a_sentence = analyzer(i_sentence, fileIndex, mode, reportBuilder);
		final ItemLayerAnalyzer a_syntax = analyzer(s_syntax, fileIndex, mode, reportBuilder);
		final ItemLayerAnalyzer a_psyntax = analyzer(s_psyntax, fileIndex, mode, reportBuilder);
		//TODO annotation analyzers

		final SimpleLoadResult loadResult = new SimpleLoadResult(list(c_token, c_sentence, c_syntax, c_psyntax));

		try(ReadableByteChannel channel = fileObject.getResource().getReadChannel();
				Reader reader = new FastChannelReader(channel, encoding.newDecoder(), IOUtil.DEFAULT_BUFFER_SIZE)) {

			// Note: we don't need to explicitly close the buffer, as the underlying reader will automatically be closed
			final CharLineBuffer buffer = new CharLineBuffer();
			buffer.startReading(reader);

			final IntList heads = new IntArrayList();
			final IntList pheads = new IntArrayList();
			final List<String> depRels = new ObjectArrayList<>();
			final List<String> pdepRels = new ObjectArrayList<>();

			final MappingWriter w_tok2sent = writer(driver.getMapping(i_token.layer, i_sentence.layer));
			final MappingWriter w_sent2tok = writer(driver.getMapping(i_sentence.layer, i_token.layer));

			// Index of current line
			int lineIndex = 0;
			// Number of consecutive items in container
			int itemCount = 0;
			// Next index to use for new container
			long sentenceIndex = getStartingIndex(i_sentence.layer, fileIndex, ReadMode.SCAN);
			// Next index to use for new token
			long tokenIndex = getStartingIndex(i_token.layer, fileIndex, ReadMode.SCAN);
			// Current sentence
			Container sentence = null;
			// Current item
			Item token = null;

			Consumer<Container> finalizer = sent -> {
				// Store sentence first
				c_sentence.offer(sent, sent.getId());

				// Now build all the structures
				Structure syntax = makeStructure(sent, b_syntax, a_deprel, heads, depRels);
				Structure psyntax = makeStructure(sent, b_psyntax, a_pdeprel, pheads, pdepRels);

				// Reset buffers
				heads.clear();
				pheads.clear();
				depRels.clear();
				pdepRels.clear();

				// Finally store structures
				c_syntax.offer(syntax, syntax.getId());
				c_psyntax.offer(psyntax, psyntax.getId());

				// Write mapping if desired
				map(sent, w_sent2tok, w_tok2sent);

				// Perform actual analysis during scan
				analyze(sent, a_sentence);
				analyze(syntax, a_syntax);
				analyze(psyntax, a_psyntax);

				if(action!=null) {
					action.accept(sent.getId(), sent, ChunkState.VALID);
				}

				loadResult.accept(sent.getId(), sent, ChunkState.VALID);
			};

			beginOrEnd(true, w_sent2tok, w_tok2sent);

			try {
				while(buffer.next()) {

					if(buffer.isEmpty()) {
						if(itemCount>0) {
							finalizer.accept(sentence);
							sentence = null;
						}

						itemCount = 0;
					} else {
						// Read current line

						// If we don't have a sentence context yet, create one and init syntax builders
						if(sentence==null) {
							sentence = gen_sentence.apply(sentenceIndex);
							b_syntax.setId(sentenceIndex);
							b_syntax.setBoundaryContainer(sentence);
							b_syntax.setBaseContainer(sentence);
							b_syntax.createRoot();

							b_psyntax.setId(sentenceIndex);
							b_psyntax.setBoundaryContainer(sentence);
							b_psyntax.setBaseContainer(sentence);
							b_psyntax.createRoot();

							sentenceIndex++;
						}

						if(buffer.split(DELIMITER, COL_LIMIT09)<COL_LIMIT09)
							throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
									"Incorrect column count in data file at line "+ (lineIndex+1) // use human readable index here
									+ ", are you sure this is the right format for CoNLL 09?");

						token = gen_token.apply(tokenIndex++);

						c_token.offer(token, token.getId());
						sentence.addItem(token);

						readCol(a_form, buffer, FORM09, "<empty>", token);
						readCol(a_lemma, buffer, LEMMA09, null, token);
						readCol(a_plemma, buffer, PLEMMA09, null, token);
						readCol(a_pos, buffer, POS09, null, token);
						readCol(a_ppos, buffer, PPOS09, null, token);
						readCol(a_feat, buffer, FEAT09, null, token);
						readCol(a_pfeat, buffer, PFEAT09, null, token);

						depRels.add(readCol(buffer, DEPREL09));
						pdepRels.add(readCol(buffer, PDEPREL09));

						heads.add(readHead(buffer, HEAD09));
						pheads.add(readHead(buffer, PHEAD09));

						b_syntax.addNode(token);
						b_psyntax.addNode(token);

						analyze(token, a_token);

						//TODO read predicate columns

						itemCount++;
					}

					lineIndex++;
				}

				// Make sure we take care of the final sentence
				if(itemCount>0) {
					finalizer.accept(sentence);
				}
			} finally {
				beginOrEnd(false, w_sent2tok, w_tok2sent);
			}
		}

		//TODO finish annotation analyzers
		commit(a_token, a_sentence, a_syntax, a_psyntax);

		// Final cleanup duty
		if(m_feats!=null) {
			m_feats.reset("");
		}

		return loadResult;
	}

	/**
	 * This implementation does not support chunking and as such has no use for cursors.
	 *
	 * @see de.ims.icarus2.filedriver.AbstractConverter#createDelegatingCursor(int, de.ims.icarus2.model.api.layer.ItemLayer)
	 */
	@Override
	protected DelegatingCursor<?> createDelegatingCursor(int fileIndex, ItemLayer layer) {
		return null;
	}

	/**
	 * @see de.ims.icarus2.filedriver.AbstractConverter#readItemFromCursor(de.ims.icarus2.filedriver.AbstractConverter.DelegatingCursor)
	 */
	@Override
	protected Item readItemFromCursor(DelegatingCursor<?> cursor)
			throws IOException, InterruptedException, IcarusApiException {
		throw new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Implementatio ndoes not support cursor interactions");
	}

	static class LayerRef<T extends Layer, A extends Analyzer> {
		final T layer;
		final IntFunction<A> gen_analyzer;
		public LayerRef(T layer, IntFunction<A> gen_analyzer) {
			this.layer = requireNonNull(layer);
			this.gen_analyzer = requireNonNull(gen_analyzer);
		}

	}

	static class ItemRef<T extends ItemLayer, M extends Item> extends LayerRef<T, ItemLayerAnalyzer> {
		final LongFunction<M> gen_member;
		public ItemRef(T layer, LongFunction<M> generator, IntFunction<ItemLayerAnalyzer> gen_analyzer) {
			super(layer, gen_analyzer);
			this.gen_member = requireNonNull(generator);
		}
	}

	static class StructureRef extends LayerRef<StructureLayer, ItemLayerAnalyzer> {
		final StructureBuilder builder;
		public StructureRef(StructureLayer layer, StructureBuilder builder, IntFunction<ItemLayerAnalyzer> gen_analyzer) {
			super(layer, gen_analyzer);
			this.builder = requireNonNull(builder);
		}
	}

	static class AnnotationRef extends LayerRef<AnnotationLayer, AnnotationAnalyzer<String>> {
		final String key;
		final BiConsumer<Item, String> setter;
		final TriConsumer<Item, String, String> unknownSetter;
		public AnnotationRef(AnnotationLayer layer, String key, IntFunction<AnnotationAnalyzer<String>> gen_analyzer) {
			super(layer, gen_analyzer);
			this.key = checkNotEmpty(key);
			AnnotationStorage storage = layer.getAnnotationStorage();
			setter = (item, value) -> storage.setString(item, key, value);
			unknownSetter = storage::setString;
		}
	}
}
