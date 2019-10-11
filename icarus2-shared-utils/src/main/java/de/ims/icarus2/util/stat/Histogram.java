/**
 *
 */
package de.ims.icarus2.util.stat;

/**
 * @author Markus Gärtner
 *
 */
public interface Histogram {

	/** Returns total number of bins in this histogram. */
	int bins();

	/** Returns the lower bound for values in the specified {@code bin}. */
	int lowerBound(int bin);

	/** Returns the lower bound for values in the specified {@code bin}. */
	int higherBound(int bin);

	/** Returns the total number of entries i nthe specified {@code bin}. */
	int entries(int bin);

	default int min() {
		return lowerBound(0);
	}

	default int max() {
		return higherBound(bins()-1);
	}

	//TODO add method for average and percentile retrieval
}
