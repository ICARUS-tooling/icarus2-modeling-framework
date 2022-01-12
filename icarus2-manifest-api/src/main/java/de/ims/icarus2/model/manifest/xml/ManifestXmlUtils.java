/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest.Note;
import de.ims.icarus2.model.manifest.api.Documentation.Resource;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.standard.DefaultCategory;
import de.ims.icarus2.model.manifest.types.ValueConversionException;
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
		throw new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Instantiation not supported");
	}

	public static final String MANIFEST_LABEL = "manifest";

	public static final String MANIFEST_NAMESPACE_URI = XmlUtils.ICARUS_NS_URI+MANIFEST_LABEL;

	public static final String MANIFEST_NS_PREFIX = XmlUtils.ICARUS_NS;

	public static final String SCHEMA_NAME = MANIFEST_NAMESPACE_URI+"/corpus.xsd";

	public static void writeDefaultXsiInfo(XmlSerializer serializer) throws XMLStreamException {
		serializer.writeAttribute("xmlns:xsi", XmlUtils.XSI_NS_URI);
		serializer.writeSchemaInfo();
		serializer.writeAttribute("xsi:schemaLocation", MANIFEST_NAMESPACE_URI+" "+SCHEMA_NAME);
	}

	/**
	 * Check whether a flag is set and differs from a given default value. Only if both conditions are met
	 * will the flag be written to the provided serializer, using the specified name as identifier.
	 */
	public static void writeFlag(XmlSerializer serializer, String name,
			@Nullable Boolean flag, boolean defaultValue) throws XMLStreamException {
		if(flag!=null && flag.booleanValue()!=defaultValue) {
			serializer.writeAttribute(name, flag.booleanValue());
		}
	}
	public static void writeFlag(XmlSerializer serializer, String name,
			boolean flag, boolean defaultValue) throws XMLStreamException {
		if(flag!=defaultValue) {
			serializer.writeAttribute(name, flag);
		}
	}

	public static Optional<Boolean> readFlag(Attributes attributes, String name, boolean defaultValue) {
		return normalize(attributes, name)
				.map(Boolean::valueOf)
				.filter(b -> b.booleanValue()!=defaultValue);
	}

	//*******************************************
	//               WRITE METHODS
	//*******************************************

	public static Optional<String> getSerializedForm(@Nullable ValueType type) {
//		return type==ValueType.STRING ? null : type.getXmlValue();

		return Optional.ofNullable(type==null ? null : type.getStringValue());
	}

	public static void writeIdentityAttributes(XmlSerializer serializer,
			@Nullable Identity identity) throws XMLStreamException {
		if(identity==null) {
			return;
		}

		serializer.writeAttribute(ManifestXmlAttributes.ID, identity.getId());
		serializer.writeAttribute(ManifestXmlAttributes.NAME, identity.getName());
		serializer.writeAttribute(ManifestXmlAttributes.DESCRIPTION, identity.getDescription());
	}

	public static void writeCategoryAttributes(XmlSerializer serializer, Category category) throws XMLStreamException {
		if(category==null) {
			return;
		}

		writeIdentityAttributes(serializer, category);

		serializer.writeAttribute(ManifestXmlAttributes.NAMESPACE, category.getNamespace());
	}

	public static Optional<String> getSerializedForm(TargetLayerManifest manifest) {
//		Optional<PrerequisiteManifest> optPrereq = manifest.getPrerequisite();
//		return optPrereq.isPresent() ? optPrereq.map(PrerequisiteManifest::getAlias)
//				: manifest.getResolvedLayerManifest().flatMap(LayerManifest::getId);
		return Optional.of(manifest.getLayerId());
	}

	/**
	 * Writes the given target layer. Uses as layer id the alias of the prerequisite of the
	 * link if present, or the resolved layer's id otherwise.
	 */
	public static void writeTargetLayerManifestElement(XmlSerializer serializer, String name,
			@Nullable TargetLayerManifest manifest) throws XMLStreamException {
		if(manifest==null) {
			return;
		}

		serializer.startEmptyElement(name);

		// ATTRIBUTES
		serializer.writeAttribute(ManifestXmlAttributes.LAYER_ID, getSerializedForm(manifest));

		serializer.endElement(name);
	}

	public static void writeAliasElement(XmlSerializer serializer, String alias) throws XMLStreamException {
		serializer.startEmptyElement(ManifestXmlTags.ALIAS);
		serializer.writeAttribute(ManifestXmlAttributes.NAME, alias);
		serializer.endElement(ManifestXmlTags.ALIAS);
	}

	public static void writeElement(XmlSerializer serializer, String name, @Nullable String content) throws XMLStreamException {
		serializer.startEmptyElement(name);
		serializer.writeTextOrCData(content);
		serializer.endElement(name);
	}

	private static boolean maybeWriteElement(XmlSerializer serializer, String name, Optional<String> content) throws XMLStreamException {
		if(content.isPresent() && XmlUtils.hasIllegalAttributeSymbols(content.get())) {
			serializer.startElement(name);
			serializer.writeTextOrCData(content.get());
			serializer.endElement(name);
			return true;
		}

		return false;
	}

	public static void writeIdentityElement(XmlSerializer serializer,
			String name, Identity identity) throws XMLStreamException {
		boolean empty = XmlUtils.isLegalAttribute(identity.getName())
				&& XmlUtils.isLegalAttribute(identity.getDescription());

		serializer.startElement(name, empty);
		writeIdentityAttributes(serializer, identity);
		writeIdentityFieldElements(serializer, identity);
		serializer.endElement(name);
	}

	public static boolean writeIdentityFieldElements(XmlSerializer serializer, Identity identity) throws XMLStreamException {
		boolean elementsWritten = false;
		elementsWritten |= maybeWriteElement(serializer, ManifestXmlTags.NAME, identity.getName());
		elementsWritten |= maybeWriteElement(serializer, ManifestXmlTags.DESCRIPTION, identity.getDescription());
		return elementsWritten;
	}

	public static void writeIdentityFieldElements(XmlSerializer serializer, String name, String description) throws XMLStreamException {


		// Nest identity information if needed
		if(name!=null && XmlUtils.hasIllegalAttributeSymbols(name)) {
			writeElement(serializer, ManifestXmlTags.NAME, name);
		}

		if(description!=null && XmlUtils.hasIllegalAttributeSymbols(description)) {
			writeElement(serializer, ManifestXmlTags.DESCRIPTION, description);
		}
	}

	public static void writeValueElement(XmlSerializer serializer, String name,
			@Nullable Object value, ValueType type) throws XMLStreamException {
		if(value instanceof Optional) {
			value = ((Optional<?>) value).orElse(null);
		}

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

	public static void writeValue(XmlSerializer serializer,
			@Nullable Object value, ValueType type) throws XMLStreamException {
		if(value instanceof Optional) {
			value = ((Optional<?>) value).orElse(null);
		}

		if(value==null) {
			return;
		}

		if(value instanceof Expression) {
			writeEvalElement(serializer, (Expression)value);
		} else {
			try {
				serializer.writeTextOrCData(type.toChars(value));
			} catch (ValueConversionException e) {
				throw new XMLStreamException("Failed to write value: "+value, e);
			}
		}
	}

	public static void writeEvalElement(XmlSerializer serializer, Expression expression) throws XMLStreamException {
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
		serializer.writeTextOrCData(expression.getCode());
		serializer.endElement(ManifestXmlTags.CODE);

		serializer.endElement(ManifestXmlTags.EVAL);
	}

	public static void writeResourceElement(XmlSerializer serializer, Resource resource) throws XMLStreamException {
		if(resource.getUri()==null)
			throw new IllegalStateException("Resource is missing url"); //$NON-NLS-1$

		serializer.startElement(ManifestXmlTags.RESOURCE);
		ManifestXmlUtils.writeIdentityAttributes(serializer, resource);
		serializer.writeTextOrCData(resource.getUri().toString());
		serializer.endElement(ManifestXmlTags.RESOURCE);
	}

	public static void writePrerequisiteElement(XmlSerializer serializer, PrerequisiteManifest manifest) throws XMLStreamException {

		serializer.startEmptyElement(ManifestXmlTags.PREREQUISITE);

		// ATTRIBUTES

		serializer.writeAttribute(ManifestXmlAttributes.CONTEXT_ID, manifest.getContextId());
		serializer.writeAttribute(ManifestXmlAttributes.LAYER_ID, manifest.getLayerId());

		// Only write the layer type attribute for unresolved prerequisites!
		if(!manifest.getUnresolvedForm().isPresent()) {
			serializer.writeAttribute(ManifestXmlAttributes.TYPE_ID, manifest.getTypeId());
		}

		serializer.writeAttribute(ManifestXmlAttributes.ALIAS, manifest.getAlias());
		serializer.writeAttribute(ManifestXmlAttributes.DESCRIPTION, manifest.getDescription());

		serializer.endElement(ManifestXmlTags.PREREQUISITE);
	}

	public static void writeNoteElement(XmlSerializer serializer, @Nullable Note note) throws XMLStreamException {
		if(note==null) {
			return;
		}

		serializer.startElement(ManifestXmlTags.NOTE);

		// Attributes

		serializer.writeAttribute(ManifestXmlAttributes.NAME, note.getName());
		serializer.writeAttribute(ManifestXmlAttributes.DATE,
				note.getModificationDate().flatMap(DateUtils::formatDate));

		// Content

		serializer.writeTextOrCData(note.getContent());

		serializer.endElement(ManifestXmlTags.NOTE);
	}

	//*******************************************
	//               READ METHOD
	//*******************************************

	public static void readIdentityAttributes(Attributes attr, ModifiableIdentity identity) {
		normalize(attr, ManifestXmlAttributes.ID).ifPresent(identity::setId);
		normalize(attr, ManifestXmlAttributes.NAME).ifPresent(identity::setName);
		normalize(attr, ManifestXmlAttributes.DESCRIPTION).ifPresent(identity::setDescription);
	}

	public static Category readCategory(Attributes attr) {

		DefaultCategory category = new DefaultCategory();

		normalize(attr, ManifestXmlAttributes.NAMESPACE).ifPresent(category::setNamespace);

		readIdentityAttributes(attr, category);

		// Returned value is to be treated as read-only
		category.lock();

		return category;
	}

	public static Optional<Icon> iconValue(@Nullable String text, boolean allowSerializedForm) {
		if(text==null) {
			return Optional.empty();
		}

		IconRegistry iconRegistry = IconRegistry.getGlobalRegistry();
		// Try icon name first (can occur if it contains illegal attribute symbols)
		if(!allowSerializedForm || iconRegistry.hasIconInfo(text)) {
			return Optional.of(new IconWrapper(text));
		}

		// Otherwise assume we have a serialized image here
		return Optional.of(ImageSerializer.string2Icon(text));
	}

	public static Optional<Icon> iconValue(@Nullable String iconName) {
		return iconValue(iconName, false);
	}

	public static Optional<Icon> iconValue(Attributes attr, String key) {
		return normalize(attr, key).flatMap(ManifestXmlUtils::iconValue);
	}

	public static Optional<String> stringValue(Attributes attr, String key) {
		return normalize(attr, key);
	}

	public static long longValue(String s) {
		return Long.parseLong(s);
	}

	public static long longValue(Attributes attr, String key, long defaultValue) {
		return normalize(attr, key)
				.map(ManifestXmlUtils::longValue)
				.orElse(Long.valueOf(defaultValue))
				.longValue();
	}

	public static double doubleValue(String s) {
		return Double.parseDouble(s);
	}

	public static double doubleValue(Attributes attr, String key, double defaultValue) {
		return normalize(attr, key)
				.map(ManifestXmlUtils::doubleValue)
				.orElse(Double.valueOf(defaultValue))
				.doubleValue();
	}

	public static float floatValue(String s) {
		return Float.parseFloat(s);
	}

	public static float floatValue(Attributes attr, String key, float defaultValue) {
		return normalize(attr, key)
				.map(ManifestXmlUtils::floatValue)
				.orElse(Float.valueOf(defaultValue))
				.floatValue();
	}

	public static int intValue(String s) {
		return Integer.parseInt(s);
	}

	public static int intValue(Attributes attr, String key, int defaultValue) {
		return normalize(attr, key)
				.map(ManifestXmlUtils::intValue)
				.orElse(Integer.valueOf(defaultValue))
				.intValue();
	}

	public static boolean booleanValue(String s) {
		return s!=null && ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static Optional<Boolean> booleanValue(Attributes attr, String key) {
		return normalize(attr, key)
				.map(ManifestXmlUtils::booleanValue);
	}

	public static Optional<Boolean> booleanValue(Attributes attr, String key, boolean defaultValue) {
		return booleanValue(attr, key).filter(b -> b.booleanValue()!=defaultValue);
	}

	public static Optional<ValueType> typeValue(Attributes attr) {
		return normalize(attr, ManifestXmlAttributes.VALUE_TYPE)
				.map(ManifestXmlUtils::typeValue);
	}

	public static ValueType typeValue(String s) {
		return s==null ? ValueType.STRING : ValueType.parseValueType(s);
	}

	public static Optional<String> normalize(Attributes attr, String name) {
		String value = attr.getValue(name);

		return normalize(value);
	}

	public static Optional<String> normalize(String value) {
		return Optional.ofNullable((value==null || value.isEmpty()) ? null : value);
	}

	public static Optional<String> serializeIcon(Icon icon) {
		if(icon==null) {
			return Optional.empty();
		}

		String iconString = serialize(icon).orElse(null);
		if(iconString==null || XmlUtils.hasIllegalAttributeSymbols(iconString)) {
			iconString = ImageSerializer.icon2String(icon);
		}

		return Optional.of(iconString);
	}

	/**
	 * Tries to serialize values. Unpacks {@link Optional} arguments first.
	 * @param value
	 * @return
	 */
	public static Optional<String> serialize(Object value) {
		if(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}

		if(value == null) {
			return Optional.empty();
		} else if(value instanceof StringResource) {
			return Optional.of(((StringResource) value).getStringValue());
		} else if(value instanceof NamedObject) {
			return Optional.ofNullable(((NamedObject) value).getName());
		}

		// If we couldn't find any way to perform simple serialization, default to null
		return Optional.empty();
	}

	public static Object parse(ValueType type, ManifestLocation location, CharSequence input, boolean persist) throws SAXException {

		Object value;

		try {
			value = type.parse(input, location.getClassLoader());
		} catch (ValueConversionException e) {
			throw new SAXException("Failed to parse input of type '"+type+"': "+input, e);
		}

		if(persist) {
			value = type.persist(value);
		}

		return value;
	}
}
