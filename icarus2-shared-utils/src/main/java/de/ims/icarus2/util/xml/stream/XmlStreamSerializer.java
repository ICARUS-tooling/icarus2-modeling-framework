/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.xml.stream;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public abstract class XmlStreamSerializer implements XmlSerializer {

	public static XmlStreamSerializer withoutNamespace(XMLStreamWriter writer) {
		return new XmlStreamSerializer(writer, null) {

			@Override
			public void writeSchemaInfo() throws XMLStreamException {
				// no-op
			}
		};
	}

	public static XmlStreamSerializer withDefaultNamespace(XMLStreamWriter writer,
			String namespace) throws XMLStreamException {
		requireNonNull(namespace);

		writer.setDefaultNamespace(namespace);

		return new XmlStreamSerializer(writer, null) {

			@Override
			public void writeSchemaInfo() throws XMLStreamException {
				writer.writeDefaultNamespace(namespace);
			}
		};
	}

	public static XmlStreamSerializer withNamespace(XMLStreamWriter writer,
			String prefix, String namespace) throws XMLStreamException {
		requireNonNull(prefix);
		requireNonNull(namespace);

		writer.setPrefix(prefix, namespace);

		return new XmlStreamSerializer(writer, namespace) {

			@Override
			public void writeSchemaInfo() throws XMLStreamException {
				writer.writeNamespace(prefix, namespace);
			}
		};
	}

	private final XMLStreamWriter writer;

	private StringBuilder characters = new StringBuilder();

	private char[] indentBuffer;

	private int indent = 0;
	private boolean nested = false;
	private boolean elementIsEmpty = false;

	private Stack<String> trace = new Stack<>();

	private final String namespace;

	private XmlStreamSerializer(XMLStreamWriter writer, String namespace) {
		this.namespace = namespace;
		this.writer = requireNonNull(writer);

		buildIndentBuffer(10);
	}

	private void buildIndentBuffer(int length) {
		indentBuffer = new char[length];

		Arrays.fill(indentBuffer, '\t');
	}

	private void writeIndent() throws XMLStreamException {

		if(indent>=indentBuffer.length) {
			buildIndentBuffer(indent*2);
		}

		writer.writeCharacters(indentBuffer, 0, indent);
	}

	private boolean flushCharacters() throws XMLStreamException {
		if(characters.length()==0) {
			return false;
		}

		writer.writeCharacters(characters.toString());

		characters.setLength(0);

		return true;
	}

	private void pushCharacters(CharSequence text) {
		characters.append(text);
	}

	@Override
	public void startElement(String name, boolean empty) throws XMLStreamException {
		checkState("Cannot nest elements until current one is closed", !elementIsEmpty);

		writeLineBreak();
		writeIndent();

		if(empty) {
			if(namespace==null) {
				writer.writeEmptyElement(name);
			} else {
				writer.writeEmptyElement(namespace, name);
			}
			elementIsEmpty = true;
		} else {
			if(namespace==null) {
				writer.writeStartElement(name);
			} else {
				writer.writeStartElement(namespace, name);
			}
		}
		indent++;
		nested = false;
//		pushElement(name, true);

		trace.add(name);
	}

	/**
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#writeAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void writeAttribute(String name, String value) throws XMLStreamException {
		if(value==null) {
			return;
		}
//		pushAttribute(name, value);
		writer.writeAttribute(name, value);
	}

	/**
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#endElement(java.lang.String)
	 */
	@Override
	public void endElement(String name) throws XMLStreamException {
		if(!trace.pop().equals(name))
			throw new XMLStreamException("Unexpected closing tag: "+name); //$NON-NLS-1$

		indent--;
//		popElement(name);
		if(nested) {
			writeLineBreak();
			writeIndent();
		} else {
			flushCharacters();
		}

		// XMLStreamWriter no longer writes the close tag explicitly
		if(!elementIsEmpty) {
			writer.writeEndElement();
		}

		nested = true;
		elementIsEmpty = false;
	}

	/**
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#writeText(java.lang.CharSequence)
	 */
	@Override
	public void writeText(CharSequence text) throws XMLStreamException {
		if(text==null) {
			return;
		}
		pushCharacters(text);
	}

	/**
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#writeCData(java.lang.CharSequence)
	 */
	@Override
	public void writeCData(CharSequence text) throws XMLStreamException {
		writer.writeCData(text.toString());
		nested = true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This implementation simply writes the content of {@link System#lineSeparator()}
	 * as character output.
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#writeLineBreak()
	 */
	@Override
	public void writeLineBreak() throws XMLStreamException {
		writer.writeCharacters(System.lineSeparator());
	}

	/**
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#startDocument()
	 */
	@Override
	public void startDocument() throws XMLStreamException {
		writer.writeStartDocument("utf-8", "1.0");
	}

	/**
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#endDocument()
	 */
	@Override
	public void endDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}

	/**
	 *
	 * @see de.ims.icarus2.util.xml.XmlSerializer#close()
	 */
	@Override
	public void close() throws XMLStreamException {
		writer.flush();
		writer.close();
	}

}
