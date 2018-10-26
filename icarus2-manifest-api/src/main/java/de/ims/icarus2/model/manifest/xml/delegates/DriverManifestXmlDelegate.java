/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.standard.DocumentationImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl.ModuleManifestImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl.ModuleSpecImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class DriverManifestXmlDelegate extends AbstractForeignImplementationManifestXmlDelegate<DriverManifest> {

	private MappingManifestXmlDelegate mappingManifestXmlDelegate;
	private ModuleSpecXmlDelegate moduleSpecXmlDelegate;
	private ModuleManifestXmlDelegate moduleManifestXmlDelegate;

	private MappingManifestXmlDelegate getMappingManifestXmlDelegate() {
		if(mappingManifestXmlDelegate==null) {
			mappingManifestXmlDelegate = new MappingManifestXmlDelegate();
		}
		return mappingManifestXmlDelegate;
	}

	private ModuleSpecXmlDelegate getModuleSpecXmlDelegate() {
		if(moduleSpecXmlDelegate==null) {
			moduleSpecXmlDelegate = new ModuleSpecXmlDelegate();
		}
		return moduleSpecXmlDelegate;
	}

	private ModuleManifestXmlDelegate getModuleManifestXmlDelegate() {
		if(moduleManifestXmlDelegate==null) {
			moduleManifestXmlDelegate = new ModuleManifestXmlDelegate();
		}
		return moduleManifestXmlDelegate;
	}

	public DriverManifestXmlDelegate reset(ContextManifest contextManifest) {
		reset();
		setInstance(new DriverManifestImpl(contextManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractMemberManifestXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(mappingManifestXmlDelegate!=null) {
			mappingManifestXmlDelegate.reset();
		}

		if(moduleSpecXmlDelegate!=null) {
			moduleSpecXmlDelegate.reset();
		}

		if(moduleManifestXmlDelegate!=null) {
			moduleManifestXmlDelegate.reset();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		DriverManifest manifest = getInstance();

		if(manifest.isLocalLocationType()) {
			serializer.writeAttribute(ManifestXmlAttributes.LOCATION_TYPE,
					manifest.getLocationType().map(LocationType::getStringValue));
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		DriverManifest manifest = getInstance();

		// Write module specs
		for(ModuleSpec moduleSpec : manifest.getLocalModuleSpecs()) {
			getModuleSpecXmlDelegate().reset(moduleSpec).writeXml(serializer);
		}

		// Write module manifests
		for(ModuleManifest moduleManifest : manifest.getLocalModuleManifests()) {
			getModuleManifestXmlDelegate().reset(moduleManifest).writeXml(serializer);
		}

		// Write index manifests
		for(MappingManifest mappingManifest : manifest.getLocalMappingManifests()) {
			getMappingManifestXmlDelegate().reset(mappingManifest).writeXml(serializer);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LOCATION_TYPE)
			.map(LocationType::parseLocationType)
			.ifPresent(getInstance()::setLocationType);
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.DRIVER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.MAPPING: {
			handler = getMappingManifestXmlDelegate().reset(getInstance());
		} break;

		case ManifestXmlTags.MODULE_SPEC: {
			handler = getModuleSpecXmlDelegate().reset(getInstance());
		} break;

		case ManifestXmlTags.MODULE: {
			handler = getModuleManifestXmlDelegate().reset(getInstance());
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
		case ManifestXmlTags.DRIVER: {
			handler = null;
		} break;

		case ManifestXmlTags.MODULE_SPEC: {
			// no-op
		} break;

		case ManifestXmlTags.MODULE: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
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

		case ManifestXmlTags.MAPPING: {
			getInstance().addMappingManifest(((MappingManifestXmlDelegate) handler).getInstance());
		} break;

		case ManifestXmlTags.MODULE_SPEC: {
			getInstance().addModuleSpec(((ModuleSpecXmlDelegate) handler).getInstance());
		} break;

		case ManifestXmlTags.MODULE: {
			getInstance().addModuleManifest(((ModuleManifestXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.DRIVER;
	}

	public static class ModuleSpecXmlDelegate extends AbstractXmlDelegate<ModuleSpec> {

		private DocumentationXmlDelegate documentationXmlDelegate;

		private DocumentationXmlDelegate getDocumentationXmlDelegate() {
			if(documentationXmlDelegate==null) {
				documentationXmlDelegate = new DocumentationXmlDelegate();
			}
			return documentationXmlDelegate;
		}

		public ModuleSpecXmlDelegate reset(DriverManifest driverManifest) {
			reset();
			setInstance(new ModuleSpecImpl(driverManifest));

			return this;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
		 */
		@Override
		public void reset() {
			super.reset();

			if(documentationXmlDelegate!=null) {
				documentationXmlDelegate.reset();
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
		 */
		@Override
		public void writeXml(XmlSerializer serializer) throws XMLStreamException {

			ModuleSpec spec = getInstance();

			Optional<String> extensionPointUid = spec.getExtensionPointUid();

			if(!extensionPointUid.isPresent()) {
				serializer.startEmptyElement(ManifestXmlTags.MODULE_SPEC);
			} else {
				serializer.startElement(ManifestXmlTags.MODULE_SPEC);
			}

			// ATTRIBUTES

			ManifestXmlUtils.writeIdentityAttributes(serializer, spec);

			if(spec.isCustomizable()!=ModuleSpec.DEFAULT_IS_CUSTOMIZABLE) {
				serializer.writeAttribute(ManifestXmlAttributes.CUSTOMIZABLE, spec.isCustomizable());
			}

			if(spec.getMultiplicity()!=ModuleSpec.DEFAULT_MULTIPLICITY) {
				serializer.writeAttribute(ManifestXmlAttributes.MULTIPLICITY, spec.getMultiplicity().getStringValue());
			}

			// ELEMENTS

			if(extensionPointUid.isPresent()) {
				serializer.startElement(ManifestXmlTags.EXTENSION_POINT);
				serializer.writeTextOrCData(extensionPointUid);
				serializer.endElement(ManifestXmlTags.EXTENSION_POINT);
			}

			serializer.endElement(ManifestXmlTags.MODULE_SPEC);
		}

		/**
		 * @param attributes
		 */
		protected void readAttributes(Attributes attributes) {

			ModuleSpec spec = getInstance();

			ManifestXmlUtils.readIdentityAttributes(attributes, spec);

			ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.CUSTOMIZABLE)
				.ifPresent(spec::setCustomizable);
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.MULTIPLICITY)
				.map(Multiplicity::parseMultiplicity)
				.ifPresent(spec::setMultiplicity);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			ManifestXmlHandler handler = this;

			switch (localName) {
			case ManifestXmlTags.MODULE_SPEC: {
				readAttributes(attributes);
			} break;

			case ManifestXmlTags.NAME:
			case ManifestXmlTags.DESCRIPTION:
			case ManifestXmlTags.ICON:
				break;

			case ManifestXmlTags.DOCUMENTATION: {
				handler = getDocumentationXmlDelegate().reset(new DocumentationImpl());
			} break;

			case ManifestXmlTags.EXTENSION_POINT: {
				// no-op
			} break;

			case ManifestXmlTags.CATEGORIES: {
				// no-op
			} break;

			case ManifestXmlTags.CATEGORY: {
				Category category = ManifestXmlUtils.readCategory(attributes);
				getInstance().addCategory(category);
			} break;

			default:
				throw new UnexpectedTagException(qName, true, ManifestXmlTags.MODULE_SPEC);
			}

			return Optional.ofNullable(handler);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
				throws SAXException {
			ManifestXmlHandler handler = this;

			switch (localName) {
			case ManifestXmlTags.MODULE_SPEC: {
				handler = null;
			} break;

			case ManifestXmlTags.EXTENSION_POINT: {
				getInstance().setExtensionPointUid(text);
			} break;

			case ManifestXmlTags.CATEGORIES:
			case ManifestXmlTags.CATEGORY:
				break;

			case ManifestXmlTags.NAME: {
				getInstance().setName(text);
			} break;

			case ManifestXmlTags.DESCRIPTION: {
				getInstance().setDescription(text);
			} break;

			case ManifestXmlTags.ICON: {
				getInstance().setIcon(ManifestXmlUtils.iconValue(text, true).get());
			} break;

			default:
				throw new UnexpectedTagException(qName, false, ManifestXmlTags.MODULE_SPEC);
			}

			return Optional.ofNullable(handler);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
		 */
		@Override
		public void endNestedHandler(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				ManifestXmlHandler handler) throws SAXException {
			switch (localName) {

			case ManifestXmlTags.DOCUMENTATION: {
				getInstance().setDocumentation(((DocumentationXmlDelegate) handler).getInstance());
			} break;

			default:
				throw new UnsupportedNestingException(qName, ManifestXmlTags.MODULE_SPEC);
			}
		}
	}

	public static class ModuleManifestXmlDelegate extends AbstractForeignImplementationManifestXmlDelegate<ModuleManifest> {

		public ModuleManifestXmlDelegate reset(DriverManifest driverManifest) {
			reset();
			setInstance(new ModuleManifestImpl(driverManifest));

			return this;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
		 */
		@Override
		protected void readAttributes(Attributes attributes) {
			super.readAttributes(attributes);

			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.MODULE_SPEC_ID)
				.ifPresent(getInstance()::setModuleSpecId);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
		 */
		@Override
		protected void writeAttributes(XmlSerializer serializer)
				throws XMLStreamException {
			super.writeAttributes(serializer);

			ModuleManifest manifest = getInstance();

			if(manifest.getModuleSpec()!=null) {
				serializer.writeAttribute(ManifestXmlAttributes.MODULE_SPEC_ID,
						manifest.getModuleSpec().flatMap(ModuleSpec::getId));
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
		 */
		@Override
		protected String xmlTag() {
			return ManifestXmlTags.MODULE;
		}
	}
}
