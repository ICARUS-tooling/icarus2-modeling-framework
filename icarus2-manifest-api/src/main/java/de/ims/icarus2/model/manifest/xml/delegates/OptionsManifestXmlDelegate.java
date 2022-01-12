/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.collections.CollectionUtils.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity;
import de.ims.icarus2.model.manifest.standard.OptionsManifestImpl.OptionImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class OptionsManifestXmlDelegate extends AbstractManifestXmlDelegate<OptionsManifest> {

	private ValueSetXmlDelegate valueSetXmlDelegate;
	private ValueRangeXmlDelegate valueRangeXmlDelegate;

	private Option option;

	private ModifiableIdentity group;

	public OptionsManifestXmlDelegate() {
		// no-op
	}

	public OptionsManifestXmlDelegate(OptionsManifest optionsManifest) {
		setInstance(optionsManifest);
	}

	private ValueSetXmlDelegate getValueSetXmlDelegate() {
		if(valueSetXmlDelegate==null) {
			valueSetXmlDelegate = new ValueSetXmlDelegate();
		}

		return valueSetXmlDelegate;
	}

	private ValueRangeXmlDelegate getValueRangeXmlDelegate() {
		if(valueRangeXmlDelegate==null) {
			valueRangeXmlDelegate = new ValueRangeXmlDelegate();
		}

		return valueRangeXmlDelegate;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(valueSetXmlDelegate!=null) {
			valueSetXmlDelegate.reset();
		}

		if(valueRangeXmlDelegate!=null) {
			valueRangeXmlDelegate.reset();
		}

		option = null;
		group = null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		OptionsManifest manifest = getInstance();

		// Write groups in alphabetic order
		if(manifest.hasLocalGroupIdentifiers()) {
			List<Identity> identities = CollectionUtils.asSortedList(
					manifest.getLocalGroupIdentifiers(),
					Identity.COMPARATOR);

			for(Identity group : identities) {
				ManifestXmlUtils.writeIdentityElement(serializer, ManifestXmlTags.GROUP, group);
			}
		}

		// Write options in alphabetic order
		if(manifest.hasLocalOptions()) {
			List<Option> sortedOptions = CollectionUtils.asSortedList(
					manifest.getLocalOptions(),
					Identity.ID_COMPARATOR);

			for(Option option : sortedOptions) {

				ValueType type = option.getValueType();

				Optional<Object> defaultValue = option.getDefaultValue();
				Optional<ValueSet> valueSet = option.getSupportedValues();
				Optional<ValueRange> valueRange = option.getSupportedRange();
				Optional<String> extensionPointUid = option.getExtensionPointUid();

				if(IcarusUtils.nonePresent(defaultValue, valueSet, valueRange, extensionPointUid)) {
					serializer.startEmptyElement(ManifestXmlTags.OPTION);
				} else {
					serializer.startElement(ManifestXmlTags.OPTION);
				}

				// Attributes

				ManifestXmlUtils.writeIdentityAttributes(serializer, option);
				serializer.writeAttribute(ManifestXmlAttributes.VALUE_TYPE, ManifestXmlUtils.getSerializedForm(type));
				if(option.isPublished()!=Option.DEFAULT_PUBLISHED_VALUE) {
					serializer.writeAttribute(ManifestXmlAttributes.PUBLISHED, option.isPublished());
				}
				if(option.isMultiValue()!=Option.DEFAULT_MULTIVALUE_VALUE) {
					serializer.writeAttribute(ManifestXmlAttributes.MULTI_VALUE, option.isMultiValue());
				}
				if(option.isAllowNull()!=Option.DEFAULT_ALLOW_NULL) {
					serializer.writeAttribute(ManifestXmlAttributes.ALLOW_NULL, option.isAllowNull());
				}
				serializer.writeAttribute(ManifestXmlAttributes.GROUP, option.getOptionGroupId());

				// Elements

				if(extensionPointUid.isPresent()) {
					serializer.startElement(ManifestXmlTags.EXTENSION_POINT);
					serializer.writeTextOrCData(extensionPointUid);
					serializer.endElement(ManifestXmlTags.EXTENSION_POINT);
				}

				if(defaultValue.isPresent()) {
					serializer.startElement(ManifestXmlTags.DEFAULT_VALUE);
					if(option.isMultiValue()) {
						Collection<?> defaultValues = (Collection<?>) defaultValue.get();
						for(Object value : defaultValues) {
							ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.VALUE, value, type);
						}
					} else {
						ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.VALUE, defaultValue.get(), type);
					}
					serializer.endElement(ManifestXmlTags.DEFAULT_VALUE);
				}

				if(valueSet.isPresent()) {
					getValueSetXmlDelegate().reset(valueSet.get()).writeXml(serializer);
				}

				if(valueRange.isPresent()) {
					getValueRangeXmlDelegate().reset(valueRange.get()).writeXml(serializer);
				}

				serializer.endElement(ManifestXmlTags.OPTION);
			}
		}
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.OPTIONS: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.OPTION: {
			option = new OptionImpl();
			readOptionAttributes(attributes);
		} break;

		case ManifestXmlTags.GROUP: {
			group = new DefaultModifiableIdentity<>();
			ManifestXmlUtils.readIdentityAttributes(attributes, group);

			getInstance().addGroupIdentifier(group);
		} break;

		case ManifestXmlTags.NAME:
		case ManifestXmlTags.DESCRIPTION: {
			// no-op
		} break;

		case ManifestXmlTags.VALUE_RANGE : {
			handler = getValueRangeXmlDelegate().reset(option.getValueType());
		} break;

		case ManifestXmlTags.VALUE_SET : {
			handler = getValueSetXmlDelegate().reset(option.getValueType());
		} break;

		case ManifestXmlTags.DEFAULT_VALUE : {
			// only handled when closing element
		} break;

		case ManifestXmlTags.VALUE : {
			// no-op
		} break;

		case ManifestXmlTags.EXTENSION_POINT : {
			// no-op
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return Optional.ofNullable(handler);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.OPTIONS: {
			handler = null;
		} break;

		case ManifestXmlTags.GROUP: {
			group = null;
		} break;

		case ManifestXmlTags.NAME: {
			group.setName(text);
		} break;

		case ManifestXmlTags.DESCRIPTION: {
			group.setDescription(text);
		} break;

		case ManifestXmlTags.OPTION: {
			getInstance().addOption(option);
			option = null;
		} break;

		case ManifestXmlTags.DEFAULT_VALUE : {
			// no-op
		} break;

		case ManifestXmlTags.VALUE : {
			Object value = ManifestXmlUtils.parse(option.getValueType(), manifestLocation, text, true);
			if(option.isMultiValue()) {
				addDefaultValue(value);
			} else {
				option.setDefaultValue(value);
			}
		} break;

		case ManifestXmlTags.EXTENSION_POINT : {
			option.setExtensionPointUid(text);
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return Optional.ofNullable(handler);
	}

	protected void addDefaultValue(Object value) {
		Object defaultValue = option.getDefaultValue().orElse(null);

		if(!option.isMultiValue())
			throw new IllegalStateException("Cannot add more than one default value "
					+ "to option that is not declared as multivalue");

		if(defaultValue instanceof Collection) {
			Collection.class.cast(defaultValue).add(value);
		} else if(defaultValue!=null) {

			List<Object> list = new ArrayList<>(4);
			CollectionUtils.feedItems(list, defaultValue, value);
			option.setDefaultValue(list);
		}  else {
			option.setDefaultValue(list(value));
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (localName) {

		case ManifestXmlTags.VALUE_RANGE : {
			option.setSupportedRange(((ValueRangeXmlDelegate)handler).getInstance());
		} break;

		case ManifestXmlTags.VALUE_SET : {
			option.setSupportedValues(((ValueSetXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @param attributes
	 */
	protected void readOptionAttributes(Attributes attributes) {
		ManifestXmlUtils.typeValue(attributes)
			.ifPresent(option::setValueType);
		ManifestXmlUtils.readIdentityAttributes(attributes, option);

		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.PUBLISHED)
			.ifPresent(option::setPublished);

		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.MULTI_VALUE)
			.ifPresent(option::setMultiValue);

		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.ALLOW_NULL)
			.ifPresent(option::setAllowNull);

		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.GROUP)
			.ifPresent(option::setOptionGroup);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.OPTIONS;
	}

}
