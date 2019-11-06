/**
 *
 */
package de.ims.icarus2.model.api.driver.mapping;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.test.func.ThrowingBiConsumer;

/**
 * @author Markus Gärtner
 *
 */
public interface WritableMappingTest<M extends WritableMapping> extends MappingTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.api.driver.mapping.WritableMapping#newWriter()}.
	 */
	@Test
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

	default Stream<Coverage> coverages() {
		return Stream.of(Coverage.values());
	}

	default Stream<Relation> relations() {
		return Stream.of(Relation.values());
	}
}
