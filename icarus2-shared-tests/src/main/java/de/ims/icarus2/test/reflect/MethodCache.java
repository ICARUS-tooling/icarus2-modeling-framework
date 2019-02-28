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
package de.ims.icarus2.test.reflect;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Markus Gärtner
 *
 */
public class MethodCache {

	/**
	 * The method for which we cache info
	 */
	final Method method;

	/**
	 * For every annotation available for this method or any of its
	 * overridden ones this map shows where in the hierarchy they
	 * have been assigned.
	 */
	final Map<Class<? extends Annotation>, List<Method>> annotations = new HashMap<>();

	final Map<Class<? extends Annotation>, List<Method>> resultAnnotations = new HashMap<>();

	//TODO disabled for now, needs better data structrue that maps from exception types
//	final Map<Annotation, List<Method>> exceptionAnnotations = new HashMap<>();

	final List<Map<Class<? extends Annotation>, List<Method>>> parameterAnnotations = new ArrayList<>();

	final Consumer<String> log;

	MethodCache(Method method, Set<Method> overridden, Consumer<String> log) {
		this.method = requireNonNull(method);
		this.log = log;

		for (int i = 0; i < method.getParameterCount(); i++) {
			parameterAnnotations.add(new HashMap<>());
		}

		cacheMethodInfo(method);
		overridden.forEach(this::cacheMethodInfo);

		//TODO sort method lists in the lookup maps
	}

	private List<Method> lookupList(Map<Class<? extends Annotation>, List<Method>> map,
			Class<? extends Annotation> annotationClass) {
		List<Method> list = map.get(annotationClass);
		return list==null ? Collections.emptyList() :
			Collections.unmodifiableList(list);
	}

	//TODO add getter methods for annotations and the associated method lists (cf. ClassCache)

	private void cacheMethodInfo(Method method) {
		mapAnnotation(resultAnnotations, method.getAnnotatedReturnType(), method, "result");
		mapAnnotation(annotations, method, method, "method");
//		mapAnnotation(exceptionAnnotations, method.getAnnotatedExceptionTypes(), method);

		//TODO somehow we're missing out on parameter annotations in this cache...
		Parameter[] parameters = method.getParameters();
		assertEquals(parameterAnnotations.size(), parameters.length);
		for (int i = 0; i < parameterAnnotations.size(); i++) {
			mapAnnotation(parameterAnnotations.get(i), parameters[i], method, "parameter");
		}
	}

	private void mapAnnotation(Map<Class<? extends Annotation>, List<Method>> map,
			AnnotatedElement element, Method method, String type) {

		for(Annotation annotation : element.getDeclaredAnnotations()) {
			map.computeIfAbsent(annotation.annotationType(), k -> new ArrayList<>()).add(method);
			if(log!=null)
				log.accept(String.format("Mapping %s annotation '%s' for method: %s",
						type, annotation.annotationType(), method));
		}
	}

	private void sortHierarchically(List<Method> list) {
		list.sort(RefUtils.METHOD_INHERITANCE_ORDER);
	}

	public Method getMethod() {
		return method;
	}

	public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
		return annotations.containsKey(annotationClass);
	}

	public boolean hasResultAnnotation(
			Class<? extends Annotation> annotationClass) {
		return resultAnnotations.containsKey(annotationClass);
	}

	public boolean hasParameterAnnotation(
			Class<? extends Annotation> annotationClass) {
		return parameterAnnotations.stream()
				.anyMatch(map -> map.containsKey(annotationClass));
	}

	public List<Method> getMethodsForAnnotation(
			Class<? extends Annotation> annotationClass) {
		return lookupList(annotations, annotationClass);
	}

	public List<Method> getMethodsForResultAnnotation(
			Class<? extends Annotation> annotationClass) {
		return lookupList(resultAnnotations, annotationClass);
	}

	public List<Method> getMethodsForParameterAnnotation(
			Class<? extends Annotation> annotationClass) {
		return parameterAnnotations.stream()
				.map(map -> lookupList(map, annotationClass))
				.flatMap(List::stream)
				.distinct()
				.collect(Collectors.toList());
	}
}
