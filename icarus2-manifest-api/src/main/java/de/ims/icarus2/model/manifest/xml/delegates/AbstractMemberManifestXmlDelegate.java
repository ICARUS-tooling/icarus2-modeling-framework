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
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.standard.AbstractMemberManifest.PropertyImpl;
import de.ims.icarus2.model.manifest.standard.DocumentationImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractMemberManifestXmlDelegate<M extends MemberManifest> extends AbstractManifestXmlDelegate<M> {

	private int localPropertyCount = 0;
	private boolean hasLocalOptions;
	private Property property;

	private DocumentationXmlDelegate documentationXmlDelegate;
	private OptionsManifestXmlDelegate optionsManifestXmlDelegate;

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		localPropertyCount = 0;
		hasLocalOptions = false;
		property = null;

		if(documentationXmlDelegate!=null) {
			documentationXmlDelegate.reset();
		}

		if(optionsManifestXmlDelegate!=null) {
			optionsManifestXmlDelegate.reset();
		}
	}

	private DocumentationXmlDelegate getDocumentationXmlDelegate() {
		if(documentationXmlDelegate==null) {
			documentationXmlDelegate = new DocumentationXmlDelegate();
		}

		return documentationXmlDelegate;
	}

	private OptionsManifestXmlDelegate getOptionsManifestXmlDelegate() {
		if(optionsManifestXmlDelegate==null) {
			optionsManifestXmlDelegate = new OptionsManifestXmlDelegate();
		}

		return optionsManifestXmlDelegate;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractManifestXmlDelegate#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		MemberManifest manifest = getInstance();
		// IMPORTANT: we must not write the ID field again, since super implementation took care of that!
		ManifestXmlUtils.writeIdentityAttributes(serializer,
				null, manifest.getName(), manifest.getDescription(), manifest.getIcon());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractManifestXmlDelegate#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ManifestXmlUtils.readIdentityAttributes(attributes, getInstance());
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		MemberManifest manifest = getInstance();

		ManifestXmlUtils.writeIdentityFieldElements(serializer, manifest);

		// Write documentation
		if(manifest.getDocumentation()!=null) {
			getDocumentationXmlDelegate().reset(manifest.getDocumentation()).writeXml(serializer);
		}

		// Write options manifest
		if(manifest.getOptionsManifest()!=null) {
			getOptionsManifestXmlDelegate().reset(manifest.getOptionsManifest()).writeXml(serializer);
		}

		Set<Property> localProperties = manifest.getLocalProperties();

		if(!localProperties.isEmpty()) {
			serializer.startElement(ManifestXmlTags.PROPERTIES);

			List<Property> sortedProperties = new ArrayList<>(localProperties);
			sortedProperties.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));

			for(Property property : sortedProperties) {
				String propertyName = property.getName();
				ValueType type = property.getValueType();

				if(type==ValueType.UNKNOWN)
					throw new UnsupportedOperationException("Cannot serialize unknown value for property: "+propertyName); //$NON-NLS-1$
				if(type==ValueType.CUSTOM)
					throw new UnsupportedOperationException("Cannot serialize custom value for propert: "+propertyName); //$NON-NLS-1$

				serializer.startElement(ManifestXmlTags.PROPERTY);
				serializer.writeAttribute(ManifestXmlAttributes.NAME, propertyName);
				serializer.writeAttribute(ManifestXmlAttributes.VALUE_TYPE, ManifestXmlUtils.getSerializedForm(type));

				if(property.isMultiValue()) {
					for(Object item : (Collection<?>) property.getValue()) {
						ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.VALUE, item, type);
					}
				} else {
					Object value = property.getValue();

					serializer.writeTextOrCData(type.toChars(value));
				}

				serializer.endElement(ManifestXmlTags.PROPERTY);
			}

			serializer.endElement(ManifestXmlTags.PROPERTIES);
		}
	}

	private void maybeLinkProperties() {
		if(hasLocalOptions && localPropertyCount>0) {
			OptionsManifest optionsManifest = getInstance().getOptionsManifest();
			for(String name : optionsManifest.getOptionIds()) {
				Option option = optionsManifest.getOption(name);
				Property property = getInstance().getProperty(name);

				// Gets only called by the parsing routines and therefore we
				// can safely cast to the implementation we use there.
				if(property!=null) {
					((PropertyImpl)property).setOption(option);
				}
			}
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (localName) {

		case ManifestXmlTags.NAME:
		case ManifestXmlTags.DESCRIPTION:
		case ManifestXmlTags.ICON: {
			return this;
		}

		case ManifestXmlTags.OPTIONS: {
			hasLocalOptions = true;
			return getOptionsManifestXmlDelegate().reset(getInstance().getOptionsManifest());
		}

		case ManifestXmlTags.DOCUMENTATION: {
			return getDocumentationXmlDelegate().reset(new DocumentationImpl());
		}

		case ManifestXmlTags.CATEGORIES: {
			return this;
		}

		case ManifestXmlTags.CATEGORY: {
			Category category = ManifestXmlUtils.readCategory(attributes);
			getInstance().addCategory(category);
			return this;
		}

		case ManifestXmlTags.PROPERTIES: {
			return this;
		}

		case ManifestXmlTags.PROPERTY: {
			localPropertyCount++;
			String name = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.NAME);
			ValueType valueType = ManifestXmlUtils.typeValue(attributes);
			property = new PropertyImpl(name, valueType);

			return this;
		}

		case ManifestXmlTags.VALUE: {
			checkEmptyOrMultiValue();
			property.setMultiValue(true);
			return this;
		}

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}
	}

	private void checkEmptyOrMultiValue() {
		Object value = property.getValue();
		if(value!=null && !(value instanceof Collection))
			throw new IllegalStateException("Non-collection value already set");
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {

		switch (localName) {

		case ManifestXmlTags.NAME: {
			getInstance().setName(text);
			return this;
		}

		case ManifestXmlTags.DESCRIPTION: {
			getInstance().setDescription(text);
			return this;
		}

		case ManifestXmlTags.ICON: {
			getInstance().setIcon(ManifestXmlUtils.iconValue(text, true));
			return this;
		}

		case ManifestXmlTags.CATEGORIES: {
			return this;
		}

		case ManifestXmlTags.CATEGORY: {
			return this;
		}

		case ManifestXmlTags.PROPERTIES: {
			maybeLinkProperties();
			return this;
		}

		case ManifestXmlTags.PROPERTY: {

			if(!property.isMultiValue()) {
				property.setValue(property.getValueType().parse(text, manifestLocation.getClassLoader()));
			}

			getInstance().addProperty(property);

			return this;
		}

		case ManifestXmlTags.VALUE: {
			Object value = property.getValueType().parse(text, manifestLocation.getClassLoader());
			addValue(value);
			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	private void addValue(Object value) {
		if(property.getValue()==null) {
			property.setValue(new ArrayList<>(4));
		}

		Collection.class.cast(property.getValue()).add(value);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {

		switch (localName) {
		case ManifestXmlTags.OPTIONS: {
			getInstance().setOptionsManifest(((OptionsManifestXmlDelegate) handler).getInstance());
			maybeLinkProperties();
		} break;

		case ManifestXmlTags.DOCUMENTATION: {
			getInstance().setDocumentation(((DocumentationXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}

	}
}
