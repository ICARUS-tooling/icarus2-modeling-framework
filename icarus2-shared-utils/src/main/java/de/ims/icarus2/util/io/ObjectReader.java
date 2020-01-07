/**
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
package de.ims.icarus2.util.io;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface ObjectReader<E extends Object> extends AutoCloseable {

	void init(Reader input, Options options);

	boolean hasMoreData() throws IOException, InterruptedException;

	E read() throws IOException, InterruptedException;

	default int readAll(Consumer<? super E> action) throws IOException, InterruptedException {
		int counter = 0;
		while(hasMoreData()) {
			action.accept(read());
		}
		return counter;
	}

	default List<E> readAll() throws IOException, InterruptedException {
		LazyCollection<E> buffer = LazyCollection.lazyList();
		readAll(buffer);
		return buffer.getAsList();
	}

	@Override
	void close() throws IOException;
}
