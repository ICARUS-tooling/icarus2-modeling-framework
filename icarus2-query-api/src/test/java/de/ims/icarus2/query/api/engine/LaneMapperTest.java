/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives._int;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.LongStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;

/**
 * @author Markus Gärtner
 *
 */
class LaneMapperTest {

	void assertMapping(LaneMapper mapper, long...values) {
		assertThat(mapper.size()).isEqualTo(values.length);
		for (int i = 0; i < values.length; i++) {
			assertThat(mapper.indexAt(i)).as("mapping mismatch at index %d", _int(i)).isEqualTo(values[i]);
		}
	}

	@Nested
	class ForMapperBacked {
		@Test
		public void testFull() throws Exception {
			long[] valuesFor0 = {0, 1, 2, 3};
			long[] valuesFor1 = {3};
			long[] valuesFor2 = {2, 4, 5};
			long[] valuesFor4 = {999, 9999999};

			MappingReader reader = mock(MappingReader.class);
			when(_boolean(reader.lookup(anyLong(), any(), any()))).thenAnswer(invoc -> {
				long[] values = null;
				switch (((Number)invoc.getArgument(0)).intValue()) {
				case 0: values = valuesFor0; break;
				case 1: values = valuesFor1; break;
				case 2: values = valuesFor2; break;
				case 4: values = valuesFor4; break;
				default:
					break;
				}

				if(values!=null) {
					IndexCollector collector = invoc.getArgument(1);
					LongStream.of(values).forEach(collector);
					return Boolean.TRUE;
				}

				return Boolean.FALSE;
			});

			LaneMapper mapper = LaneMapper.forMapping(reader, 2);

			// No mapping chosen yet, so should be empty
			assertMapping(mapper);

			mapper.reset(1);
			assertMapping(mapper, valuesFor1);

			mapper.reset(4);
			assertMapping(mapper, valuesFor4);

			mapper.reset(0);
			assertMapping(mapper, valuesFor0);

			mapper.reset(2);
			assertMapping(mapper, valuesFor2);

			// Unknown source value, so should be empty
			mapper.reset(123456);
			assertMapping(mapper);
		}
	}

	@Nested
	class ForFixed {
		@Test
		public void testFull() throws Exception {
			long[] valuesFor0 = {0, 1, 2, 3};
			long[] valuesFor1 = {3};
			long[] valuesFor2 = {2, 4, 5};
			long[] valuesFor4 = {999, 9999999};

			LaneMapper mapper = LaneMapper.fixedBuilder()
					.map(0, valuesFor0)
					.map(1, valuesFor1)
					.map(2, valuesFor2)
					.map(4, valuesFor4)
					.build();

			// No mapping chosen yet, so should be empty
			assertMapping(mapper);

			mapper.reset(1);
			assertMapping(mapper, valuesFor1);

			mapper.reset(4);
			assertMapping(mapper, valuesFor4);

			mapper.reset(0);
			assertMapping(mapper, valuesFor0);

			mapper.reset(2);
			assertMapping(mapper, valuesFor2);

			// Unknown source value, so should be empty
			mapper.reset(123456);
			assertMapping(mapper);
		}
	}
}
