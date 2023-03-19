/**
 *
 */
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkArgument;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provides a blocking view on a {@code long} array that behaves like
 * {@link ArrayBlockingQueue} but offers specialized batch methods to
 * add or remove (large) chunks of values.
 * <p>
 * Note that this class does not implement any of the interfaces of
 * the collections or concurrency framework! It is meant as a specialized
 * buffer for parts of the ICARUS2 query framework to enable efficient
 * swapping of index values between threads/modules.
 *
 * @author Markus Gärtner
 *
 */
public class BlockingLongBatchArray implements AutoCloseable {

	public static final int MIN_CAPACITY = 10;

    /** The queued items */
    final long[] items;

    /** items index for next take, poll, peek or remove */
    int takeIndex;

    /** items index for next put, offer, or add */
    int putIndex;

    /** Number of elements in the queue */
    int count;

    /** Main lock guarding all access */
	@VisibleForTesting
    final ReentrantLock lock;

    /** Condition for waiting takes */
    private final Condition notEmpty;

    /** Condition for waiting puts */
    private final Condition notFull;

    public BlockingLongBatchArray(int capacity) {
    	this(capacity, false);
    }

    public BlockingLongBatchArray(int capacity, boolean fair) {
    	checkArgument("Capacity must be at least of size "+MIN_CAPACITY, capacity>=MIN_CAPACITY);

    	// No defensive copying here
    	items = new long[capacity];

    	lock = new ReentrantLock(fair);
    	notEmpty = lock.newCondition();
    	notFull = lock.newCondition();
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws InterruptedException {
    	// TODO Auto-generated method stub

    }

    /**  */
    private int dequeue(long[] buffer) {

    }

    /**
     * Tries to read values into the given {@code buffer}. If at least {@code 1} value is
     * available, this method will immediately copy as many values as possible into the
     * {@code buffer} and return the number of copied values. If the internal queue is empty,
     * the method will block until new values are available.
     *
     * @param buffer
     * @return the number
     * @throws InterruptedException
     */
    public int get(long[] buffer) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0)
                notEmpty.await();
            return dequeue(buffer);
        } finally {
            lock.unlock();
        }
    }
}
