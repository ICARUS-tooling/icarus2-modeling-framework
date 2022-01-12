/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkState;

import java.util.Map;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.QueryOutput;
import de.ims.icarus2.query.api.engine.ThreadVerifier;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

/**
 * Base class for {@link QueryOutput} implementations.
 * Focuses on the management of created {@link MatchCollector} objects
 * and will {@link #finish()} the output once the last collector has been closed.
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractOutput implements QueryOutput {

	private final Map<Thread, MatchCollector> openCollectors = new Reference2ReferenceOpenHashMap<>();
	private final Object collectorLock = new Object();

	@Override
	public final MatchCollector createTerminalCollector(ThreadVerifier threadVerifier) {
		MatchCollector collector = createRawCollector(threadVerifier);
		synchronized (collectorLock) {
			if(openCollectors.put(threadVerifier.getThread(), collector)!=null)
				throw new QueryException(GlobalErrorCode.INVALID_INPUT,
						"Thread already has an active collector: "+threadVerifier.getThread());
		}
		return collector;
	}

	protected abstract MatchCollector createRawCollector(ThreadVerifier threadVerifier);

	@Override
	public final void closeTerminalCollector(ThreadVerifier threadVerifier) {
		synchronized (collectorLock) {
			MatchCollector collector = openCollectors.remove(threadVerifier.getThread());
			if(collector==null)
				throw new QueryException(GlobalErrorCode.INVALID_INPUT,
						"No open collector available for thread: "+threadVerifier.getThread());

			// When last collector is closed we need to also finalize our result buffer
			if(openCollectors.isEmpty()) {
				finish();
			}
		}
	}

	protected abstract void finish();

	@Override
	public void close() {
		synchronized (collectorLock) {
			checkState("Unclosed collectors left over", openCollectors.isEmpty());
		}
	}
}
