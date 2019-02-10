/**
 *
 */
package de.ims.icarus2.test.refelct;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	MethodCache(Method method, Set<Method> overridden) {
		this.method = requireNonNull(method);

		for (int i = 0; i < method.getParameterCount(); i++) {
			parameterAnnotations.add(new HashMap<>());
		}

		cacheMethodInfo(method);
		overridden.forEach(this::cacheMethodInfo);

		//TODO sort method lists in the lookup maps
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

	private List<Method> lookupList(Map<Class<? extends Annotation>, List<Method>> map,
			Class<? extends Annotation> annotationClass) {
		List<Method> list = map.get(annotationClass);
		return list==null ? Collections.emptyList() :
			Collections.unmodifiableList(list);
	}

	//TODO add getter methods for annotations and the associated method lists (cf. ClassCache)

	private void cacheMethodInfo(Method method) {
		mapAnnotation(resultAnnotations, method.getAnnotatedReturnType(), method);
		mapAnnotation(annotations, method, method);
//		mapAnnotation(exceptionAnnotations, method.getAnnotatedExceptionTypes(), method);

		AnnotatedType[] paramAnnotations = method.getAnnotatedParameterTypes();
		for (int i = 0; i < method.getParameterCount(); i++) {
			mapAnnotation(parameterAnnotations.get(i), paramAnnotations[i], method);
		}
	}

	private void mapAnnotation(Map<Class<? extends Annotation>, List<Method>> map,
			AnnotatedElement element, Method method) {

		for(Annotation annotation : element.getDeclaredAnnotations()) {
			map.computeIfAbsent(annotation.getClass(), k -> new ArrayList<>()).add(method);
		}
	}

	private void sortHierarchically(List<Method> list) {
		list.sort(RefUtils.INHERITANCE_ORDER);
	}
}
