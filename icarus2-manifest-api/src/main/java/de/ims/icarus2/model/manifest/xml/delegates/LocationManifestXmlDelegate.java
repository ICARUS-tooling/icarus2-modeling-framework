/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.normalize;

import java.util.Optional;

import javax.xml.stream.XMLStreamException;

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

		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.INLINE)
			.ifPresent(getInstance()::setIsInline);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractManifestXmlDelegate#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		if(getInstance().isInline()!=LocationManifest.DEFAULT_IS_INLINE) {
			serializer.writeAttribute(ManifestXmlAttributes.INLINE, getInstance().isInline());
		}
	}

	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		LocationManifest manifest = getInstance();

		if(manifest.isInline()) {
			serializer.startElement(ManifestXmlTags.CONTENT);
			serializer.writeTextOrCData(manifest.getInlineData().get());
			serializer.endElement(ManifestXmlTags.CONTENT);
		} else {

			if(manifest.getRootPath().isPresent()) {
				serializer.startElement(ManifestXmlTags.PATH);
				serializer.writeAttribute(ManifestXmlAttributes.TYPE,
						manifest.getRootPathType().map(PathType::getStringValue));
				serializer.writeTextOrCData(manifest.getRootPath().get());
				serializer.endElement(ManifestXmlTags.PATH);
			}

			// ELEMENTS

			// Write path entries
			for(PathEntry pathEntry : manifest.getPathEntries()) {
				if(pathEntry.getType()==null)
					throw new IllegalStateException("Path entry is missing type"); //$NON-NLS-1$
				if(pathEntry.getValue()==null)
					throw new IllegalStateException("Path entry is missing value"); //$NON-NLS-1$

				serializer.startElement(ManifestXmlTags.PATH_ENTRY);
				serializer.writeAttribute(ManifestXmlAttributes.TYPE, pathEntry.getType().getStringValue());
				serializer.writeTextOrCData(pathEntry.getValue());
				serializer.endElement(ManifestXmlTags.PATH_ENTRY);
			}

			// Write rootPath resolver
			if(manifest.getPathResolverManifest().isPresent()) {
				getPathResolverManifestXmlDelegate().reset(manifest.getPathResolverManifest().get()).writeXml(serializer);
			}
		}

	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.PATH: {
			normalize(attributes, ManifestXmlAttributes.TYPE)
				.map(PathType::parsePathType)
				.ifPresent(getInstance()::setRootPathType);
		} break;

		case ManifestXmlTags.PATH_ENTRY : {
			String typeId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.TYPE)
					.orElseThrow(ManifestXmlHandler.error("Missing path entry type"));
			pathType = PathType.parsePathType(typeId);
		} break;

		case ManifestXmlTags.PATH_RESOLVER: {
			handler = getPathResolverManifestXmlDelegate().reset(getInstance());
		} break;

		case ManifestXmlTags.CONTENT:
			break;

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

		case ManifestXmlTags.PATH_RESOLVER : {
			getInstance().setPathResolverManifest(((PathResolverManifestXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}
}
