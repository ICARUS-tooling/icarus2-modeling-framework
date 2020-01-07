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
package de.ims.icarus2.util.xml;

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.net.URL;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.lang.Lazy;

/**
 * @author Markus Gärtner
 *
 */
public class XmlUtils {

	/**
	 * The global namespace used for all xml schemas native to
	 * the ICARUS2 framework.
	 */
	public static final String ICARUS_NS = "imf";

	public static final String ICARUS_NS_URI = "http://www.ims.uni-stuttgart.de/icarus/xml/";
	public static final String XSI_NS_URI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

	public static final String ICARUS_SCHEMA_LOC = ICARUS_NS_URI;

	public static void writeSchemaAttributes(XMLStreamWriter writer, String namespaceSuffix, String schemaName) throws XMLStreamException {
		writer.writeAttribute("xmlns:xsi", XSI_NS_URI);
		writer.writeAttribute("xmlns:"+ICARUS_NS, ICARUS_NS_URI+namespaceSuffix);
		writer.writeAttribute("xsi:schemaLocation", ICARUS_SCHEMA_LOC+namespaceSuffix+" "+schemaName); // following rule for even number of URIs in schema location
	}

	private static Schema createSchema(URL location) {
		String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;

		//FIXME remove once we get proper XSD 1.1 support within the JSE core packages
//		schemaLanguage = "http://www.w3.org/XML/XMLSchema/v1.1";

		SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage);
		Schema schema;
		try {
			schema = factory.newSchema(location);
		} catch (SAXException e) {
			throw new IcarusRuntimeException(GlobalErrorCode.DELEGATION_FAILED, "Failed to create XML schema from location: "+location, e);
		}
		return schema;
	}

	public static Lazy<Schema> createShareableSchemaSource(URL location) {
		checkArgument("Schema location must not be null", location!=null);

		Lazy<Schema> lazy = Lazy.create(() -> createSchema(location), true);

		return lazy;
	}

	/**
	 * Checks whether the given {@link CharSequence} contains any symbols that
	 * are reserved for XML syntax/control characters.
	 *
	 * @param s
	 * @return
	 *
	 * @see #isReservedXMLSymbol(char)
	 */
	public static boolean hasReservedXMLSymbols(CharSequence s) {
		for(int i=0; i<s.length(); i++) {
			if(isReservedXMLSymbol(s.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static boolean isReservedXMLSymbol(char c) {
		switch(c) {

			case '<':
			case '>':
			case '&':
			case '"':
			case '\'':
				return true;

			default:
				return false;
		}
	}

	/**
	 * Checks for an attribute string to be either {@link Optional#isPresent() not set}
	 * or not contain any {@link #isIllegalAttributeSymbol(char) illegal symbols}.
	 *
	 * @param opt
	 * @return
	 */
	public static <S extends CharSequence> boolean isLegalAttribute(Optional<S> opt) {
		return !opt.isPresent() || !hasIllegalAttributeSymbols(opt.get());
	}

	public static boolean hasIllegalAttributeSymbols(CharSequence s) {
		for(int i=0; i<s.length(); i++) {
			if(isIllegalAttributeSymbol(s.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static boolean isIllegalAttributeSymbol(char c) {
		switch(c) {
			// Exclusive to attributes
			case '\n':
			case '\r':
			case '\t':
			// Generally reserved symbols
			case '<':
			case '>':
			case '&':
			case '"':
			case '\'':
				return true;

			default:
				return false;
		}
	}
}
