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

import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.standard.StructureLayerManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureLayerManifestXmlDelegate extends AbstractLayerManifestXmlDelegate<StructureLayerManifest> {

	private ContainerManifestXmlDelegate containerManifestXmlDelegate;
	private StructureManifestXmlDelegate structureManifestXmlDelegate;

	public StructureLayerManifestXmlDelegate() {
		// no-op
	}

	public StructureLayerManifestXmlDelegate(StructureLayerManifest manifest) {
		setInstance(manifest);
	}

	public StructureLayerManifestXmlDelegate(LayerGroupManifest groupManifest) {
		setInstance(new StructureLayerManifestImpl(groupManifest));
	}

	private ContainerManifestXmlDelegate getContainerManifestXmlDelegate() {
		if(containerManifestXmlDelegate==null) {
			containerManifestXmlDelegate = new ContainerManifestXmlDelegate();
		}

		return containerManifestXmlDelegate;
	}

	private StructureManifestXmlDelegate getStructureManifestXmlDelegate() {
		if(structureManifestXmlDelegate==null) {
			structureManifestXmlDelegate = new StructureManifestXmlDelegate();
		}

		return structureManifestXmlDelegate;
	}

	public StructureLayerManifestXmlDelegate reset(LayerGroupManifest groupManifest) {
		reset();
		setInstance(new StructureLayerManifestImpl(groupManifest));

		return this;
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

		if(structureManifestXmlDelegate!=null) {
			structureManifestXmlDelegate.reset();
		}
	}


	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractLayerManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		ItemLayerManifest manifest = getInstance();

		if(manifest.isLocalBoundaryLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, TAG_BOUNDARY_LAYER, manifest.getBoundaryLayerManifest());
		}

		if(manifest.isLocalFoundationLayerManifest()) {
			ManifestXmlUtils.writeTargetLayerManifestElement(serializer, TAG_FOUNDATION_LAYER, manifest.getFoundationLayerManifest());
		}

		if(manifest.hasLocalContainers()) {
			for(ContainerManifest containerManifest : manifest.getContainerManifests()) {
				if(containerManifest.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
					getStructureManifestXmlDelegate().reset((StructureManifest)containerManifest).writeXml(serializer);
				} else {
					getContainerManifestXmlDelegate().reset(containerManifest).writeXml(serializer);
				}
			}
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_STRUCTURE_LAYER: {
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

		case TAG_STRUCTURE: {
			return getStructureManifestXmlDelegate().reset(getInstance());
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
		case TAG_STRUCTURE_LAYER: {
			return null;
		}

		case TAG_BOUNDARY_LAYER: {
			// no-op
		} break;

		case TAG_FOUNDATION_LAYER: {
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

		case TAG_CONTAINER: {
			getInstance().addContainerManifest(((ContainerManifestXmlDelegate) handler).getInstance(), -1);
		} break;

		case TAG_STRUCTURE: {
			getInstance().addStructureManifest(((StructureManifestXmlDelegate) handler).getInstance(), -1);
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
		return TAG_STRUCTURE_LAYER;
	}
}
