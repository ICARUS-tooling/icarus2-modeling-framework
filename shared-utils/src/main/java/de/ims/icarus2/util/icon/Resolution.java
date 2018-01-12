/**
 *
 */
package de.ims.icarus2.util.icon;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.lang.Primitives._int;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public final class Resolution {

	private final int width, height;

	private Resolution(int width, int height) {
		checkArgument("Width cannot be negative", width>0);
		checkArgument("Height cannot be negative", height>0);

		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public int hashCode() {
		return width + 31*height;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof Resolution) {
			Resolution other = (Resolution) obj;
			return width==other.width && height==other.height;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("Resolution@[width=%d, height=%d]", _int(width), _int(height));
	}

	private static final Long2ObjectMap<Resolution> cache = new Long2ObjectOpenHashMap<>();

	private static Resolution getCachedOrCreate(int width, int height, boolean mayCache) {

		long key = (long)width<<32 + height;

		synchronized (cache) {
			Resolution res = cache.get(key);

			if(res==null) {
				res = new Resolution(width, height);

				if(mayCache) {
					cache.put(key, res);
				}
			}

			return res;
		}
	}

	/**
	 * Returns a quadratic {@literal Resolution} with the given size.
	 * This will usually access the internal cache to not create new
	 * instances.
	 *
	 * @param size
	 * @return
	 */
	public static Resolution forSize(int size) {
		return forSize(size, size);
	}

	/**
	 * Returns a {@link Resolution} object with custom {@code width}
	 * and {@code height}.The method might returned a cached instance
	 * but will usually only do so for quadratic resolutions.
	 *
	 * @param width
	 * @param height
	 * @return
	 */
	public static Resolution forSize(int width, int height) {
		// Allow caching only for quadratic resolutions
		return getCachedOrCreate(width, height, width==height);
	}
}
