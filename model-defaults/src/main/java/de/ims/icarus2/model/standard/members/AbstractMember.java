/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
