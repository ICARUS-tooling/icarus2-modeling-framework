/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.corpus;

import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl.Stage;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.standard.corpus.DefaultGenerationControl.Builder;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.BuilderTest;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultGenerationControlTest {

	private DefaultGenerationControl instance;

	@SuppressWarnings("boxing")
	@BeforeEach
	public void prepare() {
		CorpusManifest corpusManifest = mock(CorpusManifest.class);
		when(corpusManifest.isEditable()).thenReturn(true);

		Lock lock = new ReentrantLock();

		Corpus corpus = mock(Corpus.class);
		when(corpus.getManifest()).thenReturn(corpusManifest);
		when(corpus.getLock()).thenReturn(lock);

		TimeBasedGenerator generator = Generators.timeBasedGenerator();

		Stack<String> storage = new ObjectArrayList<>();

		instance = DefaultGenerationControl.builder()
				.corpus(corpus)
				.uuidGenerator(generator::generate)
				.storage(storage)
				.build();
	}

	@Test
	void testAdvance() throws Exception {
		Stage oldStage = instance.getStage();
		Stage newStage = instance.advance();

		assertNotNull(newStage);
		assertNotSame(oldStage, newStage);
		assertTrue(newStage.compareTo(oldStage)>0);
	}

	@Test
	void testBlankStep() throws Exception {
		Stage dummyStage1 = mock(Stage.class);
		Stage dummyStage2 = mock(Stage.class);

		// First with totally unknown stages
		assertFalse(instance.step(dummyStage1, dummyStage2));

		// Now with valid sentinel, but unknown target
		assertFalse(instance.step(instance.getStage(), dummyStage1));
	}

	@Test
	void testRevertSingle() throws Exception {

		Stage oldStage = instance.getStage();
		Stage newStage = instance.advance();

		assertTrue(instance.step(newStage, oldStage));

		Stage revertedStage = instance.getStage();

		assertEquals(oldStage, revertedStage);
	}

	@Test
	void testRevertMultiple() throws Exception {
		Stack<Stage> stack = new ObjectArrayList<>();

		stack.push(instance.getStage());

		for(int i=0; i<100; i++) {
			stack.push(instance.advance());
		}


		for(int i=0; i<100; i++) {
			assertTrue(instance.step(stack.pop(), stack.top()));
		}
	}

	@Nested
	class ForBuilder implements BuilderTest<DefaultGenerationControl, DefaultGenerationControl.Builder> {

		@Override
		public Class<?> getTestTargetClass() {
			return Builder.class;
		}

		@Override
		public Builder createTestInstance(TestSettings settings) {
			return settings.process(DefaultGenerationControl.builder());
		}

		/**
		 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
		 */
		@Override
		public void configureApiGuard(ApiGuard<Builder> apiGuard) {
			BuilderTest.super.configureApiGuard(apiGuard);

			apiGuard.parameterResolver(Corpus.class, b -> makeCorpus(true));
		}

		@SuppressWarnings("boxing")
		private Corpus makeCorpus(boolean editable) {
			CorpusManifest manifest = mock(CorpusManifest.class);
			when(manifest.isEditable()).thenReturn(_boolean(editable));
			Corpus corpus = mock(Corpus.class);
			when(corpus.getManifest()).thenReturn(manifest);
			return corpus;
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder>>> invalidOps() {
			return list(
					Triple.triple("corpus without manifest", NullPointerException.class, b -> b.corpus(mock(Corpus.class))),
					Triple.triple("uneditable corpus", IllegalArgumentException.class, b -> b.corpus(makeCorpus(false)))
			);
		}
	}
}
