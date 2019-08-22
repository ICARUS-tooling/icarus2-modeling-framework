/**
 *
 */
package de.ims.icarus2.test;

import static de.ims.icarus2.test.TestTags.AUTOMATIC;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.test.factories.DelegateTestFactory;

/**
 * @param <D> type of the delegate under test
 * @param <S> type of the source object wrapped by the delegate
 *
 * @author Markus Gärtner
 *
 */
public interface DelegateTest<D, S> extends GenericTest<D> {

	/**
	 * Callback to configure factory used to create tests.
	 *
	 * @param builder
	 */
	void configure(DelegateTestFactory<D, S> factory);


	@TestFactory
	@Tag(AUTOMATIC)
	@DisplayName("DelegateTest")
	default Stream<DynamicNode> guardApi(TestReporter testReporter) {
		@SuppressWarnings("unchecked")
		DelegateTestFactory<D, S> factory = new DelegateTestFactory<>((Class<D>)getTestTargetClass());
		configure(factory);
		return factory.createTests(testReporter);
	}
}
