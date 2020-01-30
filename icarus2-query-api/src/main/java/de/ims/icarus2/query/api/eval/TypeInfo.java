/**
 *
 */
package de.ims.icarus2.query.api.eval;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
public class TypeInfo {

	public TypeInfo of(Class<?> type, boolean list) {
		return new TypeInfo(type, null, false, list);
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

	public Class<?> getType() { return type; }

	public Class<?> getPrimitiveType() { return primitiveType; }

	public boolean isPrimitive() { return primitiveType!=null; }

	/** Returns {@code true} if this type describes a corpus member. */
	public boolean isMember() { return member; }

	/** Returns {@code true} if this type describes a structure that allows index-based access */
	public boolean isList() { return list; }

	public static final TypeInfo NULL = new TypeInfo(Object.class, null, false, false);

//	public static final TypeInfo INT = new TypeInfo(Integer.class, int.class, false, false);
	public static final TypeInfo LONG = new TypeInfo(Long.class, long.class, false, false);
//	public static final TypeInfo FLOAT = new TypeInfo(Float.class, float.class, false, false);
	public static final TypeInfo DOUBLE = new TypeInfo(Double.class, double.class, false, false);

	public static boolean isNumerical(TypeInfo info) {
		return info==LONG || info==DOUBLE;
	}

	public static final TypeInfo BOOLEAN = new TypeInfo(Boolean.class, boolean.class, false, false);

	/** We use {@link CharSequence} as type for strings, in accordance with {@link ValueType#STRING} */
	public static final TypeInfo STRING = new TypeInfo(CharSequence.class, null, false, false);

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
}
