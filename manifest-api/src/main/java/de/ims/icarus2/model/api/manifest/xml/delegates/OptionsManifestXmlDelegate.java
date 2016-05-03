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
package de.ims.icarus2.model.api.manifest.xml.delegates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.MemberManifest;
import de.ims.icarus2.model.api.manifest.OptionsManifest;
import de.ims.icarus2.model.api.manifest.OptionsManifest.Option;
import de.ims.icarus2.model.api.manifest.ValueRange;
import de.ims.icarus2.model.api.manifest.ValueSet;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.standard.manifest.DefaultModifiableIdentity;
import de.ims.icarus2.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus2.model.standard.manifest.OptionsManifestImpl.OptionImpl;
import de.ims.icarus2.model.types.ValueType;
import de.ims.icarus2.model.xml.XmlSerializer;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
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

	public OptionsManifestXmlDelegate reset(MemberManifest memberManifest) {
		reset();
		setInstance(new OptionsManifestImpl(memberManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.xml.delegates.AbstractXmlDelegate#reset()
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
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#writeElements(de.ims.icarus2.model.xml.XmlSerializer)
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
					serializer.startEmptyElement(TAG_OPTION);
				} else {
					serializer.startElement(TAG_OPTION);
				}

				// Attributes

				ManifestXmlUtils.writeIdentityAttributes(serializer, option);
				serializer.writeAttribute(ATTR_VALUE_TYPE, ManifestXmlUtils.getSerializedForm(type));
				if(option.isPublished()!=Option.DEFAULT_PUBLISHED_VALUE) {
					serializer.writeAttribute(ATTR_PUBLISHED, option.isPublished());
				}
				if(option.isMultiValue()!=Option.DEFAULT_MULTIVALUE_VALUE) {
					serializer.writeAttribute(ATTR_MULTI_VALUE, option.isMultiValue());
				}
				if(option.isAllowNull()!=Option.DEFAULT_ALLOW_NULL) {
					serializer.writeAttribute(ATTR_ALLOW_NULL, option.isAllowNull());
				}
				serializer.writeAttribute(ATTR_GROUP, option.getOptionGroupId());

				// Elements

				if(extensionPointUid!=null) {
					serializer.startElement(TAG_EXTENSION_POINT);
					serializer.writeText(extensionPointUid);
					serializer.endElement(TAG_EXTENSION_POINT);
				}

				if(defaultValue!=null) {
					ManifestXmlUtils.writeValueElement(serializer, TAG_DEFAULT_VALUE, defaultValue, type);
				}

				if(valueSet!=null) {
					getValueSetXmlDelegate().reset(valueSet).writeXml(serializer);
				}

				if(valueRange!=null) {
					getValueRangeXmlDelegate().reset(valueRange).writeXml(serializer);
				}

				serializer.endElement(TAG_OPTION);
			}
		}

		// Write groups in alphabetic order
		if(manifest.hasLocalGroupIdentifiers()) {
			List<Identity> identities = CollectionUtils.asSortedList(manifest.getLocalGroupIdentifiers(), Identity.COMPARATOR);

			for(Identity group : identities) {
				serializer.startEmptyElement(TAG_GROUP);

				// ATTRIBUTES
				ManifestXmlUtils.writeIdentityAttributes(serializer, group);
				serializer.endElement(TAG_GROUP);
			}
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_OPTIONS: {
			readAttributes(attributes);
		} break;

		case TAG_OPTION: {
			option = new OptionImpl();
			readOptionAttributes(attributes);
		} break;

		case TAG_GROUP: {
			DefaultModifiableIdentity identity = new DefaultModifiableIdentity();
			ManifestXmlUtils.readIdentity(attributes, identity);

			getInstance().addGroupIdentifier(identity);
		} break;

		case TAG_VALUE_RANGE : {
			return getValueRangeXmlDelegate().reset(option.getValueType());
		}

		case TAG_VALUE_SET : {
			return getValueSetXmlDelegate().reset(option.getValueType());
		}

		case TAG_DEFAULT_VALUE : {
			// no-op
		} break;

		case TAG_EXTENSION_POINT : {
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
		switch (qName) {
		case TAG_OPTIONS: {
			return null;
		}

		case TAG_GROUP: {
			// no-op
		} break;

		case TAG_OPTION: {
			getInstance().addOption(option);
			option = null;
		} break;

		case TAG_DEFAULT_VALUE : {
			addDefaultValue(option.getValueType().parse(text, manifestLocation.getClassLoader()));
		} break;

		case TAG_EXTENSION_POINT : {
			option.setExtensionPointUid(text);
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	@SuppressWarnings("unchecked")
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
	 * @see de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_VALUE_RANGE : {
			option.setSupportedRange(((ValueRangeXmlDelegate)handler).getInstance());
		} break;

		case TAG_VALUE_SET : {
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
		ManifestXmlUtils.readIdentity(attributes, option);

		String published = ManifestXmlUtils.normalize(attributes, ATTR_PUBLISHED);
		if(published!=null) {
			option.setPublished(Boolean.parseBoolean(published));
		} else {
			option.setPublished(Option.DEFAULT_PUBLISHED_VALUE);
		}

		String multivalue = ManifestXmlUtils.normalize(attributes, ATTR_MULTI_VALUE);
		if(multivalue!=null) {
			option.setMultiValue(Boolean.parseBoolean(multivalue));
		} else {
			option.setMultiValue(Option.DEFAULT_MULTIVALUE_VALUE);
		}

		String allowNull = ManifestXmlUtils.normalize(attributes, ATTR_ALLOW_NULL);
		if(allowNull!=null) {
			option.setAllowNull(Boolean.parseBoolean(allowNull));
		} else {
			option.setAllowNull(Option.DEFAULT_ALLOW_NULL);
		}

		option.setOptionGroup(ManifestXmlUtils.normalize(attributes, ATTR_GROUP));
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_OPTIONS;
	}

}
