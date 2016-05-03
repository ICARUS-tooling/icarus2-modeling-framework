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

import static de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils.iconValue;
import static de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils.normalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.MemberManifest;
import de.ims.icarus2.model.api.manifest.MemberManifest.Property;
import de.ims.icarus2.model.api.manifest.OptionsManifest;
import de.ims.icarus2.model.api.manifest.OptionsManifest.Option;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.standard.manifest.AbstractMemberManifest.PropertyImpl;
import de.ims.icarus2.model.standard.manifest.DocumentationImpl;
import de.ims.icarus2.model.types.ValueType;
import de.ims.icarus2.model.util.StringResource;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractMemberManifestXmlDelegate<M extends MemberManifest> extends AbstractManifestXmlDelegate<M> {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractMemberManifestXmlDelegate.class);

	private int localPropertyCount = 0;
	private boolean hasLocalOptions;
	private Property property;

	private DocumentationXmlDelegate documentationXmlDelegate;
	private OptionsManifestXmlDelegate optionsManifestXmlDelegate;

	/**
	 * @see de.ims.icarus2.model.api.manifest.xml.delegates.AbstractXmlDelegate#reset()
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

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		MemberManifest manifest = getInstance();

		// Write documentation
		if(manifest.getDocumentation()!=null) {
			new DocumentationXmlDelegate(manifest.getDocumentation()).writeXml(serializer);
		}

		// Write options manifest
		if(manifest.getOptionsManifest()!=null) {
			//FIXME delegate
		}

		Set<Property> localProperties = manifest.getLocalProperties();

		if(!localProperties.isEmpty()) {
			serializer.startElement(TAG_PROPERTIES);

			List<Property> sortedProperties = new ArrayList<>(localProperties);
			sortedProperties.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));

			for(Property property : sortedProperties) {
				String name = property.getName();
				ValueType type = property.getValueType();

				if(type==ValueType.UNKNOWN)
					throw new UnsupportedOperationException("Cannot serialize unknown value for property: "+name); //$NON-NLS-1$
				if(type==ValueType.CUSTOM)
					throw new UnsupportedOperationException("Cannot serialize custom value for propert: "+name); //$NON-NLS-1$

				serializer.startElement(TAG_PROPERTY);
				serializer.writeAttribute(ATTR_NAME, name);
				serializer.writeAttribute(ATTR_VALUE_TYPE, ManifestXmlUtils.getSerializedForm(type));

				if(property.isMultiValue()) {
					for(Object item : (Collection<?>) property.getValue()) {
						ManifestXmlUtils.writeValueElement(serializer, TAG_VALUE, item, type);
					}
				} else {
					Object value = property.getValue();

					serializer.writeText(type.toChars(value));
				}

				serializer.endElement(TAG_PROPERTY);
			}

			serializer.endElement(TAG_PROPERTIES);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#writeAttributes(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		MemberManifest manifest = getInstance();

		serializer.writeAttribute(ATTR_NAME, manifest.getName());
		serializer.writeAttribute(ATTR_DESCRIPTION, manifest.getDescription());

		Icon icon = manifest.getIcon();
		if(icon instanceof StringResource) {
			serializer.writeAttribute(ATTR_ICON, ((StringResource)icon).getStringValue());
		} else if(icon != null) {
			log.warn("Skipping serialization of icon for manifest: {}",
					(manifest.getId()==null ? manifest.getId() : "<unnamed>")); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String name = normalize(attributes, ATTR_NAME);
		if(name!=null) {
			getInstance().setName(name);
		}

		String description = normalize(attributes, ATTR_DESCRIPTION);
		if(description!=null) {
			getInstance().setDescription(description);
		}

		String icon = normalize(attributes, ATTR_ICON);
		if(icon!=null) {
			getInstance().setIcon(iconValue(icon));
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
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#startElement(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_OPTIONS: {
			hasLocalOptions = true;
			return getOptionsManifestXmlDelegate().reset(getInstance());
		}

		case TAG_DOCUMENTATION: {
			return getDocumentationXmlDelegate().reset(new DocumentationImpl());
		}

		case TAG_PROPERTIES: {
			return this;
		}

		case TAG_PROPERTY: {
			localPropertyCount++;
			String name = ManifestXmlUtils.normalize(attributes, ATTR_NAME);
			ValueType valueType = ManifestXmlUtils.typeValue(attributes);
			property = new PropertyImpl(name, valueType);

			return this;
		}

		case TAG_VALUE: {
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

		switch (qName) {

		case TAG_PROPERTIES: {
			maybeLinkProperties();
			return this;
		}

		case TAG_PROPERTY: {

			if(!property.isMultiValue()) {
				property.setValue(property.getValueType().parse(text, manifestLocation.getClassLoader()));
			}

			getInstance().addProperty(property);

			return this;
		}

		case TAG_VALUE: {
			Object value = property.getValueType().parse(text, manifestLocation.getClassLoader());
			addValue(value);
			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	@SuppressWarnings("unchecked")
	private void addValue(Object value) {
		if(property.getValue()==null) {
			property.setValue(new ArrayList<>(4));
		}

		Collection.class.cast(property.getValue()).add(value);
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#endNestedHandler(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {

		switch (qName) {
		case TAG_OPTIONS: {
			getInstance().setOptionsManifest(((OptionsManifestXmlDelegate) handler).getInstance());
			maybeLinkProperties();
		} break;

		case TAG_DOCUMENTATION: {
			getInstance().setDocumentation(((DocumentationXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}

	}
}
