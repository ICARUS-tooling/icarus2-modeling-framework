/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members;

import de.ims.icarus2.util.Recyclable;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Primitive;

/**
 * @author Markus Gärtner
 *
 */
@Assessable
public class AbstractMember implements Recyclable {

	@Primitive
	private int flags = MemberFlags.EMPTY_FLAGS;

	protected int getFlags() {
		return flags;
	}

	protected void setFlags(int flags) {
		this.flags = flags;
	}

	protected boolean isFlagSet(int flag) {
		return (flags & flag) == flag;
	}

	protected void setFlag(int flag, boolean active) {
		flags = (active ? (flags|flag) : (flags & ~flag));
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		flags = MemberFlags.EMPTY_FLAGS;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return true;
	}
}
