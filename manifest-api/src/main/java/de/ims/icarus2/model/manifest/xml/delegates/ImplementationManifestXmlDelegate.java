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

import org.xml.sax.Attributes;

import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
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
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		ImplementationManifest manifest = getInstance();

		// Write source
		serializer.writeAttribute(ATTR_SOURCE, manifest.getSource());

		// Write classname
		serializer.writeAttribute(ATTR_CLASSNAME, manifest.getClassname());

		// Write source type
		SourceType sourceType = manifest.getSourceType();
		if(sourceType!=null && sourceType!=SourceType.DEFAULT) {
			serializer.writeAttribute(ATTR_SOURCE_TYPE, sourceType.getStringValue());
		}

		// Write flags
		writeFlag(serializer, ATTR_FACTORY, manifest.isUseFactory(), ImplementationManifest.DEFAULT_USE_FACTORY_VALUE);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ImplementationManifest manifest = getInstance();

		manifest.setSource(ManifestXmlUtils.normalize(attributes, ATTR_SOURCE));
		manifest.setClassname(ManifestXmlUtils.normalize(attributes, ATTR_CLASSNAME));

		String type = ManifestXmlUtils.normalize(attributes, ATTR_SOURCE_TYPE);
		if(type!=null) {
			manifest.setSourceType(SourceType.parseSourceType(type));
		}

		manifest.setUseFactory(readFlag(attributes, ATTR_FACTORY, ImplementationManifest.DEFAULT_USE_FACTORY_VALUE));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_IMPLEMENTATION;
	}
}
