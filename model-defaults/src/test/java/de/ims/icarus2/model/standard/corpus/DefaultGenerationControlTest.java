/**
 *
 */
package de.ims.icarus2.model.standard.corpus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl.Stage;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus
 *
 */
public class DefaultGenerationControlTest {

	private DefaultGenerationControl instance;

	@SuppressWarnings("boxing")
	@Before
	public void prepare() {
		CorpusManifest corpusManifest = mock(CorpusManifest.class);
		when(corpusManifest.isEditable()).thenReturn(true);

		Lock lock = new ReentrantLock();

		Corpus corpus = mock(Corpus.class);
		when(corpus.getManifest()).thenReturn(corpusManifest);
		when(corpus.getLock()).thenReturn(lock);

		TimeBasedGenerator generator = Generators.timeBasedGenerator();

		Stack<String> storage = new ObjectArrayList<>();

		instance = DefaultGenerationControl.newBuilder()
				.corpus(corpus)
				.uuidGenerator(generator::generate)
				.storage(storage)
				.build();
	}

	@Test
	public void testAdvance() throws Exception {
		Stage oldStage = instance.getStage();
		Stage newStage = instance.advance();

		assertNotNull(newStage);
		assertNotSame(oldStage, newStage);
		assertTrue(newStage.compareTo(oldStage)>0);
	}

	@Test
	public void testBlankStep() throws Exception {
		Stage dummyStage1 = mock(Stage.class);
		Stage dummyStage2 = mock(Stage.class);

		// First with totally unknown stages
		assertFalse(instance.step(dummyStage1, dummyStage2));

		// Now with valid sentinel, but unknown target
		assertFalse(instance.step(instance.getStage(), dummyStage1));
	}

	@Test
	public void testRevertSingle() throws Exception {

		Stage oldStage = instance.getStage();
		Stage newStage = instance.advance();

		assertTrue(instance.step(newStage, oldStage));

		Stage revertedStage = instance.getStage();

		assertEquals(oldStage, revertedStage);
	}

	@Test
	public void testRevertMultiple() throws Exception {
		Stack<Stage> stack = new ObjectArrayList<>();

		stack.push(instance.getStage());

		for(int i=0; i<100; i++) {
			stack.push(instance.advance());
		}


		for(int i=0; i<100; i++) {
			assertTrue(instance.step(stack.pop(), stack.top()));
		}
	}
}
