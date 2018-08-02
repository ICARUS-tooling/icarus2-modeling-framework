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
package de.ims.icarus2.model.manifest.xml;

import static de.ims.icarus2.util.lang.Primitives._boolean;

import javax.swing.Icon;

import org.xml.sax.Attributes;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest.Note;
import de.ims.icarus2.model.manifest.api.Documentation.Resource;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.standard.DefaultCategory;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.date.DateUtils;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.eval.var.VariableDescriptor;
import de.ims.icarus2.util.icon.IconRegistry;
import de.ims.icarus2.util.icon.IconWrapper;
import de.ims.icarus2.util.icon.ImageSerializer;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.strings.NamedObject;
import de.ims.icarus2.util.strings.StringResource;
import de.ims.icarus2.util.xml.XmlSerializer;
import de.ims.icarus2.util.xml.XmlUtils;

/**
 * Collection of shared utility methods for transforming between
 * certain manifest types and their xml serializations.
 *
 * @author Markus Gärtner
 *
 */
public final class ManifestXmlUtils {

	private ManifestXmlUtils() {
		throw new IcarusException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Instantiation not supported");
	}

	public static final String MANIFEST_LABEL = "Manifest";

	public static final String MANIFEST_NAMESPACE_URI = XmlUtils.ICARUS_NS_URI+MANIFEST_LABEL;

	public static final String MANIFEST_NS_PREFIX = XmlUtils.ICARUS_NS;

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

	public static void writeCategoryAttributes(XmlSerializer serializer, Category category) throws Exception {
		if(category==null) {
			return;
		}

		writeIdentityAttributes(serializer, category.getId(), category.getName(), category.getDescription(), category.getIcon());

		serializer.writeAttribute(ManifestXmlAttributes.NAMESPACE, category.getNamespace());
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, String id, String name, String description, Icon icon) throws Exception {
		serializer.writeAttribute(ManifestXmlAttributes.ID, id);

		if(name!=null && !XmlUtils.hasIllegalAttributeSymbols(name)) {
			serializer.writeAttribute(ManifestXmlAttributes.NAME, name);
		}
		if(description!=null && !XmlUtils.hasIllegalAttributeSymbols(description)) {
			serializer.writeAttribute(ManifestXmlAttributes.DESCRIPTION, description);
		}

		String iconString = serialize(icon);
		if(iconString!=null && !XmlUtils.hasIllegalAttributeSymbols(iconString)) {
			serializer.writeAttribute(ManifestXmlAttributes.ICON, iconString);
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
		serializer.writeAttribute(ManifestXmlAttributes.LAYER_ID, getSerializedForm(manifest));

		serializer.endElement(name);
	}

	public static void writeAliasElement(XmlSerializer serializer, String alias) throws Exception {
		serializer.startEmptyElement(ManifestXmlTags.ALIAS);
		serializer.writeAttribute(ManifestXmlAttributes.NAME, alias);
		serializer.endElement(ManifestXmlTags.ALIAS);
	}

	public static void writeElement(XmlSerializer serializer, String name, String content) throws Exception {
		serializer.startEmptyElement(name);
		serializer.writeTextOrCData(content);
		serializer.endElement(name);
	}

	public static void writeIdentityFieldElements(XmlSerializer serializer, Identity identity) throws Exception {
		writeIdentityFieldElements(serializer, identity.getName(), identity.getDescription(), identity.getIcon());
	}

	public static void writeIdentityFieldElements(XmlSerializer serializer, String name, String description, Icon icon) throws Exception {


		// Nest identity information if needed
		if(name!=null && XmlUtils.hasIllegalAttributeSymbols(name)) {
			writeElement(serializer, ManifestXmlTags.NAME, name);
		}

		if(description!=null && XmlUtils.hasIllegalAttributeSymbols(description)) {
			writeElement(serializer, ManifestXmlTags.DESCRIPTION, description);
		}

		if(icon!=null) {
			String iconString = ManifestXmlUtils.serialize(icon);
			if(iconString==null || XmlUtils.hasIllegalAttributeSymbols(iconString)) {
				iconString = ImageSerializer.icon2String(icon);
				writeElement(serializer, ManifestXmlTags.ICON, iconString);
			}
		}
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
			serializer.writeTextOrCData(type.toChars(value));
		}
	}

	public static void writeEvalElement(XmlSerializer serializer, Expression expression) throws Exception {
		serializer.startElement(ManifestXmlTags.EVAL);

		if(expression.hasVariables()) {
			for(VariableDescriptor variableDescriptor : expression.getVariables().getVariables()) {
				serializer.startEmptyElement(ManifestXmlTags.VARIABLE);
				serializer.writeAttribute(ManifestXmlAttributes.NAME, variableDescriptor.getName());
				serializer.writeAttribute(ManifestXmlAttributes.CLASS, variableDescriptor.getNamespaceClass().getName());

				//FIXME introduce a workaround to carry on plugin information on the variable level
//				ClassLoader loader = variable.getNamespaceClass().getClassLoader();
//				if(PluginUtil.isPluginClassLoader(loader)) {
//					PluginDescriptor descriptor = PluginUtil.getDescriptor(loader);
//					serializer.writeAttribute(PLUGIN_ID, descriptor.getId());
//				}

				serializer.endElement(ManifestXmlTags.VARIABLE);
			}

		}

		serializer.startElement(ManifestXmlTags.CODE);
		serializer.writeCData(expression.getCode());
		serializer.endElement(ManifestXmlTags.CODE);

		serializer.endElement(ManifestXmlTags.EVAL);
	}

	public static void writeResourceElement(XmlSerializer serializer, Resource resource) throws Exception {
		if(resource.getUri()==null)
			throw new IllegalStateException("Resource is missing url"); //$NON-NLS-1$

		serializer.startElement(ManifestXmlTags.RESOURCE);
		ManifestXmlUtils.writeIdentityAttributes(serializer, resource);
		serializer.writeTextOrCData(resource.getUri().toString());
		serializer.endElement(ManifestXmlTags.RESOURCE);
	}

	public static void writePrerequisiteElement(XmlSerializer serializer, PrerequisiteManifest manifest) throws Exception {

		serializer.startEmptyElement(ManifestXmlTags.PREREQUISITE);

		// ATTRIBUTES

		serializer.writeAttribute(ManifestXmlAttributes.CONTEXT_ID, manifest.getContextId());
		serializer.writeAttribute(ManifestXmlAttributes.LAYER_ID, manifest.getLayerId());

		// Only write the layer type attribute for unresolved prerequisites!
		if(manifest.getUnresolvedForm()==null) {
			serializer.writeAttribute(ManifestXmlAttributes.TYPE_ID, manifest.getTypeId());
		}

		serializer.writeAttribute(ManifestXmlAttributes.ALIAS, manifest.getAlias());
		serializer.writeAttribute(ManifestXmlAttributes.DESCRIPTION, manifest.getDescription());

		serializer.endElement(ManifestXmlTags.PREREQUISITE);
	}

	public static void writeNoteElement(XmlSerializer serializer, Note note) throws Exception {
		if(note==null) {
			return;
		}

		serializer.startElement(ManifestXmlTags.NOTE);

		// Attributes

		serializer.writeAttribute(ManifestXmlAttributes.NAME, note.getName());
		serializer.writeAttribute(ManifestXmlAttributes.DATE, DateUtils.formatDate(note.getModificationDate()));

		// Content

		serializer.writeTextOrCData(note.getContent());

		serializer.endElement(ManifestXmlTags.NOTE);
	}

	//*******************************************
	//               READ METHOD
	//*******************************************

	public static void readIdentityAttributes(Attributes attr, ModifiableIdentity identity) {
		String id = normalize(attr, ManifestXmlAttributes.ID);
		if(id!=null) {
			identity.setId(id);
		}

		String name = normalize(attr, ManifestXmlAttributes.NAME);
		if(name!=null) {
			identity.setName(name);
		}

		String description = normalize(attr, ManifestXmlAttributes.DESCRIPTION);
		if(description!=null) {
			identity.setDescription(description);
		}

		String icon = normalize(attr, ManifestXmlAttributes.ICON);
		if(icon!=null) {
			identity.setIcon(iconValue(icon));
		}
	}

	public static Category readCategory(Attributes attr) {

		DefaultCategory category = new DefaultCategory();

		String namespace = normalize(attr, ManifestXmlAttributes.NAMESPACE);
		if(namespace!=null) {
			category.setNamespace(namespace);
		}

		readIdentityAttributes(attr, category);

		// Returned value is to be treated as read-only
		category.lock();

		return category;
	}

	public static Icon iconValue(String text, boolean allowSerializedForm) {
		IconRegistry iconRegistry = IconRegistry.getGlobalRegistry();
		// Try icon name first (can occur if it contains illegal attribute symbols)
		if(!allowSerializedForm || iconRegistry.hasIconInfo(text)) {
			return new IconWrapper(text);
		} else {
			// Otherwise assume we have a serialized image here
			return ImageSerializer.string2Icon(text);
		}
	}

	public static Icon iconValue(String iconName) {
		return iconValue(iconName, false);
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
		String s = normalize(attr, ManifestXmlAttributes.VALUE_TYPE);
		return typeValue(s);
	}

	public static ValueType typeValue(String s) {
		return s==null ? ValueType.STRING : ValueType.parseValueType(s);
	}

	public static Boolean boolValue(Attributes attr, String key) {
		String s = normalize(attr, key);
		return s==null ? null : _boolean(booleanValue(s));
	}

	public static String normalize(Attributes attr, String name) {
		String value = attr.getValue(name);

		return normalize(value);
	}

	public static String normalize(String value) {
		return (value==null || value.isEmpty()) ? null : value;
	}

	public static String serialize(Object value) {
		if(value == null) {
			return null;
		} else if(value instanceof StringResource) {
			return ((StringResource) value).getStringValue();
		} else if(value instanceof NamedObject) {
			return ((NamedObject) value).getName();
		}

		// If we couldn't find any way to perform simple serialization, default to null
		return null;
	}
}
