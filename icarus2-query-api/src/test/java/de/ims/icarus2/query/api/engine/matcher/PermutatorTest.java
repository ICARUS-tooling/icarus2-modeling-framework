/**
 *
 */
package de.ims.icarus2.query.api.engine.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.query.api.QueryErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
class PermutatorTest {

	@Nested
	class ForFactoryMethod {

		@ParameterizedTest
		@ValueSource(ints = {0, 1, -1})
		public void testSmallSize(int size) throws Exception {
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
					() -> Permutator.forSize(size));
		}

		@ParameterizedTest
		@ValueSource(ints = {Permutator.MAX_SIZE+1, Integer.MAX_VALUE})
		public void testLargeSize(int size) throws Exception {
			assertThatExceptionOfType(QueryException.class).isThrownBy(
					() -> Permutator.forSize(size)).satisfies(
							ex -> assertThat(ex.getErrorCode()).isSameAs(QueryErrorCode.INTRACTABLE_CONSTRUCT));
		}
	}

	private static void assertBasicPermutation(int[] data) {
		for (int i = 0; i < data.length; i++) {
			assertThat(data[i]).as("permutation mismatch at index "+i).isEqualTo(i);
		}
	}

	private static void assertNotBasicPermutation(int[] data) {
		for (int i = 0; i < data.length; i++) {
			if(data[i] != i) {
				return;
			}
		}

		throw new AssertionError("Supplied permutation is basic (i.e. contains only sorted elements)");
	}

	private static int countPermutations(int size) {
		int result = 1;
		for (int i = 1; i <=size; i++) {
			result *= i;
		}
		return result;
	}

	interface TestBase {
		Permutator create();

		@Test
		@Disabled
		default void dump() {
			Permutator p = create();
			int count = 0;
			do {
				count++;
				System.out.println(Arrays.toString(p.current()));
			} while(p.next());
			assertThat(count).isEqualTo(countPermutations(p.size()));
		}

		@Test
		default void testCount() {
			Permutator p = create();
			int count = 0;
			do { count++; } while(p.next());
			assertThat(count).isEqualTo(countPermutations(p.size()));
		}

		@Test
		default void testCurrent() {
			Permutator p = create();
			int size = p.size();
			do {
				assertThat(p.current()).isNotNull().hasSize(size);
			} while(p.next());
		}

		@Test
		default void testNext() {
			Permutator p = create();
			int size = countPermutations(p.size());
			while(--size > 0) {
				assertThat(p.next()).as("early stop - missing elements: "+size).isTrue();
			}

			assertThat(p.next()).isFalse();
		}

		@Test
		default void testInitial() {
			Permutator p = create();
			assertBasicPermutation(p.current());
		}

		@Test
		@RandomizedTest
		default void testReset(RandomGenerator rng) {
			Permutator p = create();

			// Single step check
			assertThat(p.next()).isTrue();
			assertNotBasicPermutation(p.current());
			p.reset();
			assertBasicPermutation(p.current());

			// Simulate random progress
			int size = countPermutations(p.size());
			if(size>2) {
				int steps = Math.max(2, rng.nextInt(size / 2) + 1);
				while(--steps > 0) {
					p.next();
				}
				assertNotBasicPermutation(p.current());
				p.reset();
				assertBasicPermutation(p.current());
			}
		}

		@Test
		default void testOrder() {
			Permutator p = create();
			List<String> buffer = new ObjectArrayList<>(countPermutations(p.size()));
			do {
				buffer.add(Arrays.toString(p.current()));
			} while(p.next());

			assertThat(buffer).isSorted();
		}

		//TODO add test for skip(slot)
	}

	@Nested
	class ForManualLookups {

		@Nested
		class ForSize2 implements TestBase {
			@Override
			public Permutator create() { return Permutator.prepared(2); }
		}

		@Nested
		class ForSize3 implements TestBase {
			@Override
			public Permutator create() { return Permutator.prepared(3); }
		}

		@Nested
		class ForSize4 implements TestBase {
			@Override
			public Permutator create() { return Permutator.prepared(4); }
		}
	}

	@Nested
	class ForDynamic {

		@Nested
		class ForSize5 implements TestBase {
			@Override
			public Permutator create() { return Permutator.dynamic(5); }
		}

	}
}
