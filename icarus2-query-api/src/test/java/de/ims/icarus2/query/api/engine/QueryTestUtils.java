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
/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.model.api.ModelTestUtils.mockContext;
import static de.ims.icarus2.model.api.ModelTestUtils.mockCorpus;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockLayer;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.query.api.iql.IqlTestUtils.eq_exp;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._long;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.api.view.ScopeBuilder;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.query.api.exp.AnnotationInfo;
import de.ims.icarus2.query.api.exp.BindingInfo;
import de.ims.icarus2.query.api.exp.ElementInfo;
import de.ims.icarus2.query.api.exp.LaneInfo;
import de.ims.icarus2.query.api.exp.QualifiedIdentifier;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.util.collections.set.ArraySet;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public class QueryTestUtils {

	private static final Pattern NODE = Pattern.compile("\\$([A-Za-z])");

	/** Expand {@code $X} expressions to proper constraints */
	public static String expand(String rawQuery) {
		int lastAppend = 0;
		java.util.regex.Matcher m = NODE.matcher(rawQuery);
		StringBuilder sb = new StringBuilder();
		if(!rawQuery.startsWith("FIND ")) {
			sb.append("FIND ");
		}
		while(m.find()) {
			String content = m.group(1);
			assertThat(content).as("node content not a single character").hasSize(1);
			char c = content.charAt(0);
			// Can't use Matcher.appendReplacement, as we use $ in the expression
			sb.append(rawQuery, lastAppend, m.start())
				.append(eq_exp(c));
			lastAppend = m.end();
		}
		sb.append(rawQuery, lastAppend, rawQuery.length());
		return sb.toString();
	}

	public static final String LANE_NAME = "test_lane";
	public static final String ITEMS_NAME = "test_items";

	public static Scope scope() {
		Corpus corpus = mockCorpus();
		Context context = mockContext(corpus);
		ItemLayer items = layer(context, ITEMS_NAME);
		ItemLayer lane = layer(context, LANE_NAME, items);
		Scope scope = ScopeBuilder.of(corpus)
				.addContext(context)
				.addLayer(items)
				.addLayer(lane)
				.setPrimaryLayer(lane)
				.build();
		return scope;
	}

	public static ItemLayer layer(Context context, String name, ItemLayer...baseLayers) {
		ItemLayer layer = mockLayer(ItemLayer.class, context);
		ItemLayerManifest manifest = mockTypedManifest(ManifestType.ITEM_LAYER_MANIFEST, name);
		when(layer.getManifest()).thenAnswer(invoc -> manifest);
		final DataSet<ItemLayer> bl = new ArraySet<>(baseLayers);
		when(layer.getBaseLayers()).thenReturn(bl);
		return layer;
	}

	public static IqlLane lane() {
		IqlLane lane = new IqlLane();
		lane.setName(LANE_NAME);
		return lane;
	}

	public static Item item(int index, char c) {
		Item item = mockItem();
		when(_long(item.getIndex())).thenReturn(_long(index));
		when(item.toString()).thenReturn(String.valueOf(c));
		return item;
	}

	public static Container sentence(long index, String sentence) {
		Item[] items = IntStream.range(0, sentence.length())
				.mapToObj(j -> item(j, sentence.charAt(j)))
				.toArray(Item[]::new);
		Container c = ModelTestUtils.mockContainer(items);
		ModelTestUtils.stubIndex(c, index);
		return c;
	}

	public static Container[] sentences(String...sentences) {
		return IntStream.range(0, sentences.length)
				.mapToObj(i -> {
					String sentence = sentences[i];
					return sentence(i, sentence);
				})
				.toArray(Container[]::new);
	}

	public static CorpusData dummyCorpus() { return new DummyCorpusData(); }

	private static class DummyCorpusData extends CorpusData {

		@Override
		public LaneInfo resolveLane(IqlLane lane) {
			return new LaneInfo(lane, TypeInfo.ITEM_LAYER, mock(LayerRef.class));
		}

		@Override
		public ElementInfo resolveElement(LaneInfo lane, IqlProperElement element,
				@Nullable ElementInfo parentElement) {
			return new ElementInfo(element, TypeInfo.ITEM, list(mock(LayerRef.class)));
		}

		@Override
		public Map<String, BindingInfo> bind(IqlBinding binding) {
			return Collections.emptyMap();
		}

		@Override
		public Optional<AnnotationInfo> findAnnotation(ElementInfo element, QualifiedIdentifier identifier) {
			return Optional.empty();
		}

		@Override
		public Optional<LayerRef> findLayer(String name) {
			return Optional.empty();
		}

		@Override
		public LongFunction<Container> access(LayerRef layer) {
			throw new UnsupportedOperationException("No data behind dummy corpus");
		}

		@Override
		public LaneMapper map(LayerRef source, LayerRef target) {
			throw new UnsupportedOperationException("No data behind dummy corpus");
		}

		@Override
		public void close() {
			// no-op
		}
	}
}
