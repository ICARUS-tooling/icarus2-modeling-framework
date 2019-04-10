/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.guard;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.test.reflect.RefUtils;

/**
 * @author Markus Gärtner
 *
 */
class ConstructorGuardian<T> extends Guardian<T> {

	private final Class<?> targetClass;

	public ConstructorGuardian(ApiGuard<T> apiGuard) {
		super(apiGuard);
		this.targetClass = requireNonNull(apiGuard.getTargetClass());
	}

	/**
	 * @see de.ims.icarus2.test.guard.Guardian#createTests()
	 */
	@Override
	DynamicNode createTests(TestReporter testReporter) {
		Constructor<?>[] constructors = targetClass.getConstructors();
		List<DynamicNode> tests = Stream.of(constructors)
				.filter(c -> c.getParameterCount()>0)
				.map(this::createTestsForConstructor)
				.collect(Collectors.toCollection(ArrayList::new));
		String displayName = String.format("Constructors [%d/%d]",
				Integer.valueOf(tests.size()),
				Integer.valueOf(constructors.length));

		return dynamicContainer(displayName, tests);
	}

	private DynamicNode createTestsForConstructor(Constructor<?> constructor) {
		Collection<ParamConfig> variations = variateNullParameter(null, constructor);
		String baseLabel = RefUtils.toSimpleString(constructor);

		if(variations.isEmpty()) {
			return dynamicContainer(baseLabel+" - no null-guarded arguments", Collections.emptyList());
		}

		return dynamicContainer(
				baseLabel+" ["+variations.size()+" null-guarded arguments]",
				sourceUriFor(constructor),
				variations.stream().map(config ->
					createNullTest(config, constructor::newInstance)));
	}
}
