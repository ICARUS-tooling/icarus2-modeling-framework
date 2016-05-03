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

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.model.manifest.api.DriverManifest.Multiplicity;
import de.ims.icarus2.model.manifest.standard.DocumentationImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl.ModuleManifestImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl.ModuleSpecImpl;
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
public class DriverManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<DriverManifest> {

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
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		DriverManifest manifest = getInstance();

		if(manifest.isLocalLocationType()) {
			serializer.writeAttribute(ATTR_LOCATION_TYPE, manifest.getLocationType().getStringValue());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
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

		String locationType = ManifestXmlUtils.normalize(attributes, ATTR_LOCATION_TYPE);
		if(locationType!=null) {
			getInstance().setLocationType(LocationType.parseLocationType(locationType));
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_DRIVER: {
			readAttributes(attributes);
		} break;

		case TAG_MAPPING: {
			return getMappingManifestXmlDelegate().reset(getInstance());
		}

		case TAG_MODULE_SPEC: {
			return getModuleSpecXmlDelegate().reset(getInstance());
		}

		case TAG_MODULE: {
			return getModuleManifestXmlDelegate().reset(getInstance());
		}

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
		case TAG_DRIVER: {
			return null;
		}

		case TAG_MODULE_SPEC: {
			// no-op
		} break;

		case TAG_MODULE: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
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

		case TAG_MAPPING: {
			getInstance().addMappingManifest(((MappingManifestXmlDelegate) handler).getInstance());
		} break;

		case TAG_MODULE_SPEC: {
			getInstance().addModuleSpec(((ModuleSpecXmlDelegate) handler).getInstance());
		} break;

		case TAG_MODULE: {
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
		return TAG_DRIVER;
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
		public void writeXml(XmlSerializer serializer) throws Exception {

			ModuleSpec spec = getInstance();

			String extensionPointUid = spec.getExtensionPointUid();

			if(extensionPointUid==null) {
				serializer.startEmptyElement(TAG_MODULE_SPEC);
			} else {
				serializer.startElement(TAG_MODULE_SPEC);
			}

			// ATTRIBUTES

			ManifestXmlUtils.writeIdentityAttributes(serializer, spec);

			if(spec.isCustomizable()!=ModuleSpec.DEFAULT_IS_CUSTOMIZABLE) {
				serializer.writeAttribute(ATTR_CUSTOMIZABLE, spec.isCustomizable());
			}

			if(spec.getMultiplicity()!=ModuleSpec.DEFAULT_MULTIPLICITY) {
				serializer.writeAttribute(ATTR_MULTIPLICITY, spec.getMultiplicity().getStringValue());
			}

			// ELEMENTS

			if(extensionPointUid!=null) {
				serializer.startElement(TAG_EXTENSION_POINT);
				serializer.writeText(extensionPointUid);
				serializer.endElement(TAG_EXTENSION_POINT);
			}

			serializer.endElement(TAG_MODULE_SPEC);
		}

		/**
		 * @param attributes
		 */
		protected void readAttributes(Attributes attributes) {

			ModuleSpec spec = getInstance();

			ManifestXmlUtils.readIdentity(attributes, spec);

			String customizable = ManifestXmlUtils.normalize(attributes, ATTR_CUSTOMIZABLE);
			if(customizable!=null) {
				spec.setCustomizable(Boolean.parseBoolean(customizable));
			}

			String multiplicity = ManifestXmlUtils.normalize(attributes, ATTR_MULTIPLICITY);
			if(multiplicity!=null) {
				spec.setMultiplicity(Multiplicity.parseMultiplicity(multiplicity));
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			switch (qName) {
			case TAG_MODULE_SPEC: {
				readAttributes(attributes);
			} break;

			case TAG_DOCUMENTATION: {
				return getDocumentationXmlDelegate().reset(new DocumentationImpl());
			}

			case TAG_EXTENSION_POINT: {
				// no-op
			} break;

			default:
				throw new UnexpectedTagException(qName, true, TAG_MODULE_SPEC);
			}

			return this;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_MODULE_SPEC: {
				return null;
			}

			case TAG_EXTENSION_POINT: {
				getInstance().setExtensionPointUid(text);
			} break;

			default:
				throw new UnexpectedTagException(qName, false, TAG_MODULE_SPEC);
			}

			return this;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
		 */
		@Override
		public void endNestedHandler(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				ManifestXmlHandler handler) throws SAXException {
			switch (qName) {

			case TAG_DOCUMENTATION: {
				getInstance().setDocumentation(((DocumentationXmlDelegate) handler).getInstance());
			} break;

			default:
				throw new UnsupportedNestingException(qName, TAG_MODULE_SPEC);
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

			String moduleSpecId = ManifestXmlUtils.normalize(attributes, ATTR_MODULE_SPEC_ID);
			if(moduleSpecId!=null) {
				getInstance().setModuleSpecId(moduleSpecId);
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
		 */
		@Override
		protected void writeAttributes(XmlSerializer serializer)
				throws Exception {
			super.writeAttributes(serializer);

			ModuleManifest manifest = getInstance();

			if(manifest.getModuleSpec()!=null) {
				serializer.writeAttribute(ATTR_MODULE_SPEC_ID, manifest.getModuleSpec().getId());
			}
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
		 */
		@Override
		protected String xmlTag() {
			return TAG_MODULE;
		}
	}
}
