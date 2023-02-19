/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver;

/**
 * @author Markus Gärtner
 *
 */
public enum ElementFlag {

	/**
	 * Element has neither been scanned or checked
	 */
	UNKNOWN,

	/**
	 * The element or its data source doesn't exist on the physical storage
	 */
	MISSING,

	/**
	 * Element experienced changes made to it outside of the framework or has
	 * incorrect metadata stored for it.
	 */
	CORRUPTED,

	/**
	 * External problems outside the framework control prevented preparation
	 * of the element and rendered it unusable for further processing. Examples
	 * for this state include failed file creation, invalid content or other
	 * I/O related errors.
	 */
	UNUSABLE,

	/**
	 * Element was checked but has not yet been scanned
	 */
	CHECKED,

	/**
	 * Part of the metadata for the element has been collected.
	 * This state is usable mainly for cases of layer data being
	 * distributed across several files.
	 * <p>
	 * This flag is persisted into metadata.
	 */
	PARTIALLY_SCANNED,

	/**
	 * All metadata of the element has been collected. Element is fully prepared and usable.
	 * <p>
	 * This flag is persisted into metadata.
	 */
	SCANNED,

	/**
	 * Parts of the element have been loaded into the data model
	 */
	PARTIALLY_LOADED,

	/**
	 * Entire element has been loaded into the data model.
	 */
	LOADED
	;
}
