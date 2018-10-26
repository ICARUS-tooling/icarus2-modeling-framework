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
import java.util.function.Supplier;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestXmlHandler {

	public static Supplier<SAXException> error(String message) {
		return () -> new SAXException(message);
	}

//	Supplier<XMLStreamException> error(String message);

	/**
	 * Handle the occurrence of a {@code start} tag with the given properties and return the
	 * {@link ManifestXmlHandler} responsible for it.
	 *
	 *
	 * @param manifestLocation
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 * @return
	 * @throws SAXException
	 */
	Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation, String uri, String localName, String qName,
			Attributes attributes) throws SAXException;

	/**
	 * Handle the occurrence of a {@code end} tag with the given properties and return the
	 * {@link ManifestXmlHandler} responsible for further processing. If the returned
	 * {@link Optional} is {@link Optional#empty()} then the handling will be delegated to
	 * the parent handler, i.e. the one that delegated to this handler previously.
	 *
	 * @param manifestLocation
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param text
	 * @return
	 * @throws SAXException
	 */
	Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation, String uri, String localName, String qName, String text)
			throws SAXException;

	void endNestedHandler(ManifestLocation manifestLocation, String uri, String localName, String qName,
			ManifestXmlHandler handler) throws SAXException;
}
