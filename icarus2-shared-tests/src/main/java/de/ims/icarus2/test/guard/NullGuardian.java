/**
 *
 */
package de.ims.icarus2.test.guard;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestReporter;

import de.ims.icarus2.apiguard.Unguarded;
import de.ims.icarus2.test.reflect.ClassCache;
import de.ims.icarus2.test.reflect.MethodCache;
import de.ims.icarus2.test.reflect.RefUtils;

/**
 * @author Markus Gärtner
 *
 */
public class NullGuardian<T> extends Guardian<T> {

	private final Class<T> targetClass;

	private final ClassCache<T> classCache;

	private Supplier<? extends T> creator;

	public static final Predicate<? super Method> METHOD_FILTER = (m) -> {
		boolean isStatic = Modifier.isStatic(m.getModifiers()); // must not be static
		boolean isPublic = Modifier.isPublic(m.getModifiers());  // must be public
		boolean isObjMethod = m.getDeclaringClass()==Object.class; // ignore all original Object methods
		boolean hasNoParams = m.getParameterCount()==0;

		// Early filtering of unfit methods before we access the parameter array
		if(isStatic || !isPublic || isObjMethod || hasNoParams) {
			return false;
		}

		// Check if we have at least 1 non.primitive parameter
		for(Class<?> paramClass : m.getParameterTypes()) {
			if(!paramClass.isPrimitive()) {
				return true;
			}
		}

		// All parameters are primitives
		return false;
	};

	/**
	 * @param apiGuard
	 */
	public NullGuardian(ApiGuard<T> apiGuard) {
		super(apiGuard);

		targetClass = apiGuard.getTargetClass();
		creator = apiGuard.instanceCreator();

		classCache = ClassCache.<T>newBuilder()
				.targetClass(targetClass)
				.methodFilter(METHOD_FILTER)
//				.log(System.out::println)
				.build();
	}

	/**
	 * @see de.ims.icarus2.test.guard.Guardian#createTests(org.junit.jupiter.api.TestReporter)
	 */
	@Override
	DynamicNode createTests(TestReporter testReporter) {
		Collection<MethodCache> methodCaches = classCache.getMethodCaches();
		List<DynamicNode> tests = methodCaches.stream()
				.filter(c -> !c.hasAnnotation(Unguarded.class))
				.map(this::createTestsForMethod)
				.collect(Collectors.toCollection(ArrayList::new));
		String displayName = String.format("Parameter-Guard [%d/%d]",
				Integer.valueOf(tests.size()),
				Integer.valueOf(methodCaches.size()));

		return dynamicContainer(displayName, tests);
	}

	private DynamicNode createTestsForMethod(MethodCache methodCache) {
		Method method = methodCache.getMethod();
		Collection<ParamConfig> variations = variateNullParameter(null, method);
		String baseLabel = RefUtils.toSimpleString(method);

		if(variations.isEmpty()) {
			return dynamicContainer(baseLabel+" - no null-guarded arguments", Collections.emptyList());
		}

		return dynamicContainer(
				baseLabel+" ["+variations.size()+" null-guarded arguments]",
				sourceUriFor(method),
				variations.stream().map(config ->
					createNullTest(config, args -> {
						T instance = creator.get();
						method.invoke(instance, args);
					}))) ;
	}

}
