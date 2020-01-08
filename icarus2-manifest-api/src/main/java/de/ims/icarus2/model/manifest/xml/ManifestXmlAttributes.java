/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
public class ManifestXmlAttributes {

	private ManifestXmlAttributes() {
		throw new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Instantiation not supported");
	}

	public static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$

	public static final String ID = "id"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String TEMPLATE_ID = "templateId"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String KEY = "key"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$
	public static final String VALUE_TYPE = "valueType"; //$NON-NLS-1$
	public static final String LAYER_TYPE = "layerType"; //$NON-NLS-1$

	public static final String VERSION_FORMAT = "versionFormat"; //$NON-NLS-1$

	public static final String OPTIONAL = "optional"; //$NON-NLS-1$
	public static final String CUSTOMIZABLE = "customizable"; //$NON-NLS-1$
	public static final String MULTIPLICITY = "multiplicity"; //$NON-NLS-1$
	public static final String MODULE_SPEC_ID = "moduleSpecId"; //$NON-NLS-1$

	public static final String EDITABLE = "editable"; //$NON-NLS-1$
	public static final String PARALLEL = "parallel"; //$NON-NLS-1$

	public static final String INLINE = "inline"; //$NON-NLS-1$

	public static final String NAMESPACE = "namespace"; //$NON-NLS-1$
	public static final String CLASS = "class"; //$NON-NLS-1$
	public static final String CLASSNAME = "classname"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "pluginId"; //$NON-NLS-1$
	public static final String PATH = "path"; //$NON-NLS-1$
	public static final String FACTORY = "factory"; //$NON-NLS-1$

	public static final String SOURCE_LAYER = "sourceLayer"; //$NON-NLS-1$
	public static final String TARGET_LAYER = "targetLayer"; //$NON-NLS-1$
	public static final String RELATION = "relation"; //$NON-NLS-1$
	public static final String COVERAGE = "coverage"; //$NON-NLS-1$
	public static final String INVERSE_MAPPING = "inverseMapping"; //$NON-NLS-1$

	public static final String LAYER_ID = "layerId"; //$NON-NLS-1$
	public static final String CONTEXT_ID = "contextId"; //$NON-NLS-1$
	public static final String CONTAINER_TYPE = "containerType"; //$NON-NLS-1$
	public static final String STRUCTURE_TYPE = "structureType"; //$NON-NLS-1$
	public static final String INDEPENDENT = "independent"; //$NON-NLS-1$
	public static final String PRIMARY_LAYER = "primaryLayer"; //$NON-NLS-1$
	public static final String FOUNDATION_LAYER = "foundationLayer"; //$NON-NLS-1$
	public static final String TYPE_ID = "typeId"; //$NON-NLS-1$
	public static final String ALIAS = "alias"; //$NON-NLS-1$
	public static final String DEFAULT = "default"; //$NON-NLS-1$
	public static final String PUBLISHED = "published"; //$NON-NLS-1$
	public static final String INCLUDE_MIN = "includeMin"; //$NON-NLS-1$
	public static final String INCLUDE_MAX = "includeMax"; //$NON-NLS-1$

	public static final String DATE = "date"; //$NON-NLS-1$

	public static final String CONTENT = "content"; //$NON-NLS-1$

	public static final String HANDLER = "handler"; //$NON-NLS-1$

	public static final String MULTI_VALUE = "multiValue"; //$NON-NLS-1$
	public static final String ALLOW_NULL = "allowNull"; //$NON-NLS-1$
	public static final String GROUP = "group"; //$NON-NLS-1$

	public static final String SOURCE = "source"; //$NON-NLS-1$
	public static final String SOURCE_TYPE = "sourceType"; //$NON-NLS-1$
	public static final String LOCATION_TYPE = "locationType"; //$NON-NLS-1$

	public static final String DEFAULT_KEY = "defaultKey"; //$NON-NLS-1$
	public static final String ANNOTATION_KEY = "annotationKey"; //$NON-NLS-1$
	public static final String ALLOW_UNKNOWN_VALUES = "allowUnknownValues"; //$NON-NLS-1$
}
