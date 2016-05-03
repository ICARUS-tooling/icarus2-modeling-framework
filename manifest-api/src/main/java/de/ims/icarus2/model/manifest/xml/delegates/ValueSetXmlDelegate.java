/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.standard.ValueSetImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.xml.UnexpectedTagException;
import de.ims.icarus2.model.xml.UnsupportedNestingException;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueSetXmlDelegate extends AbstractXmlDelegate<ValueSet> {

	private ValueManifestXmlDelegate valueManifestXmlDelegate;

	public ValueSetXmlDelegate() {
		//no-op
	}

	public ValueSetXmlDelegate(ValueSet values) {
		setInstance(values);
	}

	public ValueSetXmlDelegate(ValueType valueType) {
		setInstance(new ValueSetImpl(valueType));
	}

	private ValueManifestXmlDelegate getValueManifestXmlDelegate() {
		if(valueManifestXmlDelegate==null) {
			valueManifestXmlDelegate = new ValueManifestXmlDelegate();
		}

		return valueManifestXmlDelegate;
	}

	public ValueSetXmlDelegate reset(ValueType valueType) {
		reset();
		setInstance(new ValueSetImpl(valueType));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(valueManifestXmlDelegate!=null) {
			valueManifestXmlDelegate.reset();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		serializer.startElement(TAG_VALUE_SET);

		ValueType type = getInstance().getValueType();

		for(int i=0; i<getInstance().valueCount(); i++) {
			Object value = getInstance().getValueAt(i);

			if(value instanceof ValueManifest) {
				getValueManifestXmlDelegate().reset((ValueManifest) value);
				getValueManifestXmlDelegate().writeXml(serializer);
			} else {
				ManifestXmlUtils.writeValueElement(serializer, TAG_VALUE, value, type);
			}
		}
		serializer.endElement(TAG_VALUE_SET);
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_VALUE_SET: {
			// no-op
		} break;

		case TAG_VALUE : {
			if(attributes.getLength()>0) {
				return new ValueManifestXmlDelegate(getInstance().getValueType());
			}
		} break;

		default:
			throw new UnexpectedTagException(qName, true, TAG_VALUE_SET);
		}

		return this;
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_VALUE_SET: {
			return null;
		}

		case TAG_VALUE : {
			Object value = getInstance().getValueType().parse(text, manifestLocation.getClassLoader());

			getInstance().addValue(value);
		} break;

		default:
			throw new UnexpectedTagException(qName, false, TAG_VALUE_SET);
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
		switch (qName) {

		case TAG_VALUE : {
			Object value = ((ValueManifestXmlDelegate) handler).getInstance();

			getInstance().addValue(value);
		} break;

		default:
			throw new UnsupportedNestingException(qName, TAG_VALUE_SET);
		}
	}
}
