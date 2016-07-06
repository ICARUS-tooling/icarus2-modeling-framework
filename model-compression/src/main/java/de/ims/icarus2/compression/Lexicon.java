/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.compression;

import java.util.Iterator;

import de.ims.icarus2.model.api.ModelException;

/**
 * A lexicon in the context of compression is a map like construct that allows to
 * substitute text fragments with (small) numerical values to reduce the memory
 * footprint when serializing corpus members.
 * <p>
 * Note that although the {@link #iterator() iterators} and {@link #cursor() cursors}
 * returned by a lexicon are required to guarantee traversal of the mappings in the
 * lexicographical order of their respective tokens, the mapping process in general
 * does not have to reflect that. This means for example that when adding tokens not in
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Lexicon extends Iterable<CharSequence> {

	/**
	 * Returns the total size of this lexicon, i.e. the number
	 * of distinct tokens stored available for substitution.
	 *
	 * @return
	 */
	int size();

	default boolean isEmpty() {
		return size()==0;
	}

	/**
	 * Returns the unique substitute for the given {@code token} that
	 * is mapped in this lexicon.
	 * Note that valid substitutes start at {@code 1} and are never negative.
	 * The maximum possible value is therefore {@link Integer#MAX_VALUE} (2<sup>31</sup>-1).
	 *
	 * @param token
	 * @return
	 */
	int getSubstitute(CharSequence token);

	/**
	 * Introduces a new token for mapping to the lexicon. The lexicon is to decide the
	 * numerical value the token should be mapped to.
	 * This new substitute is then used as return value upon successful insertion.
	 *
	 * @param token
	 * @return
	 */
	int addToken(CharSequence token);

	/**
	 * Tries to either replace the current mapping for a given {@code token} with a new
	 * {@code substitute} or to introduce a completely new mapping.
	 * The return value indicates whether the mapping could be stored successfully.
	 *
	 * @param token
	 * @param substitute
	 * @return
	 * @throws ModelException in case the given {@code substitute} value is invalid
	 */
	boolean setSubstitute(CharSequence token, int substitute);

	/**
	 * Returns the text fragment that is mapped to the given numerical substitute.
	 *
	 * @param substitute
	 * @return
	 */
	CharSequence getToken(int substitute);

	/**
	 * Returns an iterator that traverses the tokens in this lexicon in
	 * lexicographical order.
	 * Note that the iterator implementation should exhibit a kind of
	 * fail-fast behavior when additional tokens are added to the lexicon
	 * during traversal of existing entries.
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<CharSequence> iterator();


	/**
	 * Returns a cursor that traverses the mappings in this lexicon in
	 * lexicographical order.
	 * Note that the cursor implementation should exhibit a kind of
	 * fail-fast behavior when additional tokens are added to the lexicon
	 * during traversal of existing entries.
	 */
	public Cursor cursor();

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface Cursor {

		/**
		 * Performs one step of traversal in case there is at least one more
		 * mapping left to visit.
		 *
		 * @return {@code true} iff moving the cursor forward one step was successful
		 */
		boolean next();

		/**
		 *
		 * @return
		 */
		CharSequence getToken();

		/**
		 *
		 * @return
		 */
		int getSubstitute();
	}
}
