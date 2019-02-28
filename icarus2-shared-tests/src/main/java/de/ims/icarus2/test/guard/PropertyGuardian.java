/**
 *
 */
package de.ims.icarus2.test.guard;

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.UNKNOWN_DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertOptGetter;
import static de.ims.icarus2.test.TestUtils.assertRestrictedSetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.failTest;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestReporter;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.test.func.TriConsumer;
import de.ims.icarus2.test.reflect.ClassCache;
import de.ims.icarus2.test.reflect.MethodCache;

/**
 * @author Markus Gärtner
 *
 */
class PropertyGuardian<T> extends Guardian<T> {

	private final Class<T> targetClass;

	/**
	 * Final pairing of setter and getter methods for each property.
	 */
	private final List<PropertyConfig> propertyMethods = new ArrayList<>();

	private final ClassCache<T> classCache;

	/**
	 * If set to {@code true} we're allowed to consider every method with
	 * a single argument that follows the naming scheme as a property method.
	 */
	private boolean detectUnmarkedMethods = false;

	private TestReporter testReporter;

	private Supplier<? extends T> creator;

	static final Predicate<? super Method> PROPERTY_FILTER =
			(m) -> {
				boolean isStatic = Modifier.isStatic(m.getModifiers()); // must not be static
				boolean isPublic = Modifier.isPublic(m.getModifiers());  // must be public
				boolean isObjMethod = m.getDeclaringClass()==Object.class; // ignore all original Object methods
				boolean hasParams = m.getParameterCount()>0;
				boolean hasSingleParam = m.getParameterCount()==1; // setters
				boolean isVoidReturn = m.getReturnType()==void.class;
				boolean isChainable = m.getDeclaringClass().isAssignableFrom(m.getReturnType());

//				System.out.printf("stat=%b, pub=%b, obj=%b, params=%b, single=%b, void=%b: %s%n",
//						isStatic, isPublic, isObjMethod, hasParams, hasSingleParam, isVoidReturn, m);

				return !isStatic && isPublic && !isObjMethod // Filter out via basic properties
						&& ((hasSingleParam && isVoidReturn)
							|| (hasSingleParam && isChainable)
							|| (!hasParams && !isVoidReturn));
			};

	public PropertyGuardian(ApiGuard<T> apiGuard) {
		super(apiGuard);

		targetClass = apiGuard.getTargetClass();
		detectUnmarkedMethods = apiGuard.isDetectUnmarkedMethods();
		creator = apiGuard.instanceCreator();

		classCache = ClassCache.<T>newBuilder()
				.targetClass(targetClass)
				.methodFilter(PROPERTY_FILTER)
//				.log(System.out::println)
				.build();
	}

	/**
	 * @see de.ims.icarus2.test.guard.Guardian#createTests(org.junit.jupiter.api.TestReporter)
	 */
	@Override
	DynamicNode createTests(TestReporter testReporter) {
		this.testReporter = requireNonNull(testReporter);

		//TODO maybe allow pre-filtering?

		// Detect all the property methods
		collectPropertyMethods();

		// Make sure we only keep the "correct" ones
		validatePropertyMappings();

		@SuppressWarnings("boxing")
		String displayName = String.format("%s (%d)",
				"Properties", propertyMethods.size());

		// Turn method mapping into tests
		return dynamicContainer(displayName,
				propertyMethods.stream()
				.map(this::createTestsForProperty)
				.collect(Collectors.toList()));
	}

	private Object tryParseDefaultGetterValue(MethodCache methodCache) {
		String defaultValue = extractConsistentValue(Guarded.class,
				methodCache, Guarded::defaultValue);
		if(defaultValue!=null && !defaultValue.isEmpty()) {
			Class<?> returnType = methodCache.getMethod().getReturnType();
			switch (returnType.getSimpleName()) {
			case "byte": return Byte.valueOf(defaultValue);
			case "short": return Short.valueOf(defaultValue);
			case "int": return Integer.valueOf(defaultValue);
			case "long": return Long.valueOf(defaultValue);
			case "boolean": return Boolean.valueOf(defaultValue);
			case "float": return Float.valueOf(defaultValue);
			case "double": return Double.valueOf(defaultValue);
			case "char": return Character.valueOf(defaultValue.charAt(0));

			default:
				throw new IllegalArgumentException("Unable to parse default value for return type: "+returnType);
			}
		}

		return null;
	}

	private Supplier<Object> createDefault(String property, MethodCache methodCache, boolean allowNullReturn) {
		Class<?> returnType = methodCache.getMethod().getReturnType();
		Object defaultValue = null;

		// For primitives we MUST provide non-null default return values
		if(returnType.isPrimitive()) {
			defaultValue = tryParseDefaultGetterValue(methodCache);

			if(defaultValue==null) {
				defaultValue = getDefaultReturnValue(property, returnType);

//				System.out.printf("default value for '%s': %s%n", property, defaultValue);
			}
		}


		if(defaultValue!=null) {
			return DEFAULT(defaultValue);
		}

		/*
		 * We don't have any easy way of obtaining the expected default value.
		 * As such we simply go and either expect _any_ non-null value or enforce
		 * a null value as default.
		 */
		return allowNullReturn ? NO_DEFAULT() : UNKNOWN_DEFAULT();
	}

	private String displayName(PropertyConfig config) {
		return String.format("%s [%s]", config.property,
				config.getter.getReturnType().getName());
	}

	@SuppressWarnings("unchecked")
	private DynamicNode createTestsForProperty(PropertyConfig config) {
		MethodCache getterMethodCache = classCache.getMethodCache(config.getter);
		MethodCache setterMethodCache = classCache.getMethodCache(config.setter);

		boolean allowNullParam = setterMethodCache.hasParameterAnnotation(Nullable.class);

		// Nullability of return value can be defined in two ways
		boolean allowNullReturn = getterMethodCache.hasAnnotation(Nullable.class)
				|| getterMethodCache.hasResultAnnotation(Nullable.class);

		// If return value is declared to be nullable but is a primitive type, we have to abort
		if(allowNullReturn && config.getter.getReturnType().isPrimitive())
			return dynamicTest(
					displayName(config),
					failTest(String.format("Getter declares nullable primitive: %s", config.getter)));

		// Need to handle getter methods that wrap values into an Optional
		boolean isOptionalReturn = config.getter.getReturnType()==Optional.class;

		boolean isRestrictedSetter = setterMethodCache.hasAnnotation(Guarded.class)
				&& extractConsistentValue(Guarded.class, setterMethodCache, Guarded::restricted).booleanValue();

		final Supplier<Object> DEFAULT = createDefault(config.property, getterMethodCache, allowNullReturn);

		final Class<?> paramClass = config.setter.getParameterTypes()[0];

		// Wrap setter to handle the reflection-related exceptions internally
		BiConsumer<T, Object> setter = (instance, value) -> {
			try {
				config.setter.invoke(instance, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				if(e.getCause() instanceof RuntimeException)
					throw (RuntimeException) e.getCause();

				throw new TestAbortedException("Failed to invoke setter", e);
			}
		};

		// Wrap getter to handle the reflection-related exceptions internally
		Function<T, Object> getter = (instance) -> {
			try {
				return config.getter.invoke(instance);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				if(e.getCause() instanceof RuntimeException)
					throw (RuntimeException) e.getCause();

				throw new TestAbortedException("Failed to invoke getter", e);
			}
		};

		DynamicTest getterTest;
		if(isOptionalReturn) {
			getterTest = dynamicTest("getter[optionalReturn]",
					() -> {
						T instance = createInstance();
						assertOptGetter(instance,
								resolveParameter(instance, paramClass),
								isRestrictedSetter ? null : resolveParameter(instance, paramClass),
								DEFAULT,
								x -> (Optional<Object>)getter.apply(x),
								setter);
					});
		} else {
			getterTest = dynamicTest("getter",
					() -> {
						T instance = createInstance();
						assertGetter(instance,
								resolveParameter(instance, paramClass),
								isRestrictedSetter ? null : resolveParameter(instance, paramClass),
								DEFAULT,
								getter,
								setter);
					});
		}

		DynamicTest setterTest;
		if(isRestrictedSetter) {
			// Restricted setter means we expect an exception after the first invokation
			setterTest = dynamicTest("setter[restricted]",
					() -> {
						T instance = createInstance();
						assertRestrictedSetter(instance,
								setter,
								resolveParameter(instance, paramClass),
								resolveParameter(instance, paramClass),
								!allowNullParam,
								(executable, msg) -> assertThrows(Throwable.class, executable, msg));
					});
		} else {
			setterTest = dynamicTest("setter",
					() -> {
						T instance = createInstance();
//						System.out.printf("'%s', nullable=%b%n", config.setter, allowNullParam);

						// Special case for boolean, as we know possible parameter values
						if(Boolean.class.equals(paramClass) || Boolean.TYPE.equals(paramClass)) {
							assertSetter(instance,
									(x, b) -> setter.accept(x, b));
						} else {
							assertSetter(instance, setter,
									resolveParameter(instance, paramClass),
									!allowNullParam, NO_CHECK);
						}
					});
		}

		return dynamicContainer(
				displayName(config),
				null,
				Stream.of(getterTest, setterTest));
	}

	/**
	 * Creates a blank instance of the class under test.
	 * @return
	 */
	private T createInstance() {
		return creator.get();
	}

	//TODO unwrap return type from Optional
	private Class<?> unwrapReturnType(Method method) {
		Type returnType = method.getGenericReturnType();
		Class<?> expectedClass = method.getReturnType();

		if(returnType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) returnType;
			if(parameterizedType.getRawType()==Optional.class) {
				Type paramType = parameterizedType.getActualTypeArguments()[0];
				if(paramType instanceof Class) {
					expectedClass = (Class<?>) paramType;
				}
			}
		}

		return expectedClass;
	}

	/**
	 * Check all stored mappings and in case of any mismatches or rule
	 * violations remove the faulty entry and report it.
	 */
	private void validatePropertyMappings() {
		for(Iterator<PropertyConfig> it = propertyMethods.iterator(); it.hasNext();) {
			PropertyConfig config = it.next();

			// Make sure getter and setter use compatible types
			Class<?> getterResult = unwrapReturnType(config.getter);
			Class<?> setterParam = config.setter.getParameterTypes()[0];

			// In case of mismatch report it and remove mapping from buffer
			if(!setterParam.isAssignableFrom(getterResult)) {
				it.remove();
				testReporter.publishEntry("methodMismatch",
						String.format("Incompatible types defined in method signatures for "
								+ "property %s: [getter: %s] [setter: %s]",
								config.property, config.getter, config.setter));
			}
		}
	}

	private <V extends Object, A extends Annotation> V extractConsistentValue(
			Class<A> annotationClass, MethodCache methodCache,
			Function<A, V> getter) {
		V result = null;

		for(Method m : methodCache.getMethodsForAnnotation(annotationClass)) {
			A annotation = m.getAnnotation(annotationClass);
			if(annotation!=null) {
				V value = getter.apply(annotation);
				if(result==null) {
					result = value;
				} else if(!value.equals(result)) {
					testReporter.publishEntry("inconsistentAnnotation", String.format(
							"Annotation '%s' on method '%s' returns value inconsistent with "
							+ "overridden methods: expected %s - got %s",
							annotationClass, m.toGenericString(), result, value));
				}
			}
		}

		return result;
	}

	/**
	 * Try to find all the possible mappings of getters and setters in the target class.
	 *
	 * @param methods
	 */
	//TODO rework strategy so that we first grab all the getters and try to search for matching setters (by signature)
	private void collectPropertyMethods() {
		Map<String, Method> getters = new HashMap<>();
		Map<String, Method> setters = new HashMap<>();

		BiConsumer<String, Method> mapGetter = (property, method) -> {
			if(getters.containsKey(property)) {
				testReporter.publishEntry("duplicate getter",
						String.format("Duplicate getter method for property '%s': '%s' vs. '%s'",
								property, method.toGenericString(), getters.get(property).toGenericString()));
			} else {
				getters.put(property, method);

//				System.out.printf("Adding getter for '%s': %s%n", property, method);
			}
		};

		BiConsumer<String, Method> mapSetter = (property, method) -> {
			if(setters.containsKey(property)) {
				testReporter.publishEntry("duplicate setter",
						String.format("Duplicate setter method for property '%s': '%s' vs. '%s'",
								property, method.toGenericString(), setters.get(property).toGenericString()));
			} else {
				setters.put(property, method);

//				System.out.printf("Adding setter for '%s': %s%n", property, method);
			}
		};

		TriConsumer<MethodType, String, Method> mapByType = (type, property, method) -> {
			switch (type) {
			case GETTER:
				mapGetter.accept(property, method);
				break;

			case SETTER:
				mapSetter.accept(property, method);
				break;

			default:
				throw new TestAbortedException("Not a recognized setter/getter method: "+method);
			}
		};

//		System.out.println("Methods: "+classCache.getMethods());

		for(MethodCache methodCache : classCache.getMethodCaches()) {

			// Ignore all methods specifically marked to be unguarded
			if(methodCache.hasAnnotation(Unguarded.class)) {
				continue;
			}

			Method method = methodCache.getMethod();

			// Complex: need to extract type and possibly property name
			if(methodCache.hasAnnotation(Guarded.class)) {
				// Fetch method type
				MethodType type = extractConsistentValue(Guarded.class,
						methodCache, Guarded::methodType);

				if(type==null || type==MethodType.AUTO_DETECT) {
					type = getMethodType(method);
				}

				// Fetch property name
				String property = extractConsistentValue(Guarded.class,
						methodCache, Guarded::name);

				if(property.isEmpty()) {
					property = extractPropertyName(method, type);
				}

				mapByType.accept(type, property, method);
				continue;
			}

			// Experimental: if desired try to find potential getters and setters
			if(detectUnmarkedMethods) {
				MethodType type = getMethodType(method);
				if(type!=null) {
					String property = extractPropertyName(method, type);
					mapByType.accept(type, property, method);
				}
				continue;
			}
		}

		for(Map.Entry<String, Method> eSetter : setters.entrySet()) {
			String property = eSetter.getKey();
			Method setter = eSetter.getValue();
			Method getter = getters.remove(property);

			// Report all missing getters
			if(getter==null) {
				testReporter.publishEntry("missingGetter",
						String.format("Missing getter for property %s - present setter: %s",
								property, setter));
				continue;
			}

			propertyMethods.add(new PropertyConfig(property, setter, getter));
		}

		// Report all leftover unassigned setters
		for(Map.Entry<String, Method> eGetter : getters.entrySet()) {
			testReporter.publishEntry("missingSetter",
					String.format("Missing setter for property %s - present getter: %s",
							eGetter.getKey(), eGetter.getValue()));
		}
	}

	private String extractPropertyName(Method method, MethodType type) {
		String name = method.getName();
		int begin = type==MethodType.GETTER ? "get".length() : "set".length();
		if(name.startsWith("is")) {
			begin = "is".length();
		}
		return Character.toLowerCase(name.charAt(begin))
				+name.substring(begin+1);
	}

	private static final Map<String, MethodType> prefixToTypeMap = new HashMap<>();
	static {
		prefixToTypeMap.put("is", MethodType.GETTER);
		prefixToTypeMap.put("get", MethodType.GETTER);
		prefixToTypeMap.put("set", MethodType.SETTER);
	}

	/**
	 * Important note: we only consider methods of the name {@code <prefix>Guarded}
	 * where the property part (the non-prefix {@code Guarded} substring) is at least
	 * three characters long!
	 *
	 * @param method
	 * @return
	 */
	private MethodType getMethodType(Method method) {
		String name = method.getName();

		for(Map.Entry<String, MethodType> e : prefixToTypeMap.entrySet()) {
			String prefix = e.getKey();
			MethodType type = e.getValue();

			if(name.startsWith(prefix)
					&& name.length()>prefix.length()+2
					&& Character.isUpperCase(name.charAt(prefix.length()))) {

				boolean isGetter = type==MethodType.GETTER;

				// Consistency checks for mislabeled methods
				if(isGetter && method.getReturnType()==void.class) {
					testReporter.publishEntry("inconsistentGetter",
							"Getter method does not define return type: "+method);
				} else if(isGetter && method.getParameterCount()==1) {
					testReporter.publishEntry("inconsistentGetter",
							"Getter method expects arguments: "+method);
				} else if(!isGetter && method.getParameterCount()==0) {
					testReporter.publishEntry("inconsistentSetter",
							"Setter method does not take arguments: "+method);
				} else if(!isGetter && method.getReturnType()!=void.class
						&& !method.getReturnType().isAssignableFrom(targetClass)) {
					testReporter.publishEntry("inconsistentSetter",
							"Setter method declares return (non-chainable) type: "+method);
				} else {
					return type;
				}
			}
		}

		return null;
	}

	static class PropertyConfig {
		final String property;
		final Method getter, setter;

		public PropertyConfig(String property, Method setter, Method getter) {
			this.property = requireNonNull(property);
			this.setter = requireNonNull(setter);
			this.getter = requireNonNull(getter);
		}
	}
}
