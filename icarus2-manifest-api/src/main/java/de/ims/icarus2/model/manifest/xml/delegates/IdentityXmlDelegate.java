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
/**
 *
 */
package de.ims.icarus2.model.manifest.xml.delegates;

import javax.swing.Icon;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.annotations.Unused;
import de.ims.icarus2.util.icon.IconRegistry;
import de.ims.icarus2.util.icon.IconWrapper;
import de.ims.icarus2.util.icon.ImageSerializer;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;
import de.ims.icarus2.util.xml.XmlSerializer;
import de.ims.icarus2.util.xml.XmlUtils;

/**
 * @author Markus Gärtner
 *
 */
@Unused
public class IdentityXmlDelegate implements ManifestXmlDelegate<ModifiableIdentity> {

	private ModifiableIdentity identity;

	protected String xmlTag() {
		return ManifestXmlTags.IDENTITY;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation, String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case ManifestXmlTags.IDENTITY: {
			ManifestXmlUtils.readIdentityAttributes(attributes, getInstance());
		} break;

		// Allow all the nested id fields
		case ManifestXmlTags.ID:
		case ManifestXmlTags.NAME:
		case ManifestXmlTags.DESCRIPTION:
		case ManifestXmlTags.ICON:
			break;

		default:
			throw new UnexpectedTagException(qName, true, xmlTag());
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation, String uri, String localName, String qName,
			String text) throws SAXException {

		switch (localName) {
		case ManifestXmlTags.IDENTITY:
			return null;

		case ManifestXmlTags.ID: {
			getInstance().setId(text);
		} break;

		case ManifestXmlTags.NAME: {
			getInstance().setName(text);
		} break;

		case ManifestXmlTags.DESCRIPTION: {
			getInstance().setDescription(text);
		} break;

		case ManifestXmlTags.ICON: {
			IconRegistry iconRegistry = IconRegistry.getGlobalRegistry();
			// Try icon name first
			if(iconRegistry.hasIconInfo(text)) {
				getInstance().setIcon(new IconWrapper(text));
			} else {
				// Otherwise assume we have a serialized image here
				getInstance().setIcon(ImageSerializer.string2Icon(text));
			}
		} break;

		default:
			throw new UnexpectedTagException(qName, false, xmlTag());
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri, String localName, String qName,
			ManifestXmlHandler handler) throws SAXException {
		throw new UnsupportedNestingException(qName, xmlTag());
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#setInstance(java.lang.Object)
	 */
	@Override
	public void setInstance(ModifiableIdentity instance) {
		identity = instance;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#getInstance()
	 */
	@Override
	public ModifiableIdentity getInstance() {
		return identity;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		identity = null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#writeXml(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		Identity identity = getInstance();

		String id = ManifestXmlUtils.normalize(identity.getId());
		String name = ManifestXmlUtils.normalize(identity.getName());
		String description = ManifestXmlUtils.normalize(identity.getDescription());
		Icon icon = identity.getIcon();
		// Simple serialization form of icon if applicable
		String iconString = ManifestXmlUtils.serialize(icon);

		boolean wrapId = id!=null && XmlUtils.hasIllegalAttributeSymbols(id);
		boolean wrapName = name!=null && XmlUtils.hasIllegalAttributeSymbols(name);
		boolean wrapDesc = description!=null && XmlUtils.hasIllegalAttributeSymbols(description);
		boolean wrapIcon = icon!=null && XmlUtils.hasIllegalAttributeSymbols(iconString);

		serializer.startElement(ManifestXmlTags.IDENTITY,
				!wrapId && !wrapName && !wrapDesc && !wrapIcon);

		// Write attributes
		if(id!=null && !wrapId) {
			serializer.writeAttribute(ManifestXmlAttributes.ID, id);
		}
		if(name!=null && !wrapName) {
			serializer.writeAttribute(ManifestXmlAttributes.NAME, name);
		}
		if(description!=null && !wrapDesc) {
			serializer.writeAttribute(ManifestXmlAttributes.DESCRIPTION, description);
		}
		if(iconString!=null && !wrapIcon) {
			serializer.writeAttribute(ManifestXmlAttributes.ICON, iconString);
		}

		// Write elements
		if(wrapId) {
			ManifestXmlUtils.writeElement(serializer, ManifestXmlTags.ID, id);
		}
		if(wrapName) {
			ManifestXmlUtils.writeElement(serializer, ManifestXmlTags.ID, name);
		}
		if(wrapDesc) {
			ManifestXmlUtils.writeElement(serializer, ManifestXmlTags.ID, description);
		}
		if(wrapIcon) {
			if(iconString==null) {
				iconString = ImageSerializer.icon2String(icon);
			}
			ManifestXmlUtils.writeElement(serializer, ManifestXmlTags.ICON, iconString);
		}

		serializer.endElement(ManifestXmlTags.IDENTITY);
	}

}
