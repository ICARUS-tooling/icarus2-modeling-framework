/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.test.func.ThrowingBiConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface WritableMappingTest<M extends WritableMapping, C extends MappingTest.Config<M>> extends MappingTest<M, C> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.WritableMapping#newWriter()}.
	 */
	@TestFactory
	default Stream<DynamicNode> testNewWriter() {
		return basicTests((config, instance) -> {
			MappingWriter writer = instance.newWriter();
			assertNotNull(writer);
			assertSame(instance, writer.getSource());
		});
	}

	default Stream<DynamicNode> writerTests(ThrowingBiConsumer<Config<M>, MappingWriter> task) {
		return configurations().map(config -> dynamicTest(config.label, () -> {
			try(M mapping = config.create();
					MappingWriter writer = mapping.newWriter()) {
				task.accept(config, writer);
			}
		}));
	}
}
