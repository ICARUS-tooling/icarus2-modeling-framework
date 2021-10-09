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
package de.ims.icarus2.query.api.engine.matcher;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.util.collections.ArrayUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class Permutator {

	public static final int MAX_SIZE = 9;

	private static class Template {
		final int[][] data;
		final int[][] skips;

		public Template(int[][] data, int[][] skips) {
			this.data = requireNonNull(data);
			this.skips = requireNonNull(skips);

			assert data.length==skips.length;
		}

		Permutator create() { return new Precompiled(data, skips); }
	}

	private static Template[] templates = new Template[MAX_SIZE];

	public static Permutator forSize(int size) {
		checkArgument("Size must be 2 or greater", size>1);
		if(size>MAX_SIZE)
			throw new QueryException(QueryErrorCode.INTRACTABLE_CONSTRUCT,
					"Number of elements for permutation exceeds limit: "+size);

		Permutator permutator = prepared(size);
		if(permutator==null) {
			permutator = cached(size);
		}

		return permutator;
	}

	@VisibleForTesting
	static Permutator prepared(int size) {

		switch (size) {
		case 2: return new Precompiled(Size2.DATA, Size2.SKIPS);
		case 3: return new Precompiled(Size3.DATA, Size3.SKIPS);
		case 4: return new Precompiled(Size4.DATA, Size4.SKIPS);
		default:
			return null;
		}
	}

	@VisibleForTesting
	static Permutator cached(int size) {

		if(templates[size]==null) {
			// Generate permutations
			Permutator gen = new Dynamic(size);
			List<int[]> elements = new ObjectArrayList<>();
			do {
				elements.add(gen.current().clone()); // cloning is important here!!
			} while (gen.next());
			int[][] data = elements.toArray(new int[0][]);

			// Compute skip data based on permutations
			int[][] skips = new int[data.length][];
			for (int i = 0; i < skips.length; i++) {
				skips[i] = new int[size];
			}
			for (int i = 0; i < size; i++) {
				int skipTo = -1;
				skips[skips.length-1][i] = -1;
				for (int j = skips.length-1; j > 0; j--) {
					if(data[j][i] != data[j-1][i]) {
						skipTo = j;
					}
					skips[j-1][i] = skipTo;
				}
			}

			templates[size] = new Template(data, skips);
		}

		return templates[size].create();
	}

	public static Permutator dynamic(int size) {
		return new Dynamic(size);
	}

	/** Return the number of elements to permutate */
	public abstract int size();

	/**
	 * Compute the next permutation and return {@code true}
	 * iff successful.
	 * @return {@code true} iff a new permutation could be computed, {@code false} otherwise.
	 */
	public abstract boolean next();

	/**
	 * Skip to the next permutation that changes the element
	 * currently at position {@code index}.
	 * @param index
	 * @return {@code true} iff skipping was successful and a new permutation is available.
	 */
	public abstract boolean skip(int index);

	/**
	 * Return the current permutation. Note that the returned array
	 * is <b>owned</b> by the {@link Permutator} that created it and
	 * client code mustn't  modify it!
	 */
	public abstract int[] current();

	/**
	 * Reset so that the next call to {@link #next()} is guaranteed
	 * to be successful and will return the sorted (raw) permutation.
	 */
	public abstract void reset();

	//TODO test performance with shared vs cloned local data and skips matrices
	static final class Precompiled extends Permutator {
		final int[][] data;
		final int[][] skips;
		final int size;

		public Precompiled(int[][] data, int[][] skips) {
			this.data = data;
			this.skips = skips;
			size = data[0].length;
		}

		int cursor = 0;

		@Override
		public boolean next() {
			return cursor != -1 && ++cursor < data.length;
		}

		@Override
		public boolean skip(int index) {
			return cursor!=UNSET_INT && cursor<data.length
					&& (cursor = skips[cursor][index]) != UNSET_INT;
		}

		@Override
		public void reset() { cursor = 0; }

		@Override
		public int size() { return size; }

		@Override
		public int[] current() { return data[cursor]; }
	}

	static final class Dynamic extends Permutator {

		final int[] data;
		boolean exhausted;

		Dynamic(int size) {
			checkArgument("Size must be 2 or greater", size>1);
			data = new int[size];
			reset();
		}

		@Override
		public int size() { return data.length; }

		private static void swap(int[] array, int a, int b) {
			int tmp = array[a];
			array[a] = array[b];
			array[b] = tmp;
		}

		@Override
		public boolean next() {
			if(exhausted) return false;

			/*
			 * Find the largest index k such that a[k] < a[k + 1]. If no such index exists, the permutation is the last permutation.
		     * Find the largest index l greater than k such that a[k] < a[l].
		     * Swap the value of a[k] with that of a[l].
		     * Reverse the sequence from a[k + 1] up to and including the final element a[n].
			 */

			for (int left = data.length - 2; left >= 0; left--) {
				if(data[left] < data[left+1]) {
					for (int right = data.length - 1; right > left; right--) {
						if(data[left] < data[right]) {
							swap(data, left, right);
				            for (int i=left+1, j=data.length-1, mid=i+(j-i)>>1; i<=mid || j==i+1; i++, j--) {
				            	swap(data, i, j);
				            }
							return true;
						}
					}
				}
			}

			exhausted = true;
			return false;
		}

		@Override
		public boolean skip(int index) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int[] current() { return data; }

		@Override
		public void reset() {
			exhausted = false;
			ArrayUtils.fillAscending(data);
		}
	}

	static class Size2 {
		static final int[][] DATA = {
				{0, 1},
				{1, 0},
		};
		static final int[][] SKIPS = {
				{ 1,  1},
				{-1, -1},
		};
	}

	static class Size3 {
		static final int[][] DATA = {
				{0, 1, 2},
				{0, 2, 1},
				{1, 0, 2},
				{1, 2, 0},
				{2, 0, 1},
				{2, 1, 0},
		};
		static final int[][] SKIPS = {
				{ 2,  1,  1},
				{ 2,  2,  2},
				{ 4,  3,  3},
				{ 4,  4,  4},
				{-1,  5,  5},
				{-1, -1, -1},
		};
	}

	static class Size4 {
		static final int[][] DATA = {
				{0, 1, 2, 3},
				{0, 1, 3, 2},
				{0, 2, 1, 3},
				{0, 2, 3, 1},
				{0, 3, 1, 2},
				{0, 3, 2, 1},
				{1, 0, 2, 3},
				{1, 0, 3, 2},
				{1, 2, 0, 3},
				{1, 2, 3, 0},
				{1, 3, 0, 2},
				{1, 3, 2, 0},
				{2, 0, 1, 3},
				{2, 0, 3, 1},
				{2, 1, 0, 3},
				{2, 1, 3, 0},
				{2, 3, 0, 1},
				{2, 3, 1, 0},
				{3, 0, 1, 2},
				{3, 0, 2, 1},
				{3, 1, 0, 2},
				{3, 1, 2, 0},
				{3, 2, 0, 1},
				{3, 2, 1, 0},
		};
		static final int[][] SKIPS = {
				{6, 2, 1, 1},
				{6, 2, 2, 2},
				{6, 4, 3, 3},
				{6, 4, 4, 4},
				{6, 6, 5, 5},
				{6, 6, 6, 6},
				{12, 8, 7, 7},
				{12, 8, 8, 8},
				{12, 10, 9, 9},
				{12, 10, 10, 10},
				{12, 12, 11, 11},
				{12, 12, 12, 12},
				{18, 14, 13, 13},
				{18, 14, 14, 14},
				{18, 16, 15, 15},
				{18, 16, 16, 16},
				{18, 18, 17, 17},
				{18, 18, 18, 18},
				{-1, 20, 19, 19},
				{-1, 20, 20, 20},
				{-1, 22, 21, 21},
				{-1, 22, 22, 22},
				{-1, -1, 23, 23},
				{-1, -1, -1, -1},
		};
	}
}