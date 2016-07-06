/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.readFlag;
import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.writeFlag;
import gnu.trove.map.hash.THashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.LayerGroupManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class LayerGroupManifestXmlHandler extends AbstractXmlDelegate<LayerGroupManifest> {

	@SuppressWarnings("rawtypes")
	private static final Map<ManifestType, Supplier<? extends AbstractLayerManifestXmlDelegate>>
			layerDelegateSuppliers = new THashMap<>();
	static {
		layerDelegateSuppliers.put(ManifestType.ITEM_LAYER_MANIFEST, ItemLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.STRUCTURE_LAYER_MANIFEST, StructureLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.ANNOTATION_LAYER_MANIFEST, AnnotationLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.FRAGMENT_LAYER_MANIFEST, FragmentLayerManifestXmlDelegate::new);
		layerDelegateSuppliers.put(ManifestType.HIGHLIGHT_LAYER_MANIFEST, HighlightLayerManifestXmlDelegate::new);
	}

	private Map<ManifestType, AbstractLayerManifestXmlDelegate<?>> layerDelegates = new THashMap<>();

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

	public LayerGroupManifestXmlHandler() {
		// no-op
	}

	public LayerGroupManifestXmlHandler(LayerGroupManifest groupManifest) {
		setInstance(groupManifest);
	}

	public LayerGroupManifestXmlHandler(ContextManifest contextManifest) {
		setInstance(new LayerGroupManifestImpl(contextManifest));
	}

	public LayerGroupManifestXmlHandler reset(ContextManifest contextManifest) {
		reset();
		setInstance(new LayerGroupManifestImpl(contextManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		serializer.startElement(TAG_LAYER_GROUP);

		LayerGroupManifest manifest = getInstance();

		ManifestXmlUtils.writeIdentityAttributes(serializer, manifest);

		writeFlag(serializer, ATTR_INDEPENDENT, manifest.isIndependent(), LayerGroupManifest.DEFAULT_INDEPENDENT_VALUE);

		if(manifest.getPrimaryLayerManifest()!=null) {
			serializer.writeAttribute(ATTR_PRIMARY_LAYER, manifest.getPrimaryLayerManifest().getId());
		}

		for(Iterator<LayerManifest> it = manifest.getLayerManifests().iterator(); it.hasNext();) {
			LayerManifest layerManifest = it.next();

			@SuppressWarnings("unchecked")
			AbstractLayerManifestXmlDelegate<LayerManifest> delegate = getLayerDelegate(layerManifest.getManifestType());
			delegate.reset(layerManifest).writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}

		serializer.endElement(TAG_LAYER_GROUP);
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_LAYER_GROUP: {
			LayerGroupManifest manifest = getInstance();

			ManifestXmlUtils.readIdentity(attributes, manifest);

			manifest.setIndependent(readFlag(attributes, ATTR_INDEPENDENT, LayerGroupManifest.DEFAULT_INDEPENDENT_VALUE));

			String primaryLayerId = ManifestXmlUtils.normalize(attributes, ATTR_PRIMARY_LAYER);
			if(primaryLayerId==null)
				throw new IllegalArgumentException("Missing primary layer id"); //$NON-NLS-1$
			manifest.setPrimaryLayerId(primaryLayerId);
		} break;

		case TAG_ITEM_LAYER : {
			ItemLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.ITEM_LAYER_MANIFEST);
			return delegate.reset(getInstance());
		}

		case TAG_STRUCTURE_LAYER : {
			StructureLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.STRUCTURE_LAYER_MANIFEST);
			return delegate.reset(getInstance());
		}

		case TAG_ANNOTATION_LAYER : {
			AnnotationLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.ANNOTATION_LAYER_MANIFEST);
			return delegate.reset(getInstance());
		}

		case TAG_FRAGMENT_LAYER : {
			FragmentLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.FRAGMENT_LAYER_MANIFEST);
			return delegate.reset(getInstance());
		}

		case TAG_HIGHLIGHT_LAYER : {
			HighlightLayerManifestXmlDelegate delegate = getLayerDelegate(ManifestType.HIGHLIGHT_LAYER_MANIFEST);
			return delegate.reset(getInstance());
		}

		default:
			throw new UnexpectedTagException(qName, true, TAG_LAYER_GROUP);
		}

		return this;
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_LAYER_GROUP: {
			return null;
		}

		default:
			throw new UnexpectedTagException(qName, false, TAG_LAYER_GROUP);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_ITEM_LAYER :
		case TAG_STRUCTURE_LAYER :
		case TAG_ANNOTATION_LAYER :
		case TAG_FRAGMENT_LAYER :
		case TAG_HIGHLIGHT_LAYER : {
			getInstance().addLayerManifest(((AbstractLayerManifestXmlDelegate<?>) handler).getInstance());
		} break;

		default:
			throw new UnsupportedNestingException(qName, TAG_LAYER_GROUP);
		}
	}
}
