/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.readFlag;
import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.writeFlag;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.LayerGroupManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class LayerGroupManifestXmlDelegate extends AbstractXmlDelegate<LayerGroupManifest> {

	@SuppressWarnings("rawtypes")
	private static final Map<ManifestType, Supplier<? extends AbstractLayerManifestXmlDelegate>>
			layerDelegateSuppliers = new Object2ObjectOpenHashMap<>();
	static {
		layerDelegateSuppliers.put(ManifestType.ITEM_LAYER_MANIFEST, ItemLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.STRUCTURE_LAYER_MANIFEST, StructureLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.ANNOTATION_LAYER_MANIFEST, AnnotationLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.FRAGMENT_LAYER_MANIFEST, FragmentLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.HIGHLIGHT_LAYER_MANIFEST, HighlightLayerManifestXmlDelegate::new);
	}

	private Map<ManifestType, AbstractLayerManifestXmlDelegate<?>> layerDelegates = new Object2ObjectOpenHashMap<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <D extends AbstractLayerManifestXmlDelegate> D getLayerDelegate(ManifestType type) {
		AbstractLayerManifestXmlDelegate<?> delegate = layerDelegates.get(type);

		if(delegate==null) {
			Supplier<? extends AbstractLayerManifestXmlDelegate> supplier = layerDelegateSuppliers.get(type);
			if(supplier==null)
				throw new IllegalArgumentException("Not a valid layer manifest type: "+type);

			delegate = supplier.get();
		}

		return (D) delegate;
	}

	public LayerGroupManifestXmlDelegate() {
		// no-op
	}

	public LayerGroupManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(groupManifest);
	}

	public LayerGroupManifestXmlDelegate(ContextManifest contextManifest) {
		setInstance(new LayerGroupManifestImpl(contextManifest));
	}

	public LayerGroupManifestXmlDelegate reset(ContextManifest contextManifest) {
		reset();
		setInstance(new LayerGroupManifestImpl(contextManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void writeXml(XmlSerializer serializer) throws XMLStreamException {
		serializer.startElement(ManifestXmlTags.LAYER_GROUP);

		LayerGroupManifest manifest = getInstance();

		ManifestXmlUtils.writeIdentityAttributes(serializer, manifest);

		writeFlag(serializer, ManifestXmlAttributes.INDEPENDENT, manifest.isIndependent(), LayerGroupManifest.DEFAULT_INDEPENDENT_VALUE);

		if(manifest.getPrimaryLayerManifest()!=null) {
			serializer.writeAttribute(ManifestXmlAttributes.PRIMARY_LAYER,
					manifest.getPrimaryLayerManifest().flatMap(Identity::getId));
		}

		ManifestXmlUtils.writeIdentityFieldElements(serializer, manifest);

		for(Iterator<LayerManifest<?>> it = manifest.getLayerManifests().iterator(); it.hasNext();) {
			LayerManifest<?> layerManifest = it.next();

			getLayerDelegate(layerManifest.getManifestType()).reset(layerManifest).writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}

		serializer.endElement(ManifestXmlTags.LAYER_GROUP);
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.LAYER_GROUP: {
			LayerGroupManifest manifest = getInstance();

			ManifestXmlUtils.readIdentityAttributes(attributes, manifest);

			readFlag(attributes, ManifestXmlAttributes.INDEPENDENT, LayerGroupManifest.DEFAULT_INDEPENDENT_VALUE)
				.ifPresent(manifest::setIndependent);

			ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.PRIMARY_LAYER)
				.ifPresent(manifest::setPrimaryLayerId);
		} break;

		case ManifestXmlTags.NAME:
		case ManifestXmlTags.DESCRIPTION: {
			handler = this;
		} break;

		case ManifestXmlTags.ITEM_LAYER : {
			ItemLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.ITEM_LAYER_MANIFEST);
			handler = delegate.reset(getInstance());
		} break;

		case ManifestXmlTags.STRUCTURE_LAYER : {
			StructureLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.STRUCTURE_LAYER_MANIFEST);
			handler = delegate.reset(getInstance());
		} break;

		case ManifestXmlTags.ANNOTATION_LAYER : {
			AnnotationLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.ANNOTATION_LAYER_MANIFEST);
			handler = delegate.reset(getInstance());
		} break;

		case ManifestXmlTags.FRAGMENT_LAYER : {
			FragmentLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.FRAGMENT_LAYER_MANIFEST);
			handler = delegate.reset(getInstance());
		} break;

		case ManifestXmlTags.HIGHLIGHT_LAYER : {
			HighlightLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.HIGHLIGHT_LAYER_MANIFEST);
			handler = delegate.reset(getInstance());
		} break;

		default:
			throw new UnexpectedTagException(qName, true, ManifestXmlTags.LAYER_GROUP);
		}

		return Optional.of(handler);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {

		ManifestXmlHandler handler = null;

		switch (localName) {
		case ManifestXmlTags.LAYER_GROUP: {
			// no-op
		} break;

		case ManifestXmlTags.NAME: {
			getInstance().setName(text);
			handler = this;
		} break;

		case ManifestXmlTags.DESCRIPTION: {
			getInstance().setDescription(text);
			handler = this;
		} break;

		default:
			throw new UnexpectedTagException(qName, false, ManifestXmlTags.LAYER_GROUP);
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

		case ManifestXmlTags.ITEM_LAYER :
		case ManifestXmlTags.STRUCTURE_LAYER :
		case ManifestXmlTags.ANNOTATION_LAYER :
		case ManifestXmlTags.FRAGMENT_LAYER :
		case ManifestXmlTags.HIGHLIGHT_LAYER : {
			getInstance().addLayerManifest(((AbstractLayerManifestXmlDelegate<?>) handler).getInstance());
		} break;

		default:
			throw new UnsupportedNestingException(qName, ManifestXmlTags.LAYER_GROUP);
		}
	}
}
