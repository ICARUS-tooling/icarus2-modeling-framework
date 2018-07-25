/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

	private final UUID uuid;

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
