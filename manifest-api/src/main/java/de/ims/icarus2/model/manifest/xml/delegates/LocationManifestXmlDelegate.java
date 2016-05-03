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

import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.normalize;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl.PathEntryImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationManifestXmlDelegate extends AbstractManifestXmlDelegate<LocationManifest> {

	private PathType pathType;
	private PathResolverManifestXmlDelegate pathResolverManifestXmlDelegate;

	private PathResolverManifestXmlDelegate getPathResolverManifestXmlDelegate() {
		if(pathResolverManifestXmlDelegate==null) {
			pathResolverManifestXmlDelegate = new PathResolverManifestXmlDelegate();
		}

		return pathResolverManifestXmlDelegate;
	}

	public LocationManifestXmlDelegate reset(ContextManifest contextManifest) {
		reset();
		setInstance(new LocationManifestImpl(contextManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		pathType = null;
	}

	@Override
	protected String xmlTag() {
		return TAG_LOCATION;
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		LocationManifest manifest = getInstance();

		if(manifest.getRootPath()!=null) {
			serializer.startElement(TAG_PATH);
			if(manifest.getRootPathType()!=null) {
				serializer.writeAttribute(ATTR_TYPE, manifest.getRootPathType().getStringValue());
			}
			serializer.writeCData(manifest.getRootPath());
			serializer.endElement(TAG_PATH);
		}

		// ELEMENTS

		// Write rootPath entries
		for(PathEntry pathEntry : manifest.getPathEntries()) {
			if(pathEntry.getType()==null)
				throw new IllegalStateException("Path entry is missing type"); //$NON-NLS-1$
			if(pathEntry.getValue()==null)
				throw new IllegalStateException("Path entry is missing value"); //$NON-NLS-1$

			serializer.startElement(TAG_PATH_ENTRY);
			serializer.writeAttribute(ATTR_TYPE, pathEntry.getType().getStringValue());
			serializer.writeCData(pathEntry.getValue());
			serializer.endElement(TAG_PATH_ENTRY);
		}

		// Write rootPath resolver
		if(manifest.getPathResolverManifest()!=null) {
			getPathResolverManifestXmlDelegate().reset(manifest.getPathResolverManifest()).writeXml(serializer);
		}

	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_LOCATION: {
			readAttributes(attributes);
		} break;

		case TAG_PATH: {
			String type = normalize(attributes, ATTR_TYPE);
			if(type!=null) {
				getInstance().setRootPathType(PathType.parsePathType(type));
			}
		} break;

		case TAG_PATH_ENTRY : {
			String typeId = ManifestXmlUtils.normalize(attributes, ATTR_TYPE);
			pathType = PathType.parsePathType(typeId);
		} break;

		case TAG_PATH_RESOLVER: {
			return getPathResolverManifestXmlDelegate().reset(getInstance());
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
		case TAG_LOCATION: {
			return null;
		}

		case TAG_PATH: {
			getInstance().setRootPath(text);
		} break;

		case TAG_PATH_ENTRY: {
			PathEntry pathEntry = new PathEntryImpl(pathType, text);
			getInstance().addPathEntry(pathEntry);
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

		case TAG_PATH_RESOLVER : {
			getInstance().setPathResolverManifest(((PathResolverManifestXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}
}
