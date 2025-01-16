/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * @author Markus Gärtner
 *
 */
public final class ManifestXmlAttributes {

	private ManifestXmlAttributes() {
		throw new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Instantiation not supported");
	}

	public static final String CONTENT_TYPE = "contentType";

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ICON = "icon";
	public static final String TEMPLATE_ID = "templateId";
	public static final String VALUE = "value";
	public static final String KEY = "key";
	public static final String TYPE = "type";
	public static final String VALUE_TYPE = "valueType";
	public static final String LAYER_TYPE = "layerType";

	public static final String VERSION_FORMAT = "versionFormat";

	public static final String OPTIONAL = "optional";
	public static final String CUSTOMIZABLE = "customizable";
	public static final String MULTIPLICITY = "multiplicity";
	public static final String MODULE_SPEC_ID = "moduleSpecId";

	public static final String EDITABLE = "editable";
	public static final String PARALLEL = "parallel";

	public static final String INLINE = "inline";

	public static final String NAMESPACE = "namespace";
	public static final String CLASS = "class";
	public static final String CLASSNAME = "classname";
	public static final String PLUGIN_ID = "pluginId";
	public static final String PATH = "path";
	public static final String FACTORY = "factory";

	public static final String SOURCE_LAYER = "sourceLayer";
	public static final String TARGET_LAYER = "targetLayer";
	public static final String RELATION = "relation";
	public static final String COVERAGE = "coverage";
	public static final String INVERSE_MAPPING = "inverseMapping";

	public static final String LAYER_ID = "layerId";
	public static final String CONTEXT_ID = "contextId";
	public static final String CONTAINER_TYPE = "containerType";
	public static final String STRUCTURE_TYPE = "structureType";
	public static final String INDEPENDENT = "independent";
	public static final String PRIMARY_LAYER = "primaryLayer";
	public static final String FOUNDATION_LAYER = "foundationLayer";
	public static final String TYPE_ID = "typeId";
	public static final String ALIAS = "alias";
	public static final String DEFAULT = "default";
	public static final String PUBLISHED = "published";
	public static final String INCLUDE_MIN = "includeMin";
	public static final String INCLUDE_MAX = "includeMax";

	public static final String DATE = "date";

	public static final String CONTENT = "content";

	public static final String HANDLER = "handler";

	public static final String MULTI_VALUE = "multiValue";
	public static final String ALLOW_NULL = "allowNull";
	public static final String GROUP = "group";

//	public static final String ROOT_CONTAINER_TYPE = "rootContainerType";

	public static final String SOURCE = "source";
	public static final String SOURCE_TYPE = "sourceType";
	public static final String LOCATION_TYPE = "locationType";

	public static final String DEFAULT_KEY = "defaultKey";
	public static final String ANNOTATION_KEY = "annotationKey";
	public static final String ALLOW_UNKNOWN_VALUES = "allowUnknownValues";
}
