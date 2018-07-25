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

import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.readFlag;
import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.writeFlag;

import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.ContextManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class ContextManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<ContextManifest> {

	private LayerGroupManifestXmlHandler layerGroupManifestXmlHandler;
	private DriverManifestXmlDelegate driverManifestXmlDelegate;
	private LocationManifestXmlDelegate locationManifestXmlDelegate;

	private boolean root;

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
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractMemberManifestXmlDelegate#reset()
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

		root = false;
	}

	public ContextManifestXmlDelegate reset(CorpusManifest corpusManifest) {
		reset();
		setInstance(new ContextManifestImpl(corpusManifest));

		return this;
	}

	public ContextManifestXmlDelegate root(boolean root) {
		this.root = root;

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		ContextManifest manifest = getInstance();

		// Write primary layer
		if(manifest.isLocalPrimaryLayerManifest()) {
			serializer.writeAttribute(ManifestXmlAttributes.PRIMARY_LAYER, manifest.getPrimaryLayerManifest().getId());
		}

		// Write foundation layer
		if(manifest.isLocalFoundationLayerManifest()) {
			serializer.writeAttribute(ManifestXmlAttributes.FOUNDATION_LAYER, manifest.getFoundationLayerManifest().getId());
		}

		// Write flags
		if(manifest.isLocalIndependentContext()) {
			writeFlag(serializer, ManifestXmlAttributes.INDEPENDENT, manifest.isIndependentContext(), ContextManifest.DEFAULT_INDEPENDENT_VALUE);
		}
		if(manifest.isLocalEditable()) {
			writeFlag(serializer, ManifestXmlAttributes.EDITABLE, manifest.isEditable(), ContextManifest.DEFAULT_EDITABLE_VALUE);
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
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
			serializer.startElement(ManifestXmlTags.PREREQUISITES);

			for(PrerequisiteManifest prerequisiteManifest : prerequisiteManifests) {
				ManifestXmlUtils.writePrerequisiteElement(serializer, prerequisiteManifest);
			}

			serializer.endElement(ManifestXmlTags.PREREQUISITES);
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
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ContextManifest manifest = getInstance();

		// Read primary layer id
		String primaryLayerId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.PRIMARY_LAYER);
		if(primaryLayerId!=null) {
			manifest.setPrimaryLayerId(primaryLayerId);
		}

		// Read foundation layer id
		String foundationLayerId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.FOUNDATION_LAYER);
		if(foundationLayerId!=null) {
			manifest.setFoundationLayerId(foundationLayerId);
		}

		Boolean independent = readFlag(attributes, ManifestXmlAttributes.INDEPENDENT, ContextManifest.DEFAULT_INDEPENDENT_VALUE);
		if(independent!=null) {
			manifest.setIndependentContext(independent.booleanValue());
		}

		Boolean editable = readFlag(attributes, ManifestXmlAttributes.EDITABLE, ContextManifest.DEFAULT_EDITABLE_VALUE);
		if(editable!=null) {
			manifest.setEditable(editable.booleanValue());
		}
	}

	protected void readPrereqAttributes(PrerequisiteManifest manifest, Attributes attributes) {
		String layerId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.LAYER_ID);
		if(layerId!=null) {
			manifest.setLayerId(layerId);
		}

		String typeId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.TYPE_ID);
		if(typeId!=null) {
			manifest.setTypeId(typeId);
		}

		String contextId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CONTEXT_ID);
		if(contextId!=null) {
			manifest.setContextId(contextId);
		}

		String description = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.DESCRIPTION);
		if(description!=null) {
			manifest.setDescription(description);
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {

		case ManifestXmlTags.ROOT_CONTEXT:
			root(true);
			//$FALL-THROUGH$
		case ManifestXmlTags.CONTEXT: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.LOCATION: {
			return getLocationManifestXmlDelegate().reset(getInstance());
		}

		case ManifestXmlTags.PREREQUISITES: {
			// no-op
		} break;

		case ManifestXmlTags.LOCATIONS: {
			// no-op
		} break;

		case ManifestXmlTags.PREREQUISITE: {
			String alias = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.ALIAS);
			PrerequisiteManifest prerequisite = getInstance().addPrerequisite(alias);
			readPrereqAttributes(prerequisite, attributes);
		} break;

		case ManifestXmlTags.LAYER_GROUP: {
			return getLayerGroupManifestXmlHandler().reset(getInstance());
		}

		case ManifestXmlTags.DRIVER: {
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
		switch (localName) {

		case ManifestXmlTags.ROOT_CONTEXT:
		case ManifestXmlTags.CONTEXT: {
			return null;
		}

		case ManifestXmlTags.PREREQUISITES: {
			// no-op
		} break;

		case ManifestXmlTags.LOCATIONS: {
			// no-op
		} break;

		case ManifestXmlTags.PREREQUISITE: {
			// no-op
		} break;

		case ManifestXmlTags.DRIVER: {
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
		switch (localName) {

		case ManifestXmlTags.LOCATION: {
			getInstance().addLocationManifest(((LocationManifestXmlDelegate) handler).getInstance());
		} break;

		case ManifestXmlTags.LAYER_GROUP : {
			getInstance().addLayerGroup(((LayerGroupManifestXmlHandler) handler).getInstance());
		} break;

		case ManifestXmlTags.DRIVER: {
			getInstance().setDriverManifest(((DriverManifestXmlDelegate) handler).getInstance());
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
		return root ? ManifestXmlTags.ROOT_CONTEXT : ManifestXmlTags.CONTEXT;
	}
}
