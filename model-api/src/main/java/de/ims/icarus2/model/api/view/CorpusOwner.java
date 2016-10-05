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
package de.ims.icarus2.model.api.view;

import de.ims.icarus2.util.id.Identity;

/**
 * Represents a single owner that can acquire partial ownership of a {@link CorpusView}.
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
	 * If the owner could successfully stops its current processing of the view and was
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
