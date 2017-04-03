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
 */
package de.ims.icarus2.filedriver.schema.table;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
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
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.AttributeTarget;
import de.ims.icarus2.filedriver.schema.table.TableSchema.BlockSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ColumnSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.MemberSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.ResolverSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType;
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
public class TableSchemaXmlReader implements ObjectReader<TableSchema>, TableSchemaXmlConstants {

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
			throw new IcarusException(GlobalErrorCode.DELEGATION_FAILED, "Unable to create xml event reader", e);
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
		return name.getLocalPart().equals(TAG_TABLE);
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
		case TAG_TABLE: {
			TableSchemaImpl tableSchema = push(new TableSchemaImpl());
			mapAttributes(element);

			String id = getAttribute(ATTR_ID);
			if(id!=null) {
				tableSchema.setId(id);
			}

			String name = getAttribute(ATTR_NAME);
			if(name!=null) {
				tableSchema.setName(name);
			}

			String description = getAttribute(ATTR_DESCRIPTION);
			if(description!=null) {
				tableSchema.setDescription(description);
			}
			// ignore icon declaration

			String groupId = getAttribute(ATTR_GROUP_ID);
			if(groupId!=null) {
				tableSchema.setGroupId(groupId);
			}
		} break;

		case TAG_BLOCK: {
			BlockSchemaImpl blockSchema = push(new BlockSchemaImpl());
			mapAttributes(element);

			String layerId = getAttribute(ATTR_LAYER_ID);
			if(layerId!=null) {
				blockSchema.setLayerId(layerId);
			}

			String noEntryLabel = getAttribute(ATTR_NO_ENTRY_LABEL);
			if(noEntryLabel!=null) {
				blockSchema.setNoEntryLabel(noEntryLabel);
			}

			String columnOrderFixed = getAttribute(ATTR_COLUMN_ORDER_FIXED);
			if(columnOrderFixed!=null) {
				blockSchema.setColumnOrderFixed(Boolean.parseBoolean(columnOrderFixed));
			}
		} break;

		case TAG_BEGIN_DELIMITER:
		case TAG_END_DELIMITER:
		case TAG_ATTRIBUTE: {
			AttributeSchemaImpl attributeSchema = push(new AttributeSchemaImpl());
			mapAttributes(element);

			String target = getAttribute(ATTR_TARGET);
			if(target!=null) {
				attributeSchema.setTarget(AttributeTarget.valueOf(target.toUpperCase())); //TODO maybe implement dedicated parsing method on the enum?
			}
		} break;

		case TAG_FALLBACK_COLUMN:
		case TAG_COLUMN: {
			ColumnSchemaImpl columnSchema = push(new ColumnSchemaImpl());
			mapAttributes(element);

			String name = getAttribute(ATTR_NAME);
			if(name!=null) {
				columnSchema.setName(name);
			}

			String annotationKey = getAttribute(ATTR_ANNOTATION_KEY);
			if(annotationKey!=null) {
				columnSchema.setAnnotationKey(annotationKey);
			}

			String ignoreColumn = getAttribute(ATTR_IGNORE);
			if(ignoreColumn!=null) {
				columnSchema.setIsIgnoreColumn(Boolean.parseBoolean(ignoreColumn));
			}

			String layerId = getAttribute(ATTR_LAYER_ID);
			if(layerId!=null) {
				columnSchema.setLayerId(layerId);
			}

			String noEntryLabel = getAttribute(ATTR_NO_ENTRY_LABEL);
			if(noEntryLabel!=null) {
				columnSchema.setNoEntryLabel(noEntryLabel);
			}
		} break;

		case TAG_COMPONENT: {
			MemberSchemaImpl memberSchema = push(new MemberSchemaImpl());
			mapAttributes(element);

			String memberType = getAttribute(ATTR_MEMBER_TYPE);
			if(memberType!=null) {
				memberSchema.setMemberType(MemberType.valueOf(memberType.toUpperCase()));
			}

			String reference = getAttribute(ATTR_REFERENCE);
			if(reference!=null) {
				memberSchema.setIsReference(Boolean.parseBoolean(reference));
			}
		} break;

		case TAG_RESOLVER: {
			ResolverSchemaImpl resolverSchema = push(new ResolverSchemaImpl());
			mapAttributes(element);

			String type = getAttribute(ATTR_TYPE);
			if(type!=null) {
				resolverSchema.setType(type);
			}
		} break;

		case TAG_OPTION: {
			mapAttributes(element);
			push(getAttribute(ATTR_NAME));
		} break;

		case TAG_SUBSTITUTE: {
			mapAttributes(element);
			SubstituteSchemaImpl substituteSchema = push(new SubstituteSchemaImpl());

			String type = getAttribute(ATTR_TYPE);
			if(type!=null) {
				substituteSchema.setType(SubstituteType.valueOf(type.toUpperCase()));
			}

			String memberType = getAttribute(ATTR_MEMBER_TYPE);
			if(memberType!=null) {
				substituteSchema.setMemberType(MemberType.valueOf(memberType.toUpperCase()));
			}

			String name = getAttribute(ATTR_NAME);
			if(name!=null) {
				substituteSchema.setName(name);
			}
		} break;

		// Text-only tags
		case TAG_SEPARATOR:
		case TAG_DESCRIPTION:
		case TAG_PATTERN:
			break;

		// "Wrapper" tags
		case TAG_ATTRIBUTES:
		case TAG_COLUMNS:
			break;

		default:
			throw new XMLStreamException("Unexpected begin tag: "+element.getName(), element.getLocation());
		}

		// Make sure we collect text for all valid tags
		clearText();
	}

	private void endElement(EndElement element) throws XMLStreamException {
		switch (element.getName().getLocalPart()) {
		case TAG_TABLE:
			// Leave table schema on the stack
			break;

		case TAG_BLOCK: {
			BlockSchema blockSchema = pop(BlockSchema.class);
			if(isContext(BlockSchema.class)) {
				peek(BlockSchemaImpl.class).addBlock(blockSchema);
			} else {
				peek(TableSchemaImpl.class).setRootBlock(blockSchema);
			}
		} break;

		case TAG_BEGIN_DELIMITER: {
			AttributeSchema beginDelimiter = pop(AttributeSchema.class);
			peek(BlockSchemaImpl.class).setBeginDelimiter(beginDelimiter);
		} break;

		case TAG_END_DELIMITER: {
			AttributeSchema endDelimiter = pop(AttributeSchema.class);
			peek(BlockSchemaImpl.class).setEndDelimiter(endDelimiter);
		} break;

		case TAG_ATTRIBUTE: {
			AttributeSchema attributeSchema = pop(AttributeSchema.class);
			peek(BlockSchemaImpl.class).addAttribute(attributeSchema);
		} break;

		case TAG_FALLBACK_COLUMN: {
			ColumnSchema columnSchema = pop(ColumnSchema.class);
			peek(BlockSchemaImpl.class).setFallbackColumn(columnSchema);
		} break;

		case TAG_COLUMN: {
			ColumnSchema columnSchema = pop(ColumnSchema.class);
			peek(BlockSchemaImpl.class).addColumn(columnSchema);
		} break;

		case TAG_COMPONENT: {
			MemberSchema memberSchema = pop(MemberSchema.class);
			peek(BlockSchemaImpl.class).setComponentSchema(memberSchema);
		} break;

		case TAG_RESOLVER: {
			ResolverSchema resolverSchema = pop(ResolverSchema.class);
			if(isContext(AttributeSchema.class)) {
				peek(AttributeSchemaImpl.class).setResolver(resolverSchema);
			} else if(isContext(ColumnSchema.class)) {
				peek(ColumnSchemaImpl.class).setResolver(resolverSchema);
			}
		} break;

		case TAG_SUBSTITUTE: {
			SubstituteSchema substituteSchema = pop(SubstituteSchema.class);
			peek(ColumnSchemaImpl.class).addSubstitute(substituteSchema);
		} break;

		case TAG_SEPARATOR: {
			String separator = text();
			if(isContext(TableSchema.class)) {
				peek(TableSchemaImpl.class).setSeparator(separator);
			} else {
				peek(BlockSchemaImpl.class).setSeparator(separator);
			}
		} break;

		case TAG_PATTERN:
			peek(AttributeSchemaImpl.class).setPattern(text());
			break;

		case TAG_DESCRIPTION:
			peek(TableSchemaImpl.class).setDescription(text());
			break;

		case TAG_OPTION: {
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
		case TAG_ATTRIBUTES:
		case TAG_COLUMNS:
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
