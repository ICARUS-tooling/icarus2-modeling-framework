/**
 *
 */
package de.ims.icarus2.util.collections;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.annotations.VisibleForTesting;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

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

    /** Condition for waiting reads */
    private final Condition notEmpty;

    /** Condition for waiting writes */
    private final Condition notFull;

    private volatile boolean closed;

    public BlockingLongBatchQueue(int capacity) {
    	this(capacity, false);
    }

    public BlockingLongBatchQueue(int capacity, boolean fair) {
    	checkArgument("Capacity must be positive: "+capacity, capacity>0);

    	// No defensive copying here
    	items = new long[capacity];

    	lock = new ReentrantLock(fair);
    	notEmpty = lock.newCondition();
    	notFull = lock.newCondition();
    }

    private void checkClosed() {
    	if(closed)
    		throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Queue already closed");
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws InterruptedException {
    	closed = true;
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
	    	notEmpty.signalAll();
	    	notFull.signalAll();
		} finally {
			lock.unlock();
		}
    }

    /** Circularly decrement i. */
    final int dec(int i) {
        return ((i == 0) ? items.length : i) - 1;
    }

    /** Calculates remaining number of values that can be written to buffer without blocking */
    private int remaining() {
    	return items.length-count;
    }

    /** Calculates remaining number of values to read from given buffer */
    private int remaining(long[] buffer, int offset) {
    	return buffer.length-offset;
    }

    /** */
    private int enqueue(long[] buffer, int offset) {
    	assert lock.getHoldCount() == 1;
        assert lock.isHeldByCurrentThread();
        assert count < items.length;

        int itemsToPut = Math.min(remaining(), remaining(buffer, offset));
        assert itemsToPut>0;

        final long[] items = this.items;
        int batchSize = Math.min(items.length-putIndex, itemsToPut);
        System.arraycopy(buffer, offset, items, putIndex, batchSize);

        putIndex += itemsToPut;
        if(putIndex >= items.length) {
        	putIndex -= items.length;
        	if(putIndex>0) {
                System.arraycopy(buffer, offset+batchSize, items, 0, putIndex);
        	}
        }
        count += itemsToPut;
        notEmpty.signal();

        return itemsToPut;
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

        int itemsToTake = Math.min(buffer.length, count);
        assert itemsToTake>0;

        // Copy up to 2 slices from items into buffer
        final long[] items = this.items;
        int batchSize = Math.min(items.length-takeIndex, itemsToTake);
        System.arraycopy(items, takeIndex, buffer, 0, batchSize);

        takeIndex += itemsToTake;
        if(takeIndex >= items.length) {
        	takeIndex -= items.length;
        	if(takeIndex>0) {
                System.arraycopy(items, 0, buffer, itemsToTake-takeIndex, takeIndex);
        	}
        }
        count -= itemsToTake;
        notFull.signal();

        return itemsToTake;
    }

    /** Remove and return value on head of queue */
    private long dequeue() {
    	assert lock.getHoldCount() == 1;
    	assert lock.isHeldByCurrentThread();
        assert count > 0;

        final long[] items = this.items;
    	long x = items[takeIndex];
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

    /**
     * Reads and returns a single value from this queue, blocking while no values are
     * available. If the queue gets {@link #close() closed} during the invocation of
     * this method, {@link -1} is returned.
     *
     * @return
     * @throws InterruptedException
     */
    public long read() throws InterruptedException {
    	final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0 && !closed) {
                notEmpty.await();
            }
            return count==0 ? UNSET_LONG : dequeue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a single value to this queue.
     * @param value
     * @return {@code true} iff this queue has not been {@link #close() closed} and the
     * given value has been added to the internal buffer.
     * @throws InterruptedException
     */
    public boolean write(long value) throws InterruptedException {
    	final ReentrantLock lock = this.lock;
    	lock.lockInterruptibly();
    	try {
    		while (count == items.length && !closed) {
    			notFull.await();
    		}
    		if(!closed) {
    			enqueue(value);
    			return true;
    		}
        	return false;
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
     * @return the number of elements read into the given {@code buffer} or {@code -1} iff
     * the queue was {@link #close() closed} during the invocation of this method.
     * @throws InterruptedException
     */
    public int read(long[] buffer) throws InterruptedException {
    	checkBuffer(buffer);

        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0 && !closed) {
                notEmpty.await();
            }
            return count==0 ? UNSET_INT : dequeue(buffer);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Writes a series of values to this queue, blocking if the internal buffer grows
     * full.
     * @param buffer
     * @return
     * @throws InterruptedException
     */
	public boolean write(long[] buffer) throws InterruptedException {
		checkBuffer(buffer);

		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			int offset = 0;
			while (offset < buffer.length && !closed) {
				while (count == items.length && !closed) {
					notFull.await();
				}
				if(closed) {
					return false;
				}
				offset += enqueue(buffer, offset);
			}
			return true;
		} finally {
			lock.unlock();
		}
	}
}
