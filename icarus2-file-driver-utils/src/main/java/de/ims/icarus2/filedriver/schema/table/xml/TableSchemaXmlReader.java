/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.table.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.filedriver.schema.table.TableSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.AttributeSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.BlockSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.ColumnSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.MemberSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.ResolverSchemaImpl;
import de.ims.icarus2.filedriver.schema.table.TableSchemaImpl.SubstituteSchemaImpl;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.io.ObjectReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class TableSchemaXmlReader implements ObjectReader<TableSchema> {

	public static List<TableSchema> readAllAsList(Reader reader, Options options)
			throws IOException, InterruptedException {
		List<TableSchema> result = new ArrayList<>();
		try(TableSchemaXmlReader schemaReader = new TableSchemaXmlReader()) {
			schemaReader.init(reader, options);

			schemaReader.readAll(result::add);
		}
		return result;
	}

	private XMLEventReader eventReader;
	private Stack<Object> stack = new Stack<>();

	/**
	 * @see de.ims.icarus2.util.io.ObjectReader#init(java.io.Reader, de.ims.icarus2.util.Options)
	 */
	@Override
	public void init(Reader input, Options options) {
		try {
			eventReader = XMLInputFactory.newFactory().createXMLEventReader(input);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IcarusRuntimeException(GlobalErrorCode.DELEGATION_FAILED, "Unable to create xml event reader", e);
		}
	}

	/**
	 * @see de.ims.icarus2.util.io.ObjectReader#hasMoreData()
	 */
	@Override
	public boolean hasMoreData() throws IOException, InterruptedException {
		return eventReader.hasNext();
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
	 * @see de.ims.icarus2.util.io.ObjectReader#read()
	 */
	@Override
	public TableSchema read() throws IOException, InterruptedException {
		clearStack();

		TableSchema result = null;

		try {
			readTableSchema();

			result = pop(TableSchema.class);
		} catch (XMLStreamException e) {
			throw new IOException("Failed to fetch xml event at "+e.getLocation(), e);
		} finally {
			// Make sure we hold no references
			clearStack();
		}

		return result;
	}

	private static boolean isTableTag(QName name) {
		return name.getLocalPart().equals(TableSchemaXmlConstants.TAG_TABLE);
	}

	private void readTableSchema() throws XMLStreamException {
		parse_loop : while(eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				startElement(event.asStartElement());
				break;

			case XMLStreamConstants.END_ELEMENT: {
				// Perform default handling first
				EndElement element = event.asEndElement();
				endElement(element);

				// Then check if we reached the end of our table declaration
				if(isTableTag(element.getName())) {
					break parse_loop;
				}
			} break;

			case XMLStreamConstants.CHARACTERS:
				characters(event.asCharacters());
				break;

			default:
				break;
			}
		}

		// Skim content till next table declaration or skip footer part after last table
		while(eventReader.hasNext() && !eventReader.peek().isStartElement()) {
			eventReader.next();
		}
	}

	private StringBuilder buffer = new StringBuilder();

	private Map<String, String> attributes = new Object2ObjectOpenHashMap<>();

	private void mapAttributes(StartElement element) {
		attributes.clear();

		for(Iterator<Attribute> it = element.getAttributes(); it.hasNext();) {
			Attribute attribute = it.next();
			attributes.put(attribute.getName().getLocalPart(), attribute.getValue());
		}
	}

	private String getAttribute(String name) {
		String value = attributes.get(name);
		if(value!=null && value.isEmpty()) {
			value = null;
		}
		return value;
	}

	private void startElement(StartElement element) throws XMLStreamException {
		switch (element.getName().getLocalPart()) {
		case TableSchemaXmlConstants.TAG_TABLE: {
			TableSchemaImpl tableSchema = push(new TableSchemaImpl());
			mapAttributes(element);

			String id = getAttribute(TableSchemaXmlConstants.ATTR_ID);
			if(id!=null) {
				tableSchema.setId(id);
			}

			String name = getAttribute(TableSchemaXmlConstants.ATTR_NAME);
			if(name!=null) {
				tableSchema.setName(name);
			}

			String description = getAttribute(TableSchemaXmlConstants.ATTR_DESCRIPTION);
			if(description!=null) {
				tableSchema.setDescription(description);
			}
			// ignore icon declaration

			String groupId = getAttribute(TableSchemaXmlConstants.ATTR_GROUP_ID);
			if(groupId!=null) {
				tableSchema.setGroupId(groupId);
			}
		} break;

		case TableSchemaXmlConstants.TAG_BLOCK: {
			BlockSchemaImpl blockSchema = push(new BlockSchemaImpl());
			mapAttributes(element);

			String layerId = getAttribute(TableSchemaXmlConstants.ATTR_LAYER_ID);
			if(layerId!=null) {
				blockSchema.setLayerId(layerId);
			}

			String noEntryLabel = getAttribute(TableSchemaXmlConstants.ATTR_NO_ENTRY_LABEL);
			if(noEntryLabel!=null) {
				blockSchema.setNoEntryLabel(noEntryLabel);
			}

			String columnOrderFixed = getAttribute(TableSchemaXmlConstants.ATTR_COLUMN_ORDER_FIXED);
			if(columnOrderFixed!=null) {
				blockSchema.setColumnOrderFixed(Boolean.parseBoolean(columnOrderFixed));
			}
		} break;

		case TableSchemaXmlConstants.TAG_BEGIN_DELIMITER:
		case TableSchemaXmlConstants.TAG_END_DELIMITER:
		case TableSchemaXmlConstants.TAG_ATTRIBUTE: {
			AttributeSchemaImpl attributeSchema = push(new AttributeSchemaImpl());
			mapAttributes(element);

			String target = getAttribute(TableSchemaXmlConstants.ATTR_TARGET);
			if(target!=null) {
				attributeSchema.setTarget(AttributeTarget.valueOf(target.toUpperCase())); //TODO maybe implement dedicated parsing method on the enum?
			}
		} break;

		case TableSchemaXmlConstants.TAG_FALLBACK_COLUMN:
		case TableSchemaXmlConstants.TAG_COLUMN: {
			ColumnSchemaImpl columnSchema = push(new ColumnSchemaImpl());
			mapAttributes(element);

			String name = getAttribute(TableSchemaXmlConstants.ATTR_NAME);
			if(name!=null) {
				columnSchema.setName(name);
			}

			String annotationKey = getAttribute(TableSchemaXmlConstants.ATTR_ANNOTATION_KEY);
			if(annotationKey!=null) {
				columnSchema.setAnnotationKey(annotationKey);
			}

			String ignoreColumn = getAttribute(TableSchemaXmlConstants.ATTR_IGNORE);
			if(ignoreColumn!=null) {
				columnSchema.setIsIgnoreColumn(Boolean.parseBoolean(ignoreColumn));
			}

			String layerId = getAttribute(TableSchemaXmlConstants.ATTR_LAYER_ID);
			if(layerId!=null) {
				columnSchema.setLayerId(layerId);
			}

			String noEntryLabel = getAttribute(TableSchemaXmlConstants.ATTR_NO_ENTRY_LABEL);
			if(noEntryLabel!=null) {
				columnSchema.setNoEntryLabel(noEntryLabel);
			}
		} break;

		case TableSchemaXmlConstants.TAG_COMPONENT: {
			MemberSchemaImpl memberSchema = push(new MemberSchemaImpl());
			mapAttributes(element);

			String memberType = getAttribute(TableSchemaXmlConstants.ATTR_MEMBER_TYPE);
			if(memberType!=null) {
				memberSchema.setMemberType(MemberType.valueOf(memberType.toUpperCase()));
			}

			String reference = getAttribute(TableSchemaXmlConstants.ATTR_REFERENCE);
			if(reference!=null) {
				memberSchema.setIsReference(Boolean.parseBoolean(reference));
			}
		} break;

		case TableSchemaXmlConstants.TAG_RESOLVER: {
			ResolverSchemaImpl resolverSchema = push(new ResolverSchemaImpl());
			mapAttributes(element);

			String type = getAttribute(TableSchemaXmlConstants.ATTR_TYPE);
			if(type!=null) {
				resolverSchema.setType(type);
			}
		} break;

		case TableSchemaXmlConstants.TAG_OPTION: {
			mapAttributes(element);
			push(getAttribute(TableSchemaXmlConstants.ATTR_NAME));
		} break;

		case TableSchemaXmlConstants.TAG_SUBSTITUTE: {
			mapAttributes(element);
			SubstituteSchemaImpl substituteSchema = push(new SubstituteSchemaImpl());

			String type = getAttribute(TableSchemaXmlConstants.ATTR_TYPE);
			if(type!=null) {
				substituteSchema.setType(SubstituteType.valueOf(type.toUpperCase()));
			}

			String memberType = getAttribute(TableSchemaXmlConstants.ATTR_MEMBER_TYPE);
			if(memberType!=null) {
				substituteSchema.setMemberType(MemberType.valueOf(memberType.toUpperCase()));
			}

			String name = getAttribute(TableSchemaXmlConstants.ATTR_NAME);
			if(name!=null) {
				substituteSchema.setName(name);
			}
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
			throw new XMLStreamException("Unexpected begin tag: "+element.getName(), element.getLocation());
		}

		// Make sure we collect text for all valid tags
		clearText();
	}

	private void endElement(EndElement element) throws XMLStreamException {
		switch (element.getName().getLocalPart()) {
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
			String separator = text();
			if(isContext(TableSchema.class)) {
				peek(TableSchemaImpl.class).setSeparator(separator);
			} else {
				peek(BlockSchemaImpl.class).setSeparator(separator);
			}
		} break;

		case TableSchemaXmlConstants.TAG_PATTERN:
			peek(AttributeSchemaImpl.class).setPattern(text());
			break;

		case TableSchemaXmlConstants.TAG_DESCRIPTION:
			peek(TableSchemaImpl.class).setDescription(text());
			break;

		case TableSchemaXmlConstants.TAG_OPTION: {
			String key = pop(String.class);
			String value = text();

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
			throw new XMLStreamException("Unexpected end tag: "+element.getName(), element.getLocation());
		}
	}

	private void characters(Characters characters) {
		buffer.append(characters.getData());
	}

	private void clearText() {
		buffer.setLength(0);
	}

	private String text() {
		int begin = 0;
		while(begin<buffer.length() && Character.isWhitespace(buffer.charAt(begin))) {
			begin++;
		}
		int end = buffer.length();
		while(end>begin && Character.isWhitespace(buffer.charAt(end-1))) {
			end--;
		}

		String text = (end<=begin) ? null : buffer.substring(begin, end);

		clearText();

		return text;
	}

	/**
	 * @see de.ims.icarus2.util.io.ObjectReader#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			eventReader.close();
		} catch (XMLStreamException e) {
			throw new IOException("Failed to close xml event reader", e);
		} finally {
			eventReader = null;
		}

	}

}
