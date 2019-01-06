/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.AnnotationLayerManifestImpl;
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
public class AnnotationLayerManifestXmlDelegate extends AbstractLayerManifestXmlDelegate<AnnotationLayerManifest> {

	private AnnotationManifestXmlDelegate annotationManifestXmlDelegate;

	public AnnotationLayerManifestXmlDelegate() {
		// no-op
	}

	public AnnotationLayerManifestXmlDelegate(AnnotationLayerManifest manifest) {
		setInstance(manifest);
	}

	public AnnotationLayerManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(new AnnotationLayerManifestImpl(groupManifest));
	}

	private AnnotationManifestXmlDelegate getAnnotationManifestXmlDelegate() {
		if(annotationManifestXmlDelegate==null) {
			annotationManifestXmlDelegate = new AnnotationManifestXmlDelegate();
		}

		return annotationManifestXmlDelegate;
	}

	public AnnotationLayerManifestXmlDelegate reset(LayerGroupManifest groupManifest) {
		reset();
		setInstance(new AnnotationLayerManifestImpl(groupManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(annotationManifestXmlDelegate!=null) {
			annotationManifestXmlDelegate.reset();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		AnnotationLayerManifest manifest = getInstance();

		// Write default key
		if(manifest.isLocalDefaultKey()) {
			serializer.writeAttribute(ManifestXmlAttributes.DEFAULT_KEY, manifest.getDefaultKey());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		AnnotationLayerManifest manifest = getInstance();

		// Read default key
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.DEFAULT_KEY)
			.ifPresent(manifest::setDefaultKey);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		AnnotationLayerManifest manifest = getInstance();

		// Write reference layers
		for(TargetLayerManifest layerManifest : getInstance().getLocalReferenceLayerManifests()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, ManifestXmlTags.REFERENCE_LAYER, layerManifest);
		}

		// Write annotation flags
		for(AnnotationFlag flag : manifest.getLocalActiveAnnotationFlags()) {
			serializer.startElement(ManifestXmlTags.ANNOTATION_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(ManifestXmlTags.ANNOTATION_FLAG);
		}

		// Write annotation manifests
		List<AnnotationManifest> sortedAnnotationManifests = CollectionUtils.asSortedList(
				manifest.getLocalAnnotationManifests(),
				Identity.ID_COMPARATOR);

		for(AnnotationManifest annotationManifest : sortedAnnotationManifests) {
			getAnnotationManifestXmlDelegate().reset(annotationManifest).writeXml(serializer);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.ANNOTATION_LAYER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.REFERENCE_LAYER: {
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID)
				.ifPresent(getInstance()::addAndGetReferenceLayer);
		} break;

		case ManifestXmlTags.ANNOTATION: {
			handler = getAnnotationManifestXmlDelegate().reset(getInstance());
		} break;

		case ManifestXmlTags.ANNOTATION_FLAG: {
			// no-op;
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return Optional.ofNullable(handler);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {

		ManifestXmlHandler handler = null;

		switch (localName) {
		case ManifestXmlTags.ANNOTATION_LAYER: {
			// no-op
		} break;

		case ManifestXmlTags.REFERENCE_LAYER: {
			handler = this;
		} break;

		case ManifestXmlTags.ANNOTATION_FLAG: {
			getInstance().setAnnotationFlag(AnnotationFlag.parseAnnotationFlag(text), true);
			handler = this;
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
		case ManifestXmlTags.ANNOTATION: {
			getInstance().addAnnotationManifest(((AnnotationManifestXmlDelegate) handler).getInstance());
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
		return ManifestXmlTags.ANNOTATION_LAYER;
	}
}
