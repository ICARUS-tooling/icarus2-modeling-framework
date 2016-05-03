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

 * $Revision: 398 $
 * $Date: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/layer/Dependency.java $
 *
 * $LastChangedDate: 2015-05-29 11:29:49 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 398 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.layer;

/**
 * @author Markus Gärtner
 * @version $Id: Dependency.java 398 2015-05-29 09:29:49Z mcgaerty $
 *
 */
public final class Dependency<E extends Object> {

	private final E target;
	private final DependencyType type;

	public Dependency(E target, DependencyType type) {
		if (target == null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$
		if (type == null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$

		this.target = target;
		this.type = type;
	}

	public E getTarget() {
		return target;
	}

	public DependencyType getType() {
		return type;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return target.hashCode()*type.hashCode();
	}

	/**
	 * Since a {@code Dependency} instance can only be created for live
	 * objects of a corpus, this method performs identity checks on both the
	 * {@code target} and {@code type} between this dependency and the
	 * given {@code obj} parameter (only in case the parameter actually is
	 * another {@code Dependency}).
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Dependency) {
			Dependency<?> other = (Dependency<?>) obj;
			return target==other.target && type==other.type;
		}

		return false;
	}
}
