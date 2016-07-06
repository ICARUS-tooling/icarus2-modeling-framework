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
 *
 */
package de.ims.icarus2.util.xml.stream;

import java.util.Arrays;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class XmlStreamSerializer implements XmlSerializer {

	private final XMLStreamWriter writer;

	private StringBuilder characters = new StringBuilder();

	private char[] indentBuffer;

	private int indent = 0;
	private boolean nested = false;
	private boolean noNesting = false;

	private Stack<String> trace = new Stack<>();

	public XmlStreamSerializer(XMLStreamWriter writer) {
		if (writer == null)
			throw new NullPointerException("Invalid writer"); //$NON-NLS-1$

		this.writer = writer;

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

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws XMLStreamException {
		startElement(name, false);
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#startEmptyElement(java.lang.String)
	 */
	@Override
	public void startEmptyElement(String name) throws XMLStreamException {
		startElement(name, true);
	}

	private void startElement(String name, boolean empty) throws XMLStreamException {
		if(noNesting)
			throw new IllegalStateException("Cannot nest elements until current one is closed"); //$NON-NLS-1$

		writeLineBreak();
		writeIndent();

		if(empty) {
			writer.writeEmptyElement(name);
			noNesting = true;
		} else {
			writer.writeStartElement(name);
		}
		indent++;
		nested = false;
//		pushElement(name, true);

		trace.add(name);
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, java.lang.String)
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
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, int)
	 */
	@Override
	public void writeAttribute(String name, int value) throws XMLStreamException {
//		pushAttribute(name, String.valueOf(value));
		writer.writeAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, long)
	 */
	@Override
	public void writeAttribute(String name, long value) throws XMLStreamException {
//		pushAttribute(name, String.valueOf(value));
		writer.writeAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, double)
	 */
	@Override
	public void writeAttribute(String name, double value) throws XMLStreamException {
//		pushAttribute(name, String.valueOf(value));
		writer.writeAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, float)
	 */
	@Override
	public void writeAttribute(String name, float value) throws XMLStreamException {
//		pushAttribute(name, String.valueOf(value));
		writer.writeAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, boolean)
	 */
	@Override
	public void writeAttribute(String name, boolean value) throws XMLStreamException {
//		pushAttribute(name, String.valueOf(value));
		writer.writeAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#endElement(java.lang.String)
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
			writer.writeEndElement();
		} else {
			if(flushCharacters()) {
				writer.writeEndElement();
			}
		}

		nested = true;
		noNesting = false;
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#writeText(CharSequence)
	 */
	@Override
	public void writeText(CharSequence text) throws XMLStreamException {
		if(text==null) {
			return;
		}
		pushCharacters(text);
	}

	/**
	 * @see de.ims.icarus2.util.xml.XmlSerializer#writeCData(CharSequence)
	 */
	@Override
	public void writeCData(CharSequence text) throws Exception {
		writer.writeCData(text.toString());
		nested = true;
	}

	@Override
	public void writeLineBreak() throws XMLStreamException {
		//TODO maybe reduce linebreak to a single newline character and/or do it platform dependent?
		writer.writeCharacters("\r\n"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#startDocument()
	 */
	@Override
	public void startDocument() throws XMLStreamException {
		writer.writeStartDocument();

		//FIXME enable DOCTYPE section once schema file is reworked!
//		writeLineBreak();
//		writer.writeDTD("<!DOCTYPE model SYSTEM \"corpus.dtd\">"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#endDocument()
	 */
	@Override
	public void endDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}

	/**
	 * @see de.ims.icarus2.model.api.xml.XmlSerializer#close()
	 */
	@Override
	public void close() throws Exception {
		writer.flush();
		writer.close();
	}

}
