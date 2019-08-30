/**
 *
 */
package de.ims.icarus2.model.api.edit.io;

import java.io.IOException;

import de.ims.icarus2.model.api.edit.change.AtomicChangeType;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.util.collections.seq.ArraySequence;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface ChangeReader {

	default SerializableAtomicChange readChange() throws IOException {
		AtomicChangeType type = AtomicChangeType.parseAtomicChangeType(readString());
		SerializableAtomicChange change = SerializableAtomicModelChange.forType(type);
		readDefaultHeader(change);
		change.readChange(this);
		return change;
	}

	default void readDefaultHeader(SerializableAtomicChange change) throws IOException {
		// no-op
	}

	<M extends CorpusMember> M readMember(MemberType type) throws IOException;

	default <M extends CorpusMember> DataSequence<M> readSequence(MemberType type) throws IOException {
		int size = readInt();
		@SuppressWarnings("unchecked")
		M[] items = (M[]) new CorpusMember[size];
		for (int i = 0; i < items.length; i++) {
			items[i] = readMember(type);
		}
		return new ArraySequence<>(items);
	}

	long readLong() throws IOException;

	float readFloat() throws IOException;

	double readDouble() throws IOException;

	int readInt() throws IOException;

	boolean readBoolean() throws IOException;

	String readString() throws IOException;

	Object readValue() throws IOException;

	Position readPosition() throws IOException;
}
