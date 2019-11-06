/**
 *
 */
package de.ims.icarus2.model.api.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 * @param <S> the type of the source resource to be synchronized on
 * @param <A> the accessor type under test
 */
public interface SynchronizedAccessorTest<S, A extends SynchronizedAccessor<S>>
		extends ApiGuardedTest<A> {

	@Provider
	S createSource();

	@Provider
	A createAccessor(S source);

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default A createTestInstance(TestSettings settings) {
		return settings.process(createAccessor(createSource()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#close()}.
	 */
	@Test
	default void testClose() {
		create().close();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#getSource()}.
	 */
	@Test
	default void testGetSource() {
		S source = createSource();
		A accessor = createAccessor(source);
		assertThat(accessor.getSource()).isNotNull().isSameAs(source);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#begin()}.
	 * Test method for {@link de.ims.icarus2.model.api.io.SynchronizedAccessor#end()}.
	 */
	@Test
	default void testBeginEndCycle() {
		A accessor = create();
		accessor.begin();
		try {
			// no-op
		} finally {
			accessor.end();
		}
	}
}
