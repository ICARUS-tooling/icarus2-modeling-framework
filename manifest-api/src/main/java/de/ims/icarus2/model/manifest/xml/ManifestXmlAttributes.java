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

/**
 * @author Markus Gärtner
 *
 */
public interface ManifestXmlAttributes {

	public static final String ATTR_CONTENT_TYPE = "content-type"; //$NON-NLS-1$

	public static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_NAME = "name"; //$NON-NLS-1$
	public static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	public static final String ATTR_TEMPLATE_ID = "template-id"; //$NON-NLS-1$
	public static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	public static final String ATTR_KEY = "key"; //$NON-NLS-1$
	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
	public static final String ATTR_VALUE_TYPE = "valueType"; //$NON-NLS-1$
	public static final String ATTR_LAYER_TYPE = "layerType"; //$NON-NLS-1$

	public static final String ATTR_VERSION_FORMAT = "versionFormat"; //$NON-NLS-1$

	public static final String ATTR_OPTIONAL = "optional"; //$NON-NLS-1$
	public static final String ATTR_CUSTOMIZABLE = "customizable"; //$NON-NLS-1$
	public static final String ATTR_MULTIPLICITY = "multiplicity"; //$NON-NLS-1$
	public static final String ATTR_MODULE_SPEC_ID = "moduleSpecId"; //$NON-NLS-1$

	public static final String ATTR_EDITABLE = "editable"; //$NON-NLS-1$
	public static final String ATTR_PARALLEL = "parallel"; //$NON-NLS-1$

	public static final String ATTR_INLINE = "inline"; //$NON-NLS-1$

	public static final String ATTR_NAMESPACE = "namespace"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	public static final String ATTR_CLASSNAME = "classname"; //$NON-NLS-1$
	public static final String ATTR_PLUGIN_ID = "pluginId"; //$NON-NLS-1$
	public static final String ATTR_PATH = "path"; //$NON-NLS-1$
	public static final String ATTR_FACTORY = "factory"; //$NON-NLS-1$

	public static final String ATTR_SOURCE_LAYER = "sourceLayer"; //$NON-NLS-1$
	public static final String ATTR_TARGET_LAYER = "targetLayer"; //$NON-NLS-1$
	public static final String ATTR_RELATION = "relation"; //$NON-NLS-1$
	public static final String ATTR_COVERAGE = "coverage"; //$NON-NLS-1$
	public static final String ATTR_INVERSE_MAPPING = "inverseMapping"; //$NON-NLS-1$

	public static final String ATTR_LAYER_ID = "layerId"; //$NON-NLS-1$
	public static final String ATTR_CONTEXT_ID = "contextId"; //$NON-NLS-1$
	public static final String ATTR_CONTAINER_TYPE = "containerType"; //$NON-NLS-1$
	public static final String ATTR_STRUCTURE_TYPE = "structureType"; //$NON-NLS-1$
	public static final String ATTR_INDEPENDENT = "independent"; //$NON-NLS-1$
	public static final String ATTR_PRIMARY_LAYER = "primaryLayer"; //$NON-NLS-1$
	public static final String ATTR_FOUNDATION_LAYER = "foundationLayer"; //$NON-NLS-1$
	public static final String ATTR_TYPE_ID = "typeId"; //$NON-NLS-1$
	public static final String ATTR_ALIAS = "alias"; //$NON-NLS-1$
	public static final String ATTR_DEFAULT = "default"; //$NON-NLS-1$
	public static final String ATTR_PUBLISHED = "published"; //$NON-NLS-1$
	public static final String ATTR_INCLUDE_MIN = "includeMin"; //$NON-NLS-1$
	public static final String ATTR_INCLUDE_MAX = "includeMax"; //$NON-NLS-1$

	public static final String ATTR_DATE = "date"; //$NON-NLS-1$

	public static final String ATTR_CONTENT = "content"; //$NON-NLS-1$

	public static final String ATTR_HANDLER = "handler"; //$NON-NLS-1$

	public static final String ATTR_MULTI_VALUE = "multiValue"; //$NON-NLS-1$
	public static final String ATTR_ALLOW_NULL = "allowNull"; //$NON-NLS-1$
	public static final String ATTR_GROUP = "group"; //$NON-NLS-1$

	public static final String ATTR_SOURCE = "source"; //$NON-NLS-1$
	public static final String ATTR_SOURCE_TYPE = "sourceType"; //$NON-NLS-1$
	public static final String ATTR_LOCATION_TYPE = "locationType"; //$NON-NLS-1$

	public static final String ATTR_DEFAULT_KEY = "defaultKey"; //$NON-NLS-1$
	public static final String ATTR_ANNOTATION_KEY = "annotationKey"; //$NON-NLS-1$
	public static final String ATTR_ALLOW_UNKNOWN_VALUES = "allowUnknownValues"; //$NON-NLS-1$
}
