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
 * $Revision: 392 $
 *
 */
package de.ims.icarus2.model.api.members;

import de.ims.icarus2.model.api.corpus.Corpus;



/**
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusMember {

//	/**
//	 * @return The globally unique id
//	 */
//	long getId();

	/**
	 * Returns the corpus this member is a part of.
	 * This call is usually forwarded to the host {@code PagedCorpusView}
	 * @return The corpus this member is a part of
	 */
	Corpus getCorpus();

	/**
	 * Returns the type of this member. Note that the correct
	 * way of performing type specific operations on a {@code CorpusMember}
	 * is to query its type through this method and <b>not</b> by using the
	 * {@code instanceof} operator!
	 *
	 * @return The type of this member
	 */
	MemberType getMemberType();
}
