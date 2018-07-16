/**
 *
 */
package de.ims.icarus2.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Markus
 *
 */
public class PoolTest {

	private Pool<Object> pool;

	private static final int CUSTOM_CAPACITY = 11;

	private void fill(int n) {
		Object[] tmp = new Object[n];

		for(int i=0; i<n; i++) {
			tmp[i] = pool.get();
		}

		for(int i=0; i<n; i++) {
			pool.recycle(tmp[i]);
		}
	}

	@BeforeEach
	public void prepare() {
		pool = new Pool<>(Object::new, CUSTOM_CAPACITY);
	}

	@Test
	public void testNullSupplier() throws Exception {
		Assertions.assertThrows(NullPointerException.class, () -> {
			pool = new Pool<>(null);
		});
	}

	@Test
	public void testEmpty() throws Exception {
		assertTrue(pool.isEmpty());
	}

	@Test
	public void testNullRecycle() throws Exception {
		Assertions.assertThrows(NullPointerException.class, () -> {
			pool.recycle(null);
		});
	}

	@Test
	public void testNegativeCapacity() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			pool = new Pool<>(Object::new, -999);
		});
	}

	@Test
	public void testFullBuffer() throws Exception {
		// Get item before pool gets filled
		Object item = pool.get();

		// Now fill to the brim
		fill(CUSTOM_CAPACITY);
		assertEquals(CUSTOM_CAPACITY, pool.size());

		// Try to recycle another item -> size mustn't change
		pool.recycle(item);
		assertEquals(CUSTOM_CAPACITY, pool.size());
	}

	@Test
	public void testItemPreservation() throws Exception {
		Object item1 = pool.get();

		pool.recycle(item1);

		Object item2 = pool.get();

		assertSame(item1, item2);
	}
}