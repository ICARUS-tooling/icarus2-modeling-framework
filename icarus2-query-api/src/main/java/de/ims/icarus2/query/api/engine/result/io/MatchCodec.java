/**
 *
 */
package de.ims.icarus2.query.api.engine.result.io;

import java.io.IOException;
import java.util.function.Consumer;

import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * @author Markus Gärtner
 *
 */
public interface MatchCodec {

	MatchWriter newWriter(IOResource resource) throws IOException;
	MatchReader newReader(IOResource resource) throws IOException;

	public interface MatchWriter extends AutoCloseable {
		void write(Match match) throws IOException;
		@Override
		void close() throws IOException;
	}

	public interface MatchReader extends AutoCloseable {

		/** Tries to probe for another stored match. This might also fully read and cache the next match. */
		boolean next() throws IOException;

		/** Reads the next match or returns the cached match form the latest call to {@link #next()} */
		Match read() throws IOException;

		void readAll(Consumer<? super Match> action) throws IOException;

		@Override
		void close() throws IOException;
	}
}
