/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.members.item.manager.ItemLayerManager;
import de.ims.icarus2.query.api.engine.CorpusData.CorpusBacked;
import de.ims.icarus2.query.api.engine.DummyCorpus.DummyType;
import de.ims.icarus2.query.api.exp.LaneInfo;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.query.api.iql.IqlElement.IqlNode;
import de.ims.icarus2.query.api.iql.IqlLane;

/**
 * @author Markus Gärtner
 *
 */
class CorpusDataTest {

	@TempDir
	private static Path tmpFolder;

	@ParameterizedTest
	@EnumSource(DummyType.class)
	public void testDummyCreation(DummyType type) throws Exception {
		Corpus corpus = DummyCorpus.createDummyCorpus(tmpFolder, type, 1, 2, 3);
		ItemLayerManager mgr = corpus.getDriver("context0");
		assertThat(mgr.getItemCount(corpus.getPrimaryLayer())).isEqualTo(type==DummyType.FLAT ? 6 : 3);
		assertThat(mgr.getItemCount(corpus.getFoundationLayer())).isEqualTo(6);
	}

	@Nested
	class ForCorpusBacked {


		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#close()}.
		 */
		@Test
		void testClose() throws Exception {
			CorpusData data = create(1, 2, 3);
			data.close();
			//TODO do we need to verify that methods can fail now?
		}

		private CorpusData.CorpusBacked create(int...setup) throws SAXException, IOException, InterruptedException {
			Corpus corpus = DummyCorpus.createDummyCorpus(tmpFolder, DummyType.HIERARCHICAL, setup);
			return CorpusBacked.builder()
					.scope(corpus.createCompleteScope())
					.build();
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
		 */
		@Test
		void testResolveLane() throws Exception {
			CorpusData data = create(1, 2, 3);
			IqlLane lane = new IqlLane();
			lane.setName(DummyCorpus.LAYER_SENTENCE);

			LaneInfo info = data.resolveLane(lane);
			assertThat(info.getLane()).isSameAs(lane);
			assertThat(info.getType()).isSameAs(TypeInfo.ITEM_LAYER);
			assertThat(info.getLayer().getId()).isEqualTo(DummyCorpus.LAYER_SENTENCE);
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveElement(de.ims.icarus2.query.api.exp.LaneInfo, de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement, de.ims.icarus2.query.api.exp.ElementInfo)}.
		 */
		@Test
		void testResolveElement() throws Exception {
			CorpusData data = create(1, 2, 3);
			IqlLane lane = new IqlLane();
			lane.setName(DummyCorpus.LAYER_SENTENCE);

			LaneInfo laneInfo = data.resolveLane(lane);
			IqlNode element = new IqlNode();

			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#bind(de.ims.icarus2.query.api.iql.IqlBinding)}.
		 */
		@Test
		void testBind() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
		 */
		@Test
		void testFindAnnotation() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findLayer(java.lang.String)}.
		 */
		@Test
		void testFindLayer() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#access(de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
		 */
		@Test
		void testAccess() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#map(de.ims.icarus2.query.api.engine.CorpusData.LayerRef, de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
		 */
		@Test
		void testMap() {
			fail("Not yet implemented"); // TODO
		}
	}

	@Nested
	class ForVirtual {

	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveLane(de.ims.icarus2.query.api.iql.IqlLane)}.
	 */
	@Test
	void testResolveLane() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#resolveElement(de.ims.icarus2.query.api.exp.LaneInfo, de.ims.icarus2.query.api.iql.IqlElement.IqlProperElement, de.ims.icarus2.query.api.exp.ElementInfo)}.
	 */
	@Test
	void testResolveElement() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#bind(de.ims.icarus2.query.api.iql.IqlBinding)}.
	 */
	@Test
	void testBind() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findAnnotation(de.ims.icarus2.query.api.exp.ElementInfo, de.ims.icarus2.query.api.exp.QualifiedIdentifier)}.
	 */
	@Test
	void testFindAnnotation() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#findLayer(java.lang.String)}.
	 */
	@Test
	void testFindLayer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#access(de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
	 */
	@Test
	void testAccess() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#map(de.ims.icarus2.query.api.engine.CorpusData.LayerRef, de.ims.icarus2.query.api.engine.CorpusData.LayerRef)}.
	 */
	@Test
	void testMap() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.query.api.engine.CorpusData#close()}.
	 */
	@Test
	void testClose() {
		fail("Not yet implemented"); // TODO
	}

}
