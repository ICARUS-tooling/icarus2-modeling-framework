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
