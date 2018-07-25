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
package de.ims.icarus2.model.manifest.api.events;

/**
 * @author Markus Gärtner
 *
 */
public final class ManifestEvents {

	public static final String ADD_CORPUS = "add_corpus";
	public static final String ADDED_CORPUS = "added_corpus";
	public static final String REMOVE_CORPUS = "remove_corpus";
	public static final String REMOVED_CORPUS = "removed_corpus";
	public static final String CHANGE_CORPUS = "change_corpus";
	public static final String CHANGED_CORPUS = "changed_corpus";

	public static final String ADD_CONTEXT = "add_context";
	public static final String ADDED_CONTEXT = "added_context";
	public static final String REMOVE_CONTEXT = "remove_context";
	public static final String REMOVED_CONTEXT = "removed_context";
	public static final String CHANGE_CONTEXT = "change_context";
	public static final String CHANGED_CONTEXT = "changed_context";

	public static final String ADD_TEMPLATE = "add_template";
	public static final String ADDED_TEMPLATE = "added_template";
	public static final String ADD_TEMPLATES = "add_templates";
	public static final String ADDED_TEMPLATES = "added_templates";
	public static final String REMOVE_TEMPLATE = "remove_template";
	public static final String REMOVED_TEMPLATE = "removed_template";

	public static final String ADD_LAYER_TYPE = "add_layer_type";
	public static final String ADDED_LAYER_TYPE = "added_layer_type";
	public static final String REMOVE_LAYER_TYPE = "remove_layer_type";
	public static final String REMOVED_LAYER_TYPE = "removed_layer_type";
}
