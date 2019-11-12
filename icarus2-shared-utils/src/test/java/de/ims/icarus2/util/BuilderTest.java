/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.test.TestUtils.NO_OP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;

/**
 * @author Markus Gärtner
 *
 */
public interface BuilderTest<T, B extends AbstractBuilder<B, T>> extends ApiGuardedTest<B> {

	/**
	 * Test method for {@link de.ims.icarus2.util.AbstractBuilder#thisAsCast()}.
	 */
	@Test
	default void testThisAsCast() {
		B builder = create();
		assertThat(builder.thisAsCast()).isSameAs(builder);
	}

	/**
	 * Assumes that no builder is fully initialized right from the start.
	 *
	 * Test method for {@link de.ims.icarus2.util.AbstractBuilder#validate()}.
	 */
	@Test
	default void testValidateBlank() {
		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(create()::validate);
	}

	/** Provides individual builder calls that are supposed to cause exceptions */
	default List<Triple<String, Class<? extends Throwable>, Consumer<? super B>>> invalidOps() {
		return Collections.emptyList();
	}

	/**
	 * Verifies that certain builder calls cause specific exceptions.
	 */
	@TestFactory
	default Stream<DynamicNode> testInvalidInput() {
		List<Triple<String, Class<? extends Throwable>, Consumer<? super B>>> ops = invalidOps();
		if(ops.isEmpty()) {
			return Stream.of(dynamicTest("No info for invalid builder calls", NO_OP));
		}

		return ops.stream().map(config -> dynamicTest(config.first, () ->
			assertThatExceptionOfType(config.second)
				.isThrownBy(() -> config.third.accept(create()))
		));
	}

	/**
	 * Provides builder call chains that result in invalid configurations
	 * and cause {@link IllegalStateException} to be thrown when calling
	 * {@link B#build()}.
	 */
	default List<Pair<String, Consumer<? super B>>> invalidConfigurations() {
		return Collections.emptyList();
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.AbstractBuilder#validate()}.
	 */
	@TestFactory
	default Stream<DynamicNode> testInvalidConfigurations() {
		List<Pair<String, Consumer<? super B>>> configs = invalidConfigurations();
		if(configs.isEmpty()) {
			return Stream.of(dynamicTest("No invalid configurations", NO_OP));
		}
		return configs.stream().map(config -> dynamicTest(config.first, () -> {
			B builder = create();
			config.second.accept(builder);
			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(builder::validate);
		}));
	}
}
