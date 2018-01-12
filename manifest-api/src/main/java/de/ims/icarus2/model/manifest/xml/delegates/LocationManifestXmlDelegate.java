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

import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.normalize;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl.PathEntryImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
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
		return ManifestXmlTags.LOCATION;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractManifestXmlDelegate#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String inline = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.INLINE);
		if(inline!=null) {
			getInstance().setIsInline(Boolean.parseBoolean(inline));
		}
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		LocationManifest manifest = getInstance();

		if(manifest.isInline()) {
			serializer.startElement(ManifestXmlTags.CONTENT);
			serializer.writeCData(manifest.getInlineData());
			serializer.endElement(ManifestXmlTags.CONTENT);
		} else {

			if(manifest.getRootPath()!=null) {
				serializer.startElement(ManifestXmlTags.PATH);
				if(manifest.getRootPathType()!=null) {
					serializer.writeAttribute(ManifestXmlAttributes.TYPE, manifest.getRootPathType().getStringValue());
				}
				serializer.writeCData(manifest.getRootPath());
				serializer.endElement(ManifestXmlTags.PATH);
			}

			// ELEMENTS

			// Write rootPath entries
			for(PathEntry pathEntry : manifest.getPathEntries()) {
				if(pathEntry.getType()==null)
					throw new IllegalStateException("Path entry is missing type"); //$NON-NLS-1$
				if(pathEntry.getValue()==null)
					throw new IllegalStateException("Path entry is missing value"); //$NON-NLS-1$

				serializer.startElement(ManifestXmlTags.PATH_ENTRY);
				serializer.writeAttribute(ManifestXmlAttributes.TYPE, pathEntry.getType().getStringValue());
				serializer.writeCData(pathEntry.getValue());
				serializer.endElement(ManifestXmlTags.PATH_ENTRY);
			}

			// Write rootPath resolver
			if(manifest.getPathResolverManifest()!=null) {
				getPathResolverManifestXmlDelegate().reset(manifest.getPathResolverManifest()).writeXml(serializer);
			}
		}

	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.LOCATION: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.PATH: {
			String type = normalize(attributes, ManifestXmlAttributes.TYPE);
			if(type!=null) {
				getInstance().setRootPathType(PathType.parsePathType(type));
			}
		} break;

		case ManifestXmlTags.PATH_ENTRY : {
			String typeId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.TYPE);
			pathType = PathType.parsePathType(typeId);
		} break;

		case ManifestXmlTags.PATH_RESOLVER: {
			return getPathResolverManifestXmlDelegate().reset(getInstance());
		}

		case ManifestXmlTags.CONTENT:
			break;

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
		case ManifestXmlTags.LOCATION: {
			return null;
		}

		case ManifestXmlTags.PATH: {
			getInstance().setRootPath(text);
		} break;

		case ManifestXmlTags.PATH_ENTRY: {
			PathEntry pathEntry = new PathEntryImpl(pathType, text);
			getInstance().addPathEntry(pathEntry);
		} break;

		case ManifestXmlTags.CONTENT: {
			getInstance().setInlineData(text);
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

		case ManifestXmlTags.PATH_RESOLVER : {
			getInstance().setPathResolverManifest(((PathResolverManifestXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}
}
