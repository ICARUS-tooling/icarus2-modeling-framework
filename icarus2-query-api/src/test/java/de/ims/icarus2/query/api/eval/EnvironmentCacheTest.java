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
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.dynamicGeneric;
import static de.ims.icarus2.query.api.eval.ExpressionTestUtils.raw;
import static de.ims.icarus2.util.collections.CollectionUtils.feedItems;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.query.api.eval.Environment.NsEntry;
import de.ims.icarus2.query.api.eval.env.SharedGlobalEnvironment;
import de.ims.icarus2.query.api.eval.env.SharedMemberEnvironments;
import de.ims.icarus2.query.api.eval.env.SharedUtilityEnvironments;
import de.ims.icarus2.util.Mutable;
import de.ims.icarus2.util.Mutable.MutableObject;

/**
 * @author Markus Gärtner
 *
 */
class EnvironmentCacheTest {

	@Nested
	class Construction {
		@Test
		void testOfNull() {
			assertThat(EnvironmentCache.of(null)).isNull();
		}

		@Test
		void testOfEmptyList() {
			assertThat(EnvironmentCache.of(Collections.emptyList())).isNull();
		}

		@Test
		void testOfFilledList() {
			assertThat(EnvironmentCache.of(list(SharedGlobalEnvironment.getInstance()))).isNotNull();
		}
	}

	@Nested
	class Resolve {
		private List<Environment> environments;
		private EnvironmentCache cache;

		List<Environment> makeEnvironments() {
			List<Environment> result = new ArrayList<>();

			result.add(SharedGlobalEnvironment.getInstance());

			feedItems(result, SharedMemberEnvironments.all());
			feedItems(result, SharedUtilityEnvironments.all());

			return result;
		}

		@BeforeEach
		void setUp() {
			environments = makeEnvironments();
			cache = EnvironmentCache.of(environments);
		}

		@AfterEach
		void tearDown() {
			environments = null;
			cache = null;
		}

		@Nested
		class ResolveFields {

		}

		@Nested
		class ResolveMethods {

			@Test
			void testEquals() {
				Object obj = new Object();
				Expression<?> scope = raw(obj);

				Mutable<Object> other = new MutableObject<>(new Object());
				Expression<?> arg = dynamicGeneric(other::get);

				List<NsEntry> candidates = cache.resolve(scope.getResultType(),
						"equals", TypeFilter.BOOLEAN, arg.getResultType());
				assertThat(candidates).isNotNull().hasSize(1);

				Expression<?> exp = candidates.get(0).instantiate(scope, arg);

				assertThat(exp.computeAsBoolean()).isFalse();

				other.set(obj);
				assertThat(exp.computeAsBoolean()).isTrue();
			}

			@Test
			void testFindAlias() {
				List<NsEntry> candidates1 = cache.resolve(
						TypeInfo.TEXT, "length", TypeFilter.INTEGER);
				assertThat(candidates1).isNotNull().hasSize(1);

				List<NsEntry> candidates2 = cache.resolve(
						TypeInfo.TEXT, "len", TypeFilter.INTEGER);
				assertThat(candidates2).isNotNull().hasSize(1);

				assertThat(candidates1.get(0)).isSameAs(candidates2.get(0));
			}

			@Test
			void testFindGlobal() {
				List<NsEntry> candidates = cache.resolve(
						null, "abs", TypeFilter.INTEGER, TypeInfo.INTEGER);
				assertThat(candidates).isNotNull().hasSize(1);
			}

			@Test
			void testFindNoFilter() {
				List<NsEntry> candidates = cache.resolve(
						null, "abs", null, TypeInfo.INTEGER);
				assertThat(candidates).isNotNull().hasSize(1);
			}

			@Test
			void testNameMiss() {
				List<NsEntry> candidates = cache.resolve(
						null, "XXXXX", TypeFilter.ALL);
				assertThat(candidates).isNotNull().isEmpty();
			}

			@Test
			void testArgumentCountMiss() {
				List<NsEntry> candidates = cache.resolve(
						TypeInfo.TEXT, "length", TypeFilter.ALL, TypeInfo.GENERIC);
				assertThat(candidates).isNotNull().isEmpty();
			}

			@Test
			void testArgumentTypeMiss() {
				List<NsEntry> candidates = cache.resolve(
						TypeInfo.TEXT, "charAt", TypeFilter.ALL, TypeInfo.FLOATING_POINT);
				assertThat(candidates).isNotNull().isEmpty();
			}

			//TODO
		}
	}
}
