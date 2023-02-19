/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.tabular.xml;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteType;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.io.ObjectWriter;
import de.ims.icarus2.util.xml.XmlSerializer;
import de.ims.icarus2.util.xml.XmlUtils;
import de.ims.icarus2.util.xml.stream.XmlStreamSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class TableSchemaXmlWriter implements ObjectWriter<TableSchema> {

	public static void writeDefaultXsiInfo(XmlSerializer serializer) throws XMLStreamException {
		serializer.writeAttribute("xmlns:xsi", XmlUtils.XSI_NS_URI);
		serializer.writeSchemaInfo();
		serializer.writeAttribute("xsi:schemaLocation",
				TableSchemaXmlConstants.NS_URI+" "+TableSchemaXmlConstants.SCHEMA_NAME);
	}

	private XmlStreamSerializer serializer;
	private boolean xsiInfoWritten = false;

	/**
	 * @see de.ims.icarus2.util.io.ObjectWriter#init(java.io.Writer, de.ims.icarus2.util.Options)
	 */
	@Override
	public void init(Writer output, Options options) {
		try {
			serializer = XmlStreamSerializer.withNamespace(
					XMLOutputFactory.newFactory().createXMLStreamWriter(output),
					TableSchemaXmlConstants.NS_PREFIX, TableSchemaXmlConstants.NS_URI);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IcarusRuntimeException(GlobalErrorCode.DELEGATION_FAILED, "Unable to create xml stream writer", e);
		}
	}

	/**
	 * @see de.ims.icarus2.util.io.ObjectWriter#writeHeader()
	 */
	@Override
	public void writeHeader() throws IOException {
		try {
			serializer.startDocument();
		} catch (XMLStreamException e) {
			throw new IOException("Failed to start document", e);
		}
	}

	/**
	 * @see de.ims.icarus2.util.io.ObjectWriter#writeFooter()
	 */
	@Override
	public void writeFooter() throws IOException {
		try {
			serializer.endDocument();
		} catch (XMLStreamException e) {
			throw new IOException("Failed to end document", e);
		}
	}

	/**
	 * @see de.ims.icarus2.util.io.ObjectWriter#write(java.lang.Object)
	 */
	@Override
	public void write(TableSchema element) throws IOException, InterruptedException {
		try {
			writeTableSchema(element);
		} catch (Exception e) {
			throw new IOException("Failed to write table schema", e);
		}
	}

	private void writeTableSchema(TableSchema schema) throws Exception {
		serializer.startElement(TableSchemaXmlConstants.TAG_TABLE);
		writeDefaultXsiInfo(serializer);

		// ATTRIBUTES

		// Identity
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_ID, schema.getId());
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_NAME, schema.getName());

		// Group ID
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_GROUP_ID, schema.getGroupId());

		// ELEMENTS

		// Description
		writeElement(TableSchemaXmlConstants.TAG_DESCRIPTION, schema.getDescription().orElse(null));

		// Separator
		writeElement(TableSchemaXmlConstants.TAG_SEPARATOR, schema.getSeparator());

		// Root block
		writeBlockSchema(TableSchemaXmlConstants.TAG_BLOCK, schema.getRootBlock());

		serializer.endElement(TableSchemaXmlConstants.TAG_TABLE);
	}

	private void writeBlockSchema(String tag, BlockSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		serializer.startElement(tag);

		// ATTRIBUTES

		// Layer ID
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_LAYER_ID, schema.getLayerId());

		// No-entry label
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_NO_ENTRY_LABEL, schema.getNoEntryLabel());

		// Column order fixed
		if(schema.isColumnOrderFixed()!=BlockSchema.DEFAULT_COLUMN_ORDER_FIXED) {
			serializer.writeAttribute(TableSchemaXmlConstants.ATTR_COLUMN_ORDER_FIXED, schema.isColumnOrderFixed());
		}

		// ELEMENTS

		// Separator
		writeElement(TableSchemaXmlConstants.TAG_SEPARATOR, schema.getSeparator());

		// Options
		writeOptions(schema.getOptions());

		// Component
		writeMemberSchema(TableSchemaXmlConstants.TAG_COMPONENT, schema.getComponentSchema());

		// External groups
		String[] groups = schema.getExternalGroupIds();
		if(groups.length>0) {
			for(String group : groups) {
				writeElement(TableSchemaXmlConstants.TAG_EXTERNAL_GROUP, group);
			}
		}

		// Begin delimiter
		writeAttributeSchema(TableSchemaXmlConstants.TAG_BEGIN_DELIMITER, schema.getBeginDelimiter());

		// End delimiter
		writeAttributeSchema(TableSchemaXmlConstants.TAG_END_DELIMITER, schema.getEndDelimiter());

		// Remaining attributes
		AttributeSchema[] attributes = schema.getAttributes();
		if(attributes.length>0) {
			serializer.startElement(TableSchemaXmlConstants.TAG_ATTRIBUTES);
			for(AttributeSchema attributeSchema : attributes) {
				writeAttributeSchema(TableSchemaXmlConstants.TAG_ATTRIBUTE, attributeSchema);
			}
			serializer.endElement(TableSchemaXmlConstants.TAG_ATTRIBUTES);
		}

		// Columns
		ColumnSchema[] columns = schema.getColumns();
		if(columns.length>0) {
			serializer.startElement(TableSchemaXmlConstants.TAG_COLUMNS);
			for(ColumnSchema columnSchema : columns) {
				writeColumnSchema(TableSchemaXmlConstants.TAG_COLUMN, columnSchema);
			}
			serializer.endElement(TableSchemaXmlConstants.TAG_COLUMNS);
		}

		// Fallback column
		writeColumnSchema(TableSchemaXmlConstants.TAG_FALLBACK_COLUMN, schema.getFallbackColumn());

		// Nested blocks
		BlockSchema[] nestedBlocks = schema.getNestedBlocks();
		if(nestedBlocks.length>0) {
			serializer.startElement(TableSchemaXmlConstants.TAG_NESTED_BLOCKS);
			for(BlockSchema blockSchema : nestedBlocks) {
				writeBlockSchema(TableSchemaXmlConstants.TAG_BLOCK, blockSchema);
			}
			serializer.endElement(TableSchemaXmlConstants.TAG_NESTED_BLOCKS);
		}

		serializer.endElement(tag);
	}

	private void writeOptions(Options options) throws XMLStreamException {
		if(options!=null && !options.isEmpty()) {
			for(String key : options.keySet()) {
				String value = String.valueOf(options.get(key));

				serializer.startElement(TableSchemaXmlConstants.TAG_OPTION);
				serializer.writeAttribute(TableSchemaXmlConstants.ATTR_NAME, key);
				serializer.writeTextOrCData(value);
				serializer.endElement(TableSchemaXmlConstants.TAG_OPTION);
			}
		}
	}

	private void writeMemberSchema(String tag, MemberSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		// ATTRIBUTES

		serializer.startEmptyElement(tag);
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_MEMBER_TYPE, schema.getMemberType().getStringValue());
		if(schema.isReference()!=MemberSchema.DEFAULT_IS_REFERENCE) {
			serializer.writeAttribute(TableSchemaXmlConstants.ATTR_REFERENCE, schema.isReference());
		}
		serializer.endElement(tag);
	}

	private void writeAttributeSchema(String tag, AttributeSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		boolean isEmpty = schema.getResolver()==null && schema.getPattern()==null;
		serializer.startElement(tag, isEmpty);

		// ATTRIBUTES

		// Target
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_TARGET, schema.getTarget());
		// Type
		if(schema.getType()!=AttributeSchema.DEFAULT_TYPE) {
			serializer.writeAttribute(TableSchemaXmlConstants.ATTR_TYPE, schema.getType());
		}
		// Shared
		if(schema.isShared()!=AttributeSchema.DEFAULT_SHARED) {
			serializer.writeAttribute(TableSchemaXmlConstants.ATTR_SHARED, schema.isShared());
		}

		// ELEMENTS

		// Pattern
		writeElement(TableSchemaXmlConstants.TAG_PATTERN, schema.getPattern());

		// Resolver
		writeResolverSchema(TableSchemaXmlConstants.TAG_RESOLVER, schema.getResolver());

		serializer.endElement(tag);
	}

	private void writeResolverSchema(String tag, ResolverSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		Options options = schema.getOptions();

		boolean isEmpty = options==null || options.isEmpty();
		serializer.startElement(tag, isEmpty);

		// ATTRIBUTES

		// Type (implementing class)
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_TYPE, schema.getType());

		// ELEMENTS

		// Options
		writeOptions(options);

		serializer.endElement(tag);
	}

	private void writeColumnSchema(String tag, ColumnSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		boolean isEmpty = schema.getResolver()==null && !schema.hasSubstitutes();
		serializer.startElement(tag, isEmpty);

		// ATTRIBUTES

		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_NAME, schema.getName());
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_ANNOTATION_KEY, schema.getAnnotationKey());
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_NO_ENTRY_LABEL, schema.getNoEntryLabel());
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_LAYER_ID, schema.getLayerId());
		if(schema.isIgnoreColumn()!=ColumnSchema.DEFAULT_IGNORE_COLUMN) {
			serializer.writeAttribute(TableSchemaXmlConstants.ATTR_IGNORE, schema.isIgnoreColumn());
		}
		if(schema.getColumnIndex()!=UNSET_INT) {
			serializer.writeAttribute(TableSchemaXmlConstants.ATTR_COLUMN_NDEX, schema.getColumnIndex());
		}

		// ELEMENTS

		// Substitutes
		if(schema.hasSubstitutes()) {
			writeSubstituteSchema(TableSchemaXmlConstants.TAG_SUBSTITUTE, schema.getSubstitute(SubstituteType.ADDITION));
			writeSubstituteSchema(TableSchemaXmlConstants.TAG_SUBSTITUTE, schema.getSubstitute(SubstituteType.REPLACEMENT));
			writeSubstituteSchema(TableSchemaXmlConstants.TAG_SUBSTITUTE, schema.getSubstitute(SubstituteType.TARGET));
		}

		// Resolver
		writeResolverSchema(TableSchemaXmlConstants.TAG_RESOLVER, schema.getResolver());

		serializer.endElement(tag);
	}

	private void writeSubstituteSchema(String tag, SubstituteSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		serializer.startEmptyElement(TableSchemaXmlConstants.TAG_SUBSTITUTE);
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_TYPE, schema.getType());
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_NAME, schema.getName());
		serializer.writeAttribute(TableSchemaXmlConstants.ATTR_MEMBER_TYPE, schema.getMemberType());
		serializer.endElement(TableSchemaXmlConstants.TAG_SUBSTITUTE);
	}

	private void writeElement(String tag, String text) throws XMLStreamException {
		if(text!=null) {
			serializer.startElement(tag);
			serializer.writeTextOrCData(text);
			serializer.endElement(tag);
		}
	}

	/**
	 * @see de.ims.icarus2.util.io.ObjectWriter#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			serializer.close();
		} catch (Exception e) {
			throw new IOException("Failed to close xml serializer", e);
		} finally {
			serializer = null;
		}
	}

}
