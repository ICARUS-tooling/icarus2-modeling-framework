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
package de.ims.icarus2.test.factories;

import static de.ims.icarus2.test.TestUtils.typeLabel;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.test.util.DummyCache;
import it.unimi.dsi.fastutil.Hash.Strategy;

/**
 * @param <D> type of the delegate under test
 * @param <S> type of the source object wrapped by the delegate
 *
 * @author Markus Gärtner
 *
 */
public class DelegateTestFactory<D,S> extends DummyCache<DelegateTestFactory<D, S>, S> {

	private final Class<D> delegateClass;
	private Class<S> sourceClass;
	private Function<S, D> delegateGenerator;
	private Predicate<Method> methodFilter;
	private Consumer<S> sourceProcessor;

	public DelegateTestFactory(Class<D> delegateClass) {
		this.delegateClass = requireNonNull(delegateClass);
	}

	public DelegateTestFactory<D, S> sourceClass(Class<S> sourceClass) {
		this.sourceClass = requireNonNull(sourceClass);
		return this;
	}

	public DelegateTestFactory<D, S> delegateGenerator(Function<S, D> delegateGenerator) {
		this.delegateGenerator = requireNonNull(delegateGenerator);
		return this;
	}

	public DelegateTestFactory<D, S> methodFilter(Predicate<Method> methodFilter) {
		this.methodFilter = requireNonNull(methodFilter);
		return this;
	}

	public DelegateTestFactory<D, S> sourceProcessor(Consumer<S> sourceProcessor) {
		this.sourceProcessor = requireNonNull(sourceProcessor);
		return this;
	}

	private void validate() {
//		requireNonNull(delegateClass, "Missing delegate class");
		requireNonNull(sourceClass, "Missing source class");
		requireNonNull(delegateGenerator, "Missing delegate generator");
		// method filter might be null
	}

	public Stream<DynamicNode> createTests(TestReporter testReporter) {
		validate();

		/*
		 * Strategy:
		 * - detect all the delegate methods
		 * - for every delegate method, create a test:
		 *   - create mock of wrapped class/interface
		 *   - instantiate delegate class with mock as source
		 *   - mock/default all parameters
		 *   - call method on delegate instance
		 *   - verify invocation on mock and delegation of result value
		 */
		Predicate<Method> filter = m -> {
			int mods = m.getModifiers();
			return Modifier.isPublic(mods) && !Modifier.isStatic(mods);
		};
		if(methodFilter!=null) {
			 filter = filter.and(methodFilter);
		}

		List<Method> methods = Stream.of(delegateClass.getDeclaredMethods())
				.filter(filter)
				.collect(Collectors.toList());

		return Stream.of(dynamicContainer(sourceClass.getSimpleName()+"["+methods.size()+"]",
				methods.stream().map(this::createTest)));
	}

	private DynamicTest createTest(Method method) {
		return dynamicTest(method.getName()+'('+typeLabel(method.getParameterTypes())+')', () -> {
			Class<?>[] params = method.getParameterTypes();
			Object[] dummyArgs = new Object[params.length];
			Object[] returnValue = new Object[1];

			S source = mock(sourceClass, invoc -> {
				Method sourceMethod = invoc.getMethod();

				assertEquals(method.getName(), sourceMethod.getName());
				assertTrue(sourceMethod.getReturnType().isAssignableFrom(method.getReturnType()));
				assertArrayEquals(params, sourceMethod.getParameterTypes());
				assertArrayEquals(dummyArgs, invoc.getArguments());

				return returnValue[0];
			});

			if(sourceProcessor!=null) {
				sourceProcessor.accept(source);
			}

			for (int i = 0; i < params.length; i++) {
				dummyArgs[i] = createDummy(source, params[i]);
			}

			if(method.getReturnType()!=Void.TYPE) {
				returnValue[0] = createDummy(source, method.getReturnType());
			}

			D delegate = delegateGenerator.apply(source);

			Object result = method.invoke(delegate, dummyArgs);

			assertEquals(returnValue[0], result);
		});
	}

	static Strategy<Method> STRATEGY = new Strategy<Method>() {

		@Override
		public int hashCode(Method m) {
			return Objects.hash(m.getName(),
					Integer.valueOf(Arrays.hashCode(m.getParameterTypes())));
		}

		@Override
		public boolean equals(Method m1, Method m2) {
			requireNonNull(m1);
			boolean result =
					(m1==m2) ||
					(m2!=null
					&& m1.getName().equals(m2.getName())
					&& m1.getParameterCount()==m2.getParameterCount()
					&& (m1.getReturnType().isAssignableFrom(m2.getReturnType())
							|| m2.getReturnType().isAssignableFrom(m1.getReturnType()))
					&& Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())
					// Ignore exceptions in signature, as
//					&& Arrays.equals(m1.getExceptionTypes(), m2.getExceptionTypes())
					);

//			if(result)
//			System.out.printf("%b - %s vs. %s%n",result, m1, m2);

			return result;
		}
	};
}
