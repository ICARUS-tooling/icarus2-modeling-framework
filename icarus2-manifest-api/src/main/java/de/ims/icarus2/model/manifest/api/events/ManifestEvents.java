/**
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
package de.ims.icarus2.model.manifest.api.events;

import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.util.events.EventObject;

/**
 * Event names for {@link EventObject events} fired
 * by members of the manifest framework, most notably
 * instances of {@link ManifestRegistry}.
 *
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
