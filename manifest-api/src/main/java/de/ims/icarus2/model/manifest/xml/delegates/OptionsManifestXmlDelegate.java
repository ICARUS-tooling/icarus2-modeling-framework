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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
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
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		OptionsManifest manifest = getInstance();

		// Write options in alphabetic order
		if(manifest.hasLocalOptions()) {
			List<Option> sortedOptions = new ArrayList<>(manifest.getLocalOptions());
			sortedOptions.sort((o1, o2) -> o1.getId().compareTo(o2.getId()));

			for(Option option : sortedOptions) {

				ValueType type = option.getValueType();

				Object defaultValue = option.getDefaultValue();
				ValueSet valueSet = option.getSupportedValues();
				ValueRange valueRange = option.getSupportedRange();
				String extensionPointUid = option.getExtensionPointUid();

				if(defaultValue==null && valueSet==null && valueRange==null && extensionPointUid==null) {
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

				if(extensionPointUid!=null) {
					serializer.startElement(ManifestXmlTags.EXTENSION_POINT);
					serializer.writeTextOrCData(extensionPointUid);
					serializer.endElement(ManifestXmlTags.EXTENSION_POINT);
				}

				if(defaultValue!=null) {
					ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.DEFAULT_VALUE, defaultValue, type);
				}

				if(valueSet!=null) {
					getValueSetXmlDelegate().reset(valueSet).writeXml(serializer);
				}

				if(valueRange!=null) {
					getValueRangeXmlDelegate().reset(valueRange).writeXml(serializer);
				}

				serializer.endElement(ManifestXmlTags.OPTION);
			}
		}

		// Write groups in alphabetic order
		if(manifest.hasLocalGroupIdentifiers()) {
			List<Identity> identities = CollectionUtils.asSortedList(manifest.getLocalGroupIdentifiers(), Identity.COMPARATOR);

			for(Identity group : identities) {
				serializer.startEmptyElement(ManifestXmlTags.GROUP);

				// ManifestXmlAttributes.ATTRIBUTES
				ManifestXmlUtils.writeIdentityAttributes(serializer, group);
				serializer.endElement(ManifestXmlTags.GROUP);
			}
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.OPTIONS: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.OPTION: {
			option = new OptionImpl();
			readOptionAttributes(attributes);
		} break;

		case ManifestXmlTags.GROUP: {
			DefaultModifiableIdentity identity = new DefaultModifiableIdentity();
			ManifestXmlUtils.readIdentityAttributes(attributes, identity);

			getInstance().addGroupIdentifier(identity);
		} break;

		case ManifestXmlTags.VALUE_RANGE : {
			return getValueRangeXmlDelegate().reset(option.getValueType());
		}

		case ManifestXmlTags.VALUE_SET : {
			return getValueSetXmlDelegate().reset(option.getValueType());
		}

		case ManifestXmlTags.DEFAULT_VALUE : {
			// only handled when closing element
		} break;

		case ManifestXmlTags.EXTENSION_POINT : {
			// no-op
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.OPTIONS: {
			return null;
		}

		case ManifestXmlTags.GROUP: {
			// no-op
		} break;

		case ManifestXmlTags.OPTION: {
			getInstance().addOption(option);
			option = null;
		} break;

		case ManifestXmlTags.DEFAULT_VALUE : {
			addDefaultValue(option.getValueType().parse(text, manifestLocation.getClassLoader()));
		} break;

		case ManifestXmlTags.EXTENSION_POINT : {
			option.setExtensionPointUid(text);
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	protected void addDefaultValue(Object value) {
		Object defaultValue = option.getDefaultValue();
		if(defaultValue instanceof Collection) {
			Collection.class.cast(defaultValue).add(value);
		} else if(defaultValue!=null) {
			if(!option.isMultiValue())
				throw new IllegalStateException("Cannot add more than one default value to optin that is not declared as multivalue"); //$NON-NLS-1$

			List<Object> list = new ArrayList<>(4);
			CollectionUtils.feedItems(list, defaultValue, value);
			option.setDefaultValue(list);
		}  else {
			option.setDefaultValue(value);
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
		option.setValueType(ManifestXmlUtils.typeValue(attributes));
		ManifestXmlUtils.readIdentityAttributes(attributes, option);

		String published = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.PUBLISHED);
		if(published!=null) {
			option.setPublished(Boolean.parseBoolean(published));
		} else {
			option.setPublished(Option.DEFAULT_PUBLISHED_VALUE);
		}

		String multivalue = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.MULTI_VALUE);
		if(multivalue!=null) {
			option.setMultiValue(Boolean.parseBoolean(multivalue));
		} else {
			option.setMultiValue(Option.DEFAULT_MULTIVALUE_VALUE);
		}

		String allowNull = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.ALLOW_NULL);
		if(allowNull!=null) {
			option.setAllowNull(Boolean.parseBoolean(allowNull));
		} else {
			option.setAllowNull(Option.DEFAULT_ALLOW_NULL);
		}

		option.setOptionGroup(ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.GROUP));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.OPTIONS;
	}

}
