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

import static de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils.readFlag;
import static de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils.writeFlag;

import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.api.manifest.ContextManifest;
import de.ims.icarus2.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.api.manifest.CorpusManifest;
import de.ims.icarus2.model.api.manifest.LayerGroupManifest;
import de.ims.icarus2.model.api.manifest.LocationManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.api.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<ContextManifest> {

	private LayerGroupManifestXmlHandler layerGroupManifestXmlHandler;
	private DriverManifestXmlDelegate driverManifestXmlDelegate;
	private LocationManifestXmlDelegate locationManifestXmlDelegate;

	public ContextManifestXmlDelegate() {
		// no-op
	}

	public ContextManifestXmlDelegate(ContextManifest manifest) {
		setInstance(manifest);
	}

	public ContextManifestXmlDelegate(CorpusManifest corpusManifest) {
		setInstance(new ContextManifestImpl(corpusManifest));
	}

	private LayerGroupManifestXmlHandler getLayerGroupManifestXmlHandler() {
		if(layerGroupManifestXmlHandler==null) {
			layerGroupManifestXmlHandler = new LayerGroupManifestXmlHandler();
		}
		return layerGroupManifestXmlHandler;
	}

	private DriverManifestXmlDelegate getDriverManifestXmlDelegate() {
		if(driverManifestXmlDelegate==null) {
			driverManifestXmlDelegate = new DriverManifestXmlDelegate();
		}
		return driverManifestXmlDelegate;
	}

	private LocationManifestXmlDelegate getLocationManifestXmlDelegate() {
		if(locationManifestXmlDelegate==null) {
			locationManifestXmlDelegate = new LocationManifestXmlDelegate();
		}
		return locationManifestXmlDelegate;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.xml.delegates.AbstractMemberManifestXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		if(layerGroupManifestXmlHandler!=null) {
			layerGroupManifestXmlHandler.reset();
		}

		if(driverManifestXmlDelegate!=null) {
			driverManifestXmlDelegate.reset();
		}

		if(locationManifestXmlDelegate!=null) {
			locationManifestXmlDelegate.reset();
		}
	}

	public ContextManifestXmlDelegate reset(CorpusManifest corpusManifest) {
		reset();
		setInstance(new ContextManifestImpl(corpusManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		ContextManifest manifest = getInstance();

		// Write primary layer
		if(manifest.isLocalPrimaryLayerManifest()) {
			serializer.writeAttribute(ATTR_PRIMARY_LAYER, manifest.getPrimaryLayerManifest().getId());
		}

		// Write foundation layer
		if(manifest.isLocalFoundationLayerManifest()) {
			serializer.writeAttribute(ATTR_FOUNDATION_LAYER, manifest.getFoundationLayerManifest().getId());
		}

		// Write flags
		if(manifest.isLocalIndependentContext()) {
			writeFlag(serializer, ATTR_INDEPENDENT, manifest.isIndependentContext(), ContextManifest.DEFAULT_INDEPENDENT_VALUE);
		}
		if(manifest.isLocalEditable()) {
			writeFlag(serializer, ATTR_EDITABLE, manifest.isEditable(), ContextManifest.DEFAULT_EDITABLE_VALUE);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractModifiableManifest#writeElements(de.ims.icarus2.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		ContextManifest manifest = getInstance();

		// Write location manifest
		List<LocationManifest> locationManifests = manifest.getLocationManifests();
		if(!locationManifests.isEmpty()) {
			for(LocationManifest locationManifest : locationManifests) {
				getLocationManifestXmlDelegate().reset(locationManifest).writeXml(serializer);
			}
			serializer.writeLineBreak();
		}

		// Write prerequisites
		List<PrerequisiteManifest> prerequisiteManifests = manifest.getLocalPrerequisites();
		if(!prerequisiteManifests.isEmpty()) {
			serializer.startElement(TAG_PREREQUISITES);

			for(PrerequisiteManifest prerequisiteManifest : prerequisiteManifests) {
				ManifestXmlUtils.writePrerequisiteElement(serializer, prerequisiteManifest);
			}

			serializer.endElement(TAG_PREREQUISITES);
			serializer.writeLineBreak();
		}

		// Write groups
		List<LayerGroupManifest> layerGroups = manifest.getLocalGroupManifests();
		for(Iterator<LayerGroupManifest> it = layerGroups.iterator(); it.hasNext();) {
			LayerGroupManifest groupManifest = it.next();

			getLayerGroupManifestXmlHandler().reset(groupManifest).writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}

		// Write driver
		if(manifest.isLocalDriverManifest()) {
			getDriverManifestXmlDelegate().reset(manifest.getDriverManifest()).writeXml(serializer);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ContextManifest manifest = getInstance();

		// Read primary layer id
		String primaryLayerId = ManifestXmlUtils.normalize(attributes, ATTR_PRIMARY_LAYER);
		if(primaryLayerId!=null) {
			manifest.setPrimaryLayerId(primaryLayerId);
		}

		// Read foundation layer id
		String foundationLayerId = ManifestXmlUtils.normalize(attributes, ATTR_FOUNDATION_LAYER);
		if(foundationLayerId!=null) {
			manifest.setFoundationLayerId(foundationLayerId);
		}

		Boolean independent = readFlag(attributes, ATTR_INDEPENDENT, ContextManifest.DEFAULT_INDEPENDENT_VALUE);
		if(independent!=null) {
			manifest.setIndependentContext(independent.booleanValue());
		}

		Boolean editable = readFlag(attributes, ATTR_EDITABLE, ContextManifest.DEFAULT_EDITABLE_VALUE);
		if(editable!=null) {
			manifest.setEditable(editable.booleanValue());
		}
	}

	protected void readPrereqAttributes(PrerequisiteManifest manifest, Attributes attributes) {
		String layerId = ManifestXmlUtils.normalize(attributes, ATTR_LAYER_ID);
		if(layerId!=null) {
			manifest.setLayerId(layerId);
		}

		String typeId = ManifestXmlUtils.normalize(attributes, ATTR_TYPE_ID);
		if(typeId!=null) {
			manifest.setTypeId(typeId);
		}

		String contextId = ManifestXmlUtils.normalize(attributes, ATTR_CONTEXT_ID);
		if(contextId!=null) {
			manifest.setContextId(contextId);
		}

		String description = ManifestXmlUtils.normalize(attributes, ATTR_DESCRIPTION);
		if(description!=null) {
			manifest.setDescription(description);
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_CONTEXT: {
			readAttributes(attributes);
		} break;

		case TAG_LOCATION: {
			return getLocationManifestXmlDelegate().reset(getInstance());
		}

		case TAG_PREREQUISITES: {
			// no-op
		} break;

		case TAG_LOCATIONS: {
			// no-op
		} break;

		case TAG_PREREQUISITE: {
			String alias = ManifestXmlUtils.normalize(attributes, ATTR_ALIAS);
			PrerequisiteManifest prerequisite = getInstance().addPrerequisite(alias);
			readPrereqAttributes(prerequisite, attributes);
		} break;

		case TAG_LAYER_GROUP: {
			return getLayerGroupManifestXmlHandler().reset(getInstance());
		}

		case TAG_DRIVER: {
			return getDriverManifestXmlDelegate().reset(getInstance());
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
		case TAG_CONTEXT: {
			return null;
		}

		case TAG_PREREQUISITES: {
			// no-op
		} break;

		case TAG_LOCATIONS: {
			// no-op
		} break;

		case TAG_PREREQUISITE: {
			// no-op
		} break;

		case TAG_DRIVER: {
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

		case TAG_LOCATION: {
			getInstance().addLocationManifest(((LocationManifestXmlDelegate) handler).getInstance());
		} break;

		case TAG_LAYER_GROUP : {
			getInstance().addLayerGroup(((LayerGroupManifestXmlHandler) handler).getInstance());
		} break;

		case TAG_DRIVER: {
			getInstance().setDriverManifest(((DriverManifestXmlDelegate) handler).getInstance());
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
		return TAG_CONTEXT;
	}
}
