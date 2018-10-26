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
package de.ims.icarus2.util.xml;

import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public interface XmlSerializer extends AutoCloseable {

	/**
	 * Begins a document definition by writing meta data
	 * like xml version and implementation specific info.
	 */
	void startDocument() throws Exception;

	default void startElement(String name, boolean empty) throws XMLStreamException {
		if(empty) {
			startEmptyElement(name);
		} else {
			startElement(name);
		}
	}

	/**
	 * Starts a new element using the given {@code name} as tag.
	 * Note that this method will assume a non-empty element to be created
	 * and that there be at least one child element!
	 * @throws XMLStreamException TODO
	 */
	void startElement(String name) throws XMLStreamException;

	/**
	 * Starts a new empty element using the given {@code name} as tag.
	 * An empty element is not allowed to contain nested elements,
	 * therefore only attributes can be written before the matching
	 * {@link #endElement(String)} call!
	 * @throws XMLStreamException TODO
	 */
	void startEmptyElement(String name) throws XMLStreamException;

	/**
	 * Writes a new attribute, using the given {@code name} and
	 * {@code value}. If the {@code value} is {@code null}, this method
	 * does nothing.
	 * @throws XMLStreamException TODO
	 */
	void writeAttribute(String name, String value) throws XMLStreamException;

	/**
	 * Utility method to write an attribute in case it is available via an
	 * optional-bearing method and an actual attribute value {@link Optional#isPresent() is present}.
	 * @param name
	 * @param opt
	 * @throws XMLStreamException
	 */
	default void writeAttribute(String name, Optional<String> opt) throws XMLStreamException {
		// Need to use this pattern as the basic writeAttribute method is allowed to throw an exception
		if(opt.isPresent() && XmlUtils.isLegalAttribute(opt)) {
			writeAttribute(name, opt.get());
		}
	}

	default void writeAttribute(String name, StringResource value) throws XMLStreamException {
		if(value!=null) {
			writeAttribute(name, value.getStringValue());
		}
	}

	// PRIMITIVE ATTRIBUTES
	default void writeAttribute(String name, int value) throws XMLStreamException {
		writeAttribute(name, String.valueOf(value));
	}

	default void writeAttribute(String name, long value) throws XMLStreamException {
		writeAttribute(name, String.valueOf(value));
	}

	default void writeAttribute(String name, float value) throws XMLStreamException {
		writeAttribute(name, String.valueOf(value));
	}

	default void writeAttribute(String name, double value) throws XMLStreamException {
		writeAttribute(name, String.valueOf(value));
	}

	default void writeAttribute(String name, boolean value) throws XMLStreamException {
		writeAttribute(name, String.valueOf(value));
	}

	default void writeAttribute(String name, byte value) throws XMLStreamException {
		writeAttribute(name, String.valueOf(value));
	}

	default void writeAttribute(String name, short value) throws XMLStreamException {
		writeAttribute(name, String.valueOf(value));
	}

	/**
	 * Closes the element with the given {@code name}. Note that the
	 * behavior of this method depends on whether or not the element
	 * has been started with either the {@link #startElement(String)}
	 * or {@link #startEmptyElement(String)} method!
	 * @throws XMLStreamException TODO
	 */
	void endElement(String name) throws XMLStreamException;

	default void writeTextOrCData(CharSequence text) throws XMLStreamException {
		if(XmlUtils.hasReservedXMLSymbols(text)) {
			writeCData(text);
		} else {
			writeText(text);
		}
	}

	default <S extends CharSequence> void writeTextOrCData(Optional<S> text) throws XMLStreamException {
		if(text.isPresent()) {
			writeTextOrCData(text.get());
		}
	}

	/**
	 * Writes the given {@code text} as content of the current element,
	 * doing nothing in case the {@code text} argument is {@code null}.
	 * @throws XMLStreamException TODO
	 */
	void writeText(CharSequence text) throws XMLStreamException;

	/**
	 * Writes the given {@code text} as encoded character data in the
	 * content section of the current element. Note that unlike the
	 * {@link #writeText(CharSequence)} method this time the {@code text} argument
	 * is not allowed to be {@code null}!
	 * @throws XMLStreamException TODO
	 *
	 * @see #writeText(CharSequence)
	 */
	void writeCData(CharSequence text) throws XMLStreamException;

	/**
	 * Writes a line-break. It is implementation specific whether the characters written
	 * will be platform specific.
	 * @throws XMLStreamException TODO
	 */
	void writeLineBreak() throws XMLStreamException;

	/**
	 * Finishes the document. It is implementation dependent whether pending
	 * opening tags will automatically be closed.
	 * @throws XMLStreamException TODO
	 */
	void endDocument() throws XMLStreamException;

	/**
	 * Releases all previously acquired resources and closes underlying output streams.
	 */
	@Override
	void close() throws XMLStreamException;
}
