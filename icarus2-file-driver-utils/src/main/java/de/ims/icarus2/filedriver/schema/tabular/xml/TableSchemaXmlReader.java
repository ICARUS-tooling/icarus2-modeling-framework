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
package de.ims.icarus2.filedriver.schema.tabular.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.ims.icarus2.filedriver.schema.tabular.TableSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteType;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.AttributeSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.BlockSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.ColumnSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.MemberSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.ResolverSchemaImpl;
import de.ims.icarus2.filedriver.schema.tabular.TableSchemaImpl.SubstituteSchemaImpl;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.lang.Lazy;
import de.ims.icarus2.util.xml.XmlHandler;
import de.ims.icarus2.util.xml.XmlUtils;

/**
 * @author Markus Gärtner
 *
 */
public class TableSchemaXmlReader extends XmlHandler implements AutoCloseable {

	/**
	 * Creates a default {@link XMLReader} that is namespace aware
	 * and using the {@link #getDefaultSchema() default schema} for
	 * manifest instances if the {@code validate} parameter is set
	 * to {@code true}.
	 *
	 * @return
	 * @throws SAXException
	 */
	public static XMLReader defaultCreateReader(boolean validate) throws SAXException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setNamespaceAware(true);
		parserFactory.setValidating(false);
		if(validate) {
			parserFactory.setSchema(getDefaultSchema());
		}

		SAXParser parser = null;
		try {
			parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new SAXException("Parser creation failed", e);
		}

		return parser.getXMLReader();
	}

	private static final Lazy<Schema> schema = XmlUtils.createShareableSchemaSource(
			TableSchemaXmlReader.class.getResource("tabular-schema.xsd"));

	public static Schema getDefaultSchema() {
		return schema.value();
	}

	private Stack<Object> stack = new Stack<>();

	public TableSchema read(Reader input, Options options) throws IOException {
		XMLReader reader;
		try {
			reader = defaultCreateReader(true);
		} catch (SAXException e) {
			throw new IOException("Failed to prepare XML reader", e);
		}

		InputSource inputSource = new InputSource();
		reader.setContentHandler(this);
		reader.setErrorHandler(this);
		reader.setEntityResolver(this);
		reader.setDTDHandler(this);

		inputSource.setCharacterStream(input);

		TableSchema schema;
		try {
			reader.parse(inputSource);
			schema = pop(TableSchema.class);
		} catch (SAXException e) {
			throw new IOException("Failed to parse tabular schema", e);
		} finally {
			// Make sure we hold no references
			clearStack();
		}

		return schema;
	}

	@SuppressWarnings("unchecked")
	private <E extends Object> E peek(Class<E> lazz) {
		return (E) stack.peek();
	}

	@SuppressWarnings("unchecked")
	private <E extends Object> E pop(Class<E> lazz) {
		return (E) stack.pop();
	}

	private boolean isContext(Class<?> clazz) {
		return clazz.isAssignableFrom(stack.peek().getClass());
	}

	private <E extends Object> E push(E obj) {
		stack.push(obj);
		return obj;
	}

	private void clearStack() {
		stack.clear();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		switch (localName) {
		case TableSchemaXmlConstants.TAG_TABLE: {
			TableSchemaImpl tableSchema = push(new TableSchemaImpl());
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_ID)
				.ifPresent(tableSchema::setId);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_NAME)
				.ifPresent(tableSchema::setName);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_DESCRIPTION)
				.ifPresent(tableSchema::setDescription);
			// ignore icon declaration
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_GROUP_ID)
				.ifPresent(tableSchema::setGroupId);
		} break;

		case TableSchemaXmlConstants.TAG_BLOCK: {
			BlockSchemaImpl blockSchema = push(new BlockSchemaImpl());
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_LAYER_ID)
				.ifPresent(blockSchema::setLayerId);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_NO_ENTRY_LABEL)
				.ifPresent(blockSchema::setNoEntryLabel);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_COLUMN_ORDER_FIXED)
				.map(Boolean::valueOf)
				.ifPresent(blockSchema::setColumnOrderFixed);
		} break;

		case TableSchemaXmlConstants.TAG_BEGIN_DELIMITER:
		case TableSchemaXmlConstants.TAG_END_DELIMITER:
		case TableSchemaXmlConstants.TAG_ATTRIBUTE: {
			AttributeSchemaImpl attributeSchema = push(new AttributeSchemaImpl());
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_TARGET)
				.map(AttributeTarget::parseAttributeTarget)
				.ifPresent(attributeSchema::setTarget);
		} break;

		case TableSchemaXmlConstants.TAG_FALLBACK_COLUMN:
		case TableSchemaXmlConstants.TAG_COLUMN: {
			ColumnSchemaImpl columnSchema = push(new ColumnSchemaImpl());
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_NAME)
				.ifPresent(columnSchema::setName);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_ANNOTATION_KEY)
				.ifPresent(columnSchema::setAnnotationKey);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_IGNORE)
				.map(Boolean::valueOf)
				.ifPresent(columnSchema::setIsIgnoreColumn);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_LAYER_ID)
				.ifPresent(columnSchema::setLayerId);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_NO_ENTRY_LABEL)
				.ifPresent(columnSchema::setNoEntryLabel);
		} break;

		case TableSchemaXmlConstants.TAG_COMPONENT: {
			MemberSchemaImpl memberSchema = push(new MemberSchemaImpl());
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_MEMBER_TYPE)
				.map(MemberType::parseMemberType)
				.ifPresent(memberSchema::setMemberType);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_REFERENCE)
				.map(Boolean::valueOf)
				.ifPresent(memberSchema::setIsReference);
		} break;

		case TableSchemaXmlConstants.TAG_RESOLVER: {
			ResolverSchemaImpl resolverSchema = push(new ResolverSchemaImpl());
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_TYPE)
				.ifPresent(resolverSchema::setType);
		} break;

		case TableSchemaXmlConstants.TAG_OPTION: {
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_NAME)
				.ifPresent(this::push);
		} break;

		case TableSchemaXmlConstants.TAG_SUBSTITUTE: {
			SubstituteSchemaImpl substituteSchema = push(new SubstituteSchemaImpl());
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_TYPE)
				.map(SubstituteType::parseSubstituteType)
				.ifPresent(substituteSchema::setType);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_MEMBER_TYPE)
				.map(MemberType::parseMemberType)
				.ifPresent(substituteSchema::setMemberType);
			ManifestXmlUtils.normalize(attributes, TableSchemaXmlConstants.ATTR_NAME)
				.ifPresent(substituteSchema::setName);
		} break;

		// Text-only tags
		case TableSchemaXmlConstants.TAG_SEPARATOR:
		case TableSchemaXmlConstants.TAG_DESCRIPTION:
		case TableSchemaXmlConstants.TAG_PATTERN:
			break;

		// "Wrapper" tags
		case TableSchemaXmlConstants.TAG_ATTRIBUTES:
		case TableSchemaXmlConstants.TAG_COLUMNS:
			break;

		default:
			throw new SAXException("Unexpected begin tag: "+localName);
		}

		// Make sure we collect text for all valid tags
		clearText();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch (localName) {
		case TableSchemaXmlConstants.TAG_TABLE:
			// Leave table schema on the stack
			break;

		case TableSchemaXmlConstants.TAG_BLOCK: {
			BlockSchema blockSchema = pop(BlockSchema.class);
			if(isContext(BlockSchema.class)) {
				peek(BlockSchemaImpl.class).addBlock(blockSchema);
			} else {
				peek(TableSchemaImpl.class).setRootBlock(blockSchema);
			}
		} break;

		case TableSchemaXmlConstants.TAG_BEGIN_DELIMITER: {
			AttributeSchema beginDelimiter = pop(AttributeSchema.class);
			peek(BlockSchemaImpl.class).setBeginDelimiter(beginDelimiter);
		} break;

		case TableSchemaXmlConstants.TAG_END_DELIMITER: {
			AttributeSchema endDelimiter = pop(AttributeSchema.class);
			peek(BlockSchemaImpl.class).setEndDelimiter(endDelimiter);
		} break;

		case TableSchemaXmlConstants.TAG_ATTRIBUTE: {
			AttributeSchema attributeSchema = pop(AttributeSchema.class);
			peek(BlockSchemaImpl.class).addAttribute(attributeSchema);
		} break;

		case TableSchemaXmlConstants.TAG_FALLBACK_COLUMN: {
			ColumnSchema columnSchema = pop(ColumnSchema.class);
			peek(BlockSchemaImpl.class).setFallbackColumn(columnSchema);
		} break;

		case TableSchemaXmlConstants.TAG_COLUMN: {
			ColumnSchema columnSchema = pop(ColumnSchema.class);
			peek(BlockSchemaImpl.class).addColumn(columnSchema);
		} break;

		case TableSchemaXmlConstants.TAG_COMPONENT: {
			MemberSchema memberSchema = pop(MemberSchema.class);
			peek(BlockSchemaImpl.class).setComponentSchema(memberSchema);
		} break;

		case TableSchemaXmlConstants.TAG_RESOLVER: {
			ResolverSchema resolverSchema = pop(ResolverSchema.class);
			if(isContext(AttributeSchema.class)) {
				peek(AttributeSchemaImpl.class).setResolver(resolverSchema);
			} else if(isContext(ColumnSchema.class)) {
				peek(ColumnSchemaImpl.class).setResolver(resolverSchema);
			}
		} break;

		case TableSchemaXmlConstants.TAG_SUBSTITUTE: {
			SubstituteSchema substituteSchema = pop(SubstituteSchema.class);
			peek(ColumnSchemaImpl.class).addSubstitute(substituteSchema);
		} break;

		case TableSchemaXmlConstants.TAG_SEPARATOR: {
			String separator = getText();
			if(isContext(TableSchema.class)) {
				peek(TableSchemaImpl.class).setSeparator(separator);
			} else {
				peek(BlockSchemaImpl.class).setSeparator(separator);
			}
		} break;

		case TableSchemaXmlConstants.TAG_PATTERN:
			peek(AttributeSchemaImpl.class).setPattern(getText());
			break;

		case TableSchemaXmlConstants.TAG_DESCRIPTION:
			peek(TableSchemaImpl.class).setDescription(getText());
			break;

		case TableSchemaXmlConstants.TAG_OPTION: {
			String key = pop(String.class);
			String value = getText();

			if(isContext(BlockSchema.class)) {
				peek(BlockSchemaImpl.class).addOption(key, value);
			} else {
				peek(ResolverSchemaImpl.class).addOption(key, value);
			}
		} break;

		// Empty tags
		// none?

		// "Wrapper" tags
		case TableSchemaXmlConstants.TAG_ATTRIBUTES:
		case TableSchemaXmlConstants.TAG_COLUMNS:
			break;

		default:
			throw new SAXException("Unexpected end tag: "+localName);
		}
	}

	/**
	 * @see de.ims.icarus2.util.io.ObjectReader#close()
	 */
	@Override
	public void close() throws IOException {
		stack.clear();
	}

}
