/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.filedriver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Implements a simple 16 bytes checksum to encapsulate the last modification time
 * and size of a file. Since reading in an entire file to compute an actual checksum
 * is way to expensive for large files, those 2 informations are considered enough
 * to make a very quick check whether or not a file has changed since the last time it
 * was accessed by the framework.
 * <p>
 * The checksum internally uses an {@link UUID} object to simplify storage and serialization.
 *
 * @author Markus Gärtner
 *
 */
public class FileChecksum implements Serializable {

	private static final long serialVersionUID = -2035971325487167347L;

	private final UUID uuid;

	public static FileChecksum compute(Path file) throws IOException {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$
		if(Files.notExists(file))
			throw new FileNotFoundException("File does not exist: "+file); //$NON-NLS-1$

		long timestamp = Files.getLastModifiedTime(file).toMillis();
		long size = Files.size(file);

		return new FileChecksum(timestamp, size);
	}

	public static FileChecksum compute(long timestamp, long size) {
		if(timestamp<0)
			throw new IllegalArgumentException("Timestamp cannot be negative: "+timestamp); //$NON-NLS-1$
		if(size<0)
			throw new IllegalArgumentException("Size cannot be negative: "+size); //$NON-NLS-1$

		return new FileChecksum(timestamp, size);
	}

	public static FileChecksum parse(String s) {

		UUID uuid = UUID.fromString(s);

		return new FileChecksum(uuid);
	}

	private FileChecksum(UUID uuid) {
		this.uuid = uuid;
	}

	public FileChecksum(long timestamp, long size) {
		uuid = new UUID(timestamp, size);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof FileChecksum) {
			FileChecksum other = (FileChecksum)obj;
			return uuid.equals(other.uuid);
		}
		return false;
	}

	/**
	 * Converts this checksum in a {@code String} representation by
	 * delegating to {@link UUID#toString()} on the internal {@code uuid}
	 * field.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return uuid.toString();
	}


}
