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
package de.ims.icarus2.model.manifest.xml.delegates;

import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.Documentation.Resource;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.DocumentationImpl.ResourceImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class DocumentationXmlDelegate extends AbstractXmlDelegate<Documentation> {

	private Resource resource;

	public DocumentationXmlDelegate() {
		//no-op
	}

	public DocumentationXmlDelegate(Documentation documentation) {
		setInstance(documentation);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		resource = null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_DOCUMENTATION: {
			ManifestXmlUtils.readIdentity(attributes, getInstance());
		} break;

		case TAG_RESOURCE: {
			resource = new ResourceImpl();
			ManifestXmlUtils.readIdentity(attributes, resource);
		} break;

		case TAG_CONTENT: {
			// no-op
		} break;

		default:
			throw new UnexpectedTagException(qName, true, TAG_DOCUMENTATION);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (qName) {
		case TAG_DOCUMENTATION: {
			return null;
		}

		case TAG_CONTENT: {
			getInstance().setContent(text);
		} break;

		case TAG_RESOURCE: {

			if(text!=null) {
				try {
					resource.setUri(new URI(text));
				} catch (URISyntaxException e) {
					throw new SAXException("Invalid resoucre uri", e); //$NON-NLS-1$
				}
			}

			getInstance().addResource(resource);
			resource = null;
		} break;

		default:
			throw new UnexpectedTagException(qName, false, TAG_DOCUMENTATION);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		throw new UnsupportedNestingException(qName, TAG_DOCUMENTATION);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		Documentation documentation = getInstance();

		serializer.startElement(TAG_DOCUMENTATION);
		ManifestXmlUtils.writeIdentityAttributes(serializer, documentation);

		if(documentation.getContent()!=null) {
			serializer.startElement(TAG_CONTENT);
			serializer.writeCData(documentation.getContent());
			serializer.endElement(TAG_CONTENT);
		}

		for(Resource resource : documentation.getResources()) {
			if(resource.getUri()==null)
				throw new IllegalStateException("Resource is missing url"); //$NON-NLS-1$

			serializer.startElement(TAG_RESOURCE);
			ManifestXmlUtils.writeIdentityAttributes(serializer, resource);
			serializer.writeTextOrCData(resource.getUri().toString());
			serializer.endElement(TAG_RESOURCE);
		}

		serializer.endElement(TAG_DOCUMENTATION);
	}
}
