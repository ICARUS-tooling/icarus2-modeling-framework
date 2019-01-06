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
package de.ims.icarus2.model.api.corpus;

import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.util.id.Identity;

/**
 * Represents a single owner that can acquire partial ownership of a {@link PagedCorpusView}.
 * A corpus view will be prevented from being closed as long as at least one registered
 * owner still holds partial ownership of it. Note that each {@code CorpusOwner} can
 * only hold partial ownership to at most one corpus view object at any given time!
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusOwner extends Identity {

	/**
	 * Attempts to release the owners's hold on the one single corpus view it currently owns.
	 * If the owner could successfully stop its current processing of the view and was
	 * able to disconnect from it, this method returns {@code true}. A return
	 * value of {@code false} indicates, that the owner was unable to release connected
	 * resources and that the view will continue to be prevented from getting closed.
	 * <p>
	 * Note that this method should not be implemented in a blocking way when there can be no
	 * guarantee on a rather low upper bound for the blocking time.
	 *
	 * @return {@code true} if the owner released his hold on the corpus view it owned
	 */
	boolean release() throws InterruptedException;
}
