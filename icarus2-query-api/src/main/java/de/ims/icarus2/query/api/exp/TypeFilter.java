/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.exp;

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

	public static final TypeFilter INTEGER = TypeInfo::isInteger;
	public static final TypeFilter FLOATING_POINT = TypeInfo::isFloatingPoint;
	public static final TypeFilter BOOLEAN = TypeInfo::isBoolean;
	public static final TypeFilter TEXT = TypeInfo::isText;

	public static TypeFilter noneOf(TypeInfo...infos) {
		final Set<TypeInfo> filter = set(infos);
		return info -> !filter.contains(info);
	}

	public static TypeFilter anyOf(TypeInfo...infos) {
		final Set<TypeInfo> filter = set(infos);
		return info -> filter.contains(info);
	}

	public static TypeFilter exactly(TypeInfo exact) {
		return info -> exact.equals(info);
	}

	//TODO make utility classes to filter individuals or batches
}
