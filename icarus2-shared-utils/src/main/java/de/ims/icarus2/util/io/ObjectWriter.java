/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
import java.io.Writer;
import java.util.function.Supplier;

import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public interface ObjectWriter<E extends Object> extends AutoCloseable {

	/**
	 * Initialize underlying resources and link to the
	 * supplied {@link Writer} instance for serialization.
	 *
	 * @param output
	 * @param options
	 */
	void init(Writer output, Options options);

	/**
	 * Write the optional header section.
	 *
	 * @throws IOException
	 */
	default void writeHeader() throws IOException {
		// no-op
	}

	/**
	 * Write a single element to the underlying character stream.
	 *
	 * @param element
	 * @throws IOException
	 * @throws InterruptedException
	 */
	void write(E element) throws IOException, InterruptedException;

	/**
	 * Write an unspecified number of elements to the underlying character stream.
	 * This call wraps the write process inside the proper {@link #writeHeader()}
	 * and {@link #writeFooter()} invocations.
	 *
	 * @param source
	 * @throws IOException
	 * @throws InterruptedException
	 */
	default void writeAll(Supplier<? extends E> source) throws IOException, InterruptedException {
		writeHeader();

		E element;
		while((element=source.get())!=null) {
			write(element);
		}

		writeFooter();
	}

	/**
	 * Write an unspecified number of elements to the underlying character stream.
	 * This call wraps the write process inside the proper {@link #writeHeader()}
	 * and {@link #writeFooter()} invocations.
	 *
	 * @param source
	 * @throws IOException
	 * @throws InterruptedException
	 */
	default void writeAll(Iterable<? extends E> source) throws IOException, InterruptedException {
		writeHeader();

		for(E element : source) {
			write(element);
		}

		writeFooter();
	}

	/**
	 * Flush pending buffered element data and write the optional footer section.
	 *
	 * @throws IOException
	 */
	default void writeFooter() throws IOException {
		// no-op
	}

	@Override
	void close() throws IOException;
}
