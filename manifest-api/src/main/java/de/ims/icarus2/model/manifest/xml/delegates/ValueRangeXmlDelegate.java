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
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.standard.ExpressionXmlHandler;
import de.ims.icarus2.model.manifest.standard.ValueRangeImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueRangeXmlDelegate extends AbstractXmlDelegate<ValueRange> {


	// Stuff used for parsing
	private byte currentField = 0;
	private static final byte MIN_FIELD = 1;
	private static final byte MAX_FIELD = 2;
	private static final byte STEP_SIZE_FIELD = 3;

	public ValueRangeXmlDelegate() {
		// no-op
	}

	public ValueRangeXmlDelegate(ValueRange range) {
		setInstance(range);
	}

	public ValueRangeXmlDelegate(ValueType valueType) {
		setInstance(new ValueRangeImpl(valueType));
	}

	public ValueRangeXmlDelegate reset(ValueType valueType) {
		reset();
		setInstance(new ValueRangeImpl(valueType));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {

		ValueRange range = getInstance();
		ValueType type = range.getValueType();

		serializer.startElement(TAG_VALUE_RANGE);

		// ATTRIBUTES
		if(range.isLowerBoundInclusive()!=ValueRange.DEFAULT_LOWER_INCLUSIVE_VALUE) {
			serializer.writeAttribute(ATTR_INCLUDE_MIN, range.isLowerBoundInclusive());
		}
		if(range.isUpperBoundInclusive()!=ValueRange.DEFAULT_UPPER_INCLUSIVE_VALUE) {
			serializer.writeAttribute(ATTR_INCLUDE_MAX, range.isUpperBoundInclusive());
		}

		// ELEMENTS

		ManifestXmlUtils.writeValueElement(serializer, TAG_MIN, range.getLowerBound(), type);
		ManifestXmlUtils.writeValueElement(serializer, TAG_MAX, range.getUpperBound(), type);
		ManifestXmlUtils.writeValueElement(serializer, TAG_STEP_SIZE, range.getStepSize(), type);
		serializer.endElement(TAG_VALUE_RANGE);
	}

	/**
	 * @param attributes
	 */
	protected void readAttributes(Attributes attributes) {
		String lowerIncluded = ManifestXmlUtils.normalize(attributes, ATTR_INCLUDE_MIN);
		if(lowerIncluded!=null) {
			getInstance().setLowerBoundInclusive(Boolean.parseBoolean(lowerIncluded));
		}
		String upperIncluded = ManifestXmlUtils.normalize(attributes, ATTR_INCLUDE_MAX);
		if(upperIncluded!=null) {
			getInstance().setUpperBoundInclusive(Boolean.parseBoolean(upperIncluded));
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_VALUE_RANGE: {
			readAttributes(attributes);
		} break;

		case TAG_MIN : {
			currentField = MIN_FIELD;
		} break;

		case TAG_MAX : {
			currentField = MAX_FIELD;
		} break;

		case TAG_STEP_SIZE : {
			currentField = STEP_SIZE_FIELD;
		} break;

		case TAG_EVAL : {
			return new ExpressionXmlHandler();
		}

		default:
			throw new UnexpectedTagException(qName, true, TAG_VALUE_RANGE);
		}

		return this;
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		ValueRange range = getInstance();
		ValueType valueType = range.getValueType();

		switch (qName) {
		case TAG_VALUE_RANGE: {
			return null;
		}

		case TAG_MIN : {
			if(text!=null && range.getLowerBound()==null) {
				range.setLowerBound(valueType.parse(text, manifestLocation.getClassLoader()));
			}
		} break;

		case TAG_MAX : {
			if(text!=null && range.getUpperBound()==null) {
				range.setUpperBound(valueType.parse(text, manifestLocation.getClassLoader()));
			}
		} break;

		case TAG_STEP_SIZE : {
			if(text!=null && range.getStepSize()==null) {
				range.setStepSize(valueType.parse(text, manifestLocation.getClassLoader()));
			}
		} break;

		default:
			throw new UnexpectedTagException(qName, false, TAG_VALUE_RANGE);
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

		case TAG_EVAL : {
			switch (currentField) {
			case MIN_FIELD:
				getInstance().setLowerBound(((ExpressionXmlHandler) handler).createExpression());
				break;

			case MAX_FIELD:
				getInstance().setUpperBound(((ExpressionXmlHandler) handler).createExpression());
				break;

			case STEP_SIZE_FIELD:
				getInstance().setStepSize(((ExpressionXmlHandler) handler).createExpression());
				break;

			default:
				throw new IllegalStateException("Unable to assign expression to correct field"); //$NON-NLS-1$
			}

			currentField = 0;
		} break;

		default:
			throw new UnsupportedNestingException(qName, TAG_VALUE_RANGE);
		}
	}
}
