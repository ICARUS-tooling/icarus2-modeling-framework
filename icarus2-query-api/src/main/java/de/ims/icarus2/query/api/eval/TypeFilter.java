/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static de.ims.icarus2.util.collections.CollectionUtils.set;

import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface TypeFilter extends Predicate<TypeInfo> {

	public static final TypeFilter NONE = info -> false;
	public static final TypeFilter ALL = info -> true;

	public static final TypeFilter PRIMITIVES = info -> info.isPrimitive();
	public static final TypeFilter MEMBERS = info -> info.isMember();
	public static final TypeFilter LISTS = info -> info.isList();

	public static TypeFilter noneOf(TypeInfo...infos) {
		final Set<TypeInfo> filter = set(infos);
		return info -> !filter.contains(info);
	}

	public static TypeFilter anyOf(TypeInfo...infos) {
		final Set<TypeInfo> filter = set(infos);
		return info -> filter.contains(info);
	}

	public static TypeFilter exactly(TypeInfo exact) {
		return info -> info==exact;
	}

	//TODO make utility classes to filter individuals or batches
}
