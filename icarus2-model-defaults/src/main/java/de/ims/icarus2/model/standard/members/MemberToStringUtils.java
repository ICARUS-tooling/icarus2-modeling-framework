/**
 *
 */
package de.ims.icarus2.model.standard.members;

import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;

import javax.annotation.Nullable;

import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * Holds utility methods for the string representation of {@link Item}s and
 * derived classes.
 *
 * @author Markus Gärtner
 *
 */
public class MemberToStringUtils {

//	public static String toString(CorpusMember m) {
//		MemberType type = m.getMemberType();
//
//		if(type==MemberType.LAYER) {
//			Layer layer = (Layer)m;
//			return "[Layer: "+layer.getName()+"]";
//		}
//
//		Item item = (Item)m;
//		Layer layer = item.getLayer();
//		long index = item.getIndex();
//		return "["+layer.getName()+"_"+getTypePrefix(type)+"_"+index+"<"+_index(item.getBeginOffset())+"-"+_index(item.getEndOffset())+">]";
//	}

	private static String _index(long index) {
		return index==UNSET_LONG ? "?" : String.valueOf(index);
	}

	private static char getTypePrefix(MemberType type) {
		switch (type) {
		case ITEM:
			return 'I';
		case FRAGMENT:
			return 'F';
		case CONTAINER:
			return 'C';
		case STRUCTURE:
			return 'S';
		case LAYER:
			return 'L';
		case EDGE:
			return 'E';

		default:
			throw new IllegalArgumentException();
		}
	}

	public static String detachedToString(Item item) {
		return toString(item, null, UNSET_LONG, UNSET_LONG, UNSET_LONG);
	}

	public static String toString(Item item, @Nullable ItemLayer layer, long index, long begin, long end) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if(layer!=null) {
			sb.append(layer.getName()).append('_');
		}
		sb.append(getTypePrefix(item.getMemberType())).append('_').append(_index(index));
		sb.append('<').append(_index(begin)).append('_').append(_index(end)).append('>');

		sb.append(']');
		return sb.toString();
	}
}
