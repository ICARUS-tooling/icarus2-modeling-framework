/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.api.io;


/**
 * Models a reader or writer linked to a specific source that can be used by multiple
 * threads. Its individual accessor methods do not have to contain synchronizing
 * or locking code, but rather the locking of its underlying resources is done
 * via the {@link #begin()} method and the current lock is releases by
 * calling {@link #end()}.
 *
 * @author Markus Gärtner
 *
 */
public interface SynchronizedAccessor<E extends Object> extends AutoCloseable {

	E getSource();

	void begin();

	void end();

	@Override
	void close();
}
