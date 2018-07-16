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
package de.ims.icarus2.model.manifest.xml;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus2.model.manifest.api.ManifestLocation;

/**
 * @author Markus Gärtner
 *
 */
public class DelegatingHandler extends DefaultHandler {

	private final StringBuilder buffer = new StringBuilder();

	private final Stack<ManifestXmlHandler> handlers = new Stack<>();

	private final ManifestLocation manifestLocation;

	public DelegatingHandler(ManifestLocation manifestLocation, ManifestXmlHandler rootHandler) {
		this.manifestLocation = manifestLocation;

		push(rootHandler);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		buffer.append(ch, start, length);
	}

	private void push(ManifestXmlHandler handler) {
		handlers.push(handler);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if(handlers.isEmpty())
			throw new IllegalStateException("No handler present for tag: "+qName);

		ManifestXmlHandler current = handlers.peek();

		ManifestXmlHandler future = current.startElement(manifestLocation, uri, localName, qName, attributes);

		// Delegate initial element handling to next builder
		if(future!=null && future!=current) {
			push(future);

			future.startElement(manifestLocation, uri, localName, qName, attributes);
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String text = getText();

		ManifestXmlHandler current = handlers.peek();
		ManifestXmlHandler future = current.endElement(manifestLocation, uri, localName, qName, text);

		// Discard current builder and switch to ancestor
		if(future==null) {
			handlers.pop();

			if(!handlers.isEmpty()) {
				// Allow ancestor to collect nested entries
				ManifestXmlHandler ancestor = handlers.peek();

				ancestor.endNestedHandler(manifestLocation, uri, localName, qName, current);
			}
		}
	}

	private String logMsg(SAXParseException ex) {
		StringBuilder sb = new StringBuilder();
		sb.append(ex.getMessage()).append(":\n"); //$NON-NLS-1$
		sb.append("Message: ").append(ex.getMessage()).append('\n'); //$NON-NLS-1$
		sb.append("Public ID: ").append(String.valueOf(ex.getPublicId())).append('\n'); //$NON-NLS-1$
		sb.append("System ID: ").append(String.valueOf(ex.getSystemId())).append('\n'); //$NON-NLS-1$
		sb.append("Line: ").append(ex.getLineNumber()).append('\n'); //$NON-NLS-1$
		sb.append("Column: ").append(ex.getColumnNumber()); //$NON-NLS-1$
//		if(ex.getException()!=null)
//			sb.append("\nEmbedded: ").append(ex.getException()); //$NON-NLS-1$

//		report.log(level, sb.toString(), ex);

		return sb.toString();
	}

	@Override
	public void error(SAXParseException ex) throws SAXException {
		throw new SAXException(logMsg(ex));
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		throw new SAXException(logMsg(ex));
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		throw new SAXException(logMsg(ex));
	}

	private String getText() {
		String text = buffer.toString().trim();
		buffer.setLength(0);

		return text.isEmpty() ? null : text;
	}
}