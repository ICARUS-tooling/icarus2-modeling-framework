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

import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.FragmentLayerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class FragmentLayerManifestXmlDelegate extends AbstractLayerManifestXmlDelegate<FragmentLayerManifest> {

	private RasterizerManifestXmLDelegate rasterizerManifestXmLDelegate;
	private ContainerManifestXmlDelegate containerManifestXmlDelegate;

	public FragmentLayerManifestXmlDelegate() {
		// no-op
	}

	public FragmentLayerManifestXmlDelegate(FragmentLayerManifest manifest) {
		setInstance(manifest);
	}

	public FragmentLayerManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(new FragmentLayerManifestImpl(groupManifest));
	}

	private RasterizerManifestXmLDelegate getRasterizerManifestXmLDelegate() {
		if(rasterizerManifestXmLDelegate==null) {
			rasterizerManifestXmLDelegate = new RasterizerManifestXmLDelegate();
		}

		return rasterizerManifestXmLDelegate;
	}

	private ContainerManifestXmlDelegate getContainerManifestXmlDelegate() {
		if(containerManifestXmlDelegate==null) {
			containerManifestXmlDelegate = new ContainerManifestXmlDelegate();
		}

		return containerManifestXmlDelegate;
	}

	public FragmentLayerManifestXmlDelegate reset(LayerGroupManifest groupManifest) {
		reset();
		setInstance(new FragmentLayerManifestImpl(groupManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(rasterizerManifestXmLDelegate!=null) {
			rasterizerManifestXmLDelegate.reset();
		}

		if(containerManifestXmlDelegate!=null) {
			containerManifestXmlDelegate.reset();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.ANNOTATION_KEY)
			.ifPresent(getInstance()::setAnnotationKey);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		FragmentLayerManifest manifest = getInstance();

		if(manifest.isLocalAnnotationKey()) {
			serializer.writeAttribute(ManifestXmlAttributes.ANNOTATION_KEY, manifest.getAnnotationKey());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		ItemLayerManifestXmlDelegate.defaultWriteElements(this, getContainerManifestXmlDelegate(), serializer);

		FragmentLayerManifest manifest = getInstance();

		if(manifest.isLocalValueLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer,
					ManifestXmlTags.VALUE_LAYER, manifest.getValueLayerManifest().get());
		}

		if(manifest.isLocalRasterizerManifest()) {
			getRasterizerManifestXmLDelegate().reset(manifest.getRasterizerManifest().get()).writeXml(serializer);
		}
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.FRAGMENT_LAYER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.BOUNDARY_LAYER: {
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID)
				.ifPresent(getInstance()::setBoundaryLayerId);
		} break;

		case ManifestXmlTags.FOUNDATION_LAYER: {
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID)
				.ifPresent(getInstance()::setFoundationLayerId);
		} break;

		case ManifestXmlTags.CONTAINER: {
			handler = getContainerManifestXmlDelegate().reset(getInstance());
		} break;

		case ManifestXmlTags.VALUE_LAYER: {
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID)
				.ifPresent(getInstance()::setValueLayerId);
		} break;

		case ManifestXmlTags.RASTERIZER: {
			handler = getRasterizerManifestXmLDelegate().reset(getInstance());
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
		case ManifestXmlTags.FRAGMENT_LAYER: {
			handler = null;
		} break;

		case ManifestXmlTags.BOUNDARY_LAYER: {
			// no-op
		} break;

		case ManifestXmlTags.FOUNDATION_LAYER: {
			// no-op
		} break;

		case ManifestXmlTags.VALUE_LAYER: {
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

		case ManifestXmlTags.CONTAINER: {
			ItemLayerManifestXmlDelegate.defaultAddContainerManifest(this,
					((ContainerManifestXmlDelegate)handler).getInstance());
		} break;

		case ManifestXmlTags.RASTERIZER: {
			getInstance().setRasterizerManifest(((RasterizerManifestXmLDelegate) handler).getInstance());
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
		return ManifestXmlTags.FRAGMENT_LAYER;
	}
}
