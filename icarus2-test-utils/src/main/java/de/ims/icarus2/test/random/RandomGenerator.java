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
package de.ims.icarus2.test.random;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Queue;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Unicodes;

/**
 * Wraps around a {@link Random} instance and provides various
 * convenience methods to obtain random data.
 * <p>
 * Note that this implementation does <b>not</b> provide direct access to the
 * underlying {@link Random}!
 *
 * @author Markus Gärtner
 *
 */
public class RandomGenerator implements Cloneable {

	/** A helper interface to model the source of a {@link RandomGenerator} */
	public interface RandomSource {
		RandomGenerator getRandom();
	}

	public static RandomGenerator random() {
		return new RandomGenerator(System.currentTimeMillis() ^ System.nanoTime());
	}

	public static RandomGenerator forSeed(long seed) {
		return new RandomGenerator(seed);
	}

	public static RandomGenerator forString(String source) {
		int len = source.length();
		assertTrue(len>=10, "Textual source of seed too small (needs at least 10 characters): "+source);
		// adapted from String.hashCode()
		long seed = 1125899906842597L; // prime

		for (int i = 0; i < len; i++) {
			seed = 31 * seed + source.charAt(i);
		}

		return forSeed(seed);
	}

	public static RandomGenerator forClass(Class<?> clazz) {
		return forString(clazz.getName());
	}

	public static RandomGenerator forExecutable(Executable exec) {
		return forString(exec.getDeclaringClass().getName()+"."+exec.getName());
	}

	private final Random random;
	private final long seed;

	private RandomGenerator(long seed) {
		this.seed = seed;
		random = new Random(seed);
	}

	/**
	 * Creates a new {@link RandomGenerator} that uses this generator's initial
	 * seed.
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public RandomGenerator clone() {
		return new RandomGenerator(seed);
	}

	public long getSeed() {
		return seed;
	}

	public RandomGenerator reset() {
		random.setSeed(seed);
		return this;
	}


	/** Random non-negative id (without loss of generality, Long.MAX_VALUE is excluded) */
	public long randomId() {
		return random(0, Long.MAX_VALUE);
	}

	// RANDOM PAIRS

	public Pair<Long, Long> randomLongPair(long lowerInc, long upperEx) {
		long[] vals = longs(2, lowerInc, upperEx).toArray();
		return Pair.pair(vals[0], vals[1]);
	}

	public Stream<Pair<Long, Long>> randomPairs(long lowerInc, long upperEx) {
		return Stream.generate(() -> randomLongPair(lowerInc, upperEx));
	}

	// RANDOM STRINGS

	private final String alNum =
			  "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
          + "0123456789"
          + "abcdefghijklmnopqrstuvxyz";

	/**
	 * Creates an alphanumerical random string of requested size.
	 * @param len
	 * @return
	 */
	public String randomString(int len) {
		char[] tmp = new char[len];
		for(int i=0; i<len; i++) {
			tmp[i] = alNum.charAt(nextInt(alNum.length()));
		}
		return new String(tmp);
	}

	/**
	 * Creates a random unicode string of requested size. Note that unlike
	 * {@link #randomString(int)} this method considers the {@code len} argument
	 * to define the number of <i>unicode codepoints</i> and as such the
	 * {@link String#length() length} of the returned string measured in characters
	 * might be up to two times that number.
	 * @param len
	 * @return
	 */
	public String randomUnicodeString(int len) {
		int[] tmp = new int[len];
		for(int i=0; i<len; i++) {
			// 50/50 chance of BMP vs supplementary codepoint
			if(nextBoolean()) {
				tmp[i] = Unicodes.randomBMPCodepoint(this);
			} else {
				tmp[i] = Unicodes.randomSupplementaryCodepoint(this);
			}
			tmp[i] = alNum.charAt(nextInt(alNum.length()));
		}
		return new String(tmp, 0, tmp.length);
	}

	// RANDOM STREAMS

	public <T> Stream<T> stream(Supplier<T> source) {
		return Stream.generate(source);
	}

	public Stream<String> stringStream(int strLen) {
		return stream(() -> randomString(strLen));
	}

	public Stream<String> stringStream(int strMinLenInclusive, int strMaxLenExclusive) {
		return IntStream.range(strMinLenInclusive, strMaxLenExclusive)
				.mapToObj(this::randomString);
	}

	// RANDOM values within bounds

	public int random(int minInclusive, int maxExclusive) {
		return minInclusive + nextInt(maxExclusive-minInclusive);
	}

	/**
	 * Very inefficient implementation
	 * @param minInclusive
	 * @param maxExclusive
	 * @return
	 */
	public long random(long minInclusive, long maxExclusive) {
		return randomLongs(1, minInclusive, maxExclusive)[0];
	}

	public double random(double minInclusive, double maxExclusive) {
		double range = maxExclusive-minInclusive;
		return minInclusive + (range * nextDouble());
	}

	// RANDOM ITERATORS (with filter)

	public PrimitiveIterator.OfInt randomIndices(int spectrum, int size) {
		return randomIntStream(spectrum).limit(size).iterator();
	}

	public IntStream randomIntStream(int spectrum) {
		int[] source = new int[spectrum];
		for (int i = 0; i < source.length; i++) {
			source[i] = i;
		}

		for (int i = 0; i < source.length; i++) {
			int x = random(0, spectrum);
			int tmp = source[i];
			source[i] = source[x];
			source[x] = tmp;
		}

		return IntStream.of(source);
	}

	/**
	 * Creates a series of non-empty sublists of the given {@code source}.
	 * The number of created sublists is defined by applying the specified
	 * {@code fraction} to the {@link List#size() size} of the soruce list.
	 *
	 * @param source
	 * @param fraction
	 * @return
	 */
	public <T> Stream<List<T>> randomSubLists(List<T> source, double fraction) {
		assertFalse(source.isEmpty());
		assertTrue(fraction>0.0 && fraction<=1.0);

		int part = Math.max(1, (int) (source.size() * fraction));

		return randomIntStream(source.size())
				.filter(i -> i>0)
				.limit(part)
				.mapToObj(i -> source.subList(0, i));
	}

	private final <T> Pair<Integer, Integer> bounds(List<T> source,
			@SuppressWarnings("unchecked") T...sentinels) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for(T sentinel : sentinels) {
			int index = source.indexOf(sentinel);
			assertTrue(index>-1);
			min = Math.min(min, index);
			max = Math.max(max, index);
		}
		assertTrue(min!=Integer.MAX_VALUE);
		assertTrue(max!=Integer.MIN_VALUE);

		return Pair.pair(min, max);
	}

	@SuppressWarnings("boxing")
	public <T> Stream<List<T>> randomSubLists(List<T> source, double fraction,
			@SuppressWarnings("unchecked") T...sentinels) {
		assertFalse(source.isEmpty());
		assertTrue(fraction>0.0 && fraction<=1.0);
		assertTrue(sentinels.length>0);

		// Bounds that need to be covered
		Pair<Integer, Integer> bounds = bounds(source, sentinels);
		assertTrue(bounds.first<=bounds.second);

		int part = Math.max(1, (int) (source.size() * fraction));

		return IntStream.generate(() -> random(0, bounds.first+1))
				.mapToObj(i -> Pair.pair(i, random(bounds.second, source.size())))
				.distinct()
				.limit(part)
				.map(b -> source.subList(b.first, b.second+1));
	}

	// RANDOM ARRAYS

	public Object[] randomContent(int size) {
		return IntStream.range(0, random(1, size+1))
				.mapToObj(i -> "item_"+i)
				.toArray();
	}

	public Object[] randomContent() {
		return IntStream.range(0, random(10, 100))
				.mapToObj(i -> "item_"+i)
				.toArray();
	}

	@SuppressWarnings("unchecked")
	public <T> T[] randomContent(Supplier<T> gen) {
		return (T[]) Stream.generate(gen)
				.limit(random(10, 100))
				.toArray();
	}

	public boolean[] randomBooleans(int size) {
		boolean[] array = new boolean[size];
		for (int i = 0; i < array.length; i++) {
			array[i] = nextBoolean();
		}
		return array;
	}

	public long[] randomLongs(int size, long min, long max) {
		return longs(size, min, max).toArray();
	}

	public int[] randomInts(int size, int min, int max) {
		return ints(size, min, max).toArray();
	}

	public short[] randomShorts(int size, short min, short max) {
		short[] array = new short[size];
		for (int i = 0; i < array.length; i++) {
			array[i] = (short) (nextInt(max-min)-min);
		}
		return array;
	}

	public byte[] randomBytes(int size, byte min, byte max) {
		byte[] array = new byte[size];
		for (int i = 0; i < array.length; i++) {
			array[i] = (byte) (nextInt(max-min)-min);
		}
		return array;
	}

	public float[] randomFloats(int size) {
		float[] array = new float[size];
		for (int i = 0; i < array.length; i++) {
			array[i] = nextFloat();
		}
		return array;
	}

	public double[] randomDoubles(int size) {
		return doubles().limit(size).toArray();
	}

	// RANDOMIZING OF INPUT

	public <T extends Object> Supplier<T> randomizer(Collection<? extends T> source) {
		requireNonNull(source);
		if(source.size()==1) {
			final T singleton = source.iterator().next();
			return () -> singleton;
		}

		Randomizer<T> randomizer = Randomizer.from(source);
		return randomizer::randomize;
	}

	public <T extends Object> Supplier<T> randomizer(
			@SuppressWarnings("unchecked") T...source) {
		requireNonNull(source);
		if(source.length==1) {
			return () -> source[0];
		}

		Randomizer<T> randomizer = Randomizer.from(source);
		return randomizer::randomize;
	}

	public <T extends Object> T random(@SuppressWarnings("unchecked") T...source) {
		requireNonNull(source);
		return source[nextInt(source.length)];
	}

	public <T extends Enum<T>> T random(Class<T> enumClass) {
		requireNonNull(enumClass);
		T[] values = enumClass.getEnumConstants();
		return random(values);
	}

	public <T extends Object> T random(List<? extends T> source) {
		requireNonNull(source);
		return source.get(nextInt(source.size()));
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T random(Collection<? extends T> source) {
		requireNonNull(source);
		return (T) random(source.toArray());
	}

	public <T> List<T> mix(@SuppressWarnings("unchecked") Queue<T>...input) {
		List<Queue<T>> sources = Stream.of(input)
				.filter(q -> !q.isEmpty())
				.collect(Collectors.toList());

		List<T> output = new ArrayList<>();

		while(!sources.isEmpty()) {
			int index = random(0, sources.size());
			Queue<T> queue = sources.get(index);
			output.add(queue.remove());
			if(queue.isEmpty()) {
				sources.remove(index);
			}
		}

		return output;
	}

	public void shuffle(int[] array) {
		for (int i = 0; i < array.length; i++) {
			int j = nextInt(array.length);
			int item0 = array[i];
			int item1 = array[j];
			array[i] = item1;
			array[j] = item0;
		}
	}

	public void shuffle(long[] array) {
		for (int i = 0; i < array.length; i++) {
			int j = nextInt(array.length);
			long item0 = array[i];
			long item1 = array[j];
			array[i] = item1;
			array[j] = item0;
		}
	}

	public <T> void shuffle(T[] array) {
		for (int i = 0; i < array.length; i++) {
			int j = nextInt(array.length);
			T item0 = array[i];
			T item1 = array[j];
			array[i] = item1;
			array[j] = item0;
		}
	}

	public <T> void shuffle(List<T> list) {
		for (int i = 0; i < list.size(); i++) {
			int j = nextInt(list.size());
			T item0 = list.get(i);
			T item1 = list.get(j);
			list.set(i, item1);
			list.set(j, item0);
		}
	}

	// DELEGATE METHODS

	public void nextBytes(byte[] bytes) {
		random.nextBytes(bytes);
	}

	public int nextInt() {
		return random.nextInt();
	}

	public int nextInt(int bound) {
		return random.nextInt(bound);
	}

	public long nextLong() {
		return random.nextLong();
	}

	public boolean nextBoolean() {
		return random.nextBoolean();
	}

	public float nextFloat() {
		return random.nextFloat();
	}

	public double nextDouble() {
		return random.nextDouble();
	}

	public double nextGaussian() {
		return random.nextGaussian();
	}

	public IntStream ints(long streamSize) {
		return random.ints(streamSize);
	}

	public IntStream ints() {
		return random.ints();
	}

	public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
		return random.ints(streamSize, randomNumberOrigin, randomNumberBound);
	}

	public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
		return random.ints(randomNumberOrigin, randomNumberBound);
	}

	public LongStream longs(long streamSize) {
		return random.longs(streamSize);
	}

	public LongStream longs() {
		return random.longs();
	}

	public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
		return random.longs(streamSize, randomNumberOrigin, randomNumberBound);
	}

	public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
		return random.longs(randomNumberOrigin, randomNumberBound);
	}

	public DoubleStream doubles(long streamSize) {
		return random.doubles(streamSize);
	}

	public DoubleStream doubles() {
		return random.doubles();
	}

	public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
		return random.doubles(streamSize, randomNumberOrigin, randomNumberBound);
	}

	public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
		return random.doubles(randomNumberOrigin, randomNumberBound);
	}
}
