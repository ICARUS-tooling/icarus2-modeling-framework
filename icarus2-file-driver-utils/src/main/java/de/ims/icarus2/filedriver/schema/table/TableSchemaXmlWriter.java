/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.table;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.io.ObjectWriter;
import de.ims.icarus2.util.xml.stream.XmlStreamSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class TableSchemaXmlWriter implements ObjectWriter<TableSchema>, TableSchemaXmlConstants {

	private XmlStreamSerializer serializer;

	/**
	 * @see de.ims.icarus2.util.io.ObjectWriter#init(java.io.Writer, de.ims.icarus2.util.Options)
	 */
	@Override
	public void init(Writer output, Options options) {
		try {
			serializer = new XmlStreamSerializer(XMLOutputFactory.newFactory().createXMLStreamWriter(output));
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IcarusException(GlobalErrorCode.DELEGATION_FAILED, "Unable to create xml stream writer", e);
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
		serializer.startElement(TAG_TABLE);

		// ATTRIBUTES

		// Identity
		serializer.writeAttribute(ATTR_ID, schema.getId());
		serializer.writeAttribute(ATTR_NAME, schema.getName());

		// Group ID
		serializer.writeAttribute(ATTR_GROUP_ID, schema.getGroupId());

		// ELEMENTS

		// Description
		writeElement(TAG_DESCRIPTION, schema.getDescription());

		// Separator
		writeElement(TAG_SEPARATOR, schema.getSeparator());

		// Root block
		writeBlockSchema(TAG_BLOCK, schema.getRootBlock());

		serializer.endElement(TAG_TABLE);
	}

	private void writeBlockSchema(String tag, BlockSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		serializer.startElement(tag);

		// ATTRIBUTES

		// Layer ID
		serializer.writeAttribute(ATTR_LAYER_ID, schema.getLayerId());

		// No-entry label
		serializer.writeAttribute(ATTR_NO_ENTRY_LABEL, schema.getNoEntryLabel());

		// Column order fixed
		if(schema.isColumnOrderFixed()!=BlockSchema.DEFAULT_COLUMN_ORDER_FIXED) {
			serializer.writeAttribute(ATTR_COLUMN_ORDER_FIXED, schema.isColumnOrderFixed());
		}

		// ELEMENTS

		// Separator
		writeElement(TAG_SEPARATOR, schema.getSeparator());

		// Options
		writeOptions(schema.getOptions());

		// Component
		writeMemberSchema(TAG_COMPONENT, schema.getComponentSchema());

		// Begin delimiter
		writeAttributeSchema(TAG_BEGIN_DELIMITER, schema.getBeginDelimiter());

		// End delimiter
		writeAttributeSchema(TAG_END_DELIMITER, schema.getEndDelimiter());

		// Remaining attributes
		AttributeSchema[] attributes = schema.getAttributes();
		if(attributes!=null && attributes.length>0) {
			serializer.startElement(TAG_ATTRIBUTES);
			for(AttributeSchema attributeSchema : attributes) {
				writeAttributeSchema(TAG_ATTRIBUTE, attributeSchema);
			}
			serializer.endElement(TAG_ATTRIBUTES);
		}

		// Columns
		ColumnSchema[] columns = schema.getColumns();
		if(columns!=null && columns.length>0) {
			serializer.startElement(TAG_COLUMNS);
			for(ColumnSchema columnSchema : columns) {
				writeColumnSchema(TAG_COLUMN, columnSchema);
			}
			serializer.endElement(TAG_COLUMNS);
		}

		// Fallback column
		writeColumnSchema(TAG_FALLBACK_COLUMN, schema.getFallbackColumn());

		// Nested blocks
		BlockSchema[] nestedBlocks = schema.getNestedBlocks();
		if(nestedBlocks!=null && nestedBlocks.length>0) {
			serializer.startElement(TAG_NESTED_BLOCKS);
			for(BlockSchema blockSchema : nestedBlocks) {
				writeBlockSchema(TAG_BLOCK, blockSchema);
			}
			serializer.endElement(TAG_NESTED_BLOCKS);
		}

		serializer.endElement(tag);
	}

	private void writeOptions(Options options) throws XMLStreamException {
		if(options!=null && !options.isEmpty()) {
			for(String key : options.keySet()) {
				String value = String.valueOf(options.get(key));

				serializer.startElement(TAG_OPTION);
				serializer.writeAttribute(ATTR_NAME, key);
				serializer.writeTextOrCData(value);
				serializer.endElement(TAG_OPTION);
			}
		}
	}

	private void writeMemberSchema(String tag, MemberSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		// ATTRIBUTES

		serializer.startEmptyElement(tag);
		serializer.writeAttribute(ATTR_MEMBER_TYPE, schema.getMemberType().getStringValue());
		if(schema.isReference()!=MemberSchema.DEFAULT_IS_REFERENCE) {
			serializer.writeAttribute(ATTR_REFERENCE, schema.isReference());
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
		serializer.writeAttribute(ATTR_TARGET, schema.getTarget());

		// ELEMENTS

		// Pattern
		writeElement(TAG_PATTERN, schema.getPattern());

		// Resolver
		writeResolverSchema(TAG_RESOLVER, schema.getResolver());

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
		serializer.writeAttribute(ATTR_TYPE, schema.getType());

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

		serializer.writeAttribute(ATTR_NAME, schema.getName());
		serializer.writeAttribute(ATTR_ANNOTATION_KEY, schema.getAnnotationKey());
		serializer.writeAttribute(ATTR_NO_ENTRY_LABEL, schema.getNoEntryLabel());
		serializer.writeAttribute(ATTR_LAYER_ID, schema.getLayerId());
		if(schema.isIgnoreColumn()!=ColumnSchema.DEFAULT_IGNORE_COLUMN) {
			serializer.writeAttribute(ATTR_IGNORE, schema.isIgnoreColumn());
		}

		// ELEMENTS

		// Substitutes
		if(schema.hasSubstitutes()) {
			writeSubstituteSchema(TAG_SUBSTITUTE, schema.getSubstitute(SubstituteType.ADDITION));
			writeSubstituteSchema(TAG_SUBSTITUTE, schema.getSubstitute(SubstituteType.REPLACEMENT));
			writeSubstituteSchema(TAG_SUBSTITUTE, schema.getSubstitute(SubstituteType.TARGET));
		}

		// Resolver
		writeResolverSchema(TAG_RESOLVER, schema.getResolver());

		serializer.endElement(tag);
	}

	private void writeSubstituteSchema(String tag, SubstituteSchema schema) throws XMLStreamException {
		if(schema==null) {
			return;
		}

		serializer.startEmptyElement(TAG_SUBSTITUTE);
		serializer.writeAttribute(ATTR_TYPE, schema.getType());
		serializer.writeAttribute(ATTR_NAME, schema.getName());
		serializer.writeAttribute(ATTR_MEMBER_TYPE, schema.getMemberType());
		serializer.endElement(TAG_SUBSTITUTE);
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
