/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.util.icon;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum StaticResolution {
	RES_SMALL(16),
	RES_30(30),
	RES_32(32),
	RES_64(64),
	RES_128(128),
	RES_256(256),
	RES_512(512),
	RES_1024(1024);

	private final int size;

	private StaticResolution(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public static StaticResolution forSize(int size) {
		switch (size) {
			case 16: return RES_SMALL;
			case 30: return RES_30;
			case 32: return RES_32;
			case 64: return RES_64;
			case 128: return RES_128;
			case 256: return RES_256;
			case 512: return RES_512;
			case 1024: return RES_1024;
		}

		throw new IllegalArgumentException("Unknown resolution: "+size);
	}

	public static StaticResolution getNextLarger(int size) {
		if(size<=16) {
			return RES_SMALL;
		} else if(size<=30) {
			return RES_30;
		} else if(size<=32) {
			return RES_32;
		} else if(size<=64) {
			return RES_64;
		} else if(size<=128) {
			return RES_128;
		} else if(size<=256) {
			return RES_256;
		} else if(size<=512) {
			return RES_512;
		} else if(size<=1024) {
			return RES_1024;
		} else
			throw new IllegalArgumentException("Unsupported size: "+size);
	}

	public static StaticResolution getNextSmaller(int size) {
		if(size>=512) {
			return RES_512;
		} else if(size>=256) {
			return RES_256;
		} else if(size>=128) {
			return RES_128;
		} else if(size>=64) {
			return RES_64;
		} else if(size>=32) {
			return RES_32;
		} else if(size>=30) {
			return RES_30;
		} else if(size>=16) {
			return StaticResolution.RES_SMALL;
		} else
			throw new IllegalArgumentException("Unsupported size: "+size);
	}

	//TODO make a forLabel(String) method that takes constants such as "default" "small" "medium" "large"...
}
