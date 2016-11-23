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
public interface ManifestXmlTags {

	public static final String TAG_TEMPLATES = "templates";
	public static final String TAG_CORPORA = "corpora";

	public static final String TAG_CORPUS = "corpus";
	public static final String TAG_CONTEXT = "context";
	public static final String TAG_ROOT_CONTEXT = "rootContext";
	public static final String TAG_LAYER_GROUP = "layerGroup";
	public static final String TAG_ITEM_LAYER = "itemLayer";
	public static final String TAG_STRUCTURE_LAYER = "structureLayer";
	public static final String TAG_FRAGMENT_LAYER = "fragmentLayer";
	public static final String TAG_ANNOTATION_LAYER = "annotationLayer";
	public static final String TAG_HIGHLIGHT_LAYER = "highlightLayer";
	public static final String TAG_PREREQUISITES = "prerequisites";
	public static final String TAG_PREREQUISITE = "prerequisite";
	public static final String TAG_RASTERIZER = "rasterizer";

	public static final String TAG_VERSION = "version";

	public static final String TAG_BASE_LAYER = "baseLayer";
	public static final String TAG_BOUNDARY_LAYER = "boundaryLayer";
	public static final String TAG_FOUNDATION_LAYER = "foundationLayer";
	public static final String TAG_VALUE_LAYER = "valueLayer";
	public static final String TAG_REFERENCE_LAYER = "referenceLayer";

	public static final String TAG_CONTAINER = "container";
	public static final String TAG_STRUCTURE = "structure";
	public static final String TAG_CONTAINER_FLAG = "containerFlag";
	public static final String TAG_STRUCTURE_FLAG = "structureFlag";
	public static final String TAG_ANNOTATION = "annotation";
	public static final String TAG_ANNOTATION_FLAG = "annotationFlag";
	public static final String TAG_HIGHLIGHT_FLAG = "highlightFlag";
	public static final String TAG_ALIAS = "alias";

	public static final String TAG_NO_ENTRY_VALUE = "noEntryValue";
	public static final String TAG_VALUE_SET = "valueSet";
	public static final String TAG_VALUE = "value";
	public static final String TAG_OPTIONS = "options";
	public static final String TAG_OPTION = "option";
	public static final String TAG_EXTENSION_POINT = "extensionPoint";
	public static final String TAG_DEFAULT_VALUE = "defaultValue";
	public static final String TAG_IMPLEMENTATION = "implementation";
	public static final String TAG_DOCUMENTATION = "documentation";
	public static final String TAG_CONTENT = "content";
	public static final String TAG_RESOURCE = "resource";

	public static final String TAG_DRIVER = "driver";
	public static final String TAG_MAPPING = "mapping";
	public static final String TAG_MODULE = "module";
	public static final String TAG_MODULE_SPEC = "moduleSpec";

	public static final String TAG_LOCATIONS = "locations";
	public static final String TAG_LOCATION = "location";
	public static final String TAG_PATH = "path";
	public static final String TAG_PATH_ENTRY = "pathEntry";
	public static final String TAG_PATH_RESOLVER = "pathResolver";

	public static final String TAG_VALUE_RANGE = "valueRange";
	public static final String TAG_MIN = "min";
	public static final String TAG_MAX = "max";
	public static final String TAG_STEP_SIZE = "stepSize";

//	public static final String TAG_EMBEDDED = "embedded";

	public static final String TAG_EVAL = "eval";
	public static final String TAG_CODE = "code";
	public static final String TAG_VARIABLE = "variable";

	public static final String TAG_PROPERTIES = "properties";
	public static final String TAG_PROPERTY = "property";
	public static final String TAG_GROUP = "group";

	public static final String TAG_NOTE = "note";
	public static final String TAG_NOTES = "notes";
}
