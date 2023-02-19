/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.standard.AbstractMemberManifest.PropertyImpl;
import de.ims.icarus2.model.manifest.standard.DocumentationImpl;
import de.ims.icarus2.model.manifest.standard.OptionsManifestImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;
import de.ims.icarus2.util.xml.XmlUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractMemberManifestXmlDelegate<M extends MemberManifest<M>>
		extends AbstractManifestXmlDelegate<M> {

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

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractManifestXmlDelegate#isEmpty(de.ims.icarus2.model.manifest.api.Manifest)
	 */
	@Override
	protected boolean isEmpty(M instance) {
		return super.isEmpty(instance)
				&& XmlUtils.isLegalAttribute(instance.getName())
				&& XmlUtils.isLegalAttribute(instance.getDescription());
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
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		M manifest = getInstance();

		// IMPORTANT: we must not write the ID field again, since super implementation took care of that!
		serializer.writeAttribute(ManifestXmlAttributes.NAME, manifest.getName());
		serializer.writeAttribute(ManifestXmlAttributes.DESCRIPTION, manifest.getDescription());
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
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		M manifest = getInstance();

		ManifestXmlUtils.writeIdentityFieldElements(serializer, manifest);

		// Write documentation
		if(manifest.getDocumentation().isPresent()) {
			getDocumentationXmlDelegate().reset(manifest.getDocumentation().get()).writeXml(serializer);
		}

		//TODO Assuming we do decide to make categories inheritable, the following needs to change!
		Set<Category> categories = manifest.getCategories();
		if(!categories.isEmpty()) {
			serializer.startElement(ManifestXmlTags.CATEGORIES);
			for(Category category : categories) {
				serializer.startEmptyElement(ManifestXmlTags.CATEGORY);
				ManifestXmlUtils.writeCategoryAttributes(serializer, category);
				ManifestXmlUtils.writeIdentityFieldElements(serializer, category);
				serializer.endElement(ManifestXmlTags.CATEGORY);
			}
			serializer.endElement(ManifestXmlTags.CATEGORIES);
		}

		// Write options manifest
		if(manifest.getOptionsManifest().isPresent()) {
			getOptionsManifestXmlDelegate().reset(manifest.getOptionsManifest().get()).writeXml(serializer);
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

				Optional<Object> value = property.getValue();

				if(!value.isPresent()) {
					continue;
				}

				if(property.isMultiValue()) {
					for(Object item : (Collection<?>) value.get()) {
						ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.VALUE, item, type);
					}
				} else {
					ManifestXmlUtils.writeValue(serializer, value, type);
				}

				serializer.endElement(ManifestXmlTags.PROPERTY);
			}

			serializer.endElement(ManifestXmlTags.PROPERTIES);
		}
	}

	private void maybeLinkProperties() {
		if(hasLocalOptions && localPropertyCount>0) {
			OptionsManifest optionsManifest = getInstance().getOptionsManifest()
					.orElseThrow(ManifestException.error("Missing options manifest"));
			for(String name : optionsManifest.getOptionIds()) {
				optionsManifest.getOption(name);

				// Gets only called by the parsing routines and therefore we
				// can safely cast to the implementation we use there.
				getInstance().getProperty(name)
					.filter(PropertyImpl.class::isInstance)
					.ifPresent(p -> optionsManifest
							.getOption(name)
							.ifPresent(o -> ((PropertyImpl)p).setOption(o)));
			}
		}
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		ManifestXmlHandler handler = null;

		switch (localName) {

		case ManifestXmlTags.NAME:
		case ManifestXmlTags.DESCRIPTION: {
			handler = this;
		} break;

		case ManifestXmlTags.OPTIONS: {
			hasLocalOptions = true;
			handler = getOptionsManifestXmlDelegate().reset(new OptionsManifestImpl(getInstance()));
		} break;

		case ManifestXmlTags.DOCUMENTATION: {
			handler = getDocumentationXmlDelegate().reset(new DocumentationImpl());
		} break;

		case ManifestXmlTags.CATEGORIES: {
			handler = this;
		} break;

		case ManifestXmlTags.CATEGORY: {
			Category category = ManifestXmlUtils.readCategory(attributes);
			getInstance().addCategory(category);
			handler = this;
		} break;

		case ManifestXmlTags.PROPERTIES: {
			handler = this;
		} break;

		case ManifestXmlTags.PROPERTY: {
			localPropertyCount++;
			String name = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.NAME)
					.orElseThrow(ManifestXmlHandler.error("Property name is missing"));
			ValueType valueType = ManifestXmlUtils.typeValue(attributes).orElse(ValueType.DEFAULT_VALUE_TYPE);
			property = new PropertyImpl(name, valueType);

			handler = this;
		} break;

		case ManifestXmlTags.VALUE: {
			checkEmptyOrMultiValue();
			property.setMultiValue(true);
			handler = this;
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return Optional.ofNullable(handler);
	}

	private void checkEmptyOrMultiValue() {
		Optional<Object> value = property.getValue();
		if(value.isPresent() && !(value.get() instanceof Collection))
			throw new IllegalStateException("Non-collection value already set");
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {

		ManifestXmlHandler handler = null;

		switch (localName) {

		case ManifestXmlTags.NAME: {
			getInstance().setName(text);
			handler = this;
		} break;

		case ManifestXmlTags.DESCRIPTION: {
			getInstance().setDescription(text);
			handler = this;
		} break;

		case ManifestXmlTags.CATEGORIES: {
			handler = this;
		} break;

		case ManifestXmlTags.CATEGORY: {
			handler = this;
		} break;

		case ManifestXmlTags.PROPERTIES: {
			maybeLinkProperties();
			handler = this;
		} break;

		case ManifestXmlTags.PROPERTY: {

			if(!property.isMultiValue()) {
				property.setValue(ManifestXmlUtils.parse(property.getValueType(), manifestLocation, text, true));
			}

			getInstance().addProperty(property);

			handler = this;
		} break;

		case ManifestXmlTags.VALUE: {
			Object value = ManifestXmlUtils.parse(property.getValueType(), manifestLocation, text, true);
			addValue(value);
			handler = this;
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return Optional.ofNullable(handler);
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
