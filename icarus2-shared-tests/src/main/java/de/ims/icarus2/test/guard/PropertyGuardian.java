/**
 *
 */
package de.ims.icarus2.test.guard;

import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import de.ims.icarus2.test.func.TriConsumer;
import de.ims.icarus2.test.reflect.ClassCache;

/**
 * @author Markus Gärtner
 *
 */
class PropertyGuardian<T> extends Guardian {

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
	private boolean allowUnmarkedMethods = false;

	private TestReporter testReporter;

	private Supplier<? extends T> creator;

	private static final Predicate<? super Method> PROPERTY_FILTER =
			(m) -> m.getParameterCount()==1
				|| (m.getParameterCount()==0 && m.getReturnType()!=void.class);

	public PropertyGuardian(Class<T> targetClass, Supplier<? extends T> creator) {
		this.targetClass = requireNonNull(targetClass);
		this.creator = requireNonNull(creator);

		classCache = ClassCache.<T>newBuilder()
				.targetClass(targetClass)
				.methodFilter(PROPERTY_FILTER)
				.build();
	}

	public PropertyGuardian allowUnmarkedMethods(boolean value) {
		this.allowUnmarkedMethods = value;
		return this;
	}

	/**
	 * @see de.ims.icarus2.test.guard.Guardian#createTests(org.junit.jupiter.api.TestReporter)
	 */
	@Override
	DynamicNode createTests(TestReporter testReporter) {
		this.testReporter = requireNonNull(testReporter);

		List<Method> methods = Arrays.asList(targetClass.getMethods());

		//TODO maybe allow pre-filtering?

		// Detect all the property methods
		collectPropertyMethods(classCache.getMethods());

		// Make sure we only keep the "correct" ones
		validatePropertyMappings();

		// Turn method mapping into tests
		return dynamicContainer("Properties",
				propertyMethods.stream()
				.map(this::createTestsForProperty)
				.collect(Collectors.toList()));
	}

	private DynamicNode createTestsForProperty(PropertyConfig config) {
		boolean allowNullParam = config.setter.getAnnotatedParameterTypes()[0]
				.isAnnotationPresent(Nullable.class);
		boolean allowNullReturn = config.getter.getAnnotatedReturnType()
				.isAnnotationPresent(Nullable.class);

		Class<?> paramClass = config.setter.getParameterTypes()[0];

		BiConsumer<T, Object> setter = (instance, value) -> {
			try {
				config.setter.invoke(instance, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new TestAbortedException("Failed to invoke setter", e);
			}
		};

		Function<T, Object> getter = (instance) -> {
			try {
				return config.getter.invoke(instance);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new TestAbortedException("Failed to invoke getter", e);
			}
		};

		DynamicTest getterTest = dynamicTest("getter",
				() -> assertGetter(createInstance(),
						resolveParameter(paramClass),
						resolveParameter(paramClass),
						NO_DEFAULT(),
						getter,
						setter));

		DynamicTest setterTest = dynamicTest("setter",
				() -> {
					if(Boolean.class.equals(paramClass) || Boolean.TYPE.equals(paramClass)) {
						assertSetter(createInstance(), (instance, b) -> setter.accept(instance, b));
					} else {
						assertSetter(createInstance(), setter, resolveParameter(paramClass), !allowNullParam, NO_CHECK);
					}
				});

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

	/**
	 * Check all stored mappings and in case of any mismatches or rule
	 * violations remove the faulty entry and report it.
	 */
	private void validatePropertyMappings() {
		for(Iterator<PropertyConfig> it = propertyMethods.iterator(); it.hasNext();) {
			PropertyConfig config = it.next();

			// Make sure getter and setter use compatible types
			Class<?> getterResult = config.getter.getReturnType();
			Class<?> setterParam = config.setter.getParameterTypes()[0];

			// In case of mismatch report it and remove mapping from buffer
			if(!setterParam.isAssignableFrom(getterResult)) {
				it.remove();
				testReporter.publishEntry("method mismatch",
						String.format("Incompatible types defined in method signatures for "
								+ "property %s: [getter: %s] [setter: %s]",
								config.property, config.getter, config.setter));
			}
		}
	}

	/**
	 * Try to find all the possible mappings of getters and setters in the target class.
	 *
	 * @param methods
	 */
	//TODO rework strategy so that we first grab all the getters and try to search for matching setters (by signature)
	private void collectPropertyMethods(Set<Method> methods) {
		Map<String, Method> getters = new HashMap<>();
		Map<String, Method> setters = new HashMap<>();

		BiConsumer<String, Method> mapGetter = (property, method) -> {
			if(getters.containsKey(property)) {
				testReporter.publishEntry("duplicate getter",
						String.format("Duplicate getter method for property %s: %s",
								property, method.getName()));
			} else {
				getters.put(property, method);
			}
		};

		BiConsumer<String, Method> mapSetter = (property, method) -> {
			if(setters.containsKey(property)) {
				testReporter.publishEntry("duplicate setter",
						String.format("Duplicate setter method for property %s: %s",
								property, method.getName()));
			} else {
				setters.put(property, method);
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

		for(Method method : methods) {
			// Complex: need to extract type and possibly property name
			Property aProperty = ApiGuard.getInheritedAnnotation(Property.class, method);
			if(aProperty!=null) {
				String property = aProperty.value();
				MethodType type = getMethodType(method);

				if(property.isEmpty()) {
					property = extractPropertyName(method, type);
				}

				mapByType.accept(type, property, method);
				continue;
			}

			// Easy: directly grab the getter's designated property and map
			Getter aGetter = ApiGuard.getInheritedAnnotation(Getter.class, method);
			if(aGetter!=null) {
				mapGetter.accept(aGetter.value(), method);
				continue;
			}

			// Easy: directly grab the setter's designated property and map
			Setter aSetter = ApiGuard.getInheritedAnnotation(Setter.class, method);
			if(aSetter!=null) {
				mapSetter.accept(aSetter.value(), method);
				continue;
			}

			// Experimental: if desired try to find potential getters and setters
			if(allowUnmarkedMethods) {
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
				testReporter.publishEntry("missing getter",
						String.format("Missing getter for property %s - present setter: %s",
								property, setter));
			}

			propertyMethods.add(new PropertyConfig(property, setter, getter));
		}

		// Report all leftover unassigned setters
		for(Map.Entry<String, Method> eGetter : getters.entrySet()) {
			testReporter.publishEntry("missing setter",
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
					testReporter.publishEntry("inconsistent getter",
							"Getter method does not define return type: "+method);
				} else if(type.isGetter && method.getParameterCount()==1) {
					testReporter.publishEntry("inconsistent getter",
							"Getter method expects arguments: "+method);
				} else if(!type.isGetter && method.getParameterCount()==0) {
					testReporter.publishEntry("inconsistent setter",
							"Setter method does not take arguments: "+method);
				} else if(!type.isGetter && method.getReturnType()!=void.class) {
					testReporter.publishEntry("inconsistent setter",
							"Setter method declares return type: "+method);
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
