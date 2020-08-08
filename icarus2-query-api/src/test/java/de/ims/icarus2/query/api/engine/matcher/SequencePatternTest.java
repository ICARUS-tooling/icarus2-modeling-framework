/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.util.lang.Primitives._boolean;
import static de.ims.icarus2.util.lang.Primitives.unbox;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.LongStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.ims.icarus2.query.api.engine.matcher.SequencePattern.StateMachine;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * @author Markus Gärtner
 *
 */
class SequencePatternTest {

	@Test
	void testBuilder() {
		assertThat(SequencePattern.builder()).isNotNull();
	}

	private static NodeMatcher mockNode(long...hits) {
		final LongSet set = new LongArraySet();
		LongStream.of(hits).forEach(set::add);

		NodeMatcher nm = mock(NodeMatcher.class);
		when(_boolean(nm.matches(anyLong(), any()))).then(invoc -> {
			long index = unbox((Long)invoc.getArgument(0));
			return _boolean(set.contains(index));
		});

		return nm;
	}

	@Nested
	class IndividualNodeTests {

		@Nested
		class ForSingle {

			@ParameterizedTest
			@CsvSource({
				"xXx",
			})
			void testMatch(String s, int pos) {
				StateMachine sm = new StateMachine();

			}
		}
	}
}
