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

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.AnnotationManifestImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.xml.XmlSerializer;
import de.ims.icarus2.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<AnnotationManifest> {

	private ValueSetXmlDelegate valueSetXmlDelegate;
	private ValueRangeXmlDelegate valueRangeXmlDelegate;

	public AnnotationManifestXmlDelegate() {
		// no-op
	}

	public AnnotationManifestXmlDelegate(AnnotationManifest manifest) {
		setInstance(manifest);
	}

	public AnnotationManifestXmlDelegate(AnnotationLayerManifest layerManifest) {
		setInstance(new AnnotationManifestImpl(layerManifest));
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

	public AnnotationManifestXmlDelegate reset(AnnotationLayerManifest layerManifest) {
		reset();
		setInstance(new AnnotationManifestImpl(layerManifest));

		return this;
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
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#writeAttributes(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		AnnotationManifest manifest = getInstance();

		// Write key
		if(manifest.isLocalKey()) {
			serializer.writeAttribute(ATTR_KEY, manifest.getKey());
		}

		// Write value type
		//TODO for now we ALWAYS serialize the (possibly) inherited type
//		if(manifest.isLocalValueType()) {
			serializer.writeAttribute(ATTR_VALUE_TYPE, ManifestXmlUtils.getSerializedForm(manifest.getValueType()));
//		}

		if(manifest.isLocalContentType()) {
			serializer.writeAttribute(ATTR_CONTENT_TYPE, manifest.getContentType().getId());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		AnnotationManifest manifest = getInstance();

		String key = ManifestXmlUtils.normalize(attributes, ATTR_KEY);
		if(key!=null) {
			manifest.setKey(key);
		}

		ValueType valueType = ManifestXmlUtils.typeValue(attributes);
		if(valueType!=null) {
			manifest.setValueType(valueType);
		}

		String contentTypeId = ManifestXmlUtils.normalize(attributes, ATTR_CONTENT_TYPE);
		if(contentTypeId!=null) {
			manifest.setContentType(ContentTypeRegistry.getInstance().getType(contentTypeId));
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		AnnotationManifest manifest = getInstance();

		// Write aliases
		for(String alias : manifest.getLocalAliases()) {
			ManifestXmlUtils.writeAliasElement(serializer, alias);
		}

		// Write values
		if(manifest.isLocalValueSet()) {
			getValueSetXmlDelegate().reset(manifest.getValueSet()).writeXml(serializer);
		}

		// Write range
		if(manifest.isLocalValueRange()) {
			getValueRangeXmlDelegate().reset(manifest.getValueRange()).writeXml(serializer);
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_ANNOTATION: {
			readAttributes(attributes);
		} break;

		case TAG_ALIAS: {
			getInstance().addAlias(ManifestXmlUtils.normalize(attributes, ATTR_NAME));
		} break;

		case TAG_NO_ENTRY_VALUE: {
			// no-op
		} break;

		case TAG_VALUE_SET: {
			return getValueSetXmlDelegate().reset(getInstance().getValueType());
		}

		case TAG_VALUE_RANGE: {
			return getValueRangeXmlDelegate().reset(getInstance().getValueType());
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
		case TAG_ANNOTATION: {
			return null;
		}

		case TAG_ALIAS: {
			// no-op
		} break;

		case TAG_NO_ENTRY_VALUE: {
			getInstance().setNoEntryValue(getInstance().getValueType().parse(text, manifestLocation.getClassLoader()));
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {

		switch (qName) {
		case TAG_VALUE_SET: {
			getInstance().setValueSet(((ValueSetXmlDelegate) handler).getInstance());
		} break;

		case TAG_VALUE_RANGE: {
			getInstance().setValueRange(((ValueRangeXmlDelegate) handler).getInstance());
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
		return TAG_ANNOTATION;
	}
}
