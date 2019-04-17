/**
 *
 */
package de.ims.icarus2.model.api.corpus;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.util.PartTest;

/**
 * @author Markus Gärtner
 *
 */
public interface OwnableCorpusPartTest<P extends OwnableCorpusPart>
		extends PartTest<Corpus, P> {

	/**
	 * Creates an owner that does not by default release a view on request.
	 * @return
	 */
	default CorpusOwner createBlockingOwner() {
		CorpusOwner owner = mock(CorpusOwner.class);
		when(owner.getName()).thenReturn(Optional.of("test-owner"));
		return owner;
	}

	/**
	 * Creates an owner that by default releases a view on request.
	 * @return
	 */
	@SuppressWarnings("boxing")
	default CorpusOwner createNonBlockingOwner() {
		CorpusOwner owner = mock(CorpusOwner.class);
		when(owner.getName()).thenReturn(Optional.of("test-owner"));
		try {
			when(owner.release()).thenReturn(Boolean.TRUE);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		return owner;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#acquire(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testAcquire() {
		try(P part = createPart()) {
			part.acquire(createNonBlockingOwner());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#acquire(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testAcquireAfterClose() {
		try(P part = createPart()) {
			part.close();

			assertModelException(ModelErrorCode.VIEW_CLOSED, () -> part.acquire(createNonBlockingOwner()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#acquire(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testAcquireWithNull() {
		try(P part = createPart()) {
			assertNPE(() -> part.acquire(null));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#acquire(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testAcquireRepeatedly() {
		try(P part = createPart()) {
			CorpusOwner owner = createNonBlockingOwner();
			part.acquire(owner);
			// Subsequent requests with same owner are supposed to cause no issues
			part.acquire(owner);
			part.acquire(owner);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#release(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testRelease() {
		try(P part = createPart()) {
			CorpusOwner owner = createNonBlockingOwner();
			part.acquire(owner);
			part.release(owner);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#release(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 * @throws InterruptedException
	 */
	@Test
	default void testReleaseAfterClose() throws InterruptedException {
		try(P part = createPart()) {
			CorpusOwner owner = createNonBlockingOwner();

			part.acquire(owner);
			part.close();

			assertModelException(ModelErrorCode.VIEW_CLOSED, () -> part.release(createNonBlockingOwner()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#release(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testReleaseWithNull() {
		try(P part = createPart()) {
			part.acquire(createNonBlockingOwner());
			assertNPE(() -> part.release(null));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#release(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testReleaseRepeatedly() {
		try(P part = createPart()) {
			CorpusOwner owner = createNonBlockingOwner();
			part.acquire(owner);
			part.release(owner);
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> part.release(owner));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#release(de.ims.icarus2.model.api.corpus.CorpusOwner)}.
	 */
	@Test
	default void testReleaseForeignOwner() {
		try(P part = createPart()) {
			CorpusOwner owner1 = createNonBlockingOwner();
			CorpusOwner owner2 = createNonBlockingOwner();
			assumeTrue(owner1!=owner2);
			part.acquire(owner1);
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> part.release(owner2));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#getOwners()}.
	 */
	@Test
	default void testGetOwnersEmpty() {
		try(P part = createPart()) {
			assertCollectionEmpty(part.getOwners());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#getOwners()}.
	 */
	@Test
	default void testGetOwnersAfterAcquire() {
		try(P part = createPart()) {
			CorpusOwner owner1 = createNonBlockingOwner();
			CorpusOwner owner2 = createNonBlockingOwner();
			assumeTrue(owner1!=owner2);

			part.acquire(owner1);
			assertCollectionEquals(part.getOwners(), owner1);

			part.acquire(owner2);
			assertCollectionEquals(part.getOwners(), owner1, owner2);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#getOwners()}.
	 */
	@Test
	default void testGetOwnersAfterRelease() {
		try(P part = createPart()) {
			CorpusOwner owner1 = createNonBlockingOwner();
			CorpusOwner owner2 = createNonBlockingOwner();
			assumeTrue(owner1!=owner2);

			part.acquire(owner1);
			part.acquire(owner2);

			part.release(owner1);
			assertCollectionEquals(part.getOwners(), owner2);

			part.release(owner2);
			assertCollectionEmpty(part.getOwners());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#closable()}.
	 */
	@Test
	default void testClosableEmpty() {
		try(P part = createPart()) {
			assertTrue(part.closable());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#closable()}.
	 */
	@Test
	default void testClosableWithOwners() {
		try(P part = createPart()) {
			part.acquire(createNonBlockingOwner());
			assertFalse(part.closable());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#closable()}.
	 */
	@Test
	default void testClosableWithBlockingOwners() {
		try(P part = createPart()) {
			CorpusOwner owner = createBlockingOwner();
			part.acquire(owner);
			assertFalse(part.closable());
			part.release(owner);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#close()}.
	 */
	@Test
	default void testCloseWithoutOwners() {
		try(P part = createPart()) {
			part.close();
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#close()}.
	 * @throws InterruptedException
	 */
	@Test
	default void testCloseWithOwners() throws InterruptedException {
		try(P part = createPart()) {
			part.acquire(createNonBlockingOwner());
			part.close();
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#close()}.
	 * @throws InterruptedException
	 */
	@Test
	default void testCloseWithBlockingOwners() throws InterruptedException {
		try(P part = createPart()) {
			CorpusOwner owner = createBlockingOwner();
			part.acquire(owner);
			assertModelException(ModelErrorCode.VIEW_UNCLOSABLE,
					() -> part.close());
			part.release(owner);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.corpus.OwnableCorpusPart#isActive()}.
	 */
	@Test
	default void testIsActive() {
		try(P part = createPart()) {
			assertTrue(part.isActive());

			part.close();

			assertFalse(part.isActive());
		}
	}

}
