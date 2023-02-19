/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.io.resource;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractIOResource implements IOResource {

	private final AccessMode accessMode;
	private final Path path;

	protected AbstractIOResource(Path path, AccessMode accessMode) {
		this.path = requireNonNull(path);
		this.accessMode = requireNonNull(accessMode);
	}

	protected IcarusRuntimeException forMissingWriteAccess() {
		return new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "No write access specified for resource: "+this);
	}

	protected IcarusRuntimeException forMissingReadAccess() {
		return new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "No read access specified for resource: "+this);
	}

	protected void checkWriteAccess() {
		if(!accessMode.isWrite())
			throw forMissingWriteAccess();
	}

	protected void checkReadAccess() {
		if(!accessMode.isRead())
			throw forMissingReadAccess();
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getAccessMode()
	 */
	@Override
	public AccessMode getAccessMode() {
		return accessMode;
	}

	/**
	 * @see de.ims.icarus2.util.io.resource.IOResource#getPath()
	 */
	@Override
	public Path getPath() {
		return path;
	}
}
