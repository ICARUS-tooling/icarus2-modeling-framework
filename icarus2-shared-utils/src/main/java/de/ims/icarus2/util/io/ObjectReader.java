/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
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
