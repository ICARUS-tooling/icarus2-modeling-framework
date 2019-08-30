/**
 *
 */
package de.ims.icarus2.model.api.edit.io;

import static de.ims.icarus2.util.IcarusUtils.ensureIntegerValueRange;

import java.io.IOException;
import java.util.Iterator;

import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface ChangeWriter {

	default void writeChange(SerializableAtomicChange change) throws IOException {
		writeString(change.getType().getStringValue());
		writeDefaultHeader(change);
		change.writeChange(this);
	}

	default void writeDefaultHeader(SerializableAtomicChange change) throws IOException {
		// no-op
	}

	void writeMember(CorpusMember member) throws IOException;

	default void writeSequence(DataSequence<? extends CorpusMember> list) throws IOException {
		writeInt(ensureIntegerValueRange(list.entryCount()));
		for(Iterator<? extends CorpusMember> it = list.iterator(); it.hasNext();) {
			writeMember(it.next());
		}
	}

	void writeLong(long value) throws IOException;

	void writeFloat(float value) throws IOException;

	void writeDouble(double value) throws IOException;

	void writeInt(int value) throws IOException;

	void writeBoolean(boolean b) throws IOException;

	void writeString(String s) throws IOException;

	void writeValue(ValueType type, Object value) throws IOException;

	void writePosition(Position position) throws IOException;
}
