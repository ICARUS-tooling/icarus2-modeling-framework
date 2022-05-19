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
package de.ims.icarus2.filedriver.schema.tabular.xml;

/**
 * @author Markus Gärtner
 *
 */
public final class TableSchemaXmlConstants {

	public static final String NS_PREFIX = "its";
	public static final String NS_URI = "http://www.ims.uni-stuttgart.de/icarus/xml/schema";

	public static final String SCHEMA_NAME = NS_URI+"/tabular-schema.xsd";

	public static final String TAG_TABLE = "table";
	public static final String TAG_BLOCK = "block";
	public static final String TAG_EXTERNAL_GROUP = "externalGroup";
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
