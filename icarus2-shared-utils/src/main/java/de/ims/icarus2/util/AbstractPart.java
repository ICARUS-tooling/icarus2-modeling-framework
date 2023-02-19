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
package de.ims.icarus2.util;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractPart<O extends Object> implements Part<O> {

	private O owner;

	protected O getOwner() {
		return owner;
	}

	protected void checkAdded() {
		if(!isAdded())
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE,
					"No owner set");
	}

	/**
	 * @see de.ims.icarus2.util.Part#addNotify(java.lang.Object)
	 */
	@Override
	public void addNotify(O owner) {
		requireNonNull(owner);
		if(this.owner!=null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE,
					"Owner of part already set");

		this.owner = owner;
	}

	/**
	 * @see de.ims.icarus2.util.Part#removeNotify(java.lang.Object)
	 */
	@Override
	public void removeNotify(O owner) {
		requireNonNull(owner);
		if(this.owner==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE,
					"Owner of part not yet set");
		if(this.owner!=owner)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT,
					"Cannot remove foreign owner");

		this.owner = null;
	}

	/**
	 * @see de.ims.icarus2.util.Part#isAdded()
	 */
	@Override
	public boolean isAdded() {
		return owner!=null;
	}

}
