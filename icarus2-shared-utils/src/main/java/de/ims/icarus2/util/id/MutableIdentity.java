/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.id;

/**
 * Defines an {@code Identity} whose "appearance" fields can be changed
 * by foreign code. Note that the defining properties of an identity (i.e.
 * its owner and {@code id}) are <b>not</b> declared to be mutable! This
 * interface is intended for use cases where identity instances can be modified
 * by the user.
 *
 * @author Markus Gärtner
 *
 */
public interface MutableIdentity extends Identity {

	void setName(String name);

	void setDescription(String name);
}
