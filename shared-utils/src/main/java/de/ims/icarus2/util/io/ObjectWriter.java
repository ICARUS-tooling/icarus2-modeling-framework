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
