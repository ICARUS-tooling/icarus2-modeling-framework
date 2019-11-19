/**
 *
 */
package de.ims.icarus2.filedriver.schema.resolve;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public interface ResolverTest<R extends Resolver> extends ApiGuardedTest<R>, GenericTest<R> {

	default Stream<ReadMode> supportedReadModes() {
		return Stream.of(ReadMode.values());
	}

	default Options defaultOptions() {
		return Options.none();
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.schema.resolve.Resolver#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.filedriver.Converter.ReadMode, java.util.function.Function, de.ims.icarus2.util.Options)}.
	 */
	@TestFactory
	default Stream<DynamicNode> testPrepareForReading() {
		return supportedReadModes().map(mode -> dynamicTest(mode.name(), () -> {
			R resolver = create();
			resolver.prepareForReading(mock(Converter.class), mode, mock(Function.class), defaultOptions());
		}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.filedriver.schema.resolve.Resolver#close()}.
	 */
	@Test
	default void testClose() {
		create().close();
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#testMandatoryConstructors()
	 */
	@Override
	default void testMandatoryConstructors() throws Exception {
		// Resolvers are supposed to provide public no-args constructor
		getTestTargetClass().newInstance();
	}
}
