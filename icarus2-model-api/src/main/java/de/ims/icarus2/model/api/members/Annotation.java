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
package de.ims.icarus2.model.api.members;

/**
 * Wrapper interface for complex annotation types.
 *
 * @author Markus Gärtner
 *
 */
public interface Annotation {

	//TODO re-evaluate the reason for this class. Shouldn't ValueManifest be sufficient?

	/**
	 * Returns the {@code value} this annotation wraps.
	 * @return
	 */
	Object getValue();

	/**
	 * Returns the type of this annotation that provides
	 * additional information on how to visualize/present the
	 * annotation to the user.
	 *
	 * @return
	 */
	//TODO this is the difference between this class and ValueManifest. actually needed?
	AnnotationType getAnnotationType();
}
