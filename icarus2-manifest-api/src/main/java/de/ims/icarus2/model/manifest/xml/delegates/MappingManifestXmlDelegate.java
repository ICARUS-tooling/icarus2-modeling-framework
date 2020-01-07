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

import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.model.manifest.standard.MappingManifestImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class MappingManifestXmlDelegate extends AbstractXmlDelegate<MappingManifest> {

	public MappingManifestXmlDelegate reset(DriverManifest driverManifest) {
		reset();
		setInstance(new MappingManifestImpl(driverManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlElement#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws XMLStreamException {
		MappingManifest manifest = getInstance();

		serializer.startEmptyElement(ManifestXmlTags.MAPPING);

		serializer.writeAttribute(ManifestXmlAttributes.ID, manifest.getId());
		serializer.writeAttribute(ManifestXmlAttributes.SOURCE_LAYER, manifest.getSourceLayerId());
		serializer.writeAttribute(ManifestXmlAttributes.TARGET_LAYER, manifest.getTargetLayerId());
		serializer.writeAttribute(ManifestXmlAttributes.RELATION,
				manifest.getRelation().map(Relation::getStringValue));
		serializer.writeAttribute(ManifestXmlAttributes.COVERAGE,
				manifest.getCoverage().map(Coverage::getStringValue));
		serializer.writeAttribute(ManifestXmlAttributes.INVERSE_MAPPING,
				manifest.getInverse().flatMap(MappingManifest::getId));

		serializer.endElement(ManifestXmlTags.MAPPING);
	}

	/**
	 * @param attributes
	 */
	protected void readAttributes(Attributes attributes) {
		MappingManifest manifest = getInstance();

		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.COVERAGE)
			.map(Coverage::parseCoverage)
			.ifPresent(manifest::setCoverage);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.RELATION)
			.map(Relation::parseRelation)
			.ifPresent(manifest::setRelation);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.ID)
			.ifPresent(manifest::setId);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.INVERSE_MAPPING)
			.ifPresent(manifest::setInverseId);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.SOURCE_LAYER)
			.ifPresent(manifest::setSourceLayerId);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.TARGET_LAYER)
			.ifPresent(manifest::setTargetLayerId);
	}


	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.MAPPING: {
			readAttributes(attributes);
		} break;

		default:
			throw new UnexpectedTagException(qName, true, ManifestXmlTags.MAPPING);
		}

		return Optional.of(this);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.MAPPING: {
			// no-op
		} break;

		default:
			throw new UnexpectedTagException(qName, false, ManifestXmlTags.MAPPING);
		}

		return Optional.empty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		throw new UnsupportedNestingException(qName, ManifestXmlTags.MAPPING);
	}
}
