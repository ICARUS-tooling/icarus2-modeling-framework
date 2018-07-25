/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
import java.util.List;

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
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
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
		String defaultKey = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.DEFAULT_KEY);
		if(defaultKey!=null) {
			manifest.setDefaultKey(defaultKey);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		AnnotationLayerManifest manifest = getInstance();

		// Write reference layers
		for(TargetLayerManifest layerManifest : getInstance().getLocalReferenceLayerManifests()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, ManifestXmlTags.REFERENCE_LAYER, layerManifest);
		}

		// Write annotation manifests
		List<AnnotationManifest> sortedAnnotationManifests = new ArrayList<>(manifest.getLocalAnnotationManifests());
		sortedAnnotationManifests.sort((a1, a2) -> a1.getId().compareTo(a2.getId()));

		for(AnnotationManifest annotationManifest : sortedAnnotationManifests) {
			getAnnotationManifestXmlDelegate().reset(annotationManifest).writeXml(serializer);
		}

		for(AnnotationFlag flag : manifest.getLocalActiveAnnotationFlags()) {
			serializer.startElement(ManifestXmlTags.ANNOTATION_FLAG);
			serializer.writeText(flag.getStringValue());
			serializer.endElement(ManifestXmlTags.ANNOTATION_FLAG);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (localName) {
		case ManifestXmlTags.ANNOTATION_LAYER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.REFERENCE_LAYER: {
			String referenceLayerId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID);
			getInstance().addReferenceLayerId(referenceLayerId);
		} break;

		case ManifestXmlTags.ANNOTATION: {
			return getAnnotationManifestXmlDelegate().reset(getInstance());
		}

		case ManifestXmlTags.ANNOTATION_FLAG: {
			// no-op;
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (localName) {
		case ManifestXmlTags.ANNOTATION_LAYER: {
			return null;
		}

		case ManifestXmlTags.REFERENCE_LAYER: {
			return this;
		}

		case ManifestXmlTags.ANNOTATION_FLAG: {
			getInstance().setAnnotationFlag(AnnotationFlag.parseAnnotationFlag(text), true);
			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
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
