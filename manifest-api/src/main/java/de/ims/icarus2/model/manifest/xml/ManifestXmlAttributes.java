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
 *
 */
package de.ims.icarus2.model.manifest.xml;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestXmlAttributes {

	private ManifestXmlAttributes() {
		throw new IcarusException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Instantiation not supported");
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
