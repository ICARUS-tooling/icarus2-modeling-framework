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
package de.ims.icarus2.test;

import static de.ims.icarus2.test.TestTags.AUTOMATIC;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.test.guard.ApiGuard;

/**
 * Adds automatic test generation for guarding the API of a class against
 * faulty {@code null} argument handling and inconsistencies between
 * getter and setter methods.
 *
 * @author Markus Gärtner
 *
 */
public interface ApiGuardedTest<T extends Object> extends TargetedTest<T> {

	public static final String API_GUARDED = "apiGuarded";

	/**
	 * Hook to configure the {@link ApiGuard} that is used in the {@link #guardApi(TestReporter)}
	 * method for testing.
	 * <p>
	 * This default implementation calls {@link ApiGuard#testPropertiesIfApi()}
	 * and sets the {@link ApiGuard#noArgsFallback(java.util.function.Supplier) no-args fallback}
	 * to {@link #create()}.
	 * <p>
	 * If subclasses wish to further customize the {@link ApiGuard} instance they
	 * should include a call to {@code super.configureApiGuard(apiGuard)} to ensure
	 * proper default configuration, unless of course they wish to completely
	 * change it.
	 *
	 * @param apiGuard
	 */
	default void configureApiGuard(ApiGuard<T> apiGuard) {
		apiGuard
			.testPropertiesIfApi()
			.nullGuard(true)
			.detectUnmarkedMethods(true)
			.strictNameFilter(true)
			.noArgsFallback(this::create);
	}

	@SuppressWarnings("unchecked")
	@TestFactory
	@Tag(AUTOMATIC)
	@Tag(API_GUARDED)
	@DisplayName("ApiGuard")
	default Stream<DynamicNode> guardApi(TestReporter testReporter) {
		ApiGuard<T> apiGuard = new ApiGuard<T>((Class<T>)getTestTargetClass());
		configureApiGuard(apiGuard);
		return apiGuard.createTests(testReporter);
	}
}
