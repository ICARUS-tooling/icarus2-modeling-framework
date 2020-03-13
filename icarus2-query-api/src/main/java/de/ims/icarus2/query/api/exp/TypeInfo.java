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
/**
 *
 */
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.lang.Primitives._boolean;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.layer.StructureLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public class TypeInfo {

	public static TypeInfo of(Class<?> type, boolean list) {
		return new TypeInfo(type, null, false, list);
	}

	public static TypeInfo of(Class<?> type) {
		if(Primitive.class.isAssignableFrom(type))
			throw new QueryException(GlobalErrorCode.INVALID_INPUT,
					"Cannot disambiguate the exact primitive type, use TypeInfo.XXX constants");

		if(type==String.class) {
			return STRING;
		} else if(type==Object.class) {
			return GENERIC;
		}

		boolean isMember = Item.class.isAssignableFrom(type);
		//TODO do we want to determine 'isList' flag here as well?

		return new TypeInfo(type, null, isMember, false);
	}

	private final Class<?> type;
	private final Class<?> primitiveType;

	private final boolean member, list;

	private TypeInfo(Class<?> type, Class<?> primitiveType, boolean member, boolean list) {
		this.type = requireNonNull(type);
		this.primitiveType = primitiveType;
		this.member = member;
		this.list = list;
	}

	@Override
	public String toString() {
		if(primitiveType!=null) {
			return primitiveType.getSimpleName();
		}

		return type.getSimpleName();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof TypeInfo) {
			TypeInfo other = (TypeInfo)obj;
			return type.equals(other.type)
					&& Objects.equals(primitiveType, other.primitiveType)
					&& member==other.member
					&& list==other.list;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, primitiveType, _boolean(member), _boolean(list));
	}

	public Class<?> getType() { return type; }

	public Class<?> getPrimitiveType() { return primitiveType; }

	public boolean isPrimitive() { return primitiveType!=null; }

	/** Returns {@code true} if this type describes a corpus member. */
	public boolean isMember() { return member; }

	/** Returns {@code true} if this type describes a structure that allows index-based access. */
	public boolean isList() { return list; }

	/**
	 * Returns {@code true} if this info's {@link #getType() type} is
	 * {@link Class#isAssignableFrom(Class) assignable} from the {@code other}'s type.
	 * <p>
	 * For {@link #isPrimitive() primitive} class compatibility is checked via equality
	 * of the primitive type.
	 *
	 * @param other
	 * @return
	 */
	public boolean isCompatible(TypeInfo other) {
		return isPrimitive() ? primitiveType==other.primitiveType : type.isAssignableFrom(other.type);
	}

	public static boolean isNumerical(TypeInfo info) {
		return info==INTEGER || info==FLOATING_POINT;
	}

	public static boolean isFloatingPoint(TypeInfo info) {
		return info==FLOATING_POINT;
	}

	public static boolean isInteger(TypeInfo info) {
		return info==INTEGER;
	}

	public static boolean isBoolean(TypeInfo info) {
		return info==BOOLEAN;
	}

	public static boolean isText(TypeInfo info) {
		// We can't simply check info==TEXT, as more types might implement CharSequence
		return CharSequence.class.isAssignableFrom(info.type);
	}

	public static boolean isComparable(TypeInfo info) {
		return Comparable.class.isAssignableFrom(info.type);
	}

	// Dummy types
	/** Wraps all inherent {@code null} types. */
	public static final TypeInfo NULL = new TypeInfo(Object.class, null, false, false);
	/** Placeholder for general {@code unknown} non-primitive types. */
	public static final TypeInfo GENERIC = of(Object.class, false);

	// Primitives
	/** Represents all primitive integer types, up to {@code long}. */
	public static final TypeInfo INTEGER = new TypeInfo(Primitive.class, long.class, false, false);
	/** Represents all primitive floating point types, up to {@code double}. */
	public static final TypeInfo FLOATING_POINT = new TypeInfo(Primitive.class, double.class, false, false);
	/** Represents the primitive type {@code boolean}. */
	public static final TypeInfo BOOLEAN = new TypeInfo(Primitive.class, boolean.class, false, false);

	// Former "String" proxy, now adjusted for flexibility
	/** We use {@code CharSequence}, as defined by {@link ValueType#STRING} */
	public static final TypeInfo TEXT = new TypeInfo(CharSequence.class, null, false, false);
	/** String offers both CharSequence and Comparable features, so warrants an extra type */
	//TODO unify with TEXT, once we added Comparator ops for TEXT in BinaryOperations
	public static final TypeInfo STRING = new TypeInfo(String.class, null, false, false);

	// Low-level members
	public static final TypeInfo ITEM = new TypeInfo(Item.class, null, true, false);
	public static final TypeInfo EDGE = new TypeInfo(Edge.class, null, true, false);
	public static final TypeInfo FRAGMENT = new TypeInfo(Fragment.class, null, true, false);
	public static final TypeInfo CONTAINER = new TypeInfo(Container.class, null, true, true);
	public static final TypeInfo STRUCTURE = new TypeInfo(Structure.class, null, true, true);

	// High-level members
	public static final TypeInfo CORPUS = new TypeInfo(Corpus.class, null, true, false);
	public static final TypeInfo CONTEXT = new TypeInfo(Context.class, null, true, false);
	public static final TypeInfo LAYER_GROUP = new TypeInfo(LayerGroup.class, null, true, false);
	public static final TypeInfo LAYER = new TypeInfo(Layer.class, null, true, false);
	public static final TypeInfo ITEM_LAYER = new TypeInfo(ItemLayer.class, null, true, false);
	public static final TypeInfo STRUCTURE_LAYER = new TypeInfo(StructureLayer.class, null, true, false);
	public static final TypeInfo FRAGMENT_LAYER = new TypeInfo(FragmentLayer.class, null, true, false);
	public static final TypeInfo ANNOTATION_LAYER = new TypeInfo(AnnotationLayer.class, null, true, false);

	// Frequently used helpers
	public static final TypeInfo LIST = new TypeInfo(List.class, null, false, true);
	public static final TypeInfo ARRAY = new TypeInfo(Object[].class, null, false, true);

	// Special helper type for embedded data chunks
	public static final TypeInfo BINARY = new TypeInfo(byte[].class, null, false, false);

	private static final Set<TypeInfo> coreTypes = set(TEXT, INTEGER, FLOATING_POINT, BOOLEAN);

	public static boolean isCoreType(TypeInfo type) {
		return coreTypes.contains(requireNonNull(type));
	}
}
