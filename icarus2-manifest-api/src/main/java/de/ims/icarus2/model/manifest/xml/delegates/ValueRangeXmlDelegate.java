/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.xml.delegates;

import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.standard.ValueRangeImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
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
	public void writeXml(XmlSerializer serializer) throws XMLStreamException {

		ValueRange range = getInstance();
		ValueType type = range.getValueType();

		serializer.startElement(ManifestXmlTags.VALUE_RANGE);

		// ATTRIBUTES
		if(range.isLowerBoundInclusive()!=ValueRange.DEFAULT_LOWER_INCLUSIVE_VALUE) {
			serializer.writeAttribute(ManifestXmlAttributes.INCLUDE_MIN, range.isLowerBoundInclusive());
		}
		if(range.isUpperBoundInclusive()!=ValueRange.DEFAULT_UPPER_INCLUSIVE_VALUE) {
			serializer.writeAttribute(ManifestXmlAttributes.INCLUDE_MAX, range.isUpperBoundInclusive());
		}

		// ELEMENTS

		ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.MIN, range.getLowerBound(), type);
		ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.MAX, range.getUpperBound(), type);
		ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.STEP_SIZE, range.getStepSize(), type);
		serializer.endElement(ManifestXmlTags.VALUE_RANGE);
	}

	/**
	 * @param attributes
	 */
	protected void readAttributes(Attributes attributes) {
		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.INCLUDE_MIN)
			.ifPresent(getInstance()::setLowerBoundInclusive);
		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.INCLUDE_MAX)
			.ifPresent(getInstance()::setUpperBoundInclusive);
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.VALUE_RANGE: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.MIN : {
			currentField = MIN_FIELD;
		} break;

		case ManifestXmlTags.MAX : {
			currentField = MAX_FIELD;
		} break;

		case ManifestXmlTags.STEP_SIZE : {
			currentField = STEP_SIZE_FIELD;
		} break;

		case ManifestXmlTags.EVAL : {
			/*
			 *  implementation note:
			 *
			 *  Previously we read in the attributes and instantiated the required
			 *  ExpressionFactory here. To decouple this responsibility from outside
			 *  code it was moved into the ExpressionXmlHandler itself. This way
			 *  consistency checks and errors are also concentrated at the correct location.
			 */
			handler = new ExpressionXmlHandler(getInstance().getValueType().getBaseClass());
		} break;

		default:
			throw new UnexpectedTagException(qName, true, ManifestXmlTags.VALUE_RANGE);
		}

		return Optional.of(handler);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		ManifestXmlHandler handler = this;

		ValueRange range = getInstance();
		ValueType valueType = range.getValueType();

		switch (localName) {
		case ManifestXmlTags.VALUE_RANGE: {
			handler = null;
		} break;

		case ManifestXmlTags.MIN : {
			if(text!=null && !range.getLowerBound().isPresent()) {
				range.setLowerBound((Comparable<?>)ManifestXmlUtils.parse(valueType, manifestLocation, text, true));
			}
		} break;

		case ManifestXmlTags.MAX : {
			if(text!=null && !range.getUpperBound().isPresent()) {
				range.setUpperBound((Comparable<?>)ManifestXmlUtils.parse(valueType, manifestLocation, text, true));
			}
		} break;

		case ManifestXmlTags.STEP_SIZE : {
			if(text!=null && !range.getStepSize().isPresent()) {
				range.setStepSize((Comparable<?>)ManifestXmlUtils.parse(valueType, manifestLocation, text, true));
			}
		} break;

		default:
			throw new UnexpectedTagException(qName, false, ManifestXmlTags.VALUE_RANGE);
		}

		return Optional.ofNullable(handler);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (localName) {

		case ManifestXmlTags.EVAL : {
			switch (currentField) {
			case MIN_FIELD:
				getInstance().setLowerBound((Comparable<?>)((ExpressionXmlHandler) handler).createExpression());
				break;

			case MAX_FIELD:
				getInstance().setUpperBound((Comparable<?>)((ExpressionXmlHandler) handler).createExpression());
				break;

			case STEP_SIZE_FIELD:
				getInstance().setStepSize((Comparable<?>)((ExpressionXmlHandler) handler).createExpression());
				break;

			default:
				throw new IllegalStateException("Unable to assign expression to correct field"); //$NON-NLS-1$
			}

			currentField = 0;
		} break;

		default:
			throw new UnsupportedNestingException(qName, ManifestXmlTags.VALUE_RANGE);
		}
	}
}
