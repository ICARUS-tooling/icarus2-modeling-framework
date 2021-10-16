/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;

/**
 * @author Markus Gärtner
 *
 */
public final class ThreadVerifier {

	private final Thread thread = Thread.currentThread();
	private final String id;

	public ThreadVerifier(String id) { this.id = requireNonNull(id); }

	public final void checkThread() {
		if(Thread.currentThread()!=thread)
			throw new QueryException(QueryErrorCode.FOREIGN_THREAD_ACCESS,
					String.format("Illegal access to '%s' by thread %s - only authorized for %s",
							id, Thread.currentThread().getName(), thread.getName()));
	}
}
