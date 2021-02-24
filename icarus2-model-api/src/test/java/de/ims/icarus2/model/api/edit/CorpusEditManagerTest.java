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
package de.ims.icarus2.model.api.edit;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl;
import de.ims.icarus2.model.api.corpus.GenerationControl.Stage;
import de.ims.icarus2.model.api.edit.change.AtomicChange;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.SimpleEventListener;

/**
 * @author Markus Gärtner
 *
 */
class CorpusEditManagerTest implements ApiGuardedTest<CorpusEditManager> {


	@Override
	public Class<?> getTestTargetClass() {
		return CorpusEditManager.class;
	}

	@Override
	public CorpusEditManager createTestInstance(TestSettings settings) {
		return settings.process(new CorpusEditManager(mock(Corpus.class)));
	}

	@Nested
	class WithCorpus {

		Corpus corpus;
		CorpusManifest manifest;
		CorpusEditManager manager;
		GenerationControl generations;

		@BeforeEach
		void setUp() {
			generations = mock(GenerationControl.class);
			manifest = mock(CorpusManifest.class);
			Lock lock = mock(Lock.class);

			corpus = mock(Corpus.class);
			when(corpus.getManifest()).thenReturn(manifest);
			when(corpus.getGenerationControl()).thenReturn(generations);
			when(corpus.getLock()).thenReturn(lock);

			manager = new CorpusEditManager(corpus);
		}

		@AfterEach
		void tearDown() throws Exception {
			generations.close();
			generations = null;
			manifest = null;
			corpus.close();
			corpus = null;
			manager = null;
		}

		@Test
		void addListener() {
			CorpusUndoListener listener = mock(CorpusUndoListener.class);
			manager.addCorpusUndoListener(listener);
		}

		@Test
		void removeListener() {
			CorpusUndoListener listener = mock(CorpusUndoListener.class);
			manager.addCorpusUndoListener(listener);
			manager.removeCorpusUndoListener(listener);
		}

		@Test
		void beginUpdate() {
			manager.beginUpdate();
		}

		@Test
		void beginUpdate_Events() {
			SimpleEventListener eventListener = mock(SimpleEventListener.class);
			manager.addListener(eventListener);

			manager.beginUpdate();

			verify(eventListener).invoke(eq(manager), argThat((EventObject event) -> {
				return event.getName().equals(CorpusEditEvents.BEGIN_UPDATE)
						&& event.getProperty("edit") != null
						&& event.getProperty("level").equals(Integer.valueOf(1));
			}));
		}

		@Test
		@RandomizedTest
		void beginNamedUpdate(RandomGenerator rand) {
			SimpleEventListener eventListener = mock(SimpleEventListener.class);
			manager.addListener(eventListener);
			String name = rand.randomString(20);

			manager.beginUpdate(name);

			verify(eventListener).invoke(eq(manager), argThat((EventObject event) -> {
				return event.getName().equals(CorpusEditEvents.BEGIN_UPDATE)
						&& ((UndoableCorpusEdit)event.getProperty("edit"))
								.getPresentationName().equals(name)
						&& event.getProperty("level").equals(Integer.valueOf(1));
			}));
		}

		@Test
		@RandomizedTest
		void beginRedundantNamedUpdate(RandomGenerator rand) {
			manager.beginUpdate();
			assertModelException(GlobalErrorCode.ILLEGAL_STATE,
					() -> manager.beginUpdate(rand.randomString(20)));
		}

		@Test
		void endUpdate() {
			SimpleEventListener eventListener = mock(SimpleEventListener.class);
			manager.addListener(eventListener);

			manager.beginUpdate();
			manager.endUpdate();

			verify(eventListener).invoke(eq(manager), argThat((EventObject event) -> {
				return event.getName().equals(CorpusEditEvents.END_UPDATE)
						&& event.getProperty("edit") != null
						&& event.getProperty("level").equals(Integer.valueOf(0));
			}));
		}

		@Test
		void prematureEndUpdate() {
			assertModelException(GlobalErrorCode.ILLEGAL_STATE,
					() -> manager.endUpdate());
		}

		@Test
		void executeUneditable() {
			assertModelException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					() -> manager.execute(mock(AtomicChange.class)));
		}

		@Test
		void execute() {
			when(_boolean(manifest.isEditable())).thenReturn(Boolean.TRUE);

			Stage oldStage = mock(Stage.class);
			Stage newStage = mock(Stage.class);
			when(generations.getStage()).thenReturn(oldStage, newStage);

			SimpleEventListener eventListener = mock(SimpleEventListener.class);
			CorpusUndoListener undoListener = mock(CorpusUndoListener.class);

			manager.addListener(eventListener);
			manager.addCorpusUndoListener(undoListener);

			AtomicChange change = mock(AtomicChange.class);
			manager.execute(change);

			// Verify event cycle
			verify(eventListener).invoke(eq(manager), argThat((EventObject event) -> {
				return event.getName().equals(CorpusEditEvents.EXECUTE)
						&& event.getProperty("change")==change;
			}));

			verify(eventListener).invoke(eq(manager), argThat((EventObject event) -> {
				return event.getName().equals(CorpusEditEvents.BEFORE_UNDO)
						&& event.getProperty("edit") != null;
			}));

			// Verify proper construction of the undoable edit
			verify(undoListener).undoableEditHappened(argThat((UndoableCorpusEdit edit) -> {
				return edit.getCorpus()==corpus
						&& edit.getOldGenerationStage()==oldStage
						&& edit.getNewGenerationStage()==newStage
						&& Arrays.equals(edit.getChanges().toArray(), new Object[] {change});
			}));
		}
	}

}
