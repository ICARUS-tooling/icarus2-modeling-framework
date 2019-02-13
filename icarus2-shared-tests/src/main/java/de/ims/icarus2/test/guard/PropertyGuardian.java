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

import de.ims.icarus2.apiguard.Getter;
import de.ims.icarus2.apiguard.Property;
import de.ims.icarus2.apiguard.Setter;
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

//				System.out.printf("stat=%b, pub=%b, obj=%b, params=%b, single=%b, void=%b: %s%n",
//						isStatic, isPublic, isObjMethod, hasParams, hasSingleParam, isVoidReturn, m);

				return !isStatic && isPublic && !isObjMethod &&
						(hasSingleParam || (!hasParams && !isVoidReturn));
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

		// Turn method mapping into tests
		return dynamicContainer("Properties",
				propertyMethods.stream()
				.map(this::createTestsForProperty)
				.collect(Collectors.toList()));
	}

	private Object tryParseDefaultGetterValue(MethodCache methodCache) {
		String defaultValue = extractConsistentValue(Getter.class, methodCache, Getter::defaultValue);
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
				throw new IllegalArgumentException("Unable to parse defautl value for return type: "+returnType);
			}
		}

		return null;
	}

	private Supplier<Object> createDefault(MethodCache methodCache, boolean allowNullReturn) {
		Class<?> returnType = methodCache.getMethod().getReturnType();
		Object defaultValue = null;

		if(returnType.isPrimitive()) {
			defaultValue = tryParseDefaultGetterValue(methodCache);

			if(defaultValue==null) {
				defaultValue = getDefaultValue(returnType);
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

	@SuppressWarnings("unchecked")
	private DynamicNode createTestsForProperty(PropertyConfig config) {
		MethodCache getterMethodCache = classCache.getMethodCache(config.getter);
		MethodCache setterMethodCache = classCache.getMethodCache(config.setter);

		boolean allowNullParam = setterMethodCache.hasParameterAnnotation(Nullable.class);
		// Nullability of return value can be defined in two ways
		boolean allowNullReturn = getterMethodCache.hasAnnotation(Nullable.class)
				|| getterMethodCache.hasResultAnnotation(Nullable.class);

		// Need to handle getter methods that wrap values into an Optional
		boolean isOptionalReturn = config.getter.getReturnType()==Optional.class;

		boolean isRestrictedSetter = setterMethodCache.hasAnnotation(Setter.class)
				&& extractConsistentValue(Setter.class, setterMethodCache, Setter::restricted).booleanValue();

		final Supplier<Object> DEFAULT = createDefault(getterMethodCache, allowNullReturn);

		final Class<?> paramClass = config.setter.getParameterTypes()[0];

		BiConsumer<T, Object> setter = (instance, value) -> {
			try {
				config.setter.invoke(instance, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				if(e.getCause() instanceof RuntimeException)
					throw (RuntimeException) e.getCause();

				throw new TestAbortedException("Failed to invoke setter", e);
			}
		};

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
				config.property,
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
			case BOOLEAN_GETTER:
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
			if(methodCache.hasAnnotation(Property.class)) {
				String property = extractConsistentValue(Property.class,
						methodCache, Property::value);
				MethodType type = getMethodType(method);

				if(property.isEmpty()) {
					property = extractPropertyName(method, type);
				}

				mapByType.accept(type, property, method);
				continue;
			}

			// Easy: directly grab the getter's designated property and map
			if(methodCache.hasAnnotation(Getter.class)) {
				String property = extractConsistentValue(Getter.class,
						methodCache, Getter::value);
				mapGetter.accept(property, method);
				continue;
			}

			// Easy: directly grab the setter's designated property and map
			if(methodCache.hasAnnotation(Setter.class)) {
				String property = extractConsistentValue(Setter.class,
						methodCache, Setter::value);
				mapSetter.accept(property, method);
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
		int begin = type.prefix.length();
		return Character.toLowerCase(name.charAt(begin))
				+name.substring(begin+1);
	}

	/**
	 * Important note: we only consider methods of the name {@code <prefix>Property}
	 * where the property part (the non-prefix {@code Property} substring) is at least
	 * three characters long!
	 *
	 * @param method
	 * @return
	 */
	private MethodType getMethodType(Method method) {
		String name = method.getName();
		for(MethodType type : MethodType.values) {
			if(name.startsWith(type.prefix)
					&& name.length()>type.prefix.length()+2
					&& Character.isUpperCase(name.charAt(type.prefix.length()))) {

				// Consistency checks for mislabeled methods
				if(type.isGetter && method.getReturnType()==void.class) {
					testReporter.publishEntry("inconsistentGetter",
							"Getter method does not define return type: "+method);
				} else if(type.isGetter && method.getParameterCount()==1) {
					testReporter.publishEntry("inconsistentGetter",
							"Getter method expects arguments: "+method);
				} else if(!type.isGetter && method.getParameterCount()==0) {
					testReporter.publishEntry("inconsistentSetter",
							"Setter method does not take arguments: "+method);
				} else if(!type.isGetter && method.getReturnType()!=void.class
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

	static enum MethodType {
		SETTER("set", false),
		GETTER("get", true),
		BOOLEAN_GETTER("is", true),
		;

		private MethodType(String prefix, boolean isGetter) {
			this.prefix = prefix;
			this.isGetter = isGetter;
		}

		public final String prefix;
		public final boolean isGetter;

		static MethodType[] values = values();
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
