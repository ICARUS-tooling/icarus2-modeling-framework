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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/xml/ManifestXmlUtils.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.xml;

import javax.swing.Icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import de.ims.icarus2.eval.Expression;
import de.ims.icarus2.eval.Variable;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest.Note;
import de.ims.icarus2.model.manifest.api.Documentation.Resource;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.util.IconWrapper;
import de.ims.icarus2.model.util.StringResource;
import de.ims.icarus2.model.xml.XmlSerializer;
import de.ims.icarus2.util.date.DateUtils;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id: ManifestXmlUtils.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public final class ManifestXmlUtils implements ManifestXmlAttributes, ManifestXmlTags {
	
	private static final Logger log = LoggerFactory
			.getLogger(ManifestXmlUtils.class);

	private ManifestXmlUtils() {
		// no-op
	}

	/**
	 * Check whether a flag is set and differs from a given default value. Only if both conditions are met
	 * will the flag be written to the provided serializer, using the specified name as identifier.
	 */
	public static void writeFlag(XmlSerializer serializer, String name, Boolean flag, boolean defaultValue) throws Exception {
		if(flag!=null && flag.booleanValue()!=defaultValue) {
			serializer.writeAttribute(name, flag.booleanValue());
		}
	}
	public static void writeFlag(XmlSerializer serializer, String name, boolean flag, boolean defaultValue) throws Exception {
		if(flag!=defaultValue) {
			serializer.writeAttribute(name, flag);
		}
	}

	public static Boolean readFlag(Attributes attributes, String name, boolean defaultValue) {
		String value = normalize(attributes, name);
		Boolean result = (value==null) ? null : Boolean.valueOf(value);
		if(result!=null && result.booleanValue()==defaultValue) {
			result = null;
		}
		return result;
	}

	//*******************************************
	//               WRITE METHODS
	//*******************************************

	public static String getSerializedForm(ValueType type) {
//		return type==ValueType.STRING ? null : type.getXmlValue();

		return type==null ? null : type.getStringValue();
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, Identity identity) throws Exception {
		if(identity==null) {
			return;
		}

		writeIdentityAttributes(serializer, identity.getId(), identity.getName(), identity.getDescription(), identity.getIcon());
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, String id, String name, String description, Icon icon) throws Exception {
		serializer.writeAttribute(ATTR_ID, id);
		serializer.writeAttribute(ATTR_NAME, name);
		serializer.writeAttribute(ATTR_DESCRIPTION, description);

		if(icon instanceof StringResource) {
			serializer.writeAttribute(ATTR_ICON, ((StringResource)icon).getStringValue());
		} else if(icon != null) {
			log.warn("Skipping serialization of icon for identity: {}", (id==null ? id : "<unnamed>")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static String getSerializedForm(TargetLayerManifest manifest) {
		return manifest.getPrerequisite()!=null ?
				manifest.getPrerequisite().getAlias()
				: manifest.getResolvedLayerManifest().getId();
	}

	/**
	 * Writes the given target layer. Uses as layer id the alias of the prerequisite of the
	 * link if present, or the resolved layer's id otherwise.
	 */
	public static void writeTargetLayerManifestElement(XmlSerializer serializer, String name, TargetLayerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		serializer.startEmptyElement(name);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_LAYER_ID, getSerializedForm(manifest));

		serializer.endElement(name);
	}

	public static void writeAliasElement(XmlSerializer serializer, String alias) throws Exception {
		serializer.startEmptyElement(TAG_ALIAS);
		serializer.writeAttribute(ATTR_NAME, alias);
		serializer.endElement(TAG_ALIAS);
	}

	public static void writeValueElement(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
		if(value==null) {
			return;
		}

		if(type==ValueType.UNKNOWN)
			throw new UnsupportedOperationException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		if(type==ValueType.CUSTOM)
			throw new UnsupportedOperationException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		serializer.startElement(name);

		// CONTENT

		writeValue(serializer, value, type);

		serializer.endElement(name);
	}

	private static void writeValue(XmlSerializer serializer, Object value, ValueType type) throws Exception {
		if(value instanceof Expression) {
			writeEvalElement(serializer, (Expression)value);
		} else {
			serializer.writeText(type.toChars(value));
		}
	}

	public static void writeEvalElement(XmlSerializer serializer, Expression expression) throws Exception {
		serializer.startElement(TAG_EVAL);

		for(Variable variable : expression.getVariables()) {
			serializer.startEmptyElement(TAG_VARIABLE);
			serializer.writeAttribute(ATTR_NAME, variable.getName());
			serializer.writeAttribute(ATTR_CLASS, variable.getNamespaceClass().getName());

			//FIXME introduce a workaround to carry on plugin information on the variable level
//			ClassLoader loader = variable.getNamespaceClass().getClassLoader();
//			if(PluginUtil.isPluginClassLoader(loader)) {
//				PluginDescriptor descriptor = PluginUtil.getDescriptor(loader);
//				serializer.writeAttribute(ATTR_PLUGIN_ID, descriptor.getId());
//			}

			serializer.endElement(TAG_VARIABLE);
		}

		serializer.startElement(TAG_CODE);
		serializer.writeCData(expression.getCode());
		serializer.endElement(TAG_CODE);

		serializer.endElement(TAG_EVAL);
	}

	public static void writeResourceElement(XmlSerializer serializer, Resource resource) throws Exception {
		if(resource.getUri()==null)
			throw new IllegalStateException("Resource is missing url"); //$NON-NLS-1$

		serializer.startElement(TAG_RESOURCE);
		ManifestXmlUtils.writeIdentityAttributes(serializer, resource);
		serializer.writeText(resource.getUri().toString());
		serializer.endElement(TAG_RESOURCE);
	}

	public static void writePrerequisiteElement(XmlSerializer serializer, PrerequisiteManifest manifest) throws Exception {

		serializer.startEmptyElement(TAG_PREREQUISITE);

		// ATTRIBUTES

		serializer.writeAttribute(ATTR_CONTEXT_ID, manifest.getContextId());
		serializer.writeAttribute(ATTR_LAYER_ID, manifest.getLayerId());

		// Only write the layer type attribute for unresolved prerequisites!
		if(manifest.getUnresolvedForm()==null) {
			serializer.writeAttribute(ATTR_TYPE_ID, manifest.getTypeId());
		}

		serializer.writeAttribute(ATTR_ALIAS, manifest.getAlias());

		serializer.endElement(TAG_PREREQUISITE);
	}

	public static void writeNoteElement(XmlSerializer serializer, Note note) throws Exception {
		if(note==null) {
			return;
		}

		serializer.startElement(TAG_NOTE);

		// Attributes

		serializer.writeAttribute(ATTR_NAME, note.getName());
		serializer.writeAttribute(ATTR_DATE, DateUtils.formatDate(note.getModificationDate()));

		// Content

		serializer.writeText(note.getContent());

		serializer.endElement(TAG_NOTE);
	}

	//*******************************************
	//               READ METHOD
	//*******************************************

	public static void readIdentity(Attributes attr, ModifiableIdentity identity) {
		String id = normalize(attr, ATTR_ID);
		if(id!=null) {
			identity.setId(id);
		}

		String name = normalize(attr, ATTR_NAME);
		if(name!=null) {
			identity.setName(name);
		}

		String description = normalize(attr, ATTR_DESCRIPTION);
		if(description!=null) {
			identity.setDescription(description);
		}

		String icon = normalize(attr, ATTR_ICON);
		if(icon!=null) {
			identity.setIcon(iconValue(icon));
		}
	}

	public static Icon iconValue(String iconName) {
		return new IconWrapper(iconName);
	}

	public static Icon iconValue(Attributes attr, String key) {
		String icon = normalize(attr, key);
		return icon==null ? null : iconValue(icon);
	}

	public static String stringValue(Attributes attr, String key) {
		return normalize(attr, key);
	}

	public static long longValue(String s) {
		return Long.parseLong(s);
	}

	public static long longValue(Attributes attr, String key) {
		return longValue(normalize(attr, key));
	}

	public static double doubleValue(String s) {
		return Double.parseDouble(s);
	}

	public static double doubleValue(Attributes attr, String key) {
		return doubleValue(normalize(attr, key));
	}

	public static float floatValue(String s) {
		return Float.parseFloat(s);
	}

	public static float floatValue(Attributes attr, String key) {
		return floatValue(normalize(attr, key));
	}

	public static int intValue(String s) {
		return Integer.parseInt(s);
	}

	public static int intValue(Attributes attr, String key) {
		return intValue(normalize(attr, key));
	}

	public static boolean booleanValue(String s) {
		return s!=null && ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean booleanValue(Attributes attr, String key) {
		return booleanValue(normalize(attr, key));
	}

	public static boolean booleanValue(Attributes attr, String key, boolean defaultValue) {
		String s = normalize(attr, key);
		return s==null ? defaultValue : booleanValue(s);
	}

	public static ValueType typeValue(Attributes attr) {
		String s = normalize(attr, ATTR_VALUE_TYPE);
		return typeValue(s);
	}

	public static ValueType typeValue(String s) {
		return s==null ? ValueType.STRING : ValueType.parseValueType(s);
	}

	public static Boolean boolValue(Attributes attr, String key) {
		String s = normalize(attr, key);
		return s==null ? null : booleanValue(s);
	}

	public static String normalize(Attributes attr, String name) {
		String value = attr.getValue(name);

		return (value==null || value.isEmpty()) ? null : value;
	}
}
