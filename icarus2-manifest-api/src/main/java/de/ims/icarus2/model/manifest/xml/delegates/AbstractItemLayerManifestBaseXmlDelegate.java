/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.AbstractItemLayerManifestBase;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractItemLayerManifestBaseXmlDelegate<M extends ItemLayerManifestBase<M>>
		extends AbstractLayerManifestXmlDelegate<M> {

	private ContainerManifestXmlDelegate containerManifestXmlDelegate;

	protected ContainerManifestXmlDelegate getContainerManifestXmlDelegate() {
		if(containerManifestXmlDelegate==null) {
			containerManifestXmlDelegate = new ContainerManifestXmlDelegate();
		}

		return containerManifestXmlDelegate;
	}

	protected AbstractItemLayerManifestBaseXmlDelegate() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(containerManifestXmlDelegate!=null) {
			containerManifestXmlDelegate.reset();
		}
	}

	protected void defaultWriteElements(XmlSerializer serializer) throws XMLStreamException {

		M manifest = getInstance();

		if(manifest.isLocalBoundaryLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer,
					ManifestXmlTags.BOUNDARY_LAYER, manifest.getBoundaryLayerManifest().get());
		}

		if(manifest.isLocalFoundationLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer,
					ManifestXmlTags.FOUNDATION_LAYER, manifest.getFoundationLayerManifest().get());
		}

		if(manifest.hasLocalContainerHierarchy()) {
			serializer.startElement(ManifestXmlTags.HIERARCHY);
			for(ContainerManifestBase<?> containerManifest : manifest.getContainerHierarchy()
					.orElse(Hierarchy.empty())) {
				writeContainerElement(serializer, containerManifest);
			}
			serializer.endElement(ManifestXmlTags.HIERARCHY);
		}
	}


	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		defaultWriteElements(serializer);
	}

	protected void writeContainerElement(XmlSerializer serializer, ContainerManifestBase<?> containerManifest) throws XMLStreamException {
		if(containerManifest.getManifestType()==ManifestType.CONTAINER_MANIFEST) {
			getContainerManifestXmlDelegate().reset((ContainerManifest)containerManifest).writeXml(serializer);
		} else
			throw new XMLStreamException("Unsupported container class: "+containerManifest.getClass());
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.ITEM_LAYER: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.BOUNDARY_LAYER: {
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID)
				.ifPresent(getInstance()::setAndGetBoundaryLayer);
		} break;

		case ManifestXmlTags.FOUNDATION_LAYER: {
			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID)
				.ifPresent(getInstance()::setAndGetFoundationLayer);
		} break;

		case ManifestXmlTags.HIERARCHY: {
			AbstractItemLayerManifestBase.getOrCreateLocalContainerhierarchy(getInstance());
		} break;

		case ManifestXmlTags.CONTAINER: {
			handler = getContainerManifestXmlDelegate().reset(getInstance());
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
		case ManifestXmlTags.ITEM_LAYER: {
			handler = null;
		} break;

		case ManifestXmlTags.BOUNDARY_LAYER: {
			// no-op
		} break;

		case ManifestXmlTags.FOUNDATION_LAYER: {
			// no-op
		} break;

		case ManifestXmlTags.HIERARCHY: {
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
			defaultAddContainerManifest(this, ((ContainerManifestXmlDelegate)handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	public static <L extends ItemLayerManifestBase<L>> void defaultAddContainerManifest(
			ManifestXmlDelegate<L> delegate, ContainerManifestBase<?> containerManifest) {
		delegate.getInstance().getContainerHierarchy().get().add(containerManifest);
	}
}
