/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * @author Markus Gärtner
 *
 */
public interface GenericTest<T extends Object> {

	Class<? extends T> getTestTargetClass();

	default T createNoArgs() throws Exception {
		return getTestTargetClass().newInstance();
	}

	default T create(Class<?>[] signature, Object...values) throws Exception {
		Class<? extends T> clazz = getTestTargetClass();

		Constructor<? extends T> constructor = clazz.getConstructor(signature);

		return constructor.newInstance(values);
	}

	/**
	 * This is a utility test method that tries to call all the
	 * obligatory constructors for implementations of a certain interface.
	 * Usually a test specification (interface) wishing to add additional
	 * constructors to this test should simply override the method. But
	 * if a deviation from constructor rules imposed by a super interface
	 * is desired the method can be overriden without a call to the super
	 * implementation.
	 *
	 * @throws Exception
	 */
	@Test
	default void testMandatoryConstructors() throws Exception {
		// no-op
	}

	public static <K extends Object> K NO_DEFAULT() {
		return (K) null;
	}

	public static <K extends Object> K[] NO_ILLEGAL() {
		return (K[]) null;
	}

	public static final BiConsumer<Executable, Object> NO_CHECK = (e, val) -> fail("Not meant to have legality check called");


	public static final Consumer<Executable> NPE_CHECK = TestUtils::assertNPE;
}
