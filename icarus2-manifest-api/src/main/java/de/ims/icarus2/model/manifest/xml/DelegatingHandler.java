/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.xml;

import java.util.Optional;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.util.IcarusUtils;

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

		// Delegate initial element handling to next builder if needed
		ManifestXmlHandler next =
				current.startElement(manifestLocation, uri, localName, qName, attributes)
				.filter(IcarusUtils.notEq(current)).orElse(null);
		if(next!=null) {
			push(next);

			next.startElement(manifestLocation, uri, localName, qName, attributes);
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String text = getText();

		ManifestXmlHandler current = handlers.peek();
		Optional<ManifestXmlHandler> future = current.endElement(manifestLocation, uri, localName, qName, text);

		// Discard current builder and switch to ancestor
		if(!future.isPresent()) {
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