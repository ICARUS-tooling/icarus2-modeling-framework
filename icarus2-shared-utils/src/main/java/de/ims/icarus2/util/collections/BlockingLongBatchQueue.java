/**
 *
 */
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

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
public class BlockingLongBatchQueue implements AutoCloseable {

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

    public BlockingLongBatchQueue(int capacity) {
    	this(capacity, false);
    }

    public BlockingLongBatchQueue(int capacity, boolean fair) {
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

    /** Circularly decrement i. */
    final int dec(int i) {
        return ((i == 0) ? items.length : i) - 1;
    }

    private int remaining() {
    	return items.length-count;
    }

    private int remaining(long[] buffer, int offset) {
    	return buffer.length-offset;
    }

    /** */
    private int enqueue(long[] buffer, int offset) {
    	assert lock.getHoldCount() == 1;
        assert lock.isHeldByCurrentThread();
        assert count < items.length;

        int itemsToQueue = Math.min(remaining(), remaining(buffer, offset));
        assert itemsToQueue>0;

        int batchSize = Math.min(items.length-putIndex, itemsToQueue);
        System.arraycopy(buffer, offset, items, putIndex, batchSize);

        putIndex += itemsToQueue;
        if(putIndex >= items.length) {
        	putIndex -= items.length;
        	if(putIndex>0) {
                System.arraycopy(buffer, offset+batchSize, items, 0, putIndex);
        	}
        }

        return itemsToQueue;
    }

    /** */
    private void enqueue(long value) {
    	assert lock.getHoldCount() == 1;
        assert lock.isHeldByCurrentThread();
        assert count < items.length;

        final long[] items = this.items;
        items[putIndex] = value;
        if (++putIndex == items.length)
            putIndex = 0;
        count++;
        notEmpty.signal();
    }

	/**  */
    private int dequeue(long[] buffer) {
    	assert lock.getHoldCount() == 1;
        assert lock.isHeldByCurrentThread();
        assert count > 0;

        // Copy up to 2 slices from items into buffer
        final long[] items = this.items;
        @SuppressWarnings("unchecked")
        E x = (E) items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == items.length)
            takeIndex = 0;
        count--;
        notFull.signal();
        return x;
    }

    /** Remove and return value on head of queue */
    private long dequeue() {
    	assert lock.getHoldCount() == 1;
    	assert lock.isHeldByCurrentThread();
        assert count > 0;

        final long[] items = this.items;
    	long x = items[takeIndex];
    	items[takeIndex] = UNSET_LONG;
    	if (++takeIndex == items.length)
    		takeIndex = 0;
    	count--;
    	notFull.signal();
    	return x;
    }

    private static void checkBuffer(long[] buffer) {
    	requireNonNull(buffer);
    	checkArgument("Buffer must not be empty", buffer.length>0);
    }

    public long read() throws InterruptedException {
    	final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public void write(long value) throws InterruptedException {
    	final ReentrantLock lock = this.lock;
    	lock.lockInterruptibly();
    	try {
    		while (count == items.length) {
    			notFull.await();
    		}
    		enqueue(value);
    	} finally {
    		lock.unlock();
    	}
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
    public int read(long[] buffer) throws InterruptedException {
    	checkBuffer(buffer);

        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return dequeue(buffer);
        } finally {
            lock.unlock();
        }
    }

	public void write(long[] buffer) throws InterruptedException {
		checkBuffer(buffer);

		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			int offset = 0;
			while (offset < buffer.length) {
				while (count == items.length) {
					notFull.await();
				}
				offset += enqueue(buffer, offset);
			}
		} finally {
			lock.unlock();
		}
	}
}
