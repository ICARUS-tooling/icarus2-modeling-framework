/**
 *
 */
package de.ims.icarus2.query.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.TestFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.ims.icarus2.query.api.IqlQueryGenerator.Config;
import de.ims.icarus2.query.api.IqlQueryGenerator.IncrementalBuild;
import de.ims.icarus2.query.api.iql.IqlQueryElement;
import de.ims.icarus2.query.api.iql.IqlType;
import de.ims.icarus2.query.api.iql.IqlUtils;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
public class IqlJsonTest {

	@SuppressWarnings("boxing")
	@RandomizedTest
	@TestFactory
	Stream<Object> testJsonSerialization(RandomGenerator rng) {
		return Stream.of(IqlType.values())
				.map(type -> dynamicTest(type.name(), () -> {
			IqlQueryGenerator generator = new IqlQueryGenerator(rng);

			ObjectMapper mapper = IqlUtils.createMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);

			Config config = IqlQueryGenerator.config();
			final IncrementalBuild<IqlQueryElement> build = generator.build(type, config);

			build.getInstance().checkIntegrity();

			/*
			 *  do-while loop is used since the build can be static (i.e. have no
			 *  extra construction steps) but we still need to run a test for the
			 *  "blank" instance in any case.
			 */
			int step = 1;
			do {

				String label = String.format("%s: %s - step %d/%d",
						build.currentLabel(), type, step++, build.getChangeCount()+1);

				String json = mapper.writeValueAsString(build.getInstance());
				assertThat(json).isNotBlank();

				System.out.println(label);
				System.out.println(json);
				System.out.println("-----------------------------------------");
			} while(build.applyNextChange());
		}));
	}
}
