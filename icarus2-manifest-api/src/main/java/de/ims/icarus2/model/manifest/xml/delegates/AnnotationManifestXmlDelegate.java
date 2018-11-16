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

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.AnnotationManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.data.ContentType;
import de.ims.icarus2.util.data.ContentTypeRegistry;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
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
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		AnnotationManifest manifest = getInstance();

		// Write key
		if(manifest.isLocalKey()) {
			serializer.writeAttribute(ManifestXmlAttributes.KEY, manifest.getKey());
		}

		// Write value type
		//TODO for now we ALWAYS serialize the (possibly) inherited type
//		if(manifest.isLocalValueType()) {
		serializer.writeAttribute(ManifestXmlAttributes.VALUE_TYPE, ManifestXmlUtils.getSerializedForm(manifest.getValueType()));
//		}

		Optional<ContentType> contentType = manifest.getContentType();
		if(manifest.isLocalContentType() && contentType.isPresent()) {
			serializer.writeAttribute(ManifestXmlAttributes.CONTENT_TYPE, contentType.get().getId());
		}

		if(manifest.isAllowUnknownValues()!=AnnotationManifest.DEFAULT_ALLOW_UNKNOWN_VALUES) {
			serializer.writeAttribute(ManifestXmlAttributes.ALLOW_UNKNOWN_VALUES, manifest.isAllowUnknownValues());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		AnnotationManifest manifest = getInstance();

		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.KEY)
			.ifPresent(manifest::setKey);
		ManifestXmlUtils.typeValue(attributes).ifPresent(manifest::setValueType);

		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CONTENT_TYPE)
			.map(type -> ContentTypeRegistry.getInstance().getType(type))
			.ifPresent(manifest::setContentType);

		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.ALLOW_UNKNOWN_VALUES)
			.ifPresent(manifest::setAllowUnknownValues);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		AnnotationManifest manifest = getInstance();

		// Write aliases
		for(String alias : manifest.getLocalAliases()) {
			ManifestXmlUtils.writeAliasElement(serializer, alias);
		}

		// Write values
		if(manifest.isLocalValueSet()) {
			getValueSetXmlDelegate().reset(manifest.getValueSet().get()).writeXml(serializer);
		}

		// Write range
		if(manifest.isLocalValueRange()) {
			getValueRangeXmlDelegate().reset(manifest.getValueRange().get()).writeXml(serializer);
		}

		if(manifest.isLocalNoEntryValue()) {
			ManifestXmlUtils.writeValueElement(serializer, ManifestXmlTags.NO_ENTRY_VALUE,
					manifest.getNoEntryValue().orElse(null), manifest.getValueType());
		}
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.ANNOTATION: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.ALIAS: {
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.NAME)
				.ifPresent(getInstance()::addAlias);
		} break;

		case ManifestXmlTags.NO_ENTRY_VALUE: {
			// no-op
		} break;

		case ManifestXmlTags.VALUE_SET: {
			handler = getValueSetXmlDelegate().reset(getInstance().getValueType());
		} break;

		case ManifestXmlTags.VALUE_RANGE: {
			handler = getValueRangeXmlDelegate().reset(getInstance().getValueType());
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
		case ManifestXmlTags.ANNOTATION: {
			handler = null;
		} break;

		case ManifestXmlTags.ALIAS: {
			// no-op
		} break;

		case ManifestXmlTags.NO_ENTRY_VALUE: {
			getInstance().setNoEntryValue(getInstance().getValueType().parseAndPersist(text, manifestLocation.getClassLoader()));
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return Optional.ofNullable(handler);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {

		switch (localName) {
		case ManifestXmlTags.VALUE_SET: {
			getInstance().setValueSet(((ValueSetXmlDelegate) handler).getInstance());
		} break;

		case ManifestXmlTags.VALUE_RANGE: {
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
		return ManifestXmlTags.ANNOTATION;
	}
}
