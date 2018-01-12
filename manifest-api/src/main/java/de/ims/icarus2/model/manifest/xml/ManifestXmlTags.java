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
public class ManifestXmlTags {

	private ManifestXmlTags() {
		throw new IcarusException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Instantiation not supported");
	}

	public static final String MANIFEST = "manifest";

	public static final String TEMPLATES = "templates";
	public static final String CORPORA = "corpora";

	public static final String CORPUS = "corpus";
	public static final String CONTEXT = "context";
	public static final String ROOT_CONTEXT = "rootContext";
	public static final String LAYER_GROUP = "layerGroup";
	public static final String ITEM_LAYER = "itemLayer";
	public static final String STRUCTURE_LAYER = "structureLayer";
	public static final String FRAGMENT_LAYER = "fragmentLayer";
	public static final String ANNOTATION_LAYER = "annotationLayer";
	public static final String HIGHLIGHT_LAYER = "highlightLayer";
	public static final String PREREQUISITES = "prerequisites";
	public static final String PREREQUISITE = "prerequisite";
	public static final String RASTERIZER = "rasterizer";

	public static final String VERSION = "version";

	@Deprecated
	public static final String IDENTITY = "identity";

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ICON = "icon";

	public static final String CATEGORIES = "categories";
	public static final String CATEGORY = "category";

	public static final String BASE_LAYER = "baseLayer";
	public static final String BOUNDARY_LAYER = "boundaryLayer";
	public static final String FOUNDATION_LAYER = "foundationLayer";
	public static final String VALUE_LAYER = "valueLayer";
	public static final String REFERENCE_LAYER = "referenceLayer";

	public static final String CONTAINER = "container";
	public static final String STRUCTURE = "structure";
	public static final String CONTAINER_FLAG = "containerFlag";
	public static final String STRUCTURE_FLAG = "structureFlag";
	public static final String ANNOTATION = "annotation";
	public static final String ANNOTATION_FLAG = "annotationFlag";
	public static final String HIGHLIGHT_FLAG = "highlightFlag";
	public static final String ALIAS = "alias";

	public static final String NO_ENTRY_VALUE = "noEntryValue";
	public static final String VALUE_SET = "valueSet";
	public static final String VALUE = "value";
	public static final String OPTIONS = "options";
	public static final String OPTION = "option";
	public static final String EXTENSION_POINT = "extensionPoint";
	public static final String DEFAULT_VALUE = "defaultValue";
	public static final String IMPLEMENTATION = "implementation";
	public static final String DOCUMENTATION = "documentation";
	public static final String CONTENT = "content";
	public static final String RESOURCE = "resource";

	public static final String DRIVER = "driver";
	public static final String MAPPING = "mapping";
	public static final String MODULE = "module";
	public static final String MODULE_SPEC = "moduleSpec";

	public static final String LOCATIONS = "locations";
	public static final String LOCATION = "location";
	public static final String PATH = "path";
	public static final String PATH_ENTRY = "pathEntry";
	public static final String PATH_RESOLVER = "pathResolver";

	public static final String VALUE_RANGE = "valueRange";
	public static final String MIN = "min";
	public static final String MAX = "max";
	public static final String STEP_SIZE = "stepSize";

	public static final String EVAL = "eval";
	public static final String CODE = "code";
	public static final String VARIABLE = "variable";

	public static final String PROPERTIES = "properties";
	public static final String PROPERTY = "property";
	public static final String GROUP = "group";

	public static final String NOTE = "note";
	public static final String NOTES = "notes";
}
