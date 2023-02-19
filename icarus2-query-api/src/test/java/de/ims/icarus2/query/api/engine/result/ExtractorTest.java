/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.query.api.engine.result.Extractor.BooleanExtractor;
import de.ims.icarus2.query.api.engine.result.Extractor.FloatingPointExtractor;
import de.ims.icarus2.query.api.engine.result.Extractor.IntegerExtractor;
import de.ims.icarus2.query.api.engine.result.Extractor.TextExtractor;
import de.ims.icarus2.query.api.exp.Expression;
import de.ims.icarus2.query.api.exp.Literals;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.collections.Substitutor;

/**
 * @author Markus Gärtner
 *
 */
class ExtractorTest {

	private static final TypeInfo[] TYPES = {
			TypeInfo.INTEGER,
			TypeInfo.FLOATING_POINT,
			TypeInfo.BOOLEAN,
			TypeInfo.TEXT,
	};

	interface TestBase<E extends Extractor> extends ApiGuardedTest<E> {

		TypeInfo supportedType();

		long defaultValue();

		Expression<?> wrap(long value);

		E create(Expression<?> exp);

		@Override
		default E createTestInstance(TestSettings settings) {
			return settings.process(create(wrap(defaultValue())));
		}

		@TestFactory
		default Stream<DynamicTest> testUnsupportedTypes() throws Exception {
			final TypeInfo supportedType = supportedType();
			return Stream.of(TYPES)
					.filter(t -> t!=supportedType)
					.map(type -> dynamicTest(type.toString(), () -> {
						Expression<?> exp = mock(Expression.class);
						when(exp.getResultType()).thenReturn(type);
						assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
								() -> create(exp));
					}));
		}

		@Test
		default void testDefaultValue() throws Exception {
			long defaultValue = defaultValue();
			E extractor = create(wrap(defaultValue));
			long value = extractor.extract();
			assertThat(value).isEqualTo(defaultValue);
		}
	}

	@Nested
	class ForIntegerExtractor implements TestBase<IntegerExtractor> {

		@Override
		public TypeInfo supportedType() { return TypeInfo.INTEGER; }

		@Override
		public Class<?> getTestTargetClass() { return IntegerExtractor.class; }

		@Override
		public long defaultValue() { return 12345; }

		@Override
		public Expression<?> wrap(long value) { return Literals.of(value); }

		@Override
		public IntegerExtractor create(Expression<?> exp) {
			return new IntegerExtractor(exp);
		}
	}

	@Nested
	class ForFloatingPointExtractor implements TestBase<FloatingPointExtractor> {

		@Override
		public TypeInfo supportedType() { return TypeInfo.FLOATING_POINT; }

		@Override
		public Class<?> getTestTargetClass() { return FloatingPointExtractor.class; }

		@Override
		public long defaultValue() { return Double.doubleToLongBits(123.456); }

		@Override
		public Expression<?> wrap(long value) { return Literals.of(Double.longBitsToDouble(value)); }

		@Override
		public FloatingPointExtractor create(Expression<?> exp) {
			return new FloatingPointExtractor(exp);
		}
	}

	@Nested
	class ForBooleanExtractor implements TestBase<BooleanExtractor> {

		@Override
		public TypeInfo supportedType() { return TypeInfo.BOOLEAN; }

		@Override
		public Class<?> getTestTargetClass() { return BooleanExtractor.class; }

		@Override
		public long defaultValue() { return BooleanExtractor.TRUE; }

		@Override
		public Expression<?> wrap(long value) { return Literals.of(value==BooleanExtractor.TRUE); }

		@Override
		public BooleanExtractor create(Expression<?> exp) {
			return new BooleanExtractor(exp);
		}

		@Test
		void testFalseValue() throws Exception {
			long expected = BooleanExtractor.FALSE;
			BooleanExtractor extractor = create(wrap(expected));
			long value = extractor.extract();
			assertThat(value).isEqualTo(expected);
		}
	}

	@Nested
	class ForTextExtractor implements TestBase<TextExtractor> {

		private Substitutor<CharSequence> substitutor;

		@BeforeEach
		void setUp() {
			substitutor = new Substitutor<>();
		}

		@AfterEach
		void tearDown() {
			substitutor.close();
			substitutor = null;
		}

		@Override
		public TypeInfo supportedType() { return TypeInfo.TEXT; }

		@Override
		public Class<?> getTestTargetClass() { return TextExtractor.class; }

		@Override
		public long defaultValue() { return substitutor.applyAsInt("test"); }

		@Override
		public Expression<?> wrap(long value) { return Literals.of(substitutor.apply(strictToInt(value))); }

		@Override
		public TextExtractor create(Expression<?> exp) {
			return new TextExtractor(exp, substitutor);
		}
	}
}
