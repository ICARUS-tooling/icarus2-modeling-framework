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

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;

import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class ImplementationManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<ImplementationManifest> {


	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		ImplementationManifest manifest = getInstance();

		// Write source
		if(manifest.isLocalSource())
			serializer.writeAttribute(ManifestXmlAttributes.SOURCE, manifest.getSource());

		// Write classname
		if(manifest.isLocalClassname())
			serializer.writeAttribute(ManifestXmlAttributes.CLASSNAME, manifest.getClassname());

		// Write source type
		if(manifest.isLocalSourceType())
			serializer.writeAttribute(ManifestXmlAttributes.SOURCE_TYPE,
					manifest.getSourceType().map(SourceType::getStringValue));

		// Write flags
		if(manifest.isLocalUseFactory())
			serializer.writeAttribute(ManifestXmlAttributes.FACTORY, manifest.isUseFactory());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ImplementationManifest manifest = getInstance();

		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.SOURCE)
			.ifPresent(manifest::setSource);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CLASSNAME)
			.ifPresent(manifest::setClassname);
		ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.SOURCE_TYPE)
			.map(SourceType::parseSourceType)
			.ifPresent(manifest::setSourceType);
		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.FACTORY)
			.ifPresent(manifest::setUseFactory);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.IMPLEMENTATION;
	}
}
