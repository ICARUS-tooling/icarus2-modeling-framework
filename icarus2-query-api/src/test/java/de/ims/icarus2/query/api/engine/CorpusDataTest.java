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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItems;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.SAXException;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.CorpusData.CorpusBacked;
import de.ims.icarus2.query.api.engine.CorpusData.LayerRef;
import de.ims.icarus2.query.api.engine.CorpusData.Virtual;
import de.ims.icarus2.query.api.engine.DummyCorpus.DummyType;
import de.ims.icarus2.query.api.exp.AnnotationInfo;
import de.ims.icarus2.query.api.exp.BindingInfo;
import de.ims.icarus2.query.api.exp.ElementInfo;
import de.ims.icarus2.query.api.exp.LaneInfo;
import de.ims.icarus2.query.api.exp.QualifiedIdentifier;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.query.api.iql.IqlBinding;
import de.ims.icarus2.query.api.iql.IqlElement.IqlEdge;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlLane;
import de.ims.icarus2.query.api.iql.IqlReference;
import de.ims.icarus2.query.api.iql.IqlReference.ReferenceType;
import de.ims.icarus2.test.annotations.IntArrayArg;

/**
 * @author Markus Gärtner
 *
 */
class CorpusDataTest {

	@ParameterizedTest
	@CsvSource({
		"FLAT, 3, 3, {3}",
		"HIERARCHICAL, 3, 6, {1;2;3}",
		"FULL, 4, 10, {1;2;3;4}",
	})
	public void testDummyCreation(DummyType type, int primarySize, int foundationSize, @IntArrayArg int[] setup) throws Exception {
		Corpus corpus = DummyCorpus.createDummyCorpus(type, setup);
		ItemLayerManager mgr = corpus.getDriver(DummyCorpus.CONTEXT_ID);
		assertThat(mgr.getItemCount(corpus.getPrimaryLayer())).isEqualTo(primarySize);
		assertThat(mgr.getItemCount(corpus.getFoundationLayer())).isEqualTo(foundationSize);
	}

	@Nested
	class ForCorpusBacked {


		private CorpusData.CorpusBacked create(int...setup) throws SAXException, IOException, InterruptedException {
			Corpus corpus = DummyCorpus.createDummyCorpus(DummyType.FULL, setup);
			return CorpusBacked.builder()
					.scope(corpus.createCompleteScope())
					.build();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#close()}.
		 */
		@Test
		void testClose() throws Exception {
			create(1, 2, 3).close();
			//TODO do we need to verify that methods can fail now?
		}

		@Nested
		class ForResolveLane {

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
			 */
			@Test
			void testSentenceLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				LaneInfo info = data.resolveLane(lane);
				assertThat(info.getLane()).isSameAs(lane);
				assertThat(info.getType()).isSameAs(TypeInfo.ITEM_LAYER);
				assertThat(info.getLayer().getId()).isEqualTo(DummyCorpus.LAYER_SENTENCE);
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
			 */
			@Test
			void testProxyLane() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(IqlLane.PROXY_NAME);

				LaneInfo info = data.resolveLane(lane);
				assertThat(info.getLane()).isSameAs(lane);
				assertThat(info.getType()).isSameAs(TypeInfo.ITEM_LAYER);
				// The layer ref created for proxy lanes uses the UID of the layer as id
				assertThat(info.getLayer().getId()).endsWith(DummyCorpus.LAYER_SENTENCE);
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
			 */
			@Test
			void testTokenLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_TOKEN);

				assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.resolveLane(lane))
					.withMessageContaining("must not be a foundation layer")
					.extracting(IcarusRuntimeException::getErrorCode)
					.isSameAs(QueryErrorCode.INCOMPATIBLE_REFERENCE);
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
			 */
			@Test
			void testNonItemLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_ANNO);

				assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.resolveLane(lane))
					.withMessageContaining("item or structure layer")
					.extracting(IcarusRuntimeException::getErrorCode)
					.isSameAs(QueryErrorCode.INCOMPATIBLE_REFERENCE);
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
			 */
			@Test
			void testUnknownLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.UNKNOWN_LAYER);

				assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.resolveLane(lane))
					.withMessageContaining("valid layer")
					.extracting(IcarusRuntimeException::getErrorCode)
					.isSameAs(QueryErrorCode.UNKNOWN_IDENTIFIER);
			}

		}

		@Nested
		class ForResolveElement {

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveElement(de.ims.icarus2.query.api.exp.LaneInfo, de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement, de.ims.icarus2.query.api.exp.ElementInfo)}.
			 */
			@Test
			void testUnboundTokenLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				LaneInfo laneInfo = data.resolveLane(lane);
				// We use an unbound node here, so the token layer should be picked by default
				IqlNode element = new IqlNode();

				ElementInfo elementInfo = data.resolveElement(laneInfo, element, null);
				assertThat(elementInfo.getElement()).isSameAs(element);
				assertThat(elementInfo.getType()).isSameAs(TypeInfo.ITEM);
				assertThat(elementInfo.getLayers()).hasSize(1)
					.allSatisfy(ref -> assertThat(ref.getId()).endsWith(DummyCorpus.LAYER_TOKEN));
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveElement(de.ims.icarus2.query.api.exp.LaneInfo, de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement, de.ims.icarus2.query.api.exp.ElementInfo)}.
			 */
			@Test
			void testBoundTokenLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				IqlBinding binding = new IqlBinding();
				binding.setTarget(DummyCorpus.LAYER_TOKEN);
				binding.addMember(new IqlReference("tok", ReferenceType.MEMBER));
				data.bind(binding);

				LaneInfo laneInfo = data.resolveLane(lane);
				IqlNode element = new IqlNode();
				element.setLabel("tok");

				ElementInfo elementInfo = data.resolveElement(laneInfo, element, null);
				assertThat(elementInfo.getElement()).isSameAs(element);
				assertThat(elementInfo.getType()).isSameAs(TypeInfo.ITEM);
				assertThat(elementInfo.getLayers()).hasSize(1)
					.allSatisfy(ref -> assertThat(ref.getId()).isEqualTo("tok"));
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveElement(de.ims.icarus2.query.api.exp.LaneInfo, de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement, de.ims.icarus2.query.api.exp.ElementInfo)}.
			 */
			@Test
			void testEdge() throws Exception {
				CorpusData data = create(1, 2, 3);
				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SYNTAX);

				LaneInfo laneInfo = data.resolveLane(lane);
				IqlEdge edge = new IqlEdge();

				ElementInfo elementInfo = data.resolveElement(laneInfo, edge, null);
				assertThat(elementInfo.getElement()).isSameAs(edge);
				assertThat(elementInfo.getType()).isSameAs(TypeInfo.EDGE);
				assertThat(elementInfo.getLayers()).hasSize(1)
					.allSatisfy(ref -> assertThat(ref.getId()).endsWith(DummyCorpus.LAYER_SYNTAX));
			}

		}

		@Nested
		class ForBind {

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#bind(de.ims.icarus2.query.api.iql.IqlBinding)}.
			 */
			@Test
			void testSingleToken() throws Exception {
				CorpusData data = create(1, 2, 3);
				String key = "tok";

				IqlBinding binding = new IqlBinding();
				binding.setTarget(DummyCorpus.LAYER_TOKEN);
				binding.addMember(new IqlReference(key, ReferenceType.MEMBER));

				Map<String,BindingInfo> info = data.bind(binding);

				assertThat(info).hasSize(1)
					.extractingByKey(key)
					.satisfies(bind -> {
						assertThat(bind.isEdges()).isFalse();
						assertThat(bind.getType()).isSameAs(TypeInfo.ITEM);
						assertThat(bind.getLayer().getId()).isEqualTo(DummyCorpus.LAYER_TOKEN);
					});
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#bind(de.ims.icarus2.query.api.iql.IqlBinding)}.
			 */
			@Test
			void testMultipleToken() throws Exception {
				CorpusData data = create(1, 2, 3);
				String key1 = "tok1";
				String key2 = "tok2";
				String key3 = "tok3";

				IqlBinding binding = new IqlBinding();
				binding.setTarget(DummyCorpus.LAYER_TOKEN);
				binding.addMember(new IqlReference(key1, ReferenceType.MEMBER));
				binding.addMember(new IqlReference(key2, ReferenceType.MEMBER));
				binding.addMember(new IqlReference(key3, ReferenceType.MEMBER));

				Map<String,BindingInfo> info = data.bind(binding);
				assertThat(info).hasSize(3)
					.containsOnlyKeys(key1, key2, key3)
					.allSatisfy((key, bind)-> {
						assertThat(bind.isEdges()).isFalse();
						assertThat(bind.getType()).isSameAs(TypeInfo.ITEM);
						assertThat(bind.getLayer().getId()).isEqualTo(DummyCorpus.LAYER_TOKEN);
					});
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#bind(de.ims.icarus2.query.api.iql.IqlBinding)}.
			 */
			@Test
			void testUnknownLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				String key = "tok";

				IqlBinding binding = new IqlBinding();
				binding.setTarget(DummyCorpus.UNKNOWN_LAYER);
				binding.addMember(new IqlReference(key, ReferenceType.MEMBER));

				assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.bind(binding))
					.withMessageContaining("resolve name")
					.extracting(IcarusRuntimeException::getErrorCode)
					.isSameAs(QueryErrorCode.UNKNOWN_IDENTIFIER);
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#bind(de.ims.icarus2.query.api.iql.IqlBinding)}.
			 */
			@Test
			void testNonItemLayer() throws Exception {
				CorpusData data = create(1, 2, 3);
				String key = "tok";

				IqlBinding binding = new IqlBinding();
				binding.setTarget(DummyCorpus.LAYER_ANNO);
				binding.addMember(new IqlReference(key, ReferenceType.MEMBER));

				assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.bind(binding))
					.withMessageContaining("must be an item layer")
					.extracting(IcarusRuntimeException::getErrorCode)
					.isSameAs(QueryErrorCode.INCORRECT_USE);
			}

		}

		@Nested
		class ForFindAnnotation {

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
			 */
			@Test
			void testKeyOnly() throws Exception {
				CorpusData data = create(3);

				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				LaneInfo laneInfo = data.resolveLane(lane);
				// We use an unbound node here, so the token layer should be picked by default
				IqlNode element = new IqlNode();

				ElementInfo elementInfo = data.resolveElement(laneInfo, element, null);

				String key = "counter";
				QualifiedIdentifier identifier = QualifiedIdentifier.of(key);
				Optional<AnnotationInfo> info = data.findAnnotation(elementInfo, identifier);

				assertThat(info).isNotEmpty()
					.hasValueSatisfying(anno -> {
						assertThat(anno.getKey()).isEqualTo(key);
						assertThat(anno.getRawKey()).isEqualTo(key);
						assertThat(anno.getValueType()).isEqualTo(ValueType.STRING);
						assertThat(anno.getType()).isEqualTo(TypeInfo.TEXT);
						assertThat(anno.getObjectSource()).isNotNull();
					});
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
			 */
			@Test
			void testExplicitLayer() throws Exception {
				CorpusData data = create(3);

				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				LaneInfo laneInfo = data.resolveLane(lane);
				// We use an unbound node here, so the token layer should be picked by default
				IqlNode element = new IqlNode();

				ElementInfo elementInfo = data.resolveElement(laneInfo, element, null);

				String key = "anno1";
				String host = DummyCorpus.LAYER_ANNO_2;
				String rawText = host+"::"+key;
				QualifiedIdentifier identifier = QualifiedIdentifier.of(rawText, host, key);
				Optional<AnnotationInfo> info = data.findAnnotation(elementInfo, identifier);

				assertThat(info).isNotEmpty()
					.hasValueSatisfying(anno -> {
						assertThat(anno.getKey()).isEqualTo(key);
						assertThat(anno.getRawKey()).isEqualTo(rawText);
						assertThat(anno.getValueType()).isEqualTo(ValueType.STRING);
						assertThat(anno.getType()).isEqualTo(TypeInfo.TEXT);
						assertThat(anno.getObjectSource()).isNotNull();
					});
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
			 */
			@Test
			void testAmbigiousKey() throws Exception {
				CorpusData data = create(3);

				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				LaneInfo laneInfo = data.resolveLane(lane);
				// We use an unbound node here, so the token layer should be picked by default
				IqlNode element = new IqlNode();

				ElementInfo elementInfo = data.resolveElement(laneInfo, element, null);

				QualifiedIdentifier identifier = QualifiedIdentifier.of("anno1");
				assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.findAnnotation(elementInfo, identifier))
					.withMessageContaining("ambiguous and links to multiple annotations")
					.extracting(IcarusRuntimeException::getErrorCode)
					.isSameAs(ManifestErrorCode.MANIFEST_DUPLICATE_ID);
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
			 */
			@Test
			void testUnknownKey() throws Exception {
				CorpusData data = create(3);

				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				LaneInfo laneInfo = data.resolveLane(lane);
				// We use an unbound node here, so the token layer should be picked by default
				IqlNode element = new IqlNode();

				ElementInfo elementInfo = data.resolveElement(laneInfo, element, null);

				QualifiedIdentifier identifier = QualifiedIdentifier.of(DummyCorpus.UNKNOWN_KEY);
				assertThat(data.findAnnotation(elementInfo, identifier)).isEmpty();
			}

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
			 */
			@Test
			void testUnknownExplicitKey() throws Exception {
				CorpusData data = create(3);

				IqlLane lane = new IqlLane();
				lane.setName(DummyCorpus.LAYER_SENTENCE);

				LaneInfo laneInfo = data.resolveLane(lane);
				// We use an unbound node here, so the token layer should be picked by default
				IqlNode element = new IqlNode();

				ElementInfo elementInfo = data.resolveElement(laneInfo, element, null);

				QualifiedIdentifier identifier = QualifiedIdentifier.of(
						DummyCorpus.LAYER_ANNO+"::"+DummyCorpus.UNKNOWN_KEY, DummyCorpus.LAYER_ANNO, DummyCorpus.UNKNOWN_KEY);
				assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.findAnnotation(elementInfo, identifier))
					.withMessageContaining("not be resolved to an annotation")
					.extracting(IcarusRuntimeException::getErrorCode)
					.isSameAs(QueryErrorCode.UNKNOWN_IDENTIFIER);
			}

		}


		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findLayer(java.lang.String)}.
		 */
		@ParameterizedTest
		@CsvSource({
			"FLAT, {3}",
			"HIERARCHICAL, {1;2;3}",
			"FULL, {1;2;3;4}",
		})
		void testFindLayer(DummyType type, @IntArrayArg int[] setup) throws Exception {
			Corpus corpus = DummyCorpus.createDummyCorpus(DummyType.FULL, setup);
			CorpusBacked data = CorpusBacked.builder()
					.scope(corpus.createCompleteScope())
					.build();
			Map<String, ManifestType> layers = type.getLayers();
			for(String layer : layers.keySet()) {
				Optional<LayerRef> ref = data.findLayer(layer);
				assertThat(ref).isNotEmpty()
					.hasValueSatisfying(lr -> assertThat(lr.getId()).endsWith(layer));
			}
		}

		@Test
		void testFindUnknownLayer() throws Exception {
			CorpusData data = create(3);
			assertThat(data.findLayer(DummyCorpus.UNKNOWN_LAYER)).isEmpty();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#access(de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
		 */
		@ParameterizedTest
		@CsvSource({
			"FLAT, {3}, token, 3, false",
			"HIERARCHICAL, {1;2;3}, token, 6, false",
			"HIERARCHICAL, {1;2;3}, sentence, 3, true",
			"FULL, {1;2;3;4}, token, 10, false",
			"FULL, {1;2;3;4}, sentence, 4, true",
		})
		void testAccess(DummyType type, @IntArrayArg int[] setup, String layer, int size, boolean container) throws Exception {
			Corpus corpus = DummyCorpus.createDummyCorpus(DummyType.FULL, setup);
			CorpusBacked data = CorpusBacked.builder()
					.scope(corpus.createCompleteScope())
					.build();

			LayerRef ref = data.findLayer(layer).get();

			LongFunction<Item> itemLookup = data.access(ref);

			for (int i = 0; i < size; i++) {
				Item item = itemLookup.apply(i);
				assertThat(item).isNotNull();
				if(container) {
					assertThat(item).isInstanceOf(Container.class);
				}
			}
		}

		//TODO enable tests once the tabular converter actually ppulates mapping data
		@Disabled
		@Nested
		class ForMap {

			/**
			 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#map(de.ims.icarus2.query.api.engine.CorpusData.LayerRef, de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
			 */
			@Test
			void testTok2Sent() throws Exception {
				Corpus corpus = DummyCorpus.createDummyCorpus(DummyType.HIERARCHICAL, 1, 2, 3);
				CorpusBacked data = CorpusBacked.builder()
						.scope(corpus.createCompleteScope())
						.build();

				LayerRef sourceRef = data.findLayer(DummyCorpus.LAYER_TOKEN).get();
				LayerRef targetRef = data.findLayer(DummyCorpus.LAYER_SENTENCE).get();

				LaneMapper mapper = data.map(sourceRef, targetRef);

				mapper.reset(0);
				assertThat(mapper.size()).isEqualTo(1);
				assertThat(mapper.indexAt(0)).isEqualTo(0);
			}

		}
	}

	//TODO enable tests once virtual CorpusData is done
	@Nested
	class ForVirtual {


		private Virtual.Builder basic() {
			return Virtual.builder()
				.layer(DummyCorpus.LAYER_TOKEN)
					.type(TypeInfo.ITEM)
					.annotations(DummyCorpus.LAYER_ANNO).commit()
				.layer(DummyCorpus.LAYER_SENTENCE)
					.type(TypeInfo.CONTAINER)
					.sources(DummyCorpus.LAYER_TOKEN).commit()
				.layer(DummyCorpus.LAYER_ANNO)
					.type(TypeInfo.ANNOTATION_LAYER).commit();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
		 */
		@Test
		void testResolveLane() {
			Virtual data = basic().build();

			IqlLane lane = new IqlLane();
			lane.setName(DummyCorpus.LAYER_SENTENCE);
			LaneInfo laneInfo = data.resolveLane(lane);

			assertThat(laneInfo.getLane()).isSameAs(lane);
			assertThat(laneInfo.getLayer()).extracting(LayerRef::getId).isEqualTo(DummyCorpus.LAYER_SENTENCE);
			assertThat(laneInfo.getType()).isSameAs(TypeInfo.ITEM_LAYER);
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
		 */
		@Test
		void testResolveUnknownLane() {
			Virtual data = basic().build();

			IqlLane lane = new IqlLane();
			lane.setName(DummyCorpus.UNKNOWN_LAYER);

			assertThatExceptionOfType(QueryException.class).isThrownBy(() -> data.resolveLane(lane))
				.withMessageContaining("valid layer")
				.extracting(IcarusRuntimeException::getErrorCode)
				.isSameAs(QueryErrorCode.UNKNOWN_IDENTIFIER);
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveElement(de.ims.icarus2.query.api.exp.LaneInfo, de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement, de.ims.icarus2.query.api.exp.ElementInfo)}.
		 */
		@Test
		void testResolveElement() {
			Virtual data = basic().build();

			IqlLane lane = new IqlLane();
			lane.setName(DummyCorpus.LAYER_SENTENCE);
			LaneInfo laneInfo = data.resolveLane(lane);

			IqlNode node = new IqlNode();
			ElementInfo elementInfo = data.resolveElement(laneInfo, node, null);

			assertThat(elementInfo.getElement()).isSameAs(node);
			assertThat(elementInfo.getLayers()).singleElement()
				.extracting(LayerRef::getId).isEqualTo(DummyCorpus.LAYER_TOKEN);
			assertThat(elementInfo.getType()).isSameAs(TypeInfo.ITEM);
			assertThat(elementInfo.isEdge()).isFalse();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#bind(de.ims.icarus2.query.api.iql.IqlBinding)}.
		 */
		@Test
		void testBind() {
			Virtual data = basic().build();

			String key1 = "tok1";
			String key2 = "tok2";
			String key3 = "tok3";
			Set<String> keys = set(key1, key2, key3);

			IqlBinding binding = new IqlBinding();
			binding.setTarget(DummyCorpus.LAYER_TOKEN);
			binding.addMember(new IqlReference("tok1", ReferenceType.MEMBER));
			binding.addMember(new IqlReference("tok2", ReferenceType.MEMBER));
			binding.addMember(new IqlReference("tok3", ReferenceType.MEMBER));

			Map<String, BindingInfo> bindings = data.bind(binding);

			assertThat(bindings).hasSize(3)
				.allSatisfy((label, info) -> {
					assertThat(keys).contains(label);
					assertThat(info).extracting(BindingInfo::getLayer).extracting(LayerRef::getId)
						.isEqualTo(DummyCorpus.LAYER_TOKEN);
					assertThat(info).extracting(BindingInfo::getType).isSameAs(TypeInfo.ITEM);
				});
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
		 */
		@Test
		void testFindAnnotation() {
			Virtual data = basic()
					.annotation()
						.key(DummyCorpus.ANNO_1)
						.targets(DummyCorpus.LAYER_TOKEN)
						.strings(item -> null)
						.commit()
					.build();

			IqlLane lane = new IqlLane();
			lane.setName(DummyCorpus.LAYER_SENTENCE);
			LaneInfo laneInfo = data.resolveLane(lane);

			IqlNode node = new IqlNode();
			ElementInfo elementInfo = data.resolveElement(laneInfo, node, null);

			Optional<AnnotationInfo> annotationInfo = data.findAnnotation(elementInfo, QualifiedIdentifier.of(DummyCorpus.ANNO_1));
			assertThat(annotationInfo).isNotEmpty().hasValueSatisfying(info -> {
				assertThat(info.getKey()).isEqualTo(DummyCorpus.ANNO_1);
				assertThat(info.getRawKey()).isEqualTo(DummyCorpus.ANNO_1);
				assertThat(info.getType()).isSameAs(TypeInfo.TEXT);
				assertThat(info.getObjectSource()).isNotNull();
			});
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
		 */
		@Test
		void testFindUnknownAnnotation() {
			Virtual data = basic().build();

			IqlLane lane = new IqlLane();
			lane.setName(DummyCorpus.LAYER_SENTENCE);
			LaneInfo laneInfo = data.resolveLane(lane);

			IqlNode node = new IqlNode();
			ElementInfo elementInfo = data.resolveElement(laneInfo, node, null);

			assertThat(data.findAnnotation(elementInfo, QualifiedIdentifier.of(DummyCorpus.UNKNOWN_KEY))).isEmpty();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findLayer(java.lang.String)}.
		 */
		@Test
		void testFindLayer() {
			Virtual data = basic().build();

			assertThat(data.findLayer(DummyCorpus.UNKNOWN_LAYER)).isEmpty();
			assertThat(data.findLayer(DummyCorpus.LAYER_TOKEN)).isNotEmpty()
				.hasValueSatisfying(ref -> assertThat(ref.getId()).isEqualTo(DummyCorpus.LAYER_TOKEN));
			assertThat(data.findLayer(DummyCorpus.LAYER_SENTENCE)).isNotEmpty()
				.hasValueSatisfying(ref -> assertThat(ref.getId()).isEqualTo(DummyCorpus.LAYER_SENTENCE));
			assertThat(data.findLayer(DummyCorpus.LAYER_ANNO)).isNotEmpty()
				.hasValueSatisfying(ref -> assertThat(ref.getId()).isEqualTo(DummyCorpus.LAYER_ANNO));
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#access(de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
		 */
		@Test
		void testAccess() {
			Item[] items = mockItems(5);
			Virtual data = Virtual.builder()
					.layer(DummyCorpus.LAYER_TOKEN).elements(items).type(TypeInfo.ITEM).commit()
					.build();

			LongFunction<Item> lookup = data.access(data.findLayer(DummyCorpus.LAYER_TOKEN).get());
			for (int i = 0; i < items.length; i++) {
				assertThat(lookup.apply(i)).as("item mismatch at index "+i).isSameAs(items[i]);
			}
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#map(de.ims.icarus2.query.api.engine.CorpusData.LayerRef, de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
		 */
		@Test
		void testMap() throws Exception {
			Item[] items1 = mockItems(2);
			Item[] items2 = mockItems(5);

			LaneMapper expected = LaneMapper.fixedBuilder()
					.map(0, 0, 1)
					.map(1, 2, 3, 4)
					.build();

			Virtual data = Virtual.builder()
					.layer(DummyCorpus.LAYER_TOKEN).elements(items1).type(TypeInfo.ITEM).commit()
					.layer(DummyCorpus.LAYER_TOKEN_2).elements(items2).type(TypeInfo.ITEM).commit()
					.mapper(DummyCorpus.LAYER_TOKEN, DummyCorpus.LAYER_TOKEN_2, expected)
					.build();

			LaneMapper mapper = data.map(data.findLayer(DummyCorpus.LAYER_TOKEN).get(),
					data.findLayer(DummyCorpus.LAYER_TOKEN_2).get());

			assertThat(mapper.size()).isEqualTo(0);
			mapper.reset(0);
			assertThat(mapper.size()).isEqualTo(2);
			assertThat(mapper.indexAt(0)).isEqualTo(0);
			assertThat(mapper.indexAt(1)).isEqualTo(1);
			mapper.reset(1);
			assertThat(mapper.size()).isEqualTo(3);
			assertThat(mapper.indexAt(0)).isEqualTo(2);
			assertThat(mapper.indexAt(1)).isEqualTo(3);
			assertThat(mapper.indexAt(2)).isEqualTo(4);
			mapper.reset(2);
			assertThat(mapper.size()).isEqualTo(0);
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#close()}.
		 */
		@Test
		void testClose() {
			basic().build().close();
		}

	}

}
