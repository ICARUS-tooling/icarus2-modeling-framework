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

/**
 * @author Markus Gärtner
 *
 */
public interface TableSchemaXmlConstants {

	public static final String TAG_TABLE = "table";
	public static final String TAG_BLOCK = "block";
	public static final String TAG_NESTED_BLOCKS = "nestedBlocks";
	public static final String TAG_OPTION = "option";
	public static final String TAG_ATTRIBUTE = "attribute";
	public static final String TAG_COMPONENT = "component";
	public static final String TAG_BEGIN_DELIMITER = "beginDelimiter";
	public static final String TAG_END_DELIMITER = "endDelimiter";
	public static final String TAG_ATTRIBUTES = "attributes";
	public static final String TAG_SUBSTITUTE = "substitute";
	public static final String TAG_RESOLVER = "resolver";
	public static final String TAG_COLUMNS = "columns";
	public static final String TAG_COLUMN = "column";
	public static final String TAG_PATTERN = "pattern";
	public static final String TAG_SEPARATOR = "separator";
	public static final String TAG_DESCRIPTION = "description";
	public static final String TAG_FALLBACK_COLUMN = "fallbackColumn";

	public static final String ATTR_TARGET = "target";
	public static final String ATTR_GROUP_ID = "groupId";
	public static final String ATTR_ID = "id";
	public static final String ATTR_DESCRIPTION = "description";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_ICON = "icon";
	public static final String ATTR_SEPARATOR = "separator";
	public static final String ATTR_COLUMN_ORDER_FIXED = "columnOrderFixed";
	public static final String ATTR_NO_ENTRY_LABEL = "noEntryLabel";
	public static final String ATTR_MEMBER_TYPE = "memberType";
	public static final String ATTR_REFERENCE = "reference";
	public static final String ATTR_LAYER_ID = "layerId";
	public static final String ATTR_ANNOTATION_KEY = "annotationKey";
	public static final String ATTR_IGNORE = "ignore";
	public static final String ATTR_TYPE = "type";
}
