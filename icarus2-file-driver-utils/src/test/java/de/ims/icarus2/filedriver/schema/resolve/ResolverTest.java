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
package de.ims.icarus2.filedriver.schema.resolve;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

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
			resolver.prepareForReading(mock(Converter.class), mode, mock(ResolverContext.class), defaultOptions());
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
