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
package de.ims.icarus2.model.api.raster;

import de.ims.icarus2.model.api.members.item.Fragment;

/**
 * Models a simple element of a vector space to be used to define the boundaries of a
 * {@link Fragment} object. Each position vector carries information about the number
 * of elements it holds (its dimensionality) and allows an array like access to those element.
 *
 * @author Markus Gärtner
 *
 */
public interface Position {

	int getDimensionality();

	long getValue(int dimension);
}
