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
package de.ims.icarus2.model.api.manifest.xml.delegates;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.manifest.ContainerManifest;
import de.ims.icarus2.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus2.model.api.manifest.LayerGroupManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.standard.manifest.FragmentLayerManifestImpl;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
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
	 * @see de.ims.icarus2.model.api.manifest.xml.delegates.AbstractXmlDelegate#reset()
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
	 * @see de.ims.icarus2.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String annotationKey = ManifestXmlUtils.normalize(attributes, ATTR_ANNOTATION_KEY);
		if(annotationKey!=null) {
			getInstance().setAnnotationKey(annotationKey);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#writeAttributes(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		FragmentLayerManifest manifest = getInstance();

		if(manifest.isLocalAnnotationKey()) {
			serializer.writeAttribute(ATTR_ANNOTATION_KEY, manifest.getAnnotationKey());
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractLayerManifest#writeElements(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		FragmentLayerManifest manifest = getInstance();

		if(manifest.isLocalBoundaryLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, TAG_BOUNDARY_LAYER, manifest.getBoundaryLayerManifest());
		}

		if(manifest.isLocalFoundationLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, TAG_FOUNDATION_LAYER, manifest.getFoundationLayerManifest());
		}

		if(manifest.hasLocalContainers()) {
			for(ContainerManifest containerManifest : manifest.getContainerManifests()) {
				getContainerManifestXmlDelegate().reset(containerManifest).writeXml(serializer);
			}
		}

		if(manifest.isLocalValueLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, TAG_VALUE_LAYER, manifest.getValueLayerManifest());
		}

		if(manifest.isLocalRasterizerManifest()) {
			getRasterizerManifestXmLDelegate().reset(manifest.getRasterizerManifest()).writeXml(serializer);
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_FRAGMENT_LAYER: {
			readAttributes(attributes);
		} break;

		case TAG_BOUNDARY_LAYER: {
			String boundaryLayerId = ManifestXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			getInstance().setBoundaryLayerId(boundaryLayerId);
		} break;

		case TAG_FOUNDATION_LAYER: {
			String foundationLayerId = ManifestXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			getInstance().setFoundationLayerId(foundationLayerId);
		} break;

		case TAG_CONTAINER: {
			return getContainerManifestXmlDelegate().reset(getInstance());
		}

		case TAG_VALUE_LAYER: {
			String valueLayerId = ManifestXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			getInstance().setValueLayerId(valueLayerId);
		} break;

		case TAG_RASTERIZER: {
			return getRasterizerManifestXmLDelegate().reset(getInstance());
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
		case TAG_FRAGMENT_LAYER: {
			return null;
		}

		case TAG_BOUNDARY_LAYER: {
			// no-op
		} break;

		case TAG_FOUNDATION_LAYER: {
			// no-op
		} break;

		case TAG_VALUE_LAYER: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_CONTAINER: {
			getInstance().addContainerManifest(((ContainerManifestXmlDelegate) handler).getInstance(), -1);
		} break;

		case TAG_RASTERIZER: {
			getInstance().setRasterizerManifest(((RasterizerManifestXmLDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_FRAGMENT_LAYER;
	}
}
