/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
