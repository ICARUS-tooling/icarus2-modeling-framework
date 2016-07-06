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
	public static final String ATTR_VALUE_TYPE = "value-type"; //$NON-NLS-1$
	public static final String ATTR_LAYER_TYPE = "layer-type"; //$NON-NLS-1$

	public static final String ATTR_VERSION_FORMAT = "version-format"; //$NON-NLS-1$

	public static final String ATTR_OPTIONAL = "optional"; //$NON-NLS-1$
	public static final String ATTR_CUSTOMIZABLE = "customizable"; //$NON-NLS-1$
	public static final String ATTR_MULTIPLICITY = "multiplicity"; //$NON-NLS-1$
	public static final String ATTR_MODULE_SPEC_ID = "module-spec-id"; //$NON-NLS-1$

	public static final String ATTR_EDITABLE = "editable"; //$NON-NLS-1$
	public static final String ATTR_PARALLEL = "parallel"; //$NON-NLS-1$

	public static final String ATTR_NAMESPACE = "namespace"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	public static final String ATTR_CLASSNAME = "classname"; //$NON-NLS-1$
	public static final String ATTR_PLUGIN_ID = "plugin-id"; //$NON-NLS-1$
	public static final String ATTR_PATH = "path"; //$NON-NLS-1$
	public static final String ATTR_FACTORY = "factory"; //$NON-NLS-1$

	public static final String ATTR_SOURCE_LAYER = "source-layer"; //$NON-NLS-1$
	public static final String ATTR_TARGET_LAYER = "target-layer"; //$NON-NLS-1$
	public static final String ATTR_RELATION = "relation"; //$NON-NLS-1$
	public static final String ATTR_COVERAGE = "coverage"; //$NON-NLS-1$
	public static final String ATTR_INVERSE_MAPPING = "inverse-mapping"; //$NON-NLS-1$

	public static final String ATTR_LAYER_ID = "layer-id"; //$NON-NLS-1$
	public static final String ATTR_CONTEXT_ID = "context-id"; //$NON-NLS-1$
	public static final String ATTR_CONTAINER_TYPE = "container-type"; //$NON-NLS-1$
	public static final String ATTR_STRUCTURE_TYPE = "structure-type"; //$NON-NLS-1$
	public static final String ATTR_INDEPENDENT = "independent"; //$NON-NLS-1$
	public static final String ATTR_PRIMARY_LAYER = "primary-layer"; //$NON-NLS-1$
	public static final String ATTR_FOUNDATION_LAYER = "foundation-layer"; //$NON-NLS-1$
	public static final String ATTR_TYPE_ID = "type-id"; //$NON-NLS-1$
	public static final String ATTR_ALIAS = "alias"; //$NON-NLS-1$
	public static final String ATTR_DEFAULT = "default"; //$NON-NLS-1$
	public static final String ATTR_PUBLISHED = "published"; //$NON-NLS-1$
	public static final String ATTR_INCLUDE_MIN = "include-min"; //$NON-NLS-1$
	public static final String ATTR_INCLUDE_MAX = "include-max"; //$NON-NLS-1$

	public static final String ATTR_DATE = "date"; //$NON-NLS-1$

	public static final String ATTR_CONTENT = "content"; //$NON-NLS-1$

	public static final String ATTR_HANDLER = "handler"; //$NON-NLS-1$

	public static final String ATTR_MULTI_VALUE = "multi-value"; //$NON-NLS-1$
	public static final String ATTR_ALLOW_NULL = "allow-null"; //$NON-NLS-1$
	public static final String ATTR_GROUP = "group"; //$NON-NLS-1$

	public static final String ATTR_SOURCE = "source"; //$NON-NLS-1$
	public static final String ATTR_SOURCE_TYPE = "source-type"; //$NON-NLS-1$
	public static final String ATTR_LOCATION_TYPE = "location-type"; //$NON-NLS-1$

	public static final String ATTR_DEFAULT_KEY = "default-key"; //$NON-NLS-1$
	public static final String ATTR_ANNOTATION_KEY = "annotation-key"; //$NON-NLS-1$
}
