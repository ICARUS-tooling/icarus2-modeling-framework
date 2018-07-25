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

import static de.ims.icarus2.model.manifest.xml.ManifestXmlUtils.writeFlag;

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
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		ImplementationManifest manifest = getInstance();

		// Write source
		serializer.writeAttribute(ManifestXmlAttributes.SOURCE, manifest.getSource());

		// Write classname
		serializer.writeAttribute(ManifestXmlAttributes.CLASSNAME, manifest.getClassname());

		// Write source type
		SourceType sourceType = manifest.getSourceType();
		if(sourceType!=null && sourceType!=SourceType.DEFAULT) {
			serializer.writeAttribute(ManifestXmlAttributes.SOURCE_TYPE, sourceType.getStringValue());
		}

		// Write flags
		writeFlag(serializer, ManifestXmlAttributes.FACTORY, manifest.isUseFactory(), ImplementationManifest.DEFAULT_USE_FACTORY_VALUE);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		ImplementationManifest manifest = getInstance();

		String source = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.SOURCE);
		if(source!=null) {
			manifest.setSource(source);
		}

		String classname = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CLASSNAME);
		if(classname!=null) {
			manifest.setClassname(classname);
		}

		String type = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.SOURCE_TYPE);
		if(type!=null) {
			manifest.setSourceType(SourceType.parseSourceType(type));
		}

		String useFactory = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.FACTORY);
		if(useFactory!=null) {
			manifest.setUseFactory(Boolean.parseBoolean(useFactory));
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.IMPLEMENTATION;
	}
}
