/**
 *
 */
package de.ims.icarus2.model.api.view;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
public interface CorpusViewTest<V extends CorpusView> extends GenericTest<V> {

	@Provider
	V createForAccessMode(AccessMode accessMode);

	@Provider
	V createForSize(long size);

	Set<AccessMode> getSupportedAccessModes();

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getCorpus()}.
	 */
	@Test
	default void testGetCorpus() {
		assertNotNull(create().getCorpus());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getScope()}.
	 */
	@Test
	default void testGetScope() {
		assertNotNull(create().getScope());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getAccessMode()}.
	 */
	@TestFactory
	default Stream<DynamicTest>  testGetAccessMode() {
		Set<AccessMode> supportedModes = getSupportedAccessModes();
		assertCollectionNotEmpty(supportedModes);

		return Stream.concat(
				supportedModes.stream()
					.map(mode -> dynamicTest(mode.name(), () -> {
						V view = createForAccessMode(mode);
						assertNotNull(view.getAccessMode());
						if(mode.isRead()) {
							assertTrue(view.getAccessMode().isRead(), "View must be readable");
						}
						if(mode.isWrite()) {
							assertTrue(view.getAccessMode().isWrite(), "View must be writable");
						}
					})),
				Stream.of(AccessMode.values())
					.filter(mode -> !supportedModes.contains(mode))
					.map(mode -> dynamicTest(mode.name()+" (unsupported)", () -> {
						assertModelException(GlobalErrorCode.INVALID_INPUT,
								() -> createForAccessMode(mode));
					}))
				);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#getSize()}.
	 */
	@ParameterizedTest
	@ValueSource(longs = {UNSET_LONG, 100, 100_000, 100_000_000, Long.MAX_VALUE/2})
	default void testGetSize(long size) {
		long expectedSize = size==UNSET_LONG ? 0L : size;
		assertEquals(expectedSize, createForSize(size).getSize());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#fetchPrimaryLayer()}.
	 */
	@Test
	default void testFetchPrimaryLayer() {
		V view = create();
		Scope scope = view.getScope();

		assertSame(scope.getPrimaryLayer(), view.fetchPrimaryLayer());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#fetchLayer(java.lang.String)}.
	 */
	@Test
	default void testFetchLayerString() {
		V view = create();
		Scope scope = view.getScope();
		for(Layer layer : scope.getLayers()) {
			assertSame(layer, view.fetchLayer(layer.getManifest().getUniqueId()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.view.CorpusView#fetchLayer(java.lang.String, java.lang.String)}.
	 */
	@Test
	default void testFetchLayerStringString() {
		V view = create();
		Scope scope = view.getScope();
		for(Layer layer : scope.getLayers()) {
			Context context = layer.getContext();
			assertSame(layer, view.fetchLayer(
					context.getManifest().getId().get(),
					layer.getManifest().getId().get()));
		}
	}

}
